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

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class ChildFirstUrlClassLoaderTest {

    private final static String CLASS_NAME = "com.jisolate.classloader.IsolatableClass";
    private ClassLoader classLoader;
    private ChildFirstUrlClassLoader isolatedClassLoader;

    @Test
    public void canInvokeIsolatedPingMethod() throws Exception {
        Class<?> nonIsolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object result = invoke(nonIsolatedClass, "ping", new Class[]{String.class},
                new Object[]{"world"});
        assertThat("pong world").isEqualTo(result);
    }

    @Test
    public void canInvokeIsolatedStaticPingMethod() throws Exception {
        Class<?> isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object result = invoke(isolatedClass, "staticPing", new Class[]{String.class},
                new Object[]{"world"});
        assertThat("staticPong world").isEqualTo(result);
    }

    @Test
    public void canInvokeNonIsolatedPingMethod() throws Exception {
        Class<?> nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        Object result = invoke(nonIsolatedClass, "ping", new Class[]{String.class},
                new Object[]{"world"});
        assertThat("pong world").isEqualTo(result);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void isolatedClassIsNotSameAsNonIsolatedClass() throws Exception {
        Class isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Class nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        assertThat(isolatedClass).isNotEqualTo(nonIsolatedClass);
    }

    @Before
    public void setUp() {
        URL[] urls = UrlProvider.getClassPathUrls(new ArrayList<>());
        classLoader = Thread.currentThread().getContextClassLoader();
        isolatedClassLoader = new ChildFirstUrlClassLoader(urls, classLoader);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void transitiveIsolationWorks() throws Exception {
        Class isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object isolatedObject = invoke(isolatedClass, "getMessageWrapper",
                new Class[]{String.class}, new Object[]{"msg"});

        Class nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        Object nonIsolatedObject = invoke(nonIsolatedClass, "getMessageWrapper",
                new Class[]{String.class}, new Object[]{"msg"});

        assertThat(isolatedObject.getClass()).isNotSameAs(nonIsolatedObject.getClass());
        assertThat(isolatedObject.getClass().getName()).isEqualTo(nonIsolatedObject.getClass().getName());
        assertThat("" + isolatedObject).isEqualTo("" + nonIsolatedObject);
    }

    private Object invoke(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object[] args)
            throws Exception {
        Object o = clazz.getDeclaredConstructor().newInstance();
        Method m = clazz.getMethod(methodName, paramTypes);
        Object result = m.invoke(o, args);
        return result;
    }
}
