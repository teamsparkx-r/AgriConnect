package com.agriconnect.data.model

import com.google.gson.annotations.SerializedName

data class Booking(
    @SerializedName("id") val id: String = "",
    @SerializedName("booking_id") val bookingId: String = "",
    @SerializedName("buyer_id") val buyerId: String = "",
    @SerializedName("farmer_id") val farmerId: String = "",
    @SerializedName("product_id") val productId: String = "",
    @SerializedName("status") val status: String = "pending",
    @SerializedName("requested_quantity") val requestedQuantity: Float? = null,
    @SerializedName("negotiated_price") val negotiatedPrice: Float? = null,
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("farmer_name") val farmerName: String? = null,
    @SerializedName("buyer_name") val buyerName: String? = null,
    @SerializedName("buyer_id_alias") val buyerIdAlias: String? = null,
    @SerializedName("buyer_district") val buyerDistrict: String? = null,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("negotiations") val negotiations: List<NegotiationHistory>? = null
)

data class NegotiationHistory(
    val id: String = "",
    @SerializedName("sender_id") val senderId: String = "",
    @SerializedName("sender_name") val senderName: String? = null,
    val quantity: Float = 0f,
    val price: Float = 0f,
    val message: String? = null,
    val status: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)

data class BookingCreateRequest(
    @SerializedName("product_id") val productId: String,
    val quantity: Float,
    val price: Float,
    val message: String? = null,
    @SerializedName("terms_accepted") val termsAccepted: Boolean = true
)

data class CounterOfferRequest(
    val quantity: Float,
    val price: Float,
    val message: String? = null
)
