package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

open class DecryptTask : DefaultTask() {

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
            logger.info("Decrypting file ${encryptedFile.absolutePath}")
            val result = decryptFile(encryptedFile, plainFile, key)
            result?.let { logger.info("Decrypted file: ${plainFile.absolutePath}") }
        }
    }

    private fun decryptFile(inputFile: File, outputFile: File, key: Key): File? {
        if (!inputFile.exists() || !inputFile.canRead()) {
            logger.error("${inputFile.name} does not exist or can't be read")
            return null
        }
        val fileInputStream = inputFile.inputStream()
        val ivSize = fileInputStream.read()
        val iv = ByteArray(ivSize)
        fileInputStream.read(iv)
        val cipher = createCipher(Cipher.DECRYPT_MODE, key, iv)
        CipherInputStream(fileInputStream, cipher).use { cipherInputStream ->
            outputFile.outputStream().use { fileOutputStream ->
                cipherInputStream.copyTo(fileOutputStream)
            }
        }
        return inputFile
    }
}
