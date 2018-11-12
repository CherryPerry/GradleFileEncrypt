package com.cherryperry.gfe.base

import javax.crypto.SecretKey

interface SecretKeyAware {

    val key: SecretKey
}
