package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import Adapter.AdapterCardLeTuePatologie;
import BackendServer.ApiServicePatologie;
import BackendServer.ApiServiceUtente;
import Model.Patologie;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ProfiloActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "MyPrefsFile";

    BottomNavigationView navigationmenu;

    RecyclerView recyclerviewLeTuePatologie;
    RecyclerView.Adapter adapterPatologie;

    TextView Nome,Età, Peso, Altezza,Email,Password,ConfermaPassword,CounterEta, CounterAltezza, CounterPeso;
    EditText EditNome,EditEmail,EditPassword,EditConfermaPassword;

    SeekBar Seeketa, SeekAltezza, Seekpeso;

    ArrayList<Patologie> itemsPatologieUtente = new ArrayList<>();

    ImageButton ModificaEmail,ModificaPassword;

    Button confermacredenziali,DisconnettiButton;
    Boolean passwordvisibile = false,ConfermaPasswordvisibile = false;

    Boolean EmailEnabled = false, PasswordEnabled = false;

    ApiServiceUtente apiServiceDatiStringUtente,apiServiceDatiIntegerUtente;

    ApiServicePatologie apiServicePatologie;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        //Definisco il servizio per intercepire le richieste al server
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();


        // Configura Retrofit per i dati string dell'utente

        Retrofit retrofitDatiStringUtente = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        apiServiceDatiStringUtente = retrofitDatiStringUtente.create(ApiServiceUtente.class);

        // Configura Retrofit per i dati integer dell'utente

        Retrofit retrofitDatiIntegerUtente = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceDatiIntegerUtente = retrofitDatiIntegerUtente.create(ApiServiceUtente.class);

        // Configura Retrofit per le patologie

        Retrofit retrofitPatologie = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServicePatologie = retrofitPatologie.create(ApiServicePatologie.class);



        // Recupera SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);





        //Setto il menu di navigazione
        navigationmenu = findViewById(R.id.bottomNavigationViewProfilo);

        //Setto l'elemento selezionato
        navigationmenu.setSelectedItemId(R.id.Profilo);


        //Ricevo l'email dell'utente
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        //Setto il listener per il menu di navigazione spostandomi sulle rispettive activity con una transizione di schermata
        navigationmenu.setOnNavigationItemSelectedListener(item -> {
            if (R.id.Home == item.getItemId()) {
                //Invio l'email dell'utente alla HomepageActivity
                Intent intent1 = new Intent(getApplicationContext(), HomepageActivity.class);
                intent1.putExtra("email", email);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (R.id.Resoconto == item.getItemId()) {
                Intent intent2 = new Intent(getApplicationContext(), ResocontoActivity.class);
                intent2.putExtra("email", email);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (R.id.Impostazioni == item.getItemId()) {
                //Invio l'email dell'utente alla ImpostazioniActivity
                Intent intent3 = new Intent(getApplicationContext(), ImpostazioniActivity.class);
                intent3.putExtra("email", email);
                startActivity(intent3);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (R.id.Profilo == item.getItemId()) {
                return true;
            } else if (R.id.MyFisio == item.getItemId()) {

                //Mostro un dialog all'utente che informa che il dialogo con l'avatar è possibile solo nella homepage
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
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
                message.setText("Il dialogo con l'avatar è possibile solo nella homepage");
                positiveButton.setText("Ok");
                negativeButton.setVisibility(View.GONE);
                positiveButton.setOnClickListener(v1 -> {
                    alertDialog.dismiss();
                });

                return true;

            }
            return false;
        });

        //Setto gli elementi presenti nel xml per la modifica dei dati personali
        Nome = findViewById(R.id.textViewModificaNome);
        Età = findViewById(R.id.textViewModificaEtà);
        Peso = findViewById(R.id.textViewModificaPeso);
        Altezza = findViewById(R.id.textViewModificaAltezza);
        CounterEta = findViewById(R.id.textViewModificaCounterEta);
        CounterAltezza = findViewById(R.id.textViewModificaCounterAltezza);
        CounterPeso = findViewById(R.id.textViewModificaCounterPeso);
        EditNome = findViewById(R.id.editTextTextModificaNome);
        Seeketa = findViewById(R.id.ModificaAgeseekBar);
        SeekAltezza = findViewById(R.id.ModificaAltezzaseekBar);
        Seekpeso = findViewById(R.id.ModificaPesoseekBar);
        //Disabilito gli elementi presenti nel xml
        EditNome.setEnabled(false);
        Seeketa.setEnabled(false);
        SeekAltezza.setEnabled(false);
        Seekpeso.setEnabled(false);

        //Recupero i dati dell'utente

        apiServiceDatiStringUtente.getNomeUtente(email).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EditNome.setText(response.body());
                } else {
                    Log.e("API", "Errore durante il recupero del nome dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Errore durante il recupero del nome dell'utente", t);
            }
        });

        apiServiceDatiIntegerUtente.getEtaUtente(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CounterEta.setText(String.valueOf(response.body()));
                    Seeketa.setProgress(response.body());
                } else {
                    Log.e("API", "Errore durante il recupero dell'età dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("API", "Errore durante il recupero dell'età dell'utente", t);
            }

        });

        apiServiceDatiIntegerUtente.getPesoUtente(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CounterPeso.setText(String.valueOf(response.body()));
                    Seekpeso.setProgress(response.body());
                } else {
                    Log.e("API", "Errore durante il recupero del peso dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("API", "Errore durante il recupero del peso dell'utente", t);
            }

        });

        apiServiceDatiIntegerUtente.getAltezzaUtente(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CounterAltezza.setText(String.valueOf(response.body()));
                    SeekAltezza.setProgress(response.body());
                } else {
                    Log.e("API", "Errore durante il recupero dell'altezza dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("API", "Errore durante il recupero dell'altezza dell'utente", t);
            }
        });

        //Setto gli elementi presenti nel xml per la modifica delle credenziali
        ModificaEmail = findViewById(R.id.imageButtonEmail);
        ModificaPassword = findViewById(R.id.imageButtonPassword);
        confermacredenziali = findViewById(R.id.buttonconfermamodifichecredenziali);
        EditEmail = findViewById(R.id.editTextTextModificaEmailAddress);
        EditPassword = findViewById(R.id.editTextTextModificaPassword);
        EditConfermaPassword = findViewById(R.id.editTextTextModificaConfermaPassword);
        Email = findViewById(R.id.textViewModificaEmail);
        Password = findViewById(R.id.textViewModificaPassword);
        ConfermaPassword = findViewById(R.id.textViewModificaConfermaPassword);

        //Disabilito gli elementi presenti nel xml
        EditEmail.setEnabled(false);
        EditPassword.setEnabled(false);
        EditConfermaPassword.setEnabled(false);
        confermacredenziali.setEnabled(false);

        //Setto i valori degli elementi presenti nel xml
        EditEmail.setText(email);

        apiServiceDatiStringUtente.getPasswordUtente(email).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EditPassword.setText(response.body());
                    EditConfermaPassword.setText(response.body());
                } else {
                    Log.e("API", "Errore durante il recupero della password dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Errore durante il recupero della password dell'utente", t);
            }
        });




        //Setto le variabii per i confronti
        String EmailCampo = EditEmail.getText().toString();
        String PasswordCampo = EditPassword.getText().toString();

        //Se il bottone ModificaEmail è settato su enabled=false allora l'edittext viene abilitato e il bottone viene settato su enabled=true
        ModificaEmail.setOnClickListener(v -> {
            if (!EmailEnabled) {
                EditEmail.setEnabled(true);
                EditEmail.setBackground(getResources().getDrawable(R.drawable.inputtextstyle));
                EditEmail.setTextColor(getResources().getColor(R.color.BluFisio));
                EditEmail.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
                ModificaEmail.setImageResource(R.drawable.baseline_edit_off_40);
                Email.setTextColor(getResources().getColor(R.color.white));
                EmailEnabled = true;
                if (!confermacredenziali.isEnabled()) {
                    confermacredenziali.setEnabled(true);
                    confermacredenziali.setBackground(getResources().getDrawable(R.drawable.stylebutton));
                    confermacredenziali.setTextColor(getResources().getColor(R.color.BluFisio));
                    confermacredenziali.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
                }
            } else {
                EditEmail.setEnabled(false);
                EditEmail.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
                EditEmail.setTextColor(getResources().getColor(R.color.white));
                EditEmail.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                ModificaEmail.setImageResource(R.drawable.baseline_edit_24);
                Email.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
                EmailEnabled = false;
                if (EmailCampo.equals(EditEmail.getText().toString()) && PasswordCampo.equals(EditPassword.getText().toString()) && !PasswordEnabled) {
                    //Viusalizzo un alert dialog che mi dice che non sono state apportate modifiche disabilitando quindi il bottone per confermare le modifiche

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
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
                    message.setText("Non sono state apportate modifiche ai campi pertanto il bottone per confermare le modifiche verrà disabilitato");
                    positiveButton.setText("Ok");
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setOnClickListener(v1 -> {
                        confermacredenziali.setEnabled(false);
                        confermacredenziali.setBackground(getResources().getDrawable(R.drawable.stylebuttondisabled));
                        confermacredenziali.setTextColor(getResources().getColor(R.color.white));
                        confermacredenziali.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                        alertDialog.dismiss();
                    });
                }

            }
        });


        //Se il bottone ModificaPassword è settato su enabled=false allora l'edittext viene abilitato e il bottone viene settato su enabled=true
        ModificaPassword.setOnClickListener(v -> {
            if (!PasswordEnabled) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
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
                message.setText("Per modificare la password è necessario verificare la tua identità, pertanto sarai reindirizzato alla pagina di verifica della password");
                positiveButton.setText("Ok");
                negativeButton.setVisibility(View.GONE);
                positiveButton.setOnClickListener(v1 -> {
                    //Invio l'email dell'utente alla VerificaPasswordActivity
                    Intent intent1 = new Intent(getApplicationContext(), VerificaPasswordActivity.class);
                    intent1.putExtra("email", email);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                    alertDialog.dismiss();
                });
            } else {
                PasswordEnabled = false;
                EditPassword.setEnabled(false);
                EditPassword.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
                EditPassword.setTextColor(getResources().getColor(R.color.white));
                EditPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                ModificaPassword.setImageResource(R.drawable.baseline_edit_24);
                Password.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
                EditConfermaPassword.setEnabled(false);
                EditConfermaPassword.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
                EditConfermaPassword.setTextColor(getResources().getColor(R.color.white));
                EditConfermaPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                ConfermaPassword.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
                //Le password devono essere nascoste
                EditPassword.setTransformationMethod(new PasswordTransformationMethod());
                EditPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                passwordvisibile = false;
                EditConfermaPassword.setTransformationMethod(new PasswordTransformationMethod());
                EditConfermaPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                ConfermaPasswordvisibile = false;

                if (PasswordCampo.equals(EditPassword.getText().toString()) && EmailCampo.equals(EditEmail.getText().toString()) && !EmailEnabled) {
                    //Viusalizzo un alert dialog che mi dice che non sono state apportate modifiche disabilitando quindi il bottone per confermare le modifiche
                    //Viusalizzo un alert dialog che mi dice che non sono state apportate modifiche disabilitando quindi il bottone per confermare le modifiche

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
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
                    message.setText("Non sono state apportate modifiche ai campi pertanto il bottone per confermare le modifiche verrà disabilitato");
                    positiveButton.setText("Ok");
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setOnClickListener(v1 -> {
                        confermacredenziali.setEnabled(false);
                        confermacredenziali.setBackground(getResources().getDrawable(R.drawable.stylebuttondisabled));
                        confermacredenziali.setTextColor(getResources().getColor(R.color.white));
                        confermacredenziali.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                        alertDialog.dismiss();
                    });
                }
            }
        });

        //Recupero i dati dall'activity VerificaPasswordActivity
        Intent intent1 = getIntent();
        Boolean abilitato = intent1.getBooleanExtra("abilitato", false);

        //Se la password è stata verificata allora abilito la modifica della password
        if(abilitato)
        {

            EditPassword.setEnabled(true);
            EditPassword.setBackground(getResources().getDrawable(R.drawable.inputtextstyle));
            EditPassword.setTextColor(getResources().getColor(R.color.BluFisio));
            EditPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
            Password.setTextColor(getResources().getColor(R.color.white));
            ModificaPassword.setImageResource(R.drawable.baseline_edit_off_40);
            EditConfermaPassword.setEnabled(true);
            EditConfermaPassword.setBackground(getResources().getDrawable(R.drawable.inputtextstyle));
            EditConfermaPassword.setTextColor(getResources().getColor(R.color.BluFisio));
            EditConfermaPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
            ConfermaPassword.setTextColor(getResources().getColor(R.color.white));
            PasswordEnabled = true;
            if (!confermacredenziali.isEnabled()) {
                confermacredenziali.setEnabled(true);
                confermacredenziali.setBackground(getResources().getDrawable(R.drawable.stylebutton));
                confermacredenziali.setTextColor(getResources().getColor(R.color.BluFisio));
                confermacredenziali.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
            }

        } else {

            EditPassword.setEnabled(false);
            EditPassword.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
            EditPassword.setTextColor(getResources().getColor(R.color.white));
            EditPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
            ModificaPassword.setImageResource(R.drawable.baseline_edit_24);
            Password.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
            PasswordEnabled = false;
            EditConfermaPassword.setEnabled(false);
            EditConfermaPassword.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
            EditConfermaPassword.setTextColor(getResources().getColor(R.color.white));
            EditConfermaPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
            ConfermaPassword.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
        }

        //Se premo sul drawable della password allora visualizzo la password e cambia l'icona
        EditPassword.setOnTouchListener((v1, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (EditPassword.getRight() - EditPassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (!passwordvisibile) {
                        EditPassword.setTransformationMethod(null);
                        EditPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.closed_eyes__1_, 0);
                        passwordvisibile = true;
                    } else {
                        EditPassword.setTransformationMethod(new PasswordTransformationMethod());
                        EditPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        passwordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });

        //Se premo sul drawable della conferma password allora visualizzo la password e cambia l'icona
        EditConfermaPassword.setOnTouchListener((v2, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (EditConfermaPassword.getRight() - EditConfermaPassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (!ConfermaPasswordvisibile) {
                        EditConfermaPassword.setTransformationMethod(null);
                        EditConfermaPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.closed_eyes__1_, 0);
                        ConfermaPasswordvisibile = true;
                    } else {
                        EditConfermaPassword.setTransformationMethod(new PasswordTransformationMethod());
                        EditConfermaPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        ConfermaPasswordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });


        //Setto il bottone per confermare le modifiche
        confermacredenziali.setOnClickListener(v -> {
            //Controllo che i campi siano stati modificati
            if (EditEmail.getText().toString().equals(EmailCampo) && EditPassword.getText().toString().equals(PasswordCampo) && EditConfermaPassword.getText().toString().equals(PasswordCampo)) {
                //Visualizzo un alert dialog che mi dice che non sono state apportate modifiche e che dunque sia il bottone che i campi verranno disabilitati
                //Viusalizzo un alert dialog che mi dice che non sono state apportate modifiche disabilitando quindi il bottone per confermare le modifiche

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
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
                message.setText("Non sono state apportate modifiche ai campi pertanto sia i campi che il bottone per confermare le modifiche verranno disabilitati");
                positiveButton.setText("Ok");
                negativeButton.setVisibility(View.GONE);
                positiveButton.setOnClickListener(v1 -> {
                       confermacredenziali.setEnabled(false);
                        confermacredenziali.setBackground(getResources().getDrawable(R.drawable.stylebuttondisabled));
                        confermacredenziali.setTextColor(getResources().getColor(R.color.white));
                        confermacredenziali.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                        EditEmail.setEnabled(false);
                        EditEmail.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
                        EditEmail.setTextColor(getResources().getColor(R.color.white));
                        EditEmail.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                        ModificaEmail.setImageResource(R.drawable.baseline_edit_24);
                        Email.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
                        EmailEnabled = false;
                        EditPassword.setEnabled(false);
                        EditPassword.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
                        EditPassword.setTextColor(getResources().getColor(R.color.white));
                        EditPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                        ModificaPassword.setImageResource(R.drawable.baseline_edit_24);
                        Password.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
                        PasswordEnabled = false;
                        EditConfermaPassword.setEnabled(false);
                        EditConfermaPassword.setBackground(getResources().getDrawable(R.drawable.inputstyledisabled));
                        EditConfermaPassword.setTextColor(getResources().getColor(R.color.white));
                        EditConfermaPassword.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
                        ConfermaPassword.setTextColor(getResources().getColor(R.color.GrigioDisattivo));

                        //Ripristino i valori dei campi
                        EditEmail.setText(email);

                        apiServiceDatiStringUtente.getPasswordUtente(email).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    EditPassword.setText(response.body());
                                    EditConfermaPassword.setText(response.body());
                                } else {
                                    Log.e("API", "Errore durante il recupero della password dell'utente", new Exception(response.message()));
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.e("API", "Errore durante il recupero della password dell'utente", t);
                            }
                        });


                        //Le password devono essere nascoste
                        EditPassword.setTransformationMethod(new PasswordTransformationMethod());
                        EditPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        passwordvisibile = false;
                        EditConfermaPassword.setTransformationMethod(new PasswordTransformationMethod());
                        EditConfermaPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        ConfermaPasswordvisibile = false;
                        alertDialog.dismiss();

                });
            } else {
                //Verifico la validità di email e password
                //Controllo che l'email sia valida
                if(EditEmail.getText().toString().isEmpty()){

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                    builder.setView(dialogView);
                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);

                    title.setText("Errore");
                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                    title.setCompoundDrawablePadding(10);
                    message.setText("Il campo username non può essere vuoto pertanto saranno ripristinati i tuoi vecchi dati");
                    positiveButton.setText("Ok");
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setOnClickListener(v1 -> {
                        EditEmail.setText(EmailCampo);
                        EditPassword.setText(PasswordCampo);
                        EditConfermaPassword.setText(PasswordCampo);
                        alertDialog.dismiss();
                    });

                } else {

                                    EditEmail.setError(null);
                                    // controllo che la password sia stata inserita rispettando i requisiti come lunghezza minima di 10 caratteri
                                    //Almeno un carattere maiuscolo e uno maiuscolo
                                    //Almeno un numero e almeno un carattere speciale
                                    if (EditPassword.getText().toString().isEmpty()) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                                        LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
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
                                        message.setText("Hai lasciato vuoto il campo password, pertanto saranno ripristinati i tuoi vecchi dati");
                                        positiveButton.setText("Ok");
                                        negativeButton.setVisibility(View.GONE);
                                        positiveButton.setOnClickListener(v1 -> {
                                            EditEmail.setText(EmailCampo);
                                            EditPassword.setText(PasswordCampo);
                                            EditConfermaPassword.setText(PasswordCampo);
                                            alertDialog.dismiss();
                                        });
                                    } else if ((EditPassword.getText().toString().length() < 10) || !EditPassword.getText().toString().matches(".*[A-Z].*") || !EditPassword.getText().toString().matches(".*[a-z].*") || !EditPassword.getText().toString().matches(".*[0-9].*") || !EditPassword.getText().toString().matches(".*[!@#$%^&*].*")) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                                        LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
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
                                        message.setText("La password deve contenere almeno 10 caratteri tra cui almeno un carattere maiuscolo,un carattere minuscolo,un numero e un carattere speciale (!@#$%^&*). Pertanto saranno ripristinati i tuoi vecchi dati");
                                        positiveButton.setText("Ok");
                                        negativeButton.setVisibility(View.GONE);
                                        positiveButton.setOnClickListener(v1 -> {
                                            EditEmail.setText(EmailCampo);
                                            EditPassword.setText(PasswordCampo);
                                            EditConfermaPassword.setText(PasswordCampo);
                                            alertDialog.dismiss();
                                        });
                                    } else {

                                        //Controllo che la password e la conferma password siano uguali
                                        if (!EditPassword.getText().toString().equals(EditConfermaPassword.getText().toString())) {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                                            LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
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
                                            message.setText("La password e la conferma password non coincidono, pertanto saranno ripristinati i tuoi vecchi dati");
                                            positiveButton.setText("Ok");
                                            negativeButton.setVisibility(View.GONE);
                                            positiveButton.setOnClickListener(v1 -> {
                                                EditEmail.setText(EmailCampo);
                                                EditPassword.setText(PasswordCampo);
                                                EditConfermaPassword.setText(PasswordCampo);
                                                alertDialog.dismiss();
                                            });
                                        } else {
                                            //Effettuo le modifiche

                                            apiServiceDatiIntegerUtente.modificaEmailPassword(email, EditEmail.getText().toString(), EditPassword.getText().toString()).enqueue(new Callback<Boolean>() {
                                                @Override
                                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                    if (response.isSuccessful() && response.body() != null && response.body()) {

                                                        apiServicePatologie.modificaEmailUtente(email,EditEmail.getText().toString()).enqueue(new Callback<Boolean>() {
                                                            @Override
                                                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                if (response.isSuccessful() && response.body() != null && response.body()) {

                                                                    salvaCredenziali();

                                                                    //Visualizzo un alert dialog che mi dice che le modifiche sono state apportate con successo
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                                                                    LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
                                                                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                                    builder.setView(dialogView);
                                                                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                                    AlertDialog alertDialog = builder.create();
                                                                    alertDialog.show();
                                                                    alertDialog.setCancelable(false);

                                                                    title.setText("Successo");
                                                                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.trophy__2_,0);
                                                                    title.setCompoundDrawablePadding(10);
                                                                    message.setText("Modifiche apportate con successo");
                                                                    positiveButton.setText("Ok");
                                                                    negativeButton.setVisibility(View.GONE);
                                                                    positiveButton.setOnClickListener(v1 -> {
                                                                        //Ricarico la pagina
                                                                        Intent intent2 = new Intent(getApplicationContext(), ProfiloActivity.class);
                                                                        intent2.putExtra("email", EditEmail.getText().toString());
                                                                        startActivity(intent2);
                                                                        overridePendingTransition(0, 0);
                                                                        finish();
                                                                        alertDialog.dismiss();
                                                                    });



                                                                } else {

                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                                                                    LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
                                                                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                                    builder.setView(dialogView);
                                                                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                                    AlertDialog alertDialog = builder.create();
                                                                    alertDialog.show();
                                                                    alertDialog.setCancelable(false);

                                                                    title.setText("Errore");
                                                                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                                                                    title.setCompoundDrawablePadding(10);
                                                                    message.setText("Errore nella modifica del campo email nelle patologie  ");
                                                                    positiveButton.setText("Ok");
                                                                    negativeButton.setVisibility(View.GONE);
                                                                    positiveButton.setOnClickListener(v1 -> {
                                                                        //Ricarico la pagina
                                                                        Intent intent2 = new Intent(getApplicationContext(), ProfiloActivity.class);
                                                                        intent2.putExtra("email", EditEmail.getText().toString());
                                                                        startActivity(intent2);
                                                                        overridePendingTransition(0, 0);
                                                                        finish();
                                                                        alertDialog.dismiss();
                                                                    });

                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<Boolean> call, Throwable t) {
                                                                Log.e("API", "Errore durante la modifice dell'email nelle patologie", t);
                                                            }

                                                        });

                                                    } else {
                                                        //Visualizzo un alert dialog che mi dice che le modifiche non sono state apportate
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfiloActivity.this);
                                                        LayoutInflater inflater = ProfiloActivity.this.getLayoutInflater();
                                                        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                        builder.setView(dialogView);
                                                        TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                        Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                        Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                        AlertDialog alertDialog = builder.create();
                                                        alertDialog.show();
                                                        alertDialog.setCancelable(false);

                                                        title.setText("Errore");
                                                        title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                                                        title.setCompoundDrawablePadding(10);
                                                        message.setText("Errore nella modifica delle credenziali di accesso");
                                                        positiveButton.setText("Ok");
                                                        negativeButton.setVisibility(View.GONE);
                                                        positiveButton.setOnClickListener(v1 -> {
                                                            //Ricarico la pagina
                                                            Intent intent2 = new Intent(getApplicationContext(), ProfiloActivity.class);
                                                            intent2.putExtra("email", EditEmail.getText().toString());
                                                            startActivity(intent2);
                                                            overridePendingTransition(0, 0);
                                                            finish();
                                                            alertDialog.dismiss();
                                                        });

                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<Boolean> call, Throwable t) {
                                                    Log.e("API", "Errore durante la modifica delle credenziali", t);
                                                }
                                            });

                                        }
                                    }
                }
            }
        });

        //Inizializzo il recyclerview delle patologie già presenti
        String tempemail = EditEmail.getText().toString();
        apiServicePatologie.getPatologieUtente(tempemail).enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<String> nomipatologie = response.body();
                    initRecyclerviewLeTuePatologie(nomipatologie,tempemail);
                } else {
                    Log.e("API", "Errore durante il recupero delle patologie dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                Log.e("API", "Errore durante il recupero delle patologie dell'utente", t);
            }
        });



        //Inizializzo il bottone per disconnettersi
        DisconnettiButton = findViewById(R.id.Disconnettibutton);

        //Se premo sul bottone disconnetti allora visualizzo un alert dialog che mi chiede se voglio disconnettermi
        DisconnettiButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
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
            message.setText("Sei sicuro di voler disconnetterti?");
            positiveButton.setText("Si");
            negativeButton.setText("No");
            positiveButton.setOnClickListener(v1 -> {
                //Se confermo allora torno alla pagina di login
                Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent2);
                overridePendingTransition(0, 0);
                finish();
                alertDialog.dismiss();
            });
            negativeButton.setOnClickListener(v1 -> {
                alertDialog.dismiss();
            });
        });

    }

    private void initRecyclerviewLeTuePatologie(ArrayList<String> nomePatologie,String email) {
        //Inizializzo il recyclerview

        for (String nome : nomePatologie) {
            itemsPatologieUtente.add(new Patologie(nome,email));
        }
        recyclerviewLeTuePatologie = findViewById(R.id.recyclerViewLeTuePatologie);
        recyclerviewLeTuePatologie.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        adapterPatologie = new AdapterCardLeTuePatologie(itemsPatologieUtente);
        recyclerviewLeTuePatologie.setAdapter(adapterPatologie);
    }


    private void salvaCredenziali() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", EditEmail.getText().toString());
        editor.putString("password", EditPassword.getText().toString());
        editor.apply();
    }


   }