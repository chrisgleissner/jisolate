package com.jisolate.jvm;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.jisolate.AbstractIsolaterTest;
import com.jisolate.Isolate;
import com.jisolate.IsolatedClass;
import com.jisolate.Jisolate;
import com.jisolate.jvm.JvmIsolate;

public class JvmIsolaterTest extends AbstractIsolaterTest {

    @Override
    protected void assertIsolationWorks(Isolate isolate) {
        int resultCode = ((JvmIsolate) isolate).waitFor();
        assertEquals(0, resultCode);
    }

    @Override
    protected Isolate createIsolate() {
        Isolate isolate = Jisolate.jvmIsolation().withMainClass(IsolatedClass.class)
                .withInheritSystemProperties(Lists.newArrayList(SYSTEM_PROPERTY_NAME))
                .withMainClassArguments(FILE_CONTENTS).isolate();
        return isolate;
    }
}
