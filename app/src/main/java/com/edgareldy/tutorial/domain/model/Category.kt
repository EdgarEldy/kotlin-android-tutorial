package com.edgareldy.tutorial.domain.model

/**
 * Domain model representing a product category.
 * Intentionally minimal - the category list is used only for filter chips in
 * the product list UI; the backend also returns a products list inside
 * CategoryResponse but that data is already owned by the Product model.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
data class Category(
    val id: Long,
    val name: String
)
