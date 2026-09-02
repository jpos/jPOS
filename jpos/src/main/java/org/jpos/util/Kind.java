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

package org.jpos.util;

import org.jpos.log.AuditLogEvent;
import org.jpos.log.AuditLogEventProvider;
import org.jpos.log.AuditLogEventRegistry;
import org.jpos.log.AuditLogEventType;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Registered log-event kinds and their severity families.
 *
 * <p>A {@link LogEvent}'s tag is its <em>kind</em>: what happened. Kinds
 * sit between two other levels:</p>
 * <ul>
 *   <li><b>family</b> — a coarse severity projection ({@link Family}) used
 *       for row colouring and alerting; every kind maps to exactly one;</li>
 *   <li><b>type</b> — the {@code t} discriminator of an {@link AuditLogEvent}
 *       payload, registered through {@link AuditLogEventRegistry}. A type
 *       may imply a kind; many types share one kind (for example
 *       {@code connect}, {@code listen} and {@code session-start} all land
 *       in {@link #ISO_SESSION}).</li>
 * </ul>
 *
 * <p>Core registers only the kinds it emits. jPOS-EE modules and
 * applications register their own through
 * {@link AuditLogEventProvider#kinds()} and by declaring the implied kind
 * in their {@link AuditLogEventType}s. Prefer an existing kind and a new
 * type over a new kind, and an existing family over a new one: facets and
 * viewers enumerate these.</p>
 *
 * <p>Ad-hoc kinds that are not registered anywhere should be dot-namespaced
 * ({@code jcard.pin-change}) so log consumers can collapse them by prefix.
 * Nothing in core rewrites or rejects a tag; validation is the log
 * consumer's job.</p>
 *
 * @since 3.0.0
 */
public final class Kind {
    /** Maximum kind length. */
    public static final int MAX_LENGTH = 48;

    /** Severity families. Modules may register further ones; keep the set small. */
    public static final class Family {
        /** The {@code fail} family. */
        public static final String FAIL = "fail";
        /** The {@code warn} family. */
        public static final String WARN = "warn";
        /** The {@code commit} family. */
        public static final String COMMIT = "commit";
        /** The {@code abort} family. */
        public static final String ABORT = "abort";
        /** The {@code transport} family. */
        public static final String TRANSPORT = "transport";
        /** The {@code telemetry} family. */
        public static final String TELEMETRY = "telemetry";
        /** The {@code security} family. */
        public static final String SECURITY = "security";
        /** Family for kinds that are unknown, unregistered or blank. */
        public static final String NEUTRAL = "neutral";

        private Family() { }
    }

    /**
     * A kind registration: name plus family.
     *
     * @param name   kind, must satisfy {@link Kind#isValid(String)}
     * @param family family, lowercase kebab
     */
    public record Def(String name, String family) {
        /** Validates name and family. @throws IllegalArgumentException when either is malformed */
        public Def {
            if (!isValid(name))
                throw new IllegalArgumentException("invalid kind '" + name + "'");
            if (family == null || !FAMILY_RE.matcher(family).matches())
                throw new IllegalArgumentException("invalid family '" + family + "' for kind '" + name + "'");
        }
    }

    // fail
    /** The {@code error} kind. */
    public static final String ERROR = "error";
    /** The {@code fatal} kind. */
    public static final String FATAL = "fatal";
    /** The {@code session-error} kind. */
    public static final String SESSION_ERROR = "session-error";
    /** The {@code pack-error} kind. */
    public static final String PACK_ERROR = "pack-error";
    /** The {@code unpack-error} kind. */
    public static final String UNPACK_ERROR = "unpack-error";
    // warn
    /** The {@code warn} kind. */
    public static final String WARN = "warn";
    /** The {@code session-warning} kind. */
    public static final String SESSION_WARNING = "session-warning";
    // commit / abort
    /** The {@code commit} kind. */
    public static final String COMMIT = "commit";
    /** The {@code abort} kind. */
    public static final String ABORT = "abort";
    // transport
    /** The {@code iso-session} kind. */
    public static final String ISO_SESSION = "iso-session";
    /** The {@code send} kind. */
    public static final String SEND = "send";
    /** The {@code receive} kind. */
    public static final String RECEIVE = "receive";
    /** The {@code poll} kind. */
    public static final String POLL = "poll";
    /** The {@code usable} kind. */
    public static final String USABLE = "usable";
    // telemetry
    /** The {@code info} kind. */
    public static final String INFO = "info";
    /** The {@code debug} kind. */
    public static final String DEBUG = "debug";
    /** The {@code trace} kind. */
    public static final String TRACE = "trace";
    /** The {@code status} kind. */
    public static final String STATUS = "status";
    // security
    /** The {@code s-m-operation} kind. */
    public static final String S_M_OPERATION = "s-m-operation";
    /** The {@code jce-provider} kind. */
    public static final String JCE_PROVIDER = "jce-provider";
    /** The {@code local-master-keys} kind. */
    public static final String LOCAL_MASTER_KEYS = "local-master-keys";
    /** The {@code get-key} kind. */
    public static final String GET_KEY = "get-key";
    /** The {@code set-key} kind. */
    public static final String SET_KEY = "set-key";
    // neutral
    /** The {@code txn} kind. */
    public static final String TXN = "txn";
    /** The {@code lifecycle} kind. */
    public static final String LIFECYCLE = "lifecycle";
    /** The {@code deploy} kind. */
    public static final String DEPLOY = "deploy";
    /** The {@code prepare} kind. */
    public static final String PREPARE = "prepare";
    /** The {@code prepare-for-abort} kind. */
    public static final String PREPARE_FOR_ABORT = "prepare-for-abort";
    /** The {@code select} kind. */
    public static final String SELECT = "select";
    /** The {@code recover} kind. */
    public static final String RECOVER = "recover";
    /** The {@code config} kind. */
    public static final String CONFIG = "config";
    /** The {@code notify} kind. */
    public static final String NOTIFY = "notify";
    /** The {@code pack} kind. */
    public static final String PACK = "pack";
    /** The {@code unpack} kind. */
    public static final String UNPACK = "unpack";
    /** The {@code validate} kind. */
    public static final String VALIDATE = "validate";

    private static final Pattern KIND_RE   = Pattern.compile("^[a-z][a-z0-9-]*(\\.[a-z][a-z0-9-]*)*$");
    private static final Pattern FAMILY_RE = Pattern.compile("^[a-z][a-z0-9-]*$");

    private static final List<Def> BUILTINS = List.of(
      new Def(ERROR, Family.FAIL),
      new Def(FATAL, Family.FAIL),
      new Def(SESSION_ERROR, Family.FAIL),
      new Def(PACK_ERROR, Family.FAIL),
      new Def(UNPACK_ERROR, Family.FAIL),
      new Def(WARN, Family.WARN),
      new Def(SESSION_WARNING, Family.WARN),
      new Def(COMMIT, Family.COMMIT),
      new Def(ABORT, Family.ABORT),
      new Def(ISO_SESSION, Family.TRANSPORT),
      new Def(SEND, Family.TRANSPORT),
      new Def(RECEIVE, Family.TRANSPORT),
      new Def(POLL, Family.TRANSPORT),
      new Def(USABLE, Family.TRANSPORT),
      new Def(INFO, Family.TELEMETRY),
      new Def(DEBUG, Family.TELEMETRY),
      new Def(TRACE, Family.TELEMETRY),
      new Def(STATUS, Family.TELEMETRY),
      new Def(S_M_OPERATION, Family.SECURITY),
      new Def(JCE_PROVIDER, Family.SECURITY),
      new Def(LOCAL_MASTER_KEYS, Family.SECURITY),
      new Def(GET_KEY, Family.SECURITY),
      new Def(SET_KEY, Family.SECURITY),
      new Def(TXN, Family.NEUTRAL),
      new Def(LIFECYCLE, Family.NEUTRAL),
      new Def(DEPLOY, Family.NEUTRAL),
      new Def(PREPARE, Family.NEUTRAL),
      new Def(PREPARE_FOR_ABORT, Family.NEUTRAL),
      new Def(SELECT, Family.NEUTRAL),
      new Def(RECOVER, Family.NEUTRAL),
      new Def(CONFIG, Family.NEUTRAL),
      new Def(NOTIFY, Family.NEUTRAL),
      new Def(PACK, Family.NEUTRAL),
      new Def(UNPACK, Family.NEUTRAL),
      new Def(VALIDATE, Family.NEUTRAL)
    );

    private static volatile Map<String, String> families;

    private Kind() { }

    /**
     * Severity family for a kind.
     *
     * <p>Matching is case-insensitive. The legacy {@code warning} tag maps to
     * the {@link Family#WARN} family. Null, blank, unregistered and
     * namespaced kinds map to {@link Family#NEUTRAL}.</p>
     *
     * @param kind a kind, raw or normalized
     * @return the family; never {@code null}
     */
    public static String family(String kind) {
        if (kind == null || kind.isBlank())
            return Family.NEUTRAL;
        String k = kind.trim().toLowerCase(Locale.ROOT);
        if ("warning".equals(k))
            k = WARN;
        return load().getOrDefault(k, Family.NEUTRAL);
    }

    /**
     * Kind implied by a registered audit-event type id.
     *
     * @param type the {@code t} discriminator
     * @return the implied kind, or {@link #INFO} when the type is unknown or implies none
     */
    public static String kindOf(String type) {
        return kindOf(AuditLogEventRegistry.typeOf(type));
    }

    /**
     * Kind implied by an audit-event payload's registered type.
     *
     * @param evt the payload
     * @return the implied kind, or {@link #INFO} when its type is unknown or implies none
     */
    public static String kindOf(AuditLogEvent evt) {
        return kindOf(evt == null ? null : AuditLogEventRegistry.typeOf(evt.getClass()));
    }

    private static String kindOf(AuditLogEventType t) {
        return t == null || t.kind() == null ? INFO : t.kind();
    }

    /**
     * Whether a value is a well-formed kind: lowercase kebab segments,
     * optionally dot-namespaced, at most {@link #MAX_LENGTH} characters.
     *
     * @param kind candidate
     * @return true when well-formed
     */
    public static boolean isValid(String kind) {
        return kind != null && kind.length() <= MAX_LENGTH && KIND_RE.matcher(kind).matches();
    }

    /**
     * Whether a kind is registered by core or by a provider.
     *
     * @param kind candidate (exact match, no folding)
     * @return true when registered
     */
    public static boolean isRegistered(String kind) {
        return kind != null && load().containsKey(kind);
    }

    /**
     * Every registered kind.
     *
     * @return immutable snapshot of every registered kind
     */
    public static Set<String> registered() {
        return load().keySet();
    }

    private static Map<String, String> load() {
        Map<String, String> snapshot = families;
        if (snapshot != null)
            return snapshot;
        synchronized (Kind.class) {
            if (families != null)
                return families;
            Map<String, String> map = new LinkedHashMap<>();
            for (Def d : BUILTINS)
                register(map, d, "core");
            for (AuditLogEventProvider provider : ServiceLoader.load(AuditLogEventProvider.class)) {
                Collection<Def> contributed = provider.kinds();
                if (contributed == null)
                    continue;
                for (Def d : contributed)
                    register(map, d, provider.getClass().getName());
            }
            for (AuditLogEventType t : AuditLogEventRegistry.types()) {
                if (t.kind() != null && !map.containsKey(t.kind()))
                    throw new IllegalStateException(
                      "AuditLogEventType '" + t.name() + "' implies unregistered kind '" + t.kind() + "'");
            }
            families = Collections.unmodifiableMap(map);
            return families;
        }
    }

    static void register(Map<String, String> map, Def d, String source) {
        String existing = map.get(d.name());
        if (existing != null && !existing.equals(d.family())) {
            throw new IllegalStateException(
              source + " attempted to register kind '" + d.name() + "' with family '" + d.family()
                + "' (already registered with family '" + existing + "')");
        }
        map.put(d.name(), Objects.requireNonNull(d.family()));
    }
}
