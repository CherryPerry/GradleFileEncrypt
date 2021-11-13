package com.cherryperry.gfe

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import java.util.concurrent.Callable
import javax.inject.Inject

open class FileEncryptExtension @Inject constructor(
    objectFactory: ObjectFactory,
) {

    /**
     * Files to encrypt.
     */
    val plainFiles: ConfigurableFileCollection =
        objectFactory.fileCollection()

    /**
     * File mapping between plain and encrypted versions of file.
     * Key is plain file and value is encrypted.
     * Encrypted part SHOULD NOT contain `.encrypted` extension.
     *
     * Sample: `[ 'secrets.txt' : 'encrypted/1' ]`
     * will create 'encrypted/1.encrypted' file from `secrets.txt`.
     */
    val mapping: MapProperty<Any, Any> =
        objectFactory.mapProperty(Any::class.java, Any::class.java)

    /**
     * Custom password provider to encrypt and decrypt files.
     * Use it, if you don't like provided methods of setting password.
     */
    val passwordProvider: Property<Callable<CharArray>> =
        objectFactory.property(Callable::class.java) as Property<Callable<CharArray>>
}
