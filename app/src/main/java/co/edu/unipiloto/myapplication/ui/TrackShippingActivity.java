package co.edu.unipiloto.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton; // Importar ImageButton
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.model.Solicitud;

public class TrackShippingActivity extends AppCompatActivity {

    // Componentes del Layout
    private TextInputEditText etGuideCode;
    private MaterialButton btnSearch;
    private ImageButton btnBack; // Declaración del botón de regreso
    private ProgressBar progressBar;
    private CardView cvResults;
    private TextView tvCurrentStatus;
    private TextView tvGuideNumber;
    private TextView tvDeliveryAddress;
    private TextView tvDeliveryDate;
    private TextView tvDeliveryFranja;
    private TextView tvErrorMessage;

    private SolicitudRepository solicitudRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_shipping);

        // 1. Inicializar Componentes (Binding)
        etGuideCode = findViewById(R.id.etGuideCode);
        btnSearch = findViewById(R.id.btnSearch);
        btnBack = findViewById(R.id.btnBack); // Inicialización del botón de regreso
        cvResults = findViewById(R.id.cvResults);
        tvCurrentStatus = findViewById(R.id.tvCurrentStatus);

        tvGuideNumber = findViewById(R.id.tvGuideNumber);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvDeliveryDate = findViewById(R.id.tvDeliveryDate);
        tvDeliveryFranja = findViewById(R.id.tvDeliveryFranja);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        // Si tienes un ProgressBar en tu XML
        // progressBar = findViewById(R.id.progressBar);

        // 2. Inicializar el Repositorio
        solicitudRepository = new SolicitudRepository(this);

        // 3. Configurar Listeners

        // 🟢 FUNCIONALIDAD DE REGRESO 🟢
        btnBack.setOnClickListener(v -> {
            finish(); // Cierra esta Activity y regresa a la Activity anterior (WelcomeActivity)
        });

        btnSearch.setOnClickListener(v -> searchShipping());
    }

    private void searchShipping() {
        hideKeyboard();
        hideResultsAndError();
        // showLoading();

        String guideCodeStr = etGuideCode.getText().toString().trim();

        if (guideCodeStr.isEmpty()) {
            etGuideCode.setError("Ingresa el ID de la solicitud.");
            return;
        }

        try {
            long guideId = Long.parseLong(guideCodeStr);

            Solicitud result = solicitudRepository.getSolicitudById(guideId);

            // hideLoading();

            if (result != null) {
                displayShippingDetails(result);
            } else {
                displayError("El ID de la solicitud \"" + guideCodeStr + "\" no fue encontrado.");
            }
        } catch (NumberFormatException e) {
            // hideLoading();
            displayError("El formato del ID es incorrecto. Debe ser un número.");
        }
    }

    private void displayShippingDetails(Solicitud solicitud) {
        tvGuideNumber.setText(String.valueOf(solicitud.id));
        tvDeliveryAddress.setText(solicitud.direccion);
        tvCurrentStatus.setText(solicitud.estado);
        tvDeliveryDate.setText(solicitud.fecha);
        tvDeliveryFranja.setText(solicitud.franja);

        int colorResId = getColorForStatus(solicitud.estado);
        tvCurrentStatus.setTextColor(ContextCompat.getColor(this, colorResId));

        cvResults.setVisibility(View.VISIBLE);
    }

    // Métodos auxiliares de UX

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

    /**
     * Oculta el teclado virtual.
     */
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
        switch (status.toUpperCase()) {
            case SolicitudRepository.EST_ENTREGADA:
            case SolicitudRepository.EST_CONFIRMADA:
                return R.color.status_success;
            case SolicitudRepository.EST_EN_CAMINO:
                return R.color.primary_color;
            case SolicitudRepository.EST_PENDIENTE:
            case SolicitudRepository.EST_ASIGNADA:
                return R.color.accent_orange;
            case SolicitudRepository.EST_CANCELADA:
                return R.color.status_error;
            default:
                return R.color.on_surface_secondary;
        }
    }
}