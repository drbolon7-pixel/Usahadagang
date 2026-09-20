package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncomeStatementReport
import com.example.data.model.ReportPeriod
import com.example.data.model.ShopProfile
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.LossRed
import com.example.ui.theme.LossRedLight
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.PrimaryNavyLight
import com.example.ui.util.FormatUtils
import com.example.ui.util.ReceiptHelper
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Category

@Composable
fun ProfitLossScreen(
    report: IncomeStatementReport,
    selectedPeriod: ReportPeriod,
    onSelectPeriod: (ReportPeriod) -> Unit,
    shopProfile: ShopProfile = ShopProfile()
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profit_loss_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filter Pilihan Periode Laporan Laba Rugi
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Periode Laporan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ReportPeriod.values()) { period ->
                        val isSelected = period == selectedPeriod
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPeriod(period) },
                            label = { Text(period.title) },
                            modifier = Modifier.testTag("period_chip_${period.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // Hero Card: Laba Bersih & Status Laba/Rugi
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (report.isProfitable) PrimaryNavy else Color(0xFF7F1D1D)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().testTag("income_statement_hero_card")
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (report.isProfitable) EmeraldGreen else LossRed
                        ) {
                            Text(
                                text = if (report.isProfitable) "SURPLUS - LABA BERSIH" else "DEFISIT - RUGI BERSIH",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${shopProfile.name} • ${selectedPeriod.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }

                    Text(
                        text = FormatUtils.formatRupiah(report.netProfit),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Net Profit Margin: ${FormatUtils.formatPercent(report.netProfitMarginPercent)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "Omset: ${FormatUtils.formatRupiah(report.grossSales)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Dokumen Format Formal Laporan Laba Rugi Usaha Dagang
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("formal_income_statement_card")
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Judul Dokumen
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shopProfile.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "LAPORAN LABA RUGI USAHA DAGANG",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Periode: ${selectedPeriod.title} (Otomatis)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // I. PENDAPATAN PENJUALAN
                    SectionHeader(title = "1. PENDAPATAN USAHA")
                    AccountingLineItem(
                        label = "Penjualan Barang Dagang (Omset)",
                        amount = report.grossSales,
                        isPositive = true,
                        details = "${report.totalSalesTransactions} transaksi • ${report.totalUnitsSold} unit terjual"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // II. HARGA POKOK PENJUALAN (HPP)
                    SectionHeader(title = "2. HARGA POKOK PENJUALAN (HPP)")
                    AccountingLineItem(
                        label = "Harga Pokok Penjualan (Modal Barang Terjual)",
                        amount = report.totalCostOfGoodsSold,
                        isPositive = false,
                        details = "Beban pokok modal atas unit barang yang terjual"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // III. LABA KOTOR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LABA KOTOR (GROSS PROFIT)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Gross Margin: ${FormatUtils.formatPercent(report.grossProfitMarginPercent)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = FormatUtils.formatRupiah(report.grossProfit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (report.grossProfit >= 0) EmeraldGreen else LossRed
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // IV. BEBAN OPERASIONAL
                    SectionHeader(title = "3. BEBAN OPERASIONAL TOKO")
                    if (report.operatingExpenses.isEmpty()) {
                        Text(
                            text = "Tidak ada beban operasional tercatat pada periode ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        report.operatingExpenses.forEach { expense ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "• ${expense.category}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(expense.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = LossRed
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Beban Operasional",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = FormatUtils.formatRupiah(report.totalOperatingExpenses),
                            fontWeight = FontWeight.Bold,
                            color = LossRed
                        )
                    }

                    HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.primary)

                    // V. LABA BERSIH AKHIR
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (report.isProfitable) EmeraldGreenLight else LossRedLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (report.isProfitable) "LABA BERSIH (NET PROFIT)" else "RUGI BERSIH (NET LOSS)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (report.isProfitable) EmeraldGreen else LossRed
                                )
                                Text(
                                    text = "Laba Kotor dikurangi Beban Operasional",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = FormatUtils.formatRupiah(report.netProfit),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (report.isProfitable) EmeraldGreen else LossRed
                            )
                        }
                    }
                }
            }
        }

        // Kartu Piutang Dagang (Bon Belum Lunas) jika ada
        if (report.totalUnpaidReceivables > 0) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF8E1)),
                    modifier = Modifier.fillMaxWidth().testTag("unpaid_receivables_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Piutang Dagang (Bon Belum Lunas)",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "${report.unpaidTransactionsCount} Transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100)
                            )
                        }
                        Text(
                            text = "Total Bon Tertunggak: ${FormatUtils.formatRupiah(report.totalUnpaidReceivables)}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFBF360C)
                        )
                        Text(
                            text = "Catatan: Nilai ini dihitung pada omset akrual namun belum diterima kas fisik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF795548)
                        )
                    }
                }
            }
        }

        // Kartu Analisis: Produk Terlaris & Margin Tertinggi
        if (report.topSellingProducts.isNotEmpty()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("top_products_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF57F17))
                            Text("Top Produk Terlaris (Periode Ini)", fontWeight = FontWeight.Bold)
                        }

                        report.topSellingProducts.forEachIndexed { index, top ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${index + 1}. ${top.productName}",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Terjual: ${top.unitsSold} unit • Omset: ${FormatUtils.formatRupiah(top.totalRevenue)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "+${FormatUtils.formatRupiah(top.totalGrossProfit)}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = EmeraldGreen
                                )
                            }
                            if (index < report.topSellingProducts.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }

        // Kartu Analisis: Penjualan Berdasarkan Kategori
        if (report.categorySales.isNotEmpty()) {
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().testTag("category_breakdown_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = PrimaryNavy)
                            Text("Penjualan per Kategori Barang", fontWeight = FontWeight.Bold)
                        }

                        report.categorySales.forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cat.category, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${cat.itemsSold} unit terjual (${FormatUtils.formatPercent(cat.percentageOfSales)} dari total)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    FormatUtils.formatRupiah(cat.totalSales),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PrimaryNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        // Kartu Valuasi Aset Persediaan Terkini
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("inventory_valuation_report_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = PrimaryNavy)
                        Text("Ringkasan Persediaan Barang Dagang", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Nilai Modal Persediaan (Aset)", style = MaterialTheme.typography.bodyMedium)
                        Text(FormatUtils.formatRupiah(report.totalInventoryCostValue), fontWeight = FontWeight.Bold, color = PrimaryNavy)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimasi Potensi Penjualan Stok", style = MaterialTheme.typography.bodyMedium)
                        Text(FormatUtils.formatRupiah(report.totalInventorySellingValue), fontWeight = FontWeight.Bold, color = EmeraldGreen)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Potensi Laba Stok Tersisa", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            FormatUtils.formatRupiah(report.totalInventorySellingValue - report.totalInventoryCostValue),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Jumlah Varian Barang", style = MaterialTheme.typography.bodyMedium)
                        Text("${report.totalInventoryItemsCount} Produk", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Tombol Aksi: Bagikan WhatsApp & Ekspor CSV
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        shareIncomeStatement(context, report, selectedPeriod, shopProfile)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("share_report_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Bagikan Laporan (WhatsApp / Pesan)",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val csv = ReceiptHelper.generateIncomeStatementCsv(report, shopProfile)
                        ReceiptHelper.shareText(
                            context,
                            csv,
                            "Ekspor CSV Laporan Laba Rugi - ${shopProfile.name}"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("export_csv_report_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ekspor Laporan ke Format CSV",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp
    )
}

@Composable
fun AccountingLineItem(
    label: String,
    amount: Double,
    isPositive: Boolean,
    details: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            details?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = (if (isPositive) "+" else "-") + FormatUtils.formatRupiah(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) EmeraldGreen else LossRed
        )
    }
}

private fun shareIncomeStatement(
    context: Context,
    report: IncomeStatementReport,
    period: ReportPeriod,
    shopProfile: ShopProfile = ShopProfile()
) {
    val sb = StringBuilder()
    sb.appendLine("📊 *LAPORAN LABA RUGI USAHA DAGANG*")
    sb.appendLine("*${shopProfile.name}*")
    if (shopProfile.address.isNotBlank()) sb.appendLine(shopProfile.address)
    sb.appendLine("Periode: ${period.title}")
    sb.appendLine("----------------------------------------")
    sb.appendLine("1. PENDAPATAN USAHA")
    sb.appendLine("   Penjualan Bersih: ${FormatUtils.formatRupiah(report.grossSales)}")
    sb.appendLine("   (${report.totalSalesTransactions} Transaksi, ${report.totalUnitsSold} Unit Terjual)")
    sb.appendLine("")
    sb.appendLine("2. HARGA POKOK PENJUALAN (HPP)")
    sb.appendLine("   HPP / Modal Terjual: -${FormatUtils.formatRupiah(report.totalCostOfGoodsSold)}")
    sb.appendLine("")
    sb.appendLine("3. LABA KOTOR: ${FormatUtils.formatRupiah(report.grossProfit)}")
    sb.appendLine("   Margin Laba Kotor: ${FormatUtils.formatPercent(report.grossProfitMarginPercent)}")
    sb.appendLine("")
    sb.appendLine("4. BEBAN OPERASIONAL")
    if (report.operatingExpenses.isEmpty()) {
        sb.appendLine("   (Tidak ada beban)")
    } else {
        report.operatingExpenses.forEach { exp ->
            sb.appendLine("   • ${exp.category}: ${FormatUtils.formatRupiah(exp.totalAmount)}")
        }
    }
    sb.appendLine("   Total Beban: -${FormatUtils.formatRupiah(report.totalOperatingExpenses)}")
    sb.appendLine("----------------------------------------")
    sb.appendLine(if (report.isProfitable) "✅ *LABA BERSIH: ${FormatUtils.formatRupiah(report.netProfit)}*" else "❌ *RUGI BERSIH: ${FormatUtils.formatRupiah(report.netProfit)}*")
    sb.appendLine("   Margin Laba Bersih: ${FormatUtils.formatPercent(report.netProfitMarginPercent)}")
    sb.appendLine("")
    if (report.totalUnpaidReceivables > 0) {
        sb.appendLine("⚠️ *PIUTANG BON (BELUM LUNAS)*")
        sb.appendLine("   Total Piutang: ${FormatUtils.formatRupiah(report.totalUnpaidReceivables)} (${report.unpaidTransactionsCount} bon)")
        sb.appendLine("")
    }
    sb.appendLine("📦 *INFORMASI PERSEDIAAN STOK*")
    sb.appendLine("   Nilai Modal Stok: ${FormatUtils.formatRupiah(report.totalInventoryCostValue)}")
    sb.appendLine("   Potensi Nilai Jual: ${FormatUtils.formatRupiah(report.totalInventorySellingValue)}")
    sb.appendLine("   Item Menipis: ${report.lowStockItemsCount} barang")
    sb.appendLine("----------------------------------------")
    sb.appendLine("_Dibuat otomatis oleh Aplikasi Akuntansi Dagang_")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Laba Rugi")
    context.startActivity(shareIntent)
}
