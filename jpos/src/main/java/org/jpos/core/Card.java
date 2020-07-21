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

import org.jpos.iso.ISODate;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

/**
 * @author apr@jpos.org
 * @since jPOS 2.0.5
 *
 * This class is based on the old 'CardHolder' class and adds support for multiple
 * PAN and Expiration dates taken from manual entry, track1, track2. It also corrects the name.
 */
public class Card {
    private String pan;
    private String exp;
    private String cvv2;
    private String serviceCode;
    private Track1 track1;
    private Track2 track2;
    public static final int BINLEN = 6;

    private Card() { }

    public Card(Builder builder) {
        pan         = builder.pan;
        exp         = builder.exp;
        cvv2        = builder.cvv2;
        serviceCode = builder.serviceCode;
        track1      = builder.track1;
        track2      = builder.track2;
    }

    public String getPan() {
        return pan;
    }

    public BigInteger getPanAsNumber() {
        return new BigInteger(pan);
    }

    public String getExp() {
        return exp;
    }

    public String getCvv2() {
        return cvv2;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public boolean hasTrack1() {
        return track1 != null;
    }

    public boolean hasTrack2() {
        return track2 != null;
    }

    public boolean hasBothTracks() {
        return hasTrack1() && hasTrack2();
    }

    public String getBin () {
        return pan.substring(0, BINLEN);
    }

    @Override
    public String toString() {
        return pan != null ? ISOUtil.protect(pan) : "nil";
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(pan, exp, cvv2, serviceCode, track1, track2);
    }

    public Track1 getTrack1() {
        return track1;
    }

    public Track2 getTrack2() {
        return track2;
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
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
        public Builder pan (String pan) { this.pan = pan; return this; }
        public Builder exp (String exp) { this.exp = exp; return this; }
        public Builder cvv (String cvv) { this.cvv = cvv; return this; }
        public Builder cvv2 (String cvv2) { this.cvv2 = cvv2; return this; }
        public Builder serviceCode (String serviceCode) { this.serviceCode = serviceCode; return this; }
        public Builder validator (CardValidator validator) {
            this.validator = validator;
            return this;
        }
        public Builder withTrack1Builder (Track1.Builder track1Builder) {
            this.track1Builder = track1Builder;
            return this;
        }
        public Builder withTrack2Builder (Track2.Builder track2Builder) {
            this.track2Builder = track2Builder;
            return this;
        }
        public Builder track1 (Track1 track1) {
            this.track1 = track1;
            return this;
        }
        public Builder track2 (Track2 track2) {
            this.track2 = track2;
            return this;
        }
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

        public Card build() throws InvalidCardException {
            Card c = new Card(this);
            if (validator != null)
                validator.validate(c);
            return c;
        }

    }
}
