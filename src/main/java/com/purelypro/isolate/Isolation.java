package com.purelypro.isolate;

import com.purelypro.isolate.classloader.ClassLoaderIsolater;
import com.purelypro.isolate.jvm.JvmIsolater;

public class Isolation {

    public static ClassLoaderIsolater.Builder classLoaderIsolation() {
        return new ClassLoaderIsolater.Builder();
    }

    public static JvmIsolater.Builder jvmIsolation() {
        return new JvmIsolater.Builder();
    }
}
