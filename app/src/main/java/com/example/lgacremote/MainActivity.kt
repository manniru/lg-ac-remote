package com.example.lgacremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var irManager: ConsumerIrManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager

        setContent {
            ModernLgAcRemoteApp(irManager)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernLgAcRemoteApp(irManager: ConsumerIrManager?) {
    val context = LocalContext.current
    var temperature by remember { mutableIntStateOf(24) }
    var fanSpeed by remember { mutableIntStateOf(5) } // 5 = Auto
    var mode by remember { mutableIntStateOf(0) } // 0 = Cool
    var isOn by remember { mutableStateOf(false) }

    val fanSpeedLabels = mapOf(0 to "Low", 2 to "Mid", 4 to "High", 5 to "Auto")
    val modeLabels = mapOf(0 to "Cool", 1 to "Dry", 2 to "Fan", 4 to "Heat")
    val modeIcons = mapOf(
        0 to Icons.Default.AcUnit, 
        1 to Icons.Default.WaterDrop, 
        2 to Icons.Default.Air, 
        4 to Icons.Default.WbSunny
    )

    fun sendIrCode(powerToggle: Boolean) {
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(context, "IR Emitter not found on this device", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val code = LgIrCodeGenerator.generateAcCode(powerToggle, temperature, mode, fanSpeed)
            val pattern = LgIrCodeGenerator.buildIrPattern(code)
            irManager.transmit(LgIrCodeGenerator.getFrequency(), pattern)
            if (powerToggle) {
                isOn = !isOn
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    val primaryColor = if (isOn) Color(0xFF00E676) else Color(0xFF757575)
    val bgColor = Color(0xFF121212)
    val surfaceColor = Color(0xFF1E1E1E)

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = bgColor,
            surface = surfaceColor,
            primary = primaryColor,
            onPrimary = Color.Black
        )
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("LG Dual Inverter", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = bgColor,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor)
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Display Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(surfaceColor)
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$temperature",
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Light,
                                color = if (isOn) Color.White else Color.Gray
                            )
                            Text(
                                text = "°C",
                                fontSize = 24.sp,
                                color = if (isOn) primaryColor else Color.Gray,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusIconText(
                                icon = modeIcons[mode] ?: Icons.Default.AcUnit,
                                text = modeLabels[mode] ?: "Cool",
                                isActive = isOn
                            )
                            StatusIconText(
                                icon = Icons.Default.Air,
                                text = fanSpeedLabels[fanSpeed] ?: "Auto",
                                isActive = isOn
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Primary Controls
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Up Button (Temp +)
                    IconButton(
                        onClick = { 
                            if (temperature < 30) {
                                temperature++
                                sendIrCode(powerToggle = false)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(64.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Temp Up", modifier = Modifier.size(40.dp), tint = Color.White)
                    }

                    // Down Button (Temp -)
                    IconButton(
                        onClick = { 
                            if (temperature > 16) {
                                temperature--
                                sendIrCode(powerToggle = false)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(64.dp)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Temp Down", modifier = Modifier.size(40.dp), tint = Color.White)
                    }

                    // Power Button (Center)
                    FloatingActionButton(
                        onClick = { sendIrCode(powerToggle = true) },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        containerColor = if (isOn) Color(0xFFE53935) else primaryColor
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", modifier = Modifier.size(40.dp), tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Controls (Mode & Fan)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(
                        icon = Icons.Default.Dashboard,
                        label = "Mode",
                        onClick = {
                            mode = if (mode == 0) 1 else if (mode == 1) 2 else if (mode == 2) 4 else 0
                            sendIrCode(powerToggle = false)
                        }
                    )

                    ControlButton(
                        icon = Icons.Default.Air,
                        label = "Fan",
                        onClick = {
                            fanSpeed = if (fanSpeed == 0) 2 else if (fanSpeed == 2) 4 else if (fanSpeed == 4) 5 else 0
                            sendIrCode(powerToggle = false)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusIconText(icon: ImageVector, text: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = if (isActive) Color.White else Color.Gray,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isActive) Color.White else Color.Gray
        )
    }
}

@Composable
fun ControlButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color(0xFF1E1E1E),
                contentColor = Color.White
            )
        ) {
            Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
    }
}