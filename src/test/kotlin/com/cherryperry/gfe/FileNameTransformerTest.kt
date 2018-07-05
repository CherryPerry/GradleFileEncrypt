package com.cherryperry.gfe

import org.junit.Assert
import org.junit.Test
import java.io.File

class FileNameTransformerTest {

    private val originalFile = File("/test/test.file")
    private val encryptedFile = File(originalFile.parent, "${originalFile.name}.${FileNameTransformer.EXTENSION}")

    @Test
    fun testEncryptedFileFromFile() {
        Assert.assertEquals(encryptedFile, FileNameTransformer.encryptedFileFromFile(originalFile))
    }

    @Test
    fun testOriginalFileFromEncryptedFile() {
        Assert.assertEquals(originalFile, FileNameTransformer.originalFileFromEncryptedFile(encryptedFile))
        Assert.assertEquals(originalFile, FileNameTransformer.originalFileFromEncryptedFile(originalFile))
    }
}
