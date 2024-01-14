package com.example.wojtekcarandroidupdated

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.example.wojtekcarandroidupdated.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val carViewModel: CarViewModel by viewModels()
    lateinit var binding: ActivityMainBinding
    lateinit var btAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this@MainActivity

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bluetoothManager.adapter

        subscribeObservers()
        actionsOnClick()
    }

    private fun subscribeObservers() {
        carViewModel.viewState.observe(this) { state ->
            state.toast?.let {
                Toast.makeText(this, "${state.toast}", Toast.LENGTH_LONG).show()
            }
            state.buttonConnect?.let {
                binding.connect.text = state.buttonConnect
            }
        }
    }

    private fun actionsOnClick() {
        binding.connect.setOnClickListener {
            carViewModel.onTriggerEvent(
                CarStateEvent.Connect(
                    btAdapter
                )
            )
        }
        binding.accelerometer.setOnClickListener {
            carViewModel.onTriggerEvent(
                CarStateEvent.Accelerometer) }
    }
}
