/*
 * Copyright (C) 2013-2018 Christian Gleissner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jisolate.jvm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamGobbler extends Thread {

    public interface LineHandler {
        void handle(String line);
    }

    private static AtomicInteger counter = new AtomicInteger();

    private final InputStream is;
    private final LineHandler lineHandler;

    StreamGobbler(String name, InputStream is, LineHandler lineHandler) {
        this.is = is;
        this.lineHandler = lineHandler;
        super.setDaemon(true);
        super.setName(String.format("StreamGobbler-%d-%s", counter.incrementAndGet(), name));
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineHandler.handle(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read from input stream", e);
        }
    }
}
