package com.example.phoneassistant

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

typealias ConnectCallback = (address: String, success: Boolean) -> Unit
typealias ReceiveCallback = (address: String, message: String) -> Unit

class BluetoothManager private constructor(private val context: Context) {
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val connections = ConcurrentHashMap<String, BluetoothConnection>()
    private var receiveCallback: ReceiveCallback? = null

    companion object {
        @Volatile
        private var INSTANCE: BluetoothManager? = null
        fun getInstance(context: Context): BluetoothManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: BluetoothManager(context.applicationContext).also { INSTANCE = it }
            }
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }

    @SuppressLint("MissingPermission")
    fun getPairedHC05Devices(): List<BluetoothDevice> {
        val paired = adapter?.bondedDevices?.toList() ?: emptyList()
        return paired.filter { (it.name?.contains("HC-") == true) || (it.name?.contains("HC") == true) }
    }

    fun setReceiveCallback(cb: ReceiveCallback) {
        receiveCallback = cb
    }

    fun connect(device: BluetoothDevice, cb: ConnectCallback) {
        if (connections.containsKey(device.address)) {
            cb(device.address, true)
            return
        }
        val conn = BluetoothConnection(device, SPP_UUID) { addr, msg ->
            receiveCallback?.invoke(addr, msg)
        }
        connections[device.address] = conn
        CoroutineScope(Dispatchers.IO).launch {
            val ok = conn.connect()
            if (!ok) {
                connections.remove(device.address)
            }
            CoroutineScope(Dispatchers.Main).launch { cb(device.address, ok) }
        }
    }

    fun send(address: String, text: String) {
        connections[address]?.send(text)
    }

    fun registerReceiver(cb: ReceiveCallback) {
        setReceiveCallback(cb)
    }

    fun disconnectAll() {
        connections.values.forEach { it.close() }
        connections.clear()
    }
}
