package com.poly

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.widget.Toast
import android.location.Geocoder
import android.view.View
import android.widget.EditText
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var instance: GetLocation
    private var lat = 0.0
    private var lng = 0.0
    private var address = ""
    var ischeck=false


    /* recevie Broadcast lattude and longtude*/
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent?) {

           // mMap.clear()
            if (intent != null && !ischeck) {
                lat = intent.getDoubleExtra("lat", 0.0)
                lng = intent.getDoubleExtra("lng", 0.0)
                address = intent.getStringExtra("add")

                val sydney = LatLng(lat, lng)
                mMap.addMarker(MarkerOptions().position(sydney).title(address))
                pointToPosition(sydney)

                //tv_location_result.alpha=0f
                // tv_location_result.animate().alpha(1f).duration=200
                //tv_location_result.text=" Lat : $lat  Lat : $lng "
            }
        }
    }


    private fun pointToPosition(position: LatLng) {
        val cameraPosition = CameraPosition.Builder()
                .target(position)
                .zoom(12f).build()
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        initBroadCastMap()
        instance.startLocation()



    }




    private fun initBroadCastMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        instance = GetLocation(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter("key_action"))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            GetLocation.REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> Log.e("TAG", "User agreed to make required location settings changes.")
                Activity.RESULT_CANCELED -> {
                    Log.e("TAG", "User chose not to make required location settings changes.")
                    instance.mRequestingLocationUpdates = false
                }
            }// Nothing to do. startLocationupdates() gets called in onResume again.
        }
    }

    public override fun onResume() {
        super.onResume()
        if (instance.mRequestingLocationUpdates!! && instance.checkPermissions()) {
            instance.startLocationUpdates()
        }
        instance.updateLocationUI()
    }

    override fun onPause() {
        super.onPause()
        if (instance.mRequestingLocationUpdates!!) {
            instance.stopLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //  unregisterReceiver(receiver)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.mMap = googleMap;
        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style);
        this.mMap.setMapStyle(style);
        // We will provide our own zoom controls.
        mMap.uiSettings.isZoomControlsEnabled = false;
        mMap.uiSettings.isMyLocationButtonEnabled = true;
    }




    //Button..............Click Serarch.............

    @SuppressLint("ShowToast")
    fun onMapSearch(view: View) {
        ischeck=true

        val locationSearch = findViewById<EditText>(R.id.edit_text)
        val location = locationSearch.text.toString()

        if (location.isNotEmpty()) {
            var addressList: List<Address>? = null
            val geocoder = Geocoder(this)
            try {
                addressList = geocoder.getFromLocationName(location, 1)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            val address = addressList!!.get(0)
            val latLng = LatLng(address.latitude, address.longitude)

            MapMaker(address.getAddressLine(0),latLng)
            locationSearch.setText("")


        } else
            Toast.makeText(applicationContext, "Please enter city", Toast.LENGTH_SHORT)
    }


    private fun MapMaker(address: String,latLng:LatLng) {
        mMap.addMarker(MarkerOptions()
                .position(latLng)
                .title("Address")
                .snippet(address)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.pin_location_pin)))

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10f), 2000, null)

    }
}
