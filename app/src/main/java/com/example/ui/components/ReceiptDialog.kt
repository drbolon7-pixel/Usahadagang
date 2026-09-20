package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ShopProfile
import com.example.data.model.TransactionItem
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LossRed
import com.example.ui.theme.PrimaryNavy
import com.example.ui.util.FormatUtils
import com.example.ui.util.ReceiptHelper

@Composable
fun ReceiptDialog(
    transaction: TransactionItem,
    shopProfile: ShopProfile,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = PrimaryNavy
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Struk Nota Digital",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_receipt_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt Simulation Box (Thermal Paper aesthetic)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shopProfile.name.uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF0F172A)
                        )
                        if (shopProfile.address.isNotBlank()) {
                            Text(
                                text = shopProfile.address,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (shopProfile.phone.isNotBlank()) {
                            Text(
                                text = "Telp/WA: ${shopProfile.phone}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Info Row
                        ReceiptMetaRow("No Faktur", transaction.invoiceNumber)
                        ReceiptMetaRow("Waktu", FormatUtils.formatDateTime(transaction.date))
                        ReceiptMetaRow("Pelanggan", transaction.partyName ?: "Pelanggan Umum")
                        ReceiptMetaRow("Metode", transaction.paymentMethod)

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Items Detail
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = transaction.productName ?: "Barang Dagang",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${transaction.quantity} x ${FormatUtils.formatRupiah(transaction.unitPrice)}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = FormatUtils.formatRupiah(transaction.totalAmount),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = Color(0xFFCBD5E1)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Total, Payment & Change
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL BELANJA",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = FormatUtils.formatRupiah(transaction.totalAmount),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = EmeraldGreen
                            )
                        }

                        if (transaction.paymentMethod == "Tunai" && transaction.amountReceived > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            ReceiptMetaRow("Tunai Diterima", FormatUtils.formatRupiah(transaction.amountReceived))
                            ReceiptMetaRow("Kembalian", FormatUtils.formatRupiah(transaction.changeAmount))
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (transaction.isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (transaction.isPaid) "LUNAS" else "TEMPO / BON (BELUM LUNAS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (transaction.isPaid) EmeraldGreen else LossRed
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = shopProfile.footerMessage,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions: Share & Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_receipt_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tutup")
                    }

                    Button(
                        onClick = {
                            val text = ReceiptHelper.generateReceiptText(shopProfile, transaction)
                            ReceiptHelper.shareText(
                                context,
                                text,
                                "Bagikan Struk Faktur ${transaction.invoiceNumber}"
                            )
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("share_whatsapp_receipt_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan WhatsApp", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A))
    }
}
