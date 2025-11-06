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

    /**
     * Initializes the activity, inflates its layout, and binds UI elements and event listeners.
     *
     * @param savedInstanceState If non-null, contains the activity's previously saved state. 
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_shipping)

        initViews()
        setupListeners()
    }

    /**
     * Initializes view references used by the activity and sets the back button behavior.
     *
     * Binds UI widgets (guide code input, search button, results card, and error message text)
     * to their corresponding properties and attaches a click listener to the back button that
     * finishes the activity.
     */
    private fun initViews() {
        etGuideCode = findViewById(R.id.etGuideCode)
        btnSearch = findViewById(R.id.btnSearch)
        cvResults = findViewById(R.id.cvResults)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
    }

    /**
     * Sets up UI event handlers.
     *
     * Attaches a click listener to the search button that initiates a shipment lookup when tapped.
     */
    private fun setupListeners() {
        btnSearch.setOnClickListener {
            searchShipping()
        }
    }

    /**
             * Validates the entered guide code and updates the UI with the shipping status or an error message.
             *
             * If the input is empty, displays an error and hides the results card. Otherwise performs a lookup
             * (currently simulated) and either reveals the results card and populates status fields
             * (current status, guide number, delivery address, delivery date and time window) for a found
             * guide, or displays an error message if not found.
             */
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