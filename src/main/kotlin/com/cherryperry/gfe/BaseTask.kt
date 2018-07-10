package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import javax.crypto.SecretKey

/**
 * Base class for encryption tasks.
 * Has encryption key provider and project extension.
 */
abstract class BaseTask : DefaultTask() {

    protected val fileEncryptPluginExtension: FileEncryptPluginExtension =
        project.extensions.getByType(FileEncryptPluginExtension::class.java)
    protected val environment: Environment = SystemEnvironment

    @get:Input
    val key: SecretKey
        get() {
            val password = PasswordReader.getPassword(logger, project, environment,
                fileEncryptPluginExtension.passwordProvider)
            val key = generateKey(password)
            password.fill(' ')
            return key
        }

    init {
        group = GROUP_NAME
    }
}
