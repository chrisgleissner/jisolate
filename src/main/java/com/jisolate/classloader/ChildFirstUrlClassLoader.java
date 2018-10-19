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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class ChildFirstUrlClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(ChildFirstUrlClassLoader.class);
    private final Map<String, URL> loadedResources = new HashMap<>();

    ChildFirstUrlClassLoader(final URL[] urls, final ClassLoader classLoader) {
        super(urls, classLoader);
        if (log.isDebugEnabled())
            log.debug("Created child class loader. URLs: {}", stream(urls).map(URL::toString).collect(joining("\n")));
    }

    @Override
    public synchronized URL getResource(final String name) {
        URL loadResource = loadedResources.get(name);
        if (loadResource == null) {
            loadResource = findResource(name);
            if (loadResource == null)
                loadResource = super.getResource(name);
            else
                loadedResources.put(name, loadResource);
        }
        return loadResource;
    }

    @Override
    protected synchronized Class<?> loadClass(final String name, final boolean resolve)
            throws ClassNotFoundException {
        Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass == null) {
            try {
                loadedClass = findClass(name);
                log.debug("[child classloader]  {}", name);
            } catch (ClassNotFoundException e) {
                loadedClass = getParent().loadClass(name);
                log.debug("[parent classloader] {}", name);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.debug("[found loaded class] {}", name);
        }

        if (resolve) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }
}
