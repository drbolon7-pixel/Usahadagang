package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    PENJUALAN("Penjualan"),
    PEMBELIAN("Pembelian Stok"),
    BEBAN_OPERASIONAL("Beban Operasional"),
    PENYESUAIAN_STOK("Penyesuaian Stok")
}

@Entity(tableName = "transactions")
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val type: String, // Value of TransactionType.name
    val date: Long = System.currentTimeMillis(),
    val productId: Long? = null,
    val productName: String? = null,
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0, // Nilai total transaksi (Omset jika Penjualan, Biaya jika Pembelian/Beban)
    val totalCost: Double = 0.0,   // HPP untuk Penjualan (qty * modal). 0 untuk Beban
    val category: String? = null,  // Kategori beban (e.g. Gaji, Listrik, Sewa) atau kategori barang
    val partyName: String? = null, // Nama Pelanggan / Supplier / Vendor
    val paymentMethod: String = "Tunai",
    val notes: String? = null,
    val amountReceived: Double = 0.0,
    val changeAmount: Double = 0.0,
    val isPaid: Boolean = true
) {
    val grossProfit: Double
        get() = if (type == TransactionType.PENJUALAN.name) (totalAmount - totalCost) else 0.0

    val transactionType: TransactionType
        get() = try {
            TransactionType.valueOf(type)
        } catch (e: Exception) {
            TransactionType.PENJUALAN
        }
}
