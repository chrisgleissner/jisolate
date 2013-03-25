package com.jisolate;

import com.jisolate.classloader.ClassLoaderIsolater;
import com.jisolate.jvm.JvmIsolater;

/**
 * Main entrance point to the Jisolate API.
 */
public class Jisolate {

    public static ClassLoaderIsolater.Builder classLoaderIsolation() {
        return new ClassLoaderIsolater.Builder();
    }

    public static JvmIsolater.Builder jvmIsolation() {
        return new JvmIsolater.Builder();
    }
}
