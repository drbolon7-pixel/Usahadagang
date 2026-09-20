package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShopProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyLight
import com.example.ui.util.FormatUtils
import com.example.ui.util.ReceiptHelper

@Composable
fun TransactionsScreen(
    transactions: List<TransactionItem>,
    selectedTypeFilter: String?,
    onSelectTypeFilter: (String?) -> Unit,
    onDeleteTransaction: (TransactionItem) -> Unit,
    onSettleCredit: (Long) -> Unit = {},
    onViewReceipt: (TransactionItem) -> Unit = {},
    onUpdatePaymentStatus: (Long, Boolean) -> Unit = { id, paid -> onSettleCredit(id) },
    shopProfile: ShopProfile = ShopProfile()
) {
    val context = LocalContext.current
    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToSettle by remember { mutableStateOf<TransactionItem?>(null) }

    val filterOptions = listOf(
        "SEMUA" to null,
        "Penjualan" to TransactionType.PENJUALAN.name,
        "Pembelian Stok" to TransactionType.PEMBELIAN.name,
        "Beban Toko" to TransactionType.BEBAN_OPERASIONAL.name,
        "Bon Belum Lunas" to "UNPAID_BON",
        "Opname Stok" to TransactionType.PENYESUAIAN_STOK.name
    )

    val filteredTransactions = remember(transactions, selectedTypeFilter) {
        when (selectedTypeFilter) {
            null, "SEMUA" -> transactions
            "UNPAID_BON" -> transactions.filter { it.type == TransactionType.PENJUALAN.name && !it.isPaid }
            else -> transactions.filter { it.type == selectedTypeFilter }
        }
    }

    val totalPenjualan = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.PENJUALAN.name }.sumOf { it.totalAmount }
    }

    val totalPengeluaran = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == TransactionType.PEMBELIAN.name || it.type == TransactionType.BEBAN_OPERASIONAL.name }
            .sumOf { it.totalAmount }
    }

    val totalUnpaidBon = remember(transactions) {
        transactions.filter { it.type == TransactionType.PENJUALAN.name && !it.isPaid }.sumOf { it.totalAmount }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("transactions_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary & Export Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("transaction_summary_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Total Penjualan",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                FormatUtils.formatRupiah(totalPenjualan),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Total Pengeluaran",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                FormatUtils.formatRupiah(totalPengeluaran),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = LossRed
                            )
                        }
                    }

                    // Action bar: Export CSV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val csv = ReceiptHelper.generateTransactionsCsv(transactions)
                                ReceiptHelper.shareText(
                                    context,
                                    csv,
                                    "Ekspor Daftar Transaksi Akuntansi Dagang"
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("export_transactions_csv_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor CSV", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Bon alert if any unpaid
        if (totalUnpaidBon > 0 && selectedTypeFilter != "UNPAID_BON") {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTypeFilter("UNPAID_BON") }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Perhatian: Piutang Bon Belum Lunas", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
                            Text("Total: ${FormatUtils.formatRupiah(totalUnpaidBon)}", fontSize = 12.sp, color = Color(0xFFBF360C))
                        }
                        Text("Lihat Bon >", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE65100))
                    }
                }
            }
        }

        // Filter Tipe Transaksi
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterOptions) { (label, value) ->
                    val isSelected = (value == null && (selectedTypeFilter == null || selectedTypeFilter == "SEMUA")) ||
                            (value != null && selectedTypeFilter == value)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectTypeFilter(value ?: "SEMUA") },
                        label = { Text(label) },
                        modifier = Modifier.testTag("filter_trans_${label.lowercase().replace(" ", "_")}")
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
                        Text("Tidak ada transaksi pada filter ini", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { trans ->
                TransactionDetailCard(
                    transaction = trans,
                    onDelete = { transactionToDelete = trans },
                    onViewReceipt = { onViewReceipt(trans) },
                    onSettleBon = { transactionToSettle = trans }
                )
            }
        }
    }

    // Dialog Konfirmasi Lunasi Bon
    transactionToSettle?.let { trans ->
        AlertDialog(
            onDismissRequest = { transactionToSettle = null },
            title = { Text("Tandai Bon Lunas?") },
            text = {
                Text("Pelanggan '${trans.partyName ?: "Umum"}' telah melunasi tagihan sebesar ${FormatUtils.formatRupiah(trans.totalAmount)}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdatePaymentStatus(trans.id, true)
                        transactionToSettle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.testTag("confirm_settle_bon_btn")
                ) {
                    Text("Ya, Sudah Lunas")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToSettle = null }) { Text("Batal") }
            }
        )
    }

    // Dialog Konfirmasi Hapus Transaksi
    transactionToDelete?.let { trans ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = {
                Text("Anda yakin ingin menghapus transaksi '${trans.invoiceNumber}'? Jika transaksi ini mengubah stok barang, perubahan stok akan otomatis dikembalikan (rollback).")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(trans)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LossRed),
                    modifier = Modifier.testTag("confirm_delete_trans_button")
                ) {
                    Text("Hapus & Rollback")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun TransactionDetailCard(
    transaction: TransactionItem,
    onDelete: () -> Unit,
    onViewReceipt: () -> Unit,
    onSettleBon: () -> Unit
) {
    val isSale = transaction.type == TransactionType.PENJUALAN.name
    val isPurchase = transaction.type == TransactionType.PEMBELIAN.name
    val isExpense = transaction.type == TransactionType.BEBAN_OPERASIONAL.name

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().testTag("transaction_item_${transaction.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            isSale -> EmeraldGreenLight
                            isPurchase -> PrimaryNavyLight
                            isExpense -> LossRedLight
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            text = transaction.transactionType.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isSale -> EmeraldGreen
                                isPurchase -> PrimaryNavy
                                isExpense -> LossRed
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (isSale && !transaction.isPaid) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = "BON / BELUM LUNAS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LossRed,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = transaction.invoiceNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.productName ?: transaction.category ?: "Operasional",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (transaction.quantity > 0 && transaction.unitPrice > 0) {
                        Text(
                            text = "${transaction.quantity} x ${FormatUtils.formatRupiah(transaction.unitPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when {
                            isSale -> "+${FormatUtils.formatRupiah(transaction.totalAmount)}"
                            isPurchase -> "-${FormatUtils.formatRupiah(transaction.totalAmount)}"
                            isExpense -> "-${FormatUtils.formatRupiah(transaction.totalAmount)}"
                            else -> FormatUtils.formatRupiah(transaction.totalAmount)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isSale -> EmeraldGreen
                            isPurchase -> PrimaryNavy
                            isExpense -> LossRed
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (isSale && transaction.grossProfit > 0) {
                        Text(
                            text = "Laba: +${FormatUtils.formatRupiah(transaction.grossProfit)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Garis Keterangan, Struk & Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${FormatUtils.formatDateTime(transaction.date)} • ${transaction.partyName ?: "-"} • ${transaction.paymentMethod}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSale && !transaction.isPaid) {
                        TextButton(
                            onClick = onSettleBon,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lunasi", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isSale) {
                        IconButton(onClick = onViewReceipt, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = "Lihat Struk",
                                tint = PrimaryNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Transaksi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            transaction.notes?.let { note ->
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Catatan: $note",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
