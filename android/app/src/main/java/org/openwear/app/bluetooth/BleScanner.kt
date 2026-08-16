package org.openwear.app.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleScanner(
    private val bluetoothAdapter: BluetoothAdapter
) {
    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())

    val devices: StateFlow<List<BleDevice>> = _devices.asStateFlow()

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            val device = result.device

            val bleDevice = BleDevice(
                name = try {
                    device.name
                } catch (_: SecurityException) {
                    null
                },
                address = device.address,
                rssi = result.rssi
            )

            val currentDevices = _devices.value

            _devices.value =
                if (currentDevices.any { it.address == bleDevice.address }) {
                    currentDevices.map {
                        if (it.address == bleDevice.address) {
                            bleDevice
                        } else {
                            it
                        }
                    }
                } else {
                    currentDevices + bleDevice
                }
        }

        override fun onScanFailed(errorCode: Int) {
            // Error handling will be added next.
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() {
        _devices.value = emptyList()
        bluetoothAdapter.bluetoothLeScanner.startScan(scanCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() {
        bluetoothAdapter.bluetoothLeScanner.stopScan(scanCallback)
    }

}

