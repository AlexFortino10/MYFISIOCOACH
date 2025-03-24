package com.example.myfisiocoach;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {
    Button iniziaora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        //mi sposto alla pagina di login con una transizione di schermata
        iniziaora = findViewById(R.id.IniziaOraButton);
        iniziaora.setOnClickListener(v -> {
            startActivity(new Intent(StartActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

    }

}