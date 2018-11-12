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
        // test task registered lazy
        // see https://docs.gradle.org/current/userguide/task_configuration_avoidance.html
        project.tasks.named(FileEncryptPlugin.TASK_ENCRYPT_NAME)
        project.tasks.named(FileEncryptPlugin.TASK_DECRYPT_NAME)
        project.tasks.named(FileEncryptPlugin.TASK_GIT_IGNORE_NAME)
        // test tasks can be created
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) is EncryptTask)
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_DECRYPT_NAME) is DecryptTask)
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_GIT_IGNORE_NAME) is CheckGitIgnoreTask)
    }
}
