package com.agriconnect.data.api

import com.agriconnect.data.model.*
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

interface AgriConnectApi {
    
    // Authentication
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/farmer/login")
    suspend fun loginFarmer(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/buyer/login")
    suspend fun loginBuyer(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<LoginResponse>
    
    @POST("api/farmer/register")
    suspend fun registerFarmer(@Body request: FarmerRegisterRequest): Response<LoginResponse>

    @POST("api/buyer/register")
    suspend fun registerBuyer(@Body request: BuyerRegisterRequest): Response<LoginResponse>

    // Farmer Endpoints
    @GET("api/farmer/dashboard")
    suspend fun getFarmerDashboard(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String
    ): Response<FarmerDashboardResponse>

    @GET("api/farmer/products")
    suspend fun getFarmerProducts(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String
    ): Response<FarmerProductsResponse>
    
    @POST("api/farmer/products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String,
        @Body product: ProductCreateRequest
    ): Response<ProductCreateResponse>

    @GET("api/farmer/bookings")
    suspend fun getFarmerBookings(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String,
        @Query("status") status: String? = null
    ): Response<FarmerBookingsResponse>

    @PUT("api/farmer/profile/{user_id}")
    suspend fun updateFarmerProfile(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String,
        @Body updates: ProfileUpdateRequest
    ): Response<Map<String, Any>>

    // Buyer/Discovery Endpoints
    @GET("api/buyer/dashboard/{user_id}")
    suspend fun getBuyerDashboard(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String
    ): Response<MerchantDashboardResponse>

    @GET("api/buyer/home")
    suspend fun getBuyerHome(
        @Header("Authorization") token: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<BuyerProductsResponse>

    @GET("api/buyer/bookings")
    suspend fun getBuyerBookings(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String
    ): Response<Map<String, Any>>

    @GET("api/buyer/search")
    suspend fun searchProducts(
        @Header("Authorization") token: String,
        @Query("query") query: String? = null,
        @Query("category") category: String? = null,
        @Query("district") district: String? = null
    ): Response<BuyerProductsResponse>
    
    @GET("api/buyer/products/{id}")
    suspend fun getProductDetail(
        @Path("id") id: String
    ): Response<ProductDetailResponse>

    @PUT("api/buyer/profile/{user_id}")
    suspend fun updateBuyerProfile(
        @Header("Authorization") token: String,
        @Path("user_id") userId: String,
        @Body updates: ProfileUpdateRequest
    ): Response<Map<String, Any>>

    // Saved Products
    @POST("api/buyer/saved/{product_id}")
    suspend fun saveProduct(
        @Header("Authorization") token: String,
        @Path("product_id") productId: String,
        @Query("user_id") userId: String
    ): Response<Map<String, Any>>

    @DELETE("api/buyer/saved/{product_id}")
    suspend fun removeSavedProduct(
        @Header("Authorization") token: String,
        @Path("product_id") productId: String,
        @Query("user_id") userId: String
    ): Response<Map<String, Any>>

    // Admin Endpoints
    @GET("api/admin/dashboard")
    suspend fun getAdminDashboard(
        @Header("Authorization") token: String
    ): Response<Map<String, Any>>
    
    // Bookings
    @POST("api/buyer/booking")
    suspend fun createBooking(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String,
        @Body request: BookingCreateRequest
    ): Response<Map<String, Any>>

    // Platform Stats
    @GET("api/stats")
    suspend fun getPlatformStats(): Response<PlatformStatsResponse>
}

// Request Models
data class FarmerRegisterRequest(
    @SerializedName("mobile_number") val mobileNumber: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    val state: String,
    val district: String,
    val village: String,
    val email: String? = null
)

data class BuyerRegisterRequest(
    @SerializedName("mobile_number") val mobileNumber: String,
    val password: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("buyer_type") val buyer_type: String = "merchant",
    val location: String? = null,
    val state: String? = null,
    val district: String? = null,
    val email: String? = null
)

data class BookingCreateRequest(
    @SerializedName("product_id") val productId: String,
    @SerializedName("terms_accepted") val termsAccepted: Boolean = true
)

data class ProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    val email: String? = null,
    val state: String? = null,
    val district: String? = null,
    val village: String? = null
)

data class OtpVerifyRequest(
    @SerializedName("mobile_number") val mobileNumber: String,
    @SerializedName("otp_code") val otpCode: String
)

data class ProductDetailResponse(
    val success: Boolean,
    val product: Product
)

data class PlatformStatsResponse(
    val success: Boolean,
    val statistics: Map<String, Any>
)

data class FarmerDashboardResponse(
    val success: Boolean,
    val stats: FarmerStats,
    @SerializedName("recent_products") val recentProducts: List<Product>,
    @SerializedName("recent_bookings") val recentBookings: List<Map<String, Any>>,
    val notifications: List<FarmerNotification>
)

data class FarmerStats(
    @SerializedName("total_products") val totalProducts: Int,
    @SerializedName("active_products") val activeProducts: Int,
    @SerializedName("harvesting_soon") val harvestingSoon: Int,
    @SerializedName("total_bookings") val totalBookings: Int,
    @SerializedName("total_earnings") val totalEarnings: Double,
    @SerializedName("unread_messages") val unreadMessages: Int
)

data class FarmerNotification(
    val title: String,
    val message: String,
    val type: String,
    @SerializedName("created_at") val createdAt: String
)

data class FarmerProductsResponse(
    val success: Boolean,
    val products: List<Product>,
    val total: Int
)

data class ProductCreateResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("product_id") val productId: String
)

data class FarmerBookingsResponse(
    val success: Boolean,
    val bookings: List<Map<String, Any>>,
    val total: Int
)

data class BuyerProductsResponse(
    val success: Boolean,
    val products: List<Product>,
    val total: Int
)

data class MerchantDashboardResponse(
    val success: Boolean,
    val summary: MerchantSummary,
    @SerializedName("recent_bookings") val recentBookings: List<Map<String, Any>>,
    val messages: List<Map<String, Any>>
)

data class MerchantSummary(
    @SerializedName("total_bookings") val totalBookings: Int,
    @SerializedName("active_bookings") val activeBookings: Int,
    @SerializedName("completed_bookings") val completedBookings: Int,
    @SerializedName("amount_spent") val amountSpent: Double
)
