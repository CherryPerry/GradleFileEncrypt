package com.cherryperry.gfe.providers

import org.gradle.api.provider.ProviderFactory

internal class SystemPropertyPasswordProvider(
    private val providersFactory: ProviderFactory,
) : PasswordProvider {
    override val password: CharArray?
        get() = providersFactory.systemProperty(SYSTEM_PROPERTY_KEY).forUseAtConfigurationTime().orNull?.toCharArray()

    companion object {
        const val SYSTEM_PROPERTY_KEY = "GFE_PASSWORD"
    }
}
