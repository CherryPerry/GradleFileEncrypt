package com.cherryperry.gfe

object SystemEnvironment : Environment {

    override fun get(key: String): String? = System.getenv(key)
}
