package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream

open class EncryptTask : DefaultTask() {
    open var files = emptyArray<String>()

    @TaskAction
    open fun encrypt() {
        val password = PasswordReader.getPassword(logger, project, SystemEnvironment, System.console())
        val key = generateKey(password)
        password.fill('0')
        files.forEach { relativeFile ->
            val file = project.file(relativeFile)
            encryptFile(file, key)
        }
    }

    open fun encryptFile(file: File, key: Key): File? {
        if (!file.exists() || !file.canRead()) {
            return null
        }
        val cipher = createCipher(Cipher.ENCRYPT_MODE, key, generateIv())
        val encryptedFile = File(file.parentFile, "${file.name}.encrypted")
        FileInputStream(file).use { fileInputStream ->
            val fileOutputStream = FileOutputStream(encryptedFile)
            fileOutputStream.write(cipher.iv.size)
            fileOutputStream.write(cipher.iv)
            CipherOutputStream(fileOutputStream, cipher).use { cipherOutputStream ->
                fileInputStream.copyTo(cipherOutputStream, 8 * 1024)
            }
        }
        return encryptedFile
    }
}