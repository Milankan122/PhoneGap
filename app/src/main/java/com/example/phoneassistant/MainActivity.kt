package com.example.phoneassistant

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var btnJoystick: Button
    private lateinit var btnDisplay: Button
    private lateinit var btnNumpad: Button
    private lateinit var statusJoy: TextView
    private lateinit var statusDisp: TextView
    private lateinit var statusNum: TextView

    private val btManager by lazy { BluetoothManager.getInstance(this) }
    private val roleMap = mutableMapOf<String, String>() // role -> deviceAddress

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.all { it }
        if (!granted) Toast.makeText(this, "Permissions required for Bluetooth", Toast.LENGTH_LONG).show()
        else autoAssignDevices()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnJoystick = findViewById(R.id.btnJoystick)
        btnDisplay = findViewById(R.id.btnDisplay)
        btnNumpad = findViewById(R.id.btnNumpad)
        statusJoy = findViewById(R.id.statusJoy)
        statusDisp = findViewById(R.id.statusDisp)
        statusNum = findViewById(R.id.statusNum)

        btnJoystick.setOnClickListener { openOrSelect("Joystick") }
        btnDisplay.setOnClickListener { openOrSelect("Display") }
        btnNumpad.setOnClickListener { openOrSelect("Numpad") }

        ensurePermissions()
        // register to receive messages (optional) to show incoming messages for debugging
        btManager.registerReceiver { addr, msg ->
            // show toast for now
            runOnUiThread {
                Toast.makeText(this, "From $addr: $msg", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ensurePermissions() {
        val required = mutableListOf<String>()
        required.add(Manifest.permission.BLUETOOTH)
        required.add(Manifest.permission.BLUETOOTH_ADMIN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            required.add(Manifest.permission.BLUETOOTH_CONNECT)
            required.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) required.add(Manifest.permission.ACCESS_FINE_LOCATION)

        requestPermissionsLauncher.launch(required.toTypedArray())
    }

    private fun autoAssignDevices() {
        val paired = btManager.getPairedHC05Devices()
        // look for names containing JOY / DISP / NUM (case-insensitive) and prefer HC or HC- prefixes
        paired.forEach { device ->
            val name = device.name ?: ""
            val nameUpper = name.uppercase()
            when {
                nameUpper.contains("JOY") -> connectAsRole(device, "Joystick")
                nameUpper.contains("DISP") || nameUpper.contains("DISPLAY") -> connectAsRole(device, "Display")
                nameUpper.contains("NUM") || nameUpper.contains("KEYPAD") || nameUpper.contains("NUMPAD") -> connectAsRole(device, "Numpad")
            }
        }
    }

    private fun connectAsRole(device: BluetoothDevice, role: String) {
        btManager.connect(device) { address, ok ->
            runOnUiThread {
                if (ok) {
                    roleMap[role] = address
                    updateStatus(role, true, device.name ?: address)
                } else {
                    updateStatus(role, false, "Connection failed")
                }
            }
        }
    }

    private fun updateStatus(role: String, connected: Boolean, label: String) {
        when (role) {
            "Joystick" -> {
                statusJoy.text = if (connected) "Connected: $label" else "Not connected"
            }
            "Display" -> {
                statusDisp.text = if (connected) "Connected: $label" else "Not connected"
            }
            "Numpad" -> {
                statusNum.text = if (connected) "Connected: $label" else "Not connected"
            }
        }
    }

    private fun openOrSelect(role: String) {
        val addr = roleMap[role]
        if (addr != null) {
            // open fragment
            when (role) {
                "Joystick" -> openFragment(JoystickFragment.newInstance(addr))
                "Display" -> openFragment(MatrixFragment.newInstance(addr))
                "Numpad" -> openFragment(NumpadFragment.newInstance(addr))
            }
            return
        }
        // not assigned yet: show dialog to choose from paired HC-05 devices
        val paired = btManager.getPairedHC05Devices()
        if (paired.isEmpty()) {
            Toast.makeText(this, "No paired HC-05 devices found. Pair modules in system Bluetooth settings.", Toast.LENGTH_LONG).show()
            return
        }
        val names = paired.map { "${it.name ?: "Unknown"} — ${it.address}" }.toTypedArray()
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select device for $role")
        builder.setItems(names) { _, which ->
            val device = paired[which]
            connectAsRole(device, role)
        }
        builder.show()
    }

    private fun openFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        CoroutineScope(Dispatchers.IO).launch { btManager.disconnectAll() }
    }
}
