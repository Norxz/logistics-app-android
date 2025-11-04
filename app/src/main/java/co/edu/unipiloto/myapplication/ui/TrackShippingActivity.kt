package co.edu.unipiloto.myapplication.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import co.edu.unipiloto.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

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

        // TODO: Lógica de consulta en el repositorio para buscar el estado del envío

        // Simulación de resultado (debes reemplazar esto con tu lógica de BD)
        if (guideCode == "1234567890") {
            // Simulación de éxito
            cvResults.visibility = View.VISIBLE
            tvErrorMessage.visibility = View.GONE

            findViewById<TextView>(R.id.tvCurrentStatus).text = "EN RUTA"
            findViewById<TextView>(R.id.tvGuideNumber).text = guideCode
            findViewById<TextView>(R.id.tvDeliveryAddress).text = "Calle Falsa 123, Springfield"
            findViewById<TextView>(R.id.tvDeliveryDate).text = "2025-11-10"
            findViewById<TextView>(R.id.tvDeliveryFranja).text = "PM (14:00 - 18:00)"
        } else {
            // Simulación de error
            tvErrorMessage.text = "⚠️ Error: La guía $guideCode no fue encontrada o es inválida."
            tvErrorMessage.visibility = View.VISIBLE
            cvResults.visibility = View.GONE
        }
    }
}