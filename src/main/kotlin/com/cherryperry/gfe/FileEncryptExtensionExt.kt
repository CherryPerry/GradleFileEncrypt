package com.cherryperry.gfe

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

internal fun FileEncryptExtension.encryptedFiles(project: Project): FileCollection =
    plainFiles
        .map { original ->
            val originalRel = original.relativeTo(project.projectDir).path
            mapping.getting(originalRel).orNull ?: original
        }
        .map { project.file(it) }
        .map { FileNameTransformer.encryptedFileFromFile(it) }
        .let { project.files(it) }
