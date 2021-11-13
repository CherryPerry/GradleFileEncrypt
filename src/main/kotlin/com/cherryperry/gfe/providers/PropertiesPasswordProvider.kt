package com.cherryperry.gfe.providers

import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ProviderFactory
import java.io.ByteArrayInputStream
import java.util.Properties

internal class PropertiesPasswordProvider(
    private val providers: ProviderFactory,
    private val layout: ProjectLayout,
) : PasswordProvider {
    override val password: CharArray?
        get() {
            val propertiesFile =
                providers
                    .fileContents(layout.projectDirectory.file(LOCAL_PROPERTIES_FILE))
                    .asBytes
                    .forUseAtConfigurationTime()
                    .orNull
            if (propertiesFile != null) {
                Properties().apply {
                    ByteArrayInputStream(propertiesFile).use { load(it) }
                    propertiesFile.fill(0)
                    getProperty(LOCAL_PROPERTIES_KEY)?.apply { return toCharArray() }
                }
            }
            return null
        }

    companion object {
        const val LOCAL_PROPERTIES_FILE = "local.properties"
        const val LOCAL_PROPERTIES_KEY = "gfe.password"
    }
}
