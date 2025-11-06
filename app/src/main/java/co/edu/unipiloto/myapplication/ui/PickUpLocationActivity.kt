package co.edu.unipiloto.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.edu.unipiloto.myapplication.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Activity que permite al usuario ingresar la dirección de recogida de la solicitud.
 */
class PickUpLocationActivity : AppCompatActivity() {

    private lateinit var etAddress: TextInputEditText
    private lateinit var btnContinue: MaterialButton
    private lateinit var btnUseGps: ImageButton

    /**
     * Initializes the activity: sets the content view, binds UI components, and attaches listeners.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if available.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogida_ubicacion)

        initViews()
        setupListeners()
    }

    /**
     * Binds UI views for the pickup address screen and wires the back button to close the activity.
     *
     * Initializes `etAddress`, `btnContinue`, and `btnUseGps` with their corresponding views
     * and sets the back button to finish the activity when tapped.
     */
    private fun initViews() {
        etAddress = findViewById(R.id.etAddress)
        btnContinue = findViewById(R.id.btnContinue)
        btnUseGps = findViewById(R.id.btnUseGps)

        findViewById<ImageButton>(R.id.btnGoBack).setOnClickListener { finish() }
    }

    /**
     * Attaches click listeners to the GPS and continue controls.
     *
     * The GPS button shows a short "Obtaining current location" toast as a placeholder for GPS retrieval.
     * The continue button reads the trimmed pickup address from the address input; if the address is empty it shows a validation toast, otherwise it launches SolicitudActivity with the address under the "PICKUP_ADDRESS" extra and finishes this activity.
     */
    private fun setupListeners() {
        btnUseGps.setOnClickListener {
            // TODO: Implementar lógica de obtención de ubicación por GPS
            Toast.makeText(this, "Obteniendo ubicación actual...", Toast.LENGTH_SHORT).show()
        }

        btnContinue.setOnClickListener {
            val address = etAddress.text.toString().trim()
            if (address.isEmpty()) {
                Toast.makeText(this, "Debe ingresar una dirección de recogida.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Continuar a la siguiente pantalla para detalles del paquete/destino
            val intent = Intent(this, SolicitudActivity::class.java)
            // Puedes pasar la dirección como extra si es necesario
            intent.putExtra("PICKUP_ADDRESS", address)
            startActivity(intent)
            finish()
        }
    }
}