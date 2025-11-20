package co.edu.unipiloto.myapplication.utils

import android.content.pm.PackageManager
import android.content.Context.LOCATION_SERVICE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LocationHelper(
    private val activity: AppCompatActivity,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val mapFragment: SupportMapFragment?,
    private val onLocationObtained: (lat: Double, lon: Double) -> Unit
) : OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    var selectedLat: Double? = null
        private set
    var selectedLon: Double? = null
        private set

    init {
        mapFragment?.getMapAsync(this)
    }

    fun checkLocationPermission(requestCode: Int) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        } else {
            checkLocationEnabled()
        }
    }

    private fun checkLocationEnabled() {
        val locationManager = activity.getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(activity, "La ubicación está desactivada", Toast.LENGTH_LONG).show()
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                setLocation(location.latitude, location.longitude)
            } else {
                fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { newLocation ->
                    if (newLocation != null) {
                        setLocation(newLocation.latitude, newLocation.longitude)
                    } else {
                        Toast.makeText(activity, "No se pudo obtener ubicación actual", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setLocation(lat: Double, lon: Double) {
        selectedLat = lat
        selectedLon = lon
        onLocationObtained(lat, lon)
        updateMapLocation()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isZoomControlsEnabled = true
        val defaultLocation = LatLng(4.7110, -74.0721)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
    }

    fun updateMapLocation() {
        googleMap?.clear()
        selectedLat?.let { lat ->
            selectedLon?.let { lon ->
                val location = LatLng(lat, lon)
                googleMap?.addMarker(MarkerOptions().position(location).title("Destino"))
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }
    }
}
