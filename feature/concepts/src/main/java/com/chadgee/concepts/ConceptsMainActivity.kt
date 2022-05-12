package com.chadgee.concepts

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chadgee.concepts.content.CertificatesActivity

class ConceptsMainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_concepts)

        startActivity(Intent(this, CertificatesActivity::class.java))
        finish()
    }
}