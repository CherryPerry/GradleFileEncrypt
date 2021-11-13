package com.cherryperry.gfe

import org.gradle.api.Project

val Project.fileEncryptPluginExtension: FileEncryptExtension
    get() = extensions.getByType(FileEncryptExtension::class.java)
