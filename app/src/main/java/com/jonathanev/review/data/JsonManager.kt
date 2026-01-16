package com.jonathanev.review.data

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class JsonManager @Inject constructor() {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun <T> read(file: File, deserializer: DeserializationStrategy<T>): T {
        return json.decodeFromString(deserializer, file.readText())
    }

    fun <T> write(file: File, serializer: SerializationStrategy<T>, data: T) {
        file.writeText(json.encodeToString(serializer, data))
    }
}
