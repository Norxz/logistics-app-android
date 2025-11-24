package co.edu.unipiloto.myapplication.utils

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class LocationHelper(
    private val activity: AppCompatActivity,
    private val mapView: MapView,
    private val onLocationSelected: (address: String, lat: Double, lon: Double) -> Unit
) : OnMapReadyCallback {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null

    companion object {
        const val PERMISSION_CODE = 1001
    }

    init {
        mapView.getMapAsync(this)
    }

    fun checkLocationPermission(requestCode: Int) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                requestCode
            )
        } else {
            requestCurrentLocation()
        }
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_CODE
        )
    }

    private fun requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val latLng = LatLng(loc.latitude, loc.longitude)
                updateMarker(latLng)
                reverseGeocode(latLng)
            }
        }
    }

    private fun updateMarker(latLng: LatLng) {
        if (marker == null) {
            marker = googleMap?.addMarker(
                MarkerOptions().position(latLng).title("Ubicación seleccionada")
            )
        } else {
            marker!!.position = latLng
        }

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    private fun reverseGeocode(latLng: LatLng) {
        try {
            val geocoder = Geocoder(activity, Locale.getDefault())
            val list = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            if (!list.isNullOrEmpty()) {
                val address = list[0].getAddressLine(0)
                onLocationSelected(address, latLng.latitude, latLng.longitude)
            }
        } catch (e: Exception) {
            Toast.makeText(activity, "Error obteniendo dirección", Toast.LENGTH_SHORT).show()
        }
    }

    fun searchAddress(address: String) {
        Thread {
            try {
                val geocoder = Geocoder(activity, Locale.getDefault())
                val result = geocoder.getFromLocationName(address, 1)

                if (result != null && result.isNotEmpty()) {
                    val r = result[0]
                    val latLng = LatLng(r.latitude, r.longitude)

                    Handler(Looper.getMainLooper()).post {
                        updateMarker(latLng)
                        onLocationSelected(address, r.latitude, r.longitude)
                    }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(activity, "Dirección no encontrada", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(activity, "Error buscando dirección", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun setEmptyMapPosition(map: GoogleMap) {
        val emptyLatLng = LatLng(0.0, 0.0)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(emptyLatLng, 1f))
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.uiSettings?.isZoomControlsEnabled = true

        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(0.0, 0.0), 1f)
        )

        googleMap?.setOnMapClickListener { latLng ->
            updateMarker(latLng)
            reverseGeocode(latLng)
        }
    }

    fun getCurrentLocation(onResult: (String, Double, Double) -> Unit) {
        val permission = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                // Geocoder → dirección
                val geocoder = Geocoder(activity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                val address = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else {
                    "Dirección desconocida"
                }

                // Mover marcador del mapa
                googleMap?.clear()
                val pos = LatLng(lat, lon)
                googleMap?.addMarker(MarkerOptions().position(pos).title(address))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16f))

                onResult(address, lat, lon)
            } else {
                Toast.makeText(activity, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

}

