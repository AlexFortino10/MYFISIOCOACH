package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import Adapter.CalendarAdapter;
import BackendServer.ApiServiceGiorniAllenamento;
import BackendServer.ApiServiceResocontoUtente;
import Model.Resoconto;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResocontoActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener{

    BottomNavigationView navigationmenu;
    private TextView monthYearText,minutiallenamenti,numeroallenamenti,recordpersonale,serieallenamenti;
    private RecyclerView calendarRecyclerView;
    private LocalDate selectedDate;

    Resoconto resoconto = new Resoconto();

    ApiServiceResocontoUtente apiServiceResocontoNonStringhe;

    ApiServiceGiorniAllenamento apiServiceGiorniAllenamento;



    @SuppressLint({"MissingInflatedId", "ResourceType"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resoconto);

        //Definisco il servizio per intercepire le richieste al server
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        //Configuro Retrofit per i dati non stringa del resconto

        Retrofit retrofitRescontoNonString = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceResocontoNonStringhe = retrofitRescontoNonString.create(ApiServiceResocontoUtente.class);

        //Configuro Retrofit per i giorni di allenamento
        Retrofit retrofitGiorniAllenamento = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceGiorniAllenamento = retrofitGiorniAllenamento.create(ApiServiceGiorniAllenamento.class);


        //Riavvio la schermata
        overridePendingTransition(0, 0);

        //Setto le textview per i dati del resoconto
        minutiallenamenti = findViewById(R.id.textViewNumMinutiAllenamenti);
        numeroallenamenti = findViewById(R.id.textViewNumAllenamenti);
        recordpersonale = findViewById(R.id.textViewNumRecordPersonale);
        serieallenamenti = findViewById(R.id.textViewNumSerieGiorni);


        //Setto il menu di navigazione
        navigationmenu = findViewById(R.id.bottomNavigationViewResoconto);

        //Setto l'elemento selezionato
        navigationmenu.setSelectedItemId(R.id.Resoconto);

        //Ricevo l'email dell'utente
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        //Creo il resoconto dell'utente

        apiServiceResocontoNonStringhe.findByEmail(email).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null && response.body()) {
                    System.out.println("Resoconto già creato");
                }else {

                    apiServiceResocontoNonStringhe.creaResoconto(email).enqueue(new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if (response.isSuccessful() && response.body() != null && response.body()) {
                                System.out.println("Resoconto creato");
                            } else {
                                System.out.println("Resoconto non creato");
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            System.out.println("Errore nella creazione del resoconto");
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                System.out.println("Errore nella creazione del resoconto");
            }
        });

        apiServiceResocontoNonStringhe.findMinutiByEmail(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer minuti = response.body();
                    apiServiceResocontoNonStringhe.findSecondiByEmail(email).enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Integer secondi = response.body();
                                minutiallenamenti.setText(minuti.toString() + ":"+ secondi.toString());
                            } else {
                                System.out.println("Errore nel recupero dei secondi");
                            }
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                            System.out.println("Errore nel recupero dei secondi");
                        }
                    });

                } else {
                    System.out.println("Errore nel recupero dei minuti");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("Errore nel recupero dei minuti");
            }
        });

        apiServiceResocontoNonStringhe.findNumallenamentiByEmail(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Integer numallenamenti = response.body();
                    numeroallenamenti.setText(numallenamenti.toString());
                } else {
                    System.out.println("Errore nel recupero del numero di allenamenti");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("Errore nel recupero del numero di allenamenti");
            }
        });

        apiServiceResocontoNonStringhe.findRecordpersonaleByEmail(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    recordpersonale.setText(response.body().toString());
                } else {
                    System.out.println("Errore nel recupero del record personale");
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("Errore nel recupero del record personale");
            }
        });

        apiServiceResocontoNonStringhe.findSerieByEmail(email).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final Integer[] serie = {response.body()};
                    LocalDate date = LocalDate.now();
                    Integer giornocorrente = date.getDayOfMonth();
                    Integer mesecorrente = date.getMonthValue();

                    apiServiceGiorniAllenamento.getUltimoGiornoAllenamento(email,mesecorrente).enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Integer giornoultimoallenamento = response.body();
                                System.out.println(giornocorrente);
                                System.out.println(giornoultimoallenamento);

                                if (giornoultimoallenamento + 1 < giornocorrente) {
                                    serie[0] = 0;
                                }

                                System.out.println("Il numero della serie è: "+serie[0]);

                                serieallenamenti.setText(serie[0].toString());
                            }else {
                                System.out.println("Errore nel recupero del giorno dell'ultimo allenamento");
                            }
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                            System.out.println("Errore nel recupero del giorno dell'ultimo allenamento");
                        }
                    });

                } else {
                    System.out.println("Errore nel recupero del numero di serie");

                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                System.out.println("Errore nel recupero del numero di serie");
            }
        });


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
                return true;

            } else if (R.id.Impostazioni == item.getItemId()) {
                //Invio l'email dell'utente alla ImpostazioniActivity
                Intent intent2 = new Intent(getApplicationContext(), ImpostazioniActivity.class);
                intent2.putExtra("email", email);
                startActivity(intent2);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }else if (R.id.Profilo == item.getItemId()) {
                //Invio l'email dell'utente alla ProfiloActivity
                Intent intent3 = new Intent(getApplicationContext(), ProfiloActivity.class);
                intent3.putExtra("email", email);
                startActivity(intent3);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }else if (R.id.MyFisio == item.getItemId()) {
                //Mostro un dialog all'utente che informa che il dialogo con l'avatar è possibile solo nella homepage
                AlertDialog.Builder builder = new AlertDialog.Builder(ResocontoActivity.this);
                LayoutInflater inflater = ResocontoActivity.this.getLayoutInflater();
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
                    navigationmenu.setSelectedItemId(R.id.Resoconto);
                });

                return true;
            }
            return false;
        });


        //Setto il calendario
        resoconto.setEmail(email);
        initWidgets();
        selectedDate = LocalDate.now();
        setMonthView();

}

    private void initWidgets()
    {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
    }

    private void setMonthView()
    {
        monthYearText.setText(monthYearFromDate(selectedDate));
        System.out.println(selectedDate);
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        System.out.println(daysInMonth);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, monthYearText.getText().toString(),resoconto.getEmail(),this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);
        int daysInMonth = yearMonth.lengthOfMonth();

        // Calcola il primo giorno del mese
        LocalDate firstOfMonth = date.withDayOfMonth(1);
        // Calcola il giorno della settimana (da 1 a 7, dove 1 è lunedì)
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        // Aggiungi i giorni vuoti fino al primo giorno del mese
        for (int i = 1; i < dayOfWeek; i++) {
            daysInMonthArray.add("");
        }

        // Aggiungi i giorni del mese
        for (int day = 1; day <= daysInMonth; day++) {
            daysInMonthArray.add(String.valueOf(day));
        }

        // Aggiungi i giorni vuoti fino a completare 42 (6 settimane)
        while (daysInMonthArray.size() < 42) {
            daysInMonthArray.add("");
        }

        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        return date.format(formatter);
    }

    public void previousMonthAction(View view)
    {
        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
        selectedDate = selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, String dayText)
    {
        if(!dayText.equals(""))
        {
            String message = "Selected Date " + dayText + " " + monthYearFromDate(selectedDate);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }
}