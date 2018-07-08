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
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject

open class EncryptTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    private val fileEncryptPluginExtension = project.extensions.getByType(FileEncryptPluginExtension::class.java)
    private val environment: Environment = SystemEnvironment

    @get:SkipWhenEmpty
    @get:InputFiles
    val plainFiles: Iterable<File>
        get() = project.files(fileEncryptPluginExtension.files)

    @get:OutputFiles
    val encryptedFiles: Iterable<File>
        get() = plainFiles.map { FileNameTransformer.encryptedFileFromFile(it) }

    init {
        group = GROUP_NAME
        description = "Encrypts all unencrypted files from configuration if they exist"
    }

    @TaskAction
    fun encrypt() {
        val password = PasswordReader.getPassword(logger, project, environment,
            fileEncryptPluginExtension.passwordProvider)
        val key = generateKey(password)
        password.fill(' ')
        plainFiles.zip(encryptedFiles).forEach { (plainFile, encryptedFile) ->
            workerExecutor.submit(EncryptTaskRunnable::class.java) { config ->
                config.isolationMode = IsolationMode.NONE
                config.params(key, plainFile, encryptedFile)
            }
        }
    }

    class EncryptTaskRunnable @Inject constructor(
        private val key: SecretKey,
        private val plainFile: File,
        private val encryptedFile: File
    ) : Runnable {

        private val logger: Logger = Logging.getLogger(EncryptTaskRunnable::class.java)

        override fun run() {
            logger.info("Encrypting file: ${plainFile.absolutePath}")
            if (!plainFile.exists() || !plainFile.canRead()) {
                logger.error("${plainFile.name} does not exist or can't be read")
                return
            }
            val iv = if (encryptedFile.exists() && encryptedFile.canRead()) {
                // if encrypted file already exists - reuse it's IV
                logger.info("Encrypted file already exists, reuse it's IV")
                encryptedFile.inputStream().use { fileInputStream ->
                    val ivSize = fileInputStream.read()
                    val iv = ByteArray(ivSize)
                    fileInputStream.read(iv)
                    iv
                }
            } else {
                generateIv()
            }
            val cipher = createCipher(Cipher.ENCRYPT_MODE, key, iv)
            plainFile.inputStream().use { fileInputStream ->
                encryptedFile.outputStream().use { fileOutputStream ->
                    fileOutputStream.write(cipher.iv.size)
                    fileOutputStream.write(cipher.iv)
                    CipherOutputStream(fileOutputStream, cipher).use { cipherOutputStream ->
                        fileInputStream.copyTo(cipherOutputStream)
                    }
                }
            }
            logger.info("Encrypted file: ${encryptedFile.absolutePath}")
        }
    }
}
