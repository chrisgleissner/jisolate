package com.purelypro.isolate;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsolatedClass {

    private static AtomicInteger invocationCounter = new AtomicInteger();
    private static final Logger LOG = LoggerFactory.getLogger(IsolatedClass.class);

    public static int getNumberOfInvocations() {
        return invocationCounter.get();
    }

    public static void main(String[] args) throws IOException {
        String fileContents = args[0] + " "
                + System.getProperty(AbstractIsolaterTest.SYSTEM_PROPERTY_NAME);
        FileUtils.write(AbstractIsolaterTest.FILE, fileContents);
        System.setProperty(AbstractIsolaterTest.SYSTEM_PROPERTY_NAME, "moon");
        LOG.info(fileContents);
        invocationCounter.incrementAndGet();
    }
}
