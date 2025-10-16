package co.edu.unipiloto.myapplication.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.myapplication.R;

public class DriverActivity extends AppCompatActivity {

    private TextView tvWelcomeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        tvWelcomeTitle = findViewById(R.id.tvWelcomeTitle);

        // Obtener el rol desde el intent
        String role = getIntent().getStringExtra("role");
        if (role != null) {
            // Mostrar el mensaje personalizado
            tvWelcomeTitle.setText("Bienvenido, " + role);
        }
    }
}

