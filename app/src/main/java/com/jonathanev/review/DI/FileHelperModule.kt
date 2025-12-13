package com.jonathanev.review.DI

import android.content.Context
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.FileHelper
import com.jonathanev.review.Domain.repository.FileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        @ApplicationContext context: Context,
        dataStore: DataStoreManager
        //getAllGuiasUseCase: GetAllGuiasUseCase,
    ): FileRepository {
        return FileRepositoryImpl(filePathsProvider, context, dataStore)
    }
}
