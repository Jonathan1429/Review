package com.jonathanev.review.DI

import android.content.Context
import com.jonathanev.review.data.Model.DataStoreManager
import com.jonathanev.review.data.provider.FilePathsProvider
import com.jonathanev.review.data.repository.FileHelperImpl
import com.jonathanev.review.data.repository.FileRepositoryImpl
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

    /*@Provides
    @Singleton
    fun provideGuides(
        getColorRanges: GetColorRanges,
        setCifrarRutaImagenUseCase: SetCifrarRutaImagenUseCase,
        setSubstringPathUseCase: SetSubstringPathUseCase,
        guiaProvider: GuiaProvider,
        xmlSerializerFactory: XmlSerializerFactory,
        fileOutputStreamFactory: FileOutputStreamFactory
    ): GuiaRepository {
        return GuiaRepositoryImpl(
            getColorRanges,
            setCifrarRutaImagenUseCase,
            setSubstringPathUseCase,
            guiaProvider,
            xmlSerializerFactory,
            fileOutputStreamFactory
        )
    }*/
}
