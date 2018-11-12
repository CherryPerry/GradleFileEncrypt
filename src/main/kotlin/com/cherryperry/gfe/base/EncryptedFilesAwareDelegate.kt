package com.cherryperry.gfe.base

import com.cherryperry.gfe.FileNameTransformer
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class EncryptedFilesAwareDelegate<T> : ReadOnlyProperty<T, Iterable<File>>
    where T : BaseTask, T : PlainFilesAware {

    override fun getValue(thisRef: T, property: KProperty<*>): Iterable<File> =
        thisRef.fileEncryptPluginExtension.files
            .map { original ->
                thisRef.fileEncryptPluginExtension.mapping[original]?.let { mapped -> return@map mapped }
                original
            }
            .map { thisRef.project.file(it) }
            .map { FileNameTransformer.encryptedFileFromFile(it) }
}
