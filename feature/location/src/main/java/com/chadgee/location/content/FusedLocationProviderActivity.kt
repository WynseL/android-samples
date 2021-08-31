package com.chadgee.location.content

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import com.chadgee.location.R
import com.chadgee.location.databinding.ActivityFusedLocationProviderBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import java.lang.Exception

class FusedLocationProviderActivity: AppCompatActivity() {

    companion object {
        private const val DEBUGGER = " === FusedLocationProviderActivity === %s"

        private const val REQUEST_PERMISSION_CODE = 10
    }

    private var _binding: ActivityFusedLocationProviderBinding? = null
    private val binding get() = _binding!!

    private lateinit var longitudeText: AppCompatTextView
    private lateinit var latitudeText: AppCompatTextView

    private val locationService: LocationService by lazy { LocationService.Builder(this).build() }
    private val permissions: Array<String> by lazy { arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityFusedLocationProviderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        longitudeText = binding.longitudeText
        latitudeText = binding.latitudeText
    }

    override fun onStart() {
        super.onStart()

        if (!isPermissionGranted) {
            requestPermission()
        } else {
            startLocation()
        }
    }

    private fun startLocation() {
        locationService.getCurrentLocation({
            longitudeText.text = it.first.toString()
            latitudeText.text = it.second.toString()
        }, {
            Timber.e(it, DEBUGGER, "")
        })
    }

    private val isPermissionGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showSnackbar(R.string.label_location_permission_rationale, android.R.string.ok) {
                startLocationPermissionRequest()
            }
        } else {
            startLocationPermissionRequest()
        }
    }

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            when {
                grantResults.isEmpty() -> {
                    Timber.e(DEBUGGER, "Permission cancelled")
                }
                grantResults.any { it == PackageManager.PERMISSION_GRANTED } -> {
                    Timber.d(DEBUGGER, "Permission granted")
                    startLocation()
                }
                else -> {
                    Timber.e(DEBUGGER, "Permission denied")
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
    class LocationService(private val fusedLocationClient: FusedLocationProviderClient) {

        private val cancellationTokenSource: CancellationTokenSource = CancellationTokenSource()

        fun getCurrentLocation(success: (Pair<Double, Double>) -> Unit,
                               failed: (Exception?) -> Unit) {
            fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token).addOnCompleteListener { task ->
                when {
                    task.isSuccessful && task.result != null -> {
                        val location = task.result
                        success.invoke(location.longitude to location.latitude)
                    }
                    task.isSuccessful && task.result == null -> failed.invoke(NullPointerException("Task result is null"))
                    else -> failed.invoke(task.exception)
                }
            }
        }

        fun getLastLocation(success: (Pair<Double, Double>) -> Unit,
                            failed: (Exception?) -> Unit) {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful && task.result != null -> {
                            val location = task.result
                            success.invoke(location.longitude to location.latitude)
                        }
                        task.isSuccessful && task.result == null -> failed.invoke(NullPointerException("Task result is null"))
                        else -> failed.invoke(task.exception)
                    }
                }
        }

        fun cancel() {
            cancellationTokenSource.cancel()
        }

        class Builder(context: Context) {
            private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

            fun build(): LocationService {
                return LocationService(fusedLocationClient)
            }
        }
    }
}