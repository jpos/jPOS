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

package org.jpos.iso;

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

public class FieldPackagerSecurityTest {
    private static final long BASE_SEED = 0x4A504F534649454CL;
    private static final MutationBudget BUDGET = new MutationBudget(120, 128);

    @Test
    public void asciiLengthPrefixesAreHandledSafely() throws ISOException {
        ISOFieldPackager packager = new IFA_LLCHAR(99, "ASCII LLCHAR");
        verify(packager, "ascii-llchar", List.of(
          entry("empty", new byte[] {'0', '0'}, Expectation.ACCEPT),
          entry("value", new byte[] {'0', '3', 'A', 'B', 'C'}, Expectation.ACCEPT),
          entry("truncated-prefix", new byte[] {'0'}, Expectation.REJECT),
          entry("value-truncated", new byte[] {'0', '5', 'A', 'B'}, Expectation.REJECT),
          entry("malformed-prefix", new byte[] {'X', '3', 'A', 'B', 'C'}, Expectation.REJECT),
          entry("exact-maximum", packed(packager, "A".repeat(99)), Expectation.ACCEPT)
        ));
    }

    @Test
    public void bcdLengthPrefixesAreHandledSafely() throws ISOException {
        ISOFieldPackager packager = new IFB_LLCHAR(99, "BCD LLCHAR");
        verify(packager, "bcd-llchar", List.of(
          entry("empty", new byte[] {0x00}, Expectation.ACCEPT),
          entry("value", new byte[] {0x03, 'A', 'B', 'C'}, Expectation.ACCEPT),
          entry("truncated-prefix", new byte[0], Expectation.REJECT),
          entry("value-truncated", new byte[] {0x05, 'A', 'B'}, Expectation.REJECT),
          entry("malformed-prefix", malformedBcdValue(), Expectation.REJECT),
          entry("exact-maximum", packed(packager, "A".repeat(99)), Expectation.ACCEPT)
        ));
    }

    @Test
    public void ebcdicLengthPrefixesAreHandledSafely() throws ISOException {
        ISOFieldPackager packager = new IFE_LLCHAR(99, "EBCDIC LLCHAR");
        verify(packager, "ebcdic-llchar", List.of(
          entry("empty", new byte[] {(byte) 0xF0, (byte) 0xF0}, Expectation.ACCEPT),
          entry("value", packed(packager, "ABC"), Expectation.ACCEPT),
          entry("truncated-prefix", new byte[] {(byte) 0xF0}, Expectation.REJECT),
          entry("value-truncated", new byte[] {(byte) 0xF0, (byte) 0xF5, (byte) 0xC1, (byte) 0xC2},
            Expectation.REJECT),
          entry("malformed-prefix", malformedEbcdicValue(), Expectation.REJECT),
          entry("exact-maximum", packed(packager, "A".repeat(99)), Expectation.ACCEPT)
        ));
    }

    @Test
    public void overflowingDecodedLengthIsRejectedWithoutAllocation() {
        verifyOverflow(
          "ascii-overflow", new AsciiPrefixer(10), "2147483648".getBytes(ISOUtil.CHARSET)
        );
        verifyOverflow(
          "binary-overflow", new BinaryPrefixer(4),
          new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}
        );
        verifyOverflow(
          "bcd-overflow", new BcdPrefixer(10),
          new byte[] {0x21, 0x47, 0x48, 0x36, 0x48}
        );
        verifyOverflow(
          "ebcdic-overflow", new EbcdicPrefixer(10), ISOUtil.asciiToEbcdic("2147483648")
        );
    }

    private static void verify(ISOFieldPackager packager, String targetId, List<CorpusEntry> corpus) {
        SecurityTarget target = new SecurityTarget() {
            @Override
            public String id() {
                return targetId;
            }

            @Override
            public void consume(byte[] input) throws ISOException {
                ISOField first = new ISOField(2);
                packager.unpack(first, input, 0);
                byte[] canonical = packager.pack(first);

                ISOField second = new ISOField(2);
                packager.unpack(second, canonical, 0);
                if (!Arrays.equals(canonical, packager.pack(second)))
                    throw new IllegalStateException("field canonical form is not stable");
            }

            @Override
            public boolean isExpectedRejection(Throwable failure) {
                return failure instanceof ISOException;
            }

            @Override
            public List<InputMutator> mutators() {
                return List.of(new TruncationMutator());
            }
        };
        for (CorpusEntry entry : corpus)
            SecurityHarness.verify(target, entry, BASE_SEED, BUDGET);
    }

    private static byte[] packed(ISOFieldPackager packager, String value) throws ISOException {
        return packager.pack(new ISOField(2, value));
    }

    private static void verifyOverflow(String targetId, Prefixer prefixer, byte[] prefix) {
        ISOFieldPackager packager = new ISOStringFieldPackager(
          Integer.MAX_VALUE, "overflow", NullPadder.INSTANCE, AsciiInterpreter.INSTANCE, prefixer
        );
        verify(packager, targetId, List.of(
          entry("int-overflow", prefix, Expectation.REJECT)
        ));
    }

    private static byte[] malformedBcdValue() {
        byte[] input = new byte[11];
        input[0] = 0x0A;
        Arrays.fill(input, 1, input.length, (byte) 'A');
        return input;
    }

    private static byte[] malformedEbcdicValue() {
        return new byte[] {0x00, (byte) 0xF3, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3};
    }

    private static CorpusEntry entry(String id, byte[] bytes, Expectation expectation) {
        return new CorpusEntry(id, bytes, expectation);
    }
}
