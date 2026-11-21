package com.edgareldy.tutorial.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edgareldy.tutorial.data.local.entity.AppMetadataEntity

/**
 * Room database for TutorialShop. Starts with AppMetadataEntity as a minimal
 * placeholder; each feature phase adds its own entity classes and increments
 * the version if a migration is needed.
 * exportSchema = true ensures the schema JSON is tracked in version control.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Database(
    entities = [AppMetadataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class TutorialDatabase : RoomDatabase()
