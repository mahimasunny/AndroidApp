package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import java.util.*
import kotlin.collections.ArrayList
import android.location.LocationManager
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.myapplication.BuildConfig.APPLICATION_ID



class MapsActivity : AppCompatActivity(), OnMapReadyCallback
{
    private lateinit var mMap: GoogleMap //Create an instace of my map so that I can refer to it in my app

    private val TAG = "MainActivity"
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var myLat : Double = 0.0
    private var myLng : Double = 0.0

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private lateinit var fusedLocationClient: FusedLocationProviderClient



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //set the map to hybrid mode
        getMyLocationWithLocationAPI()
    }

    /*
       * Uses Google PS LOcation API to get the Latitude and Longitude
       */
//    fun getMyLocation(v:View) {
//        getMyLocationWithLocationAPI()
//    }

    fun getMyLocationWithLocationAPI(){
        //OPTION 2: USE GOOGLE PLAY SERVICES LOCATION API

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            getLastLocation()
        }
    }



    //  Provides a simple way of getting a device's location

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.lastLocation
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {

                    myLat = task.getResult()!!.latitude
                    myLng = task.getResult()!!.longitude
//                    Toast.makeText(this, "Latitude " + myLat.toString(), Toast.LENGTH_LONG).show()
//                    Toast.makeText(this, "Longitude "+myLng.toString(), Toast.LENGTH_LONG).show()
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(myLat,myLng)))
                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(myLat,myLng))
                            .title("Marker")
                    )

                } else {
                    Log.w(TAG, "getLastLocation:exception", task.exception)

                }
            }
    }



    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
        } else {
            Log.i(TAG, "Requesting permission")
            startLocationPermissionRequest()
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray   ) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                // Permission granted.
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> getLastLocation()
                // Permission denied.
                else -> {
                    View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    fun getCoordinates(view: View) {
        latitude.setText("Latitude: " + myLat.toString())
        longitude.setText("Longitude: "+myLng.toString())
    }
}