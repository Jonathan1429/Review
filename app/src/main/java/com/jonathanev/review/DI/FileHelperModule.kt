package com.jonathanev.review.DI

import android.content.Context
import com.jonathanev.review.Data.FileOutputStreamFactory
import com.jonathanev.review.Data.GuiaRepository
import com.jonathanev.review.Data.GuiaRepositoryImpl
import com.jonathanev.review.Data.Model.DataStoreManager
import com.jonathanev.review.Data.XmlSerializerFactory
import com.jonathanev.review.Data.provider.FilePathsProvider
import com.jonathanev.review.Data.provider.GuiaProvider
import com.jonathanev.review.Data.repository.FileHelperImpl
import com.jonathanev.review.Data.repository.FileRepositoryImpl
import com.jonathanev.review.Domain.FileHelper
import com.jonathanev.review.Domain.GetColorRanges
import com.jonathanev.review.Domain.SetCifrarRutaImagenUseCase
import com.jonathanev.review.Domain.SetSubstringPathUseCase
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
