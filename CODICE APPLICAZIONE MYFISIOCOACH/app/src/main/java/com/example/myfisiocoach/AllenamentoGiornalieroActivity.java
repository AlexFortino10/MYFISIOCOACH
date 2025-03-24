package com.example.myfisiocoach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;


import BackendServer.ApiServiceAllenamentoGiornaliero;
import BackendServer.ApiServiceGiorniAllenamento;
import BackendServer.ApiServiceResocontoUtente;
import Llama.LlamaService;
import Model.Resoconto;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class AllenamentoGiornalieroActivity extends AppCompatActivity {

    VideoView videoallenamento;

    BottomNavigationView navigationmenu;

    ProgressBar progressBar;

    TextView istruzioni,benefici,titoloistruzioni,titolobenefici;

    private LlamaService llamaService = new LlamaService();// Il servizio che invia il prompt a LLaMA

    Resoconto resoconto = new Resoconto();

    private TextToSpeech textToSpeech;

    VideoView animazioneallenamento;

    CardView CardViewAvatar;

    ApiServiceAllenamentoGiornaliero apiServiceAllenamentoGiornalieroString;

    ApiServiceResocontoUtente apiServiceResocontoUtente;
    ApiServiceGiorniAllenamento apiServiceGiorniAllenamento;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allenamento_giornaliero);


        //Definisco il servizio per intercepire le richieste al server
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        //Definisco il retrofit per l'allenamento giornaliero
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.61.100.135:8080/")// URL del server backend
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServiceAllenamentoGiornalieroString = retrofit.create(ApiServiceAllenamentoGiornaliero.class);
        apiServiceResocontoUtente = retrofit.create(ApiServiceResocontoUtente.class); //Definisco il retrofit per il resoconto dell'utente
        apiServiceGiorniAllenamento = retrofit.create(ApiServiceGiorniAllenamento.class); //Definisco il retrofit per i giorni di allenamento

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

        //Definisco il video per l'animazione dell'avatar e la cardview dell'avatar
        CardViewAvatar = findViewById(R.id.CardViewAvatar);
        animazioneallenamento = findViewById(R.id.animazioneallenamento);

        //Rendo non visibili sia il video che la cardview dell'avatar
        CardViewAvatar.setVisibility(View.GONE);
        animazioneallenamento.setVisibility(View.GONE);


        //creo lo shared login_from_notification
        SharedPreferences sharedPrefLog = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorNotify = sharedPrefLog.edit();
        editorNotify.putInt("login_from_notification", 0);
        editorNotify.apply();

        //Recupero lo sharedPreferences per salvare il giorno corrente e quello per il numero di giorni per attivare il dialogo
        SharedPreferences sharedDialog = getSharedPreferences("DialogPrefs", Context.MODE_PRIVATE);
        int sharedDay = sharedDialog.getInt("day", 0);
        int sharedNumDays = sharedDialog.getInt("numdays", 0);
        LocalDate data = LocalDate.now();

        System.out.println("Giorno condiviso: " + sharedDay + " Numero di giorni condiviso: " + sharedNumDays);

        if (( sharedDay < data.getDayOfMonth() || (sharedDay > data.getDayOfMonth() && data.getDayOfMonth() == 1) ) && sharedNumDays < 4) {
            //Salvo il giorno corrente e incremento il numero di giorni
            SharedPreferences.Editor editorDialog = sharedDialog.edit();
            editorDialog.putInt("day", data.getDayOfMonth());
            editorDialog.putInt("numdays", sharedNumDays + 1);
            editorDialog.apply();
        }

        //recupero il valore dello shared daily_notification_count
        SharedPreferences sharedPref = getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        int training_daily_count = sharedPref.getInt("daily_notification_count", 1);
        if(training_daily_count -1 <= 1){
            training_daily_count = 1;
        } else if (training_daily_count -1 == 2) {
            training_daily_count = 2;

        } else {
            training_daily_count = 3;

        }
        System.out.println("Training daily count: " + training_daily_count);


        // Setto l'animazione di entrata e uscita
        Animation slideInBottom = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        Animation slideOutBottom = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);

        String promptInizio,promptContinua,promptFine;

        //Recupero le frasi per l'audio
        promptInizio="Immaginati questo scenario: sei il mio personal trainer virtuale, sta per iniziare l'allenamento e tu mi devi esattamente dire questa frase: 'Sei pronto???!!! l'allenamento sta per iniziare quindi stai rilàssato, concentrati e dai del tuo meglio per completare l'allenamento.'.Ovviamente la tua risposta deve essere in italiano. devi darmi solo la risposta senza specificarmi che te l'ho detta io.";
        promptContinua="Immaginati questo scenario: sei il mio personal trainer virtuale, devo continuare l'allenamento o e tu mi devi esattamente dire questa frase: 'Sei a metà del tuo allenamento,bravissimo... ora è importante restare concentrato,motivato e terminare l'allenamentoo, sono sicura che ci riesci.'.Ovviamente la tua risposta deve essere in italiano. dammi solo la risposta.";
        promptFine="Immaginati questo scenario: sei il mio personal trainer virtuale, devo continuare l'allenamento o e tu mi devi esattamente dire questa frase: 'Complimenti, hai terminato l'allenamento alla grande... sei stato fortissimo!!!......'.Ovviamente la tua risposta deve essere in italiano. dammi solo la risposta.";

        //Recupero l'email dell'utente
        String email = getIntent().getStringExtra("email");

        //Setto le istruzioni dell'allenamento
        istruzioni = findViewById(R.id.textViewIstruzioni);
        titoloistruzioni = findViewById(R.id.textViewTitoloIstruzioni);

        //Calcolo il giorno corrente
        LocalDate currentDate = LocalDate.now();
        int giorno = currentDate.getDayOfMonth();

        apiServiceAllenamentoGiornalieroString.getIstruzioniAllenamento(giorno, training_daily_count).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful() && response.body() != null) {
                    istruzioni.setText(response.body());
                } else {
                   Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero delle istruzioni");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero delle istruzioni", t);
            }
        });




        //Setto i benefici dell'allenamento
        benefici = findViewById(R.id.textViewBenefici);
        titolobenefici= findViewById(R.id.textViewTitoloBenefici);

        apiServiceAllenamentoGiornalieroString.getBeneficiAllenamento(giorno, training_daily_count).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful() && response.body() != null) {
                    benefici.setText(response.body());
                } else {
                     Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero dei benefici");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero dei benefici", t);
            }
        });


        //Setto il video da visualizzare
        videoallenamento = findViewById(R.id.videoViewAllenamento);

        //Setto il controllo del video del allenamento
        MediaController mediaController = new MediaController(AllenamentoGiornalieroActivity.this);

        apiServiceAllenamentoGiornalieroString.getVideoAllenamento(giorno,training_daily_count).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {

                if (response.isSuccessful() && response.body() != null) {

                    //Setto l'URI del video
                    String Url = response.body();
                    Uri uri = Uri.parse(Url);
                    //Aggiungo il controllo del video
                    videoallenamento.setMediaController(mediaController);
                    mediaController.setAnchorView(videoallenamento);

                    //Setto l'URI del video al videoView
                    videoallenamento.setVideoURI(uri);

                }else {
                    Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero del video");
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero del video", t);
            }
        });


        //Aggiungo il progress bar
        progressBar = findViewById(R.id.progressBar);

        // Ora parte il video dell'allenamento
        //Il progress bar è visibile fino a quando non parte il video
        videoallenamento.setOnPreparedListener(mp -> {
            titoloistruzioni.setVisibility(View.GONE);
            istruzioni.setVisibility(View.GONE);
            titolobenefici.setVisibility(View.GONE);
            benefici.setVisibility(View.GONE);

            //Parte la frase iniziale del avatar
            new Thread(() -> {
                    runOnUiThread(() -> {
                        //Imposto il video di inizio allenamento
                        CardViewAvatar.setVisibility(View.VISIBLE);
                        animazioneallenamento.setVisibility(View.VISIBLE);
                        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoinizioallenamento);
                        animazioneallenamento.setVideoURI(uri);
                        animazioneallenamento.seekTo(1);
                        animazioneallenamento.startAnimation(slideInBottom);
                        slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                animazioneallenamento.start();
                                if (true){
                                    String utteranceId = "InizioDialogo";
                                    animazioneallenamento.start();
                                    textToSpeech.speak("Tutto pronto?!!! L'allenamento sta per iniziare, quindi rilàssati, concentrati e dai del tuo meglio per completare l'allenamento", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                        @Override
                                        public void onStart(String utteranceId) {
                                            System.out.println("Inizio dialogo");
                                        }

                                        @Override
                                        public void onDone(String utteranceId) {
                                            animazioneallenamento.startAnimation(slideOutBottom);
                                            //Quando l'animazione è finita
                                            slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {}

                                                @Override
                                                public void onAnimationEnd(Animation animation) {
                                                    CardViewAvatar.setVisibility(View.GONE);
                                                    animazioneallenamento.setVisibility(View.GONE);
                                                    //Mostro le istruzioni e i benefici
                                                    titoloistruzioni.setVisibility(View.VISIBLE);
                                                    istruzioni.setVisibility(View.VISIBLE);
                                                    titolobenefici.setVisibility(View.VISIBLE);
                                                    benefici.setVisibility(View.VISIBLE);
                                                    //Mostro il video
                                                    progressBar.setVisibility(ProgressBar.GONE);
                                                    videoallenamento.start();
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {}
                                            });
                                        }

                                        @Override
                                        public void onError(String utteranceId) {
                                            System.out.println("Errore dialogo");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                    });
            }).start();


            // A metà del video mostro l'avatar e riproduco l'audio di continuazione
            int videoDuration = mp.getDuration();
            int halfwayPoint = videoDuration / 2;
            videoallenamento.postDelayed(() -> {
                //Mostro l'avatar del robot e avvio il file audio
                videoallenamento.pause();
                titoloistruzioni.setVisibility(View.GONE);
                istruzioni.setVisibility(View.GONE);
                titolobenefici.setVisibility(View.GONE);
                benefici.setVisibility(View.GONE);

                //Parte la frase di continuazione del avatar
                new Thread(() -> {
                        runOnUiThread(() -> {
                            // Mostro l'avatar del robot e avvio il file audio
                            CardViewAvatar.setVisibility(View.VISIBLE);
                            animazioneallenamento.setVisibility(View.VISIBLE);
                            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_continua_allenamento);
                            animazioneallenamento.setVideoURI(uri);
                            animazioneallenamento.seekTo(1);
                            animazioneallenamento.startAnimation(slideInBottom);
                            slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    animazioneallenamento.start();
                                    if (true){
                                        String utteranceId = "ContinuaDialogo";

                                        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                            @Override
                                            public void onStart(String utteranceId) {
                                                System.out.println("Continua dialogo");
                                            }

                                            @Override
                                            public void onDone(String utteranceId) {
                                                animazioneallenamento.startAnimation(slideOutBottom);
                                                //Quando l'animazione è finita
                                                slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {}

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        CardViewAvatar.setVisibility(View.GONE);
                                                        animazioneallenamento.setVisibility(View.GONE);
                                                        //Mostro le istruzioni e i benefici
                                                        titoloistruzioni.setVisibility(View.VISIBLE);
                                                        istruzioni.setVisibility(View.VISIBLE);
                                                        titolobenefici.setVisibility(View.VISIBLE);
                                                        benefici.setVisibility(View.VISIBLE);
                                                        //Riprendo il video
                                                        videoallenamento.start();
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {}
                                                });
                                            }

                                            @Override
                                            public void onError(String utteranceId) {
                                                System.out.println("Errore dialogo");
                                            }
                                        });

                                        textToSpeech.speak("Sei a metà del tuo allenamento,ottimo così... ora è importante mantenere la concentrazione, la motivazione e terminare l'allenamento, sono sicuro che ci riesci", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                                    }
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });

                        });

                }).start();
            }, halfwayPoint);
        });


        //Quando il video finisce Mostro una dialog di complimento
        videoallenamento.setOnCompletionListener(mp -> {

            titoloistruzioni.setVisibility(View.GONE);
            istruzioni.setVisibility(View.GONE);
            titolobenefici.setVisibility(View.GONE);
            benefici.setVisibility(View.GONE);

            //Parte la frase finale del avatar
            new Thread(() -> {
                try {
                    runOnUiThread(() -> {
                        // Mostro l'avatar del robot e avvio il file audio
                        CardViewAvatar.setVisibility(View.VISIBLE);
                        animazioneallenamento.setVisibility(View.VISIBLE);
                        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videofineallenamento);
                        animazioneallenamento.setVideoURI(uri);
                        animazioneallenamento.seekTo(1);
                        animazioneallenamento.startAnimation(slideInBottom);
                        slideInBottom.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {}

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                animazioneallenamento.start();

                                if (true) {
                                    String utteranceId = "FineDialogo";
                                    LayoutInflater inflater = getLayoutInflater();


                                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                        @Override
                                        public void onStart(String utteranceId) {
                                            System.out.println("Fine dialogo");
                                        }

                                        @Override
                                        public void onDone(String utteranceId) {
                                            animazioneallenamento.startAnimation(slideOutBottom);
                                            //Quando l'animazione è finita
                                            slideOutBottom.setAnimationListener(new Animation.AnimationListener() {
                                                @Override
                                                public void onAnimationStart(Animation animation) {
                                                }

                                                @Override
                                                public void onAnimationEnd(Animation animation) {
                                                    CardViewAvatar.setVisibility(View.GONE);
                                                    animazioneallenamento.setVisibility(View.GONE);
                                                    //Mostro le istruzioni e i benefici
                                                    titoloistruzioni.setVisibility(View.VISIBLE);
                                                    istruzioni.setVisibility(View.VISIBLE);
                                                    titolobenefici.setVisibility(View.VISIBLE);
                                                    benefici.setVisibility(View.VISIBLE);
                                                    //Recupero il resoconto dell'utente
                                                    apiServiceResocontoUtente.findMinutiByEmail(email).enqueue(new Callback<Integer>() {
                                                        @Override
                                                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                            if (response.isSuccessful() && response.body() != null) {
                                                                resoconto.setMinuti(response.body());

                                                                apiServiceResocontoUtente.findSecondiByEmail(email).enqueue(new Callback<Integer>() {
                                                                    @Override
                                                                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                        if (response.isSuccessful() && response.body() != null) {
                                                                            resoconto.setSecondi(response.body());


                                                                            apiServiceResocontoUtente.findNumallenamentiByEmail(email).enqueue(new Callback<Integer>() {
                                                                                @Override
                                                                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                                        resoconto.setNumallenamenti(response.body());


                                                                                        apiServiceResocontoUtente.findSerieByEmail(email).enqueue(new Callback<Integer>() {
                                                                                            @Override
                                                                                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                                                if (response.isSuccessful() && response.body() != null) {
                                                                                                    resoconto.setSerie(response.body());


                                                                                                    apiServiceResocontoUtente.findRecordpersonaleByEmail(email).enqueue(new Callback<Integer>() {
                                                                                                        @Override
                                                                                                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                                                            if (response.isSuccessful() && response.body() != null) {
                                                                                                                resoconto.setRecordpersonale(response.body());

                                                                                                                LocalDate currentDate = LocalDate.now();
                                                                                                                Integer giornoallenamento = currentDate.getDayOfMonth();
                                                                                                                Integer meseallenamento = currentDate.getMonthValue();
                                                                                                                final Integer[] giornoultimoallenamento = {0};
                                                                                                                final Integer[] giornoultimoallenamentomeseprecedente = {0};

                                                                                                                apiServiceGiorniAllenamento.getUltimoGiornoAllenamento(email, meseallenamento).enqueue(new Callback<Integer>() {
                                                                                                                    @Override
                                                                                                                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                                                                        if (response.isSuccessful() && response.body() != null) {

                                                                                                                            giornoultimoallenamento[0] = response.body();
                                                                                                                            System.out.println("Sono nel OnResponse del ultimo giorno di allenamento, dopo dovresti vedere il mese precedente");

                                                                                                                            System.out.println("Giorno ultimo allenamento:" + giornoultimoallenamento[0]);

                                                                                                                            Integer meseprecedente = meseallenamento - 1;
                                                                                                                            if (meseprecedente == 0) {
                                                                                                                                meseprecedente = 12;
                                                                                                                            }

                                                                                                                            System.out.println("Mese precedente: " + meseprecedente);


                                                                                                                            Integer finalMeseprecedente = meseprecedente;
                                                                                                                            System.out.println("Final Mese precedente: " + finalMeseprecedente);
                                                                                                                            apiServiceGiorniAllenamento.getUltimoGiornoAllenamento(email, finalMeseprecedente).enqueue(new Callback<Integer>() {
                                                                                                                                @Override
                                                                                                                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                                                                                    if (response.isSuccessful() && response.body() != null) {

                                                                                                                                        giornoultimoallenamentomeseprecedente[0] = response.body();
                                                                                                                                        System.out.println("Giorno ultimo allenamento mese precedente: " + giornoultimoallenamentomeseprecedente[0]);


                                                                                                                                        //Recupero la durata del video in minuti
                                                                                                                                        int durata = videoallenamento.getDuration();
                                                                                                                                        int secondivideo = durata / 1000;
                                                                                                                                        int durataminuti = secondivideo / 60;
                                                                                                                                        int duratasecondi = secondivideo % 60;

                                                                                                                                        //Aggiorno dati del resoconto
                                                                                                                                        int minuti = resoconto.getMinuti();
                                                                                                                                        int secondi = resoconto.getSecondi();
                                                                                                                                        int numallenamenti = resoconto.getNumallenamenti();
                                                                                                                                        int serie = resoconto.getSerie();
                                                                                                                                        int recordpersonale = resoconto.getRecordpersonale();

                                                                                                                                        minuti = minuti + durataminuti;
                                                                                                                                        secondi = secondi + duratasecondi;
                                                                                                                                        if (secondi >= 60) {
                                                                                                                                            minuti = minuti + 1;
                                                                                                                                            secondi = secondi - 60;
                                                                                                                                        }
                                                                                                                                        numallenamenti = numallenamenti + 1;

                                                                                                                                        System.out.println("Serie:" + serie);

                                                                                                                                        //Controllo se l'ultimo allenamento è stato fatto il giorno precedente
                                                                                                                                        System.out.println("Giorno ultimo allenamento: " + giornoultimoallenamento[0]);
                                                                                                                                        System.out.println("Giorno allenamento: " + giornoallenamento);
                                                                                                                                        System.out.println("Mese precedente: " + finalMeseprecedente);
                                                                                                                                        YearMonth yearMonthObject = YearMonth.of(currentDate.getYear(), finalMeseprecedente);
                                                                                                                                        if ((giornoultimoallenamento[0] + 1 == giornoallenamento) || (giornoallenamento == 1 && giornoultimoallenamentomeseprecedente[0] == yearMonthObject.lengthOfMonth())) {
                                                                                                                                            serie = serie + 1;
                                                                                                                                        } else if (giornoultimoallenamento[0] + 1 < giornoallenamento) {
                                                                                                                                            System.out.println("Azzero serie");
                                                                                                                                            serie = 1;
                                                                                                                                        }

                                                                                                                                        //Controllo se il record personale è stato battuto
                                                                                                                                        if (serie > recordpersonale) {
                                                                                                                                            recordpersonale = serie;
                                                                                                                                        }

                                                                                                                                        apiServiceResocontoUtente.updateResoconto(email, minuti, secondi, numallenamenti, serie, recordpersonale).enqueue(new Callback<Boolean>() {
                                                                                                                                            @Override
                                                                                                                                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                                if (response.isSuccessful() && response.body() != null) {
                                                                                                                                                    System.out.println("Resoconto aggiornato");

                                                                                                                                                    //Aggiorno il giorno dell'allenamento
                                                                                                                                                    apiServiceGiorniAllenamento.inserisciGiornoAllenamento(email, giornoallenamento, finalMeseprecedente).enqueue(new Callback<Boolean>() {
                                                                                                                                                        @Override
                                                                                                                                                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                                            if (response.isSuccessful() && response.body() != null && response.body()) {
                                                                                                                                                                //Mostro un messaggio di congratulazioni
                                                                                                                                                                System.out.println("Giorno dell'allenamento aggiornato");
                                                                                                                                                            } else {
                                                                                                                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento");
                                                                                                                                                            }
                                                                                                                                                        }

                                                                                                                                                        @Override
                                                                                                                                                        public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento", t);
                                                                                                                                                        }
                                                                                                                                                    });

                                                                                                                                                } else {
                                                                                                                                                    Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto");
                                                                                                                                                }
                                                                                                                                            }

                                                                                                                                            @Override
                                                                                                                                            public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto", t);
                                                                                                                                            }
                                                                                                                                        });


                                                                                                                                        AlertDialog.Builder builder = new AlertDialog.Builder(AllenamentoGiornalieroActivity.this);
                                                                                                                                        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                                                                                                        builder.setView(dialogView);
                                                                                                                                        TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                                                                                                        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                                                                                                        Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                                                                                                        Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                                                                                                        AlertDialog alertDialog = builder.create();
                                                                                                                                        alertDialog.show();
                                                                                                                                        alertDialog.setCancelable(false);

                                                                                                                                        title.setText("Congratulazioni");
                                                                                                                                        title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.trophy__2_, 0);
                                                                                                                                        title.setCompoundDrawablePadding(10);
                                                                                                                                        message.setText("Hai completato l'allenamento di oggi! Vuoi visualizzare il resoconto o tornare alla pagina principale?");
                                                                                                                                        positiveButton.setText("Visualizza resoconto");
                                                                                                                                        negativeButton.setText("Pagina principale");
                                                                                                                                        positiveButton.setOnClickListener(v1 -> {
                                                                                                                                            //Vado alla pagina del resoconto
                                                                                                                                            //Stoppo il video e il media controller
                                                                                                                                            videoallenamento.stopPlayback();
                                                                                                                                            mediaController.hide();
                                                                                                                                            alertDialog.dismiss();
                                                                                                                                            Intent intent = new Intent(getApplicationContext(), ResocontoActivity.class);
                                                                                                                                            intent.putExtra("email", email);
                                                                                                                                            startActivity(intent);
                                                                                                                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                                                                                                            finish();
                                                                                                                                        });
                                                                                                                                        negativeButton.setOnClickListener(v1 -> {
                                                                                                                                            //Vado alla home page

                                                                                                                                            //Stoppo il video e il media controller
                                                                                                                                            videoallenamento.stopPlayback();
                                                                                                                                            mediaController.hide();
                                                                                                                                            alertDialog.dismiss();
                                                                                                                                            Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                                                                                                                            intent.putExtra("email", email);
                                                                                                                                            startActivity(intent);
                                                                                                                                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                                                                                            finish();
                                                                                                                                        });

                                                                                                                                    }
                                                                                                                                }

                                                                                                                                @Override
                                                                                                                                public void onFailure(Call<Integer> call, Throwable t) {
                                                                                                                                    giornoultimoallenamentomeseprecedente[0] = 0;
                                                                                                                                    System.out.println("Giorno ultimo allenamento mese precedente: " + giornoultimoallenamentomeseprecedente[0]);

                                                                                                                                    //Recupero la durata del video in minuti
                                                                                                                                    int durata = videoallenamento.getDuration();
                                                                                                                                    int secondivideo = durata / 1000;
                                                                                                                                    int durataminuti = secondivideo / 60;
                                                                                                                                    int duratasecondi = secondivideo % 60;

                                                                                                                                    //Aggiorno dati del resoconto
                                                                                                                                    int minuti = resoconto.getMinuti();
                                                                                                                                    int secondi = resoconto.getSecondi();
                                                                                                                                    int numallenamenti = resoconto.getNumallenamenti();
                                                                                                                                    int serie = resoconto.getSerie();
                                                                                                                                    int recordpersonale = resoconto.getRecordpersonale();

                                                                                                                                    minuti = minuti + durataminuti;
                                                                                                                                    secondi = secondi + duratasecondi;
                                                                                                                                    if (secondi >= 60) {
                                                                                                                                        minuti = minuti + 1;
                                                                                                                                        secondi = secondi - 60;
                                                                                                                                    }
                                                                                                                                    numallenamenti = numallenamenti + 1;

                                                                                                                                    System.out.println("Serie:" + serie);

                                                                                                                                    //Controllo se l'ultimo allenamento è stato fatto il giorno precedente
                                                                                                                                    System.out.println("Giorno ultimo allenamento: " + giornoultimoallenamento[0]);
                                                                                                                                    System.out.println("Giorno allenamento: " + giornoallenamento);
                                                                                                                                    System.out.println("Mese precedente: " + finalMeseprecedente.toString());
                                                                                                                                    YearMonth yearMonthObject = YearMonth.of(currentDate.getYear(), finalMeseprecedente);
                                                                                                                                    if ((giornoultimoallenamento[0] + 1 == giornoallenamento) || (giornoallenamento == 1 && giornoultimoallenamentomeseprecedente[0] == yearMonthObject.lengthOfMonth())) {
                                                                                                                                        serie = serie + 1;
                                                                                                                                    } else if (giornoultimoallenamento[0] + 1 < giornoallenamento) {
                                                                                                                                        System.out.println("Azzero serie");
                                                                                                                                        serie = 1;
                                                                                                                                    }

                                                                                                                                    //Controllo se il record personale è stato battuto
                                                                                                                                    if (serie > recordpersonale) {
                                                                                                                                        recordpersonale = serie;
                                                                                                                                    }

                                                                                                                                    apiServiceResocontoUtente.updateResoconto(email, minuti, secondi, numallenamenti, serie, recordpersonale).enqueue(new Callback<Boolean>() {
                                                                                                                                        @Override
                                                                                                                                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                            if (response.isSuccessful() && response.body() != null) {
                                                                                                                                                System.out.println("Resoconto aggiornato");

                                                                                                                                                //Aggiorno il giorno dell'allenamento
                                                                                                                                                apiServiceGiorniAllenamento.inserisciGiornoAllenamento(email, giornoallenamento, meseallenamento).enqueue(new Callback<Boolean>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                                        if (response.isSuccessful() && response.body() != null && response.body()) {
                                                                                                                                                            //Mostro un messaggio di congratulazioni
                                                                                                                                                            System.out.println("Giorno dell'allenamento aggiornato");
                                                                                                                                                        } else {
                                                                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento");
                                                                                                                                                        }
                                                                                                                                                    }

                                                                                                                                                    @Override
                                                                                                                                                    public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                                        Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento", t);
                                                                                                                                                    }
                                                                                                                                                });

                                                                                                                                            } else {
                                                                                                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto");
                                                                                                                                            }
                                                                                                                                        }

                                                                                                                                        @Override
                                                                                                                                        public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto", t);
                                                                                                                                        }
                                                                                                                                    });


                                                                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(AllenamentoGiornalieroActivity.this);
                                                                                                                                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                                                                                                    builder.setView(dialogView);
                                                                                                                                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                                                                                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                                                                                                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                                                                                                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                                                                                                    AlertDialog alertDialog = builder.create();
                                                                                                                                    alertDialog.show();
                                                                                                                                    alertDialog.setCancelable(false);

                                                                                                                                    title.setText("Congratulazioni");
                                                                                                                                    title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.trophy__2_, 0);
                                                                                                                                    title.setCompoundDrawablePadding(10);
                                                                                                                                    message.setText("Hai completato l'allenamento di oggi! Vuoi visualizzare il resoconto o tornare alla pagina principale?");
                                                                                                                                    positiveButton.setText("Visualizza resoconto");
                                                                                                                                    negativeButton.setText("Pagina principale");
                                                                                                                                    positiveButton.setOnClickListener(v1 -> {
                                                                                                                                        //Vado alla pagina del resoconto
                                                                                                                                        //Stoppo il video e il media controller
                                                                                                                                        videoallenamento.stopPlayback();
                                                                                                                                        mediaController.hide();
                                                                                                                                        alertDialog.dismiss();
                                                                                                                                        Intent intent = new Intent(getApplicationContext(), ResocontoActivity.class);
                                                                                                                                        intent.putExtra("email", email);
                                                                                                                                        startActivity(intent);
                                                                                                                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                                                                                                        finish();
                                                                                                                                    });
                                                                                                                                    negativeButton.setOnClickListener(v1 -> {
                                                                                                                                        //Vado alla home page

                                                                                                                                        //Stoppo il video e il media controller
                                                                                                                                        videoallenamento.stopPlayback();
                                                                                                                                        mediaController.hide();
                                                                                                                                        alertDialog.dismiss();
                                                                                                                                        Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                                                                                                                        intent.putExtra("email", email);
                                                                                                                                        startActivity(intent);
                                                                                                                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                                                                                        finish();
                                                                                                                                    });

                                                                                                                                }

                                                                                                                            });


                                                                                                                        }
                                                                                                                    }

                                                                                                                    @Override
                                                                                                                    public void onFailure(Call<Integer> call, Throwable t) {
                                                                                                                        giornoultimoallenamento[0] = 0;

                                                                                                                        System.out.println("Giorno ultimo allenamento:" + giornoultimoallenamento[0]);


                                                                                                                        Integer meseprecedente = meseallenamento - 1;
                                                                                                                        if (meseprecedente == 0) {
                                                                                                                            meseprecedente = 12;
                                                                                                                        }
                                                                                                                        Integer finalMeseprecedente = meseprecedente;
                                                                                                                        apiServiceGiorniAllenamento.getUltimoGiornoAllenamento(email, finalMeseprecedente).enqueue(new Callback<Integer>() {
                                                                                                                            @Override
                                                                                                                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                                                                                                                if (response.isSuccessful() && response.body() != null) {

                                                                                                                                    giornoultimoallenamentomeseprecedente[0] = response.body();
                                                                                                                                    System.out.println("Giorno ultimo allenamento mese precedente: " + giornoultimoallenamentomeseprecedente[0]);

                                                                                                                                    //Recupero la durata del video in minuti
                                                                                                                                    int durata = videoallenamento.getDuration();
                                                                                                                                    int secondivideo = durata / 1000;
                                                                                                                                    int durataminuti = secondivideo / 60;
                                                                                                                                    int duratasecondi = secondivideo % 60;

                                                                                                                                    //Aggiorno dati del resoconto
                                                                                                                                    int minuti = resoconto.getMinuti();
                                                                                                                                    int secondi = resoconto.getSecondi();
                                                                                                                                    int numallenamenti = resoconto.getNumallenamenti();
                                                                                                                                    int serie = resoconto.getSerie();
                                                                                                                                    int recordpersonale = resoconto.getRecordpersonale();

                                                                                                                                    minuti = minuti + durataminuti;
                                                                                                                                    secondi = secondi + duratasecondi;
                                                                                                                                    if (secondi >= 60) {
                                                                                                                                        minuti = minuti + 1;
                                                                                                                                        secondi = secondi - 60;
                                                                                                                                    }
                                                                                                                                    numallenamenti = numallenamenti + 1;

                                                                                                                                    System.out.println("Serie:" + serie);

                                                                                                                                    //Controllo se l'ultimo allenamento è stato fatto il giorno precedente
                                                                                                                                    System.out.println("Giorno ultimo allenamento: " + giornoultimoallenamento[0]);
                                                                                                                                    System.out.println("Giorno allenamento: " + giornoallenamento);
                                                                                                                                    YearMonth yearMonthObject = YearMonth.of(currentDate.getYear(), finalMeseprecedente);
                                                                                                                                    if ((giornoultimoallenamento[0] + 1 == giornoallenamento) || (giornoallenamento == 1 && giornoultimoallenamentomeseprecedente[0] == yearMonthObject.lengthOfMonth())) {
                                                                                                                                        serie = serie + 1;
                                                                                                                                    } else if (giornoultimoallenamento[0] + 1 < giornoallenamento) {
                                                                                                                                        System.out.println("Azzero serie");
                                                                                                                                        serie = 1;
                                                                                                                                    }

                                                                                                                                    //Controllo se il record personale è stato battuto
                                                                                                                                    if (serie > recordpersonale) {
                                                                                                                                        recordpersonale = serie;
                                                                                                                                    }

                                                                                                                                    apiServiceResocontoUtente.updateResoconto(email, minuti, secondi, numallenamenti, serie, recordpersonale).enqueue(new Callback<Boolean>() {
                                                                                                                                        @Override
                                                                                                                                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                            if (response.isSuccessful() && response.body() != null) {
                                                                                                                                                System.out.println("Resoconto aggiornato");

                                                                                                                                                //Aggiorno il giorno dell'allenamento
                                                                                                                                                apiServiceGiorniAllenamento.inserisciGiornoAllenamento(email, giornoallenamento, meseallenamento).enqueue(new Callback<Boolean>() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                                        if (response.isSuccessful() && response.body() != null && response.body()) {
                                                                                                                                                            //Mostro un messaggio di congratulazioni
                                                                                                                                                            System.out.println("Giorno dell'allenamento aggiornato");
                                                                                                                                                        } else {
                                                                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento");
                                                                                                                                                        }
                                                                                                                                                    }

                                                                                                                                                    @Override
                                                                                                                                                    public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                                        Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento", t);
                                                                                                                                                    }
                                                                                                                                                });

                                                                                                                                            } else {
                                                                                                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto");
                                                                                                                                            }
                                                                                                                                        }

                                                                                                                                        @Override
                                                                                                                                        public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto", t);
                                                                                                                                        }
                                                                                                                                    });


                                                                                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(AllenamentoGiornalieroActivity.this);
                                                                                                                                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                                                                                                    builder.setView(dialogView);
                                                                                                                                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                                                                                                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                                                                                                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                                                                                                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                                                                                                    AlertDialog alertDialog = builder.create();
                                                                                                                                    alertDialog.show();
                                                                                                                                    alertDialog.setCancelable(false);

                                                                                                                                    title.setText("Congratulazioni");
                                                                                                                                    title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.trophy__2_, 0);
                                                                                                                                    title.setCompoundDrawablePadding(10);
                                                                                                                                    message.setText("Hai completato l'allenamento di oggi! Vuoi visualizzare il resoconto o tornare alla pagina principale?");
                                                                                                                                    positiveButton.setText("Visualizza resoconto");
                                                                                                                                    negativeButton.setText("Pagina principale");
                                                                                                                                    positiveButton.setOnClickListener(v1 -> {
                                                                                                                                        //Vado alla pagina del resoconto
                                                                                                                                        //Stoppo il video e il media controller
                                                                                                                                        videoallenamento.stopPlayback();
                                                                                                                                        mediaController.hide();
                                                                                                                                        alertDialog.dismiss();
                                                                                                                                        Intent intent = new Intent(getApplicationContext(), ResocontoActivity.class);
                                                                                                                                        intent.putExtra("email", email);
                                                                                                                                        startActivity(intent);
                                                                                                                                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                                                                                                        finish();
                                                                                                                                    });
                                                                                                                                    negativeButton.setOnClickListener(v1 -> {
                                                                                                                                        //Vado alla home page

                                                                                                                                        //Stoppo il video e il media controller
                                                                                                                                        videoallenamento.stopPlayback();
                                                                                                                                        mediaController.hide();
                                                                                                                                        alertDialog.dismiss();
                                                                                                                                        Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                                                                                                                        intent.putExtra("email", email);
                                                                                                                                        startActivity(intent);
                                                                                                                                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                                                                                        finish();
                                                                                                                                    });

                                                                                                                                }
                                                                                                                            }

                                                                                                                            @Override
                                                                                                                            public void onFailure(Call<Integer> call, Throwable t) {
                                                                                                                                giornoultimoallenamentomeseprecedente[0] = 0;

                                                                                                                                System.out.println("Giorno ultimo allenamento mese precedente: " + giornoultimoallenamentomeseprecedente[0]);


                                                                                                                                //Recupero la durata del video in minuti
                                                                                                                                int durata = videoallenamento.getDuration();
                                                                                                                                int secondivideo = durata / 1000;
                                                                                                                                int durataminuti = secondivideo / 60;
                                                                                                                                int duratasecondi = secondivideo % 60;

                                                                                                                                //Aggiorno dati del resoconto
                                                                                                                                int minuti = resoconto.getMinuti();
                                                                                                                                int secondi = resoconto.getSecondi();
                                                                                                                                int numallenamenti = resoconto.getNumallenamenti();
                                                                                                                                int serie = resoconto.getSerie();
                                                                                                                                int recordpersonale = resoconto.getRecordpersonale();

                                                                                                                                minuti = minuti + durataminuti;
                                                                                                                                secondi = secondi + duratasecondi;
                                                                                                                                if (secondi >= 60) {
                                                                                                                                    minuti = minuti + 1;
                                                                                                                                    secondi = secondi - 60;
                                                                                                                                }
                                                                                                                                numallenamenti = numallenamenti + 1;

                                                                                                                                System.out.println("Serie:" + serie);

                                                                                                                                //Controllo se l'ultimo allenamento è stato fatto il giorno precedente
                                                                                                                                System.out.println("Giorno ultimo allenamento: " + giornoultimoallenamento[0]);
                                                                                                                                System.out.println("Giorno allenamento: " + giornoallenamento);
                                                                                                                                YearMonth yearMonthObject = YearMonth.of(currentDate.getYear(), finalMeseprecedente);
                                                                                                                                if ((giornoultimoallenamento[0] + 1 == giornoallenamento) || (giornoallenamento == 1 && giornoultimoallenamentomeseprecedente[0] == yearMonthObject.lengthOfMonth())) {
                                                                                                                                    serie = serie + 1;
                                                                                                                                } else if (giornoultimoallenamento[0] + 1 < giornoallenamento) {
                                                                                                                                    System.out.println("Azzero serie");
                                                                                                                                    serie = 1;
                                                                                                                                }

                                                                                                                                //Controllo se il record personale è stato battuto
                                                                                                                                if (serie > recordpersonale) {
                                                                                                                                    recordpersonale = serie;
                                                                                                                                }

                                                                                                                                apiServiceResocontoUtente.updateResoconto(email, minuti, secondi, numallenamenti, serie, recordpersonale).enqueue(new Callback<Boolean>() {
                                                                                                                                    @Override
                                                                                                                                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                        if (response.isSuccessful() && response.body() != null) {
                                                                                                                                            System.out.println("Resoconto aggiornato");

                                                                                                                                            //Aggiorno il giorno dell'allenamento
                                                                                                                                            apiServiceGiorniAllenamento.inserisciGiornoAllenamento(email, giornoallenamento, meseallenamento).enqueue(new Callback<Boolean>() {
                                                                                                                                                @Override
                                                                                                                                                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                                                                                                                                    if (response.isSuccessful() && response.body() != null && response.body()) {
                                                                                                                                                        //Mostro un messaggio di congratulazioni
                                                                                                                                                        System.out.println("Giorno dell'allenamento aggiornato");
                                                                                                                                                    } else {
                                                                                                                                                        Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento");
                                                                                                                                                    }
                                                                                                                                                }

                                                                                                                                                @Override
                                                                                                                                                public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                                    Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del giorno dell'allenamento", t);
                                                                                                                                                }
                                                                                                                                            });

                                                                                                                                        } else {
                                                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto");
                                                                                                                                        }
                                                                                                                                    }

                                                                                                                                    @Override
                                                                                                                                    public void onFailure(Call<Boolean> call, Throwable t) {
                                                                                                                                        Log.e("AllenamentoGiornalieroActivity", "Errore nell'aggiornamento del resoconto", t);
                                                                                                                                    }
                                                                                                                                });


                                                                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(AllenamentoGiornalieroActivity.this);
                                                                                                                                View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                                                                                                                                builder.setView(dialogView);
                                                                                                                                TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                                                                                                                                @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                                                                                                                                Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                                                                                                                                Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                                                                                                                                AlertDialog alertDialog = builder.create();
                                                                                                                                alertDialog.show();
                                                                                                                                alertDialog.setCancelable(false);

                                                                                                                                title.setText("Congratulazioni");
                                                                                                                                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.trophy__2_, 0);
                                                                                                                                title.setCompoundDrawablePadding(10);
                                                                                                                                message.setText("Hai completato l'allenamento di oggi! Vuoi visualizzare il resoconto o tornare alla pagina principale?");
                                                                                                                                positiveButton.setText("Visualizza resoconto");
                                                                                                                                negativeButton.setText("Pagina principale");
                                                                                                                                positiveButton.setOnClickListener(v1 -> {
                                                                                                                                    //Vado alla pagina del resoconto
                                                                                                                                    //Stoppo il video e il media controller
                                                                                                                                    videoallenamento.stopPlayback();
                                                                                                                                    mediaController.hide();
                                                                                                                                    alertDialog.dismiss();
                                                                                                                                    Intent intent = new Intent(getApplicationContext(), ResocontoActivity.class);
                                                                                                                                    intent.putExtra("email", email);
                                                                                                                                    startActivity(intent);
                                                                                                                                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                                                                                                                    finish();
                                                                                                                                });
                                                                                                                                negativeButton.setOnClickListener(v1 -> {
                                                                                                                                    //Vado alla home page

                                                                                                                                    //Stoppo il video e il media controller
                                                                                                                                    videoallenamento.stopPlayback();
                                                                                                                                    mediaController.hide();
                                                                                                                                    alertDialog.dismiss();
                                                                                                                                    Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                                                                                                                    intent.putExtra("email", email);
                                                                                                                                    startActivity(intent);
                                                                                                                                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                                                                                                                    finish();
                                                                                                                                });

                                                                                                                            }

                                                                                                                        });

                                                                                                                    }
                                                                                                                });

                                                                                                            } else {
                                                                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero del record personale");
                                                                                                            }
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onFailure(Call<Integer> call, Throwable t) {
                                                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero del record personale", t);
                                                                                                        }
                                                                                                    });

                                                                                                } else {
                                                                                                    Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero delle serie");
                                                                                                }
                                                                                            }

                                                                                            @Override
                                                                                            public void onFailure(Call<Integer> call, Throwable t) {
                                                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero delle serie", t);
                                                                                            }
                                                                                        });
                                                                                    } else {
                                                                                        Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero del numero di allenamenti");
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onFailure(Call<Integer> call, Throwable t) {
                                                                                    Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero del numero di allenamenti", t);
                                                                                }
                                                                            });

                                                                        } else {
                                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero dei secondi");
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onFailure(Call<Integer> call, Throwable t) {
                                                                        Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero dei secondi", t);
                                                                    }
                                                                });

                                                            } else {
                                                                Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero dei minuti");
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<Integer> call, Throwable t) {
                                                            Log.e("AllenamentoGiornalieroActivity", "Errore nel recupero dei minuti", t);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onAnimationRepeat(Animation animation) {
                                                }
                                            });
                                        }

                                        @Override
                                        public void onError(String utteranceId) {
                                            System.out.println("Errore nella pronuncia del testo");

                                        }
                                    });
                                    textToSpeech.speak("Complimenti, hai terminato l'allenamento alla grande... sei una forza della natura!!!......", TextToSpeech.QUEUE_FLUSH, null, utteranceId);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {}
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        // Setto il menu di navigazione
        navigationmenu = findViewById(R.id.bottomNavigationAllenamentoGiornaliero);

        // Disattiva la selezione automatica
        navigationmenu.getMenu().setGroupCheckable(0, true, false);

       // Rimuove la selezione dell'elemento di default
        navigationmenu.getMenu().findItem(R.id.Home).setChecked(false);

        // Setto il listener per il menu di navigazione spostandomi sulle rispettive activity con una transizione di schermata
        navigationmenu.setOnNavigationItemSelectedListener(item -> {
            if (R.id.Home == item.getItemId()) {
                Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return true;
            } else if (R.id.Resoconto == item.getItemId()) {
                Intent intent = new Intent(getApplicationContext(), ResocontoActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
                finish();
                return true;

            } else if (R.id.Impostazioni == item.getItemId()) {
                // Invio l'email dell'utente alla ImpostazioniActivity
                Intent intent = new Intent(getApplicationContext(), ImpostazioniActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_out_right, R.anim.slide_in_left);
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

            }
            return false;
        });


    }
}