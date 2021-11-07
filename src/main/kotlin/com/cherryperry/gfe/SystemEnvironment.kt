package com.cherryperry.gfe

import org.gradle.api.provider.ProviderFactory
import org.gradle.util.GradleVersion

class SystemEnvironment(
    private val providers: ProviderFactory,
) : Environment {
    override fun get(key: String): String? =
        if (GradleVersion.current() >= GradleVersion.version("6.5")) {
            providers.systemProperty(key).forUseAtConfigurationTime().orNull
        } else if (GradleVersion.current() >= GradleVersion.version("6.1")) {
            providers.systemProperty(key).orNull
        } else {
            System.getenv(key)
        }
}
