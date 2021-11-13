package com.cherryperry.gfe

import com.cherryperry.gfe.providers.DelegatePasswordProvider
import com.cherryperry.gfe.providers.EnvironmentPasswordProvider
import com.cherryperry.gfe.providers.PropertiesPasswordProvider
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import javax.crypto.SecretKey

internal fun FileEncryptExtension.encryptedFiles(project: Project): FileCollection =
    plainFiles
        .map { original ->
            val originalRel = original.relativeTo(project.projectDir).path
            mapping.getting(originalRel).orNull ?: original
        }
        .map { project.file(it) }
        .map { FileNameTransformer.encryptedFileFromFile(it) }
        .let { project.files(it) }

internal fun FileEncryptExtension.secretKey(project: Project): Provider<SecretKey> {
    val password: SecretKey? by lazy {
        val providers = listOf(
            DelegatePasswordProvider(this),
            EnvironmentPasswordProvider(project.providers),
            PropertiesPasswordProvider(project.providers, project.layout),
        )

        var password: CharArray? = null
        for (provider in providers) {
            password = provider.password
            if (password != null) break
        }

        password?.let {
            val key = generateKey(password)
            password.fill(' ')
            key
        }
    }
    // Use lazy delegate to store contents of provider between provider.get invocations
    return project
        .providers
        .provider { password }
        .forUseAtConfigurationTime() as Provider<SecretKey>
}
