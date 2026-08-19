package com.example.phoneassistant

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var btnRefresh: Button
    private val btManager by lazy { BluetoothManager.getInstance(this) }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.all { it }
        if (!granted) Toast.makeText(this, "Permissions required for Bluetooth", Toast.LENGTH_LONG).show()
        else loadPairedDevices()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listView = findViewById(R.id.deviceList)
        btnRefresh = findViewById(R.id.btnRefresh)

        btnRefresh.setOnClickListener { loadPairedDevices() }

        ensurePermissions()
        setupListClick()
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

    private fun loadPairedDevices() {
        val paired = btManager.getPairedHC05Devices()
        val names = paired.map { "${it.name ?: "Unknown"} — ${it.address}" }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
    }

    private fun setupListClick() {
        listView.setOnItemClickListener { _, _, position, _ ->
            val device = btManager.getPairedHC05Devices()[position]
            showRoleChooser(device)
        }
    }

    private fun showRoleChooser(device: BluetoothDevice) {
        val roles = arrayOf("Joystick", "Visualizer", "Matrix", "Numpad", "DeviceInfo")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Assign role to ${device.name ?: device.address}")
        builder.setItems(roles) { _, which ->
            val role = roles[which]
            btManager.connect(device) { address, ok ->
                runOnUiThread {
                    if (!ok) {
                        Toast.makeText(this, "Failed to connect to $address", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    when (role) {
                        "Joystick" -> openFragment(JoystickFragment.newInstance(device.address))
                        "Visualizer" -> openFragment(VisualizerFragment.newInstance(device.address))
                        "Matrix" -> openFragment(MatrixFragment.newInstance(device.address))
                        "Numpad" -> openFragment(NumpadFragment.newInstance(device.address))
                        "DeviceInfo" -> openFragment(DeviceInfoFragment.newInstance(device.address))
                    }
                }
            }
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
        CoroutineScope(Dispatchers.IO).launch {
            btManager.disconnectAll()
        }
    }
}
