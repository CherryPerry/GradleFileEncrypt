package com.cherryperry.gfe.base

import com.cherryperry.gfe.FileEncryptExtension
import com.cherryperry.gfe.GROUP_NAME
import com.cherryperry.gfe.fileEncryptPluginExtension
import org.gradle.api.DefaultTask

abstract class BaseTask : DefaultTask() {

    protected val fileEncryptPluginExtension: FileEncryptExtension = project.fileEncryptPluginExtension

    init {
        group = GROUP_NAME
    }
}
