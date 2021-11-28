package com.cherryperry.gfe

import com.cherryperry.gfe.base.BaseTask
import com.cherryperry.gfe.base.EncryptedFilesAware
import com.cherryperry.gfe.base.PlainFilesAware
import com.cherryperry.gfe.base.SecretKeyAware
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKey
import javax.inject.Inject

open class DecryptTask @Inject constructor(
    private val workerExecutor: WorkerExecutor
) : BaseTask(), SecretKeyAware, PlainFilesAware, EncryptedFilesAware {

    @get:Input
    override val key: Provider<SecretKey> = fileEncryptPluginExtension.secretKey(project)

    @get:[InputFiles SkipWhenEmpty]
    override val encryptedFiles: FileCollection by lazy { fileEncryptPluginExtension.encryptedFiles(project) }

    @get:OutputFiles
    override val plainFiles: FileCollection = fileEncryptPluginExtension.plainFiles

    init {
        description = "Decrypts all encrypted files from configuration if they exist"
    }

    @TaskAction
    open fun decrypt(incrementalTaskInputs: IncrementalTaskInputs) {
        val key = key.get()
        if (incrementalTaskInputs.isIncremental) {
            logger.info("Input is incremental")
            incrementalTaskInputs.outOfDate { inputFileDetails ->
                logger.info("Out of date: ${inputFileDetails.file}")
                val encryptedFile = inputFileDetails.file
                val index = encryptedFiles.indexOf(encryptedFile)
                val plainFile = plainFiles.asSequence().filterIndexed { i, _ -> i == index }.first()
                enqueueDecryptionRunnable(key, encryptedFile, plainFile)
            }
            incrementalTaskInputs.removed {
                // Do nothing on file removal, user must delete decrypted files by himself
            }
        } else {
            logger.info("Input is not incremental")
            plainFiles.zip(encryptedFiles).forEach { (plainFile, encryptedFile) ->
                enqueueDecryptionRunnable(key, encryptedFile, plainFile)
            }
        }
    }

    private fun enqueueDecryptionRunnable(key: SecretKey, encryptedFile: File, plainFile: File) {
        workerExecutor.noIsolation().submit(Action::class.java) { params ->
            params.key.set(key)
            params.encryptedFile.set(encryptedFile)
            params.plainFile.set(plainFile)
        }
    }

    interface Params : WorkParameters {
        val key: Property<SecretKey>
        val encryptedFile: Property<File>
        val plainFile: Property<File>
    }

    abstract class Action : WorkAction<Params> {
        private val logger: Logger = Logging.getLogger(Action::class.java)

        override fun execute() {
            val plainFile = parameters.plainFile.get()
            val encryptedFile = parameters.encryptedFile.get()
            val key = parameters.key.get()
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
                plainFile.parentFile.mkdirs()
                plainFile.outputStream().use { fileOutputStream ->
                    cipherInputStream.copyTo(fileOutputStream)
                }
            }
            logger.info("Decrypted file: ${plainFile.absolutePath}")
        }
    }
}
