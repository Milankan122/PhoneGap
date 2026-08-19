package com.example.phoneassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class DeviceInfoFragment : Fragment() {
    private lateinit var address: String
    private val bt by lazy { BluetoothManager.getInstance(requireContext()) }

    companion object {
        private const val ARG_ADDR = "addr"
        fun newInstance(address: String) = DeviceInfoFragment().apply {
            arguments = Bundle().apply { putString(ARG_ADDR, address) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString(ARG_ADDR) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_deviceinfo, container, false)
        val tv = root.findViewById<TextView>(R.id.infoText)
        btManagerRegister(tv)
        // Request current device info
        bt.send(address, "REQUEST:I2C")
        return root
    }

    private fun btManagerRegister(tv: TextView) {
        BluetoothManager.getInstance(requireContext()).registerReceiver { addr, msg ->
            if (addr == address) {
                activity?.runOnUiThread {
                    tv.text = msg
                }
            }
        }
    }
}
