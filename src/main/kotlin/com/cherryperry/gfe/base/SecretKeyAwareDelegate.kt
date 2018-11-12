package com.cherryperry.gfe.base

import com.cherryperry.gfe.Environment
import com.cherryperry.gfe.PasswordReader
import com.cherryperry.gfe.SystemEnvironment
import com.cherryperry.gfe.generateKey
import javax.crypto.SecretKey
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class SecretKeyAwareDelegate(
    private val environment: Environment = SystemEnvironment
) : ReadOnlyProperty<BaseTask, SecretKey> {

    override fun getValue(thisRef: BaseTask, property: KProperty<*>): SecretKey {
        val password = PasswordReader.getPassword(thisRef.logger, thisRef.project, environment,
            thisRef.fileEncryptPluginExtension.passwordProvider)
        val key = generateKey(password)
        password.fill(' ')
        return key
    }
}
