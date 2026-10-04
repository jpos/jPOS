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

package org.jpos.securitytest.harness;

import org.jpos.iso.ISOUtil;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityHarnessTest {
    @Test
    public void corpusAndMutationBytesAreDefensivelyCopied() {
        byte[] source = ISOUtil.hex2byte("010203");
        CorpusEntry entry = new CorpusEntry("sample", source, Expectation.ACCEPT);
        source[0] = 0x7f;
        assertArrayEquals(ISOUtil.hex2byte("010203"), entry.bytes());

        byte[] exposed = entry.bytes();
        exposed[1] = 0x7f;
        assertArrayEquals(ISOUtil.hex2byte("010203"), entry.bytes());

        Mutation mutation = new Mutation("changed", 1L, source);
        byte[] mutationBytes = mutation.bytes();
        mutationBytes[0] = 0;
        assertArrayEquals(source, mutation.bytes());
    }

    @Test
    public void seedsAreStableAndUnambiguous() {
        long first = DeterministicSeeds.derive(303L, "ab", "c");
        assertEquals(first, DeterministicSeeds.derive(303L, "ab", "c"));
        assertNotEquals(first, DeterministicSeeds.derive(303L, "a", "bc"));
        assertNotEquals(first, DeterministicSeeds.derive(304L, "ab", "c"));
    }

    @Test
    public void harnessOrdersMutatorsAndProducesRepeatableCases() {
        CorpusEntry entry = new CorpusEntry("valid", ISOUtil.hex2byte("010203"), Expectation.ACCEPT);
        SecurityTarget target = target(List.of(namedMutator("zeta", (byte) 2), namedMutator("alpha", (byte) 1)));
        MutationBudget budget = new MutationBudget(8, 32);

        List<Mutation> first = SecurityHarness.mutations(target, entry, 303L, budget);
        List<Mutation> second = SecurityHarness.mutations(target, entry, 303L, budget);

        assertEquals(List.of("alpha/one", "zeta/one"), first.stream().map(Mutation::id).toList());
        assertEquals(first.stream().map(Mutation::seed).toList(), second.stream().map(Mutation::seed).toList());
        assertArrayEquals(first.get(0).bytes(), second.get(0).bytes());
    }

    @Test
    public void truncationHonorsCaseAndInputBudgets() {
        CorpusEntry entry = new CorpusEntry("valid", ISOUtil.hex2byte("01020304"), Expectation.ACCEPT);
        List<Mutation> mutations = new TruncationMutator().mutate(
          entry, 303L, new MutationBudget(2, 1)
        );

        assertEquals(2, mutations.size());
        assertArrayEquals(new byte[0], mutations.get(0).bytes());
        assertArrayEquals(new byte[] { 1 }, mutations.get(1).bytes());
    }

    @Test
    public void budgetRejectsInvalidLimits() {
        assertThrows(IllegalArgumentException.class, () -> new MutationBudget(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new MutationBudget(1, -1));
    }

    @Test
    public void verifyHonorsCorpusExpectationsAndAllowsMutationsToAcceptOrReject() {
        AtomicInteger calls = new AtomicInteger();
        SecurityTarget target = new SecurityTarget() {
            @Override
            public String id() {
                return "parser";
            }

            @Override
            public void consume(byte[] input) {
                calls.incrementAndGet();
                if (input.length == 1)
                    throw new IllegalArgumentException("rejected");
            }

            @Override
            public boolean isExpectedRejection(Throwable failure) {
                return failure instanceof IllegalArgumentException;
            }

            @Override
            public List<InputMutator> mutators() {
                return List.of(namedMutator("rejecting", (byte) 1));
            }
        };

        SecurityHarness.verify(
          target, new CorpusEntry("valid", new byte[0], Expectation.ACCEPT),
          303L, new MutationBudget(2, 8)
        );

        assertEquals(2, calls.get());
    }

    @Test
    public void verifyReportsDeterministicCaseContext() {
        CorpusEntry entry = new CorpusEntry("invalid", ISOUtil.hex2byte("0102"), Expectation.REJECT);
        AssertionError failure = assertThrows(
          AssertionError.class,
          () -> SecurityHarness.verify(target(List.of()), entry, 303L, new MutationBudget(1, 8))
        );

        assertTrue(failure.getMessage().contains("target=target"));
        assertTrue(failure.getMessage().contains("corpus=invalid"));
        assertTrue(failure.getMessage().contains("case=corpus"));
        assertTrue(failure.getMessage().contains("baseSeed=0x000000000000012F"));
        assertTrue(failure.getMessage().contains("caseSeed=0x"));
        assertTrue(failure.getMessage().contains("input=0102"));
    }

    @Test
    public void verifyDoesNotTreatErrorsAsExpectedRejections() {
        LinkageError expected = new LinkageError("fatal");
        SecurityTarget target = new SecurityTarget() {
            @Override
            public String id() {
                return "fatal";
            }

            @Override
            public void consume(byte[] input) {
                throw expected;
            }

            @Override
            public boolean isExpectedRejection(Throwable failure) {
                return true;
            }
        };

        LinkageError actual = assertThrows(
          LinkageError.class,
          () -> SecurityHarness.verify(
            target, new CorpusEntry("case", new byte[0], Expectation.REJECT),
            303L, new MutationBudget(1, 8)
          )
        );
        assertSame(expected, actual);
    }

    private SecurityTarget target(List<InputMutator> mutators) {
        return new SecurityTarget() {
            @Override
            public String id() {
                return "target";
            }

            @Override
            public void consume(byte[] input) {
            }

            @Override
            public List<InputMutator> mutators() {
                return mutators;
            }
        };
    }

    private InputMutator namedMutator(String id, byte value) {
        return new InputMutator() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public List<Mutation> mutate(CorpusEntry input, long seed, MutationBudget budget) {
                return List.of(new Mutation("one", seed, new byte[] { value }));
            }
        };
    }
}
