package com.jonathanev.review.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GuiaRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGuiaRepository(
        impl: GuiaRepositoryImpl
    ): GuiaRepository
}