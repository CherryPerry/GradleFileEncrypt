package com.cherryperry.gfe

import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

fun generateKey(password: CharArray): SecretKey {
    val keySpec = PBEKeySpec(password, "salt".toByteArray(Charsets.UTF_8), 10_000, 128)
    try {
        val secret = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec)
        return SecretKeySpec(secret.encoded, "AES")
    } finally {
        keySpec.clearPassword()
    }
}

fun generateIv(): ByteArray {
    val ivBytes = ByteArray(16)
    SecureRandom.getInstanceStrong().nextBytes(ivBytes)
    return ivBytes
}

fun createCipher(mode: Int, key: Key, iv: ByteArray): Cipher {
    val parameterSpec = IvParameterSpec(iv)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(mode, key, parameterSpec)
    return cipher
}