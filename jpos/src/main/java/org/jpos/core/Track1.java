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

import org.jpos.iso.ISOUtil;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * This class is based on the old 'CardHolder' class and adds support for multiple
 * PAN and Expiration dates taken from manual entry, track1, track1. It also corrects the name.
 *
 * @author apr@jpos.org
 * @since jPOS 2.0.5
 *
 */

@SuppressWarnings("unused")
public class Track1 {
    private String pan;
    private String nameOnCard;
    private String exp;
    private String serviceCode;
    private String cvv;
    private String discretionaryData;
    private String track;

    private Track1 () { }

    /**
     * Copies the track1 fields from the supplied {@link Builder}.
     *
     * @param builder builder carrying the parsed or assembled track1 fields
     */
    public Track1 (Builder builder) {
        pan                = builder.pan;
        nameOnCard         = builder.nameOnCard;
        exp                = builder.exp;
        cvv                = builder.cvv;
        discretionaryData  = builder.discretionaryData;
        serviceCode        = builder.serviceCode;
        track              = builder.track;
    }

    /**
     * Returns the primary account number.
     *
     * @return primary account number
     */
    public String getPan() {
        return pan;
    }

    /**
     * Returns the cardholder name encoded on the track.
     *
     * @return cardholder name as encoded on the track
     */
    public String getNameOnCard() {
        return nameOnCard;
    }

    /**
     * Returns the expiration date.
     *
     * @return expiration date in {@code YYMM} form, or {@code null} if absent
     */
    public String getExp() {
        return exp;
    }

    /**
     * Returns the CVV/CVC value, when present.
     *
     * @return CVV/CVC value, or {@code null} if not present in the track
     */
    public String getCvv() {
        return cvv;
    }

    /**
     * Returns the service code.
     *
     * @return three-digit service code, or {@code null} if absent
     */
    public String getServiceCode() {
        return serviceCode;
    }

    /**
     * Returns the discretionary data trailing the service code.
     *
     * @return remaining discretionary data, or {@code null} if absent
     */
    public String getDiscretionaryData() {
        return discretionaryData;
    }

    /**
     * Returns the raw track1 string this object was built from.
     *
     * @return raw track1 string this object was built from, or {@code null} when assembled programmatically
     */
    public String getTrack() {

        return track;
    }

    /**
     * Returns {@code true} when the service code marks this as an IC (EMV) card.
     *
     * @return {@code true} if the Track 1 service code indicates an IC card
     */
    public boolean isEMV() {
        return isICCard();
    }

    /**
     * Returns {@code true} when the service code marks this as an IC card
     * (first digit {@code 2} for international or {@code 6} for national).
     *
     * @return {@code true} if the Track 1 service code indicates an IC card
     */
    public boolean isICCard() {
        return serviceCode != null && serviceCode.length() == 3 &&
          (serviceCode.charAt(0) == '2' || serviceCode.charAt(0) == '6');
    }

    /**
     * Returns {@code true} when the service code marks this as an
     * internationally-usable IC card (first digit {@code 2}).
     *
     * @return {@code true} if the Track 1 service code indicates an international IC card
     */
    public boolean isInternationalICCard() {
        return serviceCode != null && serviceCode.length() == 3 && serviceCode.charAt(0) == '2';
    }

