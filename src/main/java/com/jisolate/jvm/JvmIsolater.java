package com.jisolate.jvm;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.jisolate.Isolate;
import com.jisolate.Isolater;
import com.jisolate.util.ClassPathUtil;

public class JvmIsolater implements Isolater {

    public static class Builder {

        private final JvmIsolater template = new JvmIsolater();

        public Isolate isolate() {
            return template.isolate();
        }

        public Builder withAdditionalCommandLineArguments(
                Collection<String> additionalCommandLineArguments) {
            template.additionalCommandLineArguments = Lists
                    .newArrayList(additionalCommandLineArguments);
            return this;
        }

        public Builder withInheritClassPath(boolean inheritClassPath) {
            template.inheritClasspath = inheritClassPath;
            return this;
        }

        public Builder withInheritSystemProperties(List<String> inheritedSystemPropertyNames) {
            template.inheritedSystemPropertyNames = Lists
                    .newArrayList(inheritedSystemPropertyNames);
            return this;
        }

        public Builder withMainClass(Class<?> mainClass) {
            template.mainClassName = mainClass.getName();
            return this;
        }

        public Builder withMainClassArguments(Collection<String> mainClassArguments) {
            template.mainClassArguments = Lists.newArrayList(mainClassArguments);
            return this;
        }

        public Builder withMainClassArguments(String... mainClassArguments) {
            template.mainClassArguments = Lists.newArrayList(mainClassArguments);
            return this;
        }

        public Builder withMainClassName(String mainClassName) {
            template.mainClassName = mainClassName;
            return this;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(JvmIsolater.class);

    private List<String> additionalCommandLineArguments;
    private List<String> completeCommandLine;
    private boolean inheritClasspath = true;
    private List<String> inheritedSystemPropertyNames;
    private List<String> mainClassArguments;
    private String mainClassName;

    private JvmIsolater() {
    }

    public JvmIsolate isolate() {
        try {
            ProcessBuilder builder = new ProcessBuilder(buildCommandLine());
            final Process process = builder.start();
            handleStdOutAndStdErrOf(process);
            LOG.info("Performed JVM isolation of {}", mainClassName);
            return new JvmIsolate(process);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public <T> T isolate(Class<T> t) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<String> buildCommandLine() {
        List<String> commandLine = null;

        if (completeCommandLine != null) {
            commandLine = completeCommandLine;
        } else {
            commandLine = Lists.newArrayList();
            commandLine.add("java");
            if (additionalCommandLineArguments != null) {
                commandLine.addAll(additionalCommandLineArguments);
            }

            if (inheritClasspath) {
                commandLine.add("-cp");
                commandLine.add(getClasspath());
            }

            if (inheritedSystemPropertyNames != null && !inheritedSystemPropertyNames.isEmpty()) {
                commandLine.addAll(getInheritedSystemProperties());
            }

            Preconditions.checkNotNull(mainClassName, "The 'mainClassName' property is mandatory");
            commandLine.add(mainClassName);

            if (!mainClassArguments.isEmpty()) {
                commandLine.addAll(mainClassArguments);
            }
        }

        LOG.debug("Command line: {}", commandLine);
        return commandLine;
    }

    private String getClasspath() {
        return ClassPathUtil.getClassPath();
    }

    private Collection<String> getInheritedSystemProperties() {
        List<String> systemProperties = Lists.newArrayList();
        for (String systemPropertyName : inheritedSystemPropertyNames) {
            systemProperties.add(String.format("-D%s=%s", systemPropertyName,
                    System.getProperty(systemPropertyName)));
        }
        return systemProperties;
    }

    private void handleStdOutAndStdErrOf(Process process) {
        new StreamGobbler("stdout", process.getInputStream(), new StreamGobbler.LineHandler() {

            public void handle(String line) {
                LOG.info(line);
            }
        }).start();

        new StreamGobbler("stderr", process.getErrorStream(), new StreamGobbler.LineHandler() {

            public void handle(String line) {
                LOG.error(line);
            }
        }).start();
    }
}
