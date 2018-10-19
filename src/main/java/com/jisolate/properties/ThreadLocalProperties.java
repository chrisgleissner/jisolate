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

package com.jisolate.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ThreadLocalProperties extends Properties {
    private static final Logger log = LoggerFactory.getLogger(ThreadLocalProperties.class);

    public static synchronized void activate() {
        Properties props = System.getProperties();
        if (!(props instanceof ThreadLocalProperties)) {
            System.setProperties(new ThreadLocalProperties(props));
            log.info("Activated thread-local system properties");
        }
    }

    private final ThreadLocal<Properties> localProperties = ThreadLocal.withInitial(Properties::new);

    private ThreadLocalProperties(Properties properties) {
        super(properties);
    }

    @Override
    public String getProperty(String key) {
        String localValue = localProperties.get().getProperty(key);
        return localValue == null ? super.getProperty(key) : localValue;
    }

    @Override
    public Object setProperty(String key, String value) {
        return localProperties.get().setProperty(key, value);
    }
}