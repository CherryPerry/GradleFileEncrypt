package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Test valid configuration of input and output properties of task.
 */
class FileEncryptTaskConfigurationTest {

    companion object {
        private const val PASSWORD = "password"
    }

    @Rule
    @JvmField
    var temporaryFolder = TemporaryFolder()

    private lateinit var project: Project
    private lateinit var extension: FileEncryptExtension
    private val encryptTask by lazy { project.tasks.getByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) as EncryptTask }
    private val decryptTask by lazy { project.tasks.getByName(FileEncryptPlugin.TASK_DECRYPT_NAME) as DecryptTask }

    @Before
    fun before() {
        project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.pluginManager.apply(PLUGIN_NAME)
        extension = project.extensions.getByType(FileEncryptExtension::class.java)
        extension.passwordProvider.set { PASSWORD.toCharArray() }
        project.tasks.getByName(FileEncryptPlugin.TASK_GIT_IGNORE_NAME) as CheckGitIgnoreTask
    }

    @Test
    fun testEncryptTaskConfigurationEmpty() {
        val inputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(0, inputs.size)
        val outputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(0, outputs.size)
    }

    @Test
    fun testEncryptTaskConfigurationSingle() {
        extension.plainFiles.from("1.txt")
        val inputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(1, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt"))
        val outputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(1, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
    }

    @Test
    fun testEncryptTaskConfigurationMultiple() {
        extension.plainFiles.from("1.txt", "2.txt")
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
    fun testEncryptTaskConfigurationMapping() {
        extension.plainFiles.from("1.txt", "2.txt")
        extension.mapping.put("2.txt", "3.txt")
        val inputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(2, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt"))
        Assert.assertTrue(inputs[1].endsWith("2.txt"))
        val outputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(2, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
        Assert.assertTrue(outputs[1].endsWith("3.txt.${FileNameTransformer.EXTENSION}"))
    }

    @Test
    fun testDecryptTaskConfigurationEmpty() {
        val inputs = decryptTask.encryptedFiles.toList()
        Assert.assertEquals(0, inputs.size)
        val outputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(0, outputs.size)
    }

    @Test
    fun testDecryptTaskConfigurationSingle() {
        extension.plainFiles.from("1.txt")
        val inputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(1, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
        val outputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(1, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt"))
    }

    @Test
    fun testDecryptTaskConfigurationMultiple() {
        extension.plainFiles.from("1.txt", "2.txt")
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
    fun testDecryptTaskConfigurationMapping() {
        extension.plainFiles.from("1.txt", "2.txt")
        extension.mapping.put("2.txt", "3.txt")
        val inputs = encryptTask.encryptedFiles.toList()
        Assert.assertEquals(2, inputs.size)
        Assert.assertTrue(inputs[0].endsWith("1.txt.${FileNameTransformer.EXTENSION}"))
        Assert.assertTrue(inputs[1].endsWith("3.txt.${FileNameTransformer.EXTENSION}"))
        val outputs = encryptTask.plainFiles.toList()
        Assert.assertEquals(2, outputs.size)
        Assert.assertTrue(outputs[0].endsWith("1.txt"))
        Assert.assertTrue(outputs[1].endsWith("2.txt"))
    }
}
