package com.example.myfisiocoach;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.pm.PackageManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.widget.VideoView;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import Adapter.AdapterCardAllenamentiPatologie;
import BackendServer.ApiServiceAllenamentoGiornaliero;
import BackendServer.ApiServicePatologie;
import BackendServer.ApiServiceUtente;
import Llama.LlamaService;
import Model.Patologie;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class HomepageActivity extends AppCompatActivity {

    VideoView animazioni;

    BottomNavigationView navigationmenu;

    ImageView ImmagineAllenamentoGiornaliero;

    TextView Nomeutente, DataOdierna, AllenamentiPatologie, ConsigliatoPerTe;

    String nome, email;

    LocalDate data = LocalDate.now();

    CardView AllenamentoGiornaliero,CardViewAvatar;

    RecyclerView recyclerviewAllenamentiPatologie;

    private static final int SPEECH_REQUEST_CODE = 100;

    private LlamaService llamaService = new LlamaService();// Il servizio che invia il prompt a LLaMA
    private TextToSpeech textToSpeech ,texttoSpeechResponse;;

    int hourOfDay = 8; // Orario di default per la notifica giornaliera
    int minuteOfHour = 0; // Minuti di default per la notifica giornaliera

    ApiServicePatologie apiServicePatologie,apiServiceNomePatologie;

    ApiServiceUtente apiServiceUtente,apiServiceStageUtente;

    ApiServiceAllenamentoGiornaliero apiServiceAllenametoGiornaliero;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

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
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        apiServicePatologie = retrofitPatologie.create(ApiServicePatologie.class);

        //Setto la cardview dell'avatar del robot
        CardViewAvatar = findViewById(R.id.CardViewAvatarHomepage);

        //Setto il video di benvenuto
        animazioni = findViewById(R.id.animazioni);

        //Nascondo l'avatar del robot
        CardViewAvatar.setVisibility(View.GONE);
        animazioni.setVisibility(View.GONE);

        //Setto l'immagine dell'allenamento giornaliero
        ImmagineAllenamentoGiornaliero = findViewById(R.id.ImmagineAllenamentoGiornaliero);

        //Setto il recyclerview delle patologie
        recyclerviewAllenamentiPatologie = findViewById(R.id.recyclerViewAllenamentiPatologie);

        Retrofit retrofitNomePatologie = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceNomePatologie = retrofitNomePatologie.create(ApiServicePatologie.class);


        // Configura Retrofit per l'utente

        Retrofit retrofitUtente = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        apiServiceUtente = retrofitUtente.create(ApiServiceUtente.class);

        Retrofit retrofitStageUtente = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceStageUtente = retrofitStageUtente.create(ApiServiceUtente.class);


        //Configura il Retrofit per l'allenamento giornaliero

        Retrofit retrofitAllenamentoGiornaliero = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceAllenametoGiornaliero = retrofitAllenamentoGiornaliero.create(ApiServiceAllenamentoGiornaliero.class);

        //Inizializzo il text to speach
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Imposta la lingua
                int result = textToSpeech.setLanguage(Locale.ITALIAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Lingua non supportata");
                } else {
                    // Forza l'impostazione della voce maschile
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Voice maleVoice = null;

                        // Cerca una voce maschile italiana
                        for (Voice voice : textToSpeech.getVoices()) {
                            if (voice.getLocale().equals(Locale.ITALIAN) && voice.getName().toLowerCase().contains("male")) {
                                maleVoice = voice;
                                break;
                            }
                        }

                        if (maleVoice != null) {
                            textToSpeech.setVoice(maleVoice);
                            Log.i("TTS", "Voce maschile forzata: " + maleVoice.getName());
                        } else {
                            Log.w("TTS", "Nessuna voce maschile italiana trovata. Utilizzo voce predefinita.");
                        }
                    } else {
                        Log.e("TTS", "L'impostazione delle voci richiede Android 5.0 o superiore");
                    }
                }
            } else {
                Log.e("TTS", "Inizializzazione TextToSpeech fallita");
            }
        });


        //Inizializzo il text to speach per la risposta
        texttoSpeechResponse = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Imposta la lingua
                int result = textToSpeech.setLanguage(Locale.ITALIAN);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Lingua non supportata");
                } else {
                    // Forza l'impostazione della voce maschile
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Voice maleVoice = null;

                        // Cerca una voce maschile italiana
                        for (Voice voice : textToSpeech.getVoices()) {
                            if (voice.getLocale().equals(Locale.ITALIAN) && voice.getName().toLowerCase().contains("male")) {
                                maleVoice = voice;
                                break;
                            }
                        }

                        if (maleVoice != null) {
                            textToSpeech.setVoice(maleVoice);
                            Log.i("TTS", "Voce maschile forzata: " + maleVoice.getName());
                        } else {
                            Log.w("TTS", "Nessuna voce maschile italiana trovata. Utilizzo voce predefinita.");
                        }
                    } else {
                        Log.e("TTS", "L'impostazione delle voci richiede Android 5.0 o superiore");
                    }
                }
            } else {
                Log.e("TTS", "Inizializzazione TextToSpeech fallita");
            }
        });

        //Creo uno shared preference per tener conto se è la prima volta che l'utente accede all'app e il giorno dell accesso
        SharedPreferences sharedPrefFirstAccess = getSharedPreferences("FirstAccessPrefs", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editorFirstAccess = sharedPrefFirstAccess.edit();
        boolean firstAccess = sharedPrefFirstAccess.getBoolean("firstAccess", true);
        if (firstAccess) {
            editorFirstAccess.putBoolean("firstAccess", false);
            editorFirstAccess.putInt("dayfirstaccess", data.getDayOfMonth());
            editorFirstAccess.apply();
        }



        //Recupero lo sharedPreferences per salvare il giorno corrente e quello per il numero di giorni per attivare il dialogo
        SharedPreferences sharedDialog = getSharedPreferences("DialogPrefs", Context.MODE_PRIVATE);
        int sharedDay = sharedDialog.getInt("day", 0);
        final int[] sharedNumDays = {sharedDialog.getInt("numdays", 0)};

        System.out.println("Giorno condiviso: " + sharedDay + " Numero di giorni condiviso: " + sharedNumDays[0]);

        if (( sharedDay < data.getDayOfMonth() || (sharedDay > data.getDayOfMonth() && data.getDayOfMonth() == 1) ) && sharedNumDays[0] < 4) {
            //Salvo il giorno corrente e incremento il numero di giorni
            SharedPreferences.Editor editorDialog = sharedDialog.edit();
            editorDialog.putInt("day", data.getDayOfMonth());
            editorDialog.putInt("numdays", sharedNumDays[0] + 1);
            editorDialog.apply();
        }

        //Resetto il contatore delle registrazioni vocali
        SharedPreferences sharedPreferences = getSharedPreferences("RecVocal", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("numregistrazionivocali", 0);
        editor.apply();

        // Setto il menu di navigazione
        navigationmenu = findViewById(R.id.bottomNavigationViewHomePage);

        // Setto l'elemento selezionato
        navigationmenu.setSelectedItemId(R.id.Home);

        // Setto il listener per il menu di navigazione spostandomi sulle rispettive activity con una transizione di schermata
        navigationmenu.setOnNavigationItemSelectedListener(item -> {
            if (R.id.Home == item.getItemId()) {
                return true;
            } else if (R.id.Resoconto == item.getItemId()) {
                Intent intent = new Intent(getApplicationContext(), ResocontoActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

            } else if (R.id.Impostazioni == item.getItemId()) {
                // Invio l'email dell'utente alla ImpostazioniActivity
                Intent intent = new Intent(getApplicationContext(), ImpostazioniActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (R.id.Profilo == item.getItemId()) {
                // Invio l'email dell'utente alla ProfiloActivity
                Intent intent = new Intent(getApplicationContext(), ProfiloActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;

            }else if (R.id.MyFisio == item.getItemId()) {
                //recupero il giorno del primo accesso dell'utente e il giorno corrente
                int dayfirstaccess = sharedPrefFirstAccess.getInt("dayfirstaccess", 0);
                int day = data.getDayOfMonth();

                if (dayfirstaccess == day) {
                    StartDialogPrimoAccesso();
                } else {
                    StartDialog();
                    }
                return true;
            }
            return false;
        });

        // Setto il nome dell'utente
        Nomeutente = findViewById(R.id.textViewNomeUtente);

        //Setto le altre textView
        AllenamentiPatologie = findViewById(R.id.textView29);
        ConsigliatoPerTe = findViewById(R.id.textView16);

        // Recupero l'email passata dall'activity precedente
        Intent intent = getIntent();
        email = intent.getStringExtra("email");

        // Recupero il nome dell'utente dal server
        apiServiceUtente.getNomeUtente(email).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    nome = response.body();
                    System.out.println("Nome utente: " + nome);
                    Nomeutente.setText(nome);
                } else {
                    Log.e("API", "Errore durante il recupero del nome dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("API", "Errore durante il recupero del nome dell'utente", t);
            }
        });

        // Setto la data odierna nella forma gg/mm/aaaa
        DataOdierna = findViewById(R.id.textViewDataOdierna);
        DataOdierna.setText(data.getDayOfMonth() + "/" + data.getMonthValue() + "/" + data.getYear());

        // Setto il listener per il cardview dell'allenamento giornaliero
        AllenamentoGiornaliero = findViewById(R.id.cardViewAllenamentoGiornaliero);
        // Passo alla AllenamentoGiornalieroActivity con una transizione di schermata
        AllenamentoGiornaliero.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), AllenamentoGiornalieroActivity.class);
            intent1.putExtra("email", email);
            startActivity(intent1);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        // Inizializzo il recyclerview delle patologie già presenti
        apiServiceNomePatologie.getPatologieUtente(email).enqueue(new Callback<ArrayList<String>>() {
            @Override
            public void onResponse(Call<ArrayList<String>> call, Response<ArrayList<String>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    ArrayList<String> nomepatologiepresenti = response.body();
                    initRecyclerviewAllenamentiPatologie(nomepatologiepresenti, email);

                } else {
                    Log.e("API", "Errore durante il recupero delle patologie dell'utente", new Exception(response.message()));
                }
            }

            @Override
            public void onFailure(Call<ArrayList<String>> call, Throwable t) {
                Log.e("API", "Errore durante il recupero delle patologie dell'utente", t);
            }
        });


        //creo lo shared per memorizzare l'email del ultimo utente loggato
        SharedPreferences sharedPrefEmail = getSharedPreferences("EmailPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorEmail = sharedPrefEmail.edit();
        editorEmail.putString("email", email);
        editorEmail.apply();

        //Recupera il giorno corrente
        int giorno = data.getDayOfMonth();

        //Recupero dallo sharedPreferences degli orari degli allenamenti giornalieri, l'orario e i minuti del primo allenamento giornaliero
        //Creo uno shared preference per salvare le ore degli allenamenti
        SharedPreferences sharedPref = getSharedPreferences("Allenamenti", Context.MODE_PRIVATE);
        //Recuper ore e minuti degli allenamenti dallo shared preference
        hourOfDay = sharedPref.getInt("OraPrimoAllenamento", 9);
        minuteOfHour = sharedPref.getInt("MinutiPrimoAllenamento", 0);
        int notificheattivate = sharedPref.getInt("NotificheAttivate", 0);


        // Verifica se notifiche e sveglie sono attive e nel caso pianifica la notifica giornaliera
        if (checkNotificationPermission() && checkAlarmPermission()) {
            //Controllo se l'orario è già passato, se non è passato allora pianifica la notifica
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            if ((hourOfDay > currentHour || (hourOfDay == currentHour && minuteOfHour > currentMinute)) && notificheattivate == 0) {
                System.out.println("Pianifico la notifica");
                scheduleDailyNotification(HomepageActivity.this, hourOfDay, minuteOfHour);
                // Salvo che le notifiche sono state attivate
                SharedPreferences.Editor editor1 = sharedPref.edit();
                editor1.putInt("NotificheAttivate", 1);
                editor1.apply();
            } else {
                if (notificheattivate == 1){
                    System.out.println("Notifiche già attivate");
                } else {
                    System.out.println("Orario dell'allenamento giornaliero già passato");
                    // Se l'orario è già passato, pianifica la notifica per il giorno successivo
                    // Aggiungi un giorno al calendario
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minuteOfHour);
                    calendar.set(Calendar.SECOND, 0);
                    scheduleDailyNotification(HomepageActivity.this, hourOfDay, minuteOfHour);

                    // Salvo che le notifiche sono state attivate
                    SharedPreferences.Editor editor1 = sharedPref.edit();
                    editor1.putInt("NotificheAttivate", 1);
                    editor1.apply();
                }
            }
        }

        System.out.println("Ora dell'allenamento giornaliero: " + hourOfDay + " Minuti dell'allenamento giornaliero: " + minuteOfHour);


        //Recupero lo stage dell'utente
        //Recupero la data dell'ultimo accesso dell'utente e il giorno corrente e se ha effettuato il primo dialogo o meno
        int dayfirstaccess = sharedPrefFirstAccess.getInt("dayfirstaccess", 0);
        boolean firstdialog = sharedPrefFirstAccess.getBoolean("firstdialog", false);
        int day = data.getDayOfMonth();

        if (dayfirstaccess == day){
            System.out.println("Primo accesso");
            if (!firstdialog)
            {
                // Aggiungo un OnGlobalLayoutListener per chiamare StartDialog() solo dopo che l'interfaccia è stata caricata
                Nomeutente.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Nomeutente.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        // Ritardo di 1secondi prima di avviare StartDialog()
                        int delayMillis = 1000;
                        new android.os.Handler().postDelayed(HomepageActivity.this::StartDialogPrimoAccesso, delayMillis);
                    }
                });

                //Salvo che il primo dialogo è stato effettuato
                editorFirstAccess.putBoolean("firstdialog", true);
                editorFirstAccess.apply();
            }
        }else {

            apiServiceStageUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        int stage = response.body();
                        System.out.println("Stage utente: " + stage);

                        //Salvo lo stage in uno shared prefernces
                        SharedPreferences sharedStage = getSharedPreferences("StagePrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editorStage = sharedStage.edit();
                        editorStage.putInt("stage", stage);
                        editorStage.apply();


                        //Recupero il numero di giorni per attivare il dialogo
                        sharedNumDays[0] = sharedDialog.getInt("numdays", 0);

                        if (sharedNumDays[0] > 1) {

                            // Aggiungo un OnGlobalLayoutListener per chiamare StartDialog() solo dopo che l'interfaccia è stata caricata
                            Nomeutente.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    Nomeutente.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                                    // Ritardo di 1secondi prima di avviare StartDialog()
                                    int delayMillis = 1000;
                                    new android.os.Handler().postDelayed(HomepageActivity.this::StartDialog, delayMillis);
                                }
                            });

                        }

                    } else {
                        Log.e("API", "Errore durante il recupero dello stage dell'utente", new Exception(response.message()));
                    }
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    Log.e("API", "Errore durante il recupero dello stage dell'utente", t);
                }
            });
        }

    }

    private void initRecyclerviewAllenamentiPatologie(ArrayList<String> nomePatologie, String email) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

                ArrayList<String> nomi = nomePatologie;
                ArrayList<Patologie> itemsAllenamentiPatologie = new ArrayList<>();
                SharedPreferences sharedPreferences = getSharedPreferences("ImmaginiPatologieCache", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                CountDownLatch latch = new CountDownLatch(nomi.size()); // Aspetta tutte le richieste

                for (String nome : nomi) {
                    itemsAllenamentiPatologie.add(new Patologie(nome, email));
                    if (!sharedPreferences.contains(nome)) {
                        apiServicePatologie.getImmaginePatologia(nome).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    String url = response.body();
                                    editor.putString(nome, url);
                                    editor.apply();
                                }
                                latch.countDown(); // Decrementa il conteggio
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.e("API", "Errore durante il recupero dell'immagine della patologia: " + nome, t);
                                latch.countDown(); // Anche in caso di errore decrementa
                            }
                        });
                    } else {
                        latch.countDown(); // Se già in cache, decrementa subito
                    }
                }

            try {
                latch.await(); // Aspetta fino a che tutte le richieste siano completate
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            editor.apply();

                // Esegui l'aggiornamento della UI solo dopo aver caricato tutte le immagini
                runOnUiThread(() -> {
                    recyclerviewAllenamentiPatologie.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                    recyclerviewAllenamentiPatologie.setHasFixedSize(true);

                    AdapterCardAllenamentiPatologie adapter = new AdapterCardAllenamentiPatologie(itemsAllenamentiPatologie);
                    recyclerviewAllenamentiPatologie.setAdapter(adapter);
                });
        });
    }



    private void StartDialog() {
        // Resetto lo SharedPreferences per il giorno corrente e il numero di giorni per attivare il dialogo
        SharedPreferences sharedDialog = getSharedPreferences("DialogPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorDialog = sharedDialog.edit();
        editorDialog.putInt("day", data.getDayOfMonth());
        editorDialog.putInt("numdays", 1);
        editorDialog.apply();

        SharedPreferences sharedDialogLLM = getSharedPreferences("DialogLLM", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorDialogLLM = sharedDialogLLM.edit();
        editorDialogLLM.putString("previousquestion", "");
        editorDialogLLM.putString("previousanswer", "");
        editorDialogLLM.apply();

        // Recupera il numero di registrazioni vocali precedenti
        SharedPreferences sharedPreferences = getSharedPreferences("RecVocal", Context.MODE_PRIVATE);
        int numregistrazionivocali = sharedPreferences.getInt("numregistrazionivocali", 0);
        int numrispostegiuste = sharedPreferences.getInt("numrispostegiuste", 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("numregistrazionivocali", 0);
        editor.putInt("numrispostegiuste", 0);
        editor.apply();

        // Imposto la frase "Ciao, come stai???" da riprodurre
        String textToSpeak = "Ciao, " + Nomeutente.getText().toString() + " è sempre un piacere vederti, Allora??!!! come stai oggi????!!";


        //Disattivo gli elementi della Homepage
        DataOdierna.setVisibility(View.GONE);
        AllenamentoGiornaliero.setVisibility(View.GONE);
        ImmagineAllenamentoGiornaliero.setVisibility(View.GONE);
        recyclerviewAllenamentiPatologie.setVisibility(View.GONE);
        AllenamentiPatologie.setVisibility(View.GONE);
        ConsigliatoPerTe.setVisibility(View.GONE);

        //Setto l'animazione di entrata
        Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        //Mostro l'avatar del robot
        CardViewAvatar.setVisibility(View.VISIBLE);
        animazioni.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videosaluto);
        animazioni.setVideoURI(uri);
        animazioni.seekTo(1);
        animazioni.startAnimation(slideInBottom);
        //Quando l'animazione è finita, avvio la riproduzione del testo e del video
        slideInBottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // La riproduzione è iniziata
                Log.i("TTS", "Animazione iniziata");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // La riproduzione è finita
                Log.i("TTS", "Animazione completata");

                // Usa TextToSpeech per pronunciare la frase
                if (textToSpeech != null) {
                    String utteranceId = "StartDialogUtterance"; // ID univoco per questa frase
                    animazioni.start();
                    textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                    // Attendi il completamento della frase
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            // La riproduzione è iniziata
                            Log.i("TTS", "Riproduzione iniziata: " + utteranceId);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if ("StartDialogUtterance".equals(utteranceId)) {
                                Log.i("TTS", "Riproduzione completata: " + utteranceId);
                                runOnUiThread(HomepageActivity.this::startSpeechRecognition);
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.e("TTS", "Errore durante la riproduzione del testo: " + utteranceId);
                        }
                    });
                } else {
                    Log.e("TTS", "TextToSpeech non inizializzato.");
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // La riproduzione è ripetuta
                Log.i("TTS", "Animazione ripetuta");
            }
        });
    }

    private void StartDialogPrimoAccesso() {
        // Resetto lo SharedPreferences per il giorno corrente e il numero di giorni per attivare il dialogo
        SharedPreferences sharedDialog = getSharedPreferences("DialogPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorDialog = sharedDialog.edit();
        editorDialog.putInt("day", data.getDayOfMonth());
        editorDialog.putInt("numdays", 1);
        editorDialog.apply();

        SharedPreferences sharedDialogLLM = getSharedPreferences("DialogLLM", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorDialogLLM = sharedDialogLLM.edit();
        editorDialogLLM.putString("previousquestion", "");
        editorDialogLLM.putString("previousanswer", "");
        editorDialogLLM.apply();

        // Recupera il numero di registrazioni vocali precedenti
        SharedPreferences sharedPreferences = getSharedPreferences("RecVocal", Context.MODE_PRIVATE);
        int numregistrazionivocali = sharedPreferences.getInt("numregistrazionivocali", 0);
        int numrispostegiuste = sharedPreferences.getInt("numrispostegiuste", 0);

        //Azzero il numero di risposte giuste e di registrazioni vocali se sono maggiori di 0
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("numregistrazionivocali", 0);
            editor.putInt("numrispostegiuste", 0);
            editor.apply();

        // Imposto la frase "Ciao, come stai???" da riprodurre
        String[] risposte = {
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. Sappi che gli esercizi di respirazione profonda sono fondamentali per ridurre lo stress e migliorare il benessere generale. Ti consiglio di eseguire ogni giorno il tuo allenamento giornaliero, e io sarò sempre qui ad aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. è bene sapere per te che la respirazione profonda è essenziale per rilassare il corpo e la mente. Ti aiuterà a ridurre la tensione e migliorare la concentrazione. Ricorda di eseguire il tuo allenamento giornaliero, e io sarò sempre qui per te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. il controllo del respiro è di fondamentale importanza per il tuo benessere. La respirazione profonda riduce lo stress e aumenta l'energia. Fai ogni giorno il tuo allenamento giornaliero, e io sarò al tuo fianco. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda aiuta a rallentare il battito cardiaco e a rilassarti. Più la pratichi, più ne sentirai i benefici. Non dimenticare di eseguire il tuo allenamento giornaliero, e io ti guiderò. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Devi sapere che respirare profondamente migliora l'ossigenazione del corpo e riduce l'ansia. È un'abitudine preziosa per il tuo benessere. Ti consiglio di fare il tuo allenamento giornaliero ogni giorno, e io sarò qui ad aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a rilasciare la tensione accumulata. È un esercizio semplice ma potente. Ricorda di allenarti ogni giorno con il tuo allenamento giornaliero, e io sarò qui per supportarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare bene significa vivere meglio. Gli esercizi di respirazione profonda migliorano l'umore e riducono la fatica. Fai il tuo allenamento giornaliero quotidianamente, e io sarò qui per guidarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. Attraverso la respirazione profonda puoi ridurre lo stress e aumentare la concentrazione. È un'abilità fondamentale per il tuo benessere. Ti consiglio di eseguire il tuo allenamento giornaliero ogni giorno, e io sarò al tuo fianco. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda aiuta a calmare la mente e rilassare il corpo. Se la fai regolarmente, noterai un miglioramento generale del tuo benessere. Non dimenticare di eseguire il tuo allenamento giornaliero, e io sarò qui per aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Il respiro controllato è un potente strumento per il rilassamento e il benessere. Fai ogni giorno il tuo allenamento giornaliero e io sarò qui a guidarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. La respirazione profonda riduce lo stress e migliora il tuo stato d'animo. È un piccolo gesto che porta grandi benefici. Esegui il tuo allenamento giornaliero ogni giorno e io sarò qui con te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Inspirare profondamente e poi espirare lentamente aiuta a calmare il sistema nervoso. È un'ottima pratica da eseguire ogni giorno. Non dimenticare il tuo allenamento giornaliero e io ti guiderò passo dopo passo. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. Gli esercizi di respirazione profonda migliorano la tua salute e aiutano a rilassarti. Ti consiglio di praticarle ogni giorno il tuo allenamento giornaliero. Io sarò sempre qui per aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda ti aiuta a migliorare il focus e a rilassarti. È un esercizio semplice che può fare la differenza. Fai ogni giorno il tuo allenamento giornaliero e io ti supporterò. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Con un respiro profondo puoi liberarti dallo stress e ritrovare la calma. Non dimenticare di eseguire il tuo allenamento giornaliero, e io sarò qui ad aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. Rallentare il respiro porta equilibrio tra mente e corpo. Questo esercizio ti aiuterà ogni giorno, proprio come il tuo allenamento giornaliero. Io sarò al tuo fianco. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. La respirazione profonda aiuta a ridurre la tensione e migliorare la qualità del sonno. Praticandola ogni giorno insieme al tuo allenamento giornaliero, avrai risultati sorprendenti. Io sarò sempre con te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. Respirare lentamente e profondamente ti permette di rilassarti e ricaricare le energie. Non saltare il tuo allenamento giornaliero, è essenziale per il tuo benessere. Io sarò qui per guidarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Un respiro alla volta puoi migliorare il tuo stato d'animo e ridurre lo stress. Esegui ogni giorno il tuo allenamento giornaliero, e io sarò al tuo fianco. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda ti aiuta a gestire l'ansia e a trovare equilibrio. Non dimenticare di eseguire il tuo allenamento giornaliero ogni giorno, e io sarò qui per supportarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda è un'alleata per il tuo benessere fisico e mentale. Ogni giorno è un'opportunità per migliorare. Esegui il tuo allenamento giornaliero e io sarò qui per aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a ritrovare la calma e a migliorare la tua energia. Non dimenticare di fare il tuo allenamento giornaliero ogni giorno, e io sarò qui per te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda è un modo semplice ma potente per migliorare la tua salute. Fai il tuo allenamento giornaliero ogni giorno e io sarò al tuo fianco. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a liberare la mente e a ritrovare l'equilibrio. Non dimenticare di eseguire il tuo allenamento giornaliero, e io sarò qui per aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda è un'ottima pratica per ridurre lo stress e migliorare la tua salute. Fai il tuo allenamento giornaliero ogni giorno e io sarò qui per supportarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a rilassarti e a migliorare la tua energia. Non dimenticare di eseguire il tuo allenamento giornaliero, e io sarò qui per te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda è un'alleata per il tuo benessere fisico e mentale. Ogni giorno è un'opportunità per migliorare. Esegui il tuo allenamento giornaliero e io sarò qui per aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a ritrovare la calma e a migliorare la tua energia. Non dimenticare di fare il tuo allenamento giornaliero ogni giorno, e io sarò qui per te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda è un modo semplice ma potente per migliorare la tua salute. Fai il tuo allenamento giornaliero ogni giorno e io sarò al tuo fianco. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a liberare la mente e a ritrovare l'equilibrio. Non dimenticare di eseguire il tuo allenamento giornaliero, e io sarò qui per aiutarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e ti guiderò nel tuo percorso di riabilitazione. La respirazione profonda è un'ottima pratica per ridurre lo stress e migliorare la tua salute. Fai il tuo allenamento giornaliero ogni giorno e io sarò qui per supportarti. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                "io sono NAO e sono il tuo terapista personale e sarò la tua guida nel percorso di riabilitazione. Respirare profondamente ti aiuta a rilassarti e a migliorare la tua energia. Non dimenticare di eseguire il tuo allenamento giornaliero, e io sarò qui per te. Potrai sempre trovarmi qui, cliccando sul bottone MyFisio nel menù in basso. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo."
        };
        Random random = new Random();
        int randomIndex = random.nextInt(risposte.length); // Genera un numero casuale tra 0 e 31
        // Riproduci la risposta generata
        String response = risposte[randomIndex];

        String textToSpeak = "Ciao, " + Nomeutente.getText().toString() +" "+ response;

        System.out.println("Risposta generata: " + textToSpeak);


        //Disattivo gli elementi della Homepage
            DataOdierna.setVisibility(View.GONE);
            AllenamentoGiornaliero.setVisibility(View.GONE);
            ImmagineAllenamentoGiornaliero.setVisibility(View.GONE);
            recyclerviewAllenamentiPatologie.setVisibility(View.GONE);
            AllenamentiPatologie.setVisibility(View.GONE);
            ConsigliatoPerTe.setVisibility(View.GONE);

            //Setto l'animazione di entrata
            Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

            //Mostro l'avatar del robot
            CardViewAvatar.setVisibility(View.VISIBLE);
            animazioni.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.salutoprimoaccesso);
            animazioni.setVideoURI(uri);
            animazioni.seekTo(1);
            animazioni.startAnimation(slideInBottom);
            //Quando l'animazione è finita, avvio la riproduzione del testo e del video
            slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // La riproduzione è iniziata
                    Log.i("TTS", "Animazione iniziata");
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // La riproduzione è finita
                    Log.i("TTS", "Animazione completata");

                    // Usa TextToSpeech per pronunciare la frase
                    if (textToSpeech != null) {
                        String utteranceId = "StartDialogUtterance"; // ID univoco per questa frase
                        animazioni.start();
                        textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                        // Attendi il completamento della frase
                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String utteranceId) {
                                // La riproduzione è iniziata
                                Log.i("TTS", "Riproduzione iniziata: " + utteranceId);
                            }

                            @Override
                            public void onDone(String utteranceId) {
                                if ("StartDialogUtterance".equals(utteranceId)) {
                                    Log.i("TTS", "Riproduzione completata: " + utteranceId);
                                    runOnUiThread(HomepageActivity.this::startSpeechRecognition);
                                }
                            }

                            @Override
                            public void onError(String utteranceId) {
                                Log.e("TTS", "Errore durante la riproduzione del testo: " + utteranceId);
                            }
                        });
                    } else {
                        Log.e("TTS", "TextToSpeech non inizializzato.");
                    }

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // La riproduzione è ripetuta
                    Log.i("TTS", "Animazione ripetuta");
                }
            });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Disabilita l'interazione con lo schermo durante la sintesi vocale

        //Setto l'animazione di uscita
        Animation slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);

        // Recupera il numero di registrazioni vocali precedenti
        SharedPreferences sharedPreferences = getSharedPreferences("RecVocal", Context.MODE_PRIVATE);
        int numregistrazionivocali = sharedPreferences.getInt("numregistrazionivocali", 0);
        System.out.println("Numero registrazioni vocali: " + numregistrazionivocali);

        //Recupero domanda e risposta precedente nel dialogo
        SharedPreferences sharedDialogLLM = getSharedPreferences("DialogLLM", Context.MODE_PRIVATE);
        String previousquestion = sharedDialogLLM.getString("previousquestion", "");
        String previousanswer = sharedDialogLLM.getString("previousanswer", "");

        //Recuper il giorno del primo accesso dell'utente e il giorno corrente
        SharedPreferences sharedPrefFirstAccess = getSharedPreferences("FirstAccessPrefs", Context.MODE_PRIVATE);
        int dayfirstaccess = sharedPrefFirstAccess.getInt("dayfirstaccess", 0);
        LocalDate date = LocalDate.now();
        int giornocorrente = date.getDayOfMonth();

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && !matches.isEmpty()) {
                String spokenText = matches.get(0);
                System.out.println("Testo riconosciuto: " + spokenText);

                //Traformo tutto in minuscolo
                spokenText = spokenText.toLowerCase();

                if (dayfirstaccess == giornocorrente) {

                    if(spokenText.contains("mi voglio allenare")){

                        String[] responses = {
                                "Bravo, sono felice che tu voglia iniziare l'allenamento giornaliero. Sono sicuro che ti farà bene e che ti aiuterà a raggiungere i tuoi obiettivi!!! Ora tieniti pronto!!! l'allenamento giornaliero inizierà a breve!!!",
                                "Ottima scelta! Iniziare l'allenamento è un passo importante verso il tuo benessere. Continua così e vedrai i risultati!!! Ora preparati, l'allenamento sta per iniziare!!!",
                                "Fantastico! La tua determinazione è ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Mettiti comodo, iniziamo tra poco!!!",
                                "Eccellente decisione! L'allenamento ti aiuterà a sentirti meglio e più forte. Non vedo l'ora di iniziare con te!!! Presto iniziamo, preparati bene!!!",
                                "Mi fa davvero piacere sentire che sei pronto per allenarti! Con costanza e impegno raggiungerai i tuoi traguardi!!! Mettiti comodo, tra poco si parte!!!",
                                "Grande! Ogni allenamento è un'opportunità per migliorare. Hai preso la strada giusta!!! Presto partiremo con l'allenamento, tieniti pronto!!!",
                                "Molto bene! È con questo spirito che si ottengono i migliori risultati. Continua con questa energia!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Bravissimo! Sei sulla giusta strada per migliorare sempre di più. Questo allenamento sarà un passo importante!!! Concentrati, iniziamo tra poco!!!",
                                "Sono davvero contento della tua scelta! La tua dedizione ti porterà lontano. Andiamo avanti con determinazione!!! Preparati, è quasi ora di iniziare!!!",
                                "Bellissimo spirito! La tua decisione di allenarti dimostra grande impegno. Sei pronto? Presto inizieremo!!!",
                                "Evviva! Allenarsi regolarmente è la chiave per il successo. Continua così e otterrai grandi benefici!!! Tra pochi secondi iniziamo, preparati bene!!!",
                                "Splendido! Ogni allenamento ti porta più vicino ai tuoi obiettivi. Non mollare, stai facendo grandi progressi!!! Tra poco si parte, mettiti comodo!!!",
                                "Ottima mentalità! Ogni passo che fai oggi ti avvicina ai tuoi sogni. Non vedo l'ora di iniziare!!! Preparati, l'allenamento sta per cominciare!!!",
                                "Fantastico! La tua motivazione è un esempio. Ogni allenamento ti aiuterà a stare meglio!!! Prenditi un attimo, tra poco iniziamo!!!",
                                "Meraviglioso! Allenarti con costanza è la chiave del successo. Sono sicuro che ti sentirai alla grande dopo questa sessione!!! Preparati bene, tra poco iniziamo!!!",
                                "Grandioso! Iniziare è sempre il passo più importante. Sono orgoglioso della tua scelta!!! Raccogli le energie, iniziamo tra poco!!!",
                                "Sei un campione! Ogni allenamento ti avvicina sempre più ai tuoi obiettivi. Non fermarti mai!!! Ora mettiti comodo, iniziamo!!!",
                                "Eccellente atteggiamento! Con questo spirito arriverai lontano. Continua così!!! Respira profondamente, l'allenamento parte a breve!!!",
                                "Super! Ogni allenamento è un'opportunità per crescere e migliorare. Stai facendo un lavoro incredibile!!! Preparati, tra pochissimo si parte!!!",
                                "Magnifico! Il tuo entusiasmo è contagioso. Sono certo che darai il massimo!!! Ora rilassati un attimo, poi si parte con l'allenamento!!!",
                                "Fantastico! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!",
                                "Meraviglioso! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!",
                                "Meraviglioso! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!",
                                "Meraviglioso! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!"
                        };
                        Random random = new Random();
                        int randomIndex = random.nextInt(responses.length); // Genera un numero casuale tra 0 e 39
                        // Riproduci la risposta generata
                        String response = responses[randomIndex];

                        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mivoglioallenare);
                        animazioni.setVideoURI(uri);
                        // Usa TextToSpeech per pronunciare la frase
                        if (textToSpeech != null) {
                            String utteranceId = "DialogMiVoglioAllenareUtterance"; // ID univoco per questa frase
                            animazioni.start();
                            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                            // Attendi il completamento della frase
                            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    // La riproduzione è iniziata
                                    Log.i("TTS", "Riproduzione iniziata: " + utteranceId);
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    if ("DialogMiVoglioAllenareUtterance".equals(utteranceId)) {
                                        Log.i("TTS", "Riproduzione completata: " + utteranceId);
                                        //Nascondo l'avatar del robot
                                        animazioni.startAnimation(slideOutBottom);
                                        //Quando l'animazione è finita, vado alla attività dell'allenamento giornaliero

                                        animazioni.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                navigationmenu.setSelectedItemId(R.id.Home);

                                                animazioni.setVisibility(View.GONE);
                                                CardViewAvatar.setVisibility(View.GONE);

                                                //Riattivo gli elementi della Homepage
                                                DataOdierna.setVisibility(View.VISIBLE);
                                                AllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                ImmagineAllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                recyclerviewAllenamentiPatologie.setVisibility(View.VISIBLE);
                                                AllenamentiPatologie.setVisibility(View.VISIBLE);
                                                ConsigliatoPerTe.setVisibility(View.VISIBLE);


                                                //Passo all'attività dell'allenamento giornaliero passandogli l'email dell'utente
                                                Intent intent1 = new Intent(getApplicationContext(), AllenamentoGiornalieroActivity.class);
                                                intent1.putExtra("email", email);
                                                startActivity(intent1);
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                finish();
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    Log.e("TTS", "Errore durante la riproduzione del testo: " + utteranceId);
                                }
                            });
                        } else {
                            Log.e("TTS", "TextToSpeech non inizializzato.");
                        }

                    } else if (numregistrazionivocali >= 0) {
                        // Se è la seconda registrazione vocale, chiamo il metodo SecondAnswer
                        //SecondAnswer(spokenText, sharedPreferences, numregistrazionivocali);
                        DialogLLM(spokenText, previousquestion, previousanswer);
                        System.out.println("Seconda registrazione vocale");
                    } else {
                        System.out.println("Non ho capito");
                    }

                } else {
                    // Invia il testo riconosciuto a LLaMA per ottenere una risposta
                    if (numregistrazionivocali == 0 && !spokenText.isEmpty()) {
                        FirstAnswer(spokenText, sharedPreferences, numregistrazionivocali);
                        System.out.println("Prima registrazione vocale");
                    }else if(spokenText.contains("mi voglio allenare")) {

                        String[] responses = {
                                "Bravo, sono felice che tu voglia iniziare l'allenamento giornaliero. Sono sicuro che ti farà bene e che ti aiuterà a raggiungere i tuoi obiettivi!!! Ora tieniti pronto!!! l'allenamento giornaliero inizierà a breve!!!",
                                "Ottima scelta! Iniziare l'allenamento è un passo importante verso il tuo benessere. Continua così e vedrai i risultati!!! Ora preparati, l'allenamento sta per iniziare!!!",
                                "Fantastico! La tua determinazione è ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Mettiti comodo, iniziamo tra poco!!!",
                                "Eccellente decisione! L'allenamento ti aiuterà a sentirti meglio e più forte. Non vedo l'ora di iniziare con te!!! Presto iniziamo, preparati bene!!!",
                                "Mi fa davvero piacere sentire che sei pronto per allenarti! Con costanza e impegno raggiungerai i tuoi traguardi!!! Mettiti comodo, tra poco si parte!!!",
                                "Grande! Ogni allenamento è un'opportunità per migliorare. Hai preso la strada giusta!!! Presto partiremo con l'allenamento, tieniti pronto!!!",
                                "Molto bene! È con questo spirito che si ottengono i migliori risultati. Continua con questa energia!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Bravissimo! Sei sulla giusta strada per migliorare sempre di più. Questo allenamento sarà un passo importante!!! Concentrati, iniziamo tra poco!!!",
                                "Sono davvero contento della tua scelta! La tua dedizione ti porterà lontano. Andiamo avanti con determinazione!!! Preparati, è quasi ora di iniziare!!!",
                                "Bellissimo spirito! La tua decisione di allenarti dimostra grande impegno. Sei pronto? Presto inizieremo!!!",
                                "Evviva! Allenarsi regolarmente è la chiave per il successo. Continua così e otterrai grandi benefici!!! Tra pochi secondi iniziamo, preparati bene!!!",
                                "Splendido! Ogni allenamento ti porta più vicino ai tuoi obiettivi. Non mollare, stai facendo grandi progressi!!! Tra poco si parte, mettiti comodo!!!",
                                "Ottima mentalità! Ogni passo che fai oggi ti avvicina ai tuoi sogni. Non vedo l'ora di iniziare!!! Preparati, l'allenamento sta per cominciare!!!",
                                "Fantastico! La tua motivazione è un esempio. Ogni allenamento ti aiuterà a stare meglio!!! Prenditi un attimo, tra poco iniziamo!!!",
                                "Meraviglioso! Allenarti con costanza è la chiave del successo. Sono sicuro che ti sentirai alla grande dopo questa sessione!!! Preparati bene, tra poco iniziamo!!!",
                                "Grandioso! Iniziare è sempre il passo più importante. Sono orgoglioso della tua scelta!!! Raccogli le energie, iniziamo tra poco!!!",
                                "Sei un campione! Ogni allenamento ti avvicina sempre più ai tuoi obiettivi. Non fermarti mai!!! Ora mettiti comodo, iniziamo!!!",
                                "Eccellente atteggiamento! Con questo spirito arriverai lontano. Continua così!!! Respira profondamente, l'allenamento parte a breve!!!",
                                "Super! Ogni allenamento è un'opportunità per crescere e migliorare. Stai facendo un lavoro incredibile!!! Preparati, tra pochissimo si parte!!!",
                                "Magnifico! Il tuo entusiasmo è contagioso. Sono certo che darai il massimo!!! Ora rilassati un attimo, poi si parte con l'allenamento!!!",
                                "Fantastico! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!",
                                "Meraviglioso! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!",
                                "Meraviglioso! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!",
                                "Meraviglioso! La tua energia positiva è davvero ammirevole. Questo allenamento sarà un altro passo verso il successo!!! Preparati, iniziamo tra poco!!!",
                                "Ottimo lavoro! La tua dedizione è un esempio per tutti. Continua così e vedrai grandi risultati!!! Tra pochi istanti iniziamo, preparati!!!",
                                "Grande! La tua determinazione è davvero ammirevole. Questo allenamento ti porterà sempre più vicino ai tuoi obiettivi!!! Mettiti comodo, iniziamo!!!",
                                "Eccellente! La tua motivazione è contagiosa. Sono sicuro che darai il massimo in questa sessione!!! Prenditi un attimo, tra poco si parte!!!",
                                "Fantastico! La tua costanza sta portando risultati incredibili. Continua così e vedrai grandi progressi!!! Preparati, l'allenamento sta per iniziare!!!"
                        };
                        Random random = new Random();
                        int randomIndex = random.nextInt(responses.length); // Genera un numero casuale tra 0 e 39
                        // Riproduci la risposta generata
                        String response = responses[randomIndex];

                        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mivoglioallenare);
                        animazioni.setVideoURI(uri);
                        // Usa TextToSpeech per pronunciare la frase
                        if (textToSpeech != null) {
                            String utteranceId = "DialogMiVoglioAllenareUtterance"; // ID univoco per questa frase
                            animazioni.start();
                            textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                            // Attendi il completamento della frase
                            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    // La riproduzione è iniziata
                                    Log.i("TTS", "Riproduzione iniziata: " + utteranceId);
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    if ("DialogMiVoglioAllenareUtterance".equals(utteranceId)) {
                                        Log.i("TTS", "Riproduzione completata: " + utteranceId);
                                        //Nascondo l'avatar del robot
                                        animazioni.startAnimation(slideOutBottom);
                                        //Quando l'animazione è finita, vado alla attività dell'allenamento giornaliero

                                        animazioni.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                navigationmenu.setSelectedItemId(R.id.Home);

                                                animazioni.setVisibility(View.GONE);
                                                CardViewAvatar.setVisibility(View.GONE);

                                                //Riattivo gli elementi della Homepage
                                                DataOdierna.setVisibility(View.VISIBLE);
                                                AllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                ImmagineAllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                recyclerviewAllenamentiPatologie.setVisibility(View.VISIBLE);
                                                AllenamentiPatologie.setVisibility(View.VISIBLE);
                                                ConsigliatoPerTe.setVisibility(View.VISIBLE);


                                                //Passo all'attività dell'allenamento giornaliero passandogli l'email dell'utente
                                                Intent intent1 = new Intent(getApplicationContext(), AllenamentoGiornalieroActivity.class);
                                                intent1.putExtra("email", email);
                                                startActivity(intent1);
                                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                finish();
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    Log.e("TTS", "Errore durante la riproduzione del testo: " + utteranceId);
                                }
                            });
                        } else {
                            Log.e("TTS", "TextToSpeech non inizializzato.");
                        }

                    } else if (numregistrazionivocali >= 1 && !spokenText.isEmpty()) {
                        // Se è la seconda registrazione vocale, chiamo il metodo SecondAnswer
                        //SecondAnswer(spokenText, sharedPreferences, numregistrazionivocali);
                        DialogLLM(spokenText,previousquestion,previousanswer);
                        System.out.println("Seconda registrazione vocale");
                    } else {
                        System.out.println("Non ho capito la tua risposta, prova a rispondere in maniera più semplice!!!");
                    }
                }
            }
        } else{
            //Recupero lo sharedPreferences per l'accesso dalla notifica
                SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                int login_from_notification = sharedPref.getInt("login_from_notification", 0);
                System.out.println("Login from notification: " + login_from_notification);

                String battuta = "Ti saluto " + Nomeutente.getText().toString() + " è stato un piacere aiutarti. puoi trovarmi quando vuoi cliccando sul bottone MyFisio nel menù in basso";

                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videosaluto);
                animazioni.setVideoURI(uri);
                // Usa TextToSpeech per pronunciare la frase
                if (textToSpeech != null) {
                    String utteranceId = "FineDialogUtterance"; // ID univoco per questa frase
                    animazioni.start();
                    textToSpeech.speak(battuta, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                    // Attendi il completamento della frase
                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            // La riproduzione è iniziata
                            Log.i("TTS", "Riproduzione iniziata: " + utteranceId);
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if ("FineDialogUtterance".equals(utteranceId)) {
                                Log.i("TTS", "Riproduzione completata: " + utteranceId);
                                runOnUiThread(() -> {

                                    if (login_from_notification == 1) {

                                        //Nascondo l'avatar del robot
                                        animazioni.startAnimation(slideOutBottom);
                                        //Quando l'animazione è finita, riattivo gli elementi della Homepage

                                        animazioni.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                                // La riproduzione è iniziata
                                                Log.i("TTS", "Animazione iniziata");
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                // La riproduzione è finita
                                                Log.i("TTS", "Animazione completata");

                                                navigationmenu.setSelectedItemId(R.id.Home);

                                                animazioni.setVisibility(View.GONE);
                                                CardViewAvatar.setVisibility(View.GONE);

                                                //Riattivo gli elementi della Homepage
                                                DataOdierna.setVisibility(View.VISIBLE);
                                                AllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                ImmagineAllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                recyclerviewAllenamentiPatologie.setVisibility(View.VISIBLE);
                                                AllenamentiPatologie.setVisibility(View.VISIBLE);
                                                ConsigliatoPerTe.setVisibility(View.VISIBLE);


                                                //Aspetto 1 secondo prima di passare alla prossima activity
                                                int delayMillis = 1000;
                                                new android.os.Handler().postDelayed(() -> {
                                                    //Se l'accesso è stato effettuato dalla notifica, passo alla prossima activity
                                                    Intent intent = new Intent(getApplicationContext(), AllenamentoGiornalieroActivity.class);
                                                    intent.putExtra("email", email);
                                                    startActivity(intent);
                                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                    finish();
                                                }, delayMillis);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                                // La riproduzione è ripetuta
                                                Log.i("TTS", "Animazione ripetuta");
                                            }
                                        });

                                    } else {

                                        System.out.println("Fine dialogo");

                                        //Nascondo l'avatar del robot
                                        animazioni.startAnimation(slideOutBottom);
                                        //Quando l'animazione è finita, riattivo gli elementi della Homepage

                                        animazioni.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {
                                                // La riproduzione è iniziata
                                                Log.i("TTS", "Animazione iniziata");
                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                // La riproduzione è finita
                                                Log.i("TTS", "Animazione completata");

                                                navigationmenu.setSelectedItemId(R.id.Home);

                                                animazioni.setVisibility(View.GONE);
                                                CardViewAvatar.setVisibility(View.GONE);

                                                //Riattivo gli elementi della Homepage
                                                DataOdierna.setVisibility(View.VISIBLE);
                                                AllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                ImmagineAllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                                recyclerviewAllenamentiPatologie.setVisibility(View.VISIBLE);
                                                AllenamentiPatologie.setVisibility(View.VISIBLE);
                                                ConsigliatoPerTe.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {
                                                // La riproduzione è ripetuta
                                                Log.i("TTS", "Animazione ripetuta");
                                            }
                                        });

                                    }

                                });
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                            Log.e("TTS", "Errore durante la riproduzione del testo: " + utteranceId);
                        }
                    });
                } else {
                    Log.e("TTS", "TextToSpeech non inizializzato.");
                }
        }
    }

    private void DialogLLM(String spokenText, String previousQuestion, String previousAnswer){

        SharedPreferences sharedDialogLLM = getSharedPreferences("DialogLLM", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedDialogLLM.edit();

        if (previousQuestion == "" || previousQuestion == "")
        {
            //Creo un prompt per Llama
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String prompt = "Immagina di essere il mio terapista personale specializzato in esercizi per la respirazione profonda, e io sono il tuo paziente. " +
                        "Non sono molto esperto in materia, quindi chiedo a te, che sei un esperto, di rispondere alla mia richiesta in modo semplice, chiaro e diretto. " +
                        "La tua risposta deve essere breve, facile da comprendere e scritta in un italiano perfetto, senza errori grammaticali, di pronuncia o di sintassi. " +
                        "È fondamentale che tu mi risponda solo in italiano, evitando qualsiasi altra lingua, e che il linguaggio sia adatto a una persona che non ha conoscenze tecniche avanzate. " +
                        "Ricorda che sono il tuo paziente e che il tuo aiuto è cruciale per me: evita risposte troppo lunghe, complesse o che possano creare confusione. " +
                        "La chiarezza e la precisione sono la tua priorità assoluta,per questo ti chiedo di fare anche attenzione alla punteggiatura. " +
                        "Inoltre, è importante che tu mi motivi e mi convinca a fare gli esercizi, mostrandomi i benefici e mantenendo un tono incoraggiante. " +
                        "La mia richiesta è questa: " + spokenText + ".";

                System.out.println(prompt);
                String response = "";
                try {
                    response = llamaService.sendPromptToLlama(prompt);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //Salvo la domanda e la risposta
                editor.putString("previousquestion", spokenText);
                editor.putString("previousanswer", response);
                editor.apply();

                //Riproduco la risposta di Llama

                String finalResponse = response + " Ovviamente se hai altre domande chiedi pure, invece se vuoi allenarti dì 'Mi voglio allenare' altrimenti se vuoi terminare la conversazione ti basta cliccare sullo schermo.";
                System.out.println(finalResponse);
                runOnUiThread(() -> {
                    speakResponse(finalResponse, 100);
                });
            });
        } else {

            //Creo un prompt per Llama
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                String prompt = "Immagina di essere il mio terapista personale specializzato in esercizi per la respirazione profonda, e io sono il tuo paziente. " +
                        "Durante la nostra conversazione, ti ho già fatto una domanda: " + previousQuestion + ", alla quale tu mi hai risposto: " + previousAnswer + ". " +
                        "Ora ho una nuova richiesta: " + spokenText + ". " +
                        "Rispondimi in modo semplice, chiaro e diretto, tenendo conto del contesto della nostra conversazione precedente. " +
                        "La tua risposta deve essere breve, facile da comprendere e scritta in un italiano perfetto, senza errori grammaticali, di pronuncia o di sintassi. " +
                        "È fondamentale che tu mi risponda **solo in italiano**, evitando qualsiasi altra lingua, e che il linguaggio sia adatto a una persona che non ha conoscenze tecniche avanzate. " +
                        "Ricorda che sono il tuo paziente e che il tuo aiuto è cruciale per me: evita risposte troppo lunghe, complesse o che possano creare confusione. " +
                        "La chiarezza e la precisione sono la tua priorità assoluta, per questo ti chiedo di fare anche attenzione alla punteggiatura." +
                        "Inoltre, è importante che tu mi motivi e mi convinca a fare gli esercizi, mostrandomi i benefici e mantenendo un tono incoraggiante." +
                        "Altrimenti, rispondi alla mia richiesta in modo appropriato, facendo riferimento, se necessario, alla nostra conversazione precedente. La mia richiesta è questa: " + spokenText + ".";

                String response = "";
                try {
                    response = llamaService.sendPromptToLlama(prompt);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //Salvo la domanda e la risposta
                editor.putString("previousquestion", spokenText);
                editor.putString("previousanswer", response);
                editor.apply();

                //Riproduco la risposta di Llama

                String finalResponse = response + " Ovviamente se hai altre domande chiedi pure, invece se vuoi allenarti dì 'Mi voglio allenare' altrimenti se vuoi terminare la conversazione ti basta cliccare sullo schermo.";
                System.out.println(finalResponse);
                runOnUiThread(() -> {
                    speakResponse(finalResponse, 100);
                });
            });

        }

    }



    private void FirstAnswer(String spokenText, SharedPreferences sharedPreferences, int numregistrazionivocali) {

        //Acquisisco lo sharedPreferences per le risposte giuste
        SharedPreferences sharedPreferences1 = getSharedPreferences("RecVocal", Context.MODE_PRIVATE);
        AtomicInteger numerorispostegiuste = new AtomicInteger(sharedPreferences.getInt("numrispostegiuste", 0));
        AtomicInteger tempnumregistrazionivocali = new AtomicInteger(numregistrazionivocali);

        //Uso LLAMA per capire la risposta dell'utente se è una risposta positiva o negativa
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
                // Costruisci il prompt da inviare a LLaMA
            String prompt = "Sei un terapista virtuale. Alla domanda 'Come stai oggi?' il tuo paziente ha risposto dicendo: '" + spokenText + "'. " +
                    "Devi analizzare la seguente risposta per capire se è una risposta positiva o negativa rispondendomi 'positiva' se è positiva o 'negativa' se la risposta è negativa. " +
                    "Concentrati su tutta la risposta e sul significato che vuole trasmettere ricordandoti che la domanda a cui si riferisce è 'Come stai oggi?'. " +
                    "Se per te questa risposta significa che non sto bene (es. sono triste, stanco, stressato, insoddisfatto), rispondi esattamente con la parola 'negativa'. " +
                    "Altrimenti, se la risposta significa che sto bene, che la giornata va alla grande, che sono felice, gentile, soddisfatto o comunque non ho problemi, rispondi esattamente con la parola 'positiva'. " +
                    "Fai attenzione: se la risposta contiene parole o frasi come 'va tutto bene', 'tutto bene', 'sto bene', 'sto molto bene', 'tutto a posto', 'tutto perfetto', 'sto benissimo', 'tutto ok', 'tutto sotto controllo', 'mi sento benissimo', 'tutto fila liscio', 'tutto va alla grande', 'sto alla grande', 'tutto è perfetto', 'mi sento in forma', 'tutto scorre bene', 'sto facendo progressi', 'mi sento forte', 'mi sento stabile', 'mi sento energico', 'mi sento sicuro di me', classificala come 'positiva', a meno che non ci siano chiari indicatori di negatività. " +
                    "Non devi interpretare oltre ciò che è scritto. Non aggiungere altro testo, commenti o spiegazioni. " +
                    "La tua risposta deve essere obbligatoriamente una sola parola: 'positiva' o 'negativa'.";
           String risposta = "";
            try {
                risposta = llamaService.sendPromptToLlama(prompt);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //String response = llamaService.sendPromptToLlama(prompt);
                //System.out.println("La risposta di Llama è: " + response);
                Set<String>  positiveResponses = new HashSet<>(Arrays.asList( "Sto molto bene","molto bene","molto molto bene","sto molto molto bene","Tutto bene grazie","Sto bene grazie","tutto alla grande grazie","bene grazie","va tutto benissimo grazie","tutto perfetto grazzie","mi sento bene grazie","tutto alla granissima grazie","grazie è tutto apposto","tutto apposto grazie","sto bene grazie","Mi sento ok", "mi sento veramente strabene" ,"Mi sento veramente super bene", "Mi sento strabene", "strabene", "stra bene","Alla grandissima","Bene", "Sto bene", "Mi sento bene", "Va bene", "Tutto bene", "Davvero bene", "Sto davvero bene", "Benissimo", "Sto benissimo", "Mi sento benissimo", "Alla grande", "Sto alla grande", "Mi sento alla grande", "Fantastico", "Sto fantastico", "Mi sento fantastico", "Ottimo", "Sto ottimamente", "Mi sento ottimo", "Perfetto", "Sto perfettamente", "Va tutto alla perfezione", "Eccellente", "Sto eccellente", "Mi sento eccellente", "Tutto ok", "Va tutto ok", "Super", "Mi sento super", "Mi sento al top", "Sono al top", "Va tutto alla grande", "Oggi mi sento alla grande", "Oggi è una bella giornata", "Mi sento fortissimo", "Sto migliorando", "Mi sento felice", "Sono felice", "Felice e contento", "Contento", "Sto contento", "Mi sento sereno", "Sono sereno", "Mi sento tranquillo", "Tranquillo", "Sto rilassato", "Rilassato", "Oggi mi sento rilassato", "Mi sento carico", "Carico", "Mi sento energico", "Pieno di energia", "Sto pimpante", "Oggi sono pimpante", "Mi sento motivato", "Sono motivato", "Mi sento fortunato", "Sono fortunato", "Mi sento amato", "Oggi mi sento speciale", "Mi sento rinato", "Mi sento leggero", "Oggi mi sento leggero come una piuma", "Giornata perfetta", "Oggi splendo", "Mi sento inarrestabile", "Oggi è un gran giorno", "Sto proprio bene", "Mi sento meglio", "Mi sento pieno di voglia di fare", "Oggi mi sento figo", "Mi sento forte", "Mi sento invincibile", "Sto proprio da dio", "Mi sento più forte che mai", "Mi sento su di giri", "Va tutto liscio", "Mi sento sereno e tranquillo", "Sto migliorando ogni giorno", "Va tutto alla grande", "Mi sento come nuovo", "Sto in pace", "Mi sento in equilibrio", "Mi sento soddisfatto", "Sono soddisfatto", "Sto come un re", "Mi sento fortissimo oggi", "Oggi sono al massimo", "Mi sento rinvigorito", "Oggi è una giornata top", "Sto proprio bene oggi", "Oggi il mondo è più bello", "Mi sento sollevato", "Oggi è tutto perfetto", "Sono felice senza motivo", "Sto carico a mille", "Mi sento elettrizzato", "Mi sento pieno di energia", "Oggi mi sento al massimo delle mie capacità", "Mi sento in splendida forma","Non male ma si può sempre migliorare","Non male ma può andare meglio","Non male ma potrebbe andare meglio","Va tutto bene","va tutto alla grande", "è tutt ok","ok","ok sto bene","mi sento ok","mi sento alla grande","va tutto alla perfezione","va tutto nel migliore dei modi","va tutto super bene","va tutto strebene","va tutto alla grandissima","è tutto perfetto","tutto ok","sto ok","ok sto bene","non ci sono cose che vanno male","niente va male","sto abbastanza bene","mi sento abbastanza bene","sto abbastanza ok","mi sento abbastanza ok","sto abbastanza alla grande","mi sento abbastanza alla grande","sto abbastanza bene grazie","mi sento abbastanza bene grazie","sto abbastanza bene grazie a te","mi sento abbastanza bene grazie a te","sto abbastanza bene grazie a voi","mi sento abbastanza bene grazie"));
                Set<String> negativeResponses = new HashSet<>(Arrays.asList("Non sto molto bene","Non molto bene","non benissimo","non sto bene","non sto molto molto bene","non molto molto bene","Male", "Sto male", "Mi sento male", "Va male", "Non bene", "Non sto bene", "Non mi sento bene", "Tutto male", "Malissimo", "Sto malissimo", "Mi sento malissimo", "Non troppo bene", "Non mi sento al meglio", "Così così", "Mah", "Insomma", "Potrebbe andare meglio", "Oggi non è giornata", "Mi sento a pezzi", "Sono stanco", "Mi sento stanco", "Sono esausto", "Mi sento esausto", "Non ce la faccio più", "Sono giù", "Mi sento giù", "Sono triste", "Mi sento triste", "Sono giù di morale", "Mi sento sotto tono", "Sono abbattuto", "Sono demoralizzato", "Mi sento demoralizzato", "Sono scoraggiato", "Sono stressato", "Mi sento stressato", "Mi sento vuoto", "Sono ansioso", "Mi sento ansioso", "Sono agitato", "Mi sento agitato", "Sono preoccupato", "Mi sento preoccupato", "Sono nervoso", "Mi sento nervoso", "Ho il morale a terra", "Mi sento debole", "Sono scarico", "Mi sento scarico", "Oggi mi sento svuotato", "Sono avvilito", "Mi sento avvilito", "Oggi proprio no", "Non riesco a concentrarmi", "Oggi mi sento spento", "Mi sento inquieto", "Sono demotivato", "Mi sento demotivato", "Mi sento perso", "Mi sento confuso", "Non sto bene oggi", "Mi sento fragile", "Mi sento instabile", "Oggi non mi sento per niente bene", "Mi sento giù senza motivo", "Sono infelice", "Mi sento infelice", "Non ho voglia di fare nulla", "Mi sento frustrato", "Sono frustrato", "Oggi tutto va storto", "Oggi non vedo nulla di positivo", "Mi sento vulnerabile", "Oggi sono sottotono", "Mi sento irritabile", "Sono di pessimo umore", "Mi sento pesante", "Mi sento schiacciato", "Oggi è una giornata no", "Non ho energia", "Mi sento come se fossi sotto un peso", "Mi sento deluso", "Sono deluso", "Mi sento abbattuto e stanco", "Mi sento privo di motivazione", "Sono senza forze", "Non ho voglia di fare nulla", "Mi sento esausto mentalmente", "Mi sento sopraffatto", "Mi sento in crisi", "Sono giù di corda", "Oggi non ho stimoli", "Mi sento depresso", "Oggi mi sento di troppo", "Oggi mi sento spento e senza voglia", "Mi sento solo", "Mi sento apatico", "Sono a terra", "Oggi è un disastro", "Oggi è proprio una brutta giornata", "Mi sento come un peso morto", "Mi sento inutile", "Mi sento privo di speranza", "Non mi sento bene per niente", "Oggi sono proprio fuori fase", "Mi sento completamente scarico", "Oggi va tutto storto", "Mi sento un fallimento", "Mi sento giù di corda", "Mi sento un po’ depresso", "Oggi è una giornata orrenda", "Mi sento abbattuto senza motivo","Sto veramente male","Sto uno schifo","Mi sento uno schifo","Non sto per niente bene","Sto uno schifo","Uno schifo","Mi sento uno schifo","Mi sento pessimo","non mi sento ok","va tutto male","va tutto malissimo","va tutto uno schifo","va tutto nel peggiore dei modi","niente ok","niente è ok","niente va nel verso giusto","nulla va nel verso giusto","tutto va nel peggiore dei modi","nulla va come deve andare","va malissimo","va male"));


            String finalRisposta = risposta;
            System.out.println("Risposta di Llama: " + finalRisposta);
            runOnUiThread(() -> {
                    // Usa la risposta sulla UI
                    if ( containsIgnoreCase(positiveResponses,spokenText) || containsIgnoreCase(negativeResponses,spokenText)) {

                        if (containsIgnoreCase(positiveResponses,spokenText)) {
                            //Aggiorno il numero di registrazioni vocali

                            tempnumregistrazionivocali.getAndIncrement();
                            // Setto lo sharedPreferences

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("numregistrazionivocali", tempnumregistrazionivocali.get());
                            editor.apply();

                            //Incremento il numero di risposte giuste e aggiorno lo sharedPreferences
                            numerorispostegiuste.getAndIncrement();
                            SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                            editor1.putInt("numrispostegiuste", numerorispostegiuste.get());
                            editor1.apply();

                            //Setto il prompt giusto da passare al modello LLama
                            // Chiamare LLaMA per ottenere la risposta
                            ExecutorService executor1 = Executors.newSingleThreadExecutor();
                            executor1.execute(() -> {
                                try {
                                    /*String[] responses = {
                                            "EVVIVA!!!! Sono felicissimo che stai bene e che l'allenamento sta migliorando la tua salute!!! Continuiamo con questo spirito e vedrai che raggiungerai i tuoi obiettivi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Felicissimo di sentirlo!!! La tua energia è contagiosa e l'allenamento sta dando i suoi frutti!!! Continua così e arriveranno sempre più risultati!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Ottimo!!! Sapere che stai bene mi riempie di gioia e dimostra che il duro lavoro paga!!! Andiamo avanti con determinazione e raggiungeremo grandi obiettivi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! Ogni giorno un passo avanti verso il tuo benessere!!! La costanza sta facendo la differenza, non fermarti adesso!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Super notizia!!! La tua salute migliora e i tuoi progressi sono evidenti!!! Con questa mentalità non ci sono limiti a ciò che puoi raggiungere!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Meraviglioso!!! Vedere i tuoi miglioramenti mi riempie di soddisfazione!!! Sei sulla strada giusta per ottenere il massimo dalla tua routine!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Sono felicissimo di sentirlo!!! Il tuo impegno è davvero ammirevole e i risultati si vedono!!! Continua con questa determinazione!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Evviva!!! Questo è il miglior risultato possibile e dimostra che il tuo lavoro sta pagando!!! Andiamo avanti con lo stesso entusiasmo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Evviva!!! Questo è il miglior risultato possibile e dimostra che il tuo lavoro sta pagando!!! Andiamo avanti con lo stesso entusiasmo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Splendido!!! La tua energia e il tuo impegno stanno facendo la differenza!!! Continua a dare il massimo e il successo sarà garantito!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Che gioia sapere che stai bene!!! La tua determinazione e il tuo spirito di sacrificio sono ammirevoli e ti porteranno sempre più lontano!!! Non mollare mai!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Molto bene!!! Il tuo impegno e la tua disciplina stanno dando i loro frutti!!! Con questa dedizione i tuoi risultati saranno straordinari!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Grande!!! Sentire che stai bene mi rende davvero felice perché so quanto impegno ci stai mettendo!!! Non fermarti ora e continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Ottime notizie!!! Sei sulla giusta via per migliorare sempre di più e il tuo lavoro sta portando risultati concreti!!! Continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi fa piacere sapere che ti senti bene!!! Significa che stai trovando il giusto equilibrio tra impegno e recupero!!! Non mollare!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Splendido!!! Ogni giorno fai progressi e questo è fantastico per il tuo percorso di miglioramento!!! Continua a lavorare sodo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Sono molto contento di sapere che stai bene!!! Questo dimostra che sei sulla strada giusta e che il tuo allenamento sta funzionando!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Eccellente!!! Continua con questa energia e vedrai ancora più miglioramenti nel tuo percorso di allenamento!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Perfetto!!! Sapere che stai bene è la cosa più importante e mi motiva a supportarti sempre meglio!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Che bello sentire che stai bene!!! Significa che il tuo allenamento sta procedendo alla grande e i tuoi progressi sono reali!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Bellissimo!!! Andiamo avanti con lo stesso entusiasmo perché il tuo miglioramento è evidente e ogni sforzo vale la pena!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! La tua energia positiva è contagiosa e dimostra che sei sulla strada giusta!!! Continua così e vedrai risultati incredibili!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Magnifico!!! Sapere che stai bene mi riempie di orgoglio e mi motiva a supportarti ancora di più!!! Non fermarti ora!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Stupendo!!! Il tuo impegno e la tua costanza stanno portando risultati straordinari!!! Continua a dare il massimo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Incredibile!!! Ogni giorno fai progressi e questo è il segno che stai lavorando nel modo giusto!!! Non mollare mai!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! La tua determinazione è ammirevole e i tuoi progressi sono evidenti!!! Continua così e raggiungerai grandi traguardi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Eccellente!!! La tua energia e il tuo impegno sono davvero contagiosi!!! Continua a lavorare sodo e vedrai risultati incredibili!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare , dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Meraviglioso!!! Sapere che stai bene è la migliore notizia possibile!!! Continua con questa determinazione e vedrai grandi risultati!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Straordinario!!! Il tuo impegno sta portando frutti incredibili e questo è solo l'inizio!!! Continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! La tua energia positiva è davvero contagiosa e dimostra che sei sulla strada giusta!!! Non fermarti ora!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Incredibile!!! Ogni giorno fai progressi e questo è il segno che stai lavorando nel modo giusto!!! Non mollare mai!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Magnifico!!! La tua determinazione è ammirevole e i tuoi progressi sono evidenti!!! Continua così e raggiungerai grandi traguardi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Stupendo!!! La tua energia e il tuo impegno sono davvero contagiosi!!! Continua a lavorare sodo e vedrai risultati incredibili!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Eccellente!!! Sapere che stai bene è la migliore notizia possibile!!! Continua con questa determinazione e vedrai grandi risultati!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! Il tuo impegno sta portando frutti incredibili e questo è solo l'inizio!!! Continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo."
                                    };

                                    Random random = new Random();
                                    int randomIndex = random.nextInt(responses.length); // Genera un numero casuale tra 0 e 19
                                    // Riproduci la risposta generata

                                    String response2 = responses[randomIndex];*/

                                    //Uso LLAMA per generare una risposta
                                    String prompt1 = "Immagina che tu sei il mio terapista e io sono il tuo paziente. tu mi hai chiesto \"come stai?\" e io ti ho detto che sto bene. A questo punto tu mi devi rispondere con una frase simile a questa 'Molto bene!!! Il tuo impegno e la tua disciplina stanno dando i loro frutti!!! Con questa dedizione i tuoi risultati saranno straordinari!!!' La frase deve poi terminare esattamente così 'Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.'. mi raccomando prendi la prima frase come spunto mentre la seconda deve essere esttamente come te l'ho scritta. rispondimi solamente in italiano senza usare altre lingue. ";
                                    String response1 = llamaService.sendPromptToLlama(prompt1);
                                    response1 = response1.replaceAll("[^a-zA-Zà-ùÀ-Ù0-9.,!?'\\s]", "");
                                    System.out.println("Risposta di LLama: " + response1);
                                    String finalResponse = response1;
                                    runOnUiThread(() -> {
                                        speakResponse(finalResponse, 0);
                                    });



                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        } else {
                            tempnumregistrazionivocali.getAndIncrement();
                            // Setto lo sharedPreferences

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("numregistrazionivocali", tempnumregistrazionivocali.get());
                            editor.apply();


                            //Incremento il numero di risposte giuste e aggiorno lo sharedPreferences
                            numerorispostegiuste.getAndIncrement();
                            SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                            editor1.putInt("numrispostegiuste", numerorispostegiuste.get());
                            editor1.apply();



                            // Chiamare LLaMA per ottenere la risposta
                            ExecutorService executor1 = Executors.newSingleThreadExecutor();
                            executor1.execute(() -> {
                                try {
                                    /*String[] responses = {
                                            "Mi dispiace tantissimo che tu non stai bene, spero sia soltanto un periodo di passaggio. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi rattrista sapere che non ti senti bene, voglio aiutarti a stare meglio il prima possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Capisco che non sia facile, ma sono qui per supportarti e aiutarti a migliorare. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace davvero sapere che non ti senti al meglio, cerchiamo di capire come posso aiutarti. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "È normale avere giornate difficili, ma troveremo insieme il modo di superarle. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che non è piacevole sentirsi così, ma non sei solo e possiamo affrontarlo insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto, voglio trovare il modo di rendere questo percorso più facile per te. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non deve essere facile per te, ma possiamo lavorare per migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Capisco come ti senti, e voglio fare il possibile per aiutarti a stare meglio. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "È brutto non sentirsi al meglio, ma passo dopo passo possiamo migliorare. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi spiace molto sentire che non stai bene, troviamo un modo per affrontarlo insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non sei solo in questo, voglio aiutarti a sentirti meglio e più sereno. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere frustrante, ma voglio aiutarti a superare questo momento. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che non stai bene, ma possiamo trovare una soluzione insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Voglio davvero aiutarti a stare meglio, cerchiamo di capire cosa possiamo fare. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non è facile affrontare questi momenti, ma io sono qui per supportarti. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere difficile, ma affrontiamo la situazione un passo alla volta. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi rattrista sentirlo, ma insieme possiamo trovare il modo di migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Vorrei poterti aiutare di più, dimmi se posso fare qualcosa per te. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu stia passando un momento difficile, ma vedremo come affrontarlo. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto sapere che non ti senti bene, ma sono qui per aiutarti a superare questo momento. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere dura, ma voglio fare tutto il possibile per supportarti. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi rattrista sentire che non stai bene, ma insieme possiamo trovare una via d'uscita. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non devi affrontare tutto da solo, sono qui per aiutarti a superare questo momento. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che stai passando un periodo difficile, ma possiamo lavorare per migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere pesante, ma voglio aiutarti a trovare un po' di sollievo. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu non ti senta bene, ma possiamo affrontare questa situazione insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non sei solo, sono qui per aiutarti a superare questo momento difficile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto che tu stia passando un momento difficile, ma possiamo trovare una soluzione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere frustrante, ma voglio aiutarti a sentirti meglio il prima possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che non stai bene, ma sono qui per supportarti in ogni modo possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non devi sentirti solo, sono qui per aiutarti a superare questo momento difficile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu stia passando un periodo difficile, ma possiamo lavorare per migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere pesante, ma voglio aiutarti a trovare un po' di sollievo. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu non ti senta bene, ma possiamo affrontare questa situazione insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non sei solo, sono qui per aiutarti a superare questo momento difficile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto che tu stia passando un momento difficile, ma possiamo trovare una soluzione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere frustrante, ma voglio aiutarti a sentirti meglio il prima possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che non stai bene, ma sono qui per supportarti in ogni modo possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo."
                                    };

                                    Random random = new Random();
                                    int randomIndex = random.nextInt(responses.length); // Genera un numero casuale tra 0 e 19
                                    // Riproduci la risposta generata

                                    String response2 = responses[randomIndex];*/

                                    // Riproduci la risposta generata
                                    //Uso LLAMA per generare una risposta
                                    String prompt1 = "Immagina che tu sei il mio terapista e io sono il tuo paziente. tu mi hai chiesto \"come stai?\" e io ti ho detto che non sto bene. a questo punto tu mi devi dire una frase in cui esprimi la tua preoccupazione come ad esempio 'Mi dispiace sapere che non stai bene, ma sono qui per supportarti in ogni modo possibile' o una frase simile. al termine della frase mi devi poi dire esattamente 'Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.'.Prendi la prima frase come spunto per generarne un'altra simile mentre la seconda deve essere esttamente come te l'ho passata. mi raccomando rispodnimi in italiano e senza usare altre lingue.";
                                    String response1 = llamaService.sendPromptToLlama(prompt1);
                                    response1 = response1.replaceAll("[^a-zA-Zà-ùÀ-Ù0-9.,!?'\\s]", "");
                                    System.out.println("Risposta di LLama: " + response1);
                                    String finalResponse = response1;
                                    runOnUiThread(() -> {
                                        speakResponse(finalResponse, 1);
                                    });
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }else if(finalRisposta.contains("positiva")|| finalRisposta.contains("Positiva")||finalRisposta.contains("POSITIVA")||finalRisposta.contains("negativa") || finalRisposta.contains("NEGATIVA") || finalRisposta.contains("Negativa") ){
                        if (finalRisposta.contains("positiva")|| finalRisposta.contains("Positiva")||finalRisposta.contains("POSITIVA")) {
                            //Aggiorno il numero di registrazioni vocali

                            tempnumregistrazionivocali.getAndIncrement();
                            // Setto lo sharedPreferences

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("numregistrazionivocali", tempnumregistrazionivocali.get());
                            editor.apply();

                            //Incremento il numero di risposte giuste e aggiorno lo sharedPreferences
                            numerorispostegiuste.getAndIncrement();
                            SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                            editor1.putInt("numrispostegiuste", numerorispostegiuste.get());
                            editor1.apply();

                            //Setto il prompt giusto da passare al modello LLama
                            // Chiamare LLaMA per ottenere la risposta
                            ExecutorService executor1 = Executors.newSingleThreadExecutor();
                            executor1.execute(() -> {
                                try {
                                    /*String[] responses = {
                                            "EVVIVA!!!! Sono felicissimo che stai bene e che l'allenamento sta migliorando la tua salute!!! Continuiamo con questo spirito e vedrai che raggiungerai i tuoi obiettivi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Felicissimo di sentirlo!!! La tua energia è contagiosa e l'allenamento sta dando i suoi frutti!!! Continua così e arriveranno sempre più risultati!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Ottimo!!! Sapere che stai bene mi riempie di gioia e dimostra che il duro lavoro paga!!! Andiamo avanti con determinazione e raggiungeremo grandi obiettivi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! Ogni giorno un passo avanti verso il tuo benessere!!! La costanza sta facendo la differenza, non fermarti adesso!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Super notizia!!! La tua salute migliora e i tuoi progressi sono evidenti!!! Con questa mentalità non ci sono limiti a ciò che puoi raggiungere!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Meraviglioso!!! Vedere i tuoi miglioramenti mi riempie di soddisfazione!!! Sei sulla strada giusta per ottenere il massimo dalla tua routine!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Sono felicissimo di sentirlo!!! Il tuo impegno è davvero ammirevole e i risultati si vedono!!! Continua con questa determinazione!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Evviva!!! Questo è il miglior risultato possibile e dimostra che il tuo lavoro sta pagando!!! Andiamo avanti con lo stesso entusiasmo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Evviva!!! Questo è il miglior risultato possibile e dimostra che il tuo lavoro sta pagando!!! Andiamo avanti con lo stesso entusiasmo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Splendido!!! La tua energia e il tuo impegno stanno facendo la differenza!!! Continua a dare il massimo e il successo sarà garantito!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Che gioia sapere che stai bene!!! La tua determinazione e il tuo spirito di sacrificio sono ammirevoli e ti porteranno sempre più lontano!!! Non mollare mai!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Molto bene!!! Il tuo impegno e la tua disciplina stanno dando i loro frutti!!! Con questa dedizione i tuoi risultati saranno straordinari!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Grande!!! Sentire che stai bene mi rende davvero felice perché so quanto impegno ci stai mettendo!!! Non fermarti ora e continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Ottime notizie!!! Sei sulla giusta via per migliorare sempre di più e il tuo lavoro sta portando risultati concreti!!! Continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi fa piacere sapere che ti senti bene!!! Significa che stai trovando il giusto equilibrio tra impegno e recupero!!! Non mollare!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Splendido!!! Ogni giorno fai progressi e questo è fantastico per il tuo percorso di miglioramento!!! Continua a lavorare sodo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Sono molto contento di sapere che stai bene!!! Questo dimostra che sei sulla strada giusta e che il tuo allenamento sta funzionando!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Eccellente!!! Continua con questa energia e vedrai ancora più miglioramenti nel tuo percorso di allenamento!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Perfetto!!! Sapere che stai bene è la cosa più importante e mi motiva a supportarti sempre meglio!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Che bello sentire che stai bene!!! Significa che il tuo allenamento sta procedendo alla grande e i tuoi progressi sono reali!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Bellissimo!!! Andiamo avanti con lo stesso entusiasmo perché il tuo miglioramento è evidente e ogni sforzo vale la pena!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! La tua energia positiva è contagiosa e dimostra che sei sulla strada giusta!!! Continua così e vedrai risultati incredibili!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Magnifico!!! Sapere che stai bene mi riempie di orgoglio e mi motiva a supportarti ancora di più!!! Non fermarti ora!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Stupendo!!! Il tuo impegno e la tua costanza stanno portando risultati straordinari!!! Continua a dare il massimo!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Incredibile!!! Ogni giorno fai progressi e questo è il segno che stai lavorando nel modo giusto!!! Non mollare mai!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! La tua determinazione è ammirevole e i tuoi progressi sono evidenti!!! Continua così e raggiungerai grandi traguardi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Eccellente!!! La tua energia e il tuo impegno sono davvero contagiosi!!! Continua a lavorare sodo e vedrai risultati incredibili!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare , dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Meraviglioso!!! Sapere che stai bene è la migliore notizia possibile!!! Continua con questa determinazione e vedrai grandi risultati!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Straordinario!!! Il tuo impegno sta portando frutti incredibili e questo è solo l'inizio!!! Continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! La tua energia positiva è davvero contagiosa e dimostra che sei sulla strada giusta!!! Non fermarti ora!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Incredibile!!! Ogni giorno fai progressi e questo è il segno che stai lavorando nel modo giusto!!! Non mollare mai!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Magnifico!!! La tua determinazione è ammirevole e i tuoi progressi sono evidenti!!! Continua così e raggiungerai grandi traguardi!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Stupendo!!! La tua energia e il tuo impegno sono davvero contagiosi!!! Continua a lavorare sodo e vedrai risultati incredibili!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Eccellente!!! Sapere che stai bene è la migliore notizia possibile!!! Continua con questa determinazione e vedrai grandi risultati!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Fantastico!!! Il tuo impegno sta portando frutti incredibili e questo è solo l'inizio!!! Continua così!!! Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo."
                                    };


                                    Random random = new Random();
                                    int randomIndex = random.nextInt(responses.length); // Genera un numero casuale tra 0 e 19
                                    // Riproduci la risposta generata

                                    String response2 = responses[randomIndex];*/

                                    //Uso LLAMA per generare una risposta
                                    String prompt1 = "Immagina che tu sei il mio terapista e io sono il tuo paziente. tu mi hai chiesto \"come stai?\" e io ti ho detto che sto bene. A questo punto tu mi devi rispondere con una frase simile a questa 'Molto bene!!! Il tuo impegno e la tua disciplina stanno dando i loro frutti!!! Con questa dedizione i tuoi risultati saranno straordinari!!!' La frase deve poi terminare esattamente così 'Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.'. mi raccomando prendi la prima frase come spunto mentre la seconda deve essere esttamente come te l'ho scritta. rispondimi solamente in italiano senza usare altre lingue. ";

                                    String response1 = llamaService.sendPromptToLlama(prompt1);
                                    response1 = response1.replaceAll("[^a-zA-Zà-ùÀ-Ù0-9.,!?'\\s]", "");
                                    System.out.println("Risposta di LLama: " + response1);
                                    String finalResponse = response1;
                                    runOnUiThread(() -> {
                                        speakResponse(finalResponse, 0);
                                    });



                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        } else {
                            tempnumregistrazionivocali.getAndIncrement();
                            // Setto lo sharedPreferences

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("numregistrazionivocali", tempnumregistrazionivocali.get());
                            editor.apply();


                            //Incremento il numero di risposte giuste e aggiorno lo sharedPreferences
                            numerorispostegiuste.getAndIncrement();
                            SharedPreferences.Editor editor1 = sharedPreferences1.edit();
                            editor1.putInt("numrispostegiuste", numerorispostegiuste.get());
                            editor1.apply();



                            // Chiamare LLaMA per ottenere la risposta
                            ExecutorService executor1 = Executors.newSingleThreadExecutor();
                            executor1.execute(() -> {
                                try {
                                    /*String[] responses = {
                                            "Mi dispiace tantissimo che tu non stai bene, spero sia soltanto un periodo di passaggio. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi rattrista sapere che non ti senti bene, voglio aiutarti a stare meglio il prima possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Capisco che non sia facile, ma sono qui per supportarti e aiutarti a migliorare. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi volgio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace davvero sapere che non ti senti al meglio, cerchiamo di capire come posso aiutarti. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "È normale avere giornate difficili, ma troveremo insieme il modo di superarle. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che non è piacevole sentirsi così, ma non sei solo e possiamo affrontarlo insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto, voglio trovare il modo di rendere questo percorso più facile per te. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non deve essere facile per te, ma possiamo lavorare per migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Capisco come ti senti, e voglio fare il possibile per aiutarti a stare meglio. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "È brutto non sentirsi al meglio, ma passo dopo passo possiamo migliorare. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi spiace molto sentire che non stai bene, troviamo un modo per affrontarlo insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non sei solo in questo, voglio aiutarti a sentirti meglio e più sereno. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere frustrante, ma voglio aiutarti a superare questo momento. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che non stai bene, ma possiamo trovare una soluzione insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Voglio davvero aiutarti a stare meglio, cerchiamo di capire cosa possiamo fare. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non è facile affrontare questi momenti, ma io sono qui per supportarti. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere difficile, ma affrontiamo la situazione un passo alla volta. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio alenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi rattrista sentirlo, ma insieme possiamo trovare il modo di migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Vorrei poterti aiutare di più, dimmi se posso fare qualcosa per te. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu stia passando un momento difficile, ma vedremo come affrontarlo. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto sapere che non ti senti bene, ma sono qui per aiutarti a superare questo momento. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere dura, ma voglio fare tutto il possibile per supportarti. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio alleanare ', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi rattrista sentire che non stai bene, ma insieme possiamo trovare una via d'uscita. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non devi affrontare tutto da solo, sono qui per aiutarti a superare questo momento. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio ellanare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che stai passando un periodo difficile, ma possiamo lavorare per migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere pesante, ma voglio aiutarti a trovare un po' di sollievo. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu non ti senta bene, ma possiamo affrontare questa situazione insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non sei solo, sono qui per aiutarti a superare questo momento difficile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto che tu stia passando un momento difficile, ma possiamo trovare una soluzione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere frustrante, ma voglio aiutarti a sentirti meglio il prima possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che non stai bene, ma sono qui per supportarti in ogni modo possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non devi sentirti solo, sono qui per aiutarti a superare questo momento difficile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu stia passando un periodo difficile, ma possiamo lavorare per migliorare la situazione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere pesante, ma voglio aiutarti a trovare un po' di sollievo. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace che tu non ti senta bene, ma possiamo affrontare questa situazione insieme. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi volgio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Non sei solo, sono qui per aiutarti a superare questo momento difficile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace molto che tu stia passando un momento difficile, ma possiamo trovare una soluzione. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi volgio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "So che può essere frustrante, ma voglio aiutarti a sentirti meglio il prima possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.",
                                            "Mi dispiace sapere che non stai bene, ma sono qui per supportarti in ogni modo possibile. Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Se sei pronto per allenarti, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo."
                                    };
                                    Random random = new Random();
                                    int randomIndex = random.nextInt(responses.length); // Genera un numero casuale tra 0 e 19
                                    // Riproduci la risposta generata

                                    String response2 = responses[randomIndex];*/

                                    // Riproduci la risposta generata
                                    //Uso LLAMA per generare una risposta
                                    String prompt1 = "Immagina che tu sei il mio terapista e io sono il tuo paziente. tu mi hai chiesto \"come stai?\" e io ti ho detto che non sto bene. a questo punto tu mi devi dire una frase in cui esprimi la tua preoccupazione come ad esempio 'Mi dispiace sapere che non stai bene, ma sono qui per supportarti in ogni modo possibile' o una frase simile. al termine della frase mi devi poi dire esattamente 'Se hai domande o dubbi non esitare a chiedere, io sono qui per te! Invece se ti vuoi allenare, dì 'Mi voglio allenare', altrimenti, se vuoi terminare la conversazione, ti basta cliccare sullo schermo.'.Prendi la prima frase come spunto per generarne un'altra simile mentre la seconda deve essere esttamente come te l'ho passata. mi raccomando rispodnimi in italiano e senza usare altre lingue.";
                                    String response1 = llamaService.sendPromptToLlama(prompt1);
                                    response1 = response1.replaceAll("[^a-zA-Zà-ùÀ-Ù0-9.,!?'\\s]", "");
                                    System.out.println("Risposta di LLama: " + response1);
                                    String finalResponse = response1;
                                    runOnUiThread(() -> {
                                        speakResponse(finalResponse, 1);
                                    });
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }



                    } else {

                        // Imposto la frase "Non ho capito la tua risposta, prova a rispondere in maniera più semplice!!!" da riprodurre
                        String textToSpeak = Nomeutente.getText().toString() + " Non ho capito la tua risposta, prova a rispondere in maniera più semplice!!! Allora???!!! Come stai????!!!";

                        // Usa TextToSpeech per pronunciare la frase
                        if (textToSpeech != null) {
                            String utteranceId = "DialogPuoiRipetereUtterance"; // ID univoco per questa frase
                            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);

                            // Attendi il completamento della frase
                            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                @Override
                                public void onStart(String utteranceId) {
                                    // La riproduzione è iniziata
                                    Log.i("TTS", "Riproduzione iniziata: " + utteranceId);
                                }

                                @Override
                                public void onDone(String utteranceId) {
                                    if ("DialogPuoiRipetereUtterance".equals(utteranceId)) {
                                        Log.i("TTS", "Riproduzione completata: " + utteranceId);
                                        runOnUiThread(() -> startSpeechRecognition());
                                    }
                                }

                                @Override
                                public void onError(String utteranceId) {
                                    Log.e("TTS", "Errore durante la riproduzione del testo: " + utteranceId);
                                }
                            });
                        } else {
                            Log.e("TTS", "TextToSpeech non inizializzato.");
                        }
                    }
                });
        });
    }

    private void speakResponse(String response,Integer stato) {

        //Setto l'animazione di uscita
        Animation slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);

        // Recupera il numero di registrazioni vocali precedenti
        SharedPreferences sharedPreferences = getSharedPreferences("RecVocal", Context.MODE_PRIVATE);
        int numregistrazionivocali = sharedPreferences.getInt("numregistrazionivocali", 0);
        int numrispostegiuste = sharedPreferences.getInt("numrispostegiuste", 0);

        System.out.println("Sono nello speakResponse e il numero di registrazioni vocali è:"+numregistrazionivocali + "mentre il numero di risposte giuste è: " + numrispostegiuste);

        //Recupero lo sharedPreferences per l'accesso dalla notifica
        SharedPreferences sharedPref = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        int login_from_notification = sharedPref.getInt("login_from_notification", 0);
        System.out.println("Login from notification: " + login_from_notification);

        //Recupero il giorno del primo accesso e il giorno corrente
        SharedPreferences sharedPrefFirstAccess = getSharedPreferences("FirstAccessPrefs", Context.MODE_PRIVATE);
        int firstdataccess = sharedPrefFirstAccess.getInt("dayfirstaccess", 0);
        LocalDate date = LocalDate.now();
        int giornocorrente = date.getDayOfMonth();


        if (response != null && !response.isEmpty()) {
            // Configura il listener PRIMA di chiamare speak
            String utteranceId = "DialogResponseUtterance";

            texttoSpeechResponse.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.i("TTS", "Riproduzione iniziata");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.i("TTS", "Riproduzione completata");

                    if (numregistrazionivocali >= 0) {
                            runOnUiThread(() -> {
                                Log.i("TTS", "Avvio del riconoscimento vocale...");
                                startSpeechRecognition();
                            });
                    } else {
                            //Nascondo l'avatar del robot
                            animazioni.startAnimation(slideOutBottom);
                            //Quando l'animazione è finita, riattivo gli elementi della Homepage

                            animazioni.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    // La riproduzione è iniziata
                                    Log.i("TTS", "Animazione iniziata");
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    // La riproduzione è finita
                                    Log.i("TTS", "Animazione completata");

                                    navigationmenu.setSelectedItemId(R.id.Home);

                                    animazioni.setVisibility(View.GONE);
                                    CardViewAvatar.setVisibility(View.GONE);

                                    //Riattivo gli elementi della Homepage
                                    DataOdierna.setVisibility(View.VISIBLE);
                                    AllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                    ImmagineAllenamentoGiornaliero.setVisibility(View.VISIBLE);
                                    recyclerviewAllenamentiPatologie.setVisibility(View.VISIBLE);
                                    AllenamentiPatologie.setVisibility(View.VISIBLE);
                                    ConsigliatoPerTe.setVisibility(View.VISIBLE);


                                    //Aspetto 1 secondo prima di passare alla prossima activity
                                    int delayMillis = 1000;
                                    new android.os.Handler().postDelayed(() -> {
                                        //Se l'accesso è stato effettuato dalla notifica, passo alla prossima activity
                                        if (login_from_notification == 1) {
                                            Intent intent = new Intent(getApplicationContext(), AllenamentoGiornalieroActivity.class);
                                            intent.putExtra("email", email);
                                            startActivity(intent);
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                            finish();
                                        } else {
                                            Log.i("Dialog", "L'accesso non è stato effettuato dalla notifica");
                                        }
                                    }, delayMillis);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                    // La riproduzione è ripetuta
                                    Log.i("TTS", "Animazione ripetuta");
                                }
                            });

                        }
                    }

                @Override
                public void onError(String utteranceId) {
                    Log.e("TTS", "Errore nella Riproduzione");
                }
            });


            if (stato == 100)
        {
            //Genero un numero casuale tra 1 e 4

            Random random = new Random();
            int numero = random.nextInt(4) + 1;
            if(numero == 1){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videodialoghiamo1);
                animazioni.setVideoURI(uri);
                animazioni.start();

            }else if(numero == 2){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videodialoghiamo2);
                animazioni.setVideoURI(uri);
                animazioni.start();
            } else if (numero == 3) {
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videodialoghiamo3);
                animazioni.setVideoURI(uri);
                animazioni.start();
            } else {
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videodialoghiamo4);
                animazioni.setVideoURI(uri);
                animazioni.start();
            }

        }else if(stato == 0){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videotuttobene);
                animazioni.setVideoURI(uri);
                animazioni.start();

            } else if (stato == 1){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videovamale);
                animazioni.setVideoURI(uri);
                animazioni.start();

            } else if (stato == 2){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videononcisonoproblemi);
                animazioni.setVideoURI(uri);
                animazioni.start();

            } else if( stato == 3){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videocisonoproblemi);
                animazioni.setVideoURI(uri);
                animazioni.start();
            } else if (stato == 4){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mivoglioallenarept2);
                animazioni.setVideoURI(uri);
                animazioni.start();
            } else if (stato == 5){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.nonmivoglioallenare);
                animazioni.setVideoURI(uri);
                animazioni.start();
            } else if (stato == 6){
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.mivoglioallenare);
                animazioni.setVideoURI(uri);
                animazioni.start();

            } else {
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videocisonoproblemi);
                animazioni.setVideoURI(uri);
                animazioni.start();
            }

            texttoSpeechResponse.speak(response, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parla ora...");
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Il riconoscimento vocale non è supportato su questo dispositivo.", Toast.LENGTH_SHORT).show();
        }
    }


    // Pianifica le notifiche giornaliere con i millisecondi specificati
    @SuppressLint("ScheduleExactAlarm")
    private void scheduleDailyNotification(Context context, int hourOfDay, int minuteOfHour) {
        // Calcola il tempo mancante fino alla notifica
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeInMillis = currentCalendar.getTimeInMillis();

        // Imposta l'orario della notifica
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        targetCalendar.set(Calendar.MINUTE, minuteOfHour);
        targetCalendar.set(Calendar.SECOND, 0);

        // Se l'orario è già passato per oggi, sposta la notifica al giorno successivo
        if (targetCalendar.getTimeInMillis() <= currentTimeInMillis) {
            targetCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        long timeDifference = targetCalendar.getTimeInMillis() - currentTimeInMillis;

        // Pianifica la notifica con il tempo mancante
        System.out.println("Ho riprogrammato la notifica con scheduleNextNotification");
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, currentTimeInMillis + timeDifference, pendingIntent);
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

    public static boolean containsIgnoreCase(Set<String> set, String value) {
        for (String item : set) {
            //Voglio togliere eventuali segni di punteggiatura dal item prima di confrontarlo QUINDI: ,.!?;:
            item = item.replaceAll("[.,!?;:]", "");
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
