package com.jisolate.properties;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.jisolate.properties.ThreadLocalProperties;

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
                LOG.info("Setting system property {}={}", PROPERTY_NAME, propertyValue);

                threadStarted.countDown();
                long startTime = System.currentTimeMillis();
                do {
                    assertEquals(propertyValue, System.getProperty(PROPERTY_NAME));
                    Thread.sleep(10);
                } while (System.currentTimeMillis() - startTime < THREAD_RUN_TIME);
                propertiesAreIsolated.countDown();
            } catch (Exception e) {
                LOG.warn("Thread failed", e);
                throw Throwables.propagate(e);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalPropertiesTest.class);

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
