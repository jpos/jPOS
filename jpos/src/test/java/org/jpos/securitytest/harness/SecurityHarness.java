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

package org.jpos.securitytest.harness;

import org.jpos.iso.ISOUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class SecurityHarness {
    private SecurityHarness() {
    }

    public static List<Mutation> mutations(
      SecurityTarget target, CorpusEntry input, long baseSeed, MutationBudget budget)
    {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(budget, "budget");

        List<InputMutator> mutators = target.mutators().stream()
          .sorted(Comparator.comparing(InputMutator::id))
          .toList();
        List<Mutation> mutations = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (InputMutator mutator : mutators) {
            int remaining = budget.maxCases() - mutations.size();
            if (remaining == 0)
                break;
            long seed = DeterministicSeeds.derive(baseSeed, target.id(), input.id(), mutator.id());
            MutationBudget remainingBudget = new MutationBudget(remaining, budget.maxInputBytes());
            List<Mutation> generated = mutator.mutate(input, seed, remainingBudget);
            if (generated.size() > remaining)
                throw new IllegalArgumentException("Mutator " + mutator.id() + " exceeded its case budget");
            for (Mutation mutation : generated) {
                if (mutation.bytes().length > budget.maxInputBytes())
                    throw new IllegalArgumentException("Mutation " + mutation.id() + " exceeded its input budget");
                String id = mutator.id() + "/" + mutation.id();
                if (!ids.add(id))
                    throw new IllegalArgumentException("Duplicate mutation id " + id);
                mutations.add(new Mutation(id, mutation.seed(), mutation.bytes()));
            }
        }
        return List.copyOf(mutations);
    }

    public static void verify(
      SecurityTarget target, CorpusEntry input, long baseSeed, MutationBudget budget)
    {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(budget, "budget");

        long corpusSeed = DeterministicSeeds.derive(baseSeed, target.id(), input.id(), "corpus");
        verifyCase(target, input, "corpus", baseSeed, corpusSeed, input.bytes(), input.expectation());
        for (Mutation mutation : mutations(target, input, baseSeed, budget)) {
            verifyCase(
              target, input, mutation.id(), baseSeed, mutation.seed(), mutation.bytes(),
              Expectation.ACCEPT_OR_REJECT
            );
        }
    }

    private static void verifyCase(
      SecurityTarget target, CorpusEntry input, String caseId, long baseSeed, long caseSeed,
      byte[] bytes, Expectation expectation)
    {
        try {
            target.consume(bytes);
        } catch (AssertionError failure) {
            throw failure(target, input, caseId, baseSeed, caseSeed, bytes, expectation,
              "unexpected failure", failure);
        } catch (Error fatal) {
            throw fatal;
        } catch (Throwable failure) {
            boolean expectedRejection = failure instanceof Exception && target.isExpectedRejection(failure);
            if (expectation != Expectation.ACCEPT && expectedRejection)
                return;
            throw failure(target, input, caseId, baseSeed, caseSeed, bytes, expectation,
              expectedRejection ? "unexpected rejection" : "unexpected failure", failure);
        }
        if (expectation == Expectation.REJECT) {
            throw failure(target, input, caseId, baseSeed, caseSeed, bytes, expectation,
              "input was accepted", null);
        }
    }

    private static AssertionError failure(
      SecurityTarget target, CorpusEntry input, String caseId, long baseSeed, long caseSeed,
      byte[] bytes, Expectation expectation, String outcome, Throwable cause)
    {
        String message = String.format(
          "Security case failed [target=%s, corpus=%s, case=%s, baseSeed=0x%016X, " +
            "caseSeed=0x%016X, expectation=%s, input=%s]: %s",
          target.id(), input.id(), caseId, baseSeed, caseSeed, expectation,
          ISOUtil.hexString(bytes), outcome
        );
        return cause == null ? new AssertionError(message) : new AssertionError(message, cause);
    }
}
