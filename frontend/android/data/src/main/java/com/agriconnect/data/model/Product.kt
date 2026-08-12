package com.agriconnect.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: String = "",
    @SerializedName("farmer_id") val farmerId: String? = null,
    @SerializedName("farmer_id_alias") val farmerIdAlias: String? = null,
    val name: String? = null,
    val category: String? = null,
    val description: String? = null,
    val quantity: Float? = null,
    val unit: String? = null,
    @SerializedName("expected_price") val expectedPrice: Float? = null,
    @SerializedName("harvest_date") val harvestDate: String? = null,
    val status: String? = null,
    val images: String? = null,
    val state: String? = null,
    val district: String? = null,
    val village: String? = null,
    @SerializedName("farm_address") val farmAddress: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ProductCreateRequest(
    val name: String,
    val category: String,
    val description: String?,
    val quantity: Float,
    val unit: String,
    @SerializedName("expected_price") val expectedPrice: Float?,
    @SerializedName("harvest_date") val harvestDate: String?,
    val status: String = "active",
    val state: String?,
    val district: String?,
    val village: String?,
    @SerializedName("farm_address") val farmAddress: String?,
    val images: String? = null
)
