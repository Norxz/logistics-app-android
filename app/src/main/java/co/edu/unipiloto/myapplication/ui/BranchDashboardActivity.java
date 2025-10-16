package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.adapters.BranchPagerAdapter;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class BranchDashboardActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private MaterialButton btnLogout;
    // Las pestañas para el funcionario de sucursal
    private final String[] tabTitles = new String[]{"PENDIENTES", "EN RUTA", "FINALIZADOS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout del dashboard de sucursal
        setContentView(R.layout.activity_branch_dashboard);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnLogout = findViewById(R.id.btnLogout);

        setupViewPagerAndTabs();

        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupViewPagerAndTabs() {
        // 🛑 Usamos el adaptador BranchPagerAdapter que crearemos a continuación
        BranchPagerAdapter adapter = new BranchPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Conecta el TabLayout con el ViewPager2 usando los títulos
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    private void logout() {
        new SessionManager(this).clear();
        Intent intent = new Intent(this, LoginFunctionaryActivity.class); // Redirige al login de funcionario
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Sesión de sucursal cerrada.", Toast.LENGTH_SHORT).show();
    }
}