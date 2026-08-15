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

    private val _users = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val users: State<List<Map<String, Any>>> = _users

    private val _userDetail = mutableStateOf<Map<String, Any>?>(null)
    val userDetail: State<Map<String, Any>?> = _userDetail

    private val _notifications = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val notifications: State<List<Map<String, Any>>> = _notifications

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchDashboard(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
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

    fun fetchUsers(token: String, role: String? = null) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getAdminUsers("Bearer $token", role = role)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body["success"] == true) {
                        _users.value = (body["users"] as? List<Map<String, Any>>) ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch user directory."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchUserDetail(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getAdminUserDetail("Bearer $token", userId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body["success"] == true) {
                        _userDetail.value = body["user"] as? Map<String, Any>
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch user details."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchNotifications(token: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getAdminNotifications("Bearer $token")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body["success"] == true) {
                        _notifications.value = (body["notifications"] as? List<Map<String, Any>>) ?: emptyList()
                    }
                }
            } catch (e: Exception) {
                // error handling
            } finally {
                _loading.value = false
            }
        }
    }

    fun approveUser(token: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.approveUser("Bearer $token", userId)
                if (response.isSuccessful) {
                    onSuccess()
                    fetchUserDetail(token, userId)
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun rejectUser(token: String, userId: String, reason: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.rejectUser("Bearer $token", userId, reason)
                if (response.isSuccessful) {
                    onSuccess()
                    fetchUserDetail(token, userId)
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _loading.value = false
            }
        }
    }
}
