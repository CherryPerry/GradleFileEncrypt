package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.Console
import java.io.FileInputStream
import java.util.Properties
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

object PasswordReader {

    const val LOCAL_PROPERTIES_FILE = "local.properties"
    const val LOCAL_PROPERTIES_KEY = "gfe.password"
    const val ENVIRONMENT_KEY = "GFE.PASSWORD"

    fun getPassword(logger: Logger?, project: Project, environment: Environment, console: Console?): CharArray {
        // local.properties file
        val properties = Properties()
        val propertiesFile = project.file(LOCAL_PROPERTIES_FILE)
        if (propertiesFile.exists() && propertiesFile.canRead()) {
            FileInputStream(propertiesFile).use { stream ->
                properties.load(stream)
                val password = properties.getProperty(LOCAL_PROPERTIES_KEY)
                password?.apply { return toCharArray() }
            }
        }
        // environment
        val environmentPassword: String? = environment[ENVIRONMENT_KEY]
        environmentPassword?.apply { return toCharArray() }
        // user input
        val inputPassword = AtomicReference<CharArray>()
        console?.apply {
            val semaphore = Semaphore(0)
            val thread = thread {
                try {
                    inputPassword.set(readPassword("Input password:"))
                } catch (exception: Exception) {
                    logger?.error("Error while reading password", exception)
                } finally {
                    semaphore.release()
                }
            }
            // must be timeout for invalid configuration
            semaphore.tryAcquire(30, TimeUnit.SECONDS)
            thread.interrupt()
        }
        if (inputPassword.get() == null) {
            throw IllegalStateException("Set $LOCAL_PROPERTIES_KEY in $LOCAL_PROPERTIES_FILE, environment variable $ENVIRONMENT_KEY or input password in console")
        }
        return inputPassword.get()
    }
}