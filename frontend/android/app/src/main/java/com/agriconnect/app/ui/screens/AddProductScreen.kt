package com.agriconnect.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.agriconnect.app.ui.components.*
import com.agriconnect.app.ui.theme.*
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.data.model.ProductCreateRequest
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun createImageFile(context: android.content.Context): File? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir != null && !storageDir.exists()) storageDir.mkdirs()
        File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    token: String,
    userId: String,
    viewModel: ProductViewModel,
    authViewModel: com.agriconnect.app.ui.viewmodel.AuthViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user by authViewModel.user
    var step by remember { mutableIntStateOf(1) }
    
    // Form Data
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var price by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("A Grade") }
    var description by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    
    val capturedImages = remember { mutableStateListOf<Uri>() }
    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoUri != null) {
            capturedImages.add(currentPhotoUri!!)
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = createImageFile(context)
            if (photoFile != null) {
                val uri = FileProvider.getUriForFile(context, "com.agriconnect.app.fileprovider", photoFile)
                currentPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }
    }

    val loading by viewModel.loading
    val error by viewModel.error

    Scaffold(
        containerColor = AgriBackground,
        topBar = {
            AgriTopAppBar(
                title = if (step == 4) "Review Listing" else "Add Produce",
                showLogo = false,
                onBackClick = { if (step > 1) step-- else onBack() }
            )
        }
    ) { padding ->
        if (user?.accountStatus == "pending") {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.VerifiedUser, null, tint = Warning, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    AgriText("Verification Required", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    AgriText(
                        "Your account is currently awaiting Admin approval. You will be notified once your profile is verified and you can start listing your crops.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    AgriButton(text = "GO BACK", onClick = onBack)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Multi-step indicator
                Row(
                    modifier = Modifier.fillMaxWidth().background(White).padding(vertical = 16.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepItem(1, "Info", step >= 1)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 1) AgriPrimary else Gray200)
                    StepItem(2, "Images", step >= 2)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 2) AgriPrimary else Gray200)
                    StepItem(3, "Harvest", step >= 3)
                    Divider(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), color = if (step > 3) AgriPrimary else Gray200)
                    StepItem(4, "Review", step >= 4)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    when (step) {
                        1 -> StepCropInfo(name, {name = it}, category, {category = it}, grade, {grade = it}, quantity, {quantity = it}, unit, {unit = it}, price, {price = it})
                        2 -> StepImages(capturedImages, context, permissionLauncher, cameraLauncher) { currentPhotoUri = it }
                        3 -> StepDetails(description, {description = it}, harvestDate, {harvestDate = it})
                        4 -> StepReview(name, category, grade, quantity, unit, price, capturedImages.size)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (error != null) {
                        Surface(color = Error.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            AgriText(text = error!!, color = Error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    AgriButton(
                        text = if (step < 4) "Continue" else "Publish to Marketplace",
                        onClick = {
                            if (step < 4) {
                                step++
                            } else {
                                // Mock a real URL for the demo as we don't have a storage bucket configured yet
                                val imageString = if (capturedImages.isNotEmpty()) {
                                    "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=800" 
                                } else null

                                val request = ProductCreateRequest(
                                    name = name,
                                    category = category,
                                    description = description,
                                    quantity = quantity.toFloatOrNull() ?: 0f,
                                    unit = unit,
                                    expectedPrice = price.toFloatOrNull(),
                                    harvestDate = if (harvestDate.isEmpty()) null else harvestDate,
                                    status = "active",
                                    state = user?.state ?: "",
                                    district = user?.district ?: "",
                                    village = user?.village ?: "",
                                    farmAddress = "",
                                    images = imageString
                                )
                                viewModel.listProduce(token, userId, request) { onNext() }
                            }
                        },
                        loading = loading,
                        enabled = when(step) {
                            1 -> name.isNotEmpty() && category.isNotEmpty() && quantity.isNotEmpty() && price.isNotEmpty()
                            2 -> true // Made images optional for now to avoid blocking, though recommended
                            3 -> true // Harvest date is optional
                            else -> true
                        }
                    )
                    
                    if (step == 1 && (name.isEmpty() || category.isEmpty() || quantity.isEmpty() || price.isEmpty())) {
                        AgriText(
                            text = "* Please fill all required fields to continue",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gray400,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    
                    if (step == 2 && capturedImages.isEmpty()) {
                        AgriText(
                            text = "Tip: Adding photos helps you sell faster!",
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriPrimary,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun StepItem(number: Int, label: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(28.dp),
            shape = CircleShape,
            color = if (isActive) AgriPrimary else Gray100
        ) {
            Box(contentAlignment = Alignment.Center) {
                AgriText(text = number.toString(), style = MaterialTheme.typography.labelSmall, color = if (isActive) White else Gray400, fontWeight = FontWeight.Black)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        AgriText(text = label, style = MaterialTheme.typography.labelSmall, color = if (isActive) AgriPrimary else Gray400, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StepCropInfo(name: String, onName: (String) -> Unit, cat: String, onCat: (String) -> Unit, grade: String, onGrade: (String) -> Unit, qty: String, onQty: (String) -> Unit, unit: String, onUnit: (String) -> Unit, price: String, onPrice: (String) -> Unit) {
    val categories = listOf("Vegetables", "Fruits", "Grains", "Pulses", "Spices", "Oilseeds", "Other")
    
    AgriSectionTitle(title = "Crop Information")
    AgriTextField(value = name, onValueChange = onName, label = "Crop Name", placeholder = "e.g. Sona Masuri Rice")
    Spacer(modifier = Modifier.height(16.dp))
    
    AgriText("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { categoryItem ->
            val isSelected = cat.lowercase() == categoryItem.lowercase()
            Surface(
                onClick = { onCat(categoryItem.lowercase()) },
                color = if (isSelected) AgriPrimary else AgriSecondary,
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Gray200)
            ) {
                AgriText(
                    text = categoryItem, 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) White else AgriPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    AgriText("Grade", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("A Grade", "B Grade").forEach { g ->
            val isSel = grade == g
            Surface(
                onClick = { onGrade(g) },
                color = if (isSel) AgriPrimary else AgriSecondary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                AgriText(text = g, modifier = Modifier.padding(12.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if (isSel) White else AgriPrimary, fontWeight = FontWeight.Black)
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        AgriTextField(value = qty, onValueChange = onQty, label = "Quantity", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        AgriTextField(value = unit, onValueChange = onUnit, label = "Unit", modifier = Modifier.weight(0.6f))
    }
    Spacer(modifier = Modifier.height(16.dp))
    AgriTextField(value = price, onValueChange = onPrice, label = "Expected Price (₹)", placeholder = "Price per $unit", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

@Composable
fun StepImages(images: List<Uri>, context: android.content.Context, perm: androidx.activity.result.ActivityResultLauncher<String>, cam: androidx.activity.result.ActivityResultLauncher<Uri>, onUri: (Uri) -> Unit) {
    AgriSectionTitle(title = "Add Crop Photos")
    AgriText("Take direct photos of the produce for verification.", style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(20.dp))
    
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Surface(
                modifier = Modifier.size(120.dp).clickable {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val photoFile = createImageFile(context)
                        if (photoFile != null) {
                            val uri = FileProvider.getUriForFile(context, "com.agriconnect.app.fileprovider", photoFile)
                            onUri(uri)
                            cam.launch(uri)
                        }
                    } else {
                        perm.launch(Manifest.permission.CAMERA)
                    }
                },
                color = AgriSecondary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.CameraAlt, null, tint = AgriPrimary)
                    AgriText("Take Photo", style = MaterialTheme.typography.labelSmall, color = AgriPrimary, fontWeight = FontWeight.Black)
                }
            }
        }
        items(images) { uri ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(120.dp).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_gallery),
                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
            )
        }
    }
}

@Composable
fun StepDetails(desc: String, onDesc: (String) -> Unit, date: String, onDate: (String) -> Unit) {
    AgriSectionTitle(title = "Additional Details")
    AgriTextField(value = desc, onValueChange = onDesc, label = "Intelligence Details", placeholder = "Describe quality, harvest audit...", modifier = Modifier.height(150.dp))
    Spacer(modifier = Modifier.height(16.dp))
    AgriTextField(value = date, onValueChange = onDate, label = "Harvest Date", placeholder = "Select Date", trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Gray400) })
}

@Composable
fun StepReview(name: String, cat: String, grade: String, qty: String, unit: String, price: String, imgCount: Int) {
    AgriSectionTitle(title = "Final Review")
    Surface(color = White, shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Gray100)) {
        Column(modifier = Modifier.padding(24.dp)) {
            ReviewRow("Crop", name)
            ReviewRow("Grade", grade)
            ReviewRow("Quantity", "$qty $unit")
            ReviewRow("Price", "₹$price / kg")
            ReviewRow("Photos", "$imgCount captured")
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        AgriText(label, color = Gray400, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        AgriText(value, color = Gray900, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Black)
    }
}
