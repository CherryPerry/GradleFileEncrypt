package com.cherryperry.gfe.base

import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PlainFilesAwareDelegate : ReadOnlyProperty<BaseTask, Iterable<File>> {

    override fun getValue(thisRef: BaseTask, property: KProperty<*>): Iterable<File> =
        thisRef.project.files(thisRef.fileEncryptPluginExtension.files)
}
