package com.jonathanev.review.Data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class JsonManager @Inject constructor() {

    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    inline fun <reified T> read(file: File): T {
        return json.decodeFromString(file.readText())
    }

    inline fun <reified T> write(file: File, data: T) {
        file.writeText(json.encodeToString(data))
    }
}
