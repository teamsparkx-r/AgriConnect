package com.agriconnect.app.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.data.api.RetrofitClient
import com.agriconnect.data.api.MerchantDashboardResponse
import com.agriconnect.data.api.BookingCreateRequest
import com.agriconnect.data.model.Product
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

    private val _currentBooking = mutableStateOf<Map<String, Any>?>(null)
    val currentBooking: State<Map<String, Any>?> = _currentBooking

    private val _notifications = mutableStateOf<List<Map<String, Any>>>(emptyList())
    val notifications: State<List<Map<String, Any>>> = _notifications

    private val _savedProductsList = mutableStateOf<List<Product>>(emptyList())
    val savedProductsList: State<List<Product>> = _savedProductsList

    val hasUnread = mutableStateOf(false)

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

    fun fetchBookingDetail(token: String, bookingId: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getBuyerBookingDetail("Bearer $token", bookingId, userId)
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

    fun fetchNotifications(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getBuyerNotifications("Bearer $token", userId)
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

    fun fetchSavedProducts(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getSavedProducts("Bearer $token", userId)
                if (response.isSuccessful) {
                    val list = response.body()?.products ?: emptyList()
                    _savedProductsList.value = list
                    _savedProducts.value = list.map { it.id }
                }
            } catch (e: Exception) {
                // handle error
            } finally {
                _loading.value = false
            }
        }
    }

    fun markAsRead(token: String, userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.markBuyerNotificationsRead("Bearer $token", userId)
                if (response.isSuccessful) {
                    hasUnread.value = false
                }
            } catch (e: Exception) {
            }
        }
    }

    fun acceptOffer(token: String, bookingId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.merchantAcceptOffer("Bearer $token", bookingId, userId)
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

    fun rejectOffer(token: String, bookingId: String, userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.merchantRejectOffer("Bearer $token", bookingId, userId)
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

    fun counterOffer(token: String, bookingId: String, userId: String, quantity: Float, price: Float, message: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = com.agriconnect.data.model.CounterOfferRequest(quantity, price, message)
                val response = RetrofitClient.instance.merchantCounterOffer("Bearer $token", bookingId, userId, request)
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

    fun createBooking(token: String, userId: String, productId: String, quantity: Float, price: Float, message: String? = null, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val request = BookingCreateRequest(
                    productId = productId, 
                    quantity = quantity, 
                    price = price, 
                    message = message,
                    termsAccepted = true
                )
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
                    _savedProductsList.value = _savedProductsList.value.filter { it.id != productId }
                } else {
                    RetrofitClient.instance.saveProduct("Bearer $token", productId, userId)
                    _savedProducts.value = _savedProducts.value + productId
                    // Ideally fetch again or add placeholder
                    fetchSavedProducts(token, userId)
                }
            } catch (e: Exception) {
                // fail silently for fav toggle
            }
        }
    }
}
