package com.jonathanev.review.di

import com.jonathanev.review.data.repository.MetadataRepositoryImpl
import com.jonathanev.review.domain.repository.MetadataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {
    @Binds
    abstract fun bindMetadataRepository(
        impl: MetadataRepositoryImpl
    ): MetadataRepository
}