package co.edu.unipiloto.myapplication.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Activity dedicada a la selección de una dirección (Recolección o Entrega).
 * Usa un placeholder para el mapa y devuelve la dirección seleccionada al llamador.
 */
class RecogidaActivity : AppCompatActivity() {

    // --- CONSTANTES DE RESULTADO ---
    companion object {
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
        const val EXTRA_IS_RECOLECTION = "EXTRA_IS_RECOLECTION" // Para saber qué tipo de dirección es
    }

    // --- VISTAS ---
    private lateinit var etAddress: TextInputEditText
    private lateinit var tilAddress: TextInputLayout
    private lateinit var btnGoBack: ImageButton
    private lateinit var btnUseGps: ImageButton
    private lateinit var btnContinue: MaterialButton

    private var isRecolectionAddress = false // Estado para determinar el tipo de dirección

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogida)

        supportActionBar?.hide()

        // Obtener el estado del Intent
        isRecolectionAddress = intent.getBooleanExtra(EXTRA_IS_RECOLECTION, false)

        initViews()
        setupListeners()

        // 🏆 Nota: Si tienes Google Maps SDK, aquí inicializarías el mapa real.
    }

    private fun initViews() {
        // Inicialización de vistas según el XML que proporcionaste
        btnGoBack = findViewById(R.id.btnGoBack)
        etAddress = findViewById(R.id.etAddress)
        tilAddress = findViewById(R.id.tilAddress)
        btnUseGps = findViewById(R.id.btnUseGps)
        btnContinue = findViewById(R.id.btnContinue)
    }

    private fun setupListeners() {
        btnGoBack.setOnClickListener {
            // Cancelar y volver
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        btnUseGps.setOnClickListener {
            // TODO: Implementar lógica de permiso de GPS y obtener ubicación actual.
            Toast.makeText(this, "Simulando obtener ubicación GPS...", Toast.LENGTH_SHORT).show()
            etAddress.setText("Cra 15 # 56-20, Bogotá") // Simulación
        }

        btnContinue.setOnClickListener {
            returnSelectedAddress()
        }
    }

    /**
     * Valida la dirección y la devuelve a la actividad llamadora.
     */
    private fun returnSelectedAddress() {
        val address = etAddress.text.toString().trim()

        if (address.isEmpty() || address.length < 5) {
            tilAddress.error = "Por favor, ingrese una dirección válida."
            return
        }

        // Crear el Intent para devolver el resultado
        val resultIntent = Intent().apply {
            putExtra(EXTRA_ADDRESS, address)
        }

        // Establecer el resultado y finalizar
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}