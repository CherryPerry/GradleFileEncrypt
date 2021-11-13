package com.cherryperry.gfe

import com.cherryperry.gfe.providers.DelegatePasswordProvider
import com.cherryperry.gfe.providers.EnvironmentPasswordProvider
import com.cherryperry.gfe.providers.PropertiesPasswordProvider
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
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

internal fun FileEncryptExtension.secretKey(project: Project): SecretKey? {
    val providers = listOf(
        DelegatePasswordProvider(this),
        EnvironmentPasswordProvider(project.providers),
        PropertiesPasswordProvider(project.providers, project.layout),
    )

    var password: CharArray? = null
    providers.forEach {
        password = it.password
        if (password != null) return@forEach
    }

    val pswd = password
    return if (pswd == null) {
        null
    } else {
        val key = generateKey(pswd)
        pswd.fill(' ')
        key
    }
}
