package com.example.myfisiocoach;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import BackendServer.ApiServiceUtente;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DatiDiVerificaActivity extends AppCompatActivity {

    ImageView tornaindietro;
    Button avanza;

    EditText email,nome;
    ApiServiceUtente apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dati_di_verifica);

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

        //Quando premo sulla freccia indietro mi riporta alla LoginActivity con una transizione di schermata
        tornaindietro = findViewById(R.id.TornaIndietroButton);
        tornaindietro.setOnClickListener(v -> {
            startActivity(new Intent(DatiDiVerificaActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        //Quando premo sul bottone avanza mi porta alla CodiceVerificaActivity con una transizione di schermata
        avanza = findViewById(R.id.AvanzaButton);
        nome = findViewById(R.id.editTextTextNomeVerifica);
        avanza.setOnClickListener(v -> {
            //Verifico che i dati siano stati inseriti
            email = findViewById(R.id.editTextTextEmailDiVerifica);
            if(email.getText().toString().isEmpty()){
                email.setError("Devi inserire un'email");
                nome.setText("");
            }else if (nome.getText().toString().isEmpty()){
                nome.setError("Devi inserire il tuo nome");
            }else{

                apiService.AccessToModificaPassword(email.getText().toString(),nome.getText().toString(),"b").enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if(response.isSuccessful() && response.body() != null && response.body()){
                            Intent intent = new Intent(DatiDiVerificaActivity.this, ModificaPasswordActivity.class);
                            intent.putExtra("email", email.getText().toString());
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        }else{
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DatiDiVerificaActivity.this);
                            LayoutInflater inflater = DatiDiVerificaActivity.this.getLayoutInflater();
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
                            message.setText("Utente non trovato, inserisci i dati correttamente.");
                            positiveButton.setText("Ok");
                            negativeButton.setVisibility(View.GONE);
                            positiveButton.setOnClickListener(v1 -> {
                                email.setText("");
                                nome.setText("");
                                alertDialog.dismiss();
                            });
                        }

                    }


                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        System.out.println("Si è verificato un errore nel recupero dell'utente");

                    }
                });

            }
        });
    }
}