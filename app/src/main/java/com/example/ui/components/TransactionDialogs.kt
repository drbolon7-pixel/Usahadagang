package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryNavy
import com.example.ui.util.FormatUtils

val EXPENSE_CATEGORIES = listOf(
    "Listrik, Air & Internet",
    "Gaji & Upah Karyawan",
    "Sewa Tempat Usaha",
    "Transportasi & BBM",
    "Kemasan & Kantong Plastik",
    "Perawatan & Perbaikan",
    "Konsumsi Toko",
    "Beban Lain-lain"
)

val PAYMENT_METHODS = listOf("Tunai", "Transfer Bank", "QRIS", "Tempo / Kredit")

/**
 * Dialog Tambah / Edit Produk Barang Dagang
 */
@Composable
fun AddOrEditProductDialog(
    initialProduct: Product? = null,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "BRG-${(100..999).random()}") }
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "Sembako") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "Pcs") }
    var costPriceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.costPrice.toInt().toString() else "") }
    var sellingPriceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.sellingPrice.toInt().toString() else "") }
    var stockStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.currentStock.toString() else "10") }
    var minStockStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.minStockAlert.toString() else "5") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialProduct == null) "Tambah Barang Dagang" else "Edit Barang Dagang",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("Kode / SKU Barang") },
                    modifier = Modifier.fillMaxWidth().testTag("product_sku_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang *") },
                    placeholder = { Text("Contoh: Beras Ramos 5kg") },
                    modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        modifier = Modifier.weight(1f).testTag("product_category_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Satuan") },
                        placeholder = { Text("Pcs / Kg / Dus") },
                        modifier = Modifier.weight(1f).testTag("product_unit_input"),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("Harga Modal (HPP) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_cost_price_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sellingPriceStr,
                        onValueChange = { sellingPriceStr = it },
                        label = { Text("Harga Jual *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_selling_price_input"),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stok Fisik") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_stock_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockStr,
                        onValueChange = { minStockStr = it },
                        label = { Text("Min. Alert") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("product_min_stock_input"),
                        singleLine = true
                    )
                }

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cost = costPriceStr.toDoubleOrNull()
                            val sell = sellingPriceStr.toDoubleOrNull()
                            val stock = stockStr.toIntOrNull()
                            val minStock = minStockStr.toIntOrNull() ?: 5

                            if (name.isBlank()) {
                                errorMessage = "Nama barang tidak boleh kosong"
                                return@Button
                            }
                            if (cost == null || cost < 0) {
                                errorMessage = "Harga modal tidak valid"
                                return@Button
                            }
                            if (sell == null || sell < 0) {
                                errorMessage = "Harga jual tidak valid"
                                return@Button
                            }
                            if (stock == null || stock < 0) {
                                errorMessage = "Jumlah stok tidak valid"
                                return@Button
                            }

                            val product = initialProduct?.copy(
                                sku = sku.ifBlank { "BRG-${(100..999).random()}" },
                                name = name.trim(),
                                category = category.ifBlank { "Umum" },
                                unit = unit.ifBlank { "Pcs" },
                                costPrice = cost,
                                sellingPrice = sell,
                                currentStock = stock,
                                minStockAlert = minStock,
                                updatedAt = System.currentTimeMillis()
                            ) ?: Product(
                                sku = sku.ifBlank { "BRG-${(100..999).random()}" },
                                name = name.trim(),
                                category = category.ifBlank { "Umum" },
                                unit = unit.ifBlank { "Pcs" },
                                costPrice = cost,
                                sellingPrice = sell,
                                currentStock = stock,
                                minStockAlert = minStock,
                                updatedAt = System.currentTimeMillis()
                            )
                            onSave(product)
                        },
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Text("Simpan Barang")
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pencatatan Penjualan Cepat (Mengurangi Stok Otomatis & Menghitung Laba Kotor Otomatis)
 */
