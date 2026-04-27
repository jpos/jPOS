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

package org.jpos.core;

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 * Immutable card data carrier.
 *
 * This class is based on the old {@code CardHolder} class and adds support for multiple
 * PAN and expiration date sources (manual entry, track 1, track 2). It also fixes naming.
 *
 * @author apr@jpos.org
 * @since jPOS 2.0.5
 */
public class Card {
    /** Primary account number. */
    private String pan;
    /** Expiration date (YYMM). */
    private String exp;
    /** CVV2 / CVC2 value. */
    private String cvv2;
    /** 3-digit ISO service code. */
    private String serviceCode;
    /** Track 1 data. */
    private Track1 track1;
    /** Track 2 data. */
    private Track2 track2;
    /** Length of the BIN (Bank Identification Number) in digits. */
    public static final int BINLEN = 6;

    private Card() { }

    /**
     * Creates a Card from the given Builder.
     * @param builder the builder
     */
    public Card(Builder builder) {
        pan         = builder.pan;
        exp         = builder.exp;
        cvv2        = builder.cvv2;
        serviceCode = builder.serviceCode;
        track1      = builder.track1;
        track2      = builder.track2;
    }

    /**
     * Returns the primary account number.
     * @return the PAN
     */
    public String getPan() {
        return pan;
    }

    /**
     * Returns the primary account number as a {@link BigInteger}.
     * @return the PAN as a BigInteger
     */
    public BigInteger getPanAsNumber() {
        return new BigInteger(pan);
    }

    /**
     * Returns the card expiry date.
     * @return the expiry date in YYMM format
     */
    public String getExp() {
        return exp;
    }

    /**
     * Returns the CVV2 / CVC2 value.
     * @return the CVV2 value
     */
    public String getCvv2() {
        return cvv2;
    }

    /**
     * Returns the ISO service code.
     * @return the 3-digit service code
     */
    public String getServiceCode() {
        return serviceCode;
    }

    /**
     * Returns true if track 1 data is present.
     * @return true if track 1 is available
     */
    public boolean hasTrack1() {
        return track1 != null;
    }

    /**
     * Returns true if track 2 data is present.
     * @return true if track 2 is available
     */
    public boolean hasTrack2() {
        return track2 != null;
    }

    /**
     * Returns true if both track 1 and track 2 data are present.
     * @return true if both tracks are available
     */
    public boolean hasBothTracks() {
        return hasTrack1() && hasTrack2();
    }

    /**
     * Returns the traditional 6-digit BIN from the PAN.
     * @return the first {@value #BINLEN} digits of the PAN
     */
    public String getBin () {
        return getBin(BINLEN);
    }

    /**
     * Returns the first {@code len} digits from the PAN.
     * Can be used for the newer 8-digit BINs, or some arbitrary length.
     * @param len number of leading digits to return
     * @return the first {@code len} digits of the PAN
     */
    public String getBin (int len) {
        return pan.substring(0, len);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return pan != null ? ISOUtil.protect(pan) : "nil";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(pan, card.pan) &&
          Objects.equals(exp, card.exp) &&
          Objects.equals(cvv2, card.cvv2) &&
          Objects.equals(serviceCode, card.serviceCode) &&
          Objects.equals(track1, card.track1) &&
          Objects.equals(track2, card.track2);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(pan, exp, cvv2, serviceCode, track1, track2);
    }

    /**
     * Returns the Track 1 data.
     * @return the {@link Track1} object, or null if not present
     */
    public Track1 getTrack1() {
        return track1;
    }

    /**
     * Returns the Track 2 data.
     * @return the {@link Track2} object, or null if not present
     */
    public Track2 getTrack2() {
        return track2;
    }

    /**
     * Returns true if the card is expired relative to the given date.
     * @param currentDate the date to compare against
     * @return true if the card is expired as of {@code currentDate}
     */
    public boolean isExpired (Date currentDate) {
        if (exp == null || exp.length() != 4)
            return true;
        String now = ISODate.formatDate(currentDate, "yyyyMM");
        try {
            int mm = Integer.parseInt(exp.substring(2));
            int aa = Integer.parseInt(exp.substring(0,2));
            if (aa < 100 && mm > 0 && mm <= 12) {
                String expDate = (aa < 70 ? "20" : "19") + exp;
                if (expDate.compareTo(now) >= 0)
                    return false;
            }
        } catch (NumberFormatException ignored) {
            // NOPMD
        }
        return true;
    }

