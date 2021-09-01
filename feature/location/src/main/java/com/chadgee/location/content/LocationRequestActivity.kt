package com.chadgee.location.content

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.chadgee.location.R
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
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.lang.Exception

class LocationRequestActivity: AppCompatActivity() {

    companion object {
        private const val DEBUGGER = " === LocationRequestActivity === %s"

        private const val REQUEST_PERMISSION_CODE = 10
    }

    private var _binding: ActivityLocationRequestBinding? = null
    private val binding get() = _binding!!

    private lateinit var longitudeText: AppCompatTextView
    private lateinit var latitudeText: AppCompatTextView

    private val locationService: LocationService by lazy {
        LocationService.Builder(this)
                .setInterval(5)
                .setNumUpdates(1)
                .build()
    }

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
        locationService.getCurrentLocation({
            longitudeText.text = it.first.toString()
            latitudeText.text = it.second.toString()
        }, {
            Timber.e(it, DEBUGGER, "")
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    showSnackbar(R.string.label_location_service_approved)
                    startLocation()
                }
                Activity.RESULT_CANCELED -> {
                    showSnackbar(R.string.label_location_service_cancelled)
                    Timber.e(DEBUGGER, "User cancelled")
                }
            }
        }
    }

    private fun showSnackbar(snackStrId: Int,
                             actionStrId: Int = 0,
                             listener: View.OnClickListener? = null) {
        Snackbar.make(findViewById(android.R.id.content), getString(snackStrId), Snackbar.LENGTH_INDEFINITE).apply {
            if (actionStrId != 0 && listener != null) {
                setAction(getString(actionStrId), listener)
            }
        }.show()
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

        fun getCurrentLocation(success: (Pair<Double, Double>) -> Unit,
                               failed: (Exception?) -> Unit) {
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
                        else -> failed.invoke(exception)
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
            companion object {
                private const val SECOND = 1000L
            }

            private lateinit var fusedLocationClient: FusedLocationProviderClient
            private lateinit var settingsClient: SettingsClient
            private lateinit var locationRequest: LocationRequest
            private lateinit var locationSettingsRequest: LocationSettingsRequest

            private var interval: Long = 1 * SECOND
            private var numUpdates: Int = 3

            fun setInterval(intervalInSec: Int): Builder {
                this.interval = intervalInSec * SECOND
                return this
            }

            fun setNumUpdates(numUpdates: Int): Builder {
                this.numUpdates = numUpdates
                return this
            }

            fun build(): LocationService {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
                settingsClient = LocationServices.getSettingsClient(activity)
                locationRequest = LocationRequest.create().apply {
                    setExpirationDuration(this@Builder.interval * this@Builder.numUpdates * SECOND)
                    fastestInterval = this@Builder.interval / 5
                    interval = this@Builder.interval
                    numUpdates = this@Builder.numUpdates
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                locationSettingsRequest = LocationSettingsRequest.Builder().apply {
                    addLocationRequest(locationRequest)
                }.build()

                return LocationService(activity, fusedLocationClient, settingsClient,
                        locationRequest, locationSettingsRequest)
            }
        }
    }
}