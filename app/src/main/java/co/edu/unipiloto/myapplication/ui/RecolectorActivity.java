package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class RecolectorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recolector);

        SessionManager session = new SessionManager(this);

        TextView tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setText("Bienvenido Recolector\nZona: " + session.getZona());

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            new SessionManager(this).clear();   // borra userId y todo
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
