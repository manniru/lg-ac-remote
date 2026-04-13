package com.example.lgacremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            LgAcRemoteApp(irManager)
        }
    }
}

@Composable
fun LgAcRemoteApp(irManager: ConsumerIrManager?) {
    val context = LocalContext.current
    var temperature by remember { mutableStateOf(24) }
    var fanSpeed by remember { mutableStateOf(5) } // 5 = Auto
    var mode by remember { mutableStateOf(0) } // 0 = Cool

    val fanSpeedLabels = mapOf(0 to "Low", 2 to "Mid", 4 to "High", 5 to "Auto")
    val modeLabels = mapOf(0 to "Cool", 1 to "Dry", 2 to "Fan", 4 to "Heat")

    fun sendIrCode(powerToggle: Boolean) {
        if (irManager == null || !irManager.hasIrEmitter()) {
            Toast.makeText(context, "IR Emitter not found on this device", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val code = LgIrCodeGenerator.generateAcCode(powerToggle, temperature, mode, fanSpeed)
            val pattern = LgIrCodeGenerator.buildIrPattern(code)
            irManager.transmit(LgIrCodeGenerator.getFrequency(), pattern)
            Toast.makeText(context, "Signal sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF00C853),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "LG Dual Inverter",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Screen Display
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${temperature}°C",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mode: ${modeLabels[mode]} | Fan: ${fanSpeedLabels[fanSpeed]}",
                            fontSize = 18.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                // Power Button
                Button(
                    onClick = { sendIrCode(powerToggle = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier
                        .size(80.dp),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Text("PWR", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Temp Controls
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            if (temperature > 16) {
                                temperature--
                                sendIrCode(powerToggle = false)
                            }
                        },
                        modifier = Modifier.size(70.dp),
                        shape = RoundedCornerShape(35.dp)
                    ) {
                        Text("-", fontSize = 24.sp)
                    }

                    Button(
                        onClick = {
                            if (temperature < 30) {
                                temperature++
                                sendIrCode(powerToggle = false)
                            }
                        },
                        modifier = Modifier.size(70.dp),
                        shape = RoundedCornerShape(35.dp)
                    ) {
                        Text("+", fontSize = 24.sp)
                    }
                }

                // Mode Controls
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = {
                        mode = if (mode == 0) 1 else if (mode == 1) 2 else if (mode == 2) 4 else 0
                        sendIrCode(powerToggle = false)
                    }) {
                        Text("Mode")
                    }

                    Button(onClick = {
                        fanSpeed = if (fanSpeed == 0) 2 else if (fanSpeed == 2) 4 else if (fanSpeed == 4) 5 else 0
                        sendIrCode(powerToggle = false)
                    }) {
                        Text("Fan")
                    }
                }
                
                // Extra commands (Jet, etc) - keeping simple for now
                Button(
                    onClick = { sendIrCode(powerToggle = false) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Apply Current Settings", fontSize = 18.sp)
                }
            }
        }
    }
}
