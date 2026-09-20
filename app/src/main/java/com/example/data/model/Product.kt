package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    val name: String,
    val category: String,
    val unit: String = "Pcs",
    val costPrice: Double, // Harga Beli Dasar (Modal / HPP per unit)
    val sellingPrice: Double, // Harga Jual per unit
    val currentStock: Int,
    val minStockAlert: Int = 5,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalCostValue: Double
        get() = costPrice * currentStock

    val totalSellingValue: Double
        get() = sellingPrice * currentStock

    val profitMarginPerUnit: Double
        get() = sellingPrice - costPrice

    val profitMarginPercent: Double
        get() = if (costPrice > 0) ((sellingPrice - costPrice) / costPrice) * 100 else 0.0

    val isLowStock: Boolean
        get() = currentStock <= minStockAlert

    val isOutOfStock: Boolean
        get() = currentStock <= 0
}
