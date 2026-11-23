package com.edgareldy.tutorial.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for product images. Each row belongs to exactly one product via
 * a foreign key on productId. The ON DELETE CASCADE ensures orphan image rows
 * are removed automatically when their parent product is deleted from the cache.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Entity(
    tableName = "product_images",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class ProductImageEntity(
    @PrimaryKey val id: Long,
    val productId: Long,
    val imageUrl: String,
    val altText: String?,
    val isPrimary: Boolean,
    val displayOrder: Int
)
