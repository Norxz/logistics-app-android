package co.edu.unipiloto.myapplication.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.adapters.DriverRequestAdapter
import co.edu.unipiloto.myapplication.adapters.OnRequestClickListener
import co.edu.unipiloto.myapplication.api.SolicitudApi
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import co.edu.unipiloto.myapplication.storage.SessionManager
// --- IMPORTS PARA SERVICIOS ---
import co.edu.unipiloto.myapplication.services.NotificationService
import co.edu.unipiloto.myapplication.services.OdometerService
import retrofit2.Response

/**
 * Actividad principal del conductor.
 * Integra: Listado de rutas, Odómetro (Bound Service), Notificaciones (Started Service) y Google Maps.
 */
class DriverDashboardActivity : AppCompatActivity(), OnRequestClickListener {

    // Vistas existentes
    private lateinit var tvDriverTitle: TextView
    private lateinit var tvDriverSubtitle: TextView
    private lateinit var btnLogout: Button
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var tvNoRoutes: TextView
    private lateinit var requestAdapter: DriverRequestAdapter

    // --- VISTAS NUEVAS (Panel de Control GPS) ---
    private lateinit var tvOdometer: TextView
    private lateinit var etGpsTime: EditText
    private lateinit var etGpsDist: EditText
    private lateinit var btnConfigGps: Button
    private lateinit var btnOpenMaps: Button

    // Servicios API y Sesión
    private lateinit var solicitudApi: SolicitudApi
    private lateinit var sessionManager: SessionManager

    // --- VARIABLES PARA BOUND SERVICE (Odómetro) ---
    private var odometerService: OdometerService? = null
    private var bound = false

