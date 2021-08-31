package com.chadgee.location

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chadgee.location.content.FusedLocationProviderActivity
import com.chadgee.location.content.LocationRequestActivity

class LocationMainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_location)

        startActivity(Intent(this, LocationRequestActivity::class.java))
        finish()
    }
}