package com.agriconnect.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val mobile: String,
    val email: String? = null,
    @SerializedName("full_name") val fullName: String,
    val role: String,
    @SerializedName("account_status") val accountStatus: String? = "active",
    
    // Additional fields for Farmer/Merchant
    val village: String? = null,
    val mandal: String? = null,
    val district: String? = null,
    val state: String? = null,
    @SerializedName("business_name") val businessName: String? = null,
    @SerializedName("current_location") val currentLocation: String? = null,
    @SerializedName("preferred_locations") val preferredLocations: List<String>? = null,
    @SerializedName("preferred_crops") val preferredCrops: List<String>? = null
)

data class LoginRequest(
    val identity: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    val user: User?,
    @SerializedName("user_id") val userId: String?,
    val role: String?,
    @SerializedName("farmer_id") val farmerId: String?,
    @SerializedName("buyer_id") val buyerId: String?
)
