/*
 * Copyright (c) 2002-2020 Manorrock.com. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *   3. Neither the name of the copyright holder nor the names of its 
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package jakarta.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import jakarta.servlet.UnavailableException;

/**
 * The JUnit tests for the UnavailableException class.
 *
 * @author Manfred Riem (mriem@manorrock.com)
 */
public class UnavailableExceptionTest {

    /**
     * Test constructor.
     */
    @Test
    public void testConstructor() {
        UnavailableException exception = new UnavailableException("Unavailable", 100);
        assertEquals(100, exception.getUnavailableSeconds());
    }

    /**
     * Test constructor.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testConstructor2() {
        new UnavailableException(null, "Message");
    }

    /**
     * Test constructor.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testConstructor3() {
        new UnavailableException(100, null, "Message");
    }

    /**
     * Test getServlet method.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetServlet() {
        UnavailableException exception = new UnavailableException("Unavailable");
        exception.getServlet();
    }

    /**
     * Test getUnavailableSeconds method.
     */
    @Test
    public void testGetUnavailableSeconds() {
        UnavailableException exception = new UnavailableException("Unavailable");
        assertEquals(-1, exception.getUnavailableSeconds());
    }

    /**
     * Test isPermanent method.
     */
    @Test
    public void testIsPermanent() {
        UnavailableException exception = new UnavailableException("Unavailable");
        assertTrue(exception.isPermanent());
    }
}
