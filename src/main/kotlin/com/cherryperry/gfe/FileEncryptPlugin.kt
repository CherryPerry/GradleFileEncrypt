package com.cherryperry.gfe

import org.gradle.api.Plugin
import org.gradle.api.Project

open class FileEncryptPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("fileEncrypt", FileEncryptPluginExtension::class.java, project)
        project.tasks.create("encryptFiles", EncryptTask::class.java) {
            it.files.set(extension.files)
        }
        project.tasks.create("decryptFiles", DecryptTask::class.java) {
            it.files.set(extension.files)
        }
    }
}
