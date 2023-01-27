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

import com.github.erdi.gradle.webdriver.task.ConfigureBinary
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.process.JavaForkOptions

@SuppressWarnings(['AbstractClassWithPublicConstructor'])
abstract class WebDriverBinariesPluginExtension {

    public static final String DRIVER_URLS_CONFIG_URL =
        'https://raw.githubusercontent.com/webdriverextensions/webdriverextensions-maven-plugin-repository/master/repository-3.0.json'

    private final Project project
    private final ObjectFactory objectFactory

    final DriverConfiguration ieDriverServerConfiguration
    final DriverConfiguration chromedriverConfiguration
    final DriverConfiguration geckodriverConfiguration
    final DriverConfiguration edgedriverConfiguration

    WebDriverBinariesPluginExtension(Project project) {
        this.project = project

        this.objectFactory = project.objects
        this.ieDriverServerConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)
        this.chromedriverConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)
        this.geckodriverConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)
        this.edgedriverConfiguration = new DriverConfiguration(project, this.fallbackTo32Bit)

        this.downloadRoot.convention(
            project.layout.dir(
                project.providers.provider { project.gradle.gradleUserHomeDir }
            )
        )
        this.driverUrlsConfiguration.convention(project.resources.text.fromUri(DRIVER_URLS_CONFIG_URL))
        this.fallbackTo32Bit.convention(false)
    }

    abstract DirectoryProperty getDownloadRoot()
    abstract Property<TextResource> getDriverUrlsConfiguration()
    abstract Property<Boolean> getFallbackTo32Bit()

    void iedriverserver(String configuredVersion) {
        iedriverserver {
            version = configuredVersion
        }
    }

    void setIedriverserver(String configuredVersion) {
        iedriverserver(configuredVersion)
    }

    void iedriverserver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(ieDriverServerConfiguration, configuration)
    }

    void chromedriver(String configuredVersion) {
        chromedriver {
            version = configuredVersion
        }
    }

    void setChromedriver(String configuredVersion) {
        chromedriver(configuredVersion)
    }

    void chromedriver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(chromedriverConfiguration, configuration)
    }

    void geckodriver(String configuredVersion) {
        geckodriver {
            version = configuredVersion
        }
    }

    void setGeckodriver(String configuredVersion) {
        geckodriver(configuredVersion)
    }

    void geckodriver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(geckodriverConfiguration, configuration)
    }

    void edgedriver(String configuredVersion) {
        edgedriver {
            version = configuredVersion
        }
    }

    void setEdgedriver(String configuredVersion) {
        edgedriver(configuredVersion)
    }

    void edgedriver(@DelegatesTo(DriverConfiguration) Closure configuration) {
        project.configure(edgedriverConfiguration, configuration)
    }

    public <T extends Task & JavaForkOptions> void configureTask(T task) {
        def tasks = objectFactory.domainObjectSet(Task) as DomainObjectCollection<T>
        tasks.add(task)
        configureTasks(tasks)
    }

    public <T extends Task & JavaForkOptions> void configureTasks(DomainObjectCollection<T> tasks) {
        configure(tasks)
    }

    private <T extends Task & JavaForkOptions> void configure(DomainObjectCollection<T> tasks) {
        def configureBinaryTasks = project.tasks.withType(ConfigureBinary)
        tasks.configureEach { T javaForkOptions ->
            javaForkOptions.dependsOn(configureBinaryTasks)
        }
        configureBinaryTasks.configureEach { ConfigureBinary configureBinary ->
            configureBinary.addBinaryAware(new BinaryAwareJavaForkOptions(tasks, configureBinary.webDriverBinaryMetadata.systemProperty))
        }
    }

}
