package com.example.wojtekcarandroidupdated

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CarViewModel @Inject constructor() : ViewModel() {

    private var booleanConnect = false
    private var booleanUltrasonic = false
    private var booleanAccelerometer = false
    private var booleanLight = false

    lateinit var nxtDevice: BluetoothDevice
    lateinit var btSocket: BluetoothSocket
    lateinit var outputStream: OutputStream
    private val beetle_id = "00:13:03:21:06:14"

    private val _viewState: MutableLiveData<CarViewState> = MutableLiveData()
    val viewState: LiveData<CarViewState>
        get() = _viewState

    fun onTriggerEvent(event: CarStateEvent) {

        when (event) {
            is CarStateEvent.Connect -> {
                connectVehicle(event.btAdapter)
            }

            is CarStateEvent.Accelerometer -> {
                connectAccelerometer()
            }

            is CarStateEvent.Ultrasonic -> {}
            is CarStateEvent.LightSensor -> {}
        }

    }


    @SuppressLint("MissingPermission")
    private fun connectVehicle(btAdapter: BluetoothAdapter) {

        if (!btAdapter.isEnabled) {
            _viewState.value = CarViewState(toast = "Turn On Bluetooth first")
            return
        }

        if (!booleanConnect) {
            nxtDevice = btAdapter.getRemoteDevice(beetle_id)
            try {
                btSocket = nxtDevice.createRfcommSocketToServiceRecord(
                    UUID
                        .fromString("00001101-0000-1000-8000-00805F9B34FB")
                )
                btSocket.connect()
                _viewState.value = CarViewState(toast = "Connected")
            } catch (e: IOException) {
                _viewState.value = CarViewState(toast = "Not Connected")
                return
            }
            booleanConnect = true
            _viewState.value = CarViewState(buttonConnect = "Connected")
        } else {
            if (booleanUltrasonic || booleanAccelerometer
                || booleanLight
            ) {
                _viewState.value = CarViewState(toast = "Disconnect another module first")
                return
            }
            try {
                btSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            _viewState.value = CarViewState(buttonConnect = "Connect")
            _viewState.value = CarViewState(toast = "Disconnected")
            booleanConnect = false
        }
    }

    private fun connectAccelerometer() {
        if (!booleanConnect) {
            _viewState.value = CarViewState(toast = "Need to Connect with Vehicle first")
            return
        }
        if (booleanAccelerometer) {
            _viewState.value = CarViewState(buttonAccelerometer = "Accelerometer Off")
            booleanAccelerometer = false
            stopAccelerometer()
            return
        }
        if (booleanUltrasonic || booleanLight) {
            _viewState.value = CarViewState(toast = "Disconnect another module first")
            return
        }
        _viewState.value = CarViewState(buttonAccelerometer = "Accelerometer On")
        booleanAccelerometer = true
        startAccelerometer()
    }

    private fun startAccelerometer() {

    }


    private fun stopAccelerometer() {
        TODO("Not yet implemented")
    }


}