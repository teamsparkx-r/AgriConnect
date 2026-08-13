package com.agriconnect.app.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.data.api.RetrofitClient
import kotlinx.coroutines.launch

data class AdminStats(
    val totalFarmers: Int,
    val totalMerchants: Int,
    val activeProducts: Int,
    val totalBookings: Int,
    val totalRevenue: Double,
    val pendingReports: Int
)

data class AdminDashboardData(
    val stats: AdminStats,
    val recentActivity: List<Map<String, Any>>
)

class AdminViewModel : ViewModel() {
    private val _dashboardData = mutableStateOf<AdminDashboardData?>(null)
    val dashboardData: State<AdminDashboardData?> = _dashboardData

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchDashboard(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                // Since I don't have AdminDashboardResponse in AgriConnectApi yet, 
                // I'll need to add it or use a generic Map for now if I want to avoid errors.
                // Actually, it's better to add it to AgriConnectApi.
                val response = RetrofitClient.instance.getAdminDashboard("Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body["success"] == true) {
                        val statsMap = body["stats"] as? Map<String, Any>
                        val stats = AdminStats(
                            totalFarmers = (statsMap?.get("total_farmers") as? Number)?.toInt() ?: 0,
                            totalMerchants = (statsMap?.get("total_merchants") as? Number)?.toInt() ?: 0,
                            activeProducts = (statsMap?.get("active_products") as? Number)?.toInt() ?: 0,
                            totalBookings = (statsMap?.get("total_bookings") as? Number)?.toInt() ?: 0,
                            totalRevenue = (statsMap?.get("total_revenue") as? Number)?.toDouble() ?: 0.0,
                            pendingReports = (statsMap?.get("pending_reports") as? Number)?.toInt() ?: 0
                        )
                        _dashboardData.value = AdminDashboardData(
                            stats = stats,
                            recentActivity = (body["recent_activity"] as? List<Map<String, Any>>) ?: emptyList()
                        )
                    } else {
                        _error.value = "Control center rejected the synchronization request."
                    }
                } else {
                    _error.value = "Failed to synchronize with control center (Error ${response.code()})."
                }
            } catch (e: Exception) {
                _error.value = "Network failure in administration link."
            } finally {
                _loading.value = false
            }
        }
    }
}
