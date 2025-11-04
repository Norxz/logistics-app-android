
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recogida_ubicacion)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etAddress = findViewById(R.id.etAddress)
        btnContinue = findViewById(R.id.btnContinue)
        btnUseGps = findViewById(R.id.btnUseGps)

        findViewById<ImageButton>(R.id.btnGoBack).setOnClickListener { finish() }
    }

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