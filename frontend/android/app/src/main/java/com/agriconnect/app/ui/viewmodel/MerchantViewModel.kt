package com.agriconnect.app.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.data.api.RetrofitClient
import com.agriconnect.data.api.MerchantDashboardResponse
import com.agriconnect.data.api.BookingCreateRequest
import kotlinx.coroutines.launch

class MerchantViewModel : ViewModel() {
    private val _dashboardData = mutableStateOf<MerchantDashboardResponse?>(null)
    val dashboardData: State<MerchantDashboardResponse?> = _dashboardData

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _bookings = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val bookings: State<List<Map<String, Any>>> = _bookings

    private val _savedProducts = mutableStateOf<List<String>>(emptyList())
    val savedProducts: State<List<String>> = _savedProducts

    fun fetchDashboard(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getBuyerDashboard("Bearer $token", userId)
                if (response.isSuccessful) {
                    _dashboardData.value = response.body()
                } else {
                    _error.value = "Failed to sync with merchant network."
                }
            } catch (e: Exception) {
                _error.value = "Merchant dashboard offline."
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
                val response = RetrofitClient.instance.getBuyerBookings("Bearer $token", userId)
                if (response.isSuccessful) {
                    _bookings.value = (response.body()?.get("bookings") as? List<Map<String, Any>>) ?: emptyList()
                } else {
                    _error.value = "Failed to fetch your reservations."
                }
            } catch (e: Exception) {
                _error.value = "History network failure."
            } finally {
                _loading.value = false
            }
        }
    }

    fun createBooking(token: String, userId: String, productId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = BookingCreateRequest(productId = productId, termsAccepted = true)
                val response = RetrofitClient.instance.createBooking("Bearer $token", userId, request)
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleSaveProduct(token: String, userId: String, productId: String) {
        viewModelScope.launch {
            try {
                if (_savedProducts.value.contains(productId)) {
                    RetrofitClient.instance.removeSavedProduct("Bearer $token", productId, userId)
                    _savedProducts.value = _savedProducts.value.filter { it != productId }
                } else {
                    RetrofitClient.instance.saveProduct("Bearer $token", productId, userId)
                    _savedProducts.value = _savedProducts.value + productId
                }
            } catch (e: Exception) {
                // fail silently for fav toggle
            }
        }
    }
}