    // Objeto de conexión para el servicio enlazado
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val odometerBinder = binder as OdometerService.OdometerBinder
            odometerService = odometerBinder.getOdometer()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_dashboard)

        initServices()
        initViews()

        setupListeners()
        setupRecyclerView()
        loadAssignedRequests()

        // Iniciar el actualizador de la UI del Odómetro (Handler Loop)
        setupOdometerUpdater()
    }

    private fun initViews() {
        // Vistas originales
        tvDriverTitle = findViewById(R.id.tvDriverTitle)
        tvDriverSubtitle = findViewById(R.id.tvDriverSubtitle)
        btnLogout = findViewById(R.id.btnLogout)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        tvNoRoutes = findViewById(R.id.tvNoRoutes)

        // Session Manager
        tvDriverTitle.text = getString(R.string.driver_dashboard_title, sessionManager.getUserFullName() ?: "Conductor")
        tvDriverSubtitle.text = getString(R.string.driver_dashboard_subtitle)

        // --- VISTAS NUEVAS DEL PANEL ---
        tvOdometer = findViewById(R.id.tvOdometer)
        etGpsTime = findViewById(R.id.etGpsTime)
        etGpsDist = findViewById(R.id.etGpsDist)
        btnConfigGps = findViewById(R.id.btnConfigGps)
        btnOpenMaps = findViewById(R.id.btnOpenMaps)
    }

    private fun initServices() {
        solicitudApi = RetrofitClient.getSolicitudApi()
        sessionManager = SessionManager(this)
    }

    private fun setupListeners() {
        // Logout
        btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // --- LISTENER: Botón Genérico "Ver Ruta en Google Maps" (Panel Superior) ---
        btnOpenMaps.setOnClickListener {
            // Ejemplo: Ubicación de la U. Piloto o una por defecto
            val lat = 4.632
            val lon = -74.065
            launchGenericMap(lat, lon)
        }

        // --- LISTENER: Configurar GPS (Bound Service) ---
        btnConfigGps.setOnClickListener {
            if (bound && odometerService != null) {
                val timeStr = etGpsTime.text.toString()
                val distStr = etGpsDist.text.toString()

                if (timeStr.isNotEmpty() && distStr.isNotEmpty()) {
                    try {
                        val time = timeStr.toLong()
                        val dist = distStr.toFloat()
                        // Llamada al método del servicio
                        odometerService?.updateConfig(time, dist)
                        Toast.makeText(this, "GPS Reconfigurado: ${time}s, ${dist}m", Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this, "Ingrese números válidos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Complete tiempo y distancia", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Servicio GPS no conectado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        requestAdapter = DriverRequestAdapter(emptyList(), this)
        recyclerViewRoutes.adapter = requestAdapter
    }

    // =========================================================
    // IMPLEMENTACIÓN DE LA INTERFAZ OnRequestClickListener
    // =========================================================

    // 1. Clic en el botón de Estado (Amarillo)
    override fun onRequestStatusChange(solicitudId: Long, currentStatus: String) {
        val nextStatus = getNextStatus(currentStatus)
        if (nextStatus == null) {
            Toast.makeText(this, "La solicitud ya ha sido completada o cancelada.", Toast.LENGTH_SHORT).show()
            return
        }
        updateRequestStatus(solicitudId, nextStatus)
    }

    // 2. Clic en el botón de Mapa (Nuevo ícono) - LABORATORIO MAPAS
    override fun onMapRouteClick(solicitud: SolicitudResponse) {
        // 1. Definimos los dos puntos
        val origen = solicitud.direccionRecoleccion // Punto A (Donde se recoge)
        val destino = solicitud.direccionCompleta   // Punto B (Donde se entrega)

        // 2. Validamos que existan
        if (!origen.isNullOrEmpty() && destino.isNotEmpty()) {
            // Trazamos la línea entre A y B
            launchRouteView(origen, destino)

            // Mensaje para confirmar qué está haciendo
            Toast.makeText(this, "Trazando ruta: Recolección -> Entrega", Toast.LENGTH_SHORT).show()
        } else {
            // Fallback: Si falta el origen, navegamos desde el GPS actual hacia el destino
            val target = if (destino.isNotEmpty()) destino else origen
            if (!target.isNullOrEmpty()) {
                launchNavigationMap(target!!)
                Toast.makeText(this, "Falta una dirección. Navegando desde tu ubicación.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Información de ruta incompleta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Abre Google Maps dibujando la ruta entre dos direcciones específicas (A -> B).
     * No usa el GPS actual como inicio.
     */
    private fun launchRouteView(originAddress: String, destinationAddress: String) {
        try {
            // Usamos la URL estándar de Google Maps para direcciones
            // saddr = Start Address (Origen)
            // daddr = Destination Address (Destino)
            val url = "http://maps.google.com/maps?saddr=${Uri.encode(originAddress)}&daddr=${Uri.encode(destinationAddress)}&mode=driving"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.setPackage("com.google.android.apps.maps") // Intentamos forzar la app nativa

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Si no tiene la app, abre el navegador
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir mapa: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Navegación simple (GPS Actual -> Destino)
     * Se mantiene como respaldo.
     */
    private fun launchNavigationMap(address: String) {
        val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        }
    }

    // Abre navegación hacia coordenadas (Botón genérico)
    private fun launchGenericMap(lat: Double, lon: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$lat,$lon")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps no instalado", Toast.LENGTH_SHORT).show()
        }
    }

    // =========================================================
    // CICLO DE VIDA SERVICIO (BOUND SERVICE)
    // =========================================================

    override fun onStart() {
        super.onStart()
        // Enlazar al servicio de Odómetro
        Intent(this, OdometerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        // Desenlazar servicio para ahorrar recursos
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    private fun setupOdometerUpdater() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (bound && odometerService != null) {
                    val distance = odometerService?.getDistance() ?: 0.0
                    tvOdometer.text = String.format("Distancia recorrida: %.2f m", distance)
                }
                handler.postDelayed(this, 1000) // Actualizar cada segundo
            }
        })
    }

    // =========================================================
    // LÓGICA DE DATOS Y RED
    // =========================================================

    private fun loadAssignedRequests() {
        val driverId = sessionManager.getUserId()
        if (driverId == -1L) {
            tvNoRoutes.visibility = View.VISIBLE
            return
        }

        lifecycleScope.launch {
            try {
                val response: Response<List<SolicitudResponse>> = solicitudApi.getRoutesByDriverIdCoroutines(driverId)
                if (response.isSuccessful) {
                    val requests = response.body() ?: emptyList()
                    if (requests.isNotEmpty()) {
                        requestAdapter.updateData(requests)
                        tvNoRoutes.visibility = View.GONE
                        recyclerViewRoutes.visibility = View.VISIBLE
                    } else {
                        requestAdapter.updateData(emptyList())
                        tvNoRoutes.visibility = View.VISIBLE
                        recyclerViewRoutes.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@DriverDashboardActivity, "Error ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DriverDashboardActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getNextStatus(currentStatus: String): String? {
        return when (currentStatus) {
            "ASIGNADA" -> "EN_RUTA_RECOLECCION"
            "EN_RUTA_RECOLECCION" -> "EN_DISTRIBUCION"
            "EN_DISTRIBUCION" -> "EN_RUTA_REPARTO"
            "EN_RUTA_REPARTO" -> "ENTREGADA"
            else -> null
        }
    }

    private fun updateRequestStatus(solicitudId: Long, newStatus: String) {
        val body = mapOf("estado" to newStatus)

        lifecycleScope.launch {
            try {
                val response: Response<Void> = solicitudApi.updateEstado(solicitudId, body)

                if (response.isSuccessful || response.code() == 204) {
                    Toast.makeText(this@DriverDashboardActivity, "Estado actualizado a $newStatus.", Toast.LENGTH_SHORT).show()
                    loadAssignedRequests()

                    // --- STARTED SERVICE: NOTIFICACIÓN (Laboratorio 3) ---
                    val intentNotif = Intent(this@DriverDashboardActivity, NotificationService::class.java)
                    intentNotif.putExtra(NotificationService.EXTRA_MESSAGE, "Solicitud #$solicitudId cambió a $newStatus")
                    startService(intentNotif)

                } else {
                    Toast.makeText(this@DriverDashboardActivity, "Error ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DriverDashboardActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}