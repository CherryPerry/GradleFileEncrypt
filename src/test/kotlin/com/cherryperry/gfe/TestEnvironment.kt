package com.cherryperry.gfe

class TestEnvironment(
    private val map: Map<String, String>
) : Environment {

    override fun get(key: String): String? = map[key]
}
