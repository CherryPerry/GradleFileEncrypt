package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

open class DecryptTask : DefaultTask() {

    @get:Input
    open var files = project.objects.listProperty(File::class.java)!!

    init {
        group = GROUP_NAME
        description = "Decrypts all encrypted files from configuration if they exist"
    }

    @TaskAction
    open fun decrypt() {
        val password = PasswordReader.getPassword(logger, project, SystemEnvironment)
        val key = generateKey(password)
        password.fill(' ')
        files.get().forEach { relativeFile ->
            val file = project.file(relativeFile)
            logger.warn("Full path $relativeFile")
            decryptFile(file, key)
        }
    }

    open fun decryptFile(file: File, key: Key) {
        val encryptedFile = File(file.parentFile, "${file.name}.encrypted")
        logger.warn("Encrypted full path $encryptedFile")
        if (!encryptedFile.exists() || !encryptedFile.canRead()) {
            logger.error("${encryptedFile.name} does not exist of can't be read")
            return
        }
        val fileInputStream = FileInputStream(encryptedFile)
        val ivSize = fileInputStream.read()
        val iv = ByteArray(ivSize)
        fileInputStream.read(iv)
        val cipher = createCipher(Cipher.DECRYPT_MODE, key, iv)
        CipherInputStream(fileInputStream, cipher).use { cipherInputStream ->
            FileOutputStream(file).use { fileOutputStream ->
                cipherInputStream.copyTo(fileOutputStream, BUFFER_SIZE)
            }
        }
    }
}
