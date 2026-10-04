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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TruncationMutator implements InputMutator {
    @Override
    public String id() {
        return "truncate";
    }

    @Override
    public List<Mutation> mutate(CorpusEntry input, long seed, MutationBudget budget) {
        byte[] source = input.bytes();
        int capacity = Math.min(source.length, budget.maxCases());
        List<Mutation> mutations = new ArrayList<>(capacity);
        for (int length = 0;
             length < source.length && length <= budget.maxInputBytes() && mutations.size() < budget.maxCases();
             length++) {
            String mutationId = Integer.toString(length);
            long mutationSeed = DeterministicSeeds.derive(seed, input.id(), mutationId);
            mutations.add(new Mutation(mutationId, mutationSeed, Arrays.copyOf(source, length)));
        }
        return List.copyOf(mutations);
    }
}
