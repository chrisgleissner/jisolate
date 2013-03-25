package com.purelypro.isolate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.purelypro.isolate.Isolate;

public abstract class AbstractIsolaterTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIsolaterTest.class);
    private static final long MAX_WAIT_TIME = 300000000;
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

        long startTime = System.currentTimeMillis();
        while (!FILE.exists() && System.currentTimeMillis() - startTime < MAX_WAIT_TIME) {
            Thread.sleep(50);
        }
        LOG.info("Isolate was created in {}ms", System.currentTimeMillis() - startTime);
        String actualFileContents = FileUtils.readFileToString(FILE);

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
