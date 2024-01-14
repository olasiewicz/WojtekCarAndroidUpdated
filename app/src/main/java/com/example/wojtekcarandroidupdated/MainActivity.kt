package com.example.wojtekcarandroidupdated

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
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
    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this@MainActivity

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = bluetoothManager.adapter

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
//        sensorManager.registerListener(
//            gSensorEventListener,
//            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//            SensorManager.SENSOR_DELAY_NORMAL
//        )

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
            state.buttonAccelerometer?.let {
                binding.accelerometer.text = state.buttonAccelerometer
            }
            state.buttonUltrasonic?.let {
                binding.ultrasonic.text = state.buttonUltrasonic
            }
            state.buttonLight?.let {
                binding.light.text = state.buttonLight
            }
            state.tvAccelerometer?.let {
                binding.tvAccelerometer.text = state.tvAccelerometer
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
                CarStateEvent.Accelerometer(
                    sensorManager
                )
            )
        }
        binding.ultrasonic.setOnClickListener {
            carViewModel.onTriggerEvent(
                CarStateEvent.Ultrasonic
            )
        }
    }
}
