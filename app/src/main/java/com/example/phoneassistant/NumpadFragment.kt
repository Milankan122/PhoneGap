package com.example.phoneassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class NumpadFragment : Fragment() {
    private lateinit var address: String
    private val bt by lazy { BluetoothManager.getInstance(requireContext()) }

    companion object {
        private const val ARG_ADDR = "addr"
        fun newInstance(address: String) = NumpadFragment().apply {
            arguments = Bundle().apply { putString(ARG_ADDR, address) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString(ARG_ADDR) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_numpad, container, false)
        val tv = root.findViewById<TextView>(R.id.numStatus)
        val buttons = listOf(
            root.findViewById<Button>(R.id.btn0),
            root.findViewById<Button>(R.id.btn1),
            root.findViewById<Button>(R.id.btn2),
            root.findViewById<Button>(R.id.btn3),
            root.findViewById<Button>(R.id.btn4),
            root.findViewById<Button>(R.id.btn5),
            root.findViewById<Button>(R.id.btn6),
            root.findViewById<Button>(R.id.btn7),
            root.findViewById<Button>(R.id.btn8),
            root.findViewById<Button>(R.id.btn9)
        )
        buttons.forEachIndexed { i, b ->
            b.setOnClickListener {
                bt.send(address, "NUM:$i")
                tv.text = "Sent NUM:$i"
            }
        }
        return root
    }
}
