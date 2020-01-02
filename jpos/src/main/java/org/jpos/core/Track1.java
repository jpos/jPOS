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

package org.jpos.core;

import org.jpos.iso.ISOUtil;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author apr@jpos.org
 * @since jPOS 2.0.5
 *
 * This class is based on the old 'CardHolder' class and adds support for multiple
 * PAN and Expiration dates taken from manual entry, track1, track1. It also corrects the name.
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

    public Track1 (Builder builder) {
        pan                = builder.pan;
        nameOnCard         = builder.nameOnCard;
        exp                = builder.exp;
        cvv                = builder.cvv;
        discretionaryData  = builder.discretionaryData;
        serviceCode        = builder.serviceCode;
        track              = builder.track;
    }

    public String getPan() {
        return pan;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getExp() {
        return exp;
    }

    public String getCvv() {
        return cvv;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getDiscretionaryData() {
        return discretionaryData;
    }

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

    public static Builder builder() {
        return new Builder();
    }

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

        public Builder pan (String pan) {
            this.pan = pan; return this;
        }

        public Builder nameOnCard (String nameOnCard) {
            this.nameOnCard = nameOnCard;
            return this;
        }

        public Builder exp (String exp) {
            this.exp = exp; return this;
        }

        public Builder cvv (String cvv) {
            this.cvv = cvv; return this;
        }

        public Builder serviceCode (String serviceCode) {
            this.serviceCode = serviceCode; return this;
        }

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

        public Track1 build() {
            return new Track1(this);
        }
    }
}
