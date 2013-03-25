package com.jisolate.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.jisolate.util.ClassPathUtil;

public class UrlProvider {

    private static final Logger LOG = LoggerFactory.getLogger(UrlProvider.class);

    public static URL[] getClassPathUrls(final Collection<String> jarsToExcludeFromClassPath) {
        return getClassPathUrls(ClassPathUtil.getJavaHome(System.getProperties()),
                ClassPathUtil.getClassPath(),
                ClassPathUtil.getPathSeparator(System.getProperties()), jarsToExcludeFromClassPath);
    }

    public static URL[] getClassPathUrls(final String javaHomePath, final String classPath,
            final String pathSeparator, final Collection<String> jarsToExcludeFromClassPath) {
        final List<URL> classPathUrls = Lists.newArrayList();
        final String[] classPathArray = classPath.split(pathSeparator);
        for (final String partOfClassPath : classPathArray) {
            if (!partOfClassPath.startsWith(javaHomePath)) {
                boolean includeInClassPath = true;
                if (jarsToExcludeFromClassPath != null) {
                    for (final String jarToExclude : jarsToExcludeFromClassPath) {
                        final String trimmedJarToExclude = jarToExclude.trim();
                        if (trimmedJarToExclude.length() != 0
                                && partOfClassPath.contains(trimmedJarToExclude)) {
                            LOG.debug("JAR {} excluded from classpath", trimmedJarToExclude);
                            includeInClassPath = false;
                        }
                    }
                }

                if (includeInClassPath) {
                    try {
                        classPathUrls.add(new File(partOfClassPath).toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw Throwables.propagate(e);
                    }
                }
            }
        }
        return classPathUrls.toArray(new URL[classPathUrls.size()]);
    }

    public URL[] getUrls(Collection<String> jarsToExcludeFromClasspath) {
        final Properties systemProperties = System.getProperties();
        final String pathSeparator = ClassPathUtil.getPathSeparator(systemProperties);
        final String classPath = ClassPathUtil.getClassPath();
        final String javaHome = ClassPathUtil.getJavaHome(systemProperties);
        final URL[] classPathUrls = getClassPathUrls(javaHome, classPath, pathSeparator,
                jarsToExcludeFromClasspath);
        return classPathUrls;

    }

}
