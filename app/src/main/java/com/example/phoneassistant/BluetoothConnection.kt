package com.example.phoneassistant

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.UUID

class BluetoothConnection(
    private val device: BluetoothDevice,
    private val uuid: UUID,
    private val onReceive: (address: String, message: String) -> Unit
) {
    private var socket: BluetoothSocket? = null
    private var writer: PrintWriter? = null
    private var readerJob: Job? = null

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
            val out = socket!!.outputStream
            val inp = socket!!.inputStream
            writer = PrintWriter(OutputStreamWriter(out), true)
            val reader = BufferedReader(InputStreamReader(inp))
            readerJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    var line: String?
                    while (isActive) {
                        line = reader.readLine() ?: break
                        if (line != null) {
                            onReceive(device.address, line)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("BTConn", "Read loop stopped: ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            close()
            false
        }
    }

    fun send(text: String) {
        try {
            writer?.println(text)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun close() {
        try { readerJob?.cancel() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }
}
