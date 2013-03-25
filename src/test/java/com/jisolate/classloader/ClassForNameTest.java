package com.jisolate.classloader;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassForNameTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClassForNameTest.class);

    @Test
    public void test() throws ClassNotFoundException {
        LOG.info("{}", String[].class.getName());
        LOG.info("{}", Class.forName("[Ljava.lang.String;").getName());
    }
}
