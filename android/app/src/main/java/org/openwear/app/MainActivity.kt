package org.openwear.app

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import org.openwear.app.bluetooth.BleConnection

class MainActivity : Activity() {

    // ------------------------------------------------------------
    // BLE connection to the Polar Pacer
    // ------------------------------------------------------------
    private lateinit var bleConnection: BleConnection

    // MAC address of the Polar Pacer used for testing.
    private val polarPacerAddress = "A0:9E:1A:B7:61:1C"

    // ------------------------------------------------------------
    // UI elements
    // ------------------------------------------------------------
    private lateinit var connectionStatus: TextView
    private lateinit var batteryText: TextView
    private lateinit var heartRateText: TextView
    private lateinit var connectButton: Button

    companion object {
        private const val TAG = "OpenWear"
        private const val BLUETOOTH_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the main activity layout.
        setContentView(R.layout.activity_main)

        // Find the UI elements from activity_main.xml.
        connectionStatus = findViewById(R.id.connectionStatus)
        batteryText = findViewById(R.id.batteryText)
        heartRateText = findViewById(R.id.heartRateText)
        connectButton = findViewById(R.id.connectButton)

        // Create the BLE connection handler.
        bleConnection = BleConnection(this)

        // --------------------------------------------------------
        // BLE connection state callback
        // --------------------------------------------------------
        bleConnection.onConnectionChanged = { connected ->

            // BLE callbacks may happen on a background thread,
            // so UI changes must happen on the main thread.
            runOnUiThread {

                if (connected) {
                    connectionStatus.text = "Connected"
                    connectButton.text = "Connected"
                } else {
                    connectionStatus.text = "Disconnected"
                    connectButton.text = "Connect"
                }
            }
        }

        // --------------------------------------------------------
        // Battery level callback
        // --------------------------------------------------------
        bleConnection.onBatteryChanged = { battery ->

            runOnUiThread {
                batteryText.text = "Battery: $battery%"
            }
        }

        // --------------------------------------------------------
        // Heart rate callback
        // --------------------------------------------------------
        bleConnection.onHeartRateChanged = { heartRate ->

            runOnUiThread {
                heartRateText.text = "Heart Rate: $heartRate BPM"
            }
        }

        // --------------------------------------------------------
        // Connect button
        // --------------------------------------------------------
        connectButton.setOnClickListener {

            if (hasBluetoothPermission()) {

                connectionStatus.text = "Connecting..."
                connectButton.text = "Connecting..."

                Log.d(
                    TAG,
                    "Connecting to Polar Pacer: $polarPacerAddress"
                )

                // Start the BLE connection.
                bleConnection.connect(polarPacerAddress)

            } else {

                // Request Bluetooth permissions if necessary.
                requestBluetoothPermission()
            }
        }

        // Check permissions when the app starts.
        if (!hasBluetoothPermission()) {
            requestBluetoothPermission()
        }
    }

    // ============================================================
    // BLUETOOTH PERMISSION CHECK
    // ============================================================

    private fun hasBluetoothPermission(): Boolean {

        // Android 12 and newer require these Bluetooth permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
        }

        // Older Android versions use the permissions declared
        // in the manifest without these runtime checks.
        return true
    }

    // ============================================================
    // REQUEST BLUETOOTH PERMISSIONS
    // ============================================================

    private fun requestBluetoothPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                ),
                BLUETOOTH_PERMISSION_REQUEST
            )
        }
    }

    // ============================================================
    // PERMISSION REQUEST RESULT
    // ============================================================

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {

            if (
                grantResults.isNotEmpty() &&
                grantResults.all {
                    it == PackageManager.PERMISSION_GRANTED
                }
            ) {

                Log.d(
                    TAG,
                    "Bluetooth permissions granted"
                )

            } else {

                Log.e(
                    TAG,
                    "Bluetooth permissions denied"
                )

                connectionStatus.text =
                    "Bluetooth permission required"
            }
        }
    }

    // ============================================================
    // ACTIVITY CLEANUP
    // ============================================================

    override fun onDestroy() {
        super.onDestroy()

        // Close the BLE connection when the Activity is destroyed.
        if (::bleConnection.isInitialized) {
            bleConnection.disconnect()
        }
    }
}