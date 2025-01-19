/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2024 jPOS Software SRL
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TxnId {
    private long id;
    private static final long YMUL = 10000000000000000L;
    private static final long DMUL = 10000000000000L;
    private static final long SMUL = 100000000L;
    private static final long NMUL = 100000L;
    private static final long MAX_VALUE = Long.parseLong("zzzzzzzzzzzz", 36);
    private static Pattern pattern = Pattern.compile("^([\\d]{3})-([\\d]{3})-([\\d]{5})-([\\d]{3})-([\\d]{5})$");
    private static ZoneId UTC = ZoneId.of("UTC");

    private TxnId() {
        super();
    }

    private TxnId(long l) {
        this.id = l;
    }

    public long id() {
        return id;
    }

    private TxnId init (int year, int dayOfYear, int secondOfDay, int node, long transactionId) {
        id = year * YMUL
           + dayOfYear * DMUL
           + secondOfDay * SMUL
           + (node % 1000) * NMUL
           + transactionId % 100000;
        return this;
    }
    /**
     * Returns a file suitable to store contents of <i>this</i> transaction.
     * File format is <i>yyyy/mm/dd/hh-mm-ss-node-id</i>
     * @return file in said format
     */
    public File toFile () {
        long l = id;
        int yy = (int) (id / YMUL); l -= yy*YMUL;
        int dd = (int) (l / DMUL);  l -= dd*DMUL;
        int sod = (int) (l / SMUL); l -= sod*SMUL;
        int node = (int) (l / NMUL); l -= node * NMUL;
        int hh = sod/3600;
        int mm = (sod-3600*hh) / 60;
        int ss = sod % 60;

        ZonedDateTime dt = ZonedDateTime.of(2000 + yy, 1, 1, 0, 0, 0, 0, UTC)
          .plusDays(dd - 1)
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

    @Override
    public String toString() {
        long l = id;
        int yy = (int) (id / YMUL);
        l -= yy*YMUL;

        int dd = (int) (l / DMUL);
        l -= dd * DMUL;

        int ss = (int) (l / SMUL);
        l -= ss * SMUL;

        int node = (int) (l / NMUL);
        l -= node * NMUL;
        return String.format("%03d-%03d-%05d-%03d-%05d", yy, dd, ss, node, l);
    }

    public String toRrn() {
        return Long.toString(id, 36);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TxnId tranLogId = (TxnId) o;
        return id == tranLogId.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Creates new TxnId object
     *
     * @param zonedDateTime Transaction's TIMESTAMP DateTime
     * @param node node id
     * @param transactionId TransactionManager's ID
     */
    
    public static TxnId create(ZonedDateTime zonedDateTime, int node, long transactionId) {
        TxnId id = new TxnId();
        ZonedDateTime utcTime = zonedDateTime.withZoneSameInstant(UTC);
        return id.init(utcTime.getYear() - 2000, utcTime.getDayOfYear(), utcTime.toLocalTime().toSecondOfDay(), node, transactionId);
    }

    public static TxnId create (Instant instant, int node, long transactionId) {
        return create(instant.atZone(UTC), node, transactionId);
    }

    /**
     * @param idString TxnId in YYYY-DDD-SSS-NN-TTTTT format
     *
     * <ul>
     *   <li><code>CYY</code> Century Year Year</li>
     *   <li><code>DDD</code> day of year</li>
     *   <li><code>SSS</code> second of day</li>
     *   <li><code>NNN</code> unique node number (000 to 999)</li>
     *   <li><code>TTTTT</code> last 5 digits of transaction manager's transaction id</li>
     * </ul>
     */
    public static TxnId parse (String idString) {
        Matcher matcher = pattern.matcher(idString);
        if (!matcher.matches() && matcher.groupCount() != 5)
            throw new IllegalArgumentException("Invalid idString '" + idString + "'");
        return new TxnId().init(
          Integer.parseInt(matcher.group(1)),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          Integer.parseInt(matcher.group(4)),
          Integer.parseInt(matcher.group(5))
        );
    }

    /**
     * Parse TxnId from long
     *
     * @param id value
     * @return newly created TxnId
     */
    public static TxnId parse (long id) {
        if (id <0 || id > MAX_VALUE)
            throw new IllegalArgumentException("Invalid id " + id);
        return new TxnId(id);
    }

    /**
     * Parse TxnId from rrn
     *
     * @param rrn value
     * @return newly created TxnId
     */
    public static TxnId fromRrn (String rrn) {
        long id = Long.parseLong(rrn, 36);
        if (id <0 || id > MAX_VALUE)
            throw new IllegalArgumentException("Invalid rrn " + rrn);
        return new TxnId(id);
    }
}
