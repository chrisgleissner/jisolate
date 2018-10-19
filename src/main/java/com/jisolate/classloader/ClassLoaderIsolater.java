/*
 * Copyright (c) 2013-2018 Christian Gleissner.
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

import com.jisolate.properties.ThreadLocalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * Isolater that uses a child-first classloader along with thread-local system properties to ensure isolation.
 */
public class ClassLoaderIsolater {

    public static class Builder {
        private final ClassLoaderIsolater template = new ClassLoaderIsolater();

        public void isolate() {
            template.isolate();
        }

        public Builder withIsolatableArguments(Object... isolatableArguments) {
            template.isolatableArguments = asList(isolatableArguments);
            return this;
        }

        public Builder withIsolatableClass(Class<?> isolatableClass) {
            template.mainClassName = isolatableClass.getName();
            return this;
        }

        public Builder withJarsToExcludeFromClassPath(Collection<String> jarsToExcludeFromClassPath) {
            template.jarsToExcludeFromClassPath = jarsToExcludeFromClassPath;
            return this;
        }

        public Builder withMainClassArguments(Collection<Object> isolatableArguments) {
            template.isolatableArguments = isolatableArguments;
            return this;
        }

        public Builder withMainClassName(String mainClassName) {
            template.mainClassName = mainClassName;
            return this;
        }
    }

    private static class IsolatedRunnable implements Runnable {
        private final URLClassLoader contextClassLoader;
        private final Collection<Object> isolatableArguments;
        private final String isolatableClassName;
        private volatile Object result;

        IsolatedRunnable(URLClassLoader contextClassLoader, String isolatableClassName,
                         Collection<Object> isolatableArguments) {
            this.contextClassLoader = contextClassLoader;
            this.isolatableArguments = isolatableArguments;
            this.isolatableClassName = isolatableClassName;
            ThreadLocalProperties.activate();
        }

        public Object getResult() {
            return result;
        }

        public void run() {
            try {
                Class<?> clazz = loadClass();
                Optional<Method> method = getMainMethod(clazz);
                if (method.isPresent()) {
                    invokeMainMethod(method.get());
                } else {
                    method = getCallMethod(clazz);
                    if (method.isPresent()) {
                        result = invokeCallMethod(method.get());
                    } else {
                        throw new RuntimeException(
                                format(
                                        "The class '%s' contained neither a static main(String[]) "
                                                + "nor a call(Object...) method and could therefore not be invoked",
                                        isolatableClassName));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(format("Failed to execute %s#main() in isolation", isolatableClassName), e);
            }
        }

        private Optional<Method> getCallMethod(Class<?> clazz) {
            try {
                return Optional.of(clazz.getMethod("call", Object[].class));
            } catch (Exception e) {
                log.debug("Clazz contained no call() method: {}", clazz, e);
                return Optional.empty();
            }
        }

        private Optional<Method> getMainMethod(Class<?> clazz) {
            try {
                return Optional.of(clazz.getMethod("main", String[].class));
            } catch (Exception e) {
                log.debug("Clazz contained no main() method: {}", clazz, e);
                return Optional.empty();
            }
        }

        private Object invokeCallMethod(Method method) {
            try {
                long startTime = System.currentTimeMillis();
                Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                Object[] arrayArgs = null;
                if (isolatableArguments != null) {
                    arrayArgs = isolatableArguments.toArray(new Object[0]);
                }
                Object result = method.invoke(instance, arrayArgs);
                log.info("Invoked {}#call in {}ms", isolatableClassName, System.currentTimeMillis()
                        - startTime);
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Invocation of main method failed: " + method, e);
            }
        }

        private void invokeMainMethod(Method method) {
            try {
                long startTime = System.currentTimeMillis();
                String[] arrayArgs = null;
                if (isolatableArguments != null) {
                    arrayArgs = isolatableArguments.toArray(new String[0]);
                }
                method.invoke(null, new Object[]{arrayArgs});
                log.info("Invoked {}#main in {}ms", isolatableClassName, System.currentTimeMillis()
                        - startTime);
            } catch (Exception e) {
                throw new RuntimeException("Invocation of main method failed: " + method, e);
            }
        }

        private Class<?> loadClass() throws ClassNotFoundException {
            long startTime = System.currentTimeMillis();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            Class<?> clazz = Class.forName(isolatableClassName, true, contextClassLoader);
            log.info("Loaded isolated class {} in {}ms", isolatableClassName,
                    System.currentTimeMillis() - startTime);
            return clazz;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(ClassLoaderIsolater.class);
    public Collection<Object> isolatableArguments;
    private final AtomicInteger isolationThreadCount = new AtomicInteger();
    private Collection<String> jarsToExcludeFromClassPath;
    private String mainClassName;

    private ClassLoaderIsolater() {
    }

    public void isolate() {
        URL[] urls = UrlProvider.getClassPathUrls(jarsToExcludeFromClassPath);
        URLClassLoader contextClassLoader = new ChildFirstUrlClassLoader(urls, Thread
                .currentThread().getContextClassLoader());
        IsolatedRunnable isolatedRunnable = new IsolatedRunnable(contextClassLoader, mainClassName,
                isolatableArguments);

        Thread isolatedThread = new Thread(isolatedRunnable);
        isolatedThread.setDaemon(true);
        isolatedThread.setName("Isolater-" + isolationThreadCount.incrementAndGet());
        isolatedThread.start();
    }
}
