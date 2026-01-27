package com.jonathanev.review.di

import com.jonathanev.review.data.filesystem.GuiaRepositoryImpl
import com.jonathanev.review.data.filesystem.GuideMoveRepositoryImpl
import com.jonathanev.review.data.repository.FolderRepositoryImpl
import com.jonathanev.review.data.repository.ImagesRepositoryImpl
import com.jonathanev.review.data.repository.NavigationPathRepositoryImpl
import com.jonathanev.review.domain.repository.FolderRepository
import com.jonathanev.review.domain.repository.GuiaRepository
import com.jonathanev.review.domain.repository.GuideMoveRepository
import com.jonathanev.review.domain.repository.ImagesRepository
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
        impl: FolderRepositoryImpl
    ): FolderRepository

    @Binds
    abstract fun bindGuideMoveRepository(
        impl: GuideMoveRepositoryImpl
    ): GuideMoveRepository

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
}