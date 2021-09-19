/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.erdi.gradle.webdriver

import org.gradle.process.JavaForkOptions

class BinaryAwareJavaForkOptions implements DriverBinaryAware {

    private final Collection<JavaForkOptions> javaForkOptions
    private final String propertyName

    BinaryAwareJavaForkOptions(Collection<JavaForkOptions> javaForkOptions, String propertyName) {
        this.javaForkOptions = javaForkOptions
        this.propertyName = propertyName
    }

    @Override
    void setDriverBinaryPathAndVersion(String binaryPath, String version) {
        javaForkOptions*.jvmArgumentProviders*.add(
            new DriverBinaryPathCommandLineArgumentProvider(
                propertyName: propertyName,
                version: version,
                path: binaryPath
            )
        )
    }

    @Override
    void setDriverBinaryPath(String binaryPath) {
        throw new UnsupportedOperationException()
    }

}