@Composable
fun RecordSaleDialog(
    products: List<Product>,
    preSelectedProduct: Product? = null,
    onDismiss: () -> Unit,
    onSubmit: (Product, Int, Double, String, String, String?, Double, Double, Boolean) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(preSelectedProduct ?: products.firstOrNull()) }
    var quantity by remember { mutableIntStateOf(1) }
    var pricePerUnit by remember { mutableDoubleStateOf(selectedProduct?.sellingPrice ?: 0.0) }
    var customerName by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Tunai") }
    var cashReceivedStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }

    val totalSale = quantity * pricePerUnit
    val totalCost = quantity * (selectedProduct?.costPrice ?: 0.0)
    val estimatedProfit = totalSale - totalCost

    val cashReceived = cashReceivedStr.toDoubleOrNull() ?: totalSale
    val changeAmount = if (cashReceived >= totalSale) cashReceived - totalSale else 0.0
    val isUnderpaid = selectedPaymentMethod == "Tunai" && cashReceivedStr.isNotBlank() && (cashReceived < totalSale)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kasir & Penjualan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Stok berkurang & laba otomatis tercatat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Pilih Produk
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "Pilih Barang...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Pilih Barang Dagang *") },
                        trailingIcon = {
                            IconButton(onClick = { isProductDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("sale_product_dropdown")
                    )
                    DropdownMenu(
                        expanded = isProductDropdownExpanded,
                        onDismissRequest = { isProductDropdownExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(prod.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Stok: ${prod.currentStock} ${prod.unit} • Jual: ${FormatUtils.formatRupiah(prod.sellingPrice)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProduct = prod
                                    pricePerUnit = prod.sellingPrice
                                    isProductDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                selectedProduct?.let { prod ->
                    Text(
                        text = "Tersedia: ${prod.currentStock} ${prod.unit} | Modal (HPP): ${FormatUtils.formatRupiah(prod.costPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (prod.currentStock <= prod.minStockAlert) LossRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Jumlah & Qty Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Jumlah Qty:", fontWeight = FontWeight.Medium)
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier.testTag("sale_qty_decrement")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Kurang")
                    }
                    Text(
                        text = "$quantity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("sale_qty_display")
                    )
                    IconButton(
                        onClick = { quantity++ },
                        modifier = Modifier.testTag("sale_qty_increment")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah")
                    }
                }

                // Harga Jual per Unit
                OutlinedTextField(
                    value = pricePerUnit.toInt().toString(),
                    onValueChange = { pricePerUnit = it.toDoubleOrNull() ?: 0.0 },
                    label = { Text("Harga Jual per Unit (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("sale_unit_price_input"),
                    singleLine = true
                )

                // Kartu Kalkulasi Laba Kotor Otomatis
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Penjualan (Omset):", style = MaterialTheme.typography.bodyMedium)
                            Text(FormatUtils.formatRupiah(totalSale), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("HPP (Modal Barang):", style = MaterialTheme.typography.bodyMedium)
                            Text(FormatUtils.formatRupiah(totalCost), style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Laba Kotor Transaksi:", fontWeight = FontWeight.SemiBold)
                            Text(
                                FormatUtils.formatRupiah(estimatedProfit),
                                fontWeight = FontWeight.Bold,
                                color = if (estimatedProfit >= 0) EmeraldGreen else LossRed
                            )
                        }
                    }
                }

                // Metode Pembayaran
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Metode Pembayaran:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PAYMENT_METHODS.forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            val label = when (method) {
                                "Tempo / Kredit" -> "Tempo / Bon"
                                else -> method
                            }
                            if (isSelected) {
                                Button(
                                    onClick = { selectedPaymentMethod = method },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedPaymentMethod = method },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Kasir Cepat: Perhitungan Kembalian Tunai
                if (selectedPaymentMethod == "Tunai") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = cashReceivedStr,
                            onValueChange = { cashReceivedStr = it },
                            label = { Text("Uang Tunai Diterima (Rp)") },
                            placeholder = { Text(totalSale.toInt().toString()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("sale_cash_received_input"),
                            singleLine = true
                        )

                        // Quick denomination buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { cashReceivedStr = totalSale.toInt().toString() },
                                modifier = Modifier.weight(1f),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                Text("Uang Pas", fontSize = 11.sp)
                            }
                            listOf(10000, 20000, 50000, 100000).forEach { addNominal ->
                                OutlinedButton(
                                    onClick = {
                                        val cur = cashReceivedStr.toDoubleOrNull() ?: 0.0
                                        cashReceivedStr = (cur + addNominal).toInt().toString()
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                                ) {
                                    Text("+${addNominal / 1000}k", fontSize = 11.sp)
                                }
                            }
                        }

                        // Display Change / Kurang
                        if (cashReceivedStr.isNotBlank()) {
                            if (isUnderpaid) {
                                Text(
                                    text = "Uang Kurang: ${FormatUtils.formatRupiah(totalSale - cashReceived)}",
                                    color = LossRed,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = "Kembalian: ${FormatUtils.formatRupiah(changeAmount)}",
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else if (selectedPaymentMethod.contains("Tempo")) {
                    Text(
                        text = "Status: Transaksi ini akan dicatat sebagai Piutang Bon (Belum Lunas).",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { 
                        Text(if (selectedPaymentMethod.contains("Tempo")) "Nama Pelanggan (Wajib untuk Bon) *" else "Nama Pelanggan (Opsional)") 
                    },
                    placeholder = { Text("Contoh: Ibu Ani") },
                    modifier = Modifier.fillMaxWidth().testTag("sale_customer_input"),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val prod = selectedProduct
                            if (prod == null) {
                                errorMessage = "Silakan pilih barang terlebih dahulu"
                                return@Button
                            }
                            if (quantity <= 0) {
                                errorMessage = "Jumlah harus minimal 1"
                                return@Button
                            }
                            if (prod.currentStock < quantity) {
                                errorMessage = "Stok tidak mencukupi (Tersedia: ${prod.currentStock} ${prod.unit})"
                                return@Button
                            }
                            if (pricePerUnit <= 0) {
                                errorMessage = "Harga jual harus lebih dari 0"
                                return@Button
                            }
                            if (selectedPaymentMethod.contains("Tempo") && customerName.isBlank()) {
                                errorMessage = "Nama pelanggan wajib diisi untuk transaksi Bon / Tempo"
                                return@Button
                            }
                            if (isUnderpaid) {
                                errorMessage = "Uang tunai yang diterima kurang dari total belanja"
                                return@Button
                            }

                            val finalReceived = if (selectedPaymentMethod == "Tunai") {
                                if (cashReceivedStr.isNotBlank()) cashReceived else totalSale
                            } else totalSale

                            val finalChange = if (selectedPaymentMethod == "Tunai") changeAmount else 0.0
                            val isPaid = !selectedPaymentMethod.contains("Tempo")

                            onSubmit(
                                prod,
                                quantity,
                                pricePerUnit,
                                customerName.ifBlank { "Pelanggan Umum" },
                                selectedPaymentMethod,
                                notes.ifBlank { null },
                                finalReceived,
                                finalChange,
                                isPaid
                            )
                        },
                        modifier = Modifier.testTag("submit_sale_button")
                    ) {
                        Text("Simpan Transaksi")
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pencatatan Pembelian Stok / Kulakan (Menambah Stok Barang Dagang Otomatis)
 */
@Composable
fun RecordPurchaseDialog(
    products: List<Product>,
    preSelectedProduct: Product? = null,
    onDismiss: () -> Unit,
    onSubmit: (Product, Int, Double, String, String, String?) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(preSelectedProduct ?: products.firstOrNull()) }
    var quantityStr by remember { mutableStateOf("10") }
    var costPriceStr by remember { mutableStateOf(if (selectedProduct != null) selectedProduct!!.costPrice.toInt().toString() else "") }
    var supplierName by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Tunai") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProductDropdownExpanded by remember { mutableStateOf(false) }

    val qty = quantityStr.toIntOrNull() ?: 0
    val cost = costPriceStr.toDoubleOrNull() ?: 0.0
    val totalExpense = qty * cost

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kulakan / Beli Stok",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Stok fisik barang bertambah otomatis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Pilih Produk
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedProduct?.name ?: "Pilih Barang...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Barang yang Dibeli *") },
                        trailingIcon = {
                            IconButton(onClick = { isProductDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("purchase_product_dropdown")
                    )
                    DropdownMenu(
                        expanded = isProductDropdownExpanded,
                        onDismissRequest = { isProductDropdownExpanded = false }
                    ) {
                        products.forEach { prod ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(prod.name, fontWeight = FontWeight.SemiBold)
                                        Text("Stok sekarang: ${prod.currentStock} ${prod.unit} • Modal lama: ${FormatUtils.formatRupiah(prod.costPrice)}", style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    selectedProduct = prod
                                    costPriceStr = prod.costPrice.toInt().toString()
                                    isProductDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Jumlah Masuk *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("purchase_qty_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("Harga Modal Beli *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("purchase_cost_input"),
                        singleLine = true
                    )
                }

                // Total Pengeluaran Modal
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Biaya Kulakan:", fontWeight = FontWeight.Medium)
                        Text(
                            FormatUtils.formatRupiah(totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("Nama Pemasok / Agen") },
                    placeholder = { Text("Contoh: Agen Sembako Makmur") },
                    modifier = Modifier.fillMaxWidth().testTag("purchase_supplier_input"),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val prod = selectedProduct
                            if (prod == null) {
                                errorMessage = "Silakan pilih barang"
                                return@Button
                            }
                            if (qty <= 0) {
                                errorMessage = "Jumlah harus minimal 1"
                                return@Button
                            }
                            if (cost <= 0) {
                                errorMessage = "Harga beli harus lebih dari 0"
                                return@Button
                            }

                            onSubmit(
                                prod,
                                qty,
                                cost,
                                supplierName.ifBlank { "Pemasok Toko" },
                                selectedPaymentMethod,
                                notes.ifBlank { null }
                            )
                        },
                        modifier = Modifier.testTag("submit_purchase_button")
                    ) {
                        Text("Simpan Kulakan")
                    }
                }
            }
        }
    }
}

/**
 * Dialog Pencatatan Beban Operasional Usaha
 */
@Composable
fun RecordExpenseDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, Double, String, String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(EXPENSE_CATEGORIES.first()) }
    var amountStr by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Tunai") }
    var notes by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Catat Beban Operasional",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Biaya operasional untuk laporan laba rugi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                // Dropdown Kategori Beban
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Beban *") },
                        trailingIcon = {
                            IconButton(onClick = { isCategoryDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("expense_category_dropdown")
                    )
                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false }
                    ) {
                        EXPENSE_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Jumlah Nominal Beban (Rp) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Contoh: 100000") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Keterangan Biaya") },
                    placeholder = { Text("Contoh: Pembayaran listrik bulan September") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_notes_input"),
                    singleLine = true
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                errorMessage = "Nominal biaya tidak valid"
                                return@Button
                            }
                            onSubmit(selectedCategory, amount, selectedPaymentMethod, notes.ifBlank { null })
                        },
                        modifier = Modifier.testTag("submit_expense_button")
                    ) {
                        Text("Simpan Beban")
                    }
                }
            }
        }
    }
}

