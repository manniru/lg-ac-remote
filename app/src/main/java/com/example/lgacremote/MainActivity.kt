package com.example.lgacremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var irManager: ConsumerIrManager? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

        setContent {
            RealLgAcRemoteApp(irManager, vibrator)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLgAcRemoteApp(irManager: ConsumerIrManager?, vibrator: Vibrator?) {
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

    fun triggerVibration() {
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    fun sendRawCode(code: Int) {
        triggerVibration()
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(context, "IR Emitter not found", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val pattern = LgIrCodeGenerator.buildIrPattern(code)
            irManager.transmit(LgIrCodeGenerator.getFrequency(), pattern)
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun applyState() {
        triggerVibration()
        if (irManager == null || !irManager.hasIrEmitter()) return
        try {
            val code = LgIrCodeGenerator.generateAcCode(temperature, mode, fanSpeed)
            val pattern = LgIrCodeGenerator.buildIrPattern(code)
            irManager.transmit(LgIrCodeGenerator.getFrequency(), pattern)
        } catch (e: Exception) {}
    }

    val primaryColor = Color(0xFF00E676)
    val bgColor = Color(0xFFE0E0E0) // Light theme like a physical remote
    val remoteBodyColor = Color(0xFFF5F5F5)
    val screenColor = Color(0xFFC8E6C9) // Greenish LCD look
    val buttonColor = Color.White
    val textDark = Color(0xFF333333)

    MaterialTheme(
        colorScheme = lightColorScheme(
            background = bgColor,
            surface = remoteBodyColor,
            primary = primaryColor,
            onPrimary = Color.Black
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            // Remote Body
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 400.dp) // Constrain width on wide screens (Mate XT unfolded)
                    .padding(vertical = 24.dp, horizontal = 16.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(remoteBodyColor)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(40.dp))
                    .shadow(8.dp, RoundedCornerShape(40.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LG INVERTER V",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // LCD Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOn) screenColor else Color(0xFF9E9E9E))
                        .border(2.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    if (isOn) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(modeIcons[mode] ?: Icons.Default.AcUnit, contentDescription = null, tint = textDark)
                                Text(text = "Fan: ${fanSpeedLabels[fanSpeed]}", color = textDark, fontWeight = FontWeight.Bold)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "$temperature",
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Light,
                                    color = textDark
                                )
                                Text(
                                    text = "°C",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textDark,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Power & Light Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RemoteButtonSmall(
                        icon = Icons.Default.Lightbulb,
                        label = "LIGHT",
                        onClick = { sendRawCode(LgIrCodeGenerator.CODE_LIGHT) }
                    )
                    
                    // Main Power Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                            .clickable {
                                isOn = !isOn
                                sendRawCode(LgIrCodeGenerator.CODE_POWER)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    RemoteButtonSmall(
                        icon = Icons.Default.Eco,
                        label = "ENERGY",
                        onClick = { sendRawCode(LgIrCodeGenerator.CODE_ENERGY_SAVING) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // D-Pad Area
                Box(
                    modifier = Modifier.size(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Up Button (Temp +)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .size(70.dp)
                            .clip(RoundedCornerShape(topStart = 35.dp, topEnd = 35.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(buttonColor)
                            .clickable { 
                                if (isOn && temperature < 30) {
                                    temperature++
                                    applyState()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Temp Up", tint = textDark, modifier = Modifier.size(32.dp))
                        Text("TEMP", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp))
                    }

                    // Down Button (Temp -)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(70.dp)
                            .clip(RoundedCornerShape(bottomStart = 35.dp, bottomEnd = 35.dp, topStart = 8.dp, topEnd = 8.dp))
                            .background(buttonColor)
                            .clickable { 
                                if (isOn && temperature > 16) {
                                    temperature--
                                    applyState()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Temp Down", tint = textDark, modifier = Modifier.size(32.dp))
                    }

                    // Left Button (Mode)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(70.dp)
                            .clip(RoundedCornerShape(topStart = 35.dp, bottomStart = 35.dp, topEnd = 8.dp, bottomEnd = 8.dp))
                            .background(buttonColor)
                            .clickable { 
                                if (isOn) {
                                    mode = if (mode == 0) 1 else if (mode == 1) 2 else if (mode == 2) 4 else 0
                                    applyState()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Dashboard, contentDescription = "Mode", tint = textDark)
                            Text("MODE", fontSize = 10.sp, color = textDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Right Button (Fan)
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(70.dp)
                            .clip(RoundedCornerShape(topEnd = 35.dp, bottomEnd = 35.dp, topStart = 8.dp, bottomStart = 8.dp))
                            .background(buttonColor)
                            .clickable { 
                                if (isOn) {
                                    fanSpeed = if (fanSpeed == 0) 2 else if (fanSpeed == 2) 4 else if (fanSpeed == 4) 5 else 0
                                    applyState()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Air, contentDescription = "Fan", tint = textDark)
                            Text("FAN", fontSize = 10.sp, color = textDark, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Center Logo
                    Box(
                        modifier = Modifier.size(50.dp).clip(CircleShape).background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("LG", fontWeight = FontWeight.Black, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Bottom Utilities Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RemotePillButton(
                        label = "JET MODE",
                        onClick = { sendRawCode(LgIrCodeGenerator.CODE_JET) }
                    )
                    RemotePillButton(
                        label = "SWING ↕",
                        onClick = { sendRawCode(LgIrCodeGenerator.CODE_SWING_V) }
                    )
                    RemotePillButton(
                        label = "SWING ↔",
                        onClick = { sendRawCode(LgIrCodeGenerator.CODE_SWING_H) }
                    )
                }
            }
        }
    }
}

@Composable
fun RemoteButtonSmall(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { onClick() }
                .border(1.dp, Color(0xFFE0E0E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color(0xFF555555), modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RemotePillButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(80.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable { onClick() }
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 10.sp, color = Color(0xFF333333), fontWeight = FontWeight.Bold, maxLines = 1)
    }
}