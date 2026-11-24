package com.edgareldy.tutorial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edgareldy.tutorial.data.local.dao.CategoryDao
import com.edgareldy.tutorial.data.local.dao.ProductDao
import com.edgareldy.tutorial.data.local.entity.AppMetadataEntity
import com.edgareldy.tutorial.data.local.entity.CategoryEntity
import com.edgareldy.tutorial.data.local.entity.ProductEntity
import com.edgareldy.tutorial.data.local.entity.ProductImageEntity

/**
 * Room database for TutorialShop.
 * Version 2 adds the products, product_images and categories tables introduced
 * in the product-catalog phase. fallbackToDestructiveMigration is enabled here
 * because this is a tutorial app and no production data needs to be preserved
 * during schema evolution between dev builds.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Database(
    entities = [
        AppMetadataEntity::class,
        ProductEntity::class,
        ProductImageEntity::class,
        CategoryEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class TutorialDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
}
