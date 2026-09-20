package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE currentStock <= minStockAlert ORDER BY currentStock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET currentStock = currentStock + :delta, updatedAt = :updatedAt WHERE id = :id")
    suspend fun adjustStock(id: Long, delta: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET currentStock = :newStock, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setStock(id: Long, newStock: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE products SET currentStock = currentStock + :addedStock, costPrice = :newCostPrice, updatedAt = :updatedAt WHERE id = :id")
    suspend fun addStockWithNewCostPrice(id: Long, addedStock: Int, newCostPrice: Double, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
}
