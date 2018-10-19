package com.jisolate;

import com.google.common.collect.Lists;
import com.jisolate.jvm.JvmIsolate;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static java.lang.System.currentTimeMillis;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.slf4j.LoggerFactory.getLogger;

public class JisolateTest {
    private static final Logger log = getLogger(JisolateTest.class);

    private static final long MAX_WAIT_TIME = 10000;
    private static final String FILE_CONTENTS = "hello";
    static final String SYSTEM_PROPERTY_NAME = "test-sysprop";
    static final File FILE = new File("target/test-data/" + JisolateTest.class.getSimpleName());

    @Before
    public void setUp() {
        deleteQuietly(FILE);
        assertFalse(FILE.exists());
        System.setProperty(SYSTEM_PROPERTY_NAME, "world");
    }

    @Test
    public void classLoaderIsolation() throws IOException, InterruptedException {
        Jisolate.classLoaderIsolation().withIsolatableClass(IsolatedClass.class).withIsolatableArguments(FILE_CONTENTS).isolate();
        assertIsolationWorks();
    }

    @Test
    public void jvmIsolation() throws IOException, InterruptedException {
        try (JvmIsolate isolate = Jisolate.jvmIsolation().withMainClass(IsolatedClass.class)
                .withInheritSystemProperties(Lists.newArrayList(SYSTEM_PROPERTY_NAME))
                .withMainClassArguments(FILE_CONTENTS).isolate()) {
            assertIsolationWorks();
            assertEquals(0, isolate.waitFor());
        }
    }


    private void assertIsolationWorks() throws IOException, InterruptedException {
        // Wait for the isolated 'Isolate' instance to write to FILE
        long startTime = currentTimeMillis();
        boolean fileWriteOccurred;
        do {
            Thread.sleep(50);
            fileWriteOccurred = FILE.exists() && readFileToString(FILE, defaultCharset()).equals("hello world");
        } while (!fileWriteOccurred && currentTimeMillis() - startTime < MAX_WAIT_TIME);

        log.info("Isolate was created in {}ms", currentTimeMillis() - startTime);
        String actualFileContents = readFileToString(FILE, defaultCharset());

        // Shows that isolate ran
        assertEquals("hello world", actualFileContents);

        // Shows that isolate didn't run in the same JVM as this test
        assertEquals("world", System.getProperty(SYSTEM_PROPERTY_NAME));
        assertEquals(0, IsolatedClass.getNumberOfInvocations());

        // Give stream gobblers time to log output of isolate
        Thread.sleep(100);
    }

}
