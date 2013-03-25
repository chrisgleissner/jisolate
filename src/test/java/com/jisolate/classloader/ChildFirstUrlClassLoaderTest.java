package com.jisolate.classloader;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ChildFirstUrlClassLoaderTest {

    private final static String CLASS_NAME = "com.jisolate.classloader.IsolatableClass";
    private ClassLoader classLoader;
    private ChildFirstUrlClassLoader isolatedClassLoader;

    @Test
    public void canInvokeIsolatedPingMethod() throws Exception {
        Class<?> nonIsolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object result = invoke(nonIsolatedClass, "ping", new Class[] { String.class },
                new Object[] { "world" });
        assertEquals("pong world", result);
    }

    @Test
    public void canInvokeIsolatedStaticPingMethod() throws Exception {
        Class<?> isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object result = invoke(isolatedClass, "staticPing", new Class[] { String.class },
                new Object[] { "world" });
        assertEquals("staticPong world", result);
    }

    @Test
    public void canInvokeNonIsolatedPingMethod() throws Exception {
        Class<?> nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        Object result = invoke(nonIsolatedClass, "ping", new Class[] { String.class },
                new Object[] { "world" });
        assertEquals("pong world", result);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void isolatedClassIsNotSameAsNonIsolatedClass() throws Exception {
        Class isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Class nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        assertThat(isolatedClass, not(nonIsolatedClass));
    }

    @Before
    public void setUp() {
        URL[] urls = UrlProvider.getClassPathUrls(Lists.<String> newArrayList());
        classLoader = Thread.currentThread().getContextClassLoader();
        isolatedClassLoader = new ChildFirstUrlClassLoader(urls, classLoader);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void transitiveIsolationWorks() throws Exception {
        Class isolatedClass = isolatedClassLoader.loadClass(CLASS_NAME);
        Object isolatedObject = invoke(isolatedClass, "getMessageWrapper",
                new Class[] { String.class }, new Object[] { "msg" });

        Class nonIsolatedClass = classLoader.loadClass(CLASS_NAME);
        Object nonIsolatedObject = invoke(nonIsolatedClass, "getMessageWrapper",
                new Class[] { String.class }, new Object[] { "msg" });

        assertTrue(isolatedObject.getClass() != nonIsolatedObject.getClass());
        assertTrue(isolatedObject.getClass().getName()
                .equals(nonIsolatedObject.getClass().getName()));
        assertEquals("" + isolatedObject, "" + nonIsolatedObject);
    }

    private Object invoke(Class<?> clazz, String methodName, Class<?>[] paramTypes, Object[] args)
            throws Exception {
        Object o = clazz.newInstance();
        Method m = clazz.getMethod(methodName, paramTypes);
        Object result = m.invoke(o, args);
        return result;
    }
}
