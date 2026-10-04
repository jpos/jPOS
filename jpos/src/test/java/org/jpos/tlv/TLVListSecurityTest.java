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

package org.jpos.tlv;

import org.jpos.iso.ISOUtil;
import org.jpos.securitytest.harness.CorpusEntry;
import org.jpos.securitytest.harness.Expectation;
import org.jpos.securitytest.harness.InputMutator;
import org.jpos.securitytest.harness.MutationBudget;
import org.jpos.securitytest.harness.SecurityHarness;
import org.jpos.securitytest.harness.SecurityTarget;
import org.jpos.securitytest.harness.TruncationMutator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class TLVListSecurityTest {
    private static final long BASE_SEED = 0x4A504F5353454331L;
    private static final MutationBudget BUDGET = new MutationBudget(160, 256);

    private static final SecurityTarget TARGET = new SecurityTarget() {
        @Override
        public String id() {
            return "tlv-list";
        }

        @Override
        public void consume(byte[] input) {
            TLVList first = new TLVList();
            first.unpack(input);
            byte[] canonical = first.pack();

            TLVList second = new TLVList();
            second.unpack(canonical);
            if (!Arrays.equals(canonical, second.pack()))
                throw new IllegalStateException("TLV canonical form is not stable");
        }

        @Override
        public boolean isExpectedRejection(Throwable failure) {
            return failure instanceof IllegalArgumentException;
        }

        @Override
        public List<InputMutator> mutators() {
            return List.of(new TruncationMutator());
        }
    };

    private static final List<CorpusEntry> CORPUS = List.of(
      entry("empty", "", Expectation.ACCEPT),
      entry("short-length", "5A03112233", Expectation.ACCEPT),
      entry("extended-tag", "9F3303E0F8C8", Expectation.ACCEPT),
      entry("long-form-length", "5A8101AA", Expectation.ACCEPT),
      entry("multiple", "9A032610049F370411223344", Expectation.ACCEPT),
      entry("truncated-tag", "9F", Expectation.REJECT),
      entry("missing-long-length", "5A81", Expectation.REJECT),
      entry("value-truncated", "5A0311", Expectation.REJECT),
      entry("negative-int-length", "5A8480000000", Expectation.REJECT),
      entry("overwide-length", "5A85FFFFFFFFFF", Expectation.REJECT)
    );

    @Test
    public void deterministicCorpusIsHandledSafely() {
        for (CorpusEntry entry : CORPUS)
            SecurityHarness.verify(TARGET, entry, BASE_SEED, BUDGET);
    }

    private static CorpusEntry entry(String id, String hex, Expectation expectation) {
        return new CorpusEntry(id, ISOUtil.hex2byte(hex), expectation);
    }
}
