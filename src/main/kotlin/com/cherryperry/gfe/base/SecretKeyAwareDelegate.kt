package com.cherryperry.gfe.base

import com.cherryperry.gfe.PasswordReader
import com.cherryperry.gfe.SystemEnvironment
import com.cherryperry.gfe.generateKey
import javax.crypto.SecretKey
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class SecretKeyAwareDelegate(
    thisRef: BaseTask,
) : ReadOnlyProperty<BaseTask, SecretKey?> {

    private var key: SecretKey? = null

    init {
        try {
            val password = PasswordReader.getPassword(
                thisRef.logger,
                thisRef.project,
                SystemEnvironment(thisRef.project.providers),
                thisRef.fileEncryptPluginExtension.passwordProvider,
            )
            key = generateKey(password)
            password.fill(' ')
        } catch (ignored: IllegalStateException) {
        }
    }

    override fun getValue(thisRef: BaseTask, property: KProperty<*>): SecretKey? =
        key

}
