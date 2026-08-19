package com.example.phoneassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class JoystickFragment : Fragment() {
    private lateinit var address: String
    private val bt by lazy { BluetoothManager.getInstance(requireContext()) }

    companion object {
        private const val ARG_ADDR = "addr"
        fun newInstance(address: String) = JoystickFragment().apply {
            arguments = Bundle().apply { putString(ARG_ADDR, address) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString(ARG_ADDR) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_joystick, container, false)
        val joystick = root.findViewById<JoystickView>(R.id.joystickView)
        val tv = root.findViewById<TextView>(R.id.joyStatus)
        joystick.onMove = { x, y ->
            val ix = (x * 100).toInt()
            val iy = (y * 100).toInt()
            val msg = "JOY:$ix,$iy"
            bt.send(address, msg)
            tv.text = "Sent $msg"
        }
        return root
    }
}