    /**
     * Returns a new {@link Builder} for constructing a {@link Card}.
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Builder for constructing {@link Card} instances. */
    public static class Builder {
        /** Default card validator instance. */
        public static CardValidator DEFAULT_CARD_VALIDATOR  = new DefaultCardValidator();
        private String pan;
        private String exp;
        private String cvv;
        private String cvv2;
        private String serviceCode;
        private Track1 track1;
        private Track2 track2;
        private Track1.Builder track1Builder = Track1.builder();
        private Track2.Builder track2Builder = Track2.builder();
        private CardValidator validator = DEFAULT_CARD_VALIDATOR;

        private Builder () { }

        /**
         * Sets the primary account number.
         * @param pan the PAN
         * @return this builder
         */
        public Builder pan (String pan) { this.pan = pan; return this; }

        /**
         * Sets the expiry date.
         * @param exp the expiry in YYMM format
         * @return this builder
         */
        public Builder exp (String exp) { this.exp = exp; return this; }

        /**
         * Sets the CVV1 value.
         * @param cvv the CVV1 value
         * @return this builder
         */
        public Builder cvv (String cvv) { this.cvv = cvv; return this; }

        /**
         * Sets the CVV2 value.
         * @param cvv2 the CVV2 value
         * @return this builder
         */
        public Builder cvv2 (String cvv2) { this.cvv2 = cvv2; return this; }

        /**
         * Sets the service code.
         * @param serviceCode the 3-digit service code
         * @return this builder
         */
        public Builder serviceCode (String serviceCode) { this.serviceCode = serviceCode; return this; }

        /**
         * Sets the card validator.
         * @param validator the card validator to use
         * @return this builder
         */
        public Builder validator (CardValidator validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Provides a Track 1 builder.
         * @param track1Builder a {@link Track1.Builder} instance
         * @return this builder
         */
        public Builder withTrack1Builder (Track1.Builder track1Builder) {
            this.track1Builder = track1Builder;
            return this;
        }

        /**
         * Provides a Track 2 builder.
         * @param track2Builder a {@link Track2.Builder} instance
         * @return this builder
         */
        public Builder withTrack2Builder (Track2.Builder track2Builder) {
            this.track2Builder = track2Builder;
            return this;
        }

        /**
         * Sets the Track 1 data.
         * @param track1 the {@link Track1} object
         * @return this builder
         */
        public Builder track1 (Track1 track1) {
            this.track1 = track1;
            return this;
        }

        /**
         * Sets the Track 2 data.
         * @param track2 the {@link Track2} object
         * @return this builder
         */
        public Builder track2 (Track2 track2) {
            this.track2 = track2;
            return this;
        }

        /**
         * Populates card data from an {@link ISOMsg}.
         * Extracts PAN, expiry, track 1, and track 2 from the appropriate fields.
         * @param m an ISOMsg to extract card data from
         * @return this builder
         * @throws InvalidCardException if card data is invalid
         */
        public Builder isomsg (ISOMsg m) throws InvalidCardException {
            if (m.hasField(2))
                pan(m.getString(2));
            if (m.hasField(14))
                exp(m.getString(14));
            if (m.hasField(35))
                track2(track2Builder.track(m.getString(35)).build());
            if (m.hasField(45))
                track1(track1Builder.track(m.getString(45)).build());
            if (pan == null && track2 != null)
                pan (track2.getPan());
            if (pan == null && track1 != null)
                pan (track1.getPan());
            if (exp == null && track2 != null)
                exp (track2.getExp());
            if (exp == null && track1 != null)
                exp (track1.getExp());
            if (track2 != null) {
                if (pan == null)
                    pan (track2.getPan());
                if (exp == null)
                    exp (track2.getExp());
                if (serviceCode == null)
                    serviceCode(track2.getServiceCode());
            }
            if (track1 != null) {
                if (pan == null)
                    pan (track1.getPan());
                if (exp == null)
                    exp (track1.getExp());
                if (serviceCode == null)
                    serviceCode(track1.getServiceCode());
            }
            return this;
        }

        /**
         * Builds and validates the {@link Card}.
         * @return a new Card instance
         * @throws InvalidCardException if the card data is invalid
         */
        public Card build() throws InvalidCardException {
            Card c = new Card(this);
            if (validator != null)
                validator.validate(c);
            return c;
        }

    }
}
