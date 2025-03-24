package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;

import BackendServer.ApiServiceUtente;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistrazioneActivity extends AppCompatActivity {

    TextView accedi;
    Button avanza;

    EditText username,password,confermapassword;

    Boolean passwordvisibile = false, confermapasswordvisibile = false;

    ApiServiceUtente apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrazione);

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
        accedi = findViewById(R.id.textViewAccedi);
        accedi.setOnClickListener(v -> {
            startActivity(new Intent(RegistrazioneActivity.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        //mi sposto alla pagina per la scelta del genere con una transizione di schermata quando clicco su avanti
        avanza = findViewById(R.id.AvanzaButton);
        username = findViewById(R.id.editTextTextEmailAddress);

        //Quando premo sul drawableEnd della textfield della password mi mostra la password
        //Se premo di nuovo la password non sarà più visibile
        password = findViewById(R.id.editTextPassword);

        //Se clicco sulla textflied mi esce un info per vedere quali caratteri deve contenere la password
        password.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                AlertDialog.Builder builder = new AlertDialog.Builder(RegistrazioneActivity.this);
                LayoutInflater inflater = RegistrazioneActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                builder.setView(dialogView);
                TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.setCancelable(false);

                title.setText("Requisiti password");
                title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                title.setCompoundDrawablePadding(10);
                message.setText("La password deve contenere almeno 7 caratteri\n" + "Almeno un carattere maiuscolo e uno minuscolo\n" + "Almeno un numero e un carattere speciale: !@#$%^&*_");
                positiveButton.setText("Ok");
                negativeButton.setVisibility(View.GONE);
                positiveButton.setOnClickListener(v1 -> {
                    alertDialog.dismiss();
                });
            }
        });


        password.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP){
                if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[2].getBounds().width())){
                    if(!passwordvisibile){
                        password.setTransformationMethod(null);
                        password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.closed_eyes__1_,0);
                        passwordvisibile = true;
                    }else{
                        password.setTransformationMethod(new PasswordTransformationMethod());
                        password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.baseline_remove_red_eye_24,0);
                        passwordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });

        //Quando premo sul drawableEnd della textfield della confermapassword mi mostra la password
        //Se premo di nuovo la password non sarà più visibile
        confermapassword = findViewById(R.id.editTextConfermaPassword);
        confermapassword.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP){
                if(event.getRawX() >= (confermapassword.getRight() - confermapassword.getCompoundDrawables()[2].getBounds().width())){
                    if(!confermapasswordvisibile){
                        confermapassword.setTransformationMethod(null);
                        confermapassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.closed_eyes__1_,0);
                        confermapasswordvisibile = true;
                    }else{
                        confermapassword.setTransformationMethod(new PasswordTransformationMethod());
                        confermapassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_lock_24,0,R.drawable.baseline_remove_red_eye_24,0);
                        confermapasswordvisibile = false;
                    }
                    return true;
                }
            }
            return false;
        });


        avanza.setOnClickListener(v -> {

            //Controllo che l'email sia valida
            if(username.getText().toString().isEmpty()){
                username.setError("Devi inserire uno username");
                username.setText("");
                password.setText("");
                confermapassword.setText("");
            } else {
                //Controllo che nel server non ci sia già lo stesso username
                apiService.verificaEmail(username.getText().toString()).enqueue(new retrofit2.Callback<Boolean>() {
                    @Override
                    public void onResponse(retrofit2.Call<Boolean> call, retrofit2.Response<Boolean> response) {
                        if(response.isSuccessful() && response.body()!=null && response.body()){

                            AlertDialog.Builder builder = new AlertDialog.Builder(RegistrazioneActivity.this);
                            LayoutInflater inflater = RegistrazioneActivity.this.getLayoutInflater();
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
                            message.setText("Username già presente nel database, inserisci un altro username");
                            positiveButton.setText("Ok");
                            negativeButton.setVisibility(View.GONE);
                            positiveButton.setOnClickListener(v1 -> {
                                username.setText("");
                                password.setText("");
                                confermapassword.setText("");
                                alertDialog.dismiss();
                            });
                        } else {
                            username.setError(null);
                            // controllo che la password sia stata inserita rispettando i requisiti come lunghezza minima di 10 caratteri
                            //Almeno un carattere maiuscolo e uno maiuscolo
                            //Almeno un numero e almeno un carattere speciale
                            if (password.getText().toString().isEmpty()) {
                                password.setError("Devi inserire una password");
                                password.setText("");
                                confermapassword.setText("");
                            } else if (password.getText().toString().length() < 7) {
                                password.setError("La password deve contenere almeno 7 caratteri\n" + "Almeno un carattere maiuscolo e uno minuscolo\n" + "Almeno un numero e un carattere speciale");
                                password.setText("");
                                confermapassword.setText("");
                            } else if (!password.getText().toString().matches(".*[A-Z].*") || !password.getText().toString().matches(".*[a-z].*") || !password.getText().toString().matches(".*[0-9].*") || !password.getText().toString().matches(".*[!@#$%^&*_].*")) {
                                password.setError("La password deve contenere almeno 7 caratteri\n" + "Almeno un carattere maiuscolo e uno minuscolo\n" + "Almeno un numero e un carattere speciale: !@#$%^&*_");
                                password.setText("");
                                confermapassword.setText("");
                            } else {
                                password.setError(null);
                                //Controllo che la conferma della password sia uguale alla password
                                if (!password.getText().toString().matches(confermapassword.getText().toString())) {
                                    confermapassword.setError("Le due password non coincidono");
                                    password.setText("");
                                    confermapassword.setText("");

                                } else {
                                    //Se tutto è corretto mi sposto alla pagina per la scelta del genere passando i dati acquisiti in questo caso l'email e la password
                                    Intent intent = new Intent(RegistrazioneActivity.this, SceltaGenereActivity.class);
                                    intent.putExtra("email", username.getText().toString());
                                    intent.putExtra("password", password.getText().toString());
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                    finish();
                                }
                            }

                        }

                    }

                    @Override
                    public void onFailure(retrofit2.Call<Boolean> call, Throwable t) {

                    }
                });
            }
        });
    }
}