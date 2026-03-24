/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2026 jPOS Software SRL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.transaction.participant;

import org.jdom2.Element;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.TransactionConstants;
import org.jpos.transaction.TransactionManager;
import org.jpos.transaction.TransactionParticipant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

/**
 * Runs a set of {@link TransactionParticipant}s in parallel using
 * Java 26 Structured Concurrency ({@link StructuredTaskScope}).
 *
 * <p>Requires Java 26+. {@code StructuredTaskScope} is a preview API in Java 21–25
 * and graduates as a standard API in Java 26. This branch is parked until jPOS
 * moves its baseline to Java 26.</p>
 *
 * <p>Compared to the previous {@code Runner}-based implementation:</p>
 * <ul>
 *   <li>The {@code Runner} inner class is eliminated entirely.</li>
 *   <li>Subtask lifetimes are strictly bounded by the enclosing scope —
 *       no subtask can outlive {@code prepare()}, {@code commit()}, or {@code abort()}.</li>
 *   <li>Exceptions thrown by participants are propagated to the caller via
 *       {@code throwIfFailed()} rather than being silently swallowed.</li>
 *   <li>The {@code NO_JOIN} flag from {@code prepare} is preserved per-transaction
 *       and honoured correctly in {@code commit}/{@code abort}.</li>
 * </ul>
 */
public class Join implements TransactionConstants, AbortParticipant, XmlConfigurable {

    private TransactionManager tm;
    private final List<TransactionParticipant> participants = new ArrayList<>();

    /**
     * Stores the prepare result per participant per transaction id,
     * so that the NO_JOIN flag is correctly honoured in commit/abort.
     */
    private final Map<Long, Map<TransactionParticipant, Integer>> prepareResults =
        new ConcurrentHashMap<>();

    @Override
    public int prepare(long id, Serializable o) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<int[]>> subtasks = participants.stream()
                .map(p -> scope.fork(() -> new int[]{ participants.indexOf(p), p.prepare(id, o) }))
                .toList();

            scope.join().throwIfFailed();

            Map<TransactionParticipant, Integer> results = new ConcurrentHashMap<>();
            int[] actions = new int[participants.size()];
            for (var subtask : subtasks) {
                int[] result = subtask.get();
                int idx    = result[0];
                int action = result[1];
                results.put(participants.get(idx), action);
                actions[idx] = action;
            }
            prepareResults.put(id, results);
            return mergeActions(actions);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ABORTED;
        } catch (ExecutionException e) {
            return ABORTED;
        }
    }

    @Override
    public int prepareForAbort(long id, Serializable o) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<TransactionParticipant> abortParticipants = participants.stream()
                .filter(p -> p instanceof AbortParticipant)
                .toList();

            List<StructuredTaskScope.Subtask<int[]>> subtasks = abortParticipants.stream()
                .map(p -> scope.fork(() ->
                    new int[]{ abortParticipants.indexOf(p),
                               ((AbortParticipant) p).prepareForAbort(id, o) }))
                .toList();

            scope.join().throwIfFailed();

            int[] actions = new int[abortParticipants.size()];
            for (var subtask : subtasks) {
                int[] result = subtask.get();
                actions[result[0]] = result[1];
            }
            return mergeActions(actions);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ABORTED;
        } catch (ExecutionException e) {
            return ABORTED;
        }
    }

    @Override
    public void commit(long id, Serializable o) {
        Map<TransactionParticipant, Integer> results = prepareResults.remove(id);
        List<TransactionParticipant> targets = participants.stream()
            .filter(p -> results == null || (results.getOrDefault(p, 0) & NO_JOIN) == 0)
            .toList();
        runAll(targets, p -> p.commit(id, o));
    }

    @Override
    public void abort(long id, Serializable o) {
        Map<TransactionParticipant, Integer> results = prepareResults.remove(id);
        List<TransactionParticipant> targets = participants.stream()
            .filter(p -> results == null || (results.getOrDefault(p, 0) & NO_JOIN) == 0)
            .toList();
        runAll(targets, p -> p.abort(id, o));
    }

    @Override
    public void setConfiguration(Element e) throws ConfigurationException {
        for (Element element : e.getChildren("participant")) {
            participants.add(tm.createParticipant(element));
        }
    }

    @Override
    public void setTransactionManager(TransactionManager mgr) {
        this.tm = mgr;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void runAll(List<TransactionParticipant> targets, ParticipantAction action) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            targets.forEach(p -> scope.fork(() -> { action.run(p); return null; }));
            scope.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int mergeActions(int[] actions) {
        boolean prepared = true;
        boolean readonly = true;
        boolean no_join  = true;
        for (int action : actions) {
            if ((action & RETRY) == RETRY)
                return RETRY;
            if ((action & PREPARED) == ABORTED)
                prepared = false;
            if ((action & READONLY) != READONLY)
                readonly = false;
            if ((action & NO_JOIN) != NO_JOIN)
                no_join  = false;
        }
        return (prepared ? PREPARED : ABORTED)
             | (no_join  ? NO_JOIN  : 0)
             | (readonly ? READONLY : 0);
    }

    @FunctionalInterface
    private interface ParticipantAction {
        void run(TransactionParticipant p) throws Exception;
    }
}
