package com.example.streetviewapp

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.Matrix
import android.os.Bundle
import android.util.Half.EPSILON
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.maps.StreetViewPanoramaView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.StreetViewPanoramaCamera
import com.google.android.gms.maps.model.StreetViewPanoramaLocation
import com.google.android.gms.maps.model.StreetViewSource
import kotlinx.android.synthetic.main.fragment_streetview.*
import kotlinx.android.synthetic.main.fragment_streetview.view.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class StreetviewFragment : Fragment(), OnStreetViewPanoramaReadyCallback{

    private lateinit var streetViewPanoramaFragment: StreetViewPanoramaFragment
    private lateinit var sensorManager: SensorManager
    var streetViewPanorama: StreetViewPanorama? = null
    private lateinit var compassImageView: TextView
    private lateinit var gyroscopeSensor: Sensor;
    lateinit var streetViewPanoramaCamera: StreetViewPanoramaCamera


    override fun onStreetViewPanoramaReady(streetViewPanorama: StreetViewPanorama) {
        this.streetViewPanorama = streetViewPanorama

        val location = LatLng(34.02395343689089, -84.13859808694289)

        streetViewPanorama.setPosition(location)
        val streetView = StreetViewPanoramaFragment.newInstance(
            StreetViewPanoramaOptions().position(location)
        )

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)


//        gyroscope()

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


        view.fab.setOnClickListener{navigateToHomePage()}
        return view

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



