/*
 * Copyright (c) 2013 Christian Gleissner.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the jisolate nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jisolate;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.*;

public abstract class AbstractIsolaterTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIsolaterTest.class);
    private static final long MAX_WAIT_TIME = 10000;
    protected static final String FILE_CONTENTS = "hello";
    protected static final String SYSTEM_PROPERTY_NAME = "test-sysprop";
    static final File FILE = new File("target/test-data/"
            + AbstractIsolaterTest.class.getSimpleName());

    @Test
    public void canIsolate() throws IOException, InterruptedException {
        assertFalse(FILE.exists());

        System.setProperty(SYSTEM_PROPERTY_NAME, "world");
        Isolate isolate = createIsolate();
        assertNotNull(isolate);

        // Wait for the isolated 'Isolate' instance to write to FILE
        long startTime = currentTimeMillis();
        boolean fileWriteOccurred;
        do  {
            Thread.sleep(50);
            fileWriteOccurred = FILE.exists() && readFileToString(FILE).equals("hello world");
        } while (!fileWriteOccurred && currentTimeMillis() - startTime < MAX_WAIT_TIME);

        LOG.info("Isolate was created in {}ms", currentTimeMillis() - startTime);
        String actualFileContents = readFileToString(FILE);

        // Shows that isolate ran
        assertEquals("hello world", actualFileContents);

        // Shows that isolate didn't run in the same JVM as this test
        assertEquals("world", System.getProperty(SYSTEM_PROPERTY_NAME));
        assertEquals(0, IsolatedClass.getNumberOfInvocations());

        // Give stream gobblers time to log output of isolate
        Thread.sleep(100);

        assertIsolationWorks(isolate);
    }

    @Before
    public void setUp() {
        FileUtils.deleteQuietly(FILE);
    }

    protected abstract void assertIsolationWorks(Isolate isolate);

    protected abstract Isolate createIsolate();
}
