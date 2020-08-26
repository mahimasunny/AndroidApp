package com.example.myapplication

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun startMap(v: View){
        val intent = Intent(this, MapsActivity::class.java).apply {}
        startActivity(intent)
    }

}

