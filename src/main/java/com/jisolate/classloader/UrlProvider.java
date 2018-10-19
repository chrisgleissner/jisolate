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

package com.jisolate.classloader;

import com.jisolate.util.ClassPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class UrlProvider {
    private static final Logger log = LoggerFactory.getLogger(UrlProvider.class);

    static URL[] getClassPathUrls(final Collection<String> jarsToExcludeFromClassPath) {
        return getClassPathUrls(ClassPathUtil.getJavaHome(System.getProperties()),
                ClassPathUtil.getClassPath(),
                ClassPathUtil.getPathSeparator(System.getProperties()), jarsToExcludeFromClassPath);
    }

    private static URL[] getClassPathUrls(final String javaHomePath, final String classPath,
                                          final String pathSeparator, final Collection<String> jarsToExcludeFromClassPath) {
        final List<URL> classPathUrls = new ArrayList<>();
        for (final String classPathElement : classPath.split(pathSeparator)) {
            if (!classPathElement.startsWith(javaHomePath)) {
                boolean includeInClassPath = true;
                if (jarsToExcludeFromClassPath != null) {
                    for (final String jarToExclude : jarsToExcludeFromClassPath) {
                        final String trimmedJarToExclude = jarToExclude.trim();
                        if (trimmedJarToExclude.length() != 0
                                && classPathElement.contains(trimmedJarToExclude)) {
                            log.debug("JAR {} excluded from classpath", trimmedJarToExclude);
                            includeInClassPath = false;
                        }
                    }
                }
                if (includeInClassPath) {
                    try {
                        classPathUrls.add(new File(classPathElement).toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Could not get class path URLs", e);
                    }
                }
            }
        }
        return classPathUrls.toArray(new URL[0]);
    }
}
