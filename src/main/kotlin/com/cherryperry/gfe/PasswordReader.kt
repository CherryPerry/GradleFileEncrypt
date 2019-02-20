package com.cherryperry.gfe

import java.io.FileInputStream
import java.util.Properties
import java.util.concurrent.Callable
import org.gradle.api.Project
import org.gradle.api.logging.Logger

object PasswordReader {

    const val LOCAL_PROPERTIES_FILE = "local.properties"
    const val LOCAL_PROPERTIES_KEY = "gfe.password"
    const val ENVIRONMENT_KEY = "GFE_PASSWORD"

    fun getPassword(
        logger: Logger?,
        project: Project,
        environment: Environment,
        provider: Callable<CharArray>?
    ): CharArray {
        val providers = arrayListOf(
            DelegatePasswordProvider(provider),
            PropertiesPasswordProvider(project),
            EnvironmentPasswordProvider(environment))
        providers.forEach { passwordProvider ->
            val result = passwordProvider.getPassword()
            result?.let {
                logger?.info("${passwordProvider::class} is used for password")
                return it
            }
        }
        throw IllegalStateException("Setup password before encryption/decryption!")
    }

    interface PasswordProvider {

        fun getPassword(): CharArray?
    }

    class DelegatePasswordProvider(
        private val provider: Callable<CharArray>?
    ) : PasswordProvider {

        override fun getPassword(): CharArray? = provider?.call()
    }

    class PropertiesPasswordProvider(
        private val project: Project
    ) : PasswordProvider {

        override fun getPassword(): CharArray? {
            val propertiesFile = project.file(LOCAL_PROPERTIES_FILE)
            if (propertiesFile.exists() && propertiesFile.canRead()) {
                Properties().apply {
                    FileInputStream(propertiesFile).use { load(it) }
                    getProperty(LOCAL_PROPERTIES_KEY)?.apply { return toCharArray() }
                }
            }
            return null
        }
    }

    class EnvironmentPasswordProvider(
        private val environment: Environment
    ) : PasswordProvider {

        override fun getPassword(): CharArray? = environment[ENVIRONMENT_KEY]?.toCharArray()
    }
}
