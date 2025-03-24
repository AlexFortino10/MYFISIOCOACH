package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import BackendServer.ApiServiceUtente;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CompletaRegistrazioneActivity extends AppCompatActivity {

    SeekBar etaSeekBar;
    TextView Età,Accedi;

    EditText Nome;

    Button Conferma;

    ApiServiceUtente apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completa_registrazione);


        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // Configura Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiServiceUtente.class);

        //mi sposto alla pagina di login con una transizione di schermata quando clicco su accedi

        Accedi =findViewById(R.id.textViewAccedi);
        Accedi.setOnClickListener(v -> {
            startActivity(new Intent(CompletaRegistrazioneActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });


        //Imposto il SeekBar
        Età = findViewById(R.id.textViewCounterEtà);
        etaSeekBar = findViewById(R.id.AgeseekBar);
        // Aggiorna il TextView quando cambia il valore del SeekBar
        etaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Età.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Salvo Nome, Cognome e Età
        Nome = findViewById(R.id.editTextTextNome);
        Età = findViewById(R.id.textViewCounterEtà);

        //Recupero i dati passati dalla AltezzaActivity
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String sesso = intent.getStringExtra("sesso");
        int peso = intent.getIntExtra("peso", 65);
        int altezza = intent.getIntExtra("altezza", 170);

        //mi sposto alla pagina di login con una transizione di schermata quando clicco su Conferma

        Conferma =findViewById(R.id.ConfermaRegistrazioneButton);
        Conferma.setOnClickListener(v -> {

            //Controllo che il nome e il cognome siano stati inseriti
            if(Nome.getText().toString().isEmpty()){
                Nome.setError("Devi inserire un nome");
            } else {
                String nomepersona = Nome.getText().toString();
                String cognomepersona = "b";
                int età = etaSeekBar.getProgress();

                //Registro l'utente nel server
                apiService.registraUtente(email,password,nomepersona,cognomepersona,peso,altezza,età,sesso).enqueue(new retrofit2.Callback<Boolean>() {
                    @Override
                    public void onResponse(retrofit2.Call<Boolean> call, retrofit2.Response<Boolean> response) {
                        if(response.isSuccessful() && response.body() != null && response.body()){
                            //Mostro una schermata di benvenuto e mi sposto alla pagina delle patologie
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CompletaRegistrazioneActivity.this);
                            LayoutInflater inflater = CompletaRegistrazioneActivity.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                            builder.setView(dialogView);
                            TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                            Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                            Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                            android.app.AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                            alertDialog.setCancelable(false);

                            title.setText("Congratulazioni");
                            title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.trophy__2_,0);
                            title.setCompoundDrawablePadding(10);
                            message.setText("Registrazione avvenuta con successo,BENVENUTO IN MYFISIOCOACH!");
                            positiveButton.setText("Ok");
                            negativeButton.setVisibility(View.GONE);
                            positiveButton.setOnClickListener(v1 -> {
                                alertDialog.dismiss();
                                //passo l'email alla prossima activity
                                Intent intent1 = new Intent(CompletaRegistrazioneActivity.this, PatologieActivity.class);
                                intent1.putExtra("email", email);
                                startActivity(intent1);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            });
                        } else {
                            System.out.println("Errore nella registrazione");
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(CompletaRegistrazioneActivity.this);
                            LayoutInflater inflater = CompletaRegistrazioneActivity.this.getLayoutInflater();
                            View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                            builder.setView(dialogView);
                            TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                            Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                            Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                            android.app.AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                            alertDialog.setCancelable(false);


                            title.setText("Errore");
                            title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                            title.setCompoundDrawablePadding(10);
                            message.setText("Errore nella registrazione,Verrai reindirizzato alla pagina di login");
                            positiveButton.setText("Ok");
                            negativeButton.setVisibility(View.GONE);
                            positiveButton.setOnClickListener(v1 -> {
                                alertDialog.dismiss();
                                startActivity(new Intent(CompletaRegistrazioneActivity.this, LoginActivity.class));
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            });
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Boolean> call, Throwable t) {
                        System.out.println("Errore nella registrazione");
                    }
                });
            }
        });


    }

}