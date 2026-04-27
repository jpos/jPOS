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

package org.jpos.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Three-argument variant of {@link java.util.function.Function}.
 *
 * @param <T> first argument type
 * @param <U> second argument type
 * @param <V> third argument type
 * @param <R> result type
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t first argument
     * @param u second argument
     * @param v third argument
     * @return the function result
     */
    R apply(T t, U u, V v);

    /**
     * Returns a composed {@code TriFunction} that first applies this function and
     * then applies {@code after} to the result.
     *
     * @param <K> result type of the composed function
     * @param after function applied to the result of this function
     * @return the composed function
     */
    default <K> TriFunction<T, U, V, K> andThen(Function<? super R, ? extends K> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }
}
