package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.ShopProfileManager
import com.example.data.model.CategorySalesBreakdown
import com.example.data.model.ExpenseBreakdown
import com.example.data.model.IncomeStatementReport
import com.example.data.model.Product
import com.example.data.model.ReportPeriod
import com.example.data.model.ShopProfile
import com.example.data.model.TopProductMetric
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.data.repository.AccountingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardStats(
    val todaySales: Double = 0.0,
    val monthSales: Double = 0.0,
    val monthNetProfit: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val totalInventoryCostValue: Double = 0.0,
    val totalProductsCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalUnpaidBon: Double = 0.0
)

class AccountingViewModel(
    private val repository: AccountingRepository,
    private val shopProfileManager: ShopProfileManager
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val shopProfile: StateFlow<ShopProfile> = shopProfileManager.shopProfile

    fun updateShopProfile(profile: ShopProfile) {
        shopProfileManager.saveProfile(profile)
    }

    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionItem>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPeriod = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<ReportPeriod> = _selectedPeriod.asStateFlow()

    private val _productSearchQuery = MutableStateFlow("")
    val productSearchQuery: StateFlow<String> = _productSearchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _transactionTypeFilter = MutableStateFlow<String?>("SEMUA")
    val transactionTypeFilter: StateFlow<String?> = _transactionTypeFilter.asStateFlow()

    // Dashboard metrics derived reactively
    val dashboardStats: StateFlow<DashboardStats> = combine(
        products,
        transactions
    ) { prodList, transList ->
        val now = System.currentTimeMillis()
        val (todayStart, todayEnd) = getPeriodRange(ReportPeriod.TODAY, now)
        val (monthStart, monthEnd) = getPeriodRange(ReportPeriod.THIS_MONTH, now)

        val todaySales = transList
            .filter { it.date in todayStart..todayEnd && it.type == TransactionType.PENJUALAN.name }
            .sumOf { it.totalAmount }

        val monthSalesTrans = transList
            .filter { it.date in monthStart..monthEnd && it.type == TransactionType.PENJUALAN.name }
        val monthSales = monthSalesTrans.sumOf { it.totalAmount }
        val monthHpp = monthSalesTrans.sumOf { it.totalCost }
        val monthGrossProfit = monthSales - monthHpp

        val monthExpenses = transList
            .filter { it.date in monthStart..monthEnd && it.type == TransactionType.BEBAN_OPERASIONAL.name }
            .sumOf { it.totalAmount }

        val monthNetProfit = monthGrossProfit - monthExpenses

        val totalInvValue = prodList.sumOf { it.costPrice * it.currentStock }
        val lowCount = prodList.count { it.currentStock <= it.minStockAlert }

        val unpaidBon = transList
            .filter { it.type == TransactionType.PENJUALAN.name && !it.isPaid }
            .sumOf { it.totalAmount }

        DashboardStats(
            todaySales = todaySales,
            monthSales = monthSales,
            monthNetProfit = monthNetProfit,
            monthExpenses = monthExpenses,
            totalInventoryCostValue = totalInvValue,
            totalProductsCount = prodList.size,
            lowStockCount = lowCount,
            totalUnpaidBon = unpaidBon
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // Otomatis menghitung Laporan Laba Rugi berdasarkan filter periode
    val incomeStatement: StateFlow<IncomeStatementReport> = combine(
        transactions,
        products,
        _selectedPeriod
    ) { transList, prodList, period ->
        calculateIncomeStatement(transList, prodList, period)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        calculateIncomeStatement(emptyList(), emptyList(), ReportPeriod.THIS_MONTH)
    )

    fun selectPeriod(period: ReportPeriod) {
        _selectedPeriod.value = period
    }

    fun setProductSearchQuery(query: String) {
        _productSearchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setTransactionFilter(filter: String?) {
        _transactionTypeFilter.value = filter
    }

    fun recordSale(
        product: Product,
        quantity: Int,
        pricePerUnit: Double,
        customerName: String,
        paymentMethod: String,
        notes: String?,
        amountReceived: Double = 0.0,
        changeAmount: Double = 0.0,
        isPaid: Boolean = true
    ) {
        viewModelScope.launch {
            repository.recordSale(
                product = product,
                quantity = quantity,
                sellingPricePerUnit = pricePerUnit,
                customerName = customerName,
                paymentMethod = paymentMethod,
                notes = notes,
                amountReceived = amountReceived,
                changeAmount = changeAmount,
                isPaid = isPaid
            )
        }
    }

    fun updatePaymentStatus(transactionId: Long, isPaid: Boolean) {
        viewModelScope.launch {
            repository.updatePaymentStatus(transactionId, isPaid)
        }
    }

    fun settleCreditTransaction(transactionId: Long) {
        updatePaymentStatus(transactionId, true)
    }

    fun recordPurchase(
        product: Product,
        quantity: Int,
        costPricePerUnit: Double,
        supplierName: String,
        paymentMethod: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.recordPurchase(
                product = product,
                quantity = quantity,
                purchasePricePerUnit = costPricePerUnit,
                supplierName = supplierName,
                paymentMethod = paymentMethod,
                notes = notes
            )
        }
    }

    fun recordExpense(
        category: String,
        amount: Double,
        paymentMethod: String,
        notes: String?
    ) {
        viewModelScope.launch {
            repository.recordExpense(
                category = category,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes
            )
        }
    }

    fun recordStockAdjustment(
        product: Product,
        newStock: Int,
        reason: String
    ) {
        viewModelScope.launch {
            repository.recordStockAdjustment(
                product = product,
                newPhysicalStock = newStock,
                reason = reason
            )
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun deleteTransaction(transaction: TransactionItem) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    companion object {
        fun getPeriodRange(period: ReportPeriod, nowMs: Long = System.currentTimeMillis()): Pair<Long, Long> {
            val cal = Calendar.getInstance().apply { timeInMillis = nowMs }
            return when (period) {
                ReportPeriod.TODAY -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    Pair(start, cal.timeInMillis)
                }
                ReportPeriod.THIS_WEEK -> {
                    cal.firstDayOfWeek = Calendar.MONDAY
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.add(Calendar.DAY_OF_WEEK, 6)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    Pair(start, cal.timeInMillis)
                }
                ReportPeriod.THIS_MONTH -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    Pair(start, cal.timeInMillis)
                }
                ReportPeriod.LAST_MONTH -> {
                    cal.add(Calendar.MONTH, -1)
                    cal.set(Calendar.DAY_OF_MONTH, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    Pair(start, cal.timeInMillis)
                }
                ReportPeriod.THIS_YEAR -> {
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis
                    cal.set(Calendar.MONTH, Calendar.DECEMBER)
                    cal.set(Calendar.DAY_OF_MONTH, 31)
                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    Pair(start, cal.timeInMillis)
                }
                ReportPeriod.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
            }
        }

        private fun calculateIncomeStatement(
            transactions: List<TransactionItem>,
            products: List<Product>,
            period: ReportPeriod
        ): IncomeStatementReport {
            val (startTime, endTime) = getPeriodRange(period)
            val filteredTrans = transactions.filter { it.date in startTime..endTime }

            // Penjualan
            val salesTrans = filteredTrans.filter { it.type == TransactionType.PENJUALAN.name }
            val grossSales = salesTrans.sumOf { it.totalAmount }
            val totalUnitsSold = salesTrans.sumOf { it.quantity }
            val totalSalesTransactions = salesTrans.size

            // HPP (Harga Pokok Penjualan)
            val totalHpp = salesTrans.sumOf { it.totalCost }

            // Laba Kotor
            val grossProfit = grossSales - totalHpp
            val grossProfitMargin = if (grossSales > 0) (grossProfit / grossSales) * 100 else 0.0

            // Beban Operasional
            val expenseTrans = filteredTrans.filter { it.type == TransactionType.BEBAN_OPERASIONAL.name }
            val totalExpenses = expenseTrans.sumOf { it.totalAmount }

            val expenseGroups = expenseTrans.groupBy { it.category ?: "Beban Lain-lain" }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.totalAmount }
                    val percent = if (totalExpenses > 0) (sum / totalExpenses) * 100 else 0.0
                    ExpenseBreakdown(category = cat, totalAmount = sum, percentageOfExpenses = percent)
                }.sortedByDescending { it.totalAmount }

            // Laba Bersih
            val netProfit = grossProfit - totalExpenses
            val netProfitMargin = if (grossSales > 0) (netProfit / grossSales) * 100 else 0.0

            // Arus Kas Pembelian Stok (Kulakan) periode ini
            val purchaseTrans = filteredTrans.filter { it.type == TransactionType.PEMBELIAN.name }
            val totalPurchases = purchaseTrans.sumOf { it.totalAmount }

            // Persediaan
            val totalInventoryCost = products.sumOf { it.costPrice * it.currentStock }
            val totalInventorySelling = products.sumOf { it.sellingPrice * it.currentStock }
            val lowStockCount = products.count { it.currentStock <= it.minStockAlert }

            // Piutang Bon Belum Lunas
            val unpaidReceivables = filteredTrans
                .filter { it.type == TransactionType.PENJUALAN.name && !it.isPaid }
                .sumOf { it.totalAmount }

            // Analisis Produk Terlaris & Paling Menguntungkan
            val productSalesMap = salesTrans.filter { it.productId != null }
                .groupBy { it.productId!! }
                .map { (prodId, transItems) ->
                    val prodName = transItems.firstOrNull()?.productName ?: "Produk #$prodId"
                    val catName = transItems.firstOrNull()?.category ?: "Umum"
                    val units = transItems.sumOf { it.quantity }
                    val revenue = transItems.sumOf { it.totalAmount }
                    val cost = transItems.sumOf { it.totalCost }
                    val profit = revenue - cost
                    val margin = if (revenue > 0) (profit / revenue) * 100 else 0.0
                    TopProductMetric(
                        productId = prodId,
                        productName = prodName,
                        category = catName,
                        unitsSold = units,
                        totalRevenue = revenue,
                        totalGrossProfit = profit,
                        profitMarginPercent = margin
                    )
                }

            val topSelling = productSalesMap.sortedByDescending { it.unitsSold }.take(5)
            val mostProfitable = productSalesMap.sortedByDescending { it.totalGrossProfit }.take(5)

            // Analisis Penjualan per Kategori Barang
            val categorySales = salesTrans.groupBy { it.category ?: "Lainnya" }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.totalAmount }
                    val percent = if (grossSales > 0) (sum / grossSales) * 100 else 0.0
                    CategorySalesBreakdown(
                        category = cat,
                        totalSales = sum,
                        percentageOfSales = percent,
                        itemsSold = list.sumOf { it.quantity }
                    )
                }.sortedByDescending { it.totalSales }

            return IncomeStatementReport(
                period = period,
                startDate = startTime,
                endDate = endTime,
                grossSales = grossSales,
                totalSalesTransactions = totalSalesTransactions,
                totalUnitsSold = totalUnitsSold,
                totalCostOfGoodsSold = totalHpp,
                grossProfit = grossProfit,
                grossProfitMarginPercent = grossProfitMargin,
                operatingExpenses = expenseGroups,
                totalOperatingExpenses = totalExpenses,
                netProfit = netProfit,
                netProfitMarginPercent = netProfitMargin,
                isProfitable = netProfit >= 0,
                totalPurchasesCashOut = totalPurchases,
                totalUnpaidReceivables = unpaidReceivables,
                topSellingProducts = topSelling,
                mostProfitableProducts = mostProfitable,
                categorySales = categorySales,
                totalInventoryCostValue = totalInventoryCost,
                totalInventorySellingValue = totalInventorySelling,
                totalInventoryItemsCount = products.size,
                lowStockItemsCount = lowStockCount
            )
        }
    }
}

class AccountingViewModelFactory(
    private val repository: AccountingRepository,
    private val shopProfileManager: ShopProfileManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountingViewModel::class.java)) {
            return AccountingViewModel(repository, shopProfileManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
