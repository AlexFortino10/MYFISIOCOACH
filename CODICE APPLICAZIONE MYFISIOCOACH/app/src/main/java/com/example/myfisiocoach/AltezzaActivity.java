package com.example.myfisiocoach;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AltezzaActivity extends AppCompatActivity {
    NumberPicker altezzaPicker;
    TextView accedi;
    Button avanza;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_altezza);

        accedi = findViewById(R.id.textViewAccedi);
        //mi sposto alla pagina di login con una transizione di schermata quando clicco su accedi
        accedi = findViewById(R.id.textViewAccedi);
        accedi.setOnClickListener(v -> {
            startActivity(new Intent(AltezzaActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        altezzaPicker = findViewById(R.id.altezzaPicker);

        // Imposta il range di valori
        altezzaPicker.setMinValue(110);
        altezzaPicker.setMaxValue(220);
        altezzaPicker.setWrapSelectorWheel(true);
        altezzaPicker.setValue(170);

        // Disabilita l'input tramite tastiera
        altezzaPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        //Recupero i dati passati dalla PesoActivity
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String sesso = intent.getStringExtra("sesso");
        int peso = intent.getIntExtra("peso", 65);

        //mi sposto alla pagina per la scelta del altezza con una transizione di schermata quando clicco su avanti
        avanza = findViewById(R.id.AvanzaButton);
        avanza.setOnClickListener(v -> {

                //Salvo il peso scelto
                int altezza = altezzaPicker.getValue();

                //Passo i dati alla CompletaRegistrazioneActivity
                Intent intent1 = new Intent(AltezzaActivity.this, CompletaRegistrazioneActivity.class);
                intent1.putExtra("email", email);
                intent1.putExtra("password", password);
                intent1.putExtra("sesso", sesso);
                intent1.putExtra("peso", peso);
                intent1.putExtra("altezza", altezza);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
        });
    }
}
