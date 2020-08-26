package com.ece590.sendsms

import android.support.v7.app.AppCompatActivity
import android.support.v4.app.ActivityCompat
import android.os.Bundle
import android.os.Build
import android.widget.Button
import android.support.v4.content.ContextCompat
import android.widget.EditText
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.text.TextUtils
import android.widget.Toast
import android.content.Intent
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this@MainActivity, SendSMSActivity::class.java)
        startActivity(intent)
    }

}
