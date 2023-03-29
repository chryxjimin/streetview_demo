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
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import com.google.android.gms.maps.model.StreetViewSource
import kotlinx.android.synthetic.main.fragment_streetview.view.*
import kotlin.math.pow


class StreetviewFragment : Fragment(), OnStreetViewPanoramaReadyCallback {

    var streetViewPanorama: StreetViewPanorama? = null
    private lateinit var compassImageView: ImageView
    private lateinit var gyroscopeSensor: Sensor;
    private lateinit var accelerometerSensor: Sensor;
    private lateinit var gravitySensor: Sensor;
    private lateinit var sensorManager: SensorManager
//    private lateinit var streetViewPanoramaFragment: StreetViewPanoramaFragment
    lateinit var streetViewPanoramaCamera: StreetViewPanoramaCamera
    var isCompassEnabled = false
    var isPhoneStationary = false
    var gravity = FloatArray(3)
    var linearAcceleration = FloatArray(3)


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
                gyroscopeListener
                accelerometerListener
            }

        view.fab.setOnClickListener{navigateToHomePage()}
        return view

    }



    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
//             In this example, alpha is calculated as t / (t + dT),
//             where t is the low-pass filter's time-constant and
//             dT is the event delivery rate.
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

                //the alpha determines how much weight to give to the previous and current accelerometer readings.A larger value of
                //alpha gives more weight to the current reading and less to the previous one, resulting in less smoothing.
                //You can experiment with different values to find the right balance between smoothing and responsiveness.
                val alpha: Float = 0.1f


                var values: FloatArray= event!!.values
                var axisX = event.values[0]
                var axisY = event.values[1]
                var axisZ = event.values[2]

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * axisX
                gravity[1] = alpha * gravity[1] + (1 - alpha) * axisY
                gravity[2] = alpha * gravity[2] + (1 - alpha) * axisZ

                // Remove the gravity contribution with the high-pass filter.
                linearAcceleration[0] = axisX - gravity[0]
                linearAcceleration[1] = axisY - gravity[1]
                linearAcceleration[2] = axisZ - gravity[2]

//                compassImageView.text = ("Accelerometer\n" + "X" + ":" + "$axisX\n" + "Y" + ":" + "$axisY\n" + "Z" + ":" + "$axisZ")

                var magnitude = Math.sqrt(
                    linearAcceleration[0].toDouble().pow(2.0) +
                            linearAcceleration[1].toDouble().pow(2.0) +
                            linearAcceleration[2].toDouble().pow(2.0)
                )

                isPhoneStationary = magnitude < 0.003



            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Ignore
        }
    }

    private val gyroscopeListener = object : SensorEventListener {
        private var previousTimestamp = 0L
        private var previousRotationRate = FloatArray(3)

        override fun onSensorChanged(event: SensorEvent?) {
            var values: FloatArray= event!!.values
            var axisX = event.values[0]
            var axisY = event.values[1]
            var axisZ = event.values[2]


            if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
                // Calculate time delta since last sensor event
                if (isCompassEnabled && !isPhoneStationary) {

                    val currentTimestamp = System.currentTimeMillis()
                    val timeDelta =
                        if (previousTimestamp == 0L) 0 else currentTimestamp - previousTimestamp
                    previousTimestamp = currentTimestamp

                    // Calculate rotation rate
                    val rotationRate = FloatArray(3)
                    for (i in 0..2) {
                        rotationRate[i] =
                            previousRotationRate[i] + event.values[i] * timeDelta / 1000f
                    }
                    previousRotationRate = rotationRate

                    // Calculate new camera bearing

                    val currentCamera = streetViewPanorama?.panoramaCamera ?: return
                    val newBearing =
                        currentCamera.bearing - Math.toDegrees(rotationRate[2].toDouble()).toFloat()
                    val newCamera = StreetViewPanoramaCamera.Builder(currentCamera)
                        .bearing(newBearing)
                        .build()
                    streetViewPanorama?.animateTo(newCamera, 0)
                }
                if (isCompassEnabled && isPhoneStationary) {
                    val bearing = streetViewPanorama?.panoramaCamera?.bearing ?: 0f
                    streetViewPanoramaCamera = StreetViewPanoramaCamera.builder()
                        .zoom(streetViewPanorama?.panoramaCamera?.zoom ?: 0f)
                        .tilt(streetViewPanorama?.panoramaCamera?.tilt ?: 0f)
                        .bearing(bearing - linearAcceleration[0] * 2)
                        .build()
                    streetViewPanorama?.animateTo(streetViewPanoramaCamera, 2000)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    override fun onResume() {
        super.onResume()
        if (accelerometerSensor != null ) {
           sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(gyroscopeListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(gyroscopeListener)
        sensorManager.unregisterListener(accelerometerListener)
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




//
////
////privatevargyroscopeEventListener:SensorEventListener=object:SensorEventListener{
////overridefunonSensorChanged(event:SensorEvent){
//////Log.d("hello",deltaRotationMatrix.joinToString(","))
////
////
////
////varvalues:FloatArray=event!!.values
//////valduration:Long=1000
//////valcamera=StreetViewPanoramaCamera.Builder()
//////.zoom(this@StreetviewFragment.streetViewPanorama?.panoramaCamera!!.zoom)
//////.tilt(this@StreetviewFragment.streetViewPanorama?.panoramaCamera!!.tilt)
//////.bearing(streetViewPanorama!!.panoramaCamera.bearing-60)
//////.build()
//////streetViewPanorama?.animateTo(camera,duration)
//////}elseif(event.values[2]<-0.5f&&event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
////}
////
////overridefunonAccuracyChanged(sensor:Sensor?,accuracy:Int){
////}
////
////}





