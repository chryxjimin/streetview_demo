package com.example.streetviewapp


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.streetviewapp.BuildConfig.MAPS_API_KEY
import com.example.streetviewapp.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_map.view.*
import org.json.JSONObject
import org.json.JSONTokener


class MapFragment : Fragment(), OnMapReadyCallback {


private lateinit var mMap: GoogleMap
private lateinit var binding: FragmentMapBinding

//    val metadataUrl = "https://maps.googleapis.com/maps/api/streetview/metadata?location=24.267135699699523, 153.9999570495186&key=${MAPS_API_KEY}"
val metadataUrl = "https://maps.googleapis.com/maps/api/streetview/metadata?location=34.02395343689089, -84.13859808694289&key=$MAPS_API_KEY"

    //            val imageUrl = "https://maps.googleapis.com/maps/api/streetview?size=400x400&location=24.267135699699523, 153.9999570495186&key=${MAPS_API_KEY}"
    val imageUrl = "https://maps.googleapis.com/maps/api/streetview?size=400x400&location=34.02395343689089, -84.13859808694289&key=$MAPS_API_KEY"


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val location = LatLng(24.267135699699523,153.9999570495186)
        val location = LatLng(34.02395343689089, -84.13859808694289)
        mMap.addMarker(MarkerOptions().position(location).title("Marker"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18f))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        downloadJson()
        return view
//

    }


    fun downloadJson() {
        val queue = Volley.newRequestQueue(this.context)
        val request = StringRequest(Request.Method.GET, metadataUrl ,
            Response.Listener { response ->
                val jsonObject = JSONTokener(response).nextValue() as JSONObject
                val imageView = view?.imageView
                val status = jsonObject.getString("status")
                println(status)
                if (status == "ZERO_RESULTS" ) {
                    imageView?.setVisibility(View.INVISIBLE);
                } else {
                    Picasso.get()
                        .load(imageUrl)
                        .resize(100,100)
                        .into(imageView)
                    imageView?.setOnClickListener {viewStreetView()}
                }


            }, Response.ErrorListener {  })
        queue.add(request)
    }


    private fun viewStreetView() {
        Navigation.findNavController(requireView()).navigate(R.id.action_mapFragment_to_streetviewFragment)
    }


}





