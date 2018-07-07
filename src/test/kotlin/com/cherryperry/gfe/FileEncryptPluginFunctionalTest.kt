package com.cherryperry.gfe

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.BuildTask
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class FileEncryptPluginFunctionalTest(
    private val gradleVersion: String
) {

    companion object {
        const val EMPTY_BUILD_GRADLE = "plugins { id 'com.cherryperry.gradle-file-encrypt' }"
        const val CONTENT_1 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        const val CONTENT_2 = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        const val PASSWORD = "password"

        // test all supported gradle versions
        @JvmStatic
        @Parameterized.Parameters(name = "Gradle version: {0}")
        fun gradleVersions(): Collection<Array<Any>> = arrayListOf(
            arrayOf<Any>("4.0.2"),
            arrayOf<Any>("4.1"),
            arrayOf<Any>("4.2.1"),
            arrayOf<Any>("4.3.1"),
            arrayOf<Any>("4.4.1"),
            arrayOf<Any>("4.5.1"),
            arrayOf<Any>("4.6"),
            arrayOf<Any>("4.7"),
            arrayOf<Any>("4.8.1"))
    }

    @Rule
    @JvmField
    var temporaryFolder = TemporaryFolder()

    private fun createRunner(
        buildGradleContent: String = EMPTY_BUILD_GRADLE,
        vararg args: String
    ): BuildResult {
        val buildGradle = File(temporaryFolder.root, "build.gradle")
        buildGradle.writeText(buildGradleContent)
        return GradleRunner.create()
            .withPluginClasspath()
            .withProjectDir(temporaryFolder.root)
            .withGradleVersion(gradleVersion)
            .withArguments(args.asList())
            .forwardStdOutput(System.out.writer())
            .forwardStdError(System.err.writer())
            .build()
    }

    private fun buildGradleConfigurationWithFiles(vararg args: File): String {
        val files = args.joinToString(transform = { "'${it.relativeTo(temporaryFolder.root)}'" })
        return """
            $EMPTY_BUILD_GRADLE
            gradleFileEncrypt
            {
                files $files
                passwordProvider { return '$PASSWORD'.toCharArray() }
            }
        """.trimIndent()
    }

    @Test
    fun testEncryptTaskNoSourceWhenEmpty() {
        // test task without input must be skipped
        createRunner(EMPTY_BUILD_GRADLE, FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.NO_SOURCE, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
    }

    @Test
    fun testDecryptTaskNoSourceWhenEmpty() {
        // test task without input must be skipped
        createRunner(EMPTY_BUILD_GRADLE, FileEncryptPlugin.TASK_DECRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.NO_SOURCE, it[FileEncryptPlugin.TASK_DECRYPT_NAME].outcome)
        }
    }

    @Test
    fun testEncryptAndDecryptSingleFile() {
        // test tasks work as expected
        val testFile = temporaryFolder.newFile()
        testFile.writeText(CONTENT_1)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        testFile.delete()
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_DECRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_DECRYPT_NAME].outcome)
        }
        Assert.assertEquals(CONTENT_1, testFile.readText())
    }

    @Test
    fun testEncryptAndDecryptSingleFileCache() {
        // test gradle task cache support without file changes
        val testFile = temporaryFolder.newFile()
        testFile.writeText(CONTENT_1)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        // second call should be skipped due cache
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.UP_TO_DATE, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_DECRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_DECRYPT_NAME].outcome)
        }
        // second call should be skipped due cache
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_DECRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.UP_TO_DATE, it[FileEncryptPlugin.TASK_DECRYPT_NAME].outcome)
        }
    }

    @Test
    fun testEncryptAndDecryptSingleFileCacheInvalidate() {
        // test gradle task cache support with file changes
        val testFile = temporaryFolder.newFile()
        testFile.writeText(CONTENT_1)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        // invalidate encryption source
        testFile.writeText(CONTENT_2)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        testFile.delete()
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_DECRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_DECRYPT_NAME].outcome)
        }
        Assert.assertEquals(CONTENT_2, testFile.readText())
        // invalidate decryption result
        testFile.writeText(CONTENT_1)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_DECRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_DECRYPT_NAME].outcome)
        }
        Assert.assertEquals(CONTENT_2, testFile.readText())
    }

    @Test
    fun testEncryptSameResultTwiceNoCache() {
        // 2 passes of encryption task should produce same result
        val testFile = temporaryFolder.newFile()
        testFile.writeText(CONTENT_1)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        val resultFile = FileNameTransformer.encryptedFileFromFile(testFile)
        val data = resultFile.readBytes()
        val lastModified = resultFile.lastModified()
        // reset cache
        // TODO Other cache invalidating, it may produce error in future
        testFile.delete()
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        testFile.writeText(CONTENT_1)
        // wait for timestamp change
        Thread.sleep(100)
        // generate again
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        // last modified must change and content not
        Assert.assertNotEquals(lastModified, resultFile.lastModified())
        Assert.assertArrayEquals(data, resultFile.readBytes())
    }

    @Test
    fun testEncryptSameIV() {
        // encrypt task must use same iv, if encryption target exists
        val testFile = temporaryFolder.newFile()
        testFile.writeText(CONTENT_1)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        val resultFile = FileNameTransformer.encryptedFileFromFile(testFile)
        val fileStart1 = resultFile.inputStream().use {
            val array = ByteArray(17)
            it.read(array)
            array
        }
        testFile.writeText(CONTENT_2)
        createRunner(buildGradleConfigurationWithFiles(testFile), FileEncryptPlugin.TASK_ENCRYPT_NAME).let {
            Assert.assertEquals(TaskOutcome.SUCCESS, it[FileEncryptPlugin.TASK_ENCRYPT_NAME].outcome)
        }
        val fileStart2 = resultFile.inputStream().use {
            val array = ByteArray(17)
            it.read(array)
            array
        }
        Assert.assertArrayEquals(fileStart1, fileStart2)
    }

    operator fun BuildResult.get(taskName: String): BuildTask = task(":$taskName")!!
}
