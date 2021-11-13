package com.cherryperry.gfe.base

import com.cherryperry.gfe.FileEncryptExtension
import com.cherryperry.gfe.GROUP_NAME
import com.cherryperry.gfe.fileEncryptPluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal

/**
 * Base class for encryption tasks.
 * Has encryption key provider and project extension.
 */
abstract class BaseTask : DefaultTask() {

    @get:Internal
    val fileEncryptPluginExtension: FileEncryptExtension = project.fileEncryptPluginExtension

    init {
        group = GROUP_NAME
    }
}
