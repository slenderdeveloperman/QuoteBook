package com.quotecards.di

import android.content.Context
import androidx.room.Room
import com.quotecards.data.local.QuoteDao
import com.quotecards.data.local.QuoteDatabase
import com.quotecards.data.preferences.AppPreferences
import com.quotecards.data.repository.QuoteRepository
import com.quotecards.data.repository.QuoteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuoteDatabase(
        @ApplicationContext context: Context
    ): QuoteDatabase {
        return Room.databaseBuilder(
            context,
            QuoteDatabase::class.java,
            QuoteDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideQuoteDao(database: QuoteDatabase): QuoteDao {
        return database.quoteDao()
    }

    @Provides
    @Singleton
    fun provideQuoteRepository(quoteDao: QuoteDao): QuoteRepository {
        return QuoteRepositoryImpl(quoteDao)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(
        @ApplicationContext context: Context
    ): AppPreferences {
        return AppPreferences(context)
    }
}
