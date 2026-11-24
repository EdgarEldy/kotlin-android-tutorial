package com.edgareldy.tutorial.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relation that joins a product row with all of its image rows.
 * Annotated with @Embedded and @Relation so Room generates the multi-query
 * fetch automatically - no manual JOIN is required in the DAO.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class ProductWithImages(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val images: List<ProductImageEntity>
)
