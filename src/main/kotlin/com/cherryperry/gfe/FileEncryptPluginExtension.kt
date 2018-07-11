package com.cherryperry.gfe

import java.util.concurrent.Callable

open class FileEncryptPluginExtension {

    var files: Array<Any> = emptyArray()
    var mapping: Map<Any, Any> = emptyMap()
    var passwordProvider: Callable<CharArray>? = null
}
