package com.purelypro.isolate.classloader;

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
