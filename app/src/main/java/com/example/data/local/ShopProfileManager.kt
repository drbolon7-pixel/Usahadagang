package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ShopProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShopProfileManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shop_profile_prefs", Context.MODE_PRIVATE)

    private val _shopProfile = MutableStateFlow(loadProfile())
    val shopProfile: StateFlow<ShopProfile> = _shopProfile.asStateFlow()

    private fun loadProfile(): ShopProfile {
        return ShopProfile(
            name = prefs.getString("shop_name", "Toko Usaha Dagang") ?: "Toko Usaha Dagang",
            address = prefs.getString("shop_address", "Jl. Pasar Baru No. 12") ?: "Jl. Pasar Baru No. 12",
            phone = prefs.getString("shop_phone", "0812-3456-7890") ?: "0812-3456-7890",
            footerMessage = prefs.getString("shop_footer", "Terima kasih atas kunjungan Anda! Semoga berkah.") 
                ?: "Terima kasih atas kunjungan Anda! Semoga berkah."
        )
    }

    fun saveProfile(profile: ShopProfile) {
        prefs.edit()
            .putString("shop_name", profile.name.ifBlank { "Toko Usaha Dagang" })
            .putString("shop_address", profile.address)
            .putString("shop_phone", profile.phone)
            .putString("shop_footer", profile.footerMessage)
            .apply()
        _shopProfile.value = profile
    }
}
