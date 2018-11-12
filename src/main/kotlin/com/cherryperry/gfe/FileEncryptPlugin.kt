package com.cherryperry.gfe

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

open class FileEncryptPlugin : Plugin<Project> {

    companion object {
        const val EXTENSION_NAME = "gradleFileEncrypt"
        const val TASK_ENCRYPT_NAME = "encryptFiles"
        const val TASK_DECRYPT_NAME = "decryptFiles"
        const val TASK_GIT_IGNORE_NAME = "checkFilesGitIgnored"
    }

    override fun apply(project: Project) {
        project.extensions.create(EXTENSION_NAME, FileEncryptPluginExtension::class.java)
        if (GradleVersion.current() >= GradleVersion.version("4.9")) {
            project.tasks.register(TASK_ENCRYPT_NAME, EncryptTask::class.java)
            project.tasks.register(TASK_DECRYPT_NAME, DecryptTask::class.java)
            project.tasks.register(TASK_GIT_IGNORE_NAME, CheckGitIgnoreTask::class.java)
        } else {
            project.tasks.create(TASK_ENCRYPT_NAME, EncryptTask::class.java)
            project.tasks.create(TASK_DECRYPT_NAME, DecryptTask::class.java)
            project.tasks.create(TASK_GIT_IGNORE_NAME, CheckGitIgnoreTask::class.java)
        }
    }
}
