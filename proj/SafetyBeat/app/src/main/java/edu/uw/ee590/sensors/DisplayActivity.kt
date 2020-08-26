package edu.uw.ee590.sensors

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

import android.view.View
import android.widget.AdapterView
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*
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
import edu.uw.ee590.sensors.BuildConfig.APPLICATION_ID

class DisplayActivity : AppCompatActivity() {

    private lateinit var lat : TextView
    private lateinit var long : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)

        //val position = intent.getIntExtra("KEY",)
    }
}
