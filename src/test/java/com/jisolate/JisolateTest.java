package com.jisolate;

import com.google.common.collect.Lists;
import com.jisolate.jvm.JvmIsolate;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static com.jayway.awaitility.Awaitility.await;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JisolateTest {
    public static final String SYSTEM_PROPERTY_NAME = "test-sysprop";
    public static final String SYSTEM_PROPERTY_VALUE = "world";

    public static final File FILE = new File("target/test-data/" + JisolateTest.class.getSimpleName());
    public static final String FILE_CONTENTS = "hello";
    public static final String EXPECTED_FILE_CONTENTS = "hello world";

    @Before
    public void setUp() {
        deleteQuietly(FILE);
        assertFalse(FILE.exists());
        System.setProperty(SYSTEM_PROPERTY_NAME, SYSTEM_PROPERTY_VALUE);
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
        await().atMost(2, SECONDS).until(() -> FILE.exists() && readFileToString(FILE, defaultCharset()).equals(EXPECTED_FILE_CONTENTS));
        String actualFileContents = readFileToString(FILE, defaultCharset());

        // Shows that isolate ran
        assertEquals(EXPECTED_FILE_CONTENTS, actualFileContents);

        // Shows that isolate didn't run in the same JVM as this test
        assertEquals(SYSTEM_PROPERTY_VALUE, System.getProperty(SYSTEM_PROPERTY_NAME));
        assertEquals(0, IsolatedClass.numberOfInvocations.get());

        // Give stream gobblers time to log output of isolate
        Thread.sleep(100);
    }
}
