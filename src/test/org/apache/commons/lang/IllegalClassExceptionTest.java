/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.lang;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * JUnit tests.
 * 
 * @author Matthew Hawthorne
 * @version $Id: IllegalClassExceptionTest.java,v 1.1 2003/05/15 04:05:11 bayard Exp $
 * @see IllegalClassException
 */
public class IllegalClassExceptionTest extends TestCase {

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(IllegalClassExceptionTest.class);
    }

    public IllegalClassExceptionTest(String testName) {
        super(testName);
    }

    // testConstructor_classArgs

    public void testConstructor_classArgs_allNullInput() {
        new IllegalClassException(null, null);
    }

    public void testConstructor_classArgs_nullExpected() {
        new IllegalClassException(null, String.class);
    }

    public void testConstructor_classArgs_nullActual() {
        new IllegalClassException(String.class, null);
    }

    //  testConstructor_stringArg

    public void testConstructor_stringArg_nullInput() {
        new IllegalClassException(null);
    }

    // testGetMessage

    public void testGetMessage_classArgs_nullInput() {
        final Throwable t = new IllegalClassException(null, null);
        assertEquals("Expected: null, Actual: null", t.getMessage());
    }

    public void testGetMessage_classArgs_normalInput() {
        final Throwable t =
            new IllegalClassException(String.class, Integer.class);
        assertEquals(
            "Expected: java.lang.String, Actual: java.lang.Integer",
            t.getMessage());
    }

    public void testGetMessage_stringArg_nullInput() {
        final Throwable t = new IllegalClassException(null);
        assertEquals(null, t.getMessage());
    }

    public void testGetMessage_stringArg_validInput() {
        final String message = "message";
        final Throwable t = new IllegalClassException(message);
        assertEquals(message, t.getMessage());
    }

} // IllegalClassExceptionTest
