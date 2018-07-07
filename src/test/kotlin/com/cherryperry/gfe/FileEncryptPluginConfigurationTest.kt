package com.cherryperry.gfe

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

/**
 * Test that plugin is applied correctly.
 * Plugin, extension and tasks must be created.
 */
class FileEncryptPluginConfigurationTest {

    @Test
    fun testApplyToProject() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply(PLUGIN_NAME)
        Assert.assertTrue(project.plugins.findPlugin(PLUGIN_NAME) is FileEncryptPlugin)
        Assert.assertTrue(project.extensions.findByName(FileEncryptPlugin.EXTENSION_NAME) is FileEncryptPluginExtension)
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) is EncryptTask)
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_DECRYPT_NAME) is DecryptTask)
    }
}
