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

package org.jpos.bsh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import static org.apache.commons.lang3.JavaVersion.JAVA_10;
import static org.apache.commons.lang3.JavaVersion.JAVA_14;
import static org.apache.commons.lang3.SystemUtils.isJavaVersionAtMost;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import org.jpos.core.Configuration;
import org.jpos.core.SimpleConfiguration;
import org.jpos.iso.channel.LogChannel;
import org.jpos.util.LogEvent;
import org.junit.jupiter.api.Test;

import bsh.NameSpace;

public class BSHLogListenerTest {

    @Test
    public void testAddScriptInfo() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        bSHLogListener.addScriptInfo("testBSHLogListenerFilename", "testBSHLogListenerCode", 100L);
        assertEquals(1, bSHLogListener.scripts.size(), "bSHLogListener.scripts.size()");
    }

    @Test
    public void testAddScriptInfoThrowsNullPointerException() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        try {
            bSHLogListener.addScriptInfo(null, "testBSHLogListenerCode", 100L);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("The script file name cannot be null", ex.getMessage());
            assertEquals(0, bSHLogListener.scripts.size());
        }
    }

    @Test
    public void testConstructor() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        assertEquals(0, bSHLogListener.scripts.size(), "bSHLogListener.scripts.size()");
    }

    @Test
    public void testGetScriptInfo() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        BSHLogListener.ScriptInfo result = bSHLogListener.getScriptInfo("testBSHLogListenerFilename");
        assertNull(result, "result");
        assertEquals(0, bSHLogListener.scripts.size(), "bSHLogListener.scripts.size()");
    }

    @Test
    public void testGetScriptInfo1() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        bSHLogListener.addScriptInfo("Itag", "testBSHLogListenerCode", 100L);
        BSHLogListener.ScriptInfo result = bSHLogListener.getScriptInfo("Itag");
        assertEquals("testBSHLogListenerCode", result.getCode(), "result.getCode()");
        assertEquals(1, bSHLogListener.scripts.size(), "bSHLogListener.scripts.size()");
    }

    @Test
    public void testGetScriptInfoThrowsNullPointerException() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        try {
            bSHLogListener.getScriptInfo(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            assertEquals("The script file name cannot be null", ex.getMessage());
            assertEquals(0, bSHLogListener.scripts.size());
        }
    }

    @Test
    public void testLoadCodeThrowsFileNotFoundException() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        File f = new File("testBSHLogListenerParam1");
        try {
            bSHLogListener.loadCode(f);
            fail("Expected FileNotFoundException to be thrown");
        } catch (FileNotFoundException ex) {
            assertEquals(FileNotFoundException.class, ex.getClass(), "ex.getClass()");
            assertEquals("testBSHLogListenerParam1", f.getName(), "f.getName()");
        }
    }

    @Test
    public void testLoadCodeThrowsNullPointerException() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        try {
            bSHLogListener.loadCode(null);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    @Test
    public void testLog() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        Configuration cfg = new SimpleConfiguration(new Properties());
        bSHLogListener.setConfiguration(cfg);
        LogEvent ev = new LogEvent("testBSHLogListenerTag");
        ev.setSource(new LogChannel());
        LogEvent result = bSHLogListener.log(ev);
        assertSame(ev, result, "result");
        assertSame(cfg, bSHLogListener.cfg, "bSHLogListener.cfg");
    }

    @Test
    public void testReplace() throws Throwable {
        String[] src = new String[87];
        src[0] = "x9";
        src[1] = "testString";
        src[2] = "testString";
        src[3] = "testString";
        src[4] = "D";
        src[5] = "$vnwI &I=+SgEHrK:s<G@mEj*fv!.zH>ly({cw t";
        src[6] = "testString";
        src[7] = "preload-scripts";
        src[8] = "4$%h\fZdi\\I";
        src[9] = "testString";
        src[10] = "6d_-4xsb?]^";
        src[11] = "\uA4F9\u308C\u9793$\uE72F\u6318\uDDA2\u9353\u2DD9\u1499\uC0B1\uEFBF\u0677\uC801";
        src[12] = "\uFDB5\uC8D8\u5CBD\u7FAD\u188E\u8209\u3EA8\u483C\u3038\u3729\uA8E3\u653B\uD1E0\u610B\u4C3D\u34EE\u7196\u43A5\u709E\u38EA\u1669\uA386\u3A10";
        src[13] = "testString";
        src[14] = "cfV";
        src[15] = "fg";
        src[16] = "testString";
        src[17] = "\f7q\"";
        src[18] = "testString";
        src[19] = "testString";
        src[20] = "x";
        src[21] = "testString";
        src[22] = "testString";
        src[23] = "testString";
        src[24] = "testString";
        src[25] = "1tx}czDv4UFdmi3\".z^,Fn2ijQ*ho$@5Gv8~J";
        src[26] = "testString";
        src[27] = "testString";
        src[28] = "_\\2sTMdq6}\"%ue";
        src[29] = "cf6";
        src[30] = "$`WG;I:v 1$_~vS+#\t]v=V";
        src[31] = "testString";
        src[32] = "\n8,asxain:=uH.kRd";
        src[33] = "testString";
        src[34] = "testString";
        src[35] = "ico$~vz#NKu\\jEF}+a1\\VBMMg>hwR^pzB[WUJW%d5Ua";
        src[36] = "$~\f%\r\\?@e]kO5a-";
        src[37] = "\u0EEF$\u4B77";
        src[38] = "$\u8807";
        src[39] = "cf`g";
        src[40] = "\u611A$";
        src[41] = "Su[7Fj";
        src[42] = "testString";
        src[43] = "testString";
        src[44] = "testString";
        src[45] = "cfg";
        src[46] = "testString";
        src[47] = "#*O32 hp1Wz&BtY^?;s+Eb*U$c=f)j?DxpRdMNE";
        src[48] = "vX:$b";
        src[49] = "$";
        src[50] = "testString";
        src[51] = "testString";
        src[52] = "1_";
        src[53] = "savmNameSpace";
        src[54] = "testString";
        src[55] = "$o";
        src[56] = "testString";
        src[57] = "testString";
        src[58] = "$\u36B9";
        src[59] = "testString";
        src[60] = "$";
        src[61] = "testString";
        src[62] = "reloa";
        src[63] = "testString";
        src[64] = "";
        src[65] = "$i";
        src[66] = "Negative time";
        src[67] = "\u07D4\u828F\u468A\u41C5$\u5474";
        src[68] = "testString";
        src[69] = "testString";
        src[70] = "relad";
        src[71] = "testString";
        src[72] = "$?~\\7W}dT^^lA#jo$&:WGzIm?bV";
        src[73] = "testString";
        src[74] = "testString";
        src[75] = "$lp";
        src[76] = "";
        src[77] = "r)load";
        src[78] = "testString";
        src[79] = "$n\t?-\u001C?1;F\u000E\u001F\t?Y5u(vv TLD";
        src[80] = "realm";
        src[81] = "";
        src[82] = "testString";
        src[83] = "testString";
        src[84] = "event";
        src[85] = "\n";
        src[86] = "testString";
        String[] patterns = new String[2];
        patterns[0] = "testString";
        patterns[1] = "v";
        String[] to = new String[3];
        String[] result = BSHLogListener.replace(src, patterns, to);
        assertEquals(87, result.length, "result.length");
        assertEquals("x9", result[0], "result[0]");
    }

    @Test
    public void testReplace1() throws Throwable {
        String[] src = new String[2];
        src[0] = "$\u9C99";
        src[1] = "testString";
        String[] patterns = new String[0];
        String[] to = new String[1];
        String[] result = BSHLogListener.replace(src, patterns, to);
        assertEquals(2, result.length, "result.length");
        assertEquals("\u9C99", result[0], "result[0]");
    }

    @Test
    public void testReplace2() throws Throwable {
        String[] src = new String[8];
        src[0] = "testString";
        src[1] = "abcdefghisklmnopqrstuvwxyz";
        src[2] = "testString";
        src[3] = "testString";
        src[4] = "\u53AF\u656B\u53B5\u30E0\u4AB3\u98BF$\uF51B\u7D75\u141A\u4FC6\uB213\uD2B1\uBD90";
        src[5] = "testString";
        src[6] = "testString";
        src[7] = "testString";
        String[] patterns = new String[1];
        patterns[0] = "testString";
        String[] to = new String[1];
        String[] result = BSHLogListener.replace(src, patterns, to);
        assertEquals(8, result.length, "result.length");
        assertEquals("testString", result[0], "result[0]");
    }

    @Test
    public void testReplace3() throws Throwable {
        String[] src = new String[1];
        src[0] = "testString";
        String[] patterns = new String[3];
        String[] to = new String[2];
        String[] result = BSHLogListener.replace(src, patterns, to);
        assertEquals(1, result.length, "result.length");
        assertEquals("testString", result[0], "result[0]");
    }

    @Test
    public void testReplace4() throws Throwable {
        String[] src = new String[0];
        String[] patterns = new String[2];
        String[] to = new String[2];
        String[] result = BSHLogListener.replace(src, patterns, to);
        assertEquals(0, result.length, "result.length");
    }

    @Test
    public void testReplaceThrowsArrayIndexOutOfBoundsException() throws Throwable {
        String[] src = new String[84];
        src[0] = "$i";
        src[1] = "testString";
        src[2] = "W&s*BDBn*OJ$4E$j";
        src[3] = "CxZZ$>";
        String[] patterns = new String[1];
        patterns[0] = ">";
        String[] to = new String[0];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (isJavaVersionAtMost(JAVA_10)) {
                assertEquals("0", ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Index 0 out of bounds for length 0", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException() throws Throwable {
        String[] src = new String[87];
        src[0] = "x9";
        src[1] = "testString";
        src[2] = "testString";
        src[3] = "testString";
        src[4] = "D";
        src[5] = "$vnwI &I=+SgEHrK:s<G@mEj*fv!.zH>ly({cw t";
        src[6] = "testString";
        src[7] = "preload-scripts";
        src[8] = "4$%h\fZdi\\I";
        String[] patterns = new String[2];
        patterns[0] = "testString";
        patterns[1] = "v";
        String[] to = new String[3];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"src[i]\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException1() throws Throwable {
        String[] src = new String[54];
        src[0] = "6$\uBB74";
        src[1] = "testString";
        src[2] = "testString";
        src[3] = "1fg";
        src[4] = "1k";
        src[5] = "testString";
        src[6] = "testString";
        src[7] = "testString";
        src[8] = "testString";
        src[9] = "testString";
        src[10] = "testString";
        src[11] = "K2";
        src[12] = "testString";
        src[13] = "abcdefghi/jklmnopqrstuvwxyz";
        src[14] = "testString";
        src[15] = "$!PuI\f!RGsK=.om+lp!e{";
        String[] patterns = new String[3];
        patterns[0] = "testString";
        patterns[1] = "";
        patterns[2] = ")   ";
        String[] to = new String[0];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"src[i]\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException2() throws Throwable {
        String[] src = new String[87];
        src[0] = "x9";
        src[1] = "testString";
        src[2] = "testString";
        src[3] = "testString";
        src[4] = "D";
        src[5] = "$vnwI &I=+SgEHrK:s<G@mEj*fv!.zH>ly({cw t";
        String[] patterns = new String[2];
        patterns[0] = "testString";
        patterns[1] = "v";
        String[] to = new String[3];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"src[i]\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException3() throws Throwable {
        String[] src = new String[87];
        src[0] = "x9";
        src[1] = "testString";
        src[2] = "testString";
        src[3] = "testString";
        src[4] = "D";
        src[5] = "$vnwI &I=+SgEHrK:s<G@mEj*fv!.zH>ly({cw t";
        String[] patterns = new String[2];
        patterns[0] = "testString";
        String[] to = new String[3];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"value\" because \"tgtStr\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException4() throws Throwable {
        String[] src = new String[54];
        src[0] = "6$\uBB74";
        String[] patterns = new String[3];
        patterns[0] = "testString";
        String[] to = new String[0];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"value\" because \"tgtStr\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException5() throws Throwable {
        String[] src = new String[85];
        src[0] = "l\u0002\u001AFZP\u0019r{\u001449\u00157\u0019lex\u0007\"<h~\f\u00019mF0";
        src[1] = "$\n=A\tg[8%-cIo=#OiLf{k(c'z\n+i7Aavrpk2";
        String[] patterns = new String[2];
        String[] to = new String[3];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"value\" because \"tgtStr\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException6() throws Throwable {
        String[] src = new String[84];
        src[0] = "$i";
        String[] patterns = new String[1];
        String[] to = new String[0];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read field \"value\" because \"tgtStr\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException7() throws Throwable {
        String[] src = new String[8];
        src[0] = "testString";
        String[] patterns = new String[1];
        String[] to = new String[1];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"src[i]\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException8() throws Throwable {
        String[] patterns = new String[3];
        String[] to = new String[2];
        try {
            BSHLogListener.replace(null, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot read the array length because \"src\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testReplaceThrowsNullPointerException9() throws Throwable {
        String[] src = new String[1];
        String[] patterns = new String[0];
        String[] to = new String[2];
        try {
            BSHLogListener.replace(src, patterns, to);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException ex) {
            if (isJavaVersionAtMost(JAVA_14)) {
                assertNull(ex.getMessage(), "ex.getMessage()");
            } else {
                assertEquals("Cannot invoke \"String.length()\" because \"src[i]\" is null", ex.getMessage(), "ex.getMessage()");
            }
        }
    }

    @Test
    public void testScriptInfoConstructor() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo("testScriptInfoCode", 100L);
        assertEquals(100L, scriptInfo.lastModified, "scriptInfo.lastModified");
        assertEquals("testScriptInfoCode", scriptInfo.code, "scriptInfo.code");
    }

    @Test
    public void testScriptInfoConstructor1() throws Throwable {
        NameSpace ns = NameSpace.JAVACODE;
        BSHLogListener.ScriptInfo scriptInfo =  new BSHLogListener.ScriptInfo(ns);
        assertSame(ns, scriptInfo.nameSpace, "scriptInfo.nameSpace");
    }

    @Test
    public void testScriptInfoConstructor2() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo();
        assertNull(scriptInfo.getCode(), "scriptInfo.getCode()");
    }

    @Test
    public void testScriptInfoGetCode() throws Throwable {
        String result = new BSHLogListener.ScriptInfo("testScriptInfoCode", 100L).getCode();
        assertEquals("testScriptInfoCode", result, "result");
    }

    @Test
    public void testScriptInfoGetCode1() throws Throwable {
        String result = new BSHLogListener.ScriptInfo().getCode();
        assertNull(result, "result");
    }

    @Test
    public void testScriptInfoGetLastCheck() throws Throwable {
        long result = new BSHLogListener.ScriptInfo("testScriptInfoCode", 100L).getLastCheck();
        assertEquals(0L, result, "result");
    }

    @Test
    public void testScriptInfoGetLastCheck1() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo("testScriptInfoCode", 100L);
        scriptInfo.setLastCheck(100L);
        long result = scriptInfo.getLastCheck();
        assertEquals(100L, result, "result");
    }

    @Test
    public void testScriptInfoGetLastModified() throws Throwable {
        long result = new BSHLogListener.ScriptInfo(NameSpace.JAVACODE).getLastModified();
        assertEquals(0L, result, "result");
    }

    @Test
    public void testScriptInfoGetLastModified1() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo();
        scriptInfo.setLastModified(100L);
        long result = scriptInfo.getLastModified();
        assertEquals(100L, result, "result");
    }

    @Test
    public void testScriptInfoGetNameSpace() throws Throwable {
        NameSpace ns = NameSpace.JAVACODE;
        NameSpace result = new BSHLogListener.ScriptInfo(ns).getNameSpace();
        assertSame(ns, result, "result");
    }

    @Test
    public void testScriptInfoSetCode() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo();
        scriptInfo.setCode("testScriptInfoCode");
        assertEquals("testScriptInfoCode", scriptInfo.code, "scriptInfo.code");
    }

    @Test
    public void testScriptInfoSetLastCheck() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo("testScriptInfoCode", 100L);
        scriptInfo.setLastCheck(100L);
        assertEquals(100L, scriptInfo.lastCheck, "scriptInfo.lastCheck");
    }

    @Test
    public void testScriptInfoSetLastModified() throws Throwable {
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo(NameSpace.JAVACODE);
        scriptInfo.setLastModified(100L);
        assertEquals(100L, scriptInfo.lastModified, "scriptInfo.lastModified");
    }

    @Test
    public void testScriptInfoSetNameSpace() throws Throwable {
        NameSpace ns = NameSpace.JAVACODE;
        BSHLogListener.ScriptInfo scriptInfo = new BSHLogListener.ScriptInfo(ns);
        scriptInfo.getNameSpace();
        scriptInfo.setNameSpace(ns);
        assertSame(ns, scriptInfo.nameSpace, "scriptInfo.nameSpace");
    }

    @Test
    public void testSetConfiguration() throws Throwable {
        BSHLogListener bSHLogListener = new BSHLogListener();
        Configuration cfg = new SimpleConfiguration(new Properties());
        bSHLogListener.setConfiguration(cfg);
        assertSame(cfg, bSHLogListener.cfg, "bSHLogListener.cfg");
    }
}
