package com.example.streetviewapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_streetview.view.*

class StreetviewFragment : Fragment(), OnStreetViewPanoramaReadyCallback {

    var streetViewPanorama: StreetViewPanorama? = null

    override fun onStreetViewPanoramaReady(streetViewPanorama: StreetViewPanorama) {
        this.streetViewPanorama = streetViewPanorama
        //        val location = LatLng(24.267135699699523,153.9999570495186)
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

        val view = inflater.inflate(R.layout.fragment_streetview, container, false)

        val streetViewPanoramaFragment =
            childFragmentManager
                .findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment?
        streetViewPanoramaFragment?.getStreetViewPanoramaAsync(this)

        view.fab.setOnClickListener{navigateToHomePage()}

        return view
    }


    private fun navigateToHomePage() {
        Navigation.findNavController(requireView()).navigate(R.id.action_streetviewFragment_to_mapFragment)
    }


}