package co.edu.unipiloto.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast; // Añadido para mostrar mensajes de error

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import co.edu.unipiloto.myapplication.R;
// Importaciones del servicio RESTful
import co.edu.unipiloto.myapplication.model.ShippingStatus; // ¡Asegúrate de crear esta clase!
import co.edu.unipiloto.myapplication.service.ShippingService; // ¡Asegúrate de crear esta interfaz!

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TrackShippingActivity extends AppCompatActivity {

    // URL base de tu API de envíos - DEBES CAMBIAR ESTA URL POR LA REAL
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com/";

    // Nombres de estados de ejemplo (Deben coincidir con los de tu API)
    private static final String EST_ENTREGADA = "ENTREGADA";
    private static final String EST_CONFIRMADA = "CONFIRMADA";
    private static final String EST_EN_CAMINO = "EN CAMINO";
    private static final String EST_PENDIENTE = "PENDIENTE";
    private static final String EST_CANCELADA = "CANCELADA";

    // Componentes del Layout
    private TextInputEditText etGuideCode;
    private MaterialButton btnSearch;
    private ImageButton btnBack;
    private ProgressBar progressBar; // Asume que tienes este ID
    private CardView cvResults;
    private TextView tvCurrentStatus;
    private TextView tvGuideNumber;
    private TextView tvDeliveryAddress;
    private TextView tvDeliveryDate;
    private TextView tvDeliveryFranja;
    private TextView tvErrorMessage;

    // Se elimina SolicitudRepository

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_shipping);

        // 1. Inicializar Componentes (Binding)
        etGuideCode = findViewById(R.id.etGuideCode);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack);
        // Descomenta si tienes un ProgressBar:
        // progressBar = findViewById(R.id.progressBar);
        cvResults = findViewById(R.id.cvResults);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);

        tvGuideNumber = findViewById(R.id.tvGuideNumber);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        tvDeliveryFranja = findViewById(R.id.tvDeliveryFranja);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        // Se elimina la inicialización de SolicitudRepository

        // 2. Configurar Listeners

        // FUNCIONALIDAD DE REGRESO
        btnBack.setOnClickListener(v -> finish());

        // LLAMADA AL SERVICIO REST
        btnSearch.setOnClickListener(v -> searchShipping());
    }

    private void searchShipping() {
        hideKeyboard();
        hideResultsAndError();

        String guideCodeStr = etGuideCode.getText().toString().trim();

        if (guideCodeStr.isEmpty()) {
            etGuideCode.setError("Ingresa el ID de la solicitud.");
            return;
        }

        // Ya no necesitamos parsear a Long. Se envía el String directamente a la API.
        // showLoading(); // Muestra el ProgressBar si lo tienes

        fetchShippingStatus(guideCodeStr);
    }

    /**
     * Inicia la petición REST asíncrona usando Retrofit.
     */
    private void fetchShippingStatus(String guideCode) {
        // 1. Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 2. Crear la implementación del servicio
        ShippingService service = retrofit.create(ShippingService.class);

        // 3. Crear la llamada con el ID dinámico
        Call<ShippingStatus> call = service.getShippingStatus(guideCode);

        // 4. Ejecutar la llamada asíncrona
        call.enqueue(new Callback<ShippingStatus>() {
            @Override
            public void onResponse(Call<ShippingStatus> call, Response<ShippingStatus> response) {
                // hideLoading(); // Oculta el ProgressBar

                if (response.isSuccessful() && response.body() != null) {
                    // Éxito (Código 200-299)
                    displayShippingDetails(response.body());
                } else if (response.code() == 404) {
                    // Not Found (404): La API responde que no existe
                    displayError("El ID de la solicitud \"" + guideCode + "\" no fue encontrado o es inválido.");
                } else {
                    // Otros errores HTTP (400, 500, etc.)
                    displayError("Error al buscar el envío. Código de respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ShippingStatus> call, Throwable t) {
                // hideLoading(); // Oculta el ProgressBar
                // Fallo de red, timeout, etc.
                displayError("❌ Fallo de conexión. Revisa tu conexión a internet o la URL base.");
                Toast.makeText(TrackShippingActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Muestra los detalles del envío en la interfaz, usando el modelo de la API.
     */
    private void displayShippingDetails(ShippingStatus status) {
        // Usamos los getters del POJO de la API (ShippingStatus)
        tvGuideNumber.setText(status.getId());
        tvDeliveryAddress.setText(status.getDestinationAddress());
        tvCurrentStatus.setText(status.getStatus());
        tvDeliveryDate.setText(status.getEstimatedDate());
        tvDeliveryFranja.setText(status.getTimeFranja());

        int colorResId = getColorForStatus(status.getStatus());
        tvCurrentStatus.setTextColor(ContextCompat.getColor(this, colorResId));

        cvResults.setVisibility(View.VISIBLE);
    }

    // Métodos auxiliares de UX (se mantienen)

    private void displayError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        cvResults.setVisibility(View.GONE);
        etGuideCode.setText(""); // Limpiar la entrada después de un error
    }

    private void hideResultsAndError() {
        cvResults.setVisibility(View.GONE);
        tvErrorMessage.setVisibility(View.GONE);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Mapea el estado de la solicitud a un color dinámico.
     */
    private int getColorForStatus(String status) {
        if (status == null) return R.color.on_surface_secondary;

        switch (status.toUpperCase()) {
            case EST_ENTREGADA:
            case EST_CONFIRMADA:
                return R.color.status_success;
            case EST_EN_CAMINO:
                return R.color.primary_color;
            case EST_PENDIENTE:
                return R.color.accent_orange;
            case EST_CANCELADA:
                return R.color.status_error;
            default:
                return R.color.on_surface_secondary;
        }
    }
}