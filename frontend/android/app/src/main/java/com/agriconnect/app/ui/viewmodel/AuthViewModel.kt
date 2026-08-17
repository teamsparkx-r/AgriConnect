package com.agriconnect.app.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agriconnect.app.data.SessionManager
import com.agriconnect.data.api.*
import com.agriconnect.data.model.User
import kotlinx.coroutines.launch
import java.util.UUID

// Actual backend demo IDs for reference
private val mockUsers = mutableListOf<User>(
    User(id = "5737b51e-8640-48c2-bdaa-63fbba1b70a7", mobile = "8888888888", fullName = "Demo Farmer", role = "farmer", village = "Demo Village", district = "Pune", state = "Maharashtra"),
    User(id = "c5393115-bd07-4f3e-aee3-89d3d97745b9", mobile = "7777777777", fullName = "Demo Buyer", role = "buyer", businessName = "Demo Traders", currentLocation = "Mumbai", district = "Mumbai", state = "Maharashtra"),
    User(id = "2687eded-053d-4cbc-8a06-18be9ad5888b", mobile = "9999999999", fullName = "AgriConnect Admin", role = "admin")
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    
    private val _user = mutableStateOf<User?>(null)
    val user: State<User?> = _user

    private val _token = mutableStateOf<String?>(null)
    val token: State<String?> = _token

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        _user.value = sessionManager.getUser()
        _token.value = sessionManager.getToken()
    }

    private fun sanitizeMobile(mobile: String): String {
        val digits = mobile.filter { it.isDigit() }
        return if (digits.length > 10) digits.takeLast(10) else digits
    }

    fun sendOtp(mobileNumber: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loading.value = true
            // In dev, we skip actual SMS
            _loading.value = false
            onSuccess()
        }
    }

    fun verifyOtp(mobileNumber: String, otpCode: String, onVerifySuccess: (String) -> Unit) {
        val sanitizedMobile = sanitizeMobile(mobileNumber)
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                val request = OtpVerifyRequest(sanitizedMobile, otpCode)
                val response = RetrofitClient.instance.verifyOtp(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val backendUser = loginResponse.user
                    
                    if (backendUser != null) {
                        val rawRole = backendUser.role
                        val finalRole = rawRole.lowercase().trim()
                        android.util.Log.d("AgriConnect", "VerifyOtp backend user role: '$rawRole' -> '$finalRole'")
                        val updatedUser = backendUser.copy(role = finalRole)
                        
                        _user.value = updatedUser
                        _token.value = loginResponse.accessToken
                        sessionManager.saveUser(updatedUser)
                        sessionManager.saveToken(loginResponse.accessToken)
                        
                        android.util.Log.d("AgriConnect", "Login successful. Assigned Role: '$finalRole'")
                        onVerifySuccess(finalRole)
                        _loading.value = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Network failure
            }

            // Fallback for development
            if (otpCode == "000000" || otpCode == "123456") {
                val demoUser = mockUsers.find { it.mobile == sanitizedMobile }
                if (demoUser != null) {
                    val status = if (demoUser.role == "admin") "active" else "pending"
                    val finalUser = demoUser.copy(accountStatus = status)
                    _user.value = finalUser
                    val demoToken = "demo_token_${finalUser.id}"
                    _token.value = demoToken
                    sessionManager.saveUser(finalUser)
                    sessionManager.saveToken(demoToken)
                    onVerifySuccess(finalUser.role)
                } else {
                    _error.value = "Account not found. Please register."
                    onVerifySuccess("none")
                }
            } else {
                _error.value = "Verification failed."
                onVerifySuccess("none")
            }
            _loading.value = false
        }
    }

    fun register(
        mobileNumber: String,
        fullName: String,
        role: String,
        village: String? = null,
        mandal: String? = null,
        district: String? = null,
        state: String? = null,
        businessName: String? = null,
        currentLocation: String? = null,
        preferredLocations: List<String>? = null,
        preferredCrops: List<String>? = null,
        onSuccess: () -> Unit
    ) {
        val sanitizedMobile = sanitizeMobile(mobileNumber)
        
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                val response = if (role == "farmer") {
                    val request = FarmerRegisterRequest(
                        mobileNumber = sanitizedMobile,
                        password = "Agri123!",
                        fullName = fullName,
                        state = state ?: "",
                        district = district ?: "",
                        village = village ?: ""
                    )
                    RetrofitClient.instance.registerFarmer(request)
                } else {
                    val request = BuyerRegisterRequest(
                        mobileNumber = sanitizedMobile,
                        password = "Agri123!",
                        fullName = fullName,
                        state = state ?: "",
                        district = district ?: "",
                        location = currentLocation ?: ""
                    )
                    RetrofitClient.instance.registerBuyer(request)
                }

                if (response.isSuccessful && response.body() != null) {
                    val loginRes = response.body()!!
                    val registeredUser = loginRes.user ?: User(
                        id = loginRes.userId ?: UUID.randomUUID().toString(),
                        mobile = sanitizedMobile,
                        fullName = fullName,
                        role = if (role == "merchant") "buyer" else role,
                        accountStatus = "pending"
                    )
                    
                    _user.value = registeredUser
                    _token.value = loginRes.accessToken
                    sessionManager.saveUser(registeredUser)
                    sessionManager.saveToken(loginRes.accessToken)
                    onSuccess()
                } else {
                    _error.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                // Mock fallback if offline
                val newUser = User(
                    id = UUID.randomUUID().toString(),
                    mobile = sanitizedMobile,
                    fullName = fullName,
                    role = if (role == "merchant") "buyer" else role,
                    village = village, district = district, state = state,
                    accountStatus = "pending"
                )
                _user.value = newUser
                val mockToken = "mock_token_${newUser.id}"
                _token.value = mockToken
                sessionManager.saveUser(newUser)
                sessionManager.saveToken(mockToken)
                onSuccess()
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        _user.value = null
        _token.value = null
        sessionManager.logout()
    }

    fun updateUserStatus(status: String) {
        val current = _user.value
        if (current != null && current.accountStatus != status) {
            val updated = current.copy(accountStatus = status)
            _user.value = updated
            sessionManager.saveUser(updated)
        }
    }

    fun updateProfile(updatesMap: Map<String, String>, onResult: (Boolean) -> Unit) {
        val currentUser = _user.value ?: return
        val currentToken = _token.value ?: return

        viewModelScope.launch {
            _loading.value = true
            try {
                val updateReq = ProfileUpdateRequest(
                    fullName = updatesMap["full_name"],
                    email = updatesMap["email"],
                    state = updatesMap["state"],
                    district = updatesMap["district"],
                    village = updatesMap["village"]
                )
                
                val response = if (currentUser.role == "farmer") {
                    RetrofitClient.instance.updateFarmerProfile("Bearer $currentToken", currentUser.id, updateReq)
                } else {
                    RetrofitClient.instance.updateBuyerProfile("Bearer $currentToken", currentUser.id, updateReq)
                }

                if (response.isSuccessful) {
                    val updatedUser = currentUser.copy(
                        fullName = updateReq.fullName ?: currentUser.fullName,
                        email = updateReq.email ?: currentUser.email,
                        state = updateReq.state ?: currentUser.state,
                        district = updateReq.district ?: currentUser.district,
                        village = updateReq.village ?: currentUser.village
                    )
                    _user.value = updatedUser
                    sessionManager.saveUser(updatedUser)
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
}
