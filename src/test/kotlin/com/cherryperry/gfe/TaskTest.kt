package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Callable

class TaskTest {

    companion object {
        private const val CONTENT = "Super secret information"
        private const val PASSWORD = "password"
    }

    @Rule
    @JvmField
    var temporaryFolder = TemporaryFolder()

    private lateinit var project: Project
    private lateinit var extension: FileEncryptPluginExtension
    private lateinit var encryptTask: EncryptTask
    private lateinit var decryptTask: DecryptTask

    @Before
    fun before() {
        project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.pluginManager.apply("com.cherryperry.gradle-file-encrypt")
        extension = project.extensions.getByType(FileEncryptPluginExtension::class.java)
        extension.passwordProvider = Callable { PASSWORD.toCharArray() }
        encryptTask = project.tasks.getByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) as EncryptTask
        decryptTask = project.tasks.getByName(FileEncryptPlugin.TASK_DECRYPT_NAME) as DecryptTask
    }

    @Test
    fun testEncryptTaskConfigurationEmpty() {
        extension.files = emptyArray()
        val inputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(0, inputs.size)
        val outputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(0, outputs.size)
    }

    @Test
    fun testEncryptTaskConfigurationSingle() {
        extension.files = arrayOf("1.txt")
        val inputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(1, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt"))
        val outputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(1, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
    }

    @Test
    fun testEncryptTaskConfigurationMultiple() {
        extension.files = arrayOf("1.txt", "2.txt")
        val inputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(2, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt"))
        Assert.assertTrue(inputs[1].endsWith("2.txt"))
        val outputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(2, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
        Assert.assertTrue(outputs[1].endsWith("2.txt.${FileNameTransformer.EXTENSION}"))
    }

    @Test
    fun testDecryptTaskConfigurationEmpty() {
        extension.files = emptyArray()
        val inputs = decryptTask.encryptedFiles.toList()
        Assert.assertEquals(0, inputs.size)
        val outputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(0, outputs.size)
    }

    @Test
    fun testDecryptTaskConfigurationSingle() {
        extension.files = arrayOf("1.txt")
        val inputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(1, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
        val outputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(1, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt"))
    }

    @Test
    fun testDecryptTaskConfigurationMultiple() {
        extension.files = arrayOf("1.txt", "2.txt")
        val inputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(2, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
        Assert.assertTrue(inputs[1].endsWith("2.txt.${FileNameTransformer.EXTENSION}"))
        val outputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(2, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt"))
        Assert.assertTrue(outputs[1].endsWith("2.txt"))
    }

    @Test
    fun testEncryptionAndDecryption() {
        val file = temporaryFolder.newFile()
        FileOutputStream(file).use { it.writer(Charsets.UTF_8).use { it.write(CONTENT) } }
        extension.files = arrayOf(file)
        encryptTask.encrypt()
        file.delete()
        decryptTask.decrypt()
        val content = FileInputStream(file).use { it.reader(Charsets.UTF_8).use { it.readText() } }
        Assert.assertEquals(CONTENT, content)
    }

    @Test
    fun testEncryptionSameIv() {
        val file = temporaryFolder.newFile()
        FileOutputStream(file).use { it.writer(Charsets.UTF_8).use { it.write(CONTENT) } }
        extension.files = arrayOf(file)
        encryptTask.encrypt()
        val originalBytes = encryptTask.encryptedFiles.toList()[0].readBytes()
        encryptTask.encrypt()
        val newBytes = encryptTask.encryptedFiles.toList()[0].readBytes()
        Assert.assertArrayEquals(originalBytes, newBytes)
    }
}
