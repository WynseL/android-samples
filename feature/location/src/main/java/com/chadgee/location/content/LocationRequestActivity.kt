package com.chadgee.location.content

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.chadgee.location.databinding.ActivityLocationRequestBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import timber.log.Timber

class LocationRequestActivity: AppCompatActivity() {

    companion object {
        private const val DEBUGGER = " === LocationRequestActivity === %s"

        private const val REQUEST_PERMISSION_CODE = 10
    }

    private var _binding: ActivityLocationRequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var longitudeText: AppCompatTextView
    private lateinit var latitudeText: AppCompatTextView

    private val locationService: LocationService by lazy { LocationService.Builder(this).build() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLocationRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        longitudeText = binding.longitudeText
        latitudeText = binding.latitudeText
    }

    override fun onStart() {
        super.onStart()

        startLocation()
    }

    private fun startLocation() {
        locationService.getCurrentLocation {
            longitudeText.text = it.first.toString()
            latitudeText.text = it.second.toString()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    startLocation()
                }
                Activity.RESULT_CANCELED -> {
                    Timber.e(DEBUGGER, "User cancelled")
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()

        _binding = null
        locationService.cancel()
    }

    /***
     *
     */

    @SuppressLint("MissingPermission")
    class LocationService(private val activity: Activity,
                          private val fusedLocationClient: FusedLocationProviderClient,
                          private val settingsClient: SettingsClient,
                          private val locationRequest: LocationRequest,
                          private val locationSettingsRequest: LocationSettingsRequest) {
        private var locationCallback: LocationCallback? = null

        fun getCurrentLocation(success: (Pair<Double, Double>) -> Unit) {
            settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener {
                    locationCallback = object: LocationCallback() {
                        override fun onLocationResult(p0: LocationResult) {
                            super.onLocationResult(p0)
                            val location = p0.lastLocation
                            success.invoke(location.longitude to location.latitude)
                        }
                    }

                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
                }.addOnFailureListener { exception ->
                    val statusCode = (exception as ApiException).statusCode
                    Timber.e(DEBUGGER, statusCode)
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Timber.e(DEBUGGER, "RESOLUTION_REQUIRED")
                            try {
                                val rae: ResolvableApiException = exception as ResolvableApiException
                                rae.startResolutionForResult(activity, REQUEST_PERMISSION_CODE)
                            } catch (isse: IntentSender.SendIntentException) {
                                Timber.e(isse, DEBUGGER, "")
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            Timber.e(DEBUGGER, "SETTINGS_CHANGE_UNAVAILABLE")
                        }
                    }
                }
        }

        fun cancel() {
            if (locationCallback != null) {
                fusedLocationClient.removeLocationUpdates(locationCallback!!)
                locationCallback = null
            }
        }

        class Builder(private val activity: Activity) {
            private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
            private val settingsClient: SettingsClient = LocationServices.getSettingsClient(activity)
            private val locationRequest: LocationRequest = LocationRequest.create().apply {
                interval = 0L
                fastestInterval = 0L
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            private val locationSettingsRequest: LocationSettingsRequest = LocationSettingsRequest.Builder().apply {
                addLocationRequest(locationRequest)
            }.build()

            fun build(): LocationService {
                return LocationService(activity, fusedLocationClient, settingsClient,
                        locationRequest, locationSettingsRequest)
            }
        }
    }
}