package com.example.easemytrip

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    lateinit var listLocations : ArrayList<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        listLocations = intent.getSerializableExtra("list") as ArrayList<LatLng>
        home.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(listLocations[0]).title("Start"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(listLocations[0]))
        mMap.setMinZoomPreference(12F)

        mMap.addMarker(MarkerOptions().position(listLocations[1]).title("End"))

        mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .add(
                    listLocations[0],
                    listLocations[1]))

    }
}