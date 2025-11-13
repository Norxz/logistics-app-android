package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import co.edu.unipiloto.myapplication.R
import co.edu.unipiloto.myapplication.models.ShippingStatus
import co.edu.unipiloto.myapplication.rest.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Activity que permite a cualquier usuario rastrear un envío mediante su código de guía.
 */
class TrackShippingActivity : AppCompatActivity() {

    private lateinit var etGuideCode: TextInputEditText
    private lateinit var btnSearch: MaterialButton
    private lateinit var cvResults: CardView
    private lateinit var tvErrorMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_shipping)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etGuideCode = findViewById(R.id.etGuideCode)
        btnSearch = findViewById(R.id.btnSearch)
        cvResults = findViewById(R.id.cvResults)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)

        // Inicializar vistas con estado oculto
        cvResults.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun setupListeners() {
        btnSearch.setOnClickListener {
            searchShipping()
        }
    }

    private fun searchShipping() {
        val guideCode = etGuideCode.text.toString().trim()

        if (guideCode.isEmpty()) {
            tvErrorMessage.text = "Ingrese un código de guía válido."
            tvErrorMessage.visibility = View.VISIBLE
            cvResults.visibility = View.GONE
            return
        }

        // Limpiar errores y resultados anteriores
        tvErrorMessage.visibility = View.GONE
        cvResults.visibility = View.GONE

        // 🏆 LLAMADA A RETROFIT para buscar el estado del envío
        RetrofitClient.apiService.getShippingStatus(guideCode).enqueue(object : Callback<ShippingStatus> {
            override fun onResponse(call: Call<ShippingStatus>, response: Response<ShippingStatus>) {
                if (response.isSuccessful && response.body() != null) {
                    val statusData = response.body()!!

                    // Mostrar resultados
                    displayResults(statusData, guideCode)

                } else if (response.code() == 404) {
                    // La guía no fue encontrada en el backend
                    showError("⚠️ Error: La guía $guideCode no fue encontrada o es inválida.")
                }
                else {
                    // Otros errores 5xx o 4xx
                    showError("Error del servidor: No se pudo obtener el estado (${response.code()}).")
                }
            }

            override fun onFailure(call: Call<ShippingStatus>, t: Throwable) {
                Log.e("Tracking", "Fallo de red: ${t.message}")
                showError("Fallo de red. Verifique la conexión al servidor.")
            }
        })
    }

    /**
     * Muestra la tarjeta de resultados con los datos recibidos.
     */
    private fun displayResults(statusData: ShippingStatus, guideCode: String) {
        cvResults.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE

        // Mapear datos del DTO a la UI
        findViewById<TextView>(R.id.tvCurrentStatus).text = statusData.status
        findViewById<TextView>(R.id.tvGuideNumber).text = statusData.trackingNumber // Usar el tracking number
        findViewById<TextView>(R.id.tvDeliveryAddress).text = statusData.destinationAddress
        findViewById<TextView>(R.id.tvDeliveryDate).text = statusData.estimatedDate
        findViewById<TextView>(R.id.tvDeliveryFranja).text = statusData.timeFranja
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