package com.coderknock.codegen.tool.bundle

import org.jetbrains.annotations.PropertyKey

const val BUNDLE = "messages.TranslationBundle"

object TranslationBundle : TranslationDynamicBundle(BUNDLE)

fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return TranslationBundle.getMessage(key, *params)
}

fun adaptedMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
    return TranslationBundle.getAdaptedMessage(key, *params).replace("\\n", System.lineSeparator())
}