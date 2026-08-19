package com.example.phoneassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class VisualizerFragment : Fragment() {
    private lateinit var address: String
    private val bt by lazy { BluetoothManager.getInstance(requireContext()) }

    companion object {
        private const val ARG_ADDR = "addr"
        fun newInstance(address: String) = VisualizerFragment().apply {
            arguments = Bundle().apply { putString(ARG_ADDR, address) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString(ARG_ADDR) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_visualizer, container, false)
        val tv = root.findViewById<TextView>(R.id.visStatus)
        val btnRainbow = root.findViewById<Button>(R.id.btnRainbow)
        val btnOff = root.findViewById<Button>(R.id.btnOff)
        btnRainbow.setOnClickListener {
            bt.send(address, "LED:rainbow")
            tv.text = "Sent LED:rainbow"
        }
        btnOff.setOnClickListener {
            bt.send(address, "LED:off")
            tv.text = "Sent LED:off"
        }
        return root
    }
}
