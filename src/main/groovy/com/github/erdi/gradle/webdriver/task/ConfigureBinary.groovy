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
package com.github.erdi.gradle.webdriver.task

import com.github.erdi.gradle.webdriver.DriverBinaryAware
import com.github.erdi.gradle.webdriver.DriverDistributionInstaller
import com.github.erdi.gradle.webdriver.DriverDownloadSpecification
import com.github.erdi.gradle.webdriver.WebDriverBinaryMetadata
import com.github.erdi.gradle.webdriver.repository.DriverUrlsConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant.api.core.OperatingSystem
import org.ysb33r.grolifant.api.core.OperatingSystem.Arch

class ConfigureBinary extends DefaultTask {

    private final Property<TextResource> driverUrlsConfigurationProperty = project.objects.property(TextResource)
    private final Property<File> downloadRootProperty = project.objects.property(File)
    private final Property<String> versionProperty = project.objects.property(String)
    private final Property<Arch> architectureProperty = project.objects.property(Arch)
    private final Property<Boolean> fallbackTo32BitProperty = project.objects.property(Boolean)

    private final String driverName

    protected final List<DriverBinaryAware> binaryAwares = []

    @Internal
    final WebDriverBinaryMetadata webDriverBinaryMetadata

    protected ConfigureBinary(WebDriverBinaryMetadata webDriverBinaryMetadata, String driverName) {
        this.webDriverBinaryMetadata = webDriverBinaryMetadata
        this.driverName = driverName
        onlyIf { versionConfigured }
    }

    void setDownloadRoot(Provider<File> downloadRootProvider) {
        downloadRootProperty.set(downloadRootProvider)
    }

    void setDownloadRoot(File downloadRoot) {
        downloadRoot.set(downloadRoot)
    }

    @Internal
    File getDownloadRoot() {
        downloadRootProperty.orNull
    }

    void setVersion(Provider<String> versionProvider) {
        this.versionProperty.set(versionProvider)
    }

    void setVersion(String version) {
        this.versionProperty.set(version)
    }

    @Internal
    String getVersion() {
        versionProperty.get()
    }

    void setArchitecture(Provider<Arch> architectureProvider) {
        this.architectureProperty.set(architectureProvider)
    }

    void setArchitecture(Arch architecture) {
        this.architectureProperty.set(architecture)
    }

    @Internal
    Arch getArchitecture() {
        architectureProperty.get()
    }

    void setDriverUrlsConfiguration(Provider<TextResource> driverUrlsConfiguration) {
        this.driverUrlsConfigurationProperty.set(driverUrlsConfiguration)
    }

    void setDriverUrlsConfiguration(TextResource driverUrlsConfiguration) {
        this.driverUrlsConfigurationProperty.set(driverUrlsConfiguration)
    }

    void setFallbackTo32Bit(Provider<Boolean> fallbackTo32Bit) {
        this.fallbackTo32BitProperty.set(fallbackTo32Bit)
    }

    void setFallbackTo32Bit(boolean fallbackTo32Bit) {
        this.fallbackTo32BitProperty.set(fallbackTo32Bit)
    }

    @Internal
    boolean getFallbackTo32Bit() {
        fallbackTo32BitProperty.get()
    }

    @Internal
    TextResource getDriverUrlsConfiguration() {
        driverUrlsConfigurationProperty.get()
    }

    void addBinaryAware(DriverBinaryAware aware) {
        binaryAwares << aware
    }

    @TaskAction
    void configure() {
        def versionAndUri = new DriverUrlsConfiguration(driverUrlsConfiguration.asFile()).versionAndUriFor(downloadSpec())
        def installer = new DriverDistributionInstaller(project, downloadRoot, driverName, versionAndUri)
        def distributionRoot = installer.getDistributionRoot(versionAndUri.version).get()
        def binaryFile = new File(distributionRoot, operatingSystem.getExecutableName(webDriverBinaryMetadata.binaryName))
        def binaryAbsolutePath = binaryFile.absolutePath
        binaryAwares*.setDriverBinaryPathAndVersion(binaryAbsolutePath, versionAndUri.version)
    }

    @Internal
    protected OperatingSystem getOperatingSystem() {
        OperatingSystem.current()
    }

    @Internal
    protected boolean isVersionConfigured() {
        versionProperty.present
    }

    private DriverDownloadSpecification downloadSpec() {
        DriverDownloadSpecification.builder()
            .name(driverName)
            .version(version)
            .arch(architecture)
            .os(operatingSystem)
            .fallbackTo32Bit(fallbackTo32Bit)
            .build()
    }

}
