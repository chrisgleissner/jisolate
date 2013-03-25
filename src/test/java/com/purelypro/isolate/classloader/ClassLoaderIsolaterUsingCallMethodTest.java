package com.purelypro.isolate.classloader;

import com.purelypro.isolate.AbstractIsolaterTest;
import com.purelypro.isolate.Isolate;
import com.purelypro.isolate.IsolatedClass;
import com.purelypro.isolate.Isolation;

public class ClassLoaderIsolaterUsingCallMethodTest extends AbstractIsolaterTest {

    @Override
    protected void assertIsolationWorks(Isolate isolate) {
    }

    @Override
    protected Isolate createIsolate() {
        Isolate isolate = Isolation.classLoaderIsolation().withIsolatableClass(IsolatedClass.class)
                .withIsolatableArguments(FILE_CONTENTS).isolate();
        return isolate;
    }
}
