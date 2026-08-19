package com.example.phoneassistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class MatrixFragment : Fragment() {
    private lateinit var address: String
    private val bt by lazy { BluetoothManager.getInstance(requireContext()) }

    companion object {
        private const val ARG_ADDR = "addr"
        fun newInstance(address: String) = MatrixFragment().apply {
            arguments = Bundle().apply { putString(ARG_ADDR, address) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        address = arguments?.getString(ARG_ADDR) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_matrix, container, false)
        val edit = root.findViewById<EditText>(R.id.matrixInput)
        val btn = root.findViewById<Button>(R.id.matrixSend)
        btn.setOnClickListener {
            val text = edit.text.toString()
            if (text.isNotBlank()) {
                bt.send(address, "MATRIX:$text")
            }
        }
        return root
    }
}
