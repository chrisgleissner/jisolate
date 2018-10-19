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

import java.io.Closeable;

public class JvmIsolate implements Closeable {

    private final Process process;

    JvmIsolate(Process process) {
        this.process = process;
    }

    public void close() {
        process.destroy();
    }

    public int waitFor() {
        try {
            return process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Waiting failed for process " + process, e);
        }
    }
}
