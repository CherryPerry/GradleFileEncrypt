package com.cherryperry.gfe

interface Environment {

    operator fun get(key: String): String?
}
