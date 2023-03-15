

package com.example.streetviewapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import com.google.android.gms.maps.model.StreetViewSource
import kotlinx.android.synthetic.main.fragment_streetview.*
import kotlinx.android.synthetic.main.fragment_streetview.view.*


class StreetviewFragment : Fragment(), OnStreetViewPanoramaReadyCallback {

//    private var panorama: StreetViewPanorama? = null
    var streetViewPanorama: StreetViewPanorama? = null
    private lateinit var compassImageView: TextView
    private lateinit var gyroscopeSensor: Sensor;
    private lateinit var accelerometerSensor: Sensor;
    private lateinit var sensorManager: SensorManager
    private lateinit var streetViewPanoramaFragment: StreetViewPanoramaFragment
    lateinit var streetViewPanoramaCamera: StreetViewPanoramaCamera
    var isCompassEnabled = false


        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager


            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

            val view = inflater.inflate(R.layout.fragment_streetview, container, false)

        val streetViewPanoramaFragment =
            childFragmentManager
                .findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment?

        streetViewPanoramaFragment?.getStreetViewPanoramaAsync { panorama: StreetViewPanorama ->
            streetViewPanorama = panorama
            panorama.isStreetNamesEnabled = true
            panorama.isUserNavigationEnabled = true
            panorama.isZoomGesturesEnabled = true
            panorama.isPanningGesturesEnabled = true

            savedInstanceState ?: setPosition()
        }

        compassImageView = view?.findViewById(R.id.compass_button)!!
            compassImageView.setOnClickListener {
                isCompassEnabled = !isCompassEnabled
            }

        view.fab.setOnClickListener{navigateToHomePage()}
        return view

    }

    override fun onResume() {
        super.onResume()

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(gyroscopeListener)
        sensorManager.unregisterListener(accelerometerListener)
    }

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            // Ignore
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Ignore
        }
    }

    private val gyroscopeListener = object : SensorEventListener {
        private var previousTimestamp = 0L
        private var previousRotationRate = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE && isCompassEnabled == true) {
                // Calculate time delta since last sensor event
                val currentTimestamp = System.currentTimeMillis()
                val timeDelta = if (previousTimestamp == 0L) 0 else currentTimestamp - previousTimestamp
                previousTimestamp = currentTimestamp

                // Calculate rotation rate
                val rotationRate = FloatArray(3)
                for (i in 0..2) {
                    rotationRate[i] = previousRotationRate[i] + event.values[i] * timeDelta / 1000f
                }
                previousRotationRate = rotationRate

                // Calculate new camera bearing

                val currentCamera = streetViewPanorama?.panoramaCamera ?: return
                val newBearing = currentCamera.bearing - Math.toDegrees(rotationRate[2].toDouble()).toFloat()
                val newCamera = StreetViewPanoramaCamera.Builder(currentCamera)
                    .bearing(newBearing)
                    .build()
                streetViewPanorama?.animateTo(newCamera, 0)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }


        override fun onStreetViewPanoramaReady(streetViewPanorama: StreetViewPanorama) {
            this.streetViewPanorama = streetViewPanorama

            val location = LatLng(34.02395343689089, -84.13859808694289)

            streetViewPanorama.setPosition(location)
            val streetView = StreetViewPanoramaFragment.newInstance(
                StreetViewPanoramaOptions().position(location)
            )

        }

        private fun setPosition() {
            streetViewPanorama?.setPosition(
                SAN_FRAN,
                RADIUS,
                if (true) StreetViewSource.OUTDOOR else StreetViewSource.DEFAULT
            )
        }

    private fun navigateToHomePage() {
        Navigation.findNavController(requireView()).navigate(R.id.action_streetviewFragment_to_mapFragment)
    }

    companion object {
        // Cole St, San Fran
        private var SAN_FRAN = LatLng(37.765927, -122.449972)
        private const val RADIUS = 20
    }
}




