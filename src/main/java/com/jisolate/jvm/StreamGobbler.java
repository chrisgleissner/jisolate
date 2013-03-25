package com.jisolate.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;

public class StreamGobbler extends Thread {

    public static interface LineHandler {
        void handle(String line);
    }

    private static AtomicInteger counter = new AtomicInteger();

    private final InputStream is;
    private final LineHandler lineHandler;

    public StreamGobbler(String name, InputStream is, LineHandler lineHandler) {
        this.is = is;
        this.lineHandler = lineHandler;
        super.setDaemon(true);
        super.setName(String.format("StreamGobbler-%d-%s", counter.incrementAndGet(), name));
    }

    @Override
    public void run() {
        BufferedReader br = null;
        try {
            InputStreamReader isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                lineHandler.handle(line);
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            Closeables.closeQuietly(br);
        }
    }
}
