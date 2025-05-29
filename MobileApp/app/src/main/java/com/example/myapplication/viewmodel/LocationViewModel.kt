package com.example.myapplication.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _locationString = MutableStateFlow<String?>(null)
    val locationString: MutableStateFlow<String?> = _locationString

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val context = getApplication<Application>().applicationContext
        val cancellationTokenSource = CancellationTokenSource()
        fusedLocationClient.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                resolveAddress(context, location.latitude, location.longitude)
            } else {
                _locationString.value = null
            }
        }.addOnFailureListener {
            _locationString.value = null
        }
    }

    private fun resolveAddress(
        context: android.content.Context,
        latitude: Double,
        longitude: Double
    ) {
        try {
            val geocoder = android.location.Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()
            val addressText = address?.getAddressLine(0)
            _locationString.value = addressText ?: "Lat: $latitude, Lng: $longitude"
        } catch (e: Exception) {
            _locationString.value = "Lat: $latitude, Lng: $longitude"
        }
    }

    fun clearLocation() {
        _locationString.value = null
    }
}