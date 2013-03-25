package com.purelypro.isolate.jvm;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.purelypro.isolate.AbstractIsolaterTest;
import com.purelypro.isolate.Isolate;
import com.purelypro.isolate.IsolatedClass;
import com.purelypro.isolate.Isolation;
import com.purelypro.isolate.jvm.JvmIsolate;

public class JvmIsolaterTest extends AbstractIsolaterTest {

    @Override
    protected void assertIsolationWorks(Isolate isolate) {
        int resultCode = ((JvmIsolate) isolate).waitFor();
        assertEquals(0, resultCode);
    }

    @Override
    protected Isolate createIsolate() {
        Isolate isolate = Isolation.jvmIsolation().withMainClass(IsolatedClass.class)
                .withInheritSystemProperties(Lists.newArrayList(SYSTEM_PROPERTY_NAME))
                .withMainClassArguments(FILE_CONTENTS).isolate();
        return isolate;
    }
}
