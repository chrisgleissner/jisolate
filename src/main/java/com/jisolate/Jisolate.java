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
