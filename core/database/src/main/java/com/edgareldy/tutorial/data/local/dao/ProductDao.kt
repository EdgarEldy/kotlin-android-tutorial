package com.edgareldy.tutorial.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.edgareldy.tutorial.data.local.entity.ProductEntity
import com.edgareldy.tutorial.data.local.entity.ProductImageEntity
import com.edgareldy.tutorial.data.local.entity.ProductWithImages
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for product and product-image tables.
 * @Transaction on the relation queries prevents inconsistent reads when Room
 * executes the two underlying SELECTs (parent + children) across coroutines.
 *
 * Created edgar.muhamyangabo on 11/23/26
 * Author: edgar.muhamyangabo
 * Date: 11/23/26
 * Project: kotlin-android-tutorial
 */
@Dao
interface ProductDao {

    @Upsert
    suspend fun upsertAll(products: List<ProductEntity>)

    @Upsert
    suspend fun upsertImages(images: List<ProductImageEntity>)

    @Transaction
    @Query("SELECT * FROM products ORDER BY productName ASC")
    fun getProducts(): Flow<List<ProductWithImages>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductWithImages?>

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY productName ASC")
    fun getProductsByCategoryId(categoryId: Long): Flow<List<ProductWithImages>>
}
