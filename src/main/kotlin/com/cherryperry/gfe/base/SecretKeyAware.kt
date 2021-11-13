package com.cherryperry.gfe.base

import org.gradle.api.provider.Provider
import javax.crypto.SecretKey

interface SecretKeyAware {
    val key: Provider<SecretKey>
}
