package com.edgareldy.tutorial.domain.model

/**
 * Domain model for a product image attachment.
 * No Room or Retrofit annotations - this model is framework-free and can be
 * used freely in use cases and ViewModels without any Android dependency.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class ProductImage(
    val id: Long,
    val imageUrl: String,
    val altText: String?,
    val isPrimary: Boolean,
    val displayOrder: Int
)

/**
 * Domain model representing a product in the catalogue.
 * images is a list rather than a nullable field because an empty list is the
 * correct representation when no images are associated.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class Product(
    val id: Long,
    val name: String,
    val unitPrice: Double,
    val description: String?,
    val active: Boolean,
    val categoryId: Long?,
    val categoryName: String?,
    val images: List<ProductImage>
)
