/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2020 jPOS Software SRL
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

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.io.Serializable;
import java.util.regex.Pattern;

import org.jpos.core.*;
import org.jpos.iso.*;
import org.jpos.rc.CMF;
import org.jpos.rc.Result;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Caller;

import static org.jpos.transaction.ContextConstants.*;

public class CheckFields implements TransactionParticipant, Configurable {
    private Configuration cfg;
    private String request;
    private Pattern PCODE_PATTERN = Pattern.compile("^[\\d|\\w]{6}$");
    private Pattern TID_PATTERN = Pattern.compile("^[\\w\\s]{1,16}");
    private Pattern MID_PATTERN = Pattern.compile("^[\\w\\s]{1,15}");
    private Pattern TRANSMISSION_TIMESTAMP_PATTERN = Pattern.compile("^\\d{10}");
    private Pattern LOCAL_TIMESTAMP_PATTERN = Pattern.compile("^\\d{14}");
    private Pattern CAPTUREDATE_PATTERN = Pattern.compile("^\\d{4}");
    private Pattern ORIGINAL_DATA_ELEMENTS_PATTERN = Pattern.compile("^\\d{30,41}$");
    private boolean ignoreCardValidation = false;
    private boolean allowExtraFields = false;
    private Pattern track1Pattern = null;
    private Pattern track2Pattern = null;

    public int prepare (long id, Serializable context) {
        Context ctx = (Context) context;
        Result rc = ctx.getResult();
        try {
            ISOMsg m = ctx.get (request);
            if (m == null) {
                ctx.getResult().fail(CMF.INVALID_TRANSACTION, Caller.info(), "'%s' not available in Context", request);
                return ABORTED | NO_JOIN | READONLY;
            }
            Set<String> validFields = new HashSet<>();
            assertFields (ctx, m, cfg.get ("mandatory", ""), true, validFields, rc);
            assertFields (ctx, m, cfg.get ("optional", ""), false, validFields, rc);
            if (!allowExtraFields) assertNoExtraFields (m, validFields, rc);
        } catch (Throwable t) {
            rc.fail(CMF.SYSTEM_ERROR, Caller.info(), t.getMessage());
            ctx.log(t);
        }
        return (rc.hasFailures() ? ABORTED : PREPARED) | NO_JOIN | READONLY;
    }

    public void setConfiguration (Configuration cfg) {
        this.cfg = cfg;
        request = cfg.get ("request", ContextConstants.REQUEST.toString());
        ignoreCardValidation = cfg.getBoolean("ignore-card-validation", false);
        allowExtraFields = cfg.getBoolean("allow-extra-fields", false);
        String t1 = cfg.get("track1-pattern", null);
        if (t1 != null) {
            track1Pattern = Pattern.compile(t1);
        }
        String t2 = cfg.get("track2-pattern", null);
        if (t2 != null) {
            track2Pattern = Pattern.compile(t1);
        }
    }

    private void assertFields(Context ctx, ISOMsg m, String fields, boolean mandatory, Set<String> validFields, Result rc) {
        StringTokenizer st = new StringTokenizer (fields, ", ");
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            ContextConstants k = null;
            try {
                k = ContextConstants.valueOf(s);
            } catch (IllegalArgumentException ignored) { }
            if (k != null) {
                switch (k) {
                    case PCODE:
                        putPCode(ctx, m, mandatory, validFields, rc);
                        break;
                    case CARD:
                        putCard(ctx, m, mandatory, validFields, rc);
                        break;
                    case TID:
                        putTid(ctx, m, mandatory, validFields, rc);
                        break;
                    case MID:
                        putMid(ctx, m, mandatory, validFields, rc);
                        break;
                    case TRANSMISSION_TIMESTAMP:
                        putTimestamp(ctx, m, TRANSMISSION_TIMESTAMP.toString(), 7, TRANSMISSION_TIMESTAMP_PATTERN, mandatory, validFields, rc);
                        break;
                    case TRANSACTION_TIMESTAMP:
                        putTimestamp(ctx, m, TRANSACTION_TIMESTAMP.toString(), 12, LOCAL_TIMESTAMP_PATTERN, mandatory, validFields, rc);
                        break;
                    case POS_DATA_CODE:
                        putPDC(ctx, m, mandatory, validFields, rc);
                        break;
                    case CAPTURE_DATE:
                        putCaptureDate(ctx, m, mandatory, validFields, rc);
                        break;
                    case AMOUNT:
                        putAmount(ctx, m, mandatory, validFields, rc);
                        break;
                    case ORIGINAL_DATA_ELEMENTS:
                        putOriginalDataElements(ctx, m, mandatory, validFields, rc);
                        break;
                    default:
                        k = null;
                }
            }
            if (k == null) {
                if (mandatory && !m.hasField(s))
                    rc.fail(CMF.MISSING_FIELD, Caller.info(), s);
                else
                    validFields.add(s);
            }
        }
    }
    private void assertNoExtraFields (ISOMsg m, Set validFields, Result rc) {
        StringBuilder sb = new StringBuilder();
        for (int i=1; i<=m.getMaxField(); i++) { // we start at 1, MTI is always valid
            String s = Integer.toString (i);
            if (m.hasField(i) && !validFields.contains (s)) {
                if (sb.length() > 0)
                    sb.append (' ');
                sb.append (s);
            }
        }
        if (sb.length() > 0)
            rc.fail(CMF.EXTRA_FIELD, Caller.info(), sb.toString());
    }

    private void putCard (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        boolean hasCard = m.hasAny("2", "14", "35", "45");
        if (!mandatory && !hasCard)
            return; // nothing to do, card is optional

        try {
            Card.Builder cb = Card.builder();
            if (track1Pattern != null)
                cb.withTrack1Builder(Track1.builder().pattern(track1Pattern));
            if (track2Pattern != null)
                cb.withTrack2Builder(Track2.builder().pattern(track2Pattern));
            cb.isomsg(m);
            if (ignoreCardValidation)
                cb.validator(null);
            Card card = cb.build();
            ctx.put (ContextConstants.CARD.toString(), card);
            if (card.hasTrack1())
                validFields.add("45");
            if (card.hasTrack2())
                validFields.add("35");
            if (card.getPan() != null && m.hasField(2))
                validFields.add("2");
            if (card.getExp() != null && m.hasField(14))
                validFields.add("14");
        } catch (InvalidCardException e) {
            validFields.addAll(Arrays.asList("2", "14", "35", "45"));
            if (hasCard) {
                rc.fail(CMF.INVALID_CARD_NUMBER, Caller.info(), e.getMessage());
            } else if (mandatory) {
                rc.fail(CMF.MISSING_FIELD, Caller.info(), e.getMessage());
            }
        }
    }

    private void putPCode (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        if (m.hasField(3)) {
            String s = m.getString(3);

            validFields.add("3");
            if (PCODE_PATTERN.matcher(s).matches()) {
                ctx.put(ContextConstants.PCODE.toString(), m.getString(3));
            } else
                rc.fail(CMF.INVALID_FIELD, Caller.info(), "Invalid PCODE '%s'", s);
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), "PCODE");
        }
    }

    private void putTid (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        if (m.hasField(41)) {
            String s = m.getString(41);
            validFields.add("41");
            if (TID_PATTERN.matcher(s).matches()) {
                ctx.put(ContextConstants.TID.toString(), m.getString(41));
            } else
                rc.fail(CMF.INVALID_FIELD, Caller.info(), "Invalid TID '%s'", s);
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), "TID");
        }
    }

    private void putMid (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        if (m.hasField(42)) {
            String s = m.getString(42);
            validFields.add("42");
            if (MID_PATTERN.matcher(s).matches()) {
                ctx.put(ContextConstants.MID.toString(), m.getString(42));
            } else
                rc.fail(CMF.INVALID_FIELD, Caller.info(), "Invalid MID '%s'", s);
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), "MID");
        }
    }
    private void putTimestamp (Context ctx, ISOMsg m, String key, int fieldNumber, Pattern ptrn, boolean mandatory, Set<String> validFields, Result rc) {
        if (m.hasField(fieldNumber)) {
            String s = m.getString(fieldNumber);
            validFields.add(Integer.toString(fieldNumber));
            if (ptrn.matcher(s).matches())
                ctx.put (key, ISODate.parseISODate(s));
            else
                rc.fail(CMF.INVALID_FIELD, Caller.info(), "Invalid %s '%s'", key, s);
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), TRANSMISSION_TIMESTAMP.toString());
        }
    }

    private void putCaptureDate (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        if (m.hasField(17)) {
            String s = m.getString(17);
            validFields.add("17");
            if (CAPTUREDATE_PATTERN.matcher(s).matches())
                ctx.put (CAPTURE_DATE.toString(), ISODate.parseISODate(s + "120000"));
            else
                rc.fail(CMF.INVALID_FIELD, Caller.info(), "Invalid %s '%s'", CAPTURE_DATE, s);
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), CAPTURE_DATE.toString());
        }
    }

    private void putPDC (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        if (m.hasField(22)) {
            byte[] b = m.getBytes(22);
            validFields.add("22");
            if (b.length != 16) {
                rc.fail(
                  CMF.INVALID_FIELD,
                  Caller.info(), "Invalid %s '%s'",
                  ContextConstants.POS_DATA_CODE.toString(),
                  ISOUtil.hexString(b)
                );
            }
            else {
                ctx.put(ContextConstants.POS_DATA_CODE.toString(), PosDataCode.valueOf(m.getBytes(22)));
            }
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), ContextConstants.POS_DATA_CODE.toString());
        }
    }

    private void putAmount (Context ctx, ISOMsg m, boolean mandatory,Set<String> validFields, Result rc) {
        Object o4 = m.getComponent(4);
        Object o5 = m.getComponent(5);
        ISOAmount a4 = null;
        ISOAmount a5 = null;
        if (o4 instanceof ISOAmount) {
            a4 = (ISOAmount) o4;
            validFields.add("4");
        }
        if (o5 instanceof ISOAmount) {
            a5 = (ISOAmount) o5;
            validFields.add("5");
        }
        if (a5 != null) {
            ctx.put (AMOUNT.toString(), a5);
            if (a4 != null) {
                ctx.put (LOCAL_AMOUNT.toString(), a4);
            }
        } else if (a4 != null) {
            ctx.put (AMOUNT.toString(), a4);
        }
        if (mandatory && (a4 == null && a5 == null))
            rc.fail(CMF.MISSING_FIELD, Caller.info(), ContextConstants.AMOUNT.toString());
    }

    private void putOriginalDataElements (Context ctx, ISOMsg m, boolean mandatory, Set<String> validFields, Result rc) {
        String s = m.getString(56);
        if (s != null) {
            validFields.add("56");
            if (ORIGINAL_DATA_ELEMENTS_PATTERN.matcher(s).matches()) {
                ctx.put (ORIGINAL_MTI.toString(), s.substring(0,4));
                ctx.put (ORIGINAL_STAN.toString(), s.substring(4,16));
                ctx.put (ORIGINAL_TIMESTAMP.toString(), ISODate.parseISODate (s.substring(16,30)));
            } else {
                rc.fail(CMF.INVALID_FIELD, Caller.info(), "Invalid %s '%s'", ORIGINAL_DATA_ELEMENTS, s);
            }
        } else if (mandatory) {
            rc.fail(CMF.MISSING_FIELD, Caller.info(), ContextConstants.ORIGINAL_DATA_ELEMENTS.toString());
        }
    }
}
