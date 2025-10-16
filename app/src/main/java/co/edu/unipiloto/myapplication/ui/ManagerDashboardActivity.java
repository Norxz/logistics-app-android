package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.adapters.DashboardPagerAdapter;
// Necesitas crear la clase LoginActivity y SessionManager si no existen.
// import co.edu.unipiloto.myapplication.storage.SessionManager;

public class ManagerDashboardActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MaterialButton btnLogout;

    private final String[] tabTitles = new String[]{"PENDIENTES", "ASIGNADAS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        // 1. Inicialización de Vistas
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnLogout = findViewById(R.id.btnLogout);

        // 2. Configuración del ViewPager y TabLayout
        setupViewPagerAndTabs();

        // 3. Configuración del Botón de Logout
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupViewPagerAndTabs() {
        // Inicializa el adaptador de Fragments
        DashboardPagerAdapter adapter = new DashboardPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Conecta el TabLayout con el ViewPager2 usando los títulos definidos
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    private void logout() {
        // Implementa tu lógica de cierre de sesión (ej: limpiar SharedPreferences)
        // new SessionManager(this).clear();

        Intent intent = new Intent(this, LoginActivity.class); // Asegúrate de que LoginActivity exista
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sesión cerrada.", Toast.LENGTH_SHORT).show();
    }
}