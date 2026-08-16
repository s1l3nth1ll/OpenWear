package org.openwear.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.openwear.app.bluetooth.BleConnection
import org.openwear.app.bluetooth.BleDevice
import org.openwear.app.bluetooth.BleScanner
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    private var bleScanner: BleScanner? = null

    private lateinit var bleConnection: BleConnection

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.values.all { it }

            if (granted) {
                startScanning()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bleConnection = BleConnection(this)

        bluetoothAdapter?.let { adapter ->
            bleScanner = BleScanner(adapter)
        }

        setContent {

            val devices by (
                    bleScanner?.devices
                        ?: MutableStateFlow<List<BleDevice>>(emptyList())
                    ).collectAsState()

            OpenWearApp(
                devices = devices,

                onScanClick = {
                    requestBluetoothPermissions()
                },

                onDeviceClick = { device ->
                    android.widget.Toast.makeText(
                        this,
                        "Connecting to ${device.name ?: device.address}...",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()

                    bleConnection.connect(device.address)
                }
            )
        }
    }

    private fun requestBluetoothPermissions() {

        val permissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )

            } else {

                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            }

        if (
            permissions.all {
                checkSelfPermission(it) ==
                        PackageManager.PERMISSION_GRANTED
            }
        ) {
            startScanning()
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    private fun startScanning() {
        bleScanner?.startScan()
    }

    override fun onDestroy() {

        bleScanner?.stopScan()
        bleConnection.disconnect()

        super.onDestroy()
    }
}

@androidx.compose.runtime.Composable
fun OpenWearApp(
    devices: List<BleDevice>,
    onScanClick: () -> Unit,
    onDeviceClick: (BleDevice) -> Unit
) {

    MaterialTheme {

        Surface(
            modifier = Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "OpenWear",
                    style = MaterialTheme.typography.headlineLarge
                )

                Button(
                    onClick = onScanClick,

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text("Scan for devices")
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {

                    items(
                        items = devices,
                        key = { it.address }
                    ) { device ->

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                text = device.name
                                    ?: "Unknown device",

                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = device.address
                            )

                            Text(
                                text = "RSSI: ${device.rssi} dBm"
                            )

                            Button(
                                onClick = {
                                    onDeviceClick(device)
                                },

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Connect")
                            }
                        }
                    }
                }
            }
        }
    }
}