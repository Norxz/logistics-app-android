package co.edu.unipiloto.myapplication.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import co.edu.unipiloto.myapplication.R;
// Asegúrate de que estas clases existan en estas rutas:
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.model.Solicitud;
import co.edu.unipiloto.myapplication.storage.SessionManager;

import java.util.List;
import java.util.Random;

public class DriverDashboardActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;

    private RecyclerView recyclerViewRoutes;
    private TextView tvDriverTitle;
    private TextView tvNoRoutes;
    private MaterialButton btnLogout;
    private SessionManager sessionManager;
    private SolicitudRepository solicitudRepository; // Descomentado e Inicializado

    // Variables temporales para guardar la solicitud mientras se piden permisos
    private String pendingSmsPhone;
    private String pendingSmsCode;
    private String pendingSmsGuiaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_dashboard);

        // Inicialización de vistas
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes);
        tvDriverTitle = findViewById(R.id.tvDriverTitle);
        tvNoRoutes = findViewById(R.id.tvNoRoutes);
        btnLogout = findViewById(R.id.btnLogout);

        sessionManager = new SessionManager(this);
        // Inicializar el Repositorio de Solicitudes
        solicitudRepository = new SolicitudRepository(this);

        tvDriverTitle.setText("Hola, Conductor ID: " + sessionManager.getUserId());

        setupRecyclerView();
        loadAssignedRoutes();

        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupRecyclerView() {
        recyclerViewRoutes.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadAssignedRoutes() {
        long driverId = sessionManager.getUserId();

        // 🛑 Lógica de carga de datos real
        List<Solicitud> assignedRoutes = solicitudRepository.getAssignedRoutesByDriver(driverId);

        if (assignedRoutes != null && !assignedRoutes.isEmpty()) {
            recyclerViewRoutes.setVisibility(View.VISIBLE);
            tvNoRoutes.setVisibility(View.GONE);

            // Debes crear este adaptador e inyectar la Activity/Context:
            // DriverRoutesAdapter adapter = new DriverRoutesAdapter(this, assignedRoutes);
            // recyclerViewRoutes.setAdapter(adapter);
        } else {
            recyclerViewRoutes.setVisibility(View.GONE);
            tvNoRoutes.setVisibility(View.VISIBLE);
        }
    }

    private void logout() {
        sessionManager.clear();
        Intent intent = new Intent(this, LoginDriverActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sesión de conductor cerrada.", Toast.LENGTH_SHORT).show();
    }


    // -------------------------------------------------------------
    // FUNCIONALIDAD DE SMS, CÓDIGO Y BASE DE DATOS
    // -------------------------------------------------------------

    /**
     * 🛑 Método público llamado desde el Adapter al presionar "INICIAR TAREA" 🛑
     * Genera el código, actualiza el estado de la guía y dispara el envío de SMS.
     */
    public void startDeliveryProcess(long solicitudId, String recipientPhone, String guiaId) {
        if (recipientPhone == null || recipientPhone.isEmpty()) {
            Toast.makeText(this, "Error: Número de teléfono del receptor no disponible.", Toast.LENGTH_LONG).show();
            return;
        }

        // 1. Generar Código (4 dígitos)
        String confirmationCode = generateRandomCode(4);

        // 2. Actualizar el estado y guardar el código en la DB
        boolean success = updateSolicitudStatus(solicitudId, "EN CAMINO", confirmationCode);

        if (success) {
            Toast.makeText(this, "Guía " + guiaId + " marcada EN CAMINO. Código: " + confirmationCode, Toast.LENGTH_SHORT).show();

            // 3. Guardar datos temporalmente antes de pedir permiso
            this.pendingSmsPhone = recipientPhone;
            this.pendingSmsCode = confirmationCode;
            this.pendingSmsGuiaId = guiaId;

            // 4. Pedir o verificar permiso antes de enviar el SMS
            checkSmsPermission();
        } else {
            Toast.makeText(this, "ERROR: No se pudo actualizar el estado de la guía.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Actualiza el estado de la solicitud y guarda el código de confirmación en la DB.
     * @return true si la actualización fue exitosa.
     */
    public boolean updateSolicitudStatus(long id, String newStatus, String confirmationCode) {
        // Asumiendo que SolicitudRepository tiene el método updateStatusAndCode
        try {
            int rowsAffected = solicitudRepository.updateStatusAndCode(id, newStatus, confirmationCode);
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método simple para generar un código
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------
    // Gestión de Permisos y Envío de SMS
    // -------------------------------------------------------------

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Pedir el permiso
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            // El permiso ya fue concedido, proceder con el envío.
            executeSendSms();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                executeSendSms();
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de SMS denegado. No se pudo enviar el código de confirmación.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void executeSendSms() {
        if (pendingSmsPhone == null || pendingSmsCode == null) return;

        String message = "Confirmación de Entrega - Guía " + pendingSmsGuiaId +
                ". El conductor está en camino. Su código de confirmación es: " + pendingSmsCode +
                ". Muestre este código al conductor.";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(pendingSmsPhone, null, message, null, null);
            Toast.makeText(this, "SMS de confirmación ENVIADO al receptor: " + pendingSmsPhone, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "FALLÓ el envío de SMS. Asegúrese de tener saldo y señal.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        // Limpiar variables temporales
        this.pendingSmsPhone = null;
        this.pendingSmsCode = null;
        this.pendingSmsGuiaId = null;
    }
}