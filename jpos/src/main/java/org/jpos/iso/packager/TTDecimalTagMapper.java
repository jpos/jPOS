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

package org.jpos.iso.packager;

/**
 * The {@code TagMapper} provides convertions between two decimal tags
 * {@code TT} and subtags.
 * <p>
 * Example of mappings:
 * <ul>
 *   <li>'03' &lt;-&gt; 3
 *   <li>'23' &lt;-&gt; 23
 *   <li>...
 *   <li>'99' &lt;-&gt; 99
 * </ul>
 * and additionally containing upper case letters <i>(based on base 36 with offset 100)</i>
 * <ul>
 *   <li>'0A' &lt;-&gt; 110 <i>(100 + 10)</i>
 *   <li>'0B' &lt;-&gt; 111 <i>(100 + 11)</i>
 *   <li>...
 *   <li>'9Z' &lt;-&gt; 424 <i>(100 + 36<sup>1</sup>*9 + 35)</i>
 *   <li>...
 *   <li>'ZZ' &lt;-&gt; 1395 <i>(100 + 36<sup>1</sup>*35 + 35)</i>
 * </ul>
 * Letters mappings that duplicate decimal mappings are prohibited, eg.:
 * <ul>
 *   <li>'00' &lt;-&gt; 100 <i>(100 + 0)</i>
 *   <li>'01' &lt;-&gt; 101 <i>(100 + 1)</i>
 *   <li>'09' &lt;-&gt; 109 <i>(100 + 9)</i>
 *   <li>...
 *   <li>'10' &lt;-&gt; 136 <i>(100 + 1*36<sup>1</sup> + 0)</i>
 *   <li>'11' &lt;-&gt; 137 <i>(100 + 1*36<sup>1</sup> + 1)</i>
 *   <li>...
 * </ul>
 *
 * @author Michał Wiercioch
 */
public class TTDecimalTagMapper extends DecimalTagMapper {

    public TTDecimalTagMapper() {
        super(2);
    }

}
