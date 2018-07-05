package com.cherryperry.gfe

import org.gradle.api.Plugin
import org.gradle.api.Project

open class FileEncryptPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "gradleFileEncrypt"
        const val TASK_ENCRYPT_NAME = "encryptFiles"
        const val TASK_DECRYPT_NAME = "decryptFiles"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, FileEncryptPluginExtension::class.java)
        project.tasks.create(TASK_ENCRYPT_NAME, EncryptTask::class.java)
        project.tasks.create(TASK_DECRYPT_NAME, DecryptTask::class.java)
    }
}
