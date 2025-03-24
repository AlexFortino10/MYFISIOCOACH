package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import Adapter.AdapterCardPatologia;
import BackendServer.ApiServicePatologie;
import Model.Patologie;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PatologieActivity extends AppCompatActivity {

    Button confermaPatologie;

    RecyclerView recyclerViewPatologie;
    RecyclerView.Adapter adapterPatologie;
    ImageView tornaIndietro;

    //Array di patologie
    ArrayList<Patologie> itemsPatologie = new ArrayList<>();

    String email;

    ApiServicePatologie apiServicePatologie;

    @SuppressLint({"MissingInflatedId", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patologie);

        //Definisco il servizio per intercepire le richieste al server
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        // Configura Retrofit per le patologie

        Retrofit retrofitPatologie = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SharedPreferences sharedPrefFirstAccess = getSharedPreferences("FirstAccessPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorFirstAccess = sharedPrefFirstAccess.edit();
        editorFirstAccess.putBoolean("firstAccess", true);
        editorFirstAccess.putInt("dayfirstaccess", 0);
        editorFirstAccess.putBoolean("firstdialog", false);
        editorFirstAccess.apply();

        apiServicePatologie = retrofitPatologie.create(ApiServicePatologie.class);

        //Recupero l'email dell'utente
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        //Recupero il nome delle patologie presenti nel server e inizializzo il recyclerview
        apiServicePatologie.getNomePatologie().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<String> nomepatologie = response.body();
                    initRecyclerview(nomepatologie,email);
                } else {
                    System.out.println("Errore nella stampa delle patologie");
                }

            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                System.out.println("Errore nella stampa delle patologie");
            }
        });
        tornaIndietro = findViewById(R.id.TornaIndietroButtonPatologie);
        //Torno indietro alla login activity con una transizione di schermata
        tornaIndietro.setOnClickListener(v -> {
            startActivity(new Intent(PatologieActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });


        //Quando clicco su conferma vado alla homepage con una transizione di schermata
        confermaPatologie = findViewById(R.id.ConfermaPatologieButton);
        confermaPatologie.setOnClickListener(v -> {
            //Mostro un dialog in cui esce scritto il messaggio "Ricontrolla attentamente le patologie selezionate poichè possono essere modificate solamente dal tuo medico. Sei sicuro di voler confermare?"
            //Se clicco su "Conferma" allora vado alla homepage
            AlertDialog.Builder builder = new AlertDialog.Builder(PatologieActivity.this);
            LayoutInflater inflater = LayoutInflater.from(PatologieActivity.this);
            View dialogView = inflater.inflate(R.layout.alert_dialog, null);
            builder.setView(dialogView);
            TextView title = dialogView.findViewById(R.id.Titlealertdialog);
            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
            Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
            Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);


            title.setText("Attenzione");
            title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
            title.setCompoundDrawablePadding(10);
            message.setText("Ricontrolla attentamente le patologie selezionate poichè possono essere modificate solamente dal tuo medico. Sei sicuro di voler confermare?");
            positiveButton.setText("Si");
            negativeButton.setText("No");
            negativeButton.setOnClickListener(v1 -> {
                alertDialog.dismiss();
            });
            positiveButton.setOnClickListener(v12 -> {
                alertDialog.dismiss();
                if (!checkNotificationPermission() || !checkAlarmPermission()){
                    //Passo l'email all'activity AttivaNotifiche
                    Intent intent1 = new Intent(PatologieActivity.this, AttivaNotifiche.class);
                    intent1.putExtra("email", email);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)//Controllo se il microfoo è attivo, in caso contrario mostro la pagina AttivaMicrofono
                {
                    //Passo l'email all'activity AttivaMicrofono
                    Intent intent1 = new Intent(PatologieActivity.this, AttivaMicrofonoActivity.class);
                    intent1.putExtra("email", email);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }else {
                    //Passo l'email all'activity HomepageActivity
                    Intent intent1 = new Intent(PatologieActivity.this, HomepageActivity.class);
                    intent1.putExtra("email", email);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();

                }
            });
        });



    }

    private void initRecyclerview(List<String> nomePatologie,String email) {
        //Inizializzo il recyclerview con le patologie
        for (String nome : nomePatologie) {
            itemsPatologie.add(new Patologie(nome,email));
        }
        recyclerViewPatologie = findViewById(R.id.RecyclerViewPatologie);
        recyclerViewPatologie.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        adapterPatologie = new AdapterCardPatologia(itemsPatologie);
        recyclerViewPatologie.setAdapter(adapterPatologie);
    }

    // Controlla il permesso per le notifiche (Android 13+)
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Versioni precedenti non richiedono il permesso esplicito
    }

    // Controlla il permesso per gli allarmi esatti (Android 12+)
    private boolean checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Versioni precedenti non richiedono il permesso esplicito
    }
}