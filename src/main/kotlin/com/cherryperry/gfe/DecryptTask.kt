package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.IsolationMode
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKey
import javax.inject.Inject

open class DecryptTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    private val fileEncryptPluginExtension = project.extensions.getByType(FileEncryptPluginExtension::class.java)
    private val environment: Environment = SystemEnvironment

    @get:SkipWhenEmpty
    @get:InputFiles
    val encryptedFiles: Iterable<File>
        get() = plainFiles.map { FileNameTransformer.encryptedFileFromFile(it) }

    @get:OutputFiles
    val plainFiles: Iterable<File>
        get() = project.files(fileEncryptPluginExtension.files)

    init {
        group = GROUP_NAME
        description = "Decrypts all encrypted files from configuration if they exist"
    }

    @TaskAction
    fun decrypt() {
        val password = PasswordReader.getPassword(logger, project, environment,
            fileEncryptPluginExtension.passwordProvider)
        val key = generateKey(password)
        password.fill(' ')
        plainFiles.zip(encryptedFiles).forEach { (plainFile, encryptedFile) ->
            workerExecutor.submit(DecryptTaskRunnable::class.java) { config ->
                config.isolationMode = IsolationMode.NONE
                config.params(key, encryptedFile, plainFile)
            }
        }
    }

    class DecryptTaskRunnable @Inject constructor(
        private val key: SecretKey,
        private val encryptedFile: File,
        private val plainFile: File
    ) : Runnable {

        private val logger: Logger = Logging.getLogger(DecryptTaskRunnable::class.java)

        override fun run() {
            logger.info("Decrypting file: ${encryptedFile.absolutePath}")
            if (!encryptedFile.exists() || !encryptedFile.canRead()) {
                logger.error("${encryptedFile.name} does not exist or can't be read")
                return
            }
            val fileInputStream = encryptedFile.inputStream()
            val ivSize = fileInputStream.read()
            val iv = ByteArray(ivSize)
            fileInputStream.read(iv)
            val cipher = createCipher(Cipher.DECRYPT_MODE, key, iv)
            CipherInputStream(fileInputStream, cipher).use { cipherInputStream ->
                plainFile.outputStream().use { fileOutputStream ->
                    cipherInputStream.copyTo(fileOutputStream)
                }
            }
            logger.info("Decrypted file: ${plainFile.absolutePath}")
        }
    }
}
