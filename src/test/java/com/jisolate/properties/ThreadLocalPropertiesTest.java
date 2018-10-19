/*
 * Copyright (c) 2013-2018 Christian Gleissner.
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
package com.jisolate.properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class ThreadLocalPropertiesTest {

    private class PropertyVerification implements Runnable {

        private final CountDownLatch propertiesAreIsolated;
        private final CountDownLatch threadStarted;

        private PropertyVerification(CountDownLatch threadStarted,
                                     CountDownLatch propertiesAreIsolated) {
            this.propertiesAreIsolated = propertiesAreIsolated;
            this.threadStarted = threadStarted;
        }

        public void run() {
            try {
                ThreadLocalProperties.activate();

                String propertyValue = "" + Thread.currentThread().getId();
                System.setProperty(PROPERTY_NAME, propertyValue);
                log.info("Setting system property {}={}", PROPERTY_NAME, propertyValue);

                threadStarted.countDown();
                long startTime = System.currentTimeMillis();
                do {
                    assertEquals(propertyValue, System.getProperty(PROPERTY_NAME));
                    Thread.sleep(10);
                } while (System.currentTimeMillis() - startTime < THREAD_RUN_TIME);
                propertiesAreIsolated.countDown();
            } catch (Exception e) {
                throw new RuntimeException("Thread failed", e);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ThreadLocalPropertiesTest.class);

    private final static String PROPERTY_NAME = "SystemPropertyIsolationTest."
            + System.currentTimeMillis();

    private static final long THREAD_RUN_TIME = 300;

    @Test
    public void multipleThreadsCanHaveDifferentValuesForSameIsolatedPropertyName()
            throws InterruptedException {

        // Set property in main test thread
        System.setProperty(PROPERTY_NAME, "1");
        assertEquals("1", System.getProperty(PROPERTY_NAME));

        final CountDownLatch propertiesAreIsolated = new CountDownLatch(2);
        final CountDownLatch threadsStarted = new CountDownLatch(2);

        // Start threads which will assign new values to the property
        new Thread(new PropertyVerification(threadsStarted, propertiesAreIsolated)).start();
        new Thread(new PropertyVerification(threadsStarted, propertiesAreIsolated)).start();
        threadsStarted.await(1000, TimeUnit.MILLISECONDS);

        // Verify the original value remains unaffected whilst the threads are still running
        assertEquals("1", System.getProperty(PROPERTY_NAME));
        propertiesAreIsolated.await(1000, TimeUnit.MILLISECONDS);

        // ...and after they terminate
        assertEquals("1", System.getProperty(PROPERTY_NAME));
    }
}
