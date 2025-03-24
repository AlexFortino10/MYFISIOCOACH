package com.example.myfisiocoach;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PesoActivity extends AppCompatActivity {
    NumberPicker agePicker;
    TextView accedi;
    Button avanza;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peso);

        accedi = findViewById(R.id.textViewAccedi);
        //mi sposto alla pagina di login con una transizione di schermata quando clicco su accedi
        accedi = findViewById(R.id.textViewAccedi);
        accedi.setOnClickListener(v -> {
            startActivity(new Intent(PesoActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        agePicker = findViewById(R.id.pesoPicker);

        // Imposta il range di valori
        agePicker.setMinValue(40);
        agePicker.setMaxValue(160);
        agePicker.setWrapSelectorWheel(true);
        agePicker.setValue(65);

        // Disabilita l'input tramite tastiera
        agePicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        //Recupero i dati passati dalla SceltaGenereActivity
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String sesso = intent.getStringExtra("sesso");

        //mi sposto alla pagina per la scelta del altezza con una transizione di schermata quando clicco su avanti
        avanza = findViewById(R.id.AvanzaButton);
        avanza.setOnClickListener(v -> {

            //Salvo il peso scelto
            int peso = agePicker.getValue();

            //Passo i dati alla AltezzaActivity
            Intent intent1 = new Intent(PesoActivity.this, AltezzaActivity.class);
            intent1.putExtra("email", email);
            intent1.putExtra("password", password);
            intent1.putExtra("sesso", sesso);
            intent1.putExtra("peso", peso);
            startActivity(intent1);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();

        });
    }
}
