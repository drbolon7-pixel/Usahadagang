package com.example.data.repository

import com.example.data.local.ProductDao
import com.example.data.local.TransactionDao
import com.example.data.model.Product
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AccountingRepository(
    private val productDao: ProductDao,
    private val transactionDao: TransactionDao
) {
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = productDao.getLowStockProducts()
    val allTransactions: Flow<List<TransactionItem>> = transactionDao.getAllTransactions()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionItem>> =
        transactionDao.getTransactionsBetween(start, end)

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    /**
     * Mencatat transaksi Penjualan Barang Dagang.
     * Secara otomatis memotong stok barang dan mencatat HPP berdasarkan modal produk.
     */
    suspend fun recordSale(
        product: Product,
        quantity: Int,
        sellingPricePerUnit: Double,
        customerName: String,
        paymentMethod: String,
        notes: String? = null,
        amountReceived: Double = 0.0,
        changeAmount: Double = 0.0,
        isPaid: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val totalRevenue = quantity * sellingPricePerUnit
        val totalCost = quantity * product.costPrice
        val invoiceCode = generateInvoice("PJ")

        val transaction = TransactionItem(
            invoiceNumber = invoiceCode,
            type = TransactionType.PENJUALAN.name,
            date = System.currentTimeMillis(),
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = sellingPricePerUnit,
            totalAmount = totalRevenue,
            totalCost = totalCost,
            category = product.category,
            partyName = customerName.ifBlank { "Pelanggan Umum" },
            paymentMethod = paymentMethod,
            notes = notes,
            amountReceived = if (amountReceived > 0) amountReceived else totalRevenue,
            changeAmount = changeAmount,
            isPaid = isPaid
        )

        val transId = transactionDao.insertTransaction(transaction)
        // Kurangi stok barang dagang secara otomatis
        productDao.adjustStock(product.id, -quantity)
        transId
    }

    suspend fun updatePaymentStatus(id: Long, isPaid: Boolean) = withContext(Dispatchers.IO) {
        transactionDao.updatePaymentStatus(id, isPaid)
    }

    /**
     * Mencatat transaksi Pembelian Stok (Kulakan / Restock).
     * Secara otomatis menambah stok barang dagang dan memperbarui harga modal jika diset.
     */
    suspend fun recordPurchase(
        product: Product,
        quantity: Int,
        purchasePricePerUnit: Double,
        supplierName: String,
        paymentMethod: String,
        notes: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val totalExpense = quantity * purchasePricePerUnit
        val invoiceCode = generateInvoice("PB")

        val transaction = TransactionItem(
            invoiceNumber = invoiceCode,
            type = TransactionType.PEMBELIAN.name,
            date = System.currentTimeMillis(),
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = purchasePricePerUnit,
            totalAmount = totalExpense,
            totalCost = totalExpense,
            category = product.category,
            partyName = supplierName.ifBlank { "Pemasok / Distributor" },
            paymentMethod = paymentMethod,
            notes = notes
        )

        val transId = transactionDao.insertTransaction(transaction)
        // Tambah stok barang dan perbarui harga beli dasar jika ada perubahan
        productDao.addStockWithNewCostPrice(
            id = product.id,
            addedStock = quantity,
            newCostPrice = purchasePricePerUnit
        )
        transId
    }

    /**
     * Mencatat Beban Operasional Usaha Dagang (Gaji, Listrik, Sewa, Kemasan, dll.)
     */
    suspend fun recordExpense(
        category: String,
        amount: Double,
        paymentMethod: String,
        notes: String?
    ): Long = withContext(Dispatchers.IO) {
        val invoiceCode = generateInvoice("BO")

        val transaction = TransactionItem(
            invoiceNumber = invoiceCode,
            type = TransactionType.BEBAN_OPERASIONAL.name,
            date = System.currentTimeMillis(),
            productId = null,
            productName = null,
            quantity = 1,
            unitPrice = amount,
            totalAmount = amount,
            totalCost = 0.0,
            category = category,
            partyName = "Operasional Toko",
            paymentMethod = paymentMethod,
            notes = notes
        )

        transactionDao.insertTransaction(transaction)
    }

    /**
     * Mencatat Penyesuaian Stok (Stock Opname)
     */
    suspend fun recordStockAdjustment(
        product: Product,
        newPhysicalStock: Int,
        reason: String
    ): Long = withContext(Dispatchers.IO) {
        val diff = newPhysicalStock - product.currentStock
        val invoiceCode = generateInvoice("OP")

        val transaction = TransactionItem(
            invoiceNumber = invoiceCode,
            type = TransactionType.PENYESUAIAN_STOK.name,
            date = System.currentTimeMillis(),
            productId = product.id,
            productName = product.name,
            quantity = kotlin.math.abs(diff),
            unitPrice = product.costPrice,
            totalAmount = kotlin.math.abs(diff) * product.costPrice,
            totalCost = 0.0,
            category = product.category,
            partyName = "Stock Opname",
            paymentMethod = "-",
            notes = "$reason (Stok diubah dari ${product.currentStock} menjadi $newPhysicalStock)"
        )

        productDao.setStock(product.id, newPhysicalStock)
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionItem) = withContext(Dispatchers.IO) {
        // Rollback stock effect if applicable
        if (transaction.productId != null) {
            when (transaction.type) {
                TransactionType.PENJUALAN.name -> {
                    // Rollback sale: return stock
                    productDao.adjustStock(transaction.productId, transaction.quantity)
                }
                TransactionType.PEMBELIAN.name -> {
                    // Rollback purchase: reduce stock
                    productDao.adjustStock(transaction.productId, -transaction.quantity)
                }
            }
        }
        transactionDao.deleteTransaction(transaction)
    }

    private fun generateInvoice(prefix: String): String {
        val dateStr = SimpleDateFormat("yyyyMMdd-HHmm", Locale.getDefault()).format(Date())
        val randomSuffix = (100..999).random()
        return "$prefix-$dateStr-$randomSuffix"
    }

    /**
     * Mengisi data awal realistis untuk Usaha Dagang Sembako & Ritel
     */
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = productDao.getProductCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val oneDayMs = 24 * 60 * 60 * 1000L

            val sampleProducts = listOf(
                Product(
                    sku = "SMB-001",
                    name = "Beras Ramos Setra 5 Kg",
                    category = "Sembako",
                    unit = "Karung",
                    costPrice = 65000.0,
                    sellingPrice = 75000.0,
                    currentStock = 28,
                    minStockAlert = 5,
                    updatedAt = now
                ),
                Product(
                    sku = "SMB-002",
                    name = "Minyak Goreng Bimoli 2 Liter",
                    category = "Minyak & Mentega",
                    unit = "Pouch",
                    costPrice = 33000.0,
                    sellingPrice = 38500.0,
                    currentStock = 18,
                    minStockAlert = 6,
                    updatedAt = now
                ),
                Product(
                    sku = "SMB-003",
                    name = "Gula Pasir Gulaku 1 Kg",
                    category = "Sembako",
                    unit = "Bungkus",
                    costPrice = 15000.0,
                    sellingPrice = 18000.0,
                    currentStock = 35,
                    minStockAlert = 10,
                    updatedAt = now
                ),
                Product(
                    sku = "MNM-001",
                    name = "Kopi Kapal Api Spesial 165g",
                    category = "Minuman",
                    unit = "Pcs",
                    costPrice = 12000.0,
                    sellingPrice = 14500.0,
                    currentStock = 4, // Low stock demo!
                    minStockAlert = 8,
                    updatedAt = now
                ),
                Product(
                    sku = "MNM-002",
                    name = "Teh Celup Sariwangi Isi 25",
                    category = "Minuman",
                    unit = "Kotak",
                    costPrice = 6200.0,
                    sellingPrice = 8000.0,
                    currentStock = 22,
                    minStockAlert = 5,
                    updatedAt = now
                ),
                Product(
                    sku = "SNK-001",
                    name = "Biskuit Khong Guan Mini 650g",
                    category = "Snack & Makanan",
                    unit = "Kaleng",
                    costPrice = 45000.0,
                    sellingPrice = 54000.0,
                    currentStock = 12,
                    minStockAlert = 4,
                    updatedAt = now
                ),
                Product(
                    sku = "SMB-004",
                    name = "Telur Ayam Ras Fresh 1 Kg",
                    category = "Sembako",
                    unit = "Kg",
                    costPrice = 25000.0,
                    sellingPrice = 29000.0,
                    currentStock = 3, // Low stock demo!
                    minStockAlert = 10,
                    updatedAt = now
                ),
                Product(
                    sku = "BMB-001",
                    name = "Bawang Merah Brebes Super",
                    category = "Bumbu Dapur",
                    unit = "Kg",
                    costPrice = 28000.0,
                    sellingPrice = 36000.0,
                    currentStock = 15,
                    minStockAlert = 5,
                    updatedAt = now
                )
            )

            productDao.insertAll(sampleProducts)

            // Buat transaksi demo agar laporan laba rugi otomatis memiliki data visual yang jelas
            val sampleTransactions = listOf(
                // Penjualan hari ini
                TransactionItem(
                    invoiceNumber = "PJ-202609-001",
                    type = TransactionType.PENJUALAN.name,
                    date = now - (2 * 60 * 60 * 1000L),
                    productId = 1L,
                    productName = "Beras Ramos Setra 5 Kg",
                    quantity = 2,
                    unitPrice = 75000.0,
                    totalAmount = 150000.0,
                    totalCost = 130000.0,
                    category = "Sembako",
                    partyName = "Ibu Sri Wahyuni",
                    paymentMethod = "Tunai",
                    notes = "Lunas"
                ),
                TransactionItem(
                    invoiceNumber = "PJ-202609-002",
                    type = TransactionType.PENJUALAN.name,
                    date = now - (4 * 60 * 60 * 1000L),
                    productId = 2L,
                    productName = "Minyak Goreng Bimoli 2 Liter",
                    quantity = 3,
                    unitPrice = 38500.0,
                    totalAmount = 115500.0,
                    totalCost = 99000.0,
                    category = "Minyak & Mentega",
                    partyName = "Warung Bu Joko",
                    paymentMethod = "QRIS",
                    notes = "Pesanan langganan"
                ),
                TransactionItem(
                    invoiceNumber = "PJ-202609-003",
                    type = TransactionType.PENJUALAN.name,
                    date = now - oneDayMs,
                    productId = 3L,
                    productName = "Gula Pasir Gulaku 1 Kg",
                    quantity = 5,
                    unitPrice = 18000.0,
                    totalAmount = 90000.0,
                    totalCost = 75000.0,
                    category = "Sembako",
                    partyName = "Pak Rudi",
                    paymentMethod = "Tunai"
                ),
                TransactionItem(
                    invoiceNumber = "PJ-202609-004",
                    type = TransactionType.PENJUALAN.name,
                    date = now - (2 * oneDayMs),
                    productId = 7L,
                    productName = "Telur Ayam Ras Fresh 1 Kg",
                    quantity = 4,
                    unitPrice = 29000.0,
                    totalAmount = 116000.0,
                    totalCost = 100000.0,
                    category = "Sembako",
                    partyName = "Bu Anita",
                    paymentMethod = "Transfer Bank"
                ),
                // Beban operasional
                TransactionItem(
                    invoiceNumber = "BO-202609-001",
                    type = TransactionType.BEBAN_OPERASIONAL.name,
                    date = now - (3 * oneDayMs),
                    productId = null,
                    productName = null,
                    quantity = 1,
                    unitPrice = 75000.0,
                    totalAmount = 75000.0,
                    totalCost = 0.0,
                    category = "Listrik & Air",
                    partyName = "Operasional Toko",
                    paymentMethod = "Transfer Bank",
                    notes = "Token Listrik PLN Toko"
                ),
                TransactionItem(
                    invoiceNumber = "BO-202609-002",
                    type = TransactionType.BEBAN_OPERASIONAL.name,
                    date = now - (5 * oneDayMs),
                    productId = null,
                    productName = null,
                    quantity = 1,
                    unitPrice = 25000.0,
                    totalAmount = 25000.0,
                    totalCost = 0.0,
                    category = "Kemasan & Plastik",
                    partyName = "Operasional Toko",
                    paymentMethod = "Tunai",
                    notes = "Kantong kresek & lakban"
                )
            )

            transactionDao.insertAll(sampleTransactions)
        }
    }
}
