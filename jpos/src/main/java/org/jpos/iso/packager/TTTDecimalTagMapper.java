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
 * The {@code TagMapper} provides convertions between three decimal tags
 * {@code TTT} and subtags.
 * <p>
 * Example of mappings:
 * <ul>
 *   <li>'003' &lt;-&gt; 3
 *   <li>'023' &lt;-&gt; 23
 *   <li>...
 *   <li>'934' &lt;-&gt; 934
 * </ul>
 * and additionally containing upper case letters <i>(based on base 36 with offset 1000)</i>
 * <ul>
 *   <li>'00A' &lt;-&gt; 1010 <i>(1000 + 10)</i>
 *   <li>'00B' &lt;-&gt; 1011 <i>(1000 + 11)</i>
 *   <li>...
 *   <li>'90Z' &lt;-&gt; 12699 <i>(1000 + 9*36<sup>2</sup> + 35)</i>
 *   <li>...
 *   <li>'ZZZ' &lt;-&gt; 47655 <i>(1000 + 35*36<sup>2</sup> + 35*36<sup>1</sup> + 35)</i>
 * </ul>
 * Letters mappings that duplicate decimal mappings are prohibited, eg.:
 * <ul>
 *   <li>'000' &lt;-&gt; 1000 <i>(1000 + 0)</i>
 *   <li>'001' &lt;-&gt; 1001 <i>(1000 + 1)</i>
 *   <li>'009' &lt;-&gt; 1009 <i>(1000 + 9)</i>
 *   <li>...
 *   <li>'010' &lt;-&gt; 1036 <i>(1000 + 1*36<sup>1</sup> + 0)</i>
 *   <li>'011' &lt;-&gt; 1037 <i>(1000 + 1*36<sup>1</sup> + 1)</i>
 *   <li>...
 * </ul>
 *
 * @author Michał Wiercioch, apr
 */
public class TTTDecimalTagMapper extends DecimalTagMapper {

    public TTTDecimalTagMapper() {
        super(3);
    }

}

