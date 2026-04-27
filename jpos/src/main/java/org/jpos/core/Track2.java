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
 * PAN and Expiration dates taken from manual entry, track1, track2. It also corrects the name.
 *
 * @author apr@jpos.org
 * @since jPOS 2.0.5
 *
 */
@SuppressWarnings("unused")
public class Track2 {
    private String pan;
    private String exp;
    private String cvv;
    private String serviceCode;
    private String discretionaryData;
    private String track;

    private Track2 () { }

    /**
     * Copies the track2 fields from the supplied {@link Builder}.
     *
     * @param builder builder carrying the parsed or assembled track2 fields
     */
    public Track2 (Builder builder) {
        track       = builder.track;
        pan         = builder.pan;
        exp         = builder.exp;
        cvv         = builder.cvv;
        serviceCode = builder.serviceCode;
        discretionaryData  = builder.discretionaryData;
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
     * @return remaining discretionary data trailing the service code, or {@code null}
     */
    public String getDiscretionaryData() {
        return discretionaryData;
    }

    /**
     * Returns the raw track2 string this object was built from.
     *
     * @return raw track2 string this object was built from, or {@code null} when assembled programmatically
     */
    public String getTrack() {
        return track;
    }

    @Override
    public String toString() {
        return pan != null ? ISOUtil.protect(pan) : "nil";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track2 track21 = (Track2) o;
        return Objects.equals(pan, track21.pan) &&
          Objects.equals(exp, track21.exp) &&
          Objects.equals(cvv, track21.cvv) &&
          Objects.equals(serviceCode, track21.serviceCode) &&
          Objects.equals(discretionaryData, track21.discretionaryData) &&
          Objects.equals(track, track21.track);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pan, exp, cvv, serviceCode, discretionaryData, track);
    }

    /**
     * Creates a new builder for assembling a {@code Track2}.
     *
     * @return a new {@link Builder} for assembling a {@code Track2}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder that parses a raw track2 string or assembles a {@code Track2}
     * from individual fields and validates the result against a configurable pattern.
     */
    public static class Builder {
        private static String TRACK2_EXPR = "^([0-9]{1,19})[=D]([0-9]{4})?([0-9]{3})?([0-9]{4})?([0-9]{1,})?$";
        private static Pattern TRACK2_PATTERN = Pattern.compile(TRACK2_EXPR);
        private String pan;
        private String exp;
        private String cvv;
        private String serviceCode;
        private String discretionaryData;
        private String track;
        private Pattern pattern = TRACK2_PATTERN;

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
         * Parses a raw track2 string and populates the builder fields.
         *
         * @param s raw track2 data
         * @return this builder
         * @throws InvalidCardException if {@code s} is null, exceeds 37 characters,
         *                              or does not match the configured pattern
         */
        public Builder track (String s)
          throws InvalidCardException
        {
            if (s == null)
                throw new InvalidCardException ("null track2 data");
            if (s.length() > 37)
                throw new InvalidCardException("track2 too long");

            track = s;
            Matcher matcher = pattern.matcher(s);
            int cnt = matcher.groupCount();
            if (matcher.find() && cnt >= 1) {
                pan = matcher.group(1);
                if (cnt > 1)
                    exp = matcher.group(2);
                if (cnt > 2)
                    serviceCode = matcher.group(3);
                if (cnt > 3)
                    cvv = matcher.group(4);
                if (cnt > 4)
                    discretionaryData = matcher.group(5);
            } else {
                throw new InvalidCardException ("invalid track2");
            }
            return this;
        }

        /**
         * Constructs the Track2 data based on the card data provided.
         * The generated Track2 data is validated using the pattern.
         * If the Track2 data doesn't match the pattern, the track attribute keeps the original value.
         * @return this builder.
         */
        public Builder buildTrackData() {
            StringBuilder track2 = new StringBuilder(this.pan);
            track2.append("=");
            track2.append(this.exp);
            track2.append(this.serviceCode);
            track2.append(this.cvv);
            track2.append(this.discretionaryData);

            Matcher matcher = this.pattern.matcher(track2);
            int cnt = matcher.groupCount();
            if (matcher.find() && cnt >= 1)
                this.track = track2.toString();

            return this;
        }

        /**
         * Builds the immutable {@link Track2}. If no raw track string was set,
         * one is assembled from the individual fields via {@link #buildTrackData()}.
         *
         * @return the built {@link Track2}
         */
        public Track2 build() {
            if (this.track == null)
                buildTrackData();
            return new Track2(this);
        }
    }
}
