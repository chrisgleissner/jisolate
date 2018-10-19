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
package com.jisolate.util;

import java.util.Properties;

public final class ClassPathUtil {

    private static final String JAVA_CLASS_PATH = "java.class.path";
    private static final String JAVA_HOME = "java.home";
    private static final String PATH_SEPARATOR = "path.separator";
    private static final String SUREFIRE_TEST_CLASS_PATH = "surefire.test.class.path";

    public static String getClassPath() {
        Properties systemProperties = System.getProperties();
        if (systemProperties.containsKey(SUREFIRE_TEST_CLASS_PATH)) {
            return systemProperties.getProperty(SUREFIRE_TEST_CLASS_PATH);
        } else {
            return systemProperties.getProperty(JAVA_CLASS_PATH);
        }
    }

    public static String getJavaHome(final Properties systemProperties) {
        return systemProperties.getProperty(JAVA_HOME);
    }

    public static String getPathSeparator(final Properties systemProperties) {
        return systemProperties.getProperty(PATH_SEPARATOR);
    }
}
