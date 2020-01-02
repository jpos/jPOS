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
 * PAN and Expiration dates taken from manual entry, track1, track2. It also corrects the name.
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

    public Track2 (Builder builder) {
        track       = builder.track;
        pan         = builder.pan;
        exp         = builder.exp;
        cvv         = builder.cvv;
        serviceCode = builder.serviceCode;
        discretionaryData  = builder.discretionaryData;
    }

    public String getPan() {
        return pan;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private static String TRACK2_EXPR = "^([0-9]{1,19})[=D]([0-9]{4})([0-9]{3})?([0-9]{4})?([0-9]{1,10})?$";
        private static Pattern TRACK2_PATTERN = Pattern.compile(TRACK2_EXPR);
        private String pan;
        private String exp;
        private String cvv;
        private String serviceCode;
        private String discretionaryData;
        private String track;
        private Pattern pattern = TRACK2_PATTERN;

        private Builder () { }

        public Builder pan (String pan) {
            this.pan = pan; return this;
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
                throw new InvalidCardException ("null track2 data");

            track = s;
            Matcher matcher = pattern.matcher(s);
            int cnt = matcher.groupCount();
            if (matcher.find() && cnt >= 2) {
                pan = matcher.group(1);
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
        public Track2 build() {
            return new Track2(this);
        }
    }
}
