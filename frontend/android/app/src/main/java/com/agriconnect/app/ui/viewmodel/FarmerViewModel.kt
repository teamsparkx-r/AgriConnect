package com.agriconnect.app.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.data.api.RetrofitClient
import com.agriconnect.data.api.FarmerDashboardResponse
import kotlinx.coroutines.launch

class FarmerViewModel : ViewModel() {
    private val _dashboardData = mutableStateOf<FarmerDashboardResponse?>(null)
    val dashboardData: State<FarmerDashboardResponse?> = _dashboardData

    private val _bookings = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val bookings: State<List<Map<String, Any>>> = _bookings

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchDashboard(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getFarmerDashboard("Bearer $token", userId)
                if (response.isSuccessful) {
                    _dashboardData.value = response.body()
                } else {
                    _error.value = "Failed to sync with farmer network."
                }
            } catch (e: Exception) {
                _error.value = "Dashboard connection offline."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchBookings(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getFarmerBookings("Bearer $token", userId)
                if (response.isSuccessful) {
                    _bookings.value = response.body()?.bookings ?: emptyList()
                } else {
                    _error.value = "Failed to fetch inquiries."
                }
            } catch (e: Exception) {
                _error.value = "Inquiries registry offline."
            } finally {
                _loading.value = false
            }
        }
    }
}
