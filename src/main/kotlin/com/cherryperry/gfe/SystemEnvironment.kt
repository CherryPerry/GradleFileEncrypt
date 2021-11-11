package com.cherryperry.gfe

import org.gradle.api.provider.ProviderFactory

class SystemEnvironment(
    private val providers: ProviderFactory,
) : Environment {
    override fun get(key: String): String? =
        providers.systemProperty(key).forUseAtConfigurationTime().orNull
}
