package com.example.musicplayer.di

import android.content.Context
import com.example.musicplayer.data.TrackApiService
import com.example.musicplayer.data.TrackLocalDataSource
import com.example.musicplayer.data.TrackRepositoryImpl
import com.example.musicplayer.domain.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackApiService(retrofit: Retrofit): TrackApiService {
        return retrofit.create(TrackApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTrackLocalDataSource(
        @ApplicationContext context: Context
    ): TrackLocalDataSource {
        return TrackLocalDataSource(context)
    }

    @Provides
    @Singleton
    fun provideTrackRepository(
        apiService: TrackApiService,
        localDataSource: TrackLocalDataSource
    ): TrackRepository {
        return TrackRepositoryImpl(apiService, localDataSource)
    }

}