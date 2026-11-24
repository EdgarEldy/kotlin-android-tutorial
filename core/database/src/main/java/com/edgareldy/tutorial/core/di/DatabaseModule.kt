package com.edgareldy.tutorial.core.di

import android.content.Context
import androidx.room.Room
import com.edgareldy.tutorial.data.local.TutorialDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database as a singleton.
 * fallbackToDestructiveMigration is disabled intentionally - explicit
 * migration strategies will be added as the schema evolves.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TutorialDatabase =
        Room.databaseBuilder(
            context,
            TutorialDatabase::class.java,
            "tutorial_db"
        ).build()
}
