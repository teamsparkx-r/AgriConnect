package com.agriconnect.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agriconnect.app.ui.theme.Emerald600
import com.agriconnect.app.ui.viewmodel.ProductViewModel
import com.agriconnect.data.model.ProductCreateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSellingSlotScreen(
    token: String,
    userId: String,
    viewModel: ProductViewModel,
    onNext: () -> Unit
) {
    var cropName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("grains") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var price by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("2024-05-20") }
    
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }

    val loading by viewModel.loading
    val error by viewModel.error

    val categories = listOf("vegetables", "fruits", "grains", "pulses", "spices", "oilseeds")
    val units = listOf("kg", "tonne", "quintal", "box")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // App Bar
        TopAppBar(
            title = { Text("Initialize Supply Path", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = {
                IconButton(onClick = { /* Handle back */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(modifier = Modifier.padding(24.dp)) {
            // Identity Parameters
            SectionLabel("IDENTITY PARAMETERS")
            
            OutlinedTextField(
                value = cropName,
                onValueChange = { cropName = it },
                label = { Text("Produce Label") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("e.g. Sona Masuri Rice") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Dropdown
            var expandedCat by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCat,
                onExpandedChange = { expandedCat = !expandedCat }
            ) {
                OutlinedTextField(
                    value = category.uppercase(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Registry Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCat,
                    onDismissRequest = { expandedCat = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.uppercase()) },
                            onClick = {
                                category = cat
                                expandedCat = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Intelligence Details") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Grade, quality standards, and harvest audit...") }
            )

            // Supply Metrics
            SectionLabel("SUPPLY METRICS")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Inventory Node") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("0.00") }
                )
                
                var expandedUnit by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = !expandedUnit },
                    modifier = Modifier.width(120.dp)
                ) {
                    OutlinedTextField(
                        value = unit.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u.uppercase()) },
                                onClick = {
                                    unit = u
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Discovery Rate (per Unit)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("₹ ") }
            )

            // Origin Node
            SectionLabel("ORIGIN NODE")

            OutlinedTextField(
                value = village,
                onValueChange = { village = it },
                label = { Text("Fulfillment Village") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District Cell") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State Hub") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Network Availability (Status)
            SectionLabel("NETWORK AVAILABILITY")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { status = "active" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "active") Emerald600 else Color(0xFFF3F4F6),
                        contentColor = if (status == "active") Color.White else Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ACTIVE PIPELINE", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
                Button(
                    onClick = { status = "harvesting_soon" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (status == "harvesting_soon") Color(0xFFF59E0B) else Color(0xFFF3F4F6),
                        contentColor = if (status == "harvesting_soon") Color.White else Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SOON", fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            if (error != null) {
                Text(text = error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val request = ProductCreateRequest(
                        name = cropName,
                        category = category,
                        description = description,
                        quantity = quantity.toFloatOrNull() ?: 0f,
                        unit = unit,
                        expectedPrice = price.toFloatOrNull(),
                        harvestDate = harvestDate,
                        state = state,
                        district = district,
                        village = village,
                        farmAddress = "$village, $district, $state",
                        status = status
                    )
                    viewModel.listProduce(token, "", request) {
                        onNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !loading && cropName.isNotEmpty() && quantity.isNotEmpty()
            ) {
                if (loading) {
                    run {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    }
                } else {
                    Text("PUBLISH INTELLIGENCE", fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = Color.Gray,
        modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        letterSpacing = 1.5.sp
    )
}
