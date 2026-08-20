package com.agriconnect.data.model

import com.google.gson.annotations.SerializedName

data class AIContext(
    val role: String,
    @SerializedName("current_screen") val currentScreen: String,
    @SerializedName("user_id") val userId: String? = null,
    @SerializedName("current_product_id") val currentProductId: String? = null
)

data class AIRequest(
    val text: String,
    val context: AIContext
)

data class AIActionResponse(
    val action: String, // NAVIGATE, SEARCH, EXPLAIN, ADD_PRODUCT, etc.
    val target: String? = null, // Route or screen name
    val params: Map<String, String> = emptyMap(),
    @SerializedName("response_text") val responseText: String,
    @SerializedName("requires_confirmation") val requiresConfirmation: Boolean = false
)
