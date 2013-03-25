package com.jisolate.classloader;

import com.jisolate.AbstractIsolaterTest;
import com.jisolate.Isolate;
import com.jisolate.IsolatedClass;
import com.jisolate.Jisolate;

public class ClassLoaderIsolaterTest extends AbstractIsolaterTest {

    @Override
    protected void assertIsolationWorks(Isolate isolate) {
    }

    @Override
    protected Isolate createIsolate() {
        Isolate isolate = Jisolate.classLoaderIsolation().withIsolatableClass(IsolatedClass.class)
                .withIsolatableArguments(FILE_CONTENTS).isolate();
        return isolate;
    }
}
