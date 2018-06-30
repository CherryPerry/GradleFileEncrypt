package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.FileOutputStream
import java.util.Properties

class PasswordReaderTest {

    @Rule
    @JvmField
    var temporaryFolder = TemporaryFolder()

    private lateinit var project: Project
    private val password = "PASSWORD"

    @Before
    fun before() {
        project = ProjectBuilder.builder()
            .withName("test")
            .withProjectDir(temporaryFolder.root)
            .build()
    }

    @Test
    fun testEnvironment() {
        val result = PasswordReader.getPassword(project.logger, project,
            TestEnvironment(mapOf(PasswordReader.ENVIRONMENT_KEY to password)))
        Assert.assertArrayEquals(password.toCharArray(), result)
    }

    @Test
    fun testLocalProperties() {
        val properties = Properties()
        properties.setProperty(PasswordReader.LOCAL_PROPERTIES_KEY, password)
        FileOutputStream(temporaryFolder.newFile(PasswordReader.LOCAL_PROPERTIES_FILE)).use { stream ->
            properties.store(stream, null)
        }
        val result = PasswordReader.getPassword(project.logger, project, TestEnvironment(emptyMap()))
        Assert.assertArrayEquals(password.toCharArray(), result)
    }

    @Test(expected = IllegalStateException::class)
    fun testEmpty() {
        PasswordReader.getPassword(project.logger, project, TestEnvironment(emptyMap()))
    }
}
