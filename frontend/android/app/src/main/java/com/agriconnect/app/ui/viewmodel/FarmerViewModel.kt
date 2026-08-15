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

    private val _currentBooking = mutableStateOf<Map<String, Any>?>(null)
    val currentBooking: State<Map<String, Any>?> = _currentBooking

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _notifications = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val notifications: State<List<Map<String, Any>>> = _notifications
    
    val hasUnread = mutableStateOf(false)

    fun fetchDashboard(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getFarmerDashboard("Bearer $token", userId)
                if (response.isSuccessful) {
                    val body = response.body()
                    _dashboardData.value = body
                    // Backend returns unread count in stats
                    hasUnread.value = (body?.stats?.unreadMessages ?: 0) > 0
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

    fun fetchNotifications(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getNotifications("Bearer $token", userId)
                if (response.isSuccessful) {
                    val list = (response.body()?.get("notifications") as? List<Map<String, Any>>) ?: emptyList()
                    _notifications.value = list
                    hasUnread.value = list.any { (it["is_read"] as? Boolean) == false }
                }
            } catch (e: Exception) {
                // error handling
            } finally {
                _loading.value = false
            }
        }
    }

    fun markAsRead(token: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.markFarmerNotificationsRead("Bearer $token", userId)
                if (response.isSuccessful) {
                    hasUnread.value = false
                }
            } catch (e: Exception) {
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

    fun fetchBookingDetail(token: String, bookingId: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getFarmerBookingDetail("Bearer $token", bookingId, userId)
                if (response.isSuccessful) {
                    _currentBooking.value = response.body()
                }
            } catch (e: Exception) {
                // error handling
            } finally {
                _loading.value = false
            }
        }
    }

    fun rejectOffer(token: String, bookingId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.farmerRejectOffer("Bearer $token", bookingId, userId)
                if (response.isSuccessful) {
                    onSuccess()
                    fetchBookingDetail(token, bookingId, userId)
                }
            } catch (e: Exception) {
            } finally {
                _loading.value = false
            }
        }
    }

    fun acceptOffer(token: String, bookingId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.farmerAcceptOffer("Bearer $token", bookingId, userId)
                if (response.isSuccessful) {
                    onSuccess()
                    fetchBookingDetail(token, bookingId, userId)
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun counterOffer(token: String, bookingId: String, userId: String, quantity: Float, price: Float, message: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = com.agriconnect.data.model.CounterOfferRequest(quantity, price, message)
                val response = RetrofitClient.instance.farmerCounterOffer("Bearer $token", bookingId, userId, request)
                if (response.isSuccessful) {
                    onSuccess()
                    fetchBookingDetail(token, bookingId, userId)
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _loading.value = false
            }
        }
    }
}
