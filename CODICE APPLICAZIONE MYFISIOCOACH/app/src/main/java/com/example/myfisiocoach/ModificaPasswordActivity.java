package com.example.myfisiocoach;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModificaPasswordActivity extends AppCompatActivity {
    ImageView tornaindietro;
    Button conferma;

    EditText nuovapassword, confermaNupassword;

    String email;
    Boolean nuovapasswordvisibile = false, confermaNupasswordvisibile = false;

    ApiServiceUtente apiService;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifica_password);

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

        //Prendo l'email dall'intent
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        //Quando premo sul drawableEnd della textfield della nuovapassword mi mostra la password
        //Se premo di nuovo la password non sarà più visibile
        nuovapassword = findViewById(R.id.editTextTextNuovaPassword);
        nuovapassword.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP){
                if(event.getRawX() >= (nuovapassword.getRight() - nuovapassword.getCompoundDrawables()[2].getBounds().width())){
                    if(!nuovapasswordvisibile){
                        nuovapassword.setTransformationMethod(null);
                        nuovapassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.closed_eyes__1_,0);
                        nuovapasswordvisibile = true;
                    }else{
                        nuovapassword.setTransformationMethod(new PasswordTransformationMethod());
                        nuovapassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.baseline_remove_red_eye_24,0);
                        nuovapasswordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });





        //Quando premo sul drawableEnd della textfield della confermaNupassword mi mostra la password
        //Se premo di nuovo la password non sarà più visibile
        confermaNupassword = findViewById(R.id.editTextTextConfermaNu);
        confermaNupassword.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP){
                if(event.getRawX() >= (confermaNupassword.getRight() - confermaNupassword.getCompoundDrawables()[2].getBounds().width())){
                    if(!confermaNupasswordvisibile){
                        confermaNupassword.setTransformationMethod(null);
                        confermaNupassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.closed_eyes__1_,0);
                        confermaNupasswordvisibile = true;
                    }else{
                        confermaNupassword.setTransformationMethod(new PasswordTransformationMethod());
                        confermaNupassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.baseline_remove_red_eye_24,0);
                        confermaNupasswordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });


        //Quando premo sulla freccia indietro mi riporta alla DatiDiVerificaActivity con una transizione di schermata
        tornaindietro = findViewById(R.id.TornaIndietroButton);
        tornaindietro.setOnClickListener(v -> {
            startActivity(new Intent(ModificaPasswordActivity.this, DatiDiVerificaActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        //Quando premo sul bottone conferma mi porta alla LoginActivity con una transizione di schermata
        conferma = findViewById(R.id.ConfermaButton);
        conferma.setOnClickListener(v -> {

            //Verifico che le password siano inserite correttamente e che siano uguali
            if (nuovapassword.getText().toString().isEmpty()) {
                nuovapassword.setError("Devi inserire una password.");
                confermaNupassword.setText("");
            } else if (nuovapassword.getText().toString().length() < 10) {
                nuovapassword.setError("La password deve contenere almeno 10 caratteri\n" + "Almeno un carattere maiuscolo e uno minuscolo\n" + "Almeno un numero e un carattere speciale");
                nuovapassword.setText("");
                confermaNupassword.setText("");
            } else if (!nuovapassword.getText().toString().matches(".*[A-Z].*") || !nuovapassword.getText().toString().matches(".*[a-z].*") || !nuovapassword.getText().toString().matches(".*[0-9].*") || !nuovapassword.getText().toString().matches(".*[!@#$%^&*_].*")) {
                nuovapassword.setError("La password deve contenere almeno 10 caratteri\n" + "Almeno un carattere maiuscolo e uno minuscolo\n" + "Almeno un numero e un carattere speciale: !@#$%^&*_");
                nuovapassword.setText("");
                confermaNupassword.setText("");
            } else {
                nuovapassword.setError(null);
                //Controllo che la conferma della password sia uguale alla password
                if (!nuovapassword.getText().toString().matches(confermaNupassword.getText().toString())) {
                    confermaNupassword.setError("Le due password non coincidono.");
                    nuovapassword.setText("");
                    confermaNupassword.setText("");

                } else {
                    apiService.verificaUtente(email, nuovapassword.getText().toString()).enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if (response.isSuccessful() && response.body() != null && response.body()) {
                                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ModificaPasswordActivity.this);
                                LayoutInflater inflater = ModificaPasswordActivity.this.getLayoutInflater();
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
                                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_error_32, 0);
                                title.setCompoundDrawablePadding(10);
                                message.setText("La password inserita è simile a quella attuale, inserisci una password diversa.");
                                positiveButton.setText("Ok");
                                negativeButton.setVisibility(View.GONE);
                                positiveButton.setOnClickListener(v1 -> {
                                    nuovapassword.setText("");
                                    confermaNupassword.setText("");
                                    alertDialog.dismiss();
                                });
                            } else {
                                //Andamo effetivamente a modificare la password
                                apiService.modificaPassword(email, nuovapassword.getText().toString()).enqueue(new Callback<Boolean>() {
                                    @Override
                                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                        if (response.isSuccessful() && response.body() != null && response.body()) {
                                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ModificaPasswordActivity.this);
                                            LayoutInflater inflater = ModificaPasswordActivity.this.getLayoutInflater();
                                            View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                            builder.setView(dialogView);
                                            TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                            @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                            Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                            Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                            android.app.AlertDialog alertDialog = builder.create();
                                            alertDialog.show();
                                            alertDialog.setCancelable(false);

                                            title.setText("Password modificata");
                                            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_information_outline_32, 0);
                                            title.setCompoundDrawablePadding(10);
                                            message.setText("La password è stata modificata correttamente,Verrai reindirizzato alla pagina di login");
                                            positiveButton.setText("Ok");
                                            negativeButton.setVisibility(View.GONE);
                                            positiveButton.setOnClickListener(v1 -> {
                                                alertDialog.dismiss();
                                                startActivity(new Intent(ModificaPasswordActivity.this, LoginActivity.class));
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                finish();
                                            });
                                        } else {
                                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ModificaPasswordActivity.this);
                                            LayoutInflater inflater = ModificaPasswordActivity.this.getLayoutInflater();
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
                                            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_error_32, 0);
                                            title.setCompoundDrawablePadding(10);
                                            message.setText("Errore nella modifica della password,RIPROVA!");
                                            positiveButton.setText("Ok");
                                            negativeButton.setVisibility(View.GONE);
                                            positiveButton.setOnClickListener(v1 -> {
                                                nuovapassword.setText("");
                                                confermaNupassword.setText("");
                                                alertDialog.dismiss();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Boolean> call, Throwable t) {
                                        System.out.println("Si è verificato un errore nella modifica della password");

                                    }

                                });

                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            System.out.println("Si è verificato un errore nel confromto delle password");
                        }
                    });
                }
            }
        });
    }

}