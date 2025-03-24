package com.example.myfisiocoach;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.time.LocalDate;


import BackendServer.ApiServiceUtente;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.SharedPreferences;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefsFile";

    TextView passworddimenticata,registrati;
    EditText email, password;
    Button accedi;

    Boolean passwordvisibile = false;

    ApiServiceUtente apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        password = findViewById(R.id.editTextPassword);

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

        //definisco il bottone per registrarsi
        registrati = findViewById(R.id.textViewRegistrati2);

        //quando clicco su registrati mi sposto alla pagina di registrazione
        registrati.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrazioneActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        // Verifica se l'Activity è stata avviata dalla notifica
        if (getIntent().getBooleanExtra("from_notification", false)) {
            // Aggiorna SharedPreferences
            SharedPreferences sharedPreferenceslogin = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editorlogin = sharedPreferenceslogin.edit();
            editorlogin.putInt("login_from_notification", 1);
            editorlogin.apply();
        }

        //recupero il valore dello shared login_from_notification
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        int login_from_notification = sharedPref.getInt("login_from_notification", 0);

        System.out.println("login_from_notification: " + login_from_notification);


        //Recupero lo shared preferences dei giorni per attivare il dialogo
        SharedPreferences sharedPrefGiorni = getSharedPreferences("DialogPrefs", Context.MODE_PRIVATE);
        int giorni = sharedPrefGiorni.getInt("numdays", 0);
        System.out.println("giorni controllati nel login: " + giorni);



        // Recupera SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if credentials are already saved
        checkForSavedCredentials(login_from_notification);

        // Gestione della visibilità della password
        password.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[2].getBounds().width())) {
                    if (!passwordvisibile) {
                        password.setTransformationMethod(null);
                        password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.closed_eyes__1_, 0);
                        passwordvisibile = true;
                    } else {
                        password.setTransformationMethod(new PasswordTransformationMethod());
                        password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24, 0, R.drawable.baseline_remove_red_eye_24, 0);
                        passwordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });

        accedi = findViewById(R.id.AccediButton);
        email = findViewById(R.id.editTextTextEmailAddress);
        accedi.setOnClickListener(v -> {
            if (email.getText().toString().isEmpty()) {
                email.setError("Devi inserire un'email");
                email.setText("");
                password.setText("");
            } else {
                email.setError(null);
            }

            if (password.getText().toString().isEmpty()) {
                password.setError("Devi inserire una password");
                email.setText("");
                password.setText("");
            } else {
                apiService.verificaUtente(email.getText().toString(), password.getText().toString()).enqueue(new retrofit2.Callback<Boolean>() {
                     @Override
                     public void onResponse(retrofit2.Call<Boolean> call, retrofit2.Response<Boolean> response) {
                         if (response.isSuccessful() && response.body() != null && response.body()) {

                             System.out.println("Login effettuato con successo");

                             if (credenzialiSalvate()){
                                 if (!checkNotificationPermission() || !checkAlarmPermission()){
                                     //Passo l'email all'activity AttivaNotifiche
                                     Intent intent = new Intent(LoginActivity.this, AttivaNotifiche.class);
                                     intent.putExtra("email", email.getText().toString());
                                     startActivity(intent);
                                     overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                     finish();
                                 } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)//Controllo se il microfoo è attivo, in caso contrario mostro la pagina AttivaMicrofono
                                 {
                                     //Passo l'email all'activity AttivaMicrofono
                                     Intent intent = new Intent(LoginActivity.this, AttivaMicrofonoActivity.class);
                                     intent.putExtra("email", email.getText().toString());
                                     startActivity(intent);
                                     overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                     finish();
                                 }else {
                                     //Recupero lo stage dell'utente

                                     apiService.getStageUtente(email.getText().toString()).enqueue(new retrofit2.Callback<Integer>() {
                                         @Override
                                         public void onResponse(retrofit2.Call<Integer> call, retrofit2.Response<Integer> response) {
                                             if (response.isSuccessful() && response.body() != null) {
                                                 int stage = response.body();
                                                 //Controllo ste sto accedendo da una notifica
                                                 if (login_from_notification == 1){

                                                     //Recupero lo shared preferences del giorno in cui è stato aggiornato numdays
                                                     int day = sharedPrefGiorni.getInt("day", 0);
                                                     LocalDate today = LocalDate.now();

                                                     System.out.println(" il giorno salvato è: " + day + " e oggi è: " + today.getDayOfMonth());
                                                     System.out.println("il numero di giorni è: " + giorni);
                                                     System.out.println("lo stage è: " + stage);


                                                     if (giorni==2 && stage<4 && (today.getDayOfMonth() > day || (today.getDayOfMonth() < day && today.getDayOfMonth() == 1))){
                                                         //Passo l'email all'activity HomepageActivity
                                                         Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                                                         intent.putExtra("email", email.getText().toString());
                                                         startActivity(intent);
                                                         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                         finish();
                                                     }else{
                                                         //Passo l'email all'activity AllenamentoGiornaliero
                                                         Intent intent = new Intent(LoginActivity.this, AllenamentoGiornalieroActivity.class);
                                                         intent.putExtra("email", email.getText().toString());
                                                         startActivity(intent);
                                                         overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                         finish();
                                                     }

                                                 } else {
                                                     Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                                                     intent.putExtra("email", email.getText().toString());
                                                     startActivity(intent);
                                                     overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                     finish();
                                                 }
                                             }
                                         }

                                         @Override
                                         public void onFailure(retrofit2.Call<Integer> call, Throwable t) {
                                             System.out.println("Si è verificato un errore nel recupero dello stage");
                                         }
                                     });
                                 }
                             }else{
                                 mostraDialogSalvaCredenziali(login_from_notification);

                             }
                         } else {

                             AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                             LayoutInflater inflater = LoginActivity.this.getLayoutInflater();
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
                             message.setText("Email o password errate");
                             positiveButton.setText("Ok");
                             negativeButton.setVisibility(View.GONE);
                             positiveButton.setOnClickListener(v1 -> {
                                 email.setText("");
                                 password.setText("");
                                 alertDialog.dismiss();
                             });

                         }
                     }

                     @Override
                     public void onFailure(retrofit2.Call<Boolean> call, Throwable t) {
                         System.out.println("Si è verificato un errore nel login");
                     }
                 });

            }
        });

        passworddimenticata = findViewById(R.id.textViewPasswordDimenticata);
        passworddimenticata.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, DatiDiVerificaActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });



    }

    // Metodo per mostrare il dialog di salvataggio delle credenziali
    private void mostraDialogSalvaCredenziali(int login_from_notification) {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        message.setText("Vuoi attivare il riconoscimento biometrico?");
        positiveButton.setText("Si");
        negativeButton.setText("No");
        positiveButton.setOnClickListener(v1 -> {
            salvaCredenziali(login_from_notification);
            alertDialog.dismiss();
            //Controllo se le notifiche sono attive, se non lo sono mostro passo alla AttivaNotifiche
            if (!checkNotificationPermission() || !checkAlarmPermission()){
                //Passo l'email all'activity AttivaNotifiche
                Intent intent = new Intent(LoginActivity.this, AttivaNotifiche.class);
                intent.putExtra("email", email.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)//Controllo se il microfoo è attivo, in caso contrario mostro la pagina AttivaMicrofono
            {
                //Passo l'email all'activity AttivaMicrofono
                Intent intent = new Intent(LoginActivity.this, AttivaMicrofonoActivity.class);
                intent.putExtra("email", email.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            } else {
                //Passo l'email all'activity HomepageActivity
                Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                intent.putExtra("email", email.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

            }

        });
        negativeButton.setOnClickListener(v1 -> {
            alertDialog.dismiss();
            if (!checkNotificationPermission() || !checkAlarmPermission()){
                //Passo l'email all'activity AttivaNotifiche
                Intent intent = new Intent(LoginActivity.this, AttivaNotifiche.class);
                intent.putExtra("email", email.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)//Controllo se il microfoo è attivo, in caso contrario mostro la pagina AttivaMicrofono
            {
                //Passo l'email all'activity AttivaMicrofono
                Intent intent = new Intent(LoginActivity.this, AttivaMicrofonoActivity.class);
                intent.putExtra("email", email.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }else {
                //Passo l'email all'activity HomepageActivity
                Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                intent.putExtra("email", email.getText().toString());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

            }
        });
    }

    private void salvaCredenziali(int login_from_notification) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email.getText().toString());
        editor.putString("password", password.getText().toString());
        editor.apply();
        usaAutenticazioneBiometrica(login_from_notification);
    }

    // Metodo per verificare se le credenziali sono già salvate
    private boolean credenzialiSalvate() {
        String savedEmail = sharedPreferences.getString("email", null);
        String savedPassword = sharedPreferences.getString("password", null);
        return savedEmail != null && savedPassword != null;
    }


    // Metodo per verificare se le credenziali sono già salvate
    private void checkForSavedCredentials(int login_from_notification) {
        if (credenzialiSalvate()) {
            usaAutenticazioneBiometrica(login_from_notification);
        }
    }

    // Metodo per autenticazione biometrica
    private void usaAutenticazioneBiometrica(int login_from_notification) {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
            BiometricPrompt biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                            if (!checkNotificationPermission() || !checkAlarmPermission()){
                                //Passo l'email all'activity AttivaNotifiche
                                Intent intent = new Intent(LoginActivity.this, AttivaNotifiche.class);
                                intent.putExtra("email", sharedPreferences.getString("email", null));
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)//Controllo se il microfoo è attivo, in caso contrario mostro la pagina AttivaMicrofono
                            {
                                //Passo l'email all'activity AttivaMicrofono
                                Intent intent = new Intent(LoginActivity.this, AttivaMicrofonoActivity.class);
                                intent.putExtra("email", sharedPreferences.getString("email", null));
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            }else {
                                if (login_from_notification == 1){

                                    System.out.println("Sono nell'if fi login_from_notification == 1");

                                    apiService.getStageUtente(sharedPreferences.getString("email", null)).enqueue(new retrofit2.Callback<Integer>() {
                                        @Override
                                        public void onResponse(retrofit2.Call<Integer> call, retrofit2.Response<Integer> response) {
                                            if (response.isSuccessful() && response.body() != null) {

                                                int stage = response.body();
                                                //Recupero lo shared preferences dei giorni per attivare il dialogo e il giorno in cui è stato aggiornato numdays
                                                SharedPreferences sharedPrefGiorni = getSharedPreferences("DialogPrefs", Context.MODE_PRIVATE);
                                                int giorni = sharedPrefGiorni.getInt("numdays", 0);
                                                int day = sharedPrefGiorni.getInt("day", 0);
                                                LocalDate today = LocalDate.now();

                                                if (giorni==2 && stage<4 && (today.getDayOfMonth() > day || (today.getDayOfMonth() < day && today.getDayOfMonth() == 1))){
                                                    //Passo l'email all'activity HomepageActivity
                                                    Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                                                    intent.putExtra("email", sharedPreferences.getString("email", null));
                                                    startActivity(intent);
                                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                    finish();
                                                }else{
                                                    //Passo l'email all'activity AllenamentoGiornaliero
                                                    Intent intent = new Intent(LoginActivity.this, AllenamentoGiornalieroActivity.class);
                                                    intent.putExtra("email", sharedPreferences.getString("email", null));
                                                    startActivity(intent);
                                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                    finish();
                                                }
                                            }
                                        }


                                        @Override
                                        public void onFailure(retrofit2.Call<Integer> call, Throwable t) {
                                            System.out.println("Si è verificato un errore nel recupero dello stage");
                                        }
                                    });
                                }else{

                                    System.out.println("Sono nell'else di login_from_notification == 1");

                                    //Passo l'email all'activity HomepageActivity
                                    Intent intent = new Intent(LoginActivity.this, HomepageActivity.class);
                                    intent.putExtra("email", sharedPreferences.getString("email", null));
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();
                                }

                            }
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            // Gestisci fallimento autenticazione
                        }
                    });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Autenticazione biometrica")
                    .setSubtitle("Usa l'impronta per accedere")
                    .setNegativeButtonText("Annulla")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
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

