package com.example.myfisiocoach;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import BackendServer.ApiServiceUtente;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class VerificaPasswordActivity extends AppCompatActivity {

    EditText passwordattuale;

    Button verifica;

    ImageView tornaindietrobutton;

    Boolean passwordvisibile = false;

    ApiServiceUtente apiServiceUtente;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifica_password);

        //Definisco il servizio per intercepire le richieste al server
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // Configura Retrofit per i dati dell'utente

        Retrofit retrofitUtente = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        apiServiceUtente = retrofitUtente.create(ApiServiceUtente.class);

        //Setto gli elementi della view
        passwordattuale = findViewById(R.id.editTextTextPasswordAttuale);
        verifica = findViewById(R.id.VerificaButton);
        tornaindietrobutton = findViewById(R.id.TornaProfiloIndietroButton);

        //Recupero l'email dall'intent
        String email = getIntent().getStringExtra("email");

        //Quando premo il bottone verifica controllo se la password inserita è corretta
        verifica.setOnClickListener(v -> {
            String password = passwordattuale.getText().toString();

            apiServiceUtente.verificaUtente(email, password).enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(retrofit2.Call<Boolean> call, retrofit2.Response<Boolean> response) {
                    if(response.isSuccessful() && response.body() != null  && response.body()){
                        //Se la password è corretta appare un alert dialog che mi avvisa che la password è corretta e che la modifica della password verrà abilitata

                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(VerificaPasswordActivity.this);
                        LayoutInflater inflater = VerificaPasswordActivity.this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                        builder.setView(dialogView);
                        TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                        Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                        Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                        android.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        title.setText("Password corretta");
                        title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                        title.setCompoundDrawablePadding(10);
                        message.setText("La password è corretta, ora puoi modificare la password");
                        positiveButton.setText("Ok");
                        negativeButton.setVisibility(View.GONE);
                        positiveButton.setOnClickListener(v1 -> {
                            Intent intent = new Intent(VerificaPasswordActivity.this, ProfiloActivity.class);
                            intent.putExtra("abilitato", true);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        });


                    } else {
                        //Se la password è sbagliata mostro un messaggio di errore
                        passwordattuale.setError("Password errata");
                        passwordattuale.setText("");
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Boolean> call, Throwable t) {
                    Log.e("VerificaPasswordActivity", "Errore nella verifica della password", t);
                }

            });
        });


        //Quando premo il bottone tornaindietro torno alla pagina del profilo se la password è vuota mostro un alert dialog che mi avvisa che la password è vuota e che tornando indietro non sarà possibile modificare la password
        tornaindietrobutton.setOnClickListener(v -> {

            if(passwordattuale.getText().toString().equals("")){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                builder.setView(dialogView);
                TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();

                title.setText("Password vuota");
                title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                title.setCompoundDrawablePadding(10);
                message.setText("La password è vuota, sei sicuro di voler tornare indietro? Non potrai modificare la password");
                positiveButton.setText("Torna indietro");
                negativeButton.setText("Annulla");
                negativeButton.setOnClickListener(v1 -> {
                    alertDialog.dismiss();
                });
                positiveButton.setOnClickListener(v1 -> {
                    Intent intent = new Intent(VerificaPasswordActivity.this, ProfiloActivity.class);
                    intent.putExtra("abilitato", false);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    finish();
                });
            }else{
               //Se la password non è vuota viene mostrato un alert dialog che mi avvisa che la password non è vuota e che deve essere verificata
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                builder.setView(dialogView);
                TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                android.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();

                title.setText("Password non vuota");
                title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                title.setCompoundDrawablePadding(10);
                message.setText("La password non è vuota.Si prega di verficare la password cliccando sul bottone apposito");
                positiveButton.setText("Ok");
                negativeButton.setVisibility(View.GONE);
                positiveButton.setOnClickListener(v1 -> {
                    alertDialog.dismiss();
                });

            }

        });

        // Gestione della visibilità della password
        passwordattuale.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (passwordattuale.getRight() - passwordattuale.getCompoundDrawables()[2].getBounds().width())) {
                    if (!passwordvisibile) {
                        passwordattuale.setTransformationMethod(null);
                        passwordattuale.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.closed_eyes__1_, 0);
                        passwordvisibile = true;
                    } else {
                        passwordattuale.setTransformationMethod(new PasswordTransformationMethod());
                        passwordattuale.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        passwordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });




    }


}