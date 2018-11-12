package com.cherryperry.gfe.base

import java.io.File

interface EncryptedFilesAware {

    val encryptedFiles: Iterable<File>
}
