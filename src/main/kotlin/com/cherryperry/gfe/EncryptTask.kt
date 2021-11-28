package com.cherryperry.gfe

import com.cherryperry.gfe.base.BaseTask
import com.cherryperry.gfe.base.EncryptedFilesAware
import com.cherryperry.gfe.base.PlainFilesAware
import com.cherryperry.gfe.base.SecretKeyAware
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileType
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.work.ChangeType
import org.gradle.work.InputChanges
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.inject.Inject

open class EncryptTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : BaseTask(), SecretKeyAware, PlainFilesAware, EncryptedFilesAware {

    @get:Input
    override val key: Provider<SecretKey> = fileEncryptPluginExtension.secretKey(project)

    @get:[InputFiles SkipWhenEmpty]
    override val plainFiles: FileCollection = fileEncryptPluginExtension.plainFiles

    @get:OutputFiles
    override val encryptedFiles: FileCollection by lazy { fileEncryptPluginExtension.encryptedFiles(project) }

    init {
        description = "Encrypts all unencrypted files from configuration if they exist"
    }

    @TaskAction
    open fun encrypt(inputChanges: InputChanges) {
        val key = key.get()
        if (inputChanges.isIncremental) {
            logger.info("Input is incremental")
            inputChanges.getFileChanges(plainFiles).forEach { change ->
                if (change.fileType == FileType.DIRECTORY) return@forEach
                logger.info("Out of date: ${change.normalizedPath}")
                when (change.changeType) {
                    ChangeType.ADDED,
                    ChangeType.MODIFIED -> {
                        val plainFile = change.file
                        // TODO wtf? does it work properly?
                        val index = plainFiles.indexOf(plainFile)
                        val encryptedFile = encryptedFiles.asSequence().filterIndexed { i, _ -> i == index }.first()
                        enqueueEncryptionRunnable(key, plainFile, encryptedFile)
                    }
                    ChangeType.REMOVED,
                    null ->
                        // Do nothing on file removal, user must delete encrypted files by themselves
                        Unit
                }
            }
        } else {
            logger.info("Input is not incremental")
            plainFiles.zip(encryptedFiles).forEach { (plainFile, encryptedFile) ->
                enqueueEncryptionRunnable(key, plainFile, encryptedFile)
            }
        }
    }

    private fun enqueueEncryptionRunnable(key: SecretKey, plainFile: File, encryptedFile: File) {
        workerExecutor.noIsolation().submit(Action::class.java) { params ->
            params.key.set(key)
            params.plainFile.set(plainFile)
            params.encryptedFile.set(encryptedFile)
        }
    }

    interface Params : WorkParameters {
        val key: Property<SecretKey>
        val plainFile: Property<File>
        val encryptedFile: Property<File>
    }

    abstract class Action : WorkAction<Params> {
        private val logger: Logger = Logging.getLogger(Action::class.java)

        private val plainFile: File get() = parameters.plainFile.get()
        private val encryptedFile: File get() = parameters.encryptedFile.get()
        private val key: SecretKey get() = parameters.key.get()

        override fun execute() {
            logger.info("Encrypting file: ${plainFile.absolutePath}")
            if (!plainFile.exists() || !plainFile.canRead()) {
                logger.error("${plainFile.name} does not exist or can't be read")
                return
            }
            val iv = readOrGenerateIv()
            val cipher = createCipher(Cipher.ENCRYPT_MODE, key, iv)
            writeFile(cipher)
            logger.info("Encrypted file: ${encryptedFile.absolutePath}")
        }

        private fun readOrGenerateIv(): ByteArray =
            if (encryptedFile.exists() && encryptedFile.canRead()) {
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

        private fun writeFile(cipher: Cipher) {
            plainFile.inputStream().use { fileInputStream ->
                encryptedFile.parentFile.mkdirs()
                encryptedFile.outputStream().use { fileOutputStream ->
                    fileOutputStream.write(cipher.iv.size)
                    fileOutputStream.write(cipher.iv)
                    CipherOutputStream(fileOutputStream, cipher).use { cipherOutputStream ->
                        fileInputStream.copyTo(cipherOutputStream)
                    }
                }
            }
        }
    }
}
