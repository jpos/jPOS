/*
 * Copyright (c) 2000 jPOS.org. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: 1.
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The end-user documentation
 * included with the redistribution, if any, must include the following
 * acknowledgment: "This product includes software developed by the jPOS
 * project (http://www.jpos.org/)". Alternately, this acknowledgment may appear
 * in the software itself, if and wherever such third-party acknowledgments
 * normally appear. 4. The names "jPOS" and "jPOS.org" must not be used to
 * endorse or promote products derived from this software without prior written
 * permission. For written permission, please contact license@jpos.org. 5.
 * Products derived from this software may not be called "jPOS", nor may "jPOS"
 * appear in their name, without prior written permission of the jPOS project.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE JPOS
 * PROJECT OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the jPOS Project. For more information please see
 * <http://www.jpos.org/> .
 */

package iso;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all the unit tests for the new ISOPackagers.
 * @author jonathan.oconnor@xcom.de
 */
public class ISOTests
{
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Tests for new ISO Field Packagers");
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(LeftPadderTest.class));
        suite.addTest(new TestSuite(RightPadderTest.class));
        suite.addTest(new TestSuite(AsciiInterpreterTest.class));
        suite.addTest(new TestSuite(EbcdicInterpreterTest.class));
        suite.addTest(new TestSuite(LiteralInterpreterTest.class));
        suite.addTest(new TestSuite(AsciiPrefixerTest.class));
        suite.addTest(new TestSuite(EbcdicPrefixerTest.class));
        suite.addTest(new TestSuite(AsciiHexInterpreterTest.class));
        
        suite.addTest(new TestSuite(IF_CHARTest.class));
        suite.addTest(new TestSuite(IF_NOPTest.class));
        suite.addTest(new TestSuite(IF_UNUSEDTest.class));
        suite.addTest(new TestSuite(IFA_AMOUNTTest.class));
        suite.addTest(new TestSuite(IFA_BINARYTest.class));
        suite.addTest(new TestSuite(IFA_FLLCHARTest.class));
        suite.addTest(new TestSuite(IFA_FLLNUMTest.class));
        suite.addTest(new TestSuite(IFA_LCHARTest.class));
        suite.addTest(new TestSuite(IFA_LLBINARYTest.class));
        suite.addTest(new TestSuite(IFA_LLBNUMTest.class));
        suite.addTest(new TestSuite(IFA_LLCHARTest.class));
        suite.addTest(new TestSuite(IFA_LLLBINARYTest.class));
        suite.addTest(new TestSuite(IFA_LLLCHARTest.class));
        suite.addTest(new TestSuite(IFA_LLLLCHARTest.class));
        suite.addTest(new TestSuite(IFA_LLLLLCHARTest.class));
        suite.addTest(new TestSuite(IFA_LLLNUMTest.class));
        suite.addTest(new TestSuite(IFA_LLNUMTest.class));
        suite.addTest(new TestSuite(IFA_NUMERICTest.class));
        suite.addTest(new TestSuite(IFB_AMOUNTTest.class));
        suite.addTest(new TestSuite(IFB_BINARYTest.class));
        suite.addTest(new TestSuite(IFB_LLBINARYTest.class));
        suite.addTest(new TestSuite(IFB_LLCHARTest.class));
        suite.addTest(new TestSuite(IFB_LLHBINARYTest.class));
        suite.addTest(new TestSuite(IFB_LLHCHARTest.class));
        suite.addTest(new TestSuite(IFB_LLHECHARTest.class));
        suite.addTest(new TestSuite(IFB_LLHFBINARYTest.class));
        suite.addTest(new TestSuite(IFB_LLHNUMTest.class));
        suite.addTest(new TestSuite(IFB_LLLBINARYTest.class));
        suite.addTest(new TestSuite(IFB_LLLCHARTest.class));
        suite.addTest(new TestSuite(IFB_LLLHBINARYTest.class));
        suite.addTest(new TestSuite(IFB_LLLHECHARTest.class));
        suite.addTest(new TestSuite(IFB_LLLNUMTest.class));
        suite.addTest(new TestSuite(IFB_LLNUMTest.class));
        suite.addTest(new TestSuite(IFB_NUMERICTest.class));
        suite.addTest(new TestSuite(IFE_CHARTest.class));
        suite.addTest(new TestSuite(IFE_LLCHARTest.class));
        suite.addTest(new TestSuite(IFE_LLLBINARYTest.class));
        suite.addTest(new TestSuite(IFE_LLLCHARTest.class));
        suite.addTest(new TestSuite(IFE_LLNUMTest.class));
        suite.addTest(new TestSuite(IFE_NUMERICTest.class));
        suite.addTest(new TestSuite(IFB_LLLHCHARTest.class));
        //suite.addTest(new TestSuite(IFEB_LLLNUMTest.class));
        //suite.addTest(new TestSuite(IFEB_LLNUMTest.class));
        //suite.addTest(new TestSuite(IFEP_LLCHARTest.class));
        //suite.addTest(new TestSuite(IFIPM_LLLCHARTest.class));
        //suite.addTest(new TestSuite(IFMC_LLCHARTest.class));
        //$JUnit-END$
        return suite;
    }
}
