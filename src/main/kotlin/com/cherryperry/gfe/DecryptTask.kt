package com.cherryperry.gfe

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.CipherInputStream

open class DecryptTask : DefaultTask() {
    open var files = emptyArray<String>()
    open var rewrite = true

    @TaskAction
    open fun decrypt() {
        val password = PasswordReader.getPassword(logger, project, SystemEnvironment, System.console())
        val key = generateKey(password)
        password.fill('0')
        files.forEach { relativeFile ->
            val file = project.file(relativeFile)
            decryptFile(file, key)
        }
    }

    open fun decryptFile(file: File, key: Key) {
        if (rewrite || !file.exists()) {
            val encryptedFile = File(file.parentFile, "${file.name}.encrypted")
            if (!encryptedFile.exists() || !encryptedFile.canRead()) {
                return
            }
            val fileInputStream = FileInputStream(encryptedFile)
            val ivSize = fileInputStream.read()
            val iv = ByteArray(ivSize)
            fileInputStream.read(iv)
            val cipher = createCipher(Cipher.DECRYPT_MODE, key, iv)
            cipher.updateAAD(byteArrayOf())
            CipherInputStream(fileInputStream, cipher).use { cipherInputStream ->
                FileOutputStream(file).use { fileOutputStream ->
                    cipherInputStream.copyTo(fileOutputStream, 8 * 1024)
                }
            }
        }
    }
}