package com.jonathanev.review.Data

import com.jonathanev.review.Data.repository.FolderRepository
import com.jonathanev.review.Data.repository.FolderRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindFolderRepository(
        impl: FolderRepositoryImp
    ): FolderRepository
}