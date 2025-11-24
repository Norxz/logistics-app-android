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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.location.FusedLocationProviderClient


import java.util.Locale
import android.graphics.Color
import android.util.Log

import co.edu.unipiloto.myapplication.BuildConfig
import com.google.maps.android.PolyUtil
import org.json.JSONObject
import java.net.URL

class LocationHelper(
    private val activity: AppCompatActivity,
    private val mapView: MapView,
    private val onLocationSelected: (address: String, lat: Double, lon: Double) -> Unit
) : OnMapReadyCallback {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)
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
        if (!hasLocationPermission()) return

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LocationHelper.PERMISSION_CODE
            )
            return
        }

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

        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
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

                if (!result.isNullOrEmpty()) {
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


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        googleMap?.uiSettings?.isZoomControlsEnabled = true

        if (hasLocationPermission()) {
            googleMap?.isMyLocationEnabled = true
        }

        googleMap?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(4.65, -74.1), 10f)
        )

        googleMap?.setOnMapClickListener { latLng ->
            updateMarker(latLng)
            reverseGeocode(latLng)
        }
    }

    fun getCurrentLocation(onResult: (String, Double, Double) -> Unit) {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LocationHelper.PERMISSION_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                val geocoder = Geocoder(activity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                val address = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else {
                    "Dirección desconocida"
                }

                googleMap?.clear()
                val pos = LatLng(lat, lon)
                googleMap?.addMarker(MarkerOptions().position(pos).title(address))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15f))

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

    fun geocodeAddress(
        address: String,
        onResult: (lat: Double, lon: Double, formattedAddress: String) -> Unit
    ) {
        Thread {
            try {
                val geocoder = Geocoder(activity, Locale.getDefault())
                val results = geocoder.getFromLocationName(address, 1)
                if (!results.isNullOrEmpty()) {
                    val r = results[0]
                    Handler(Looper.getMainLooper()).post {
                        onResult(r.latitude, r.longitude, r.getAddressLine(0) ?: address)
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

    fun drawRoute(origin: LatLng, destination: LatLng) {

        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=${BuildConfig.MAPS_API_KEY}"

        Thread {
            try {
                val data = URL(url).readText()
                val json = JSONObject(data)

                val routes = json.getJSONArray("routes")
                if (routes.length() == 0) {
                    Log.e("ROUTE", "No routes found")
                    return@Thread
                }

                // ---- ⭐ OVERVIEW_POLYLINE (RUTA COMPLETA) ⭐ ----
                val overview = routes
                    .getJSONObject(0)
                    .getJSONObject("overview_polyline")
                    .getString("points")

                val points = PolyUtil.decode(overview)

                activity.runOnUiThread {

                    // Dibujar la polilínea
                    val polyOptions = PolylineOptions()
                        .addAll(points)
                        .width(12f)
                        .color(Color.BLUE)
                        .geodesic(true)

                    googleMap?.addPolyline(polyOptions)

                    // ---- ⭐ Ajustar cámara a toda la ruta ⭐ ----
                    val builder = LatLngBounds.Builder()
                    for (p in points) builder.include(p)

                    val bounds = builder.build()

                    googleMap?.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 150)
                    )
                }

            } catch (e: Exception) {
                Log.e("ROUTE", "Error parsing route", e)
            }
        }.start()
    }

    fun getRouteToAddress(
        address: String,
        onResult: (startLat: Double, startLon: Double, endLat: Double, endLon: Double) -> Unit
    ) {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        // Obtener ubicación actual
        getCurrentLocation { startAddress, startLat, startLon ->
            // Geocodificar dirección destino
            geocodeAddress(address) { endLat, endLon, _ ->
                onResult(startLat, startLon, endLat, endLon)
            }
        }
    }
}
