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

package org.jpos.iso.packager;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.securitytest.harness.CorpusEntry;
import org.jpos.securitytest.harness.Expectation;
import org.jpos.securitytest.harness.InputMutator;
import org.jpos.securitytest.harness.MutationBudget;
import org.jpos.securitytest.harness.SecurityHarness;
import org.jpos.securitytest.harness.SecurityTarget;
import org.jpos.securitytest.harness.TruncationMutator;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class XMLPackagerSecurityTest {
    private static final long BASE_SEED = 0x4A504F53584D4C31L;
    private static final MutationBudget BUDGET = new MutationBudget(24, 256);

    private static final SecurityTarget TARGET = new SecurityTarget() {
        @Override
        public String id() {
            return "xml-packager";
        }

        @Override
        public void consume(byte[] input) throws Exception {
            XMLPackager firstPackager = new XMLPackager();
            ISOMsg first = firstPackager.createISOMsg();
            firstPackager.unpack(first, input);
            byte[] canonical = firstPackager.pack(first);

            XMLPackager secondPackager = new XMLPackager();
            ISOMsg second = secondPackager.createISOMsg();
            secondPackager.unpack(second, canonical);
            if (!Arrays.equals(canonical, secondPackager.pack(second)))
                throw new IllegalStateException("XML canonical form is not stable");
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

    private static final List<CorpusEntry> CORPUS = List.of(
      entry("minimal", "<isomsg/>", Expectation.ACCEPT),
      entry("document-whitespace", " \n<isomsg/>\n ", Expectation.ACCEPT),
      entry(
        "scalar",
        "<isomsg><field id=\"0\" value=\"0800\"/><field id=\"1\" value=\"prefix-\">suffix</field></isomsg>",
        Expectation.ACCEPT
      ),
      entry(
        "binary",
        "<isomsg><field id=\"52\" value=\"0011223344556677\" type=\"binary\"/></isomsg>",
        Expectation.ACCEPT
      ),
      entry(
        "amount",
        "<isomsg><field id=\"4\" value=\"10.00\" type=\"amount\" currency=\"840\"/></isomsg>",
        Expectation.ACCEPT
      ),
      entry(
        "dataset",
        "<isomsg><field id=\"55\" type=\"dataset\"><dataset id=\"37\" format=\"TLV\">" +
          "<element id=\"0x9F26\" value=\"1122334455667788\"/></dataset></field></isomsg>",
        Expectation.ACCEPT
      ),
      entry(
        "nested",
        "<isomsg><field id=\"0\" value=\"0800\"/><isomsg id=\"127\">" +
          "<field id=\"1\" value=\"nested\"/></isomsg></isomsg>",
        Expectation.ACCEPT
      ),
      entry(
        "doctype-internal",
        "<!DOCTYPE isomsg [<!ENTITY mti \"0800\">]><isomsg><field id=\"0\" value=\"&mti;\"/></isomsg>",
        Expectation.REJECT
      ),
      entry(
        "doctype-system",
        "<!DOCTYPE isomsg SYSTEM \"file:///definitely-not-present\"><isomsg/>",
        Expectation.REJECT
      ),
      entry("rootless-field", "<field id=\"1\" value=\"orphan\"/>", Expectation.REJECT),
      entry("rootless-header", "<header>00</header>", Expectation.REJECT),
      entry("rootless-end", "</isomsg>", Expectation.REJECT),
      entry("invalid-id", "<isomsg><field id=\"invalid\" value=\"value\"/></isomsg>", Expectation.REJECT),
      new CorpusEntry("depth-33", depth33(), Expectation.REJECT),
      entry("malformed-close", "<isomsg><field id=\"1\">value</isomsg>", Expectation.REJECT),
      new CorpusEntry("large-text-128k", largeText(), Expectation.ACCEPT)
    );

    @Test
    public void deterministicCorpusIsHandledSafely() {
        for (CorpusEntry entry : CORPUS)
            SecurityHarness.verify(TARGET, entry, BASE_SEED, BUDGET);
    }

    private static CorpusEntry entry(String id, String xml, Expectation expectation) {
        return new CorpusEntry(id, xml.getBytes(StandardCharsets.UTF_8), expectation);
    }

    private static byte[] depth33() {
        StringBuilder xml = new StringBuilder("<isomsg>");
        for (int i = 1; i < 33; i++)
            xml.append("<isomsg id=\"").append(i).append("\">");
        xml.append("</isomsg>".repeat(33));
        return xml.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] largeText() {
        String xml = "<isomsg><field id=\"1\">" + "x".repeat(128 * 1024) + "</field></isomsg>";
        return xml.getBytes(StandardCharsets.UTF_8);
    }
}
