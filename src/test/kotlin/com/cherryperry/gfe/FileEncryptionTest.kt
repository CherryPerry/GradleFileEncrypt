package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileOutputStream

class FileEncryptionTest {

    companion object {
        private const val CONTENT = "Super secret information"
        private val PASSWORD = "password".toCharArray()
    }

    @Rule
    @JvmField
    var temporaryFolder = TemporaryFolder()

    private lateinit var project: Project
    private lateinit var decryptTask: DecryptTask
    private lateinit var encryptTask: EncryptTask

    @Before
    fun before() {
        project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        decryptTask = project.tasks.create(DecryptTask::class.java.name, DecryptTask::class.java)
        encryptTask = project.tasks.create(EncryptTask::class.java.name, EncryptTask::class.java)
    }

    @Test
    fun testEncryptionAndDecryption() {
        // create test file
        val file = temporaryFolder.newFile()
        // write content to it
        FileOutputStream(file).writer(Charsets.UTF_8).use { it.write(CONTENT) }
        // encrypt file
        val encryptedFile = encryptTask.encryptFile(file, generateKey(PASSWORD))
        Assert.assertNotNull(encryptedFile)
        // delete original
        file.delete()
        // decrypt encrypted
        decryptTask.decryptFile(file, generateKey(PASSWORD))
        // assert content
        val content = file.readText(Charsets.UTF_8)
        Assert.assertEquals(CONTENT, content)
    }

    @Test
    fun testReEncryption() {
        // create test file
        val file = temporaryFolder.newFile()
        // write content to it
        FileOutputStream(file).writer(Charsets.UTF_8).use { it.write(CONTENT) }
        // encrypt file
        val encryptedFile = encryptTask.encryptFile(file, generateKey(PASSWORD))
        // save it's content
        val originalBytes = encryptedFile?.readBytes()
        // encrypt again
        encryptTask.encryptFile(file, generateKey(PASSWORD))
        // check content is same
        val newBytes = encryptedFile?.readBytes()
        Assert.assertArrayEquals(originalBytes, newBytes)
    }
}
