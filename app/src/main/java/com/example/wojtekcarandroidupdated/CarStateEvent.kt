package com.example.wojtekcarandroidupdated

import android.bluetooth.BluetoothAdapter

sealed class CarStateEvent {

    data class Connect(val btAdapter: BluetoothAdapter) : CarStateEvent()
    data object Accelerometer : CarStateEvent()
    data object Ultrasonic : CarStateEvent()
    data object LightSensor: CarStateEvent()

}