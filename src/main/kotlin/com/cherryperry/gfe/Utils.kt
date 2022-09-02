package com.cherryperry.gfe

import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

const val ITERATIONS = 10_000
const val KEY_LENGTH_BYTES = 16
const val KEY_LENGTH = KEY_LENGTH_BYTES * 8
const val GROUP_NAME = "encryption"
const val PLUGIN_NAME = "com.cherryperry.gradle-file-encrypt"

fun generateKey(password: CharArray): SecretKey {
    val keySpec = PBEKeySpec(password, "salt".toByteArray(Charsets.UTF_8), ITERATIONS, KEY_LENGTH)
    try {
        val secret = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(keySpec)
        return SecretKeySpec(secret.encoded, "AES")
    } finally {
        keySpec.clearPassword()
    }
}

fun generateIv(fileName: String): ByteArray {
    val ivBytes = ByteArray(KEY_LENGTH_BYTES)
    Random(fileName.hashCode()).nextBytes(ivBytes)
    return ivBytes
}

fun createCipher(mode: Int, key: Key, iv: ByteArray): Cipher {
    val parameterSpec = IvParameterSpec(iv)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(mode, key, parameterSpec)
    return cipher
}
