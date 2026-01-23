package com.jonathanev.review.di

import com.jonathanev.review.data.filesystem.DirectoryManagerImpl
import com.jonathanev.review.data.filesystem.FilePathsProvider
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.NavigationPathRepository
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
        filePathsProvider: FilePathsProvider,
    ): DirectoryManager =
        DirectoryManagerImpl(navigationPathRepository, filePathsProvider)
}
