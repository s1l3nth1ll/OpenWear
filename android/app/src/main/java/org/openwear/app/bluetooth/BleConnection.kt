package org.openwear.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import java.util.ArrayDeque
import java.util.UUID

class BleConnection(
    private val context: Context
) {
    var onBatteryChanged: ((Int) -> Unit)? = null
    var onHeartRateChanged: ((Int) -> Unit)? = null
    var onConnectionChanged: ((Boolean) -> Unit)? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val readQueue = ArrayDeque<BluetoothGattCharacteristic>()
    private var readInProgress = false

    private val deviceInformationService =
        UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")

    private val batteryService =
        UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")

    private val batteryCharacteristic =
        UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")

    private val heartRateService =
        UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")

    private val heartRateCharacteristic =
        UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")

    private val cccDescriptor =
        UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            Log.d(
                "OpenWear",
                "Connection state changed: status=$status state=$newState"
            )

            when (newState) {

                BluetoothProfile.STATE_CONNECTED -> {

                    if (gatt !== bluetoothGatt) {
                        Log.d(
                            "OpenWear",
                            "Ignoring stale GATT connection"
                        )
                        onConnectionChanged?.invoke(true)
                        gatt.close()
                        return
                    }

                    Log.d(
                        "OpenWear",
                        "CONNECTED: ${gatt.device.address}"
                    )

                    gatt.discoverServices()
                }

                BluetoothProfile.STATE_DISCONNECTED -> {

                    Log.d(
                        "OpenWear",
                        "DISCONNECTED: ${gatt.device.address}"
                    )
                    onConnectionChanged?.invoke(false)

                    if (gatt === bluetoothGatt) {
                        bluetoothGatt = null
                        readQueue.clear()
                        readInProgress = false
                    }

                    gatt.close()
                }
            }
        }

        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(
                    "OpenWear",
                    "Service discovery failed: $status"
                )
                return
            }

            Log.d(
                "OpenWear",
                "Services discovered"
            )

            queueDeviceInformationReads(gatt)

            readNextCharacteristic(gatt)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            readInProgress = false

            if (status == BluetoothGatt.GATT_SUCCESS) {

                val value = characteristic.value

                Log.d(
                    "OpenWear",
                    "READ ${characteristic.uuid}: " +
                            value.toHexString()
                )

                when (characteristic.uuid) {

                    batteryCharacteristic -> {
                        if (value.isNotEmpty()) {
                            val battery = value[0].toInt() and 0xFF

                            Log.d(
                                "OpenWear",
                                "BATTERY: $battery%"
                            )
                            onBatteryChanged?.invoke(battery)
                        }
                    }

                    else -> {
                        Log.d(
                            "OpenWear",
                            "TEXT ${characteristic.uuid}: " +
                                    value.toString(Charsets.UTF_8)
                                        .trimEnd('\u0000')
                        )
                    }
                }

            } else {

                Log.e(
                    "OpenWear",
                    "Read failed ${characteristic.uuid}: " +
                            "status=$status"
                )
            }

            readNextCharacteristic(gatt)
        }

        @Suppress("DEPRECATION")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            Log.d(
                "OpenWear",
                "Descriptor write: ${descriptor.uuid} status=$status"
            )

            if (descriptor.uuid == cccDescriptor) {

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(
                        "OpenWear",
                        "HEART RATE NOTIFICATIONS ENABLED"
                    )
                } else {
                    Log.e(
                        "OpenWear",
                        "Failed to enable heart rate notifications"
                    )
                }
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid != heartRateCharacteristic) {
                return
            }

            val value = characteristic.value

            Log.d(
                "OpenWear",
                "HEART RATE PACKET: ${value.toHexString()}"
            )

            if (value.isEmpty()) {
                return
            }

            val flags = value[0].toInt() and 0xFF

            val heartRate =
                if ((flags and 0x01) == 0) {

                    if (value.size < 2) {
                        return
                    }

                    value[1].toInt() and 0xFF

                } else {

                    if (value.size < 3) {
                        return
                    }

                    (value[1].toInt() and 0xFF) or
                            ((value[2].toInt() and 0xFF) shl 8)
                }

            Log.d(
                "OpenWear",
                "HEART RATE: $heartRate BPM"
            )
            onHeartRateChanged?.invoke(heartRate)
        }
    }

    private fun queueDeviceInformationReads(
        gatt: BluetoothGatt
    ) {
        val service = gatt.getService(
            deviceInformationService
        )

        if (service == null) {
            Log.e(
                "OpenWear",
                "Device Information service not found"
            )
        } else {

            Log.d(
                "OpenWear",
                "Device Information service found"
            )

            for (characteristic in service.characteristics) {

                if (
                    characteristic.properties and
                    BluetoothGattCharacteristic.PROPERTY_READ != 0
                ) {
                    readQueue.add(characteristic)

                    Log.d(
                        "OpenWear",
                        "Queued device-info read: " +
                                characteristic.uuid
                    )
                }
            }
        }

        val battery = gatt
            .getService(batteryService)
            ?.getCharacteristic(batteryCharacteristic)

        if (battery != null) {

            Log.d(
                "OpenWear",
                "Battery characteristic found"
            )

            readQueue.add(battery)

        } else {

            Log.e(
                "OpenWear",
                "Battery characteristic not found"
            )
        }
    }

    private fun readNextCharacteristic(
        gatt: BluetoothGatt
    ) {
        if (readInProgress) {
            return
        }

        val characteristic = readQueue.pollFirst()

        if (characteristic == null) {

            Log.d(
                "OpenWear",
                "Finished queued reads"
            )

            enableHeartRateNotifications(gatt)
            return
        }

        readInProgress = true

        Log.d(
            "OpenWear",
            "Reading: ${characteristic.uuid}"
        )

        @Suppress("DEPRECATION")
        val started = gatt.readCharacteristic(characteristic)

        if (!started) {

            readInProgress = false

            Log.e(
                "OpenWear",
                "Could not start read: " +
                        characteristic.uuid
            )

            readNextCharacteristic(gatt)
        }
    }

    @Suppress("DEPRECATION")
    private fun enableHeartRateNotifications(
        gatt: BluetoothGatt
    ) {
        val characteristic = gatt
            .getService(heartRateService)
            ?.getCharacteristic(heartRateCharacteristic)

        if (characteristic == null) {
            Log.e(
                "OpenWear",
                "Heart rate characteristic not found"
            )
            return
        }

        Log.d(
            "OpenWear",
            "Enabling heart rate notifications"
        )

        val notificationEnabled =
            gatt.setCharacteristicNotification(
                characteristic,
                true
            )

        if (!notificationEnabled) {
            Log.e(
                "OpenWear",
                "setCharacteristicNotification failed"
            )
            return
        }

        val descriptor = characteristic
            .getDescriptor(cccDescriptor)

        if (descriptor == null) {
            Log.e(
                "OpenWear",
                "Heart rate CCC descriptor not found"
            )
            return
        }

        descriptor.value =
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

        val started = gatt.writeDescriptor(descriptor)

        Log.d(
            "OpenWear",
            "Heart rate descriptor write started: $started"
        )
    }

    fun connect(address: String) {

        Log.d(
            "OpenWear",
            "Attempting connection to $address"
        )

        val adapter = BluetoothAdapter.getDefaultAdapter()

        if (adapter == null) {
            Log.e(
                "OpenWear",
                "Bluetooth adapter unavailable"
            )
            return
        }

        if (!adapter.isEnabled) {
            Log.e(
                "OpenWear",
                "Bluetooth is disabled"
            )
            return
        }

        try {

            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()

            bluetoothGatt = null

            readQueue.clear()
            readInProgress = false

            val device = adapter.getRemoteDevice(address)

            bluetoothGatt = device.connectGatt(
                context,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )

            Log.d(
                "OpenWear",
                "connectGatt() called"
            )

        } catch (e: SecurityException) {

            Log.e(
                "OpenWear",
                "Bluetooth permission error",
                e
            )

        } catch (e: IllegalArgumentException) {

            Log.e(
                "OpenWear",
                "Invalid Bluetooth address: $address",
                e
            )
        }
    }

    fun disconnect() {

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()

        bluetoothGatt = null

        readQueue.clear()
        readInProgress = false
    }

    private fun ByteArray.toHexString(): String {
        return joinToString(" ") {
            "%02X".format(it.toInt() and 0xFF)
        }
    }
}