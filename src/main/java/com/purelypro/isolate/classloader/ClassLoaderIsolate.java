package com.purelypro.isolate.classloader;

import java.io.IOException;

import com.purelypro.isolate.Isolate;

public class ClassLoaderIsolate implements Isolate {

    private final Thread isolatedThread;

    public ClassLoaderIsolate(Thread isolatedThread) {
        this.isolatedThread = isolatedThread;
    }

    public void close() throws IOException {
    }
}
