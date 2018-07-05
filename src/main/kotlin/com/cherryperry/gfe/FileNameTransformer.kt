package com.cherryperry.gfe

import java.io.File

object FileNameTransformer {

    const val EXTENSION = "encrypted"

    fun encryptedFileFromFile(file: File) = File(file.parentFile, "${file.name}.$EXTENSION")

    fun originalFileFromEncryptedFile(file: File) =
        if (file.extension == EXTENSION) File(file.parentFile, file.nameWithoutExtension) else file
}
