package com.purelypro.isolate.jvm;

import java.io.IOException;

import com.google.common.base.Throwables;
import com.purelypro.isolate.Isolate;

public class JvmIsolate implements Isolate {

    private final Process process;

    JvmIsolate(Process process) {
        this.process = process;
    }

    public void close() throws IOException {
        process.destroy();
    }

    public int waitFor() {
        try {
            return process.waitFor();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
