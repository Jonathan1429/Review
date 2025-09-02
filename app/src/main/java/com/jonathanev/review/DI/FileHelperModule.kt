package com.jonathanev.review.DI

import com.jonathanev.review.Data.Interface.FileHelper
import com.jonathanev.review.Data.Model.FileHelperImpl
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
}
