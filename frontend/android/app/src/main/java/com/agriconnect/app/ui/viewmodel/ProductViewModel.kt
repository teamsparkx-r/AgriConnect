package com.agriconnect.app.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.data.api.RetrofitClient
import com.agriconnect.data.model.Product
import com.agriconnect.data.model.ProductCreateRequest
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val _products = mutableStateOf<List<Product>>(emptyList())
    val products: State<List<Product>> = _products

    private val _currentProduct = mutableStateOf<Product?>(null)
    val currentProduct: State<Product?> = _currentProduct

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun fetchDiscoveryProducts(token: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getBuyerHome("Bearer $token")
                if (response.isSuccessful) {
                    _products.value = response.body()?.products ?: emptyList()
                } else {
                    _error.value = "Failed to fetch produce listings."
                }
            } catch (e: Exception) {
                _error.value = "Network error while discovering produce."
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchProductDetail(productId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = RetrofitClient.instance.getProductDetail(productId)
                if (response.isSuccessful) {
                    _currentProduct.value = response.body()?.product
                }
            } catch (e: Exception) {
                // error
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchFarmerProducts(token: String, userId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.getFarmerProducts("Bearer $token", userId)
                if (response.isSuccessful) {
                    _products.value = response.body()?.products ?: emptyList()
                } else {
                    _error.value = "Failed to fetch your listings."
                }
            } catch (e: Exception) {
                _error.value = "Inventory network error."
            } finally {
                _loading.value = false
            }
        }
    }

    fun listProduce(token: String, userId: String, request: ProductCreateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = RetrofitClient.instance.createProduct("Bearer $token", userId, request)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _error.value = "Failed to publish intelligence node."
                }
            } catch (e: Exception) {
                _error.value = "Fulfillment network error."
            } finally {
                _loading.value = false
            }
        }
    }
}
