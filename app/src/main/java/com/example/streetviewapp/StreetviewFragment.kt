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
import kotlinx.android.synthetic.main.fragment_streetview.view.*


class StreetviewFragment : Fragment(), OnStreetViewPanoramaReadyCallback, SensorEventListener {

    private lateinit var sensorManager: SensorManager
    var streetViewPanorama: StreetViewPanorama? = null
    private lateinit var compassImageView: TextView
    private lateinit var sensorList: List <Sensor>;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_streetview, container, false)

        val streetViewPanoramaFragment =
            childFragmentManager
                .findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment?

        streetViewPanoramaFragment?.getStreetViewPanoramaAsync(this)

        view.fab.setOnClickListener{navigateToHomePage()}

        compassImageView = view?.findViewById(R.id.compass_button)!!
        this.sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        printSensors()

        return view
    }

    private fun printSensors() {
        for(sensor in sensorList) {
            println("\n" + sensor.getName())
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


    private fun navigateToHomePage() {
        Navigation.findNavController(requireView()).navigate(R.id.action_streetviewFragment_to_mapFragment)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        TODO("Not yet implemented")
    }



}
