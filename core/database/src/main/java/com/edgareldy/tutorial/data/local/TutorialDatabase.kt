package com.edgareldy.tutorial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edgareldy.tutorial.data.local.dao.CategoryDao
import com.edgareldy.tutorial.data.local.dao.OrderDao
import com.edgareldy.tutorial.data.local.dao.ProductDao
import com.edgareldy.tutorial.data.local.entity.AppMetadataEntity
import com.edgareldy.tutorial.data.local.entity.CategoryEntity
import com.edgareldy.tutorial.data.local.entity.OrderEntity
import com.edgareldy.tutorial.data.local.entity.OrderItemEntity
import com.edgareldy.tutorial.data.local.entity.ProductEntity
import com.edgareldy.tutorial.data.local.entity.ProductImageEntity

/**
 * Room database for TutorialShop.
 * Version 3 adds the orders and order_items tables introduced in the
 * orders phase. fallbackToDestructiveMigration is kept because this is
 * a tutorial app and dev builds do not need to preserve cached data
 * across schema versions.
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
        CategoryEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class TutorialDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun orderDao(): OrderDao
}
