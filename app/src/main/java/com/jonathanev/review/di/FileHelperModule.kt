package com.jonathanev.review.di

import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.provider.DirectoryManagerImpl
import com.jonathanev.review.data.provider.PathProviderImpl
import com.jonathanev.review.domain.DirectoryManager
import com.jonathanev.review.domain.repository.NavigationPathRepository
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
    fun provideDirectoryManager(
        navigationPathRepository: NavigationPathRepository,
        filePathsProvider: FilePathsProvider
    ): DirectoryManager = DirectoryManagerImpl(navigationPathRepository, filePathsProvider)

    @Provides
    @Singleton
    fun provideFilesAndPath(
        filePathsProvider: FilePathsProvider,
    ): PathProvider {
        return PathProviderImpl(filePathsProvider)
    }
}
