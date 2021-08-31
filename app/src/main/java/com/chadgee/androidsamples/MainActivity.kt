package com.chadgee.androidsamples

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chadgee.androidsamples.databinding.ActivityMainBinding
import com.chadgee.location.LocationMainActivity

class MainActivity : AppCompatActivity(), FeaturesAdapter.FeaturesAdapterListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var featureList: RecyclerView
    private lateinit var featuresAdapter: FeaturesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeRecyclerView()
    }

    private fun initializeRecyclerView() {
        featureList = binding.listFeatures
        featuresAdapter = FeaturesAdapter(this)
        featureList.layoutManager = GridLayoutManager(this, 2)
        featureList.adapter = featuresAdapter
        featuresAdapter.setFeatures(list)
    }

    override fun onFeatureItemClick(featureItem: FeatureItem) {
        val intent = Intent(this, featureItem.clazzToIntent)
        startActivity(intent)
    }

    private val list by lazy {
        listOf(
                FeatureItem(getString(R.string.title_location),
                        getString(R.string.description_location),
                        LocationMainActivity::class.java),
        )
    }
}