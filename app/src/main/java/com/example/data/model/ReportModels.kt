package com.example.data.model

enum class ReportPeriod(val title: String) {
    TODAY("Hari Ini"),
    THIS_WEEK("Minggu Ini"),
    THIS_MONTH("Bulan Ini"),
    LAST_MONTH("Bulan Lalu"),
    THIS_YEAR("Tahun Ini"),
    ALL_TIME("Semua Waktu")
}

data class ExpenseBreakdown(
    val category: String,
    val totalAmount: Double,
    val percentageOfExpenses: Double
)

data class TopProductMetric(
    val productId: Long,
    val productName: String,
    val category: String,
    val unitsSold: Int,
    val totalRevenue: Double,
    val totalGrossProfit: Double,
    val profitMarginPercent: Double
)

data class CategorySalesBreakdown(
    val category: String,
    val totalSales: Double,
    val percentageOfSales: Double,
    val itemsSold: Int
)

data class IncomeStatementReport(
    val period: ReportPeriod,
    val startDate: Long,
    val endDate: Long,
    
    // Pendapatan Usaha
    val grossSales: Double,
    val totalSalesTransactions: Int,
    val totalUnitsSold: Int,
    
    // Harga Pokok Penjualan (HPP)
    val totalCostOfGoodsSold: Double,
    
    // Laba Kotor (Gross Profit)
    val grossProfit: Double,
    val grossProfitMarginPercent: Double,
    
    // Beban Operasional
    val operatingExpenses: List<ExpenseBreakdown>,
    val totalOperatingExpenses: Double,
    
    // Laba Bersih (Net Profit / Loss)
    val netProfit: Double,
    val netProfitMarginPercent: Double,
    val isProfitable: Boolean,
    
    // Arus Kas Sederhana dari Operasional
    val totalPurchasesCashOut: Double, // Pembelian stok barang periode ini
    val totalUnpaidReceivables: Double = 0.0, // Piutang belum lunas (Bon/Tempo)
    val unpaidTransactionsCount: Int = 0,
    
    // Analisis Produk & Kategori
    val topSellingProducts: List<TopProductMetric> = emptyList(),
    val mostProfitableProducts: List<TopProductMetric> = emptyList(),
    val categorySales: List<CategorySalesBreakdown> = emptyList(),
    
    // Persediaan Terkini
    val totalInventoryCostValue: Double,
    val totalInventorySellingValue: Double,
    val totalInventoryItemsCount: Int,
    val lowStockItemsCount: Int
)
