package com.cherryperry.gfe

import org.gradle.api.Plugin
import org.gradle.api.Project

class FileEncryptPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("fileEncrypt", FileEncryptPluginExtension::class.java)
        project.tasks.create("encryptFiles", EncryptTask::class.java) {
            it.files = extension.files
        }
        project.tasks.create("decryptFiles", DecryptTask::class.java) {
            it.files = extension.files
        }
        // TODO Auto-decrypt
    }
}