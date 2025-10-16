package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import co.edu.unipiloto.myapplication.R;
// Asegúrate de importar tu repositorio
// import co.edu.unipiloto.myapplication.db.SolicitudRepository;
// import co.edu.unipiloto.myapplication.storage.SessionManager;


/**
 * Actividad encargada de capturar los detalles completos del envío (remitente, paquete, destinatario).
 * Recibe la dirección de recolección de SolicitudActivity.
 */
public class SolicitudDetailsActivity extends AppCompatActivity {

    // --- DATOS DEL REMITENTE ---
    private TextInputEditText etSenderName;
    private Spinner spIDType;
    private TextInputEditText etSenderID;
    private Spinner spSenderCountryCode;
    private TextInputEditText etSenderPhone;

    // --- DATOS DEL PAQUETE ---
    private TextInputEditText etPackageHeight;
    private TextInputEditText etPackageWidth;
    private TextInputEditText etPackageLength;
    private TextInputEditText etPackageWeight;
    private TextInputEditText etPackageContent;

    // --- DATOS DEL DESTINATARIO ---
    private TextInputEditText etReceiverName;
    private Spinner spReceiverCountryCode;
    private TextInputEditText etReceiverPhone;
    private TextInputEditText etReceiverAddress;

    // --- PRECIO Y ACCIONES ---
    private TextInputEditText etPrice;
    private MaterialButton btnSend;
    private MaterialButton btnLogout;

    // Dirección de Recolección (viene de la actividad anterior)
    private String pickupAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usando el nombre del layout que indicaste en la respuesta anterior
        setContentView(R.layout.activity_new_delivery);

        // 1. Obtener la dirección de recolección de la actividad anterior
        pickupAddress = getIntent().getStringExtra("PICKUP_ADDRESS");
        if (pickupAddress == null) {
            Toast.makeText(this, "Error: Dirección de recolección no recibida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Inicialización de Vistas
        initViews();

        // 3. Configuración de Spinners (Ejemplo)
        setupSpinners();

        // 4. Configuración de Eventos
        btnSend.setOnClickListener(v -> createSolicitud());

        btnLogout.setOnClickListener(v -> logout());

        // Opcional: Calcular precio al inicio o al cambiar un campo
        // calculatePrice();
    }

    /** Inicializa todas las variables de vista con sus IDs correspondientes. */
    private void initViews() {
        // --- REMITENTE ---
        etSenderName = findViewById(R.id.etSenderName);
        spIDType = findViewById(R.id.spIDType);
        etSenderID = findViewById(R.id.etSenderID);
        spSenderCountryCode = findViewById(R.id.spSenderCountryCode);
        etSenderPhone = findViewById(R.id.etSenderPhone);

        // --- PAQUETE ---
        etPackageHeight = findViewById(R.id.etPackageHeight);
        etPackageWidth = findViewById(R.id.etPackageWidth);
        etPackageLength = findViewById(R.id.etPackageLength);
        etPackageWeight = findViewById(R.id.etPackageWeight);
        etPackageContent = findViewById(R.id.etPackageContent);

        // --- DESTINATARIO ---
        etReceiverName = findViewById(R.id.etReceiverName);
        spReceiverCountryCode = findViewById(R.id.spReceiverCountryCode);
        etReceiverPhone = findViewById(R.id.etReceiverPhone);
        etReceiverAddress = findViewById(R.id.etReceiverAddress);

        // --- PRECIO Y ACCIONES ---
        etPrice = findViewById(R.id.etPrice);
        btnSend = findViewById(R.id.btnSend);
        btnLogout = findViewById(R.id.btnLogout);
    }

    /** Configura los adaptadores para los Spinners. */
    private void setupSpinners() {
        // Ejemplo de datos para Spinners (DEBES usar tus propios recursos de array)
        String[] idTypes = new String[]{"C.C.", "NIT", "Passport"};
        String[] codes = new String[]{"+57", "+52", "+54"}; // Códigos de país

        ArrayAdapter<String> idAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, idTypes);
        idAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIDType.setAdapter(idAdapter);

        ArrayAdapter<String> codeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, codes);
        codeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSenderCountryCode.setAdapter(codeAdapter);
        spReceiverCountryCode.setAdapter(codeAdapter);
    }

    /** Valida los campos, crea la solicitud y navega a la pantalla de guía. */
    private void createSolicitud() {
        // 1. Obtener valores y validación básica
        String senderName = etSenderName.getText().toString().trim();
        String packageContent = etPackageContent.getText().toString().trim();
        String receiverName = etReceiverName.getText().toString().trim();
        String receiverAddress = etReceiverAddress.getText().toString().trim();
        // ... (obtener y validar todos los demás campos)

        if (senderName.isEmpty() || packageContent.isEmpty() || receiverName.isEmpty() || receiverAddress.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Ejecutar la creación de la solicitud en la DB.
        // SolicitudRepository repo = new SolicitudRepository(this);
        // long newId = repo.insertSolicitud(/* todos los datos */);

        // 3. Resultado
        Toast.makeText(this, "¡Solicitud procesada! Generando guía...", Toast.LENGTH_LONG).show();

        // 🛑 CAMBIO CLAVE: Navegar a GuideActivity (la pantalla exitosa)
        Intent intent = new Intent(this, GuideActivity.class);

        // Opcional: Pasar el ID de la nueva guía o un mensaje de éxito
        // intent.putExtra("GUIDE_ID", newId);

        startActivity(intent);

        // Finalizamos esta actividad para que el usuario no pueda regresar a llenar el formulario
        finish();
    }

    /** Cierra la sesión y regresa a la pantalla de Login. */
    private void logout() {
        // new SessionManager(this).clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}