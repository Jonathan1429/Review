package com.jonathanev.review.di

import com.jonathanev.review.data.filesystem.GuiaMigrationRepositoryImpl
import com.jonathanev.review.data.repository.GuiaMigrationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MigrationModule {
    @Binds
    abstract fun bindGuiaMigrationRepository(
        impl: GuiaMigrationRepositoryImpl
    ): GuiaMigrationRepository
}