package co.edu.unipiloto.myapplication.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import co.edu.unipiloto.myapplication.R;

public class WelcomeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        Button btnDrivers = findViewById(R.id.btnDrivers);
        Button btnOfficials = findViewById(R.id.btnOfficials);

        // botón conductor
        btnDrivers.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginDriverActivity.class);
            startActivity(i);
        });

        // botón funcionarios
        btnOfficials.setOnClickListener(v -> {
            Intent i = new Intent(WelcomeActivity.this, LoginActivity.class);
            i.putExtra("role", "FUNCIONARIO");
            startActivity(i);
        });
    }
}
