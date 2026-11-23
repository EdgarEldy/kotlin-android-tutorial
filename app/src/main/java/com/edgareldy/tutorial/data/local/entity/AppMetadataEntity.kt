package com.edgareldy.tutorial.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Minimal Room entity used as a placeholder to satisfy the @Database annotation
 * requirement that entities must be non-empty. Stores simple key-value app metadata
 * (e.g. last sync timestamps). Feature phases will add their own domain entities.
 *
 * Created edgar.muhamyangabo on 11/21/26
 * Author: edgar.muhamyangabo
 * Date: 11/21/26
 * Project: kotlin-android-tutorial
 */
@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)
