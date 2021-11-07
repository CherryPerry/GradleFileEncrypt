package com.cherryperry.gfe.base

import com.cherryperry.gfe.FileNameTransformer
import org.gradle.api.file.FileCollection
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class EncryptedFilesAwareDelegate<T>(
    thisRef: BaseTask,
) : ReadOnlyProperty<T, FileCollection>
    where T : BaseTask, T : PlainFilesAware {

    private val files =
        thisRef.fileEncryptPluginExtension.plainFiles
            .map { original ->
                val originalRel = original.relativeTo(thisRef.project.projectDir).path
                thisRef.fileEncryptPluginExtension.mapping.getting(originalRel).orNull ?: original
            }
            .map { thisRef.project.file(it) }
            .map { FileNameTransformer.encryptedFileFromFile(it) }
            .let { thisRef.project.files(it) }

    override fun getValue(thisRef: T, property: KProperty<*>): FileCollection =
        files

}
