package com.tomgregory.gradle.pandoc

import org.gradle.api.provider.ListProperty

abstract class DocumentFormatExtension {
    abstract ListProperty<String> getArguments()
}