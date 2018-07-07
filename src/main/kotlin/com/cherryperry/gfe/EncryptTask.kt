package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream

open class EncryptTask : DefaultTask() {

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
            logger.info("Encrypting file ${plainFile.absolutePath}")
            val result = encryptFile(plainFile, encryptedFile, key)
            result?.let { logger.info("Encrypted file: ${encryptedFile.absolutePath}") }
        }
    }

    private fun encryptFile(inputFile: File, outputFile: File, key: Key): File? {
        if (!inputFile.exists() || !inputFile.canRead()) {
            logger.error("${inputFile.name} does not exist or can't be read")
            return null
        }
        val iv = if (outputFile.exists() && outputFile.canRead()) {
            // if encrypted file already exists - reuse it's IV
            outputFile.inputStream().use { fileInputStream ->
                val ivSize = fileInputStream.read()
                val iv = ByteArray(ivSize)
                fileInputStream.read(iv)
                iv
            }
        } else {
            generateIv()
        }
        val cipher = createCipher(Cipher.ENCRYPT_MODE, key, iv)
        inputFile.inputStream().use { fileInputStream ->
            outputFile.outputStream().use { fileOutputStream ->
                fileOutputStream.write(cipher.iv.size)
                fileOutputStream.write(cipher.iv)
                CipherOutputStream(fileOutputStream, cipher).use { cipherOutputStream ->
                    fileInputStream.copyTo(cipherOutputStream)
                }
            }
        }
        return outputFile
    }
}
