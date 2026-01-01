package com.jonathanev.review.di

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.provider.FileHelperImpl
import com.jonathanev.review.data.provider.PathProviderImpl
import com.jonathanev.review.domain.FileHelper
import com.jonathanev.review.domain.repository.PathProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileHelperModule {

    @Provides
    @Singleton
    fun provideFileHelper(): FileHelper = FileHelperImpl()

    @Provides
    @Singleton
    fun provideFilesAndPath(
        filePathsProvider: FilePathsProvider,
    ): PathProvider {
        return PathProviderImpl(filePathsProvider)
    }
}
