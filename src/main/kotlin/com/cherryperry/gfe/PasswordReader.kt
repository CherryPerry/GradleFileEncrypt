package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logger
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.io.ByteArrayInputStream
import java.util.Properties
import java.util.concurrent.Callable

object PasswordReader {

    const val LOCAL_PROPERTIES_FILE = "local.properties"
    const val LOCAL_PROPERTIES_KEY = "gfe.password"
    const val ENVIRONMENT_KEY = "GFE_PASSWORD"

    fun getPassword(
        logger: Logger?,
        project: Project,
        environment: Environment,
        provider: Provider<Callable<CharArray>>,
    ): CharArray {
        val providers = arrayListOf(
            DelegatePasswordProvider(provider),
            PropertiesPasswordProvider(project.providers, project.layout),
            EnvironmentPasswordProvider(environment)
        )
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
        private val provider: Provider<Callable<CharArray>>,
    ) : PasswordProvider {

        override fun getPassword(): CharArray? = provider.orNull?.call()
    }

    class PropertiesPasswordProvider(
        private val providers: ProviderFactory,
        private val layout: ProjectLayout,
    ) : PasswordProvider {

        override fun getPassword(): CharArray? {
            val propertiesFile = providers.fileContents(layout.projectDirectory.file(LOCAL_PROPERTIES_FILE))
                .asBytes.forUseAtConfigurationTime().orNull
            if (propertiesFile != null) {
                Properties().apply {
                    ByteArrayInputStream(propertiesFile).use { load(it) }
                    propertiesFile.fill(0)
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
