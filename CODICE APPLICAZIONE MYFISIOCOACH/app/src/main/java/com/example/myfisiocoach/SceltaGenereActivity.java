package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class SceltaGenereActivity extends AppCompatActivity {

    ImageView maschio,femmina;
    TextView accedi;
    String sesso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scelta_genere);

        //mi sposto alla pagina di login con una transizione di schermata quando clicco su Accedi con inetent e finish
        accedi = findViewById(R.id.textViewAccedi);
        accedi.setOnClickListener(v2 -> {
            startActivity(new Intent(SceltaGenereActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        //Recupero i dati passati dalla RegistrazioneActivity
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");


        //mi sposto alla pagina del peso con una transizione di schermata quando clicco su femmina
        femmina = findViewById(R.id.ButtonFemmina);
        femmina.setOnClickListener(v1 -> {
            //Inizializzo la variabile sesso con il valore femmina
            sesso= "Femmina";

            //Passo i dati alla PesoActivity
            Intent intent1 = new Intent(SceltaGenereActivity.this, PesoActivity.class);
            intent1.putExtra("email", email);
            intent1.putExtra("password", password);
            intent1.putExtra("sesso", sesso);
            startActivity(intent1);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        //mi sposto alla pagina del peso con una transizione di schermata quando clicco su maschio
        maschio = findViewById(R.id.ButtonMaschio);
        maschio.setOnClickListener(v -> {

            //Inizializzo la variabile sesso con il valore maschio
            sesso= "Maschio";

            //Passo i dati alla PesoActivity
            Intent intent2 = new Intent(SceltaGenereActivity.this, PesoActivity.class);
            intent2.putExtra("email", email);
            intent2.putExtra("password", password);
            intent2.putExtra("sesso", sesso);
            startActivity(intent2);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
}

}