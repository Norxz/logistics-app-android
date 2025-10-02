package co.edu.unipiloto.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.myapplication.R;
import co.edu.unipiloto.myapplication.storage.SessionManager;

public class FunctionaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_delivery);

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Limpiar sesión si existe SessionManager
            SessionManager session = new SessionManager(this);
            session.clear();

            Intent intent = new Intent(FunctionaryActivity.this, LoginFunctionaryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }
}