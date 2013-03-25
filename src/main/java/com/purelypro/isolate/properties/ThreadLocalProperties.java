package com.purelypro.isolate.properties;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadLocalProperties extends Properties {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadLocalProperties.class);
    private static final long serialVersionUID = 1L;

    public static synchronized void activate() {
        Properties props = System.getProperties();
        if (!(props instanceof ThreadLocalProperties)) {
            System.setProperties(new ThreadLocalProperties(props));
            LOG.info("Activated thread-local system properties");
        }
    }

    private final ThreadLocal<Properties> localProperties = new ThreadLocal<Properties>() {
        @Override
        protected Properties initialValue() {
            return new Properties();
        }
    };

    public ThreadLocalProperties(Properties properties) {
        super(properties);
    }

    @Override
    public String getProperty(String key) {
        String localValue = localProperties.get().getProperty(key);
        return localValue == null ? super.getProperty(key) : localValue;
    }

    @Override
    public Object setProperty(String key, String value) {
        return localProperties.get().setProperty(key, value);
    }
}