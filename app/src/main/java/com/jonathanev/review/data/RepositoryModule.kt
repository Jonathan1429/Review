package com.jonathanev.review.data

import com.jonathanev.review.data.repository.FolderRepositoryImp
import com.jonathanev.review.data.repository.ImagesRepositoryImpl
import com.jonathanev.review.data.repository.MetadataRepositoryImpl
import com.jonathanev.review.data.repository.NavigationPathRepositoryImpl
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.ImagesRepository
import com.jonathanev.review.domain.repository.MetadataRepository
import com.jonathanev.review.domain.repository.NavigationPathRepository
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

    @Binds
    abstract fun bindNavigationRepository(
        impl: NavigationPathRepositoryImpl
    ): NavigationPathRepository

    @Binds
    abstract fun bindImagesRepository(
        impl: ImagesRepositoryImpl
    ): ImagesRepository

    @Binds
    abstract fun bindGuiaRepository(
        impl: GuiaRepositoryImpl
    ): GuiaRepository

    @Binds
    abstract fun bindMetadataRepository(
        impl: MetadataRepositoryImpl
    ): MetadataRepository
}