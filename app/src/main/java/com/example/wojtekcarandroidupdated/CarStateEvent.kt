package com.example.wojtekcarandroidupdated

import android.bluetooth.BluetoothAdapter
import android.hardware.SensorManager

sealed class CarStateEvent {

    data class Connect(val btAdapter: BluetoothAdapter) : CarStateEvent()
    data class Accelerometer(val sensorManager: SensorManager) : CarStateEvent()
    data object Ultrasonic : CarStateEvent()
    data object LightSensor: CarStateEvent()

}