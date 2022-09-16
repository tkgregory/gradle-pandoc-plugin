package com.tomgregory.gradle.pandoc

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty

abstract class PandocPluginExtension {
    abstract DirectoryProperty getPandocDirectory()
    abstract RegularFileProperty getCustomDockerfile()
}
