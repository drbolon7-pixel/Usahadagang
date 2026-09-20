package com.example.ui.util

import android.content.Context
import android.content.Intent
import com.example.data.model.IncomeStatementReport
import com.example.data.model.Product
import com.example.data.model.ShopProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType

object ReceiptHelper {

    fun generateReceiptText(
        shopProfile: ShopProfile,
        transaction: TransactionItem
    ): String {
        val line = "--------------------------------"
        val doubleLine = "================================"

        val paymentStatusText = if (transaction.isPaid) "LUNAS" else "TEMPO / BON (BELUM LUNAS)"

        return buildString {
            appendLine(doubleLine)
            appendLine(shopProfile.name.uppercase())
            if (shopProfile.address.isNotBlank()) appendLine(shopProfile.address)
            if (shopProfile.phone.isNotBlank()) appendLine("Telp/WA: ${shopProfile.phone}")
            appendLine(doubleLine)
            appendLine("No. Faktur : ${transaction.invoiceNumber}")
            appendLine("Tanggal    : ${FormatUtils.formatDateTime(transaction.date)}")
            appendLine("Pelanggan  : ${transaction.partyName ?: "Pelanggan Umum"}")
            appendLine("Kasir      : Kasir Toko")
            appendLine(line)
            
            // Detail barang
            val prodName = transaction.productName ?: "Barang Dagang"
            val qty = transaction.quantity
            val unitPrice = FormatUtils.formatRupiah(transaction.unitPrice)
            val subtotal = FormatUtils.formatRupiah(transaction.totalAmount)
            
            appendLine(prodName)
            appendLine("  $qty x $unitPrice = $subtotal")
            
            appendLine(line)
            appendLine("TOTAL BELANJA : $subtotal")
            appendLine("Metode Bayar  : ${transaction.paymentMethod}")
            if (transaction.paymentMethod == "Tunai" && transaction.amountReceived > 0) {
                appendLine("Tunai Diterima: ${FormatUtils.formatRupiah(transaction.amountReceived)}")
                appendLine("Kembalian     : ${FormatUtils.formatRupiah(transaction.changeAmount)}")
            }
            appendLine("Status Bayar  : $paymentStatusText")
            if (!transaction.notes.isNullOrBlank()) {
                appendLine("Catatan       : ${transaction.notes}")
            }
            appendLine(doubleLine)
            appendLine(shopProfile.footerMessage)
            appendLine(doubleLine)
        }
    }

    fun shareText(context: Context, text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        context.startActivity(shareIntent)
    }

    fun generateTransactionsCsv(transactions: List<TransactionItem>): String {
        return buildString {
            appendLine("No Faktur,Tanggal,Jenis Transaksi,Nama Barang/Keterangan,Qty,Harga Satuan,Total Nilai,HPP Modal,Laba Kotor,Metode,Status,Pihak")
            transactions.forEach { t ->
                val typeLabel = when (t.type) {
                    TransactionType.PENJUALAN.name -> "Penjualan"
                    TransactionType.PEMBELIAN.name -> "Pembelian Stok"
                    TransactionType.BEBAN_OPERASIONAL.name -> "Beban Operasional"
                    TransactionType.PENYESUAIAN_STOK.name -> "Penyesuaian Stok"
                    else -> t.type
                }
                val dateStr = FormatUtils.formatDateTime(t.date).replace(",", "")
                val name = (t.productName ?: t.category ?: "-").replace(",", " ")
                val party = (t.partyName ?: "-").replace(",", " ")
                val status = if (t.isPaid) "Lunas" else "Belum Lunas"
                appendLine("${t.invoiceNumber},$dateStr,$typeLabel,\"$name\",${t.quantity},${t.unitPrice.toLong()},${t.totalAmount.toLong()},${t.totalCost.toLong()},${t.grossProfit.toLong()},${t.paymentMethod},$status,\"$party\"")
            }
        }
    }

    fun generateStockCsv(products: List<Product>): String {
        return buildString {
            appendLine("Kode SKU,Nama Barang,Kategori,Satuan,Stok Saat Ini,Harga Beli Dasar (Modal),Harga Jual,Total Nilai Modal,Total Nilai Jual,Potensi Laba")
            products.forEach { p ->
                val name = p.name.replace(",", " ")
                val cat = p.category.replace(",", " ")
                appendLine("${p.sku},\"$name\",$cat,${p.unit},${p.currentStock},${p.costPrice.toLong()},${p.sellingPrice.toLong()},${p.totalCostValue.toLong()},${p.totalSellingValue.toLong()},${(p.totalSellingValue - p.totalCostValue).toLong()}")
            }
        }
    }

    fun generateStockCatalogCsv(products: List<Product>): String = generateStockCsv(products)

    fun generateIncomeStatementCsv(report: IncomeStatementReport, shopProfile: ShopProfile): String {
        return buildString {
            appendLine("LAPORAN LABA RUGI USAHA DAGANG")
            appendLine("Nama Usaha,${shopProfile.name.replace(",", " ")}")
            appendLine("Periode,${report.period.title}")
            appendLine("")
            appendLine("Komponen Keuangan,Nilai (Rp),Keterangan")
            appendLine("Penjualan Barang Dagang (Omset),${report.grossSales.toLong()},${report.totalSalesTransactions} transaksi (${report.totalUnitsSold} unit)")
            appendLine("Harga Pokok Penjualan (HPP),-${report.totalCostOfGoodsSold.toLong()},Modal pokok barang terjual")
            appendLine("LABA KOTOR (GROSS PROFIT),${report.grossProfit.toLong()},Margin: ${FormatUtils.formatPercent(report.grossProfitMarginPercent)}")
            appendLine("")
            appendLine("Beban Operasional Toko:")
            report.operatingExpenses.forEach { exp ->
                appendLine("Beban: ${exp.category},-${exp.totalAmount.toLong()},Operasional")
            }
            appendLine("Total Beban Operasional,-${report.totalOperatingExpenses.toLong()},")
            appendLine("")
            appendLine("LABA BERSIH (NET PROFIT),${report.netProfit.toLong()},Margin Bersih: ${FormatUtils.formatPercent(report.netProfitMarginPercent)}")
            if (report.totalUnpaidReceivables > 0) {
                appendLine("Piutang Dagang Bon (Belum Diterima Kas),${report.totalUnpaidReceivables.toLong()},${report.unpaidTransactionsCount} transaksi bon")
            }
            appendLine("")
            appendLine("ASET PERSEDIAAN")
            appendLine("Total Nilai Modal Stok (Aset Persediaan),${report.totalInventoryCostValue.toLong()},${report.totalInventoryItemsCount} ragam produk")
            appendLine("Potensi Nilai Jual Stok Tersisa,${report.totalInventorySellingValue.toLong()},")
            appendLine("Potensi Laba Stok Tersisa,${(report.totalInventorySellingValue - report.totalInventoryCostValue).toLong()},")
        }
    }
}