    @Override
    public String toString() {
        return pan != null ? ISOUtil.protect(pan) : "nil";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track1 track11 = (Track1) o;
        return Objects.equals(pan, track11.pan) &&
          Objects.equals(nameOnCard, track11.nameOnCard) &&
          Objects.equals(exp, track11.exp) &&
          Objects.equals(serviceCode, track11.serviceCode) &&
          Objects.equals(cvv, track11.cvv) &&
          Objects.equals(discretionaryData, track11.discretionaryData) &&
          Objects.equals(track, track11.track);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pan, nameOnCard, exp, serviceCode, cvv, discretionaryData, track);
    }

    /**
     * Creates a new builder for assembling a {@code Track1}.
     *
     * @return a new {@link Builder} for assembling a {@code Track1}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder that parses a raw track1 string or assembles a {@code Track1}
     * from individual fields and validates the result against a configurable pattern.
     */
    public static class Builder {
        private static String TRACK1_EXPR = "^[%]?[A-Z]+([0-9]{1,19})\\^([^\\^]{2,26})\\^([0-9]{4})([0-9]{3})([0-9]{4})?([0-9]{1,10})?";
        private static Pattern TRACK1_PATTERN = Pattern.compile(TRACK1_EXPR);
        private String pan;
        private String nameOnCard;
        private String exp;
        private String cvv;
        private String serviceCode;
        private String discretionaryData;
        private String track;
        private Pattern pattern = TRACK1_PATTERN;
        private Builder () { }

        /**
         * Sets the primary account number.
         *
         * @param pan primary account number
         * @return this builder
         */
        public Builder pan (String pan) {
            this.pan = pan; return this;
        }

        /**
         * Sets the cardholder name.
         *
         * @param nameOnCard cardholder name as encoded on the track
         * @return this builder
         */
        public Builder nameOnCard (String nameOnCard) {
            this.nameOnCard = nameOnCard;
            return this;
        }

        /**
         * Sets the expiration date.
         *
         * @param exp expiration date in {@code YYMM} form
         * @return this builder
         */
        public Builder exp (String exp) {
            this.exp = exp; return this;
        }

        /**
         * Sets the CVV/CVC value.
         *
         * @param cvv CVV/CVC value
         * @return this builder
         */
        public Builder cvv (String cvv) {
            this.cvv = cvv; return this;
        }

        /**
         * Sets the service code.
         *
         * @param serviceCode three-digit service code
         * @return this builder
         */
        public Builder serviceCode (String serviceCode) {
            this.serviceCode = serviceCode; return this;
        }

        /**
         * Sets the discretionary data trailing the service code.
         *
         * @param discretionaryData discretionary data trailing the service code
         * @return this builder
         */
        public Builder discretionaryData (String discretionaryData) {
            this.discretionaryData = discretionaryData;
            return this;
        }

        /**
         * Optional method, can be used to override default pattern
         * @param pattern overrides default pattern
         * @return this builder
         */
        public Builder pattern (Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        /**
         * Parses a raw track1 string and populates the builder fields.
         *
         * @param s raw track1 data
         * @return this builder
         * @throws InvalidCardException if {@code s} is null or does not match
         *                              the configured pattern
         */
        public Builder track (String s)
          throws InvalidCardException
        {
            if (s == null)
                throw new InvalidCardException ("null track1 data");

            track = s;
            Matcher matcher = pattern.matcher(s);
            int cnt = matcher.groupCount();
            if (matcher.find() && cnt >= 2) {
                pan = matcher.group(1);
                nameOnCard = matcher.group(2);
                if (cnt > 2)
                    exp = matcher.group(3);
                if (cnt > 3)
                    serviceCode = matcher.group(4);
                if (cnt > 4)
                    cvv = matcher.group(5);
                if (cnt > 5)
                    discretionaryData = matcher.group(6);
            } else {
                throw new InvalidCardException ("invalid track1");
            }
            return this;
        }

        /**
         * Constructs the Track1 data based on the card data provided.
         * The generated Track1 data is validated using the pattern.
         * If the Track1 data doesn't match the pattern, the track attribute keeps the original value.
         * @return this builder.
         */
        public Builder buildTrackData() {
            StringBuilder track1 = new StringBuilder("%");
            track1.append("B");
            track1.append(this.pan);
            track1.append("^");
            track1.append(this.nameOnCard);
            track1.append("^");
            track1.append(this.exp);
            track1.append(this.serviceCode);
            track1.append(this.cvv);
            track1.append(this.discretionaryData);

            Matcher matcher = this.pattern.matcher(track1);
            int cnt = matcher.groupCount();
            if (matcher.find() && cnt >= 2)
                this.track = track1.toString();

            return this;
        }

        /**
         * Builds the immutable {@link Track1}. If no raw track string was set,
         * one is assembled from the individual fields via {@link #buildTrackData()}.
         *
         * @return the built {@link Track1}
         */
        public Track1 build() {
            if (this.track == null)
                buildTrackData();
            return new Track1(this);
        }
    }
}
