package com.edgareldy.tutorial.core.di

import android.content.Context
import androidx.room.Room
import com.edgareldy.tutorial.data.local.TutorialDatabase
import com.edgareldy.tutorial.data.local.dao.CategoryDao
import com.edgareldy.tutorial.data.local.dao.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database as a singleton.
 * Each DAO is also provided as a singleton so the dependency graph can inject
 * them directly into repositories without holding a reference to the database.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
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
        )
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    @Singleton
    fun provideProductDao(database: TutorialDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: TutorialDatabase): CategoryDao = database.categoryDao()
}
