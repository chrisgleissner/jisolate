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
package com.jisolate;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.charset.Charset.defaultCharset;


public class IsolatedClass {

    static AtomicInteger numberOfInvocations = new AtomicInteger();
    private static final Logger log = LoggerFactory.getLogger(IsolatedClass.class);

    public static void main(String[] args) throws IOException {
        String fileContents = args[0] + " " + System.getProperty(JisolateTest.SYSTEM_PROPERTY_NAME);
        FileUtils.write(JisolateTest.FILE, fileContents, defaultCharset());

        System.setProperty(JisolateTest.SYSTEM_PROPERTY_NAME, "moon");
        log.info(fileContents);

        numberOfInvocations.incrementAndGet();
    }
}
