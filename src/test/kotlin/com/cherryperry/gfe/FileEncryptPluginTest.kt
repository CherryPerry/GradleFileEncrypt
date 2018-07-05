package com.cherryperry.gfe

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

class FileEncryptPluginTest {

    @Test
    fun testApplyToProject() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("com.cherryperry.gradle-file-encrypt")
        Assert.assertTrue(project.plugins.findPlugin("com.cherryperry.gradle-file-encrypt") is FileEncryptPlugin)
        Assert.assertTrue(project.extensions.findByName(FileEncryptPlugin.EXTENSION_NAME) is FileEncryptPluginExtension)
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_ENCRYPT_NAME) is EncryptTask)
        Assert.assertTrue(project.tasks.findByName(FileEncryptPlugin.TASK_DECRYPT_NAME) is DecryptTask)
    }
}
