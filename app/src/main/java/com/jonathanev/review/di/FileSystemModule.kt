package com.jonathanev.review.di

import com.jonathanev.review.data.filesystem.FilePathsProviderImpl
import com.jonathanev.review.domain.provider.FilePathsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileSystemModule {
    @Binds
    @Singleton
    abstract fun bindFilePathsProvider(
        impl: FilePathsProviderImpl
    ): FilePathsProvider
}