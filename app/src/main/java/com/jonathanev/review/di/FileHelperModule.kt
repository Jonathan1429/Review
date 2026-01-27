package com.jonathanev.review.di

import com.jonathanev.review.data.filesystem.DirectoryManagerImpl
import com.jonathanev.review.domain.provider.FilePathsProvider
import com.jonathanev.review.domain.repository.DirectoryManager
import com.jonathanev.review.domain.repository.NavigationPathRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FileHelperModule {

    @Binds
    @Singleton
    abstract fun provideDirectoryManager(
        impl: DirectoryManagerImpl
    ): DirectoryManager
}
