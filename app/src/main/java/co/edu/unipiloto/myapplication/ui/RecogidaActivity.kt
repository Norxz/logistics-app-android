package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.utils.LocationHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText


/**
 * Activity dedicada a la selección de una dirección (Recolección o Entrega).
 * IMPLEMENTA: Lógica de Mapa con OSMDroid.
 */
class RecogidaActivity : AppCompatActivity() {

    private lateinit var locationHelper: LocationHelper
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null

    private lateinit var etAddress: TextInputEditText
    private lateinit var btnGps: ImageButton
    private lateinit var btnContinuar: MaterialButton

    private var lastAddress = ""
    private var lastLat = 0.0
    private var lastLon = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogida)

        etAddress = findViewById(R.id.etAddress)
        btnGps = findViewById(R.id.btnUseGps)
        mapView = findViewById(R.id.mapView)
        btnContinuar = findViewById(R.id.btnContinue)

        mapView.onCreate(null)
        mapView.getMapAsync { map ->
            googleMap = map
        }

        etAddress.setText(lastAddress)

        locationHelper = LocationHelper(
            this,
            mapView
        ) { address, lat, lon, city ->
            etAddress.setText(address)
            lastAddress = address
            lastLat = lat
            lastLon = lon
        }

        locationHelper.checkLocationPermission(LocationHelper.PERMISSION_CODE)


        btnGps.setOnClickListener {
            locationHelper.requestLocationPermission()
        }

        btnContinuar.setOnClickListener {
            returnResult()
        }

        etAddress.setOnEditorActionListener { _, actionId, event ->
            val isEnter = actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == android.view.KeyEvent.KEYCODE_ENTER &&
                            event.action == android.view.KeyEvent.ACTION_DOWN)

            if (isEnter) {
                val query = etAddress.text.toString().trim()
                if (query.isNotEmpty()) locationHelper.searchAddress(query)
                true
            } else false
        }
    }

    private fun returnResult() {
        val i = Intent(this, SolicitudActivity::class.java).apply {
            putExtra("RECOLECTION_ADDRESS", lastAddress)
            putExtra("RECOLECTION_LATITUDE", lastLat)
            putExtra("RECOLECTION_LONGITUDE", lastLon)
        }
        startActivity(i)
        finish()
    }
    // Ciclo de vida requerido para MapView
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
}
