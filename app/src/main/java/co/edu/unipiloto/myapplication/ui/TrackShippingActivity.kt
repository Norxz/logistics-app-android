package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.dto.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import co.edu.unipiloto.myapplication.dto.ShippingStatus
import co.edu.unipiloto.myapplication.dto.SolicitudResponse
import kotlinx.coroutines.launch

/**
 * Activity que permite a cualquier usuario rastrear un envío mediante su código de guía.
 */
class TrackShippingActivity : AppCompatActivity() {

    private lateinit var etGuideCode: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var cvResults: CardView
    private lateinit var tvErrorMessage: TextView
    private lateinit var progressBar: ProgressBar // 💡 Añadimos la referencia al ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_shipping)

        supportActionBar?.hide()

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etGuideCode = findViewById(R.id.etGuideCode)
        btnSearch = findViewById(R.id.btnSearch)
        cvResults = findViewById(R.id.cvResults)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        progressBar = findViewById(R.id.progressBar) // 💡 Inicializamos el ProgressBar

        // Inicializar vistas con estado oculto
        cvResults.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
        progressBar.visibility = View.GONE // Aseguramos que inicie oculto

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener {
            searchShipping()
        }
    }

    /**
     * Habilita/deshabilita la UI y controla la visibilidad de la barra de progreso.
     */
    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !isLoading
        etGuideCode.isEnabled = !isLoading
        cvResults.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
    }

    private fun searchShipping() {
        val guideCode = etGuideCode.text.toString().trim()

        if (guideCode.isEmpty()) {
            showError("Ingrese un código de guía válido.")
            return
        }

        setLoading(true)
        val trackingNumber = guideCode

        // 🏆 CORRECCIÓN: Usamos Coroutines
        lifecycleScope.launch {
            try {
                // 1. LLAMADA SUSPENDIDA: Usamos la función getSolicitudByTrackingNumber
                val response = RetrofitClient.getSolicitudApi().getSolicitudByTrackingNumber(trackingNumber)

                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val solicitudResponse = response.body()!!

                    // 2. Mapear SolicitudResponse a ShippingStatus si es necesario
                    // Si el backend te devuelve SolicitudResponse, debes adaptar displayResults
                    displayResultsFromSolicitud(solicitudResponse)

                } else if (response.code() == 404) {
                    showError("⚠️ Error: La guía $guideCode no fue encontrada o es inválida.")
                } else {
                    showError("Error del servidor: No se pudo obtener el estado (${response.code()}).")
                }
            } catch (e: Exception) {
                setLoading(false)
                Log.e("Tracking", "Fallo de red: ${e.message}")
                showError("Fallo de red. Verifique la conexión al servidor o la URL base.")
            }
        }
    }

    /**
     * Muestra la tarjeta de resultados con los datos recibidos de SolicitudResponse.
     * DEBE REEMPLAZAR EL displayResults ANTERIOR
     */
    private fun displayResultsFromSolicitud(solicitudResponse: SolicitudResponse) {
        cvResults.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE

        // Usamos los campos del DTO SolicitudResponse
        findViewById<TextView>(R.id.tvCurrentStatus).text = solicitudResponse.estado
        findViewById<TextView>(R.id.tvGuideNumber).text = solicitudResponse.guia.trackingNumber
        findViewById<TextView>(R.id.tvDeliveryAddress).text = solicitudResponse.direccionCompleta
        findViewById<TextView>(R.id.tvDeliveryDate).text = solicitudResponse.fechaRecoleccion
        findViewById<TextView>(R.id.tvDeliveryFranja).text = solicitudResponse.franjaHoraria
    }

    /**
     * Muestra la tarjeta de resultados con los datos recibidos.
     */
    private fun displayResults(statusData: ShippingStatus) {
        cvResults.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE

        // 💡 Mapeo de datos del DTO a la UI
        findViewById<TextView>(R.id.tvCurrentStatus).text = statusData.status
        findViewById<TextView>(R.id.tvGuideNumber).text = statusData.trackingNumber
        findViewById<TextView>(R.id.tvDeliveryAddress).text = statusData.destinationAddress
        findViewById<TextView>(R.id.tvDeliveryDate).text = statusData.estimatedDate
        findViewById<TextView>(R.id.tvDeliveryFranja).text = statusData.timeFranja
        // Nota: Asegúrate de que todos estos IDs (tvCurrentStatus, tvGuideNumber, etc.)
        // existen exactamente como están escritos en tu layout activity_track_shipping.xml
    }

    /**
     * Muestra el mensaje de error y oculta la tarjeta de resultados.
     */
    private fun showError(message: String) {
        tvErrorMessage.text = message
        tvErrorMessage.visibility = View.VISIBLE
        cvResults.visibility = View.GONE
    }
}