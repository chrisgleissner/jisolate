package com.jisolate.classloader;

import java.io.IOException;

import com.jisolate.Isolate;

public class ClassLoaderIsolate implements Isolate {

    private final Thread isolatedThread;

    public ClassLoaderIsolate(Thread isolatedThread) {
        this.isolatedThread = isolatedThread;
    }

    public void close() throws IOException {
    }
}
