package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.local.ShopProfileManager
import com.example.data.model.Product
import com.example.data.model.TransactionItem
import com.example.data.repository.AccountingRepository
import com.example.ui.components.AddOrEditProductDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.RecordExpenseDialog
import com.example.ui.components.RecordPurchaseDialog
import com.example.ui.components.RecordSaleDialog
import com.example.ui.components.ShopProfileDialog
import com.example.ui.components.StockOpnameDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProfitLossScreen
import com.example.ui.screens.StockScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryNavy
import com.example.ui.viewmodel.AccountingViewModel
import com.example.ui.viewmodel.AccountingViewModelFactory

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Ringkasan", Icons.Default.Dashboard, "nav_tab_dashboard"),
    STOCK("Stok Barang", Icons.Default.Inventory, "nav_tab_stock"),
    TRANSACTIONS("Transaksi", Icons.Default.ReceiptLong, "nav_tab_transactions"),
    REPORT("Laba Rugi", Icons.Default.Assessment, "nav_tab_report")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AkuntansiDagangApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AkuntansiDagangApp() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { AccountingRepository(database.productDao(), database.transactionDao()) }
    val shopProfileManager = remember { ShopProfileManager(context) }
    val viewModel: AccountingViewModel = viewModel(factory = AccountingViewModelFactory(repository, shopProfileManager))

    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }

    // Dialog state handlers
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showSaleDialog by remember { mutableStateOf(false) }
    var preSelectedSaleProduct by remember { mutableStateOf<Product?>(null) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var preSelectedPurchaseProduct by remember { mutableStateOf<Product?>(null) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var opnameProduct by remember { mutableStateOf<Product?>(null) }
    var showShopProfileDialog by remember { mutableStateOf(false) }
    var viewingReceiptTransaction by remember { mutableStateOf<TransactionItem?>(null) }

    // Reactive State
    val products by viewModel.products.collectAsStateWithLifecycle()
    val lowStockProducts by viewModel.lowStockProducts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val dashboardStats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val incomeStatement by viewModel.incomeStatement.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val searchQuery by viewModel.productSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val transactionFilter by viewModel.transactionTypeFilter.collectAsStateWithLifecycle()
    val shopProfile by viewModel.shopProfile.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_app_scaffold"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            NavigationTab.DASHBOARD -> shopProfile.name.ifBlank { "Akuntansi Dagang" }
                            NavigationTab.STOCK -> "Persediaan Stok Barang"
                            NavigationTab.TRANSACTIONS -> "Buku Kas & Transaksi"
                            NavigationTab.REPORT -> "Laporan Laba Rugi"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showShopProfileDialog = true },
                        modifier = Modifier.testTag("action_shop_profile")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Profil Usaha & Nota",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == NavigationTab.STOCK && lowStockProducts.isNotEmpty()) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                                            Text("${lowStockProducts.size}")
                                        }
                                    }
                                ) {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            } else {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        },
                        label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryNavy,
                            selectedTextColor = PrimaryNavy,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> {
                    DashboardScreen(
                        stats = dashboardStats,
                        recentTransactions = transactions,
                        lowStockProducts = lowStockProducts,
                        onNavigateToStock = { currentTab = NavigationTab.STOCK },
                        onNavigateToTransactions = { currentTab = NavigationTab.TRANSACTIONS },
                        onNavigateToReport = { currentTab = NavigationTab.REPORT },
                        onQuickSale = {
                            preSelectedSaleProduct = null
                            showSaleDialog = true
                        },
                        onQuickPurchase = {
                            preSelectedPurchaseProduct = null
                            showPurchaseDialog = true
                        },
                        onQuickExpense = { showExpenseDialog = true },
                        onQuickAddProduct = { showAddProductDialog = true }
                    )
                }
                NavigationTab.STOCK -> {
                    StockScreen(
                        products = products,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::setProductSearchQuery,
                        selectedCategory = selectedCategory,
                        onSelectCategory = viewModel::selectCategory,
                        onAddProduct = { showAddProductDialog = true },
                        onEditProduct = { prod -> editingProduct = prod },
                        onDeleteProduct = viewModel::deleteProduct,
                        onQuickSale = { prod ->
                            preSelectedSaleProduct = prod
                            showSaleDialog = true
                        },
                        onQuickPurchase = { prod ->
                            preSelectedPurchaseProduct = prod
                            showPurchaseDialog = true
                        },
                        onStockOpname = { prod -> opnameProduct = prod }
                    )
                }
                NavigationTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        transactions = transactions,
                        selectedTypeFilter = transactionFilter,
                        onSelectTypeFilter = viewModel::setTransactionFilter,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onSettleCredit = viewModel::settleCreditTransaction,
                        onViewReceipt = { tx -> viewingReceiptTransaction = tx },
                        shopProfile = shopProfile
                    )
                }
                NavigationTab.REPORT -> {
                    ProfitLossScreen(
                        report = incomeStatement,
                        selectedPeriod = selectedPeriod,
                        onSelectPeriod = viewModel::selectPeriod,
                        shopProfile = shopProfile
                    )
                }
            }
        }
    }

    // Modal Dialog: Tambah Barang
    if (showAddProductDialog) {
        AddOrEditProductDialog(
            initialProduct = null,
            onDismiss = { showAddProductDialog = false },
            onSave = { newProd ->
                viewModel.addProduct(newProd)
                showAddProductDialog = false
            }
        )
    }

    // Modal Dialog: Edit Barang
    editingProduct?.let { prod ->
        AddOrEditProductDialog(
            initialProduct = prod,
            onDismiss = { editingProduct = null },
            onSave = { updated ->
                viewModel.updateProduct(updated)
                editingProduct = null
            }
        )
    }

    // Modal Dialog: Catat Penjualan (Stok berkurang & laba otomatis)
    if (showSaleDialog) {
        RecordSaleDialog(
            products = products,
            preSelectedProduct = preSelectedSaleProduct,
            onDismiss = { showSaleDialog = false },
            onSubmit = { prod, qty, price, customer, payment, notes, received, change, isPaid ->
                viewModel.recordSale(prod, qty, price, customer, payment, notes, received, change, isPaid)
                showSaleDialog = false
            }
        )
    }

    // Modal Dialog: Catat Pembelian Stok (Kulakan)
    if (showPurchaseDialog) {
        RecordPurchaseDialog(
            products = products,
            preSelectedProduct = preSelectedPurchaseProduct,
            onDismiss = { showPurchaseDialog = false },
            onSubmit = { prod, qty, cost, supplier, payment, notes ->
                viewModel.recordPurchase(prod, qty, cost, supplier, payment, notes)
                showPurchaseDialog = false
            }
        )
    }

    // Modal Dialog: Catat Beban Operasional Toko
    if (showExpenseDialog) {
        RecordExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onSubmit = { category, amount, payment, notes ->
                viewModel.recordExpense(category, amount, payment, notes)
                showExpenseDialog = false
            }
        )
    }

    // Modal Dialog: Penyesuaian Stok (Stock Opname)
    opnameProduct?.let { prod ->
        StockOpnameDialog(
            product = prod,
            onDismiss = { opnameProduct = null },
            onSubmit = { newStock, reason ->
                viewModel.recordStockAdjustment(prod, newStock, reason)
                opnameProduct = null
            }
        )
    }

    // Modal Dialog: Profil Toko
    if (showShopProfileDialog) {
        ShopProfileDialog(
            currentProfile = shopProfile,
            onDismiss = { showShopProfileDialog = false },
            onSave = { updated ->
                viewModel.updateShopProfile(updated)
                showShopProfileDialog = false
            }
        )
    }

    // Modal Dialog: Struk Nota Transaksi
    viewingReceiptTransaction?.let { tx ->
        ReceiptDialog(
            transaction = tx,
            shopProfile = shopProfile,
            onDismiss = { viewingReceiptTransaction = null }
        )
    }
}

