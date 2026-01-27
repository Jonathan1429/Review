package com.jonathanev.review.domain.repository

interface JsonStorage {
    fun <T> read(path: String, deserializer: kotlinx.serialization.DeserializationStrategy<T>): T
    fun <T> write(path: String, serializer: kotlinx.serialization.SerializationStrategy<T>, data: T)
}