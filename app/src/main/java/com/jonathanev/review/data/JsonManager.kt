package com.jonathanev.review.data

import com.jonathanev.review.domain.repository.JsonStorage
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class JsonManager @Inject constructor(): JsonStorage {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    override fun <T> read(path: String, deserializer: DeserializationStrategy<T>): T {
        val file = File(path)
        return json.decodeFromString(deserializer, file.readText())
    }

    override fun <T> write(path: String, serializer: SerializationStrategy<T>, data: T) {
        val file = File(path)
        file.writeText(json.encodeToString(serializer, data))
    }
}
