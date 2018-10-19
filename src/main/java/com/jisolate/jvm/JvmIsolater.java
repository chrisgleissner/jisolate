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

package com.jisolate.jvm;

import com.jisolate.util.ClassPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Isolater which relies on spawning child JVMs to ensure isolation.
 */
public class JvmIsolater {

    public static class Builder {

        private final JvmIsolater template = new JvmIsolater();

        public JvmIsolate isolate() {
            return template.isolate();
        }

        public Builder withAdditionalCommandLineArguments(
                Collection<String> additionalCommandLineArguments) {
            template.additionalCommandLineArguments = additionalCommandLineArguments;
            return this;
        }

        public Builder withInheritClassPath(boolean inheritClassPath) {
            template.inheritClasspath = inheritClassPath;
            return this;
        }

        public Builder withInheritSystemProperties(List<String> inheritedSystemPropertyNames) {
            template.inheritedSystemPropertyNames = inheritedSystemPropertyNames;
            return this;
        }

        public Builder withMainClass(Class<?> mainClass) {
            template.mainClassName = mainClass.getName();
            return this;
        }

        public Builder withMainClassArguments(Collection<String> mainClassArguments) {
            template.mainClassArguments = mainClassArguments;
            return this;
        }

        public Builder withMainClassArguments(String... mainClassArguments) {
            template.mainClassArguments = Arrays.asList(mainClassArguments);
            return this;
        }

        public Builder withMainClassName(String mainClassName) {
            template.mainClassName = mainClassName;
            return this;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JvmIsolater.class);

    private Collection<String> additionalCommandLineArguments;
    private boolean inheritClasspath = true;
    private Collection<String> inheritedSystemPropertyNames;
    private Collection<String> mainClassArguments;
    private String mainClassName;

    private JvmIsolater() {
    }

    public JvmIsolate isolate() {
        try {
            ProcessBuilder builder = new ProcessBuilder(buildCommandLine());
            final Process process = builder.start();
            handleStdOutAndStdErrOf(process);
            log.info("Performed JVM isolation of {}", mainClassName);
            return new JvmIsolate(process);
        } catch (Exception e) {
            throw new RuntimeException("Isolation of JVM failed", e);
        }
    }

    public <T> T isolate(Class<T> t) {
        throw new UnsupportedOperationException();
    }

    private List<String> buildCommandLine() {
        List<String> commandLine = new ArrayList<>();
        commandLine.add("java");

        if (additionalCommandLineArguments != null)
            commandLine.addAll(additionalCommandLineArguments);

        if (inheritClasspath) {
            commandLine.add("-cp");
            commandLine.add(getClasspath());
        }

        if (inheritedSystemPropertyNames != null && !inheritedSystemPropertyNames.isEmpty())
            commandLine.addAll(getInheritedSystemProperties());

        requireNonNull(mainClassName, "The 'mainClassName' property is mandatory");
        commandLine.add(mainClassName);

        if (!mainClassArguments.isEmpty())
            commandLine.addAll(mainClassArguments);

        log.debug("Command line: {}", commandLine);
        return commandLine;
    }

    private String getClasspath() {
        return ClassPathUtil.getClassPath();
    }

    private Collection<String> getInheritedSystemProperties() {
        return inheritedSystemPropertyNames.stream().map(n -> String.format("-D%s=%s", n, System.getProperty(n))).collect(toList());
    }

    private void handleStdOutAndStdErrOf(Process process) {
        new StreamGobbler("stdout", process.getInputStream(), line -> log.info(line)).start();
        new StreamGobbler("stderr", process.getErrorStream(), line -> log.error(line)).start();
    }
}
