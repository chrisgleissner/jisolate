package com.purelypro.isolate.classloader;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.purelypro.isolate.Isolater;
import com.purelypro.isolate.properties.ThreadLocalProperties;

public class ClassLoaderIsolater implements Isolater {

    public static class Builder {
        private final ClassLoaderIsolater template = new ClassLoaderIsolater();

        public ClassLoaderIsolate isolate() {
            return template.isolate();
        }

        public Builder withIsolatableArguments(Object... isolatableArguments) {
            template.isolatableArguments = Lists.newArrayList(isolatableArguments);
            return this;
        }

        public Builder withIsolatableClass(Class<?> isolatableClass) {
            template.mainClassName = isolatableClass.getName();
            return this;
        }

        public Builder withJarsToExcludeFromClassPath(Collection<String> jarsToExcludeFromClassPath) {
            template.jarsToExcludeFromClassPath = Lists.newArrayList(jarsToExcludeFromClassPath);
            return this;
        }

        public Builder withMainClassArguments(Collection<Object> isolatableArguments) {
            template.isolatableArguments = Lists.newArrayList(isolatableArguments);
            return this;
        }

        public Builder withMainClassName(String mainClassName) {
            template.mainClassName = mainClassName;
            return this;
        }
    }

    private static class IsolatedRunnable implements Runnable {

        private final URLClassLoader contextClassLoader;
        private final List<Object> isolatableArguments;
        private final String isolatableClassName;
        private volatile Object result;

        IsolatedRunnable(URLClassLoader contextClassLoader, String isolatableClassName,
                List<Object> isolatableArguments) {
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
                    if (method != null) {
                        result = invokeCallMethod(method.get());
                    } else {
                        throw new RuntimeException(
                                String.format(
                                        "The class '%s' contained neither a static main(String[]) "
                                                + "nor a call(Object...) method and could therefore not be invoked",
                                        isolatableClassName));
                    }
                }
            } catch (Exception e) {
                LOG.warn("Failed to execute {}#main() in isolation", isolatableClassName, e);
                throw Throwables.propagate(e);
            }
        }

        private Optional<Method> getCallMethod(Class<?> clazz) {
            try {
                return Optional.of(clazz.getMethod("call", Object[].class));
            } catch (Exception e) {
                LOG.debug("Clazz contained no call() method: {}", clazz, e);
                return Optional.absent();
            }
        }

        private Optional<Method> getMainMethod(Class<?> clazz) {
            try {
                return Optional.of(clazz.getMethod("main", String[].class));
            } catch (Exception e) {
                LOG.debug("Clazz contained no main() method: {}", clazz, e);
                return Optional.absent();
            }
        }

        private Object invokeCallMethod(Method method) {
            try {
                long startTime = System.currentTimeMillis();
                Object instance = method.getDeclaringClass().newInstance();
                Object[] arrayArgs = null;
                if (isolatableArguments != null) {
                    arrayArgs = isolatableArguments.toArray(new Object[isolatableArguments.size()]);
                }
                Object result = method.invoke(instance, arrayArgs);
                LOG.info("Invoked {}#call in {}ms", isolatableClassName, System.currentTimeMillis()
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
                    arrayArgs = isolatableArguments.toArray(new String[isolatableArguments.size()]);
                }
                method.invoke(null, new Object[] { arrayArgs });
                LOG.info("Invoked {}#main in {}ms", isolatableClassName, System.currentTimeMillis()
                        - startTime);
            } catch (Exception e) {
                throw new RuntimeException("Invocation of main method failed: " + method, e);
            }
        }

        private Class<?> loadClass() throws ClassNotFoundException {
            long startTime = System.currentTimeMillis();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            Class<?> clazz = Class.forName(isolatableClassName, true, contextClassLoader);
            LOG.info("Loaded isolated class {} in {}ms", isolatableClassName,
                    System.currentTimeMillis() - startTime);
            return clazz;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ClassLoaderIsolater.class);
    public ArrayList<Object> isolatableArguments;
    private final AtomicInteger isolationThreadCount = new AtomicInteger();
    private Collection<String> jarsToExcludeFromClassPath;
    private String mainClassName;

    private ClassLoaderIsolater() {
    }

    public ClassLoaderIsolate isolate() {
        URL[] urls = UrlProvider.getClassPathUrls(jarsToExcludeFromClassPath);
        URLClassLoader contextClassLoader = new ChildFirstUrlClassLoader(urls, Thread
                .currentThread().getContextClassLoader());
        IsolatedRunnable isolatedRunnable = new IsolatedRunnable(contextClassLoader, mainClassName,
                isolatableArguments);

        Thread isolatedThread = new Thread(isolatedRunnable);
        isolatedThread.setDaemon(true);
        isolatedThread.setName("Isolater-" + isolationThreadCount.incrementAndGet());
        isolatedThread.start();

        return new ClassLoaderIsolate(isolatedThread);
    }

    public <T> T isolate(Class<T> t) {
        // TODO Auto-generated method stub
        return null;
    }
}
