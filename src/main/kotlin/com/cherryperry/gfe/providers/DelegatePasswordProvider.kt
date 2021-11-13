package com.cherryperry.gfe.providers

import com.cherryperry.gfe.FileEncryptExtension

internal class DelegatePasswordProvider(
    private val fileEncryptExtension: FileEncryptExtension,
) : PasswordProvider {
    override val password: CharArray?
        get() = fileEncryptExtension.passwordProvider.forUseAtConfigurationTime().orNull?.call()
}
