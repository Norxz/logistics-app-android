package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository; // <-- added
import co.edu.unipiloto.myapplication.db.UserRepository; // <-- added

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

    // repository
    private SolicitudRepository solicitudRepo;
    private UserRepository userRepo; // <-- added

    // SharedPreferences name/key (consistente con LoginActivity)
    private static final String PREFS_NAME = "APP_PREFS";
    private static final String PREFS_KEY_USER_ID = "USER_ID";
    // legacy key sometimes usado en otras pantallas
    private static final String PREFS_KEY_CURRENT_USER_ID = "CURRENT_USER_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usando el nombre del layout que indicaste en la respuesta anterior
        setContentView(R.layout.activity_new_delivery);

        // 1. Obtener la dirección de recolección de la actividad anterior
        pickupAddress = getIntent().getStringExtra("PICKUP_ADDRESS");
        if (pickupAddress == null) {
            // No abortamos: permitimos continuar (usuario puede teclear o usar dirección destino).
            pickupAddress = "";
        }

        // init repositories
        solicitudRepo = new SolicitudRepository(this);
        userRepo = new UserRepository(this); // <-- added

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
        String senderName = etSenderName.getText() != null ? etSenderName.getText().toString().trim() : "";
        String packageContent = etPackageContent.getText() != null ? etPackageContent.getText().toString().trim() : "";
        String receiverName = etReceiverName.getText() != null ? etReceiverName.getText().toString().trim() : "";
        String receiverAddress = etReceiverAddress.getText() != null ? etReceiverAddress.getText().toString().trim() : "";
        String price = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";

        if (senderName.isEmpty() || packageContent.isEmpty() || receiverName.isEmpty() || receiverAddress.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Obtener user id (primero intent, si no SharedPreferences)
        long userId = getCurrentUserId();
        if (userId == -1L) {
            // No hay usuario autenticado: mostrar indicación clara
            Toast.makeText(this, "Usuario no autenticado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Obtener datos opcionales desde el intent (FRANJA, ZONA, FECHA) o usar valores por defecto
        String franja = getIntent().getStringExtra("FRANJA");
        if (franja == null) franja = "";

        String zona = getIntent().getStringExtra("ZONA");
        if (zona == null) zona = "";

        String fecha = getIntent().getStringExtra("FECHA");
        if (fecha == null) fecha = String.valueOf(System.currentTimeMillis());

        // 4. Construir notas básicas combinando info relevante
        String notas = "Remitente: " + senderName
                + " | Destinatario: " + receiverName
                + " | Contenido: " + packageContent
                + (price.isEmpty() ? "" : " | Precio: " + price);

        // 5. Insertar en la base de datos (direccion usamos pickupAddress si está; sino usamos receiverAddress)
        String direccionFinal = !pickupAddress.isEmpty() ? pickupAddress : receiverAddress;
        long newId = solicitudRepo.crear(userId, direccionFinal, fecha, franja, notas, zona);

        if (newId == -1L) {
            Toast.makeText(this, "Error al crear la solicitud. Intenta nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        // 6. Resultado: navegar a GuideActivity y pasar el id de la nueva solicitud/guía
        Toast.makeText(this, "¡Solicitud creada! ID: " + newId, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, GuideActivity.class);
        intent.putExtra("GUIDE_ID", newId);
        startActivity(intent);

        // Finalizamos esta actividad para que el usuario no pueda regresar a llenar el formulario
        finish();
    }

    /** Obtiene el id del usuario autenticado: intenta intent extra, luego SharedPreferences, luego busca un CLIENTE en la DB. */
    private long getCurrentUserId() {
        long userId = getIntent().getLongExtra("USER_ID", -1L);
        if (userId != -1L) return userId;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = prefs.getLong(PREFS_KEY_USER_ID, -1L);
        if (userId != -1L) return userId;

        // intentar clave alternativa
        userId = prefs.getLong(PREFS_KEY_CURRENT_USER_ID, -1L);
        if (userId != -1L) return userId;

        // FALLBACK: buscar en la BD un usuario con rol CLIENTE (primer resultado)
        long clientId = userRepo.getFirstIdByRole("CLIENTE");
        if (clientId != -1L) {
            // opcional: informar que se usó un fallback (no obligatorio)
            Toast.makeText(this, "Usuario cliente detectado (fallback).", Toast.LENGTH_SHORT).show();
            return clientId;
        }

        return -1L;
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

