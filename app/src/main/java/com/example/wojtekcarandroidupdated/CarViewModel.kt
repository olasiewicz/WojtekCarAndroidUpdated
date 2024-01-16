package com.example.wojtekcarandroidupdated

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
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

    private val RFCOMM_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    private val BLUETOOTH_MAC = "00:13:03:21:06:14"  //btAdapter.bondedDevices
    private var booleanConnect = false
    private var booleanUltrasonic = false
    private var booleanAccelerometer = false
    private var booleanLight = false
    private var x = 0
    private var y = 0

    private lateinit var btDevice: BluetoothDevice
    private lateinit var btSocket: BluetoothSocket
    private lateinit var outputStream: OutputStream
    private lateinit var gSensorEventListener: SensorEventListener

    private val _viewState: MutableLiveData<CarViewState> = MutableLiveData()
    val viewState: LiveData<CarViewState>
        get() = _viewState

    fun onTriggerEvent(event: CarStateEvent) {

        when (event) {
            is CarStateEvent.Connect -> {
                connectVehicle(event.btAdapter)
            }

            is CarStateEvent.Accelerometer -> {
                connectAccelerometer(event.sensorManager)
            }

            is CarStateEvent.Ultrasonic -> {
                connectUltrasonic()
            }

            is CarStateEvent.LightSensor -> {
                connectLight()
            }
        }

    }


    @SuppressLint("MissingPermission")
    private fun connectVehicle(btAdapter: BluetoothAdapter) {

        if (!btAdapter.isEnabled) {
            _viewState.value = CarViewState(toast = "Turn On Bluetooth first")
            return
        }

        if (!booleanConnect) {
            btDevice = btAdapter.getRemoteDevice(BLUETOOTH_MAC) //btAdapter.bondedDevices
            try {
                btSocket = btDevice.createRfcommSocketToServiceRecord(
                    UUID.fromString(RFCOMM_UUID)
                )
                btSocket.connect()
                _viewState.value = CarViewState(toast = "Connected")
                _viewState.value = CarViewState(tvAccelerometer = "${btAdapter.bondedDevices}")
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

    private fun connectAccelerometer(sensorManager: SensorManager) {
        if (!booleanConnect) {
            _viewState.value = CarViewState(toast = "Need to Connect with Vehicle first")
            return
        }
        if (booleanAccelerometer) {
            _viewState.value = CarViewState(buttonAccelerometer = "Accelerometer Off")
            booleanAccelerometer = false
            stopAccelerometer(sensorManager)
            return
        }
        if (booleanUltrasonic || booleanLight) {
            _viewState.value = CarViewState(toast = "Disconnect another module first")
            return
        }
        _viewState.value = CarViewState(buttonAccelerometer = "Accelerometer On")
        booleanAccelerometer = true
        startAccelerometer(sensorManager)
    }

    private fun startAccelerometer(sensorManager: SensorManager) {

        sendData("p")
        sendData("A")
        gSensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                y = Math.round(event.values[0])
                x = Math.round(event.values[1])

                if (y <= -3 && y > -6) {
                    when {
                        x in -2..2 -> commands(Control.FORWARD)
                        x in -7..-3 -> commands(Control.LEFT)
                        x <= -8 -> commands(Control.FAST_LEFT)
                        x in 3..7 -> commands(Control.RIGHT)
                        else -> commands(Control.FAST_RIGHT)
                    }
                }
                if (y <= -6) {
                    when {
                        x in -2..2 -> commands(Control.FAST_FORWARD)
                        x in -7..-3 -> commands(Control.LEFT)
                        x <= -8 -> commands(Control.FAST_LEFT)
                        x in 3..7 -> commands(Control.RIGHT)
                        else -> commands(Control.FAST_RIGHT)
                    }
                }
                if (y in 0..2 || y <= 0 && y > -3) {
                    when {
                        x in 0..2 || x in -2..0 -> commands(Control.STOP)
                        x in -7..-3 -> commands(Control.LEFT)
                        x <= -8 -> commands(Control.FAST_LEFT)
                        x in 3..7 -> commands(Control.RIGHT)
                        else -> commands(Control.FAST_RIGHT)
                    }
                }
                if (y >= 3) {
                    when {
                        x in 0..2 || x in -2..0 -> commands(Control.BACKWARD)
                        x in -7..-3 -> commands(Control.LEFT_B)
                        x <= -8 -> commands(Control.FAST_LEFT_B)
                        x in 3..7 -> commands(Control.RIGHT_B)
                        else -> commands(Control.FAST_RIGHT_B)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        sensorManager.registerListener(
            gSensorEventListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun stopAccelerometer(sensorManager: SensorManager) {
        sensorManager.unregisterListener(gSensorEventListener)
        commands(Control.PAUSE)
        _viewState.value = CarViewState(tvAccelerometer = "")
    }

    private fun sendData(message: String, value: String = "") {
        val command = message.toByteArray()
        try {
            outputStream = btSocket.outputStream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            outputStream.write(command)
            if (booleanAccelerometer) _viewState.value = CarViewState(tvAccelerometer = "$value, x: $x, y: $y")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun commands(controlValue: Control) {
        when (controlValue) {
            Control.BACKWARD -> sendData("b")
            Control.FORWARD -> sendData("f")
            Control.FAST_FORWARD -> sendData("F")
            Control.RIGHT -> sendData("r")
            Control.RIGHT_B -> sendData(">")
            Control.FAST_RIGHT -> sendData("R")
            Control.FAST_RIGHT_B -> sendData("}")
            Control.LEFT -> sendData("l")
            Control.FAST_LEFT -> sendData("L")
            Control.LEFT_B -> sendData("<")
            Control.FAST_LEFT_B -> sendData("{")
            Control.STOP -> sendData("s")
            Control.PAUSE -> sendData("X")
        }
    }


    enum class Control {
        FORWARD,
        FAST_FORWARD,
        BACKWARD,
        RIGHT,
        FAST_RIGHT,
        LEFT,
        FAST_LEFT,
        LEFT_B,
        FAST_LEFT_B,
        RIGHT_B,
        FAST_RIGHT_B,
        STOP,
        PAUSE
    }


    private fun connectUltrasonic() {
        if (!booleanConnect) {
            _viewState.value = CarViewState(toast = "Need to Connect with Vehicle first")
            return
        }
        if (!booleanUltrasonic) {
            if (booleanAccelerometer || booleanLight) {
                _viewState.value = CarViewState(toast = "Disconnect another module first")
                return
            }
            startUltrasonic()
            _viewState.value = CarViewState(buttonUltrasonic = "Ultrasonic Sensor On")
            booleanUltrasonic = true
        } else {
            stop()
            _viewState.value = CarViewState(buttonUltrasonic = "Ultrasonic Sensor Off")
            booleanUltrasonic = false
        }
    }

    private fun startUltrasonic() {
        sendData("p")
        sendData("U")
    }

    private fun connectLight() {

        if (!booleanConnect) {
            _viewState.value = CarViewState(toast = "Need to Connect with Vehicle first")
            return
        }
        if (!booleanLight) {
            if (booleanUltrasonic || booleanAccelerometer) {
                _viewState.value = CarViewState(toast = "Disconnect another module first")
                return
            }
            startLight()
            _viewState.value = CarViewState(buttonLight = "Light Sensor On")
            booleanLight = true
        } else {
            stop()
            _viewState.value = CarViewState(buttonLight = "Light Sensor Off")
            booleanLight = false
        }
    }

    private fun startLight() {
        sendData("p")
        sendData("S")
    }

    private fun stop() {
        sendData("X")
    }

}