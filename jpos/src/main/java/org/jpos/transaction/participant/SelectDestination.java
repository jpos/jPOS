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

import org.jdom2.Element;
import org.jpos.core.*;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.Q2;
import org.jpos.q2.QFactory;
import org.jpos.rc.CMF;
import org.jpos.transaction.Context;
import org.jpos.transaction.ContextConstants;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Caller;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class SelectDestination implements TransactionParticipant, Configurable, XmlConfigurable {
    private String requestName;
    private String destinationName;
    private String defaultDestination;
    private boolean failOnNoRoute;
    private CardValidator validator;
    private Set<BinRange> binranges = new TreeSet<>();
    private List<PanRegExp> regexps = new ArrayList<>();

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg m = (ISOMsg) ctx.get(requestName);
        boolean destinationSet = false;
        if (m != null && (m.hasField(2) || m.hasField(35))) {
            try {
                Card card = Card.builder().validator(validator).isomsg(m).build();
                String destination = getDestination(card);
                if (destination != null) {
                    ctx.put(destinationName, destination);
                    destinationSet = true;
                }
            } catch (InvalidCardException ex) {
                return ctx.getResult().fail(
                  CMF.INVALID_CARD_OR_CARDHOLDER_NUMBER, Caller.info(), ex.getMessage()
                ).FAIL();
            }
        }
        if (!destinationSet && ctx.get(destinationName) == null)
            ctx.put(destinationName, defaultDestination);
        if (failOnNoRoute && ctx.get(destinationName) == null)
            return ctx.getResult().fail(CMF.ROUTING_ERROR, Caller.info(), "No routing info").FAIL();

        return PREPARED | NO_JOIN | READONLY;
    }
    
    public void setConfiguration (Configuration cfg) {
        this.requestName = cfg.get("request", ContextConstants.REQUEST.toString());
        this.destinationName = cfg.get ("destination", ContextConstants.DESTINATION.toString());
        this.defaultDestination = cfg.get("default-destination", null);
        this.validator =  cfg.getBoolean("ignore-luhn") ?
          new IgnoreLuhnCardValidator() : Card.Builder.DEFAULT_CARD_VALIDATOR;
        this.failOnNoRoute = cfg.getBoolean("fail");
    }

    /**
     * @param xml Configuration element
     * @throws ConfigurationException
     *
     *
     * SelectDestination expects an XML configuration in the following format:
     *
     * <endpoint destination="XXX">
     *   4000000..499999
     *   4550000..455999
     *   5
     * </endpoint>
     *
     */
    public void setConfiguration(Element xml) throws ConfigurationException {
        for (Element ep : xml.getChildren("endpoint")) {
            String destination = QFactory.getAttributeValue(ep, "destination");
            StringTokenizer st = new StringTokenizer(ep.getText());
            while (st.hasMoreElements()) {
                BinRange br = new BinRange(destination, st.nextToken());
                binranges.add(br);
            }
        }
        for (Element re : xml.getChildren("regexp")) {
            String destination = QFactory.getAttributeValue(re, "destination");
            regexps.add(
              new PanRegExp(QFactory.getAttributeValue(re, "destination"), re.getTextTrim())
            );
        }
        LogEvent evt = Log.getLog(Q2.LOGGER_NAME, this.getClass().getName()).createLogEvent("config");
        for (PanRegExp r : regexps)
            evt.addMessage("00:" + r);
        for (BinRange r : binranges)
            evt.addMessage(r);
        Logger.log(evt);
    }

    private String getDestination (Card card) {
        String destination = getDestinationByRegExp(card.getPan());
        if (destination == null)
            destination = getDestinationByPanNumber(card.getPanAsNumber());
        return destination;
    }

    private String getDestinationByPanNumber (BigInteger pan) {
        final BigInteger p = BinRange.floor(pan);
        return binranges
          .stream()
          .filter(r -> r.isInRange(p))
          .findFirst()
          .map(BinRange::destination).orElse(null);
    }

    private String getDestinationByRegExp (String pan) {
        return regexps
          .stream()
          .filter(r -> r.matches(pan))
          .findFirst()
          .map(PanRegExp::destination).orElse(null);
    }

    public static class BinRange implements Comparable {
        private String destination;
        private BigInteger low;
        private BigInteger high;
        private short weight;
        private static Pattern rangePattern = Pattern.compile("^([\\d]{1,19})*(?:\\.\\.)?([\\d]{0,19})?$");

        /**
         * Sample bin:
         * <ul>
         *     <li>4</li>
         *     <li>411111</li>
         *     <li>4111111111111111</li>
         *     <li>411111..422222</li>
         * </ul>
         * @param destination range destination
         * @param rangeExpr either a 'bin', 'extended bin' or 'bin range'
         */
        BinRange(String destination, String rangeExpr) {
            this.destination = destination;
            Matcher matcher = rangePattern.matcher(rangeExpr);
            if (!matcher.matches())
                throw new IllegalArgumentException("Invalid range " + rangeExpr);

            String l = matcher.group(1);
            String h = matcher.group(2);
            h = h.isEmpty() ? l : h;
            weight = (short) (Math.max(l.length(), h.length()));
            low = floor(new BigInteger(l));
            high = ceiling(new BigInteger(h));
            if (low.compareTo(high) > 0)
                throw new IllegalArgumentException("Invalid range " + low + "/" + high);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BinRange binRange = (BinRange) o;
            return weight == binRange.weight &&
              Objects.equals(low, binRange.low) &&
              Objects.equals(high, binRange.high);
        }

        @Override
        public int hashCode() {
            return Objects.hash(low, high, weight);
        }

        @Override
        public String toString() {
            return String.format("%02d:%s..%s [%s]", 19-weight, low, high, destination); // Warning, compareTo expects sorteable format
        }
        @Override
        public int compareTo(Object o) {
            return toString().compareTo(o.toString());
        }
        public boolean isInRange (BigInteger i) {
            return i.compareTo(low) >=0 && i.compareTo(high) <=0;
        }

        public String destination() {
            return destination;
        }

        static BigInteger floor(BigInteger i) {
            if (!BigInteger.ZERO.equals(i)) {
                int digits  = (int)Math.log10(i.doubleValue())+1;
                i = i.multiply(BigInteger.TEN.pow(19-digits));
            }
            return i;
        }
        private BigInteger ceiling (BigInteger i) {
            int digits  = BigInteger.ZERO.equals(i) ? 1 : (int)Math.log10(i.doubleValue())+1;
            return floor(i).add(BigInteger.TEN.pow(19-digits)).subtract(BigInteger.ONE);
        }
    }

    private static class PanRegExp {
        private String destination;
        private Pattern pattern;

        PanRegExp(String destination, String regexp) {
            this.destination = destination;
            this.pattern = Pattern.compile(regexp);
        }

        public String destination() {
            return destination;
        }

        public boolean matches(String pan) {
            return pattern.matcher(pan).matches();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PanRegExp panRegExp = (PanRegExp) o;
            return Objects.equals(destination, panRegExp.destination) &&
              Objects.equals(pattern, panRegExp.pattern);
        }

        @Override
        public int hashCode() {
            return Objects.hash(destination, pattern);
        }

        @Override
        public String toString() {
            return String.format("%s [%s]", pattern.toString(), destination);
        }
    }
}
