/*
 * Copyright (c) 2013 Christian Gleissner.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the jisolate nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.jisolate.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class ChildFirstUrlClassLoader extends URLClassLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ChildFirstUrlClassLoader.class);
    private final Map<String, URL> loadedResources = Maps.newHashMap();

    public ChildFirstUrlClassLoader(final URL[] urls, final ClassLoader classLoader) {
        super(urls, classLoader);
        LOG.debug("Created child class loader. URLs: {}", Joiner.on("\n").join(urls));
    }

    @Override
    public synchronized URL getResource(final String name) {
        URL loadResource = loadedResources.get(name);
        if (loadResource == null) {
            loadResource = findResource(name);
            if (loadResource == null) {
                loadResource = super.getResource(name);
            } else {
                loadedResources.put(name, loadResource);
            }
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
                LOG.debug("[child classloader]  {}", name);
            } catch (ClassNotFoundException e) {
                loadedClass = getParent().loadClass(name);
                LOG.debug("[parent classloader] {}", name);
            } catch (SecurityException e) {
                throw Throwables.propagate(e);
            }
        } else {
            LOG.debug("[found loaded class] {}", name);
        }

        if (resolve) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }
}
