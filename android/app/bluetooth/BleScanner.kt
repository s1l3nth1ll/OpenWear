package org.openwear.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleScanner(
    private val bluetoothAdapter: BluetoothAdapter
) {

    private val scanner: BluetoothLeScanner
        get() = bluetoothAdapter.bluetoothLeScanner

    private val _devices = MutableStateFlow<List<BleDevice>>(emptyList())
    val devices: StateFlow<List<BleDevice>> = _devices.asStateFlow()

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            val device = result.device

            val bleDevice = BleDevice(
                name = device.name,
                address = device.address,
                rssi = result.rssi
            )

            val currentDevices = _devices.value

            if (currentDevices.none { it.address == bleDevice.address }) {
                _devices.value = currentDevices + bleDevice
            } else {
                _devices.value = currentDevices.map {
                    if (it.address == bleDevice.address) bleDevice else it
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            // We'll replace this with proper error handling later.
        }
    }

    fun startScan() {
        _devices.value = emptyList()
        scanner.startScan(scanCallback)
    }

    fun stopScan() {
        scanner.stopScan(scanCallback)
    }
}