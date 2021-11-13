package com.cherryperry.gfe

import com.cherryperry.gfe.providers.PropertiesPasswordProvider
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertArrayEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileEncryptExtensionSecretKeyTest {

    @get:Rule
    var temporaryFolder = TemporaryFolder()

    @Test
    fun `password can be provided by delegate`() {
        val project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.pluginManager.apply(PLUGIN_NAME)
        project.fileEncryptPluginExtension.passwordProvider.set { PASSWORD.toCharArray() }
        val encryptTask = project.tasks.getByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) as EncryptTask
        assertArrayEquals(KEY, encryptTask.key.orNull?.encoded)
    }

    @Test
    fun `password can be provided by local properties`() {
        temporaryFolder.newFile(PropertiesPasswordProvider.LOCAL_PROPERTIES_FILE).writeText("gfe.password=$PASSWORD")
        val project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
        project.pluginManager.apply(PLUGIN_NAME)
        val encryptTask = project.tasks.getByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) as EncryptTask
        assertArrayEquals(KEY, encryptTask.key.orNull?.encoded)
    }

    @Test
    fun `password can be provided by env variable`() {
        // TODO Can't provide property into separate process
        /*try {
            System.setProperty(PropertiesPasswordProvider.LOCAL_PROPERTIES_KEY, PASSWORD)
            val project = ProjectBuilder.builder().withProjectDir(temporaryFolder.root).build()
            project.pluginManager.apply(PLUGIN_NAME)
            val encryptTask = project.tasks.getByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) as EncryptTask
            assertArrayEquals(KEY, encryptTask.key.orNull?.encoded)
        } finally {
            System.clearProperty(PropertiesPasswordProvider.LOCAL_PROPERTIES_KEY)
        }*/
    }

    private companion object {
        private const val PASSWORD = "password"
        private val KEY = byteArrayOf(-94, -62, 100, 97, -122, -126, -124, 116, -73, 84, 89, 26, 84, 124, 24, -15)
    }
}
