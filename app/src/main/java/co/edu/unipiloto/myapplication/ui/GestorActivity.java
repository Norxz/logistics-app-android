package co.edu.unipiloto.myapplication.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors; // Necesario para la conversión (si usas Java 8+)

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.db.SolicitudRepository;
import co.edu.unipiloto.myapplication.db.UserRepository; // ¡NUEVO!
import co.edu.unipiloto.myapplication.model.Solicitud;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class GestorActivity extends AppCompatActivity {

    private SolicitudRepository repo;
    private UserRepository usersRepo; // Instancia para acceder a los conductores
    private SolicitudAdapter adapter;
    private SessionManager session;
    private RecyclerView rv;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        session = new SessionManager(this);
        repo = new SolicitudRepository(this);
        usersRepo = new UserRepository(this); // Inicialización del repositorio de usuarios

        TextView tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);
        // Cambiamos el mensaje de bienvenida para reflejar el rol de Gestor/Funcionario
        tvWelcomeTitle.setText("Gestión de Envíos\nRol: " + session.getRole());

        rv = findViewById(R.id.rvSolicitudes);
        rv.setLayoutManager(new LinearLayoutManager(this));

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            session.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        cargarLista();
    }

    private void cargarLista() {
        // Obtenemos todas las solicitudes (o al menos las que un Gestor debe ver)
        // **IMPORTANTE**: Necesitas un método en SolicitudRepository que devuelva List<SolicitudRepository.SolicitudItem>
        // Si no existe, usamos la conversión si es posible.

        // 1. Obtener los conductores reales de la DB
        List<UserRepository.ConductorInfo> conductores = usersRepo.getConductores();

        // 2. Obtener las solicitudes que se mostrarán. Usamos el método existente
        //    y lo convertimos a SolicitudItem si es necesario, o creamos un nuevo método en el repo.

        // **ASUMIENDO un nuevo método en SolicitudRepository:**
        List<SolicitudRepository.SolicitudItem> items = repo.getAllSolicitudesAsItem();

        // Si el método getAllSolicitudesAsItem() no existe, tendrías que usar otra lógica
        // o hacer la conversión aquí (si repo.pendientesPorZona devuelve los campos necesarios).

        // 3. Crear el adaptador usando la fábrica para el Funcionario/Gestor
        adapter = SolicitudAdapter.forFuncionario(items);

        // 4. Inyectar los conductores en el adaptador
        adapter.setConductores(conductores);

        rv.setAdapter(adapter);

        // 5. Configurar el NUEVO Listener de Asignación (para el Spinner)
        adapter.setOnAssignListener((solicitudId, conductorId, pos) -> {
            // Lógica para asignar en la DB. Asumimos que tienes el método asignarAConductor.
            int rows = repo.asignarAConductor(solicitudId, conductorId);
            if (rows > 0) {
                adapter.updateEstadoAt(pos, "ASIGNADA");
                Toast.makeText(this, "Solicitud #" + solicitudId + " asignada.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al asignar.", Toast.LENGTH_SHORT).show();
                cargarLista();
            }
        });

        // 6. Remover los listeners de Recolector que ya no son relevantes (o déjalos vacíos)
        // Los otros listeners (Aceptar, EnCamino, Entregado) no son necesarios para la vista de asignación.
        // Los dejamos como stubs, pero asegúrate de que el botón "Aceptar" del layout estándar no se muestre
        // para las solicitudes PENDIENTES, ya que en la vista de Funcionario/Gestor es reemplazado por la vista de asignación.
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Recargamos la lista solo si es necesario, pero si hay cambios en el estado es mejor recargar siempre
        cargarLista();
    }
}