package org.jpos.transaction.participant;

import org.jdom2.Element;
import org.jpos.core.*;
import org.jpos.iso.ISOMsg;
import org.jpos.q2.Q2;
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

    @Override
    public int prepare(long id, Serializable context) {
        Context ctx = (Context) context;
        ISOMsg m = (ISOMsg) ctx.get(requestName);
        boolean destinationSet = false;
        if (m != null && (m.hasField(2) || m.hasField(35))) {
            try {
                Card card = Card.builder().validator(validator).isomsg(m).build();
                String destination = getDestination(card.getPanAsNumber());
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
            String destination = ep.getAttributeValue("destination");
            StringTokenizer st = new StringTokenizer(ep.getText());
            while (st.hasMoreElements()) {
                BinRange br = new BinRange(destination, st.nextToken());
                binranges.add(br);
            }
        }
        LogEvent evt = Log.getLog(Q2.LOGGER_NAME, this.getClass().getName()).createLogEvent("config");
        for (BinRange r : binranges)
            evt.addMessage(r);
        Logger.log(evt);
    }

    private String getDestination (BigInteger pan) {
        final BigInteger p = BinRange.floor(pan);
        return binranges
          .stream()
          .filter(r -> r.isInRange(p))
          .findFirst()
          .map(BinRange::destination).orElse(null);
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
        public BinRange(String destination, String rangeExpr) {
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
            int digits  = (int)Math.log10(i.doubleValue())+1;
            return i.multiply(BigInteger.TEN.pow(19-digits));
        }
        private BigInteger ceiling (BigInteger i) {
            int digits  = (int)Math.log10(i.doubleValue())+1;
            return floor(i).add(BigInteger.TEN.pow(19-digits)).subtract(BigInteger.ONE);
        }
    }
}
