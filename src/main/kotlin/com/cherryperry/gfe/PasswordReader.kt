package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import java.io.FileInputStream
import java.util.Properties

object PasswordReader {

    const val LOCAL_PROPERTIES_FILE = "local.properties"
    const val LOCAL_PROPERTIES_KEY = "gfe.password"
    const val ENVIRONMENT_KEY = "GFE_PASSWORD"

    fun getPassword(logger: Logger?, project: Project, environment: Environment): CharArray {
        // local.properties file
        readProperties(project)?.let {
            logger?.warn("$LOCAL_PROPERTIES_FILE is used for password")
            return it
        }
        // environment
        readEnvironment(environment)?.let {
            logger?.warn("$ENVIRONMENT_KEY is used for password")
            return it
        }
        throw IllegalStateException("""
            |Set $LOCAL_PROPERTIES_KEY in $LOCAL_PROPERTIES_FILE
            |or environment variable $ENVIRONMENT_KEY
            """.trimMargin())
    }

    private fun readProperties(project: Project): CharArray? {
        val properties = Properties()
        val propertiesFile = project.file(LOCAL_PROPERTIES_FILE)
        if (propertiesFile.exists() && propertiesFile.canRead()) {
            FileInputStream(propertiesFile).use { stream ->
                properties.load(stream)
                val password = properties.getProperty(LOCAL_PROPERTIES_KEY)
                password?.apply {
                    return toCharArray()
                }
            }
        }
        return null
    }

    private fun readEnvironment(environment: Environment): CharArray? {
        val environmentPassword: String? = environment[ENVIRONMENT_KEY]
        environmentPassword?.apply {
            return toCharArray()
        }
        return null
    }
}
