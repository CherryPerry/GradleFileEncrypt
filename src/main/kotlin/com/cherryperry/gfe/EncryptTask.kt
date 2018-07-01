package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream

open class EncryptTask : DefaultTask() {

    @get:Input
    open val files = project.objects.listProperty(File::class.java)!!

    init {
        group = TASK_GROUP
        description = "Encrypts all unencrypted files from configuration if they exist"
    }

    @TaskAction
    open fun encrypt() {
        val password = PasswordReader.getPassword(logger, project, SystemEnvironment)
        val key = generateKey(password)
        password.fill(' ')
        files.get().forEach { relativeFile ->
            val file = project.file(relativeFile)
            logger.warn("Full path $relativeFile")
            encryptFile(file, key)
        }
    }

    open fun encryptFile(file: File, key: Key): File? {
        if (!file.exists() || !file.canRead()) {
            logger.error("${file.name} does not exist of can't be read")
            return null
        }
        val cipher = createCipher(Cipher.ENCRYPT_MODE, key, generateIv())
        val encryptedFile = File(file.parentFile, "${file.name}.encrypted")
        logger.warn("Encrypted full path $encryptedFile")
        FileInputStream(file).use { fileInputStream ->
            val fileOutputStream = FileOutputStream(encryptedFile)
            fileOutputStream.write(cipher.iv.size)
            fileOutputStream.write(cipher.iv)
            CipherOutputStream(fileOutputStream, cipher).use { cipherOutputStream ->
                fileInputStream.copyTo(cipherOutputStream, BUFFER_SIZE)
            }
        }
        return encryptedFile
    }
}
