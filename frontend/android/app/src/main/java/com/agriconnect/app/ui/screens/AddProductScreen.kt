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
import com.agriconnect.app.ui.components.AgriButton
import com.agriconnect.app.ui.components.AgriSectionTitle
import com.agriconnect.app.ui.components.AgriTextField
import com.agriconnect.app.ui.theme.Emerald600
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
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var price by remember { mutableStateOf("") }
    
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
            try {
                val photoFile = createImageFile(context)
                if (photoFile != null) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "com.agriconnect.app.fileprovider",
                        photoFile
                    )
                    currentPhotoUri = uri
                    cameraLauncher.launch(uri)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val loading by viewModel.loading
    val error by viewModel.error

    val categories = listOf("vegetables", "fruits", "grains", "pulses", "spices", "oilseeds")
    val units = listOf("kg", "tonne", "quintal", "box")

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("Supply Registration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Camera Section
            AgriSectionTitle(title = "LIVE VISUAL VERIFICATION", subtitle = "MANDATORY STEP")
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    if (capturedImages.size < 5) {
                        Surface(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasPermission) {
                                        try {
                                            val photoFile = createImageFile(context)
                                            if (photoFile != null) {
                                                val uri = FileProvider.getUriForFile(
                                                    context,
                                                    "com.agriconnect.app.fileprovider",
                                                    photoFile
                                                )
                                                currentPhotoUri = uri
                                                cameraLauncher.launch(uri)
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Camera launch failed", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f))
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.CameraAlt, null, tint = Emerald600)
                                Text("Add Live", style = MaterialTheme.typography.labelSmall, color = Emerald600)
                            }
                        }
                    }
                }
                
                items(capturedImages) { uri ->
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { capturedImages.remove(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .padding(4.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
            
            Text(
                text = "${capturedImages.size}/5 Photos captured. Take direct photos of the produce.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AgriSectionTitle(title = "IDENTITY PARAMETERS")
            
            AgriTextField(
                value = name,
                onValueChange = { name = it },
                label = "Produce Label",
                placeholder = "e.g. Fresh Red Tomatoes"
            )

            Spacer(modifier = Modifier.height(20.dp))

            var expandedCat by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCat,
                onExpandedChange = { expandedCat = !expandedCat }
            ) {
                AgriTextField(
                    value = category.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = "Registry Group",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCat,
                    onDismissRequest = { expandedCat = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.uppercase(), style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                category = cat
                                expandedCat = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            AgriSectionTitle(title = "SUPPLY METRICS")

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                AgriTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = "Quantity",
                    placeholder = "0.0",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                var expandedUnit by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = !expandedUnit },
                    modifier = Modifier.width(130.dp)
                ) {
                    AgriTextField(
                        value = unit.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = "Unit",
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u.uppercase(), style = MaterialTheme.typography.bodyMedium) },
                                onClick = {
                                    unit = u
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AgriTextField(
                value = price,
                onValueChange = { price = it },
                label = "Discovery Rate (per Unit)",
                placeholder = "0.00",
                prefix = { Text("₹ ", style = MaterialTheme.typography.bodyLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            AgriButton(
                text = "Publish Supply Node",
                onClick = { 
                    val request = ProductCreateRequest(
                        name = name,
                        category = category,
                        description = "Direct farm produce verified via live camera.",
                        quantity = quantity.toFloatOrNull() ?: 0f,
                        unit = unit,
                        expectedPrice = price.toFloatOrNull(),
                        harvestDate = null,
                        state = null,
                        district = null,
                        village = null,
                        farmAddress = null,
                        images = if (capturedImages.isNotEmpty()) capturedImages.joinToString(",") { it.toString() } else null
                    )
                    viewModel.listProduce(token, userId, request) {
                        onNext()
                    }
                },
                loading = loading,
                enabled = name.isNotEmpty() && category.isNotEmpty() && quantity.isNotEmpty() && price.isNotEmpty() && capturedImages.isNotEmpty()
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
