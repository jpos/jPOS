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

package org.jpos.transaction;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Distributed transaction identifier intended to be used as an ISO-8583
 * Retrieval Reference Number (DE-037, RRN).
 *
 * <p>This class encodes a transaction timestamp (UTC, second precision), a node
 * identifier, and a per-node transaction counter suffix into a single
 * {@code long}. It supports:</p>
 *
 * <ul>
 *   <li>A canonical numeric form ({@link #id()}).</li>
 *   <li>A stable human-readable form ({@link #toString()}) suitable for logs and debugging.</li>
 *   <li>A compact base-36 form ({@link #toRrn()}) suitable for ISO-8583 field 37 (RRN).</li>
 *   <li>A filesystem-friendly relative path ({@link #toFile()}) that partitions by date/time.</li>
 * </ul>
 *
 * <p><b>RRN length constraint and long-term viability.</b>
 * ISO-8583 DE-037 limits the Retrieval Reference Number to 12 characters. When
 * rendered in base 36, this implementation therefore enforces the numeric ceiling
 * {@code "zzzzzzzzzzzz"} (base 36), exposed as {@link #MAX_VALUE}, so that
 * {@link #toRrn()} always fits within 12 characters.</p>
 *
 * <p>This ceiling is purely a <i>numeric</i> bound required for DE-037 compliance;
 * it is not derived from the {@code YYY-DDD-SSSSS-NNN-TTTTT} field layout.
 * Nevertheless, when TxnIds are created through {@link #create(ZonedDateTime, int, long)},
 * the encoded timestamp remains comfortably below this bound for several centuries.</p>
 *
 * <p>In practice, the maximum <i>semantically valid</i> timestamp that can be
 * encoded while still satisfying the DE-037 12-character constraint is
 * <b>2473-12-31 23:59:59 UTC</b>. This places the effective limit more than four
 * centuries in the future, making the scheme safe for long-term production use.</p>
 *
 * <p><b>Uniqueness model.</b> The identifier is composed of:</p>
 * <ul>
 *   <li>{@code YYY}: years since 2000 (000..999).</li>
 *   <li>{@code DDD}: day of year (001..366).</li>
 *   <li>{@code SSSSS}: second of day (00000..86399).</li>
 *   <li>{@code NNN}: node id (000..999).</li>
 *   <li>{@code TTTTT}: last 5 digits of a per-node transaction counter (00000..99999).</li>
 * </ul>
 *
 * <p>Collisions are prevented as long as, for a given {@code (UTC second, node)},
 * the {@code TTTTT} suffix is not reused.</p>
 *
 * <p><b>Time semantics.</b> All time components are encoded in UTC to avoid
 * daylight-saving and timezone ambiguity. The timestamp component has second
 * precision.</p>
 *
 * <p><b>Query-range semantics.</b> Because the numeric encoding is ordered by UTC time,
 * callers can build inclusive numeric bounds for index range scans. For “between local
 * dates” queries, this class provides DST-safe helpers that define a local day as the
 * half-open interval {@code [startOfDay(d), startOfDay(d+1))} in the requested zone and
 * then converts that to an inclusive UTC-second range.</p>
 */
public class TxnId {
    private long id;

    /**
     * Multiplier for year component (years since 2000).
     */
    private static final long YMUL = 10000000000000000L;

    /**
     * Multiplier for day-of-year component.
     */
    private static final long DMUL = 10000000000000L;

    /**
     * Multiplier for second-of-day component.
     */
    private static final long SMUL = 100000000L;

    /**
     * Multiplier for node component.
     */
    private static final long NMUL = 100000L;

    /**
     * Maximum allowed numeric value so that {@link #toRrn()} fits within 12 base-36 characters,
     * i.e., {@code "zzzzzzzzzzzz"} (base 36).
     */
    private static final long MAX_VALUE = Long.parseLong("zzzzzzzzzzzz", 36);

    /**
     * Pattern for the human-readable form produced by {@link #toString()}.
     * Format: {@code YYY-DDD-SSSSS-NNN-TTTTT}.
     */
    private static final Pattern PATTERN =
      Pattern.compile("^([\\d]{3})-([\\d]{3})-([\\d]{5})-([\\d]{3})-([\\d]{5})$");

    private static final ZoneId UTC = ZoneId.of("UTC");

    /**
     * Lowest node id used for inclusive range lower bounds.
     */
    private static final int MIN_NODE = 0;

    /**
     * Highest node id used for inclusive range upper bounds.
     */
    private static final int MAX_NODE = 999;

    /**
     * Lowest transaction suffix used for inclusive range lower bounds.
     */
    private static final long MIN_SUFFIX = 0L;

    /**
     * Highest transaction suffix used for inclusive range upper bounds.
     */
    private static final long MAX_SUFFIX = 99999L;

    /**
     * Inclusive numeric id range suitable for DB index range scans.
     *
     * <p>If {@link #isEmpty()} is {@code true}, the range contains no values and callers
     * should skip querying (or deliberately query a range that returns no results).</p>
     *
     * @param fromInclusive inclusive lower bound.
     * @param toInclusive inclusive upper bound.
     */
    public record TxnIdRange(long fromInclusive, long toInclusive) {
        public boolean isEmpty() {
            return fromInclusive > toInclusive;
        }

        public static TxnIdRange empty() {
            return new TxnIdRange(1L, 0L);
        }
    }

    private TxnId() {
        super();
    }

    private TxnId(long l) {
        this.id = l;
    }

    /**
     * Returns the canonical numeric representation of this transaction id.
     *
     * <p>Note: not all numeric values are necessarily a valid structured TxnId; use {@link #parse(String)}
     * when validation of the human-readable components is required.</p>
     *
     * @return the packed long value.
     */
    public long id() {
        return id;
    }

    private TxnId init(int year, int dayOfYear, int secondOfDay, int node, long transactionId) {
        // Defensive checks: do not silently fold via modulo, because that can hide configuration errors
        // and introduce collisions.
        if (year < 0 || year > 999)
            throw new IllegalArgumentException("Invalid year (years since 2000) " + year);
        if (dayOfYear < 1 || dayOfYear > 366)
            throw new IllegalArgumentException("Invalid dayOfYear " + dayOfYear);
        if (secondOfDay < 0 || secondOfDay > 86399)
            throw new IllegalArgumentException("Invalid secondOfDay " + secondOfDay);
        if (node < 0 || node > 999)
            throw new IllegalArgumentException("Invalid node " + node);
        if (transactionId < 0 || transactionId > 99999)
            throw new IllegalArgumentException("Invalid transactionId suffix " + transactionId);

        long v =
          (long) year * YMUL
            + (long) dayOfYear * DMUL
            + (long) secondOfDay * SMUL
            + (long) node * NMUL
            + (transactionId);

        if (v < 0 || v > MAX_VALUE)
            throw new IllegalArgumentException("TxnId exceeds maximum RRN value " + v);

        id = v;
        return this;
    }

    /**
     * Returns a relative file path suitable to store contents of this transaction.
     *
     * <p>Path format: {@code yyyy/mm/dd/hh-mm-ss-NNN-TTTTT} where:</p>
     * <ul>
     *   <li>{@code yyyy/mm/dd} is the UTC date derived from the encoded timestamp.</li>
     *   <li>{@code hh-mm-ss} is the UTC time (second precision).</li>
     *   <li>{@code NNN} is the node id (000..999).</li>
     *   <li>{@code TTTTT} is the transaction suffix (00000..99999).</li>
     * </ul>
     *
     * @return a {@link File} with the above relative path.
     */
    public File toFile() {
        long l = id;

        int yy = (int) (l / YMUL);
        l -= (long) yy * YMUL;

        int dd = (int) (l / DMUL);
        l -= (long) dd * DMUL;

        int sod = (int) (l / SMUL);
        l -= (long) sod * SMUL;

        int node = (int) (l / NMUL);
        l -= (long) node * NMUL;

        int hh = sod / 3600;
        int mm = (sod - 3600 * hh) / 60;
        int ss = sod % 60;

        ZonedDateTime dt = ZonedDateTime.of(2000 + yy, 1, 1, 0, 0, 0, 0, UTC)
          .plusDays(dd - 1L)
          .plusHours(hh)
          .plusMinutes(mm)
          .plusSeconds(ss);

        return new File(
          String.format("%04d/%02d/%02d/%02d-%02d-%02d-%03d-%05d",
            dt.getYear(),
            dt.getMonthValue(),
            dt.getDayOfMonth(),
            dt.getHour(),
            dt.getMinute(),
            dt.getSecond(),
            node,
            l
          )
        );
    }

    /**
     * Returns the human-readable form: {@code YYY-DDD-SSSSS-NNN-TTTTT}.
     *
     * <p>Where:</p>
     * <ul>
     *   <li>{@code YYY}: years since 2000 (000..999).</li>
     *   <li>{@code DDD}: day of year (001..366).</li>
     *   <li>{@code SSSSS}: second of day (00000..86399).</li>
     *   <li>{@code NNN}: node id (000..999).</li>
     *   <li>{@code TTTTT}: transaction suffix (00000..99999).</li>
     * </ul>
     *
     * @return the formatted TxnId string.
     */
    @Override
    public String toString() {
        long l = id;

        int yy = (int) (l / YMUL);
        l -= (long) yy * YMUL;

        int dd = (int) (l / DMUL);
        l -= (long) dd * DMUL;

        int sod = (int) (l / SMUL);
        l -= (long) sod * SMUL;

        int node = (int) (l / NMUL);
        l -= (long) node * NMUL;

        return String.format("%03d-%03d-%05d-%03d-%05d", yy, dd, sod, node, l);
    }

    /**
     * Returns a compact base-36 rendering of {@link #id()} suitable for ISO-8583 DE-037 (RRN).
     *
     * <p>The numeric value is constrained to {@link #MAX_VALUE} so that the base-36 string fits within
     * 12 characters.</p>
     *
     * @return base-36 string representation of the TxnId.
     */
    public String toRrn() {
        return Long.toString(id, 36);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TxnId txnId = (TxnId) o;
        return id == txnId.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Creates a new {@code TxnId} from a timestamp, node, and transaction suffix.
     *
     * <p>The timestamp is converted to UTC using {@link ZonedDateTime#withZoneSameInstant(ZoneId)} and then
     * encoded at second precision.</p>
     *
     * @param zonedDateTime transaction timestamp.
     * @param node node id (0..999).
     * @param transactionId per-node transaction suffix (0..99999). If your transaction manager uses a larger
     *                      counter, pass {@code counter % 100000} explicitly to avoid silent truncation.
     * @return newly created TxnId.
     */
    public static TxnId create(ZonedDateTime zonedDateTime, int node, long transactionId) {
        TxnId id = new TxnId();
        ZonedDateTime utcTime = zonedDateTime.withZoneSameInstant(UTC);
        return id.init(
          utcTime.getYear() - 2000,
          utcTime.getDayOfYear(),
          utcTime.toLocalTime().toSecondOfDay(),
          Math.floorMod(node, 1000),
          Math.floorMod(transactionId, 100000L)
        );
    }

    /**
     * Creates a new {@code TxnId} from an {@link Instant} (assumed UTC), node, and transaction suffix.
     *
     * @param instant transaction timestamp in UTC.
     * @param node node id (0..999).
     * @param transactionId per-node transaction suffix (0..99999).
     * @return newly created TxnId.
     */
    public static TxnId create(Instant instant, int node, long transactionId) {
        return create(instant.atZone(UTC), node, transactionId);
    }

    /**
     * Parses a {@code TxnId} from its human-readable form {@code YYY-DDD-SSSSS-NNN-TTTTT}.
     *
     * @param idString TxnId in {@code YYY-DDD-SSSSS-NNN-TTTTT} format (as produced by {@link #toString()}).
     * @return newly created TxnId.
     * @throws IllegalArgumentException if {@code idString} is invalid or out of range.
     */
    public static TxnId parse(String idString) {
        Matcher matcher = PATTERN.matcher(idString);
        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid idString '" + idString + "'");

        return new TxnId().init(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          Integer.parseInt(matcher.group(4)),
          Long.parseLong(matcher.group(5))
        );
    }

    /**
     * Parses a {@code TxnId} from its canonical numeric value.
     *
     * <p>This validates only the numeric bound required for DE-037 usage (see {@link #MAX_VALUE}). It does not
     * validate that each encoded component is within expected ranges.</p>
     *
     * @param id numeric value.
     * @return newly created TxnId.
     * @throws IllegalArgumentException if the value is negative or exceeds {@link #MAX_VALUE}.
     */
    public static TxnId parse(long id) {
        if (id < 0 || id > MAX_VALUE)
            throw new IllegalArgumentException("Invalid id " + id);
        return new TxnId(id);
    }

    /**
     * Parses a {@code TxnId} from an ISO-8583 DE-037 Retrieval Reference Number (RRN) in base 36.
     *
     * @param rrn base-36 value (must decode to a non-negative number not exceeding {@link #MAX_VALUE}).
     * @return newly created TxnId.
     * @throws IllegalArgumentException if {@code rrn} is invalid or out of range.
     */
    public static TxnId fromRrn(String rrn) {
        long id;
        try {
            id = Long.parseLong(rrn, 36);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid rrn " + rrn, e);
        }

        if (id < 0 || id > MAX_VALUE)
            throw new IllegalArgumentException("Invalid rrn " + rrn);

        return new TxnId(id);
    }

    /**
     * Computes the lowest possible TxnId numeric value for the given UTC second,
     * suitable for an inclusive range lower bound.
     *
     * <p>This uses {@code node=000} and {@code suffix=00000}.</p>
     *
     * @param instantUtc a timestamp whose {@link Instant#getEpochSecond()} is used.
     * @return inclusive lower bound id.
     */
    public static long lowerBoundId(Instant instantUtc) {
        Objects.requireNonNull(instantUtc, "instantUtc");
        Instant t = Instant.ofEpochSecond(instantUtc.getEpochSecond());
        return create(t, MIN_NODE, MIN_SUFFIX).id();
    }

    /**
     * Computes the highest possible TxnId numeric value for the given UTC second,
     * suitable for an inclusive range upper bound.
     *
     * <p>This uses {@code node=999} and {@code suffix=99999}.</p>
     *
     * @param instantUtc a timestamp whose {@link Instant#getEpochSecond()} is used.
     * @return inclusive upper bound id.
     */
    public static long upperBoundId(Instant instantUtc) {
        Objects.requireNonNull(instantUtc, "instantUtc");
        Instant t = Instant.ofEpochSecond(instantUtc.getEpochSecond());
        return create(t, MAX_NODE, MAX_SUFFIX).id();
    }

    /**
     * Computes an inclusive numeric id range for the given UTC instant range.
     *
     * <p>Both ends are treated as inclusive at second precision. Any sub-second
     * component is ignored.</p>
     *
     * @param fromUtc inclusive lower endpoint (UTC).
     * @param toUtc inclusive upper endpoint (UTC).
     * @return inclusive numeric id range; may be empty.
     */
    public static TxnIdRange idRange(Instant fromUtc, Instant toUtc) {
        Objects.requireNonNull(fromUtc, "fromUtc");
        Objects.requireNonNull(toUtc, "toUtc");

        long fromSec = fromUtc.getEpochSecond();
        long toSec = toUtc.getEpochSecond();
        if (fromSec > toSec)
            return TxnIdRange.empty();

        long fromId = lowerBoundId(Instant.ofEpochSecond(fromSec));
        long toId = upperBoundId(Instant.ofEpochSecond(toSec));
        return new TxnIdRange(fromId, toId);
    }

    /**
     * Computes an inclusive numeric id range for transactions between the given local dates,
     * inclusive on both ends, in the provided time zone.
     *
     * <p>DST-safe strategy: define each local day as the half-open interval
     * {@code [startOfDay(d), startOfDay(d+1))} in {@code zone}. This avoids constructing
     * local “end of day” timestamps (which can be ambiguous on overlap days).
     * The resulting UTC range is then made inclusive at second precision by subtracting
     * one second from the exclusive end.</p>
     *
     * @param fromLocalDate inclusive start date in {@code zone}.
     * @param toLocalDate inclusive end date in {@code zone}.
     * @param zone time zone for interpreting local dates.
     * @return inclusive numeric id range; may be empty.
     */
    public static TxnIdRange idRange(LocalDate fromLocalDate, LocalDate toLocalDate, ZoneId zone) {
        Objects.requireNonNull(fromLocalDate, "fromLocalDate");
        Objects.requireNonNull(toLocalDate, "toLocalDate");
        Objects.requireNonNull(zone, "zone");

        if (fromLocalDate.isAfter(toLocalDate))
            return TxnIdRange.empty();

        ZonedDateTime fromStart = fromLocalDate.atStartOfDay(zone);
        ZonedDateTime toExclusiveStart = toLocalDate.plusDays(1L).atStartOfDay(zone);

        long fromSec = fromStart.toInstant().getEpochSecond();
        long toExclusiveSec = toExclusiveStart.toInstant().getEpochSecond();

        // If the exclusive end is not strictly after the start, the interval is empty.
        if (toExclusiveSec <= fromSec)
            return TxnIdRange.empty();

        long toInclusiveSec = toExclusiveSec - 1L;

        return idRange(Instant.ofEpochSecond(fromSec), Instant.ofEpochSecond(toInclusiveSec));
    }

    /**
     * Inclusive lower bound id for the start of the given local day in {@code zone}.
     *
     * @param localDate local date.
     * @param zone zone in which the local day is defined.
     * @return inclusive lower bound id.
     */
    public static long lowerBoundId(LocalDate localDate, ZoneId zone) {
        Objects.requireNonNull(localDate, "localDate");
        Objects.requireNonNull(zone, "zone");
        return lowerBoundId(localDate.atStartOfDay(zone).toInstant());
    }

    /**
     * Inclusive upper bound id for the end of the given local day in {@code zone}.
     *
     * <p>DST-safe: computed as one second before {@code startOfDay(localDate+1)} in {@code zone}.</p>
     *
     * @param localDate local date.
     * @param zone zone in which the local day is defined.
     * @return inclusive upper bound id.
     */
    public static long upperBoundId(LocalDate localDate, ZoneId zone) {
        Objects.requireNonNull(localDate, "localDate");
        Objects.requireNonNull(zone, "zone");

        ZonedDateTime start = localDate.atStartOfDay(zone);
        ZonedDateTime nextStart = localDate.plusDays(1L).atStartOfDay(zone);

        long startSec = start.toInstant().getEpochSecond();
        long nextStartSec = nextStart.toInstant().getEpochSecond();
        if (nextStartSec <= startSec)
            return 0L; // empty day interval (defensive)

        return upperBoundId(Instant.ofEpochSecond(nextStartSec - 1L));
    }

    /**
     * Inclusive range for local date-times in {@code zone}.
     *
     * <p>Note: this method interprets {@code fromLocalDateTime} and {@code toLocalDateTime} as local wall-clock
     * times in {@code zone}. For DST gaps/overlaps, {@link ZonedDateTime#of(LocalDateTime, ZoneId)} applies the
     * zone rules. If you need explicit overlap resolution (earlier vs later offset), pass {@link ZonedDateTime}
     * values instead and use {@link #idRange(Instant, Instant)} or {@link #idRange(ZonedDateTime, ZonedDateTime)}.</p>
     *
     * @param fromLocalDateTime inclusive start time in {@code zone}.
     * @param toLocalDateTime inclusive end time in {@code zone}.
     * @param zone zone for interpreting local date-times.
     * @return inclusive numeric id range; may be empty.
     */
    public static TxnIdRange idRange(LocalDateTime fromLocalDateTime, LocalDateTime toLocalDateTime, ZoneId zone) {
        Objects.requireNonNull(fromLocalDateTime, "fromLocalDateTime");
        Objects.requireNonNull(toLocalDateTime, "toLocalDateTime");
        Objects.requireNonNull(zone, "zone");
        return idRange(fromLocalDateTime.atZone(zone).toInstant(), toLocalDateTime.atZone(zone).toInstant());
    }

    /**
     * Inclusive range for zoned date-times.
     *
     * @param from inclusive start time.
     * @param to inclusive end time.
     * @return inclusive numeric id range; may be empty.
     */
    public static TxnIdRange idRange(ZonedDateTime from, ZonedDateTime to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        return idRange(from.toInstant(), to.toInstant());
    }
}