/**
 * Dialog Penyesuaian Stok (Stock Opname)
 */
@Composable
fun StockOpnameDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit
) {
    var newStockStr by remember { mutableStateOf(product.currentStock.toString()) }
    var reason by remember { mutableStateOf("Penyesuaian Fisik Berkala") }
    val reasons = listOf("Penyesuaian Fisik Berkala", "Barang Rusak / Bocor", "Barang Kadaluwarsa", "Selisih Hitung Kasir", "Retur dari Pelanggan")
    var isReasonDropdownExpanded by remember { mutableStateOf(false) }

    val newStock = newStockStr.toIntOrNull() ?: product.currentStock
    val diff = newStock - product.currentStock

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Penyesuaian Stok (Opname)", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Barang: ${product.name}", fontWeight = FontWeight.SemiBold)
                Text("Stok sistem saat ini: ${product.currentStock} ${product.unit}")

                OutlinedTextField(
                    value = newStockStr,
                    onValueChange = { newStockStr = it },
                    label = { Text("Stok Fisik Sebenarnya") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("opname_stock_input"),
                    singleLine = true
                )

                Text(
                    text = if (diff >= 0) "Selisih: +$diff ${product.unit} (Bertambah)" else "Selisih: $diff ${product.unit} (Berkurang)",
                    fontWeight = FontWeight.Medium,
                    color = if (diff >= 0) EmeraldGreen else LossRed
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Alasan Penyesuaian") },
                        trailingIcon = {
                            IconButton(onClick = { isReasonDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = isReasonDropdownExpanded,
                        onDismissRequest = { isReasonDropdownExpanded = false }
                    ) {
                        reasons.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                reason = r
                                isReasonDropdownExpanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stock = newStockStr.toIntOrNull() ?: product.currentStock
                    onSubmit(stock, reason)
                },
                modifier = Modifier.testTag("submit_opname_button")
            ) {
                Text("Perbarui Stok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
