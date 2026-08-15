package org.openwear.app.bluetooth

data class BleDevice(
    val name: String?,
    val address: String,
    val rssi: int
)