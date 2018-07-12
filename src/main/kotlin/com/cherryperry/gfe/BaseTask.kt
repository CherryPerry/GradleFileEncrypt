package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import java.io.File
import javax.crypto.SecretKey

/**
 * Base class for encryption tasks.
 * Has encryption key provider and project extension.
 */
abstract class BaseTask : DefaultTask() {

    private val fileEncryptPluginExtension: FileEncryptPluginExtension =
        project.extensions.getByType(FileEncryptPluginExtension::class.java)
    private val environment: Environment = SystemEnvironment

    @get:Input
    val key: SecretKey
        get() {
            val password = PasswordReader.getPassword(logger, project, environment,
                fileEncryptPluginExtension.passwordProvider)
            val key = generateKey(password)
            password.fill(' ')
            return key
        }

    protected open val plainFiles: Iterable<File>
        get() {
            return project.files(fileEncryptPluginExtension.files)
        }

    protected open val encryptedFiles: Iterable<File>
        get() {
            return fileEncryptPluginExtension.files
                .map { original ->
                    fileEncryptPluginExtension.mapping[original]?.let { mapped -> return@map mapped }
                    original
                }
                .map { project.file(it) }
                .map { FileNameTransformer.encryptedFileFromFile(it) }
        }

    init {
        group = GROUP_NAME
    }
}
