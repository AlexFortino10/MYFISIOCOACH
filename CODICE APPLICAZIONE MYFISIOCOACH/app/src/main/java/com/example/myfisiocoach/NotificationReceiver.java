package com.example.myfisiocoach;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.util.Log;  // Import necessario per i log
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.app.NotificationChannel;
import android.os.Build;
import android.widget.Toast;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import BackendServer.ApiServiceAllenamentoGiornaliero;
import BackendServer.ApiServiceUtente;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
public class NotificationReceiver extends BroadcastReceiver {
    MediaPlayer mediaPlayer;

    int Oravincolorimando,Minutivincolorimando;

    int HourProgNotifyNextDay,MinutesProgNotifyNextDay;
    ApiServiceUtente apiServiceUtente;
    ApiServiceAllenamentoGiornaliero apiServiceAllenamentoGiornaliero;

    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("NotificationReceiver", "Alarm Triggered, sending notification");

        //Definisco il servizio per intercepire le richieste al server
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

        apiServiceUtente = retrofit.create(ApiServiceUtente.class);
        apiServiceAllenamentoGiornaliero = retrofit.create(ApiServiceAllenamentoGiornaliero.class);

        // Crea il canale di notifica
        createNotificationChannel(context);

        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Imposta un valore extra per sapere che è stato avviato da una notifica
        loginIntent.putExtra("from_notification", true);

        final PendingIntent[] pendingIntent = {PendingIntent.getActivity(context, 0, loginIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE)};



        // Crea l'azione di snooze
        Intent snoozeIntent = new Intent(context, NotificationReceiver.class);
        snoozeIntent.setAction("ACTION_SNOOZE");
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // Ottieni SharedPreferences per gestire il contatore
        SharedPreferences sharedPreferences = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        final int[] snoozeCount = new int[1]; // Contatore per il numero di rinvii
        int dailyNotificationCount = sharedPreferences.getInt("daily_notification_count", 1); // Contatore delle notifiche giornaliere
        int notifysnooze=sharedPreferences.getInt("notify_snooze",0);
        final int[] timenextnotify = {sharedPreferences.getInt("time_next_notify", 0)};
        final int[] isactionsnooze = new int[1];


        //Recupero lo shared per ottenere l'email dell'utente che ha effettuato il l'ultimo accesso
        SharedPreferences sharedPreferencesEmail = context.getSharedPreferences("EmailPrefs", Context.MODE_PRIVATE);
        String email = sharedPreferencesEmail.getString("email", null);


        //Setto il media player per il suono della notifica in base al contatore delle notifiche giornaliere
        if(dailyNotificationCount == 1){
            mediaPlayer = MediaPlayer.create(context, R.raw.frasenotificaprimoallenamento);
        } else if(dailyNotificationCount == 2){
            mediaPlayer = MediaPlayer.create(context, R.raw.frasenotificasecondoallenamento);
        } else if(dailyNotificationCount == 3){
            mediaPlayer = MediaPlayer.create(context, R.raw.frasenotificaterzoallenamento);
        }

        //Recupero dallo sharedpreferences gli orari e i minuti degli allenamenti.
        //Creo uno shared preference per salvare le ore degli allenamenti
        SharedPreferences sharedPref = context.getSharedPreferences("Allenamenti", Context.MODE_PRIVATE);
        //Recuper ore e minuti degli allenamenti dallo shared preference
        int oraPrimoAllenamento = sharedPref.getInt("OraPrimoAllenamento", 9);
        int minutiPrimoAllenamento = sharedPref.getInt("MinutiPrimoAllenamento", 0);
        int oraSecondoAllenamento = sharedPref.getInt("OraSecondoAllenamento", 13);
        int minutiSecondoAllenamento = sharedPref.getInt("MinutiSecondoAllenamento", 0);
        int oraTerzoAllenamento = sharedPref.getInt("OraTerzoAllenamento", 17);
        int minutiTerzoAllenamento = sharedPref.getInt("MinutiTerzoAllenamento", 0);


        // Controlla se si tratta di un'azione di snooze
        if ("ACTION_SNOOZE".equals(intent.getAction())) {

            System.out.println("E' un'azione di snooze");
            Log.d("NotificationReceiver", "Alarm snoozed for 30 minutes");

            int hourOfDay = 0, minuteOfHour = 0;

            if(dailyNotificationCount == 2){

                System.out.println("Il contatore delle notifiche giornaliere è: "+ dailyNotificationCount);

                int finalDailyNotificationCount = dailyNotificationCount;
                apiServiceAllenamentoGiornaliero.getOraAllenamentoGiornaliero(LocalDate.now().getDayOfMonth(),2).enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if(response.isSuccessful() && response.body() != null){
                            Integer hourOfDay = oraSecondoAllenamento;

                            apiServiceAllenamentoGiornaliero.getMinutiAllenamento(LocalDate.now().getDayOfMonth(),2).enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if(response.isSuccessful() && response.body() != null){
                                        Integer minuteOfHour = minutiSecondoAllenamento;


                                        //Recuper il tempo di quando dovrebbe arrivare la notifica di rimando

                                        Oravincolorimando = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                        Minutivincolorimando = Calendar.getInstance().get(Calendar.MINUTE) + 1;
                                        System.out.println("L'orario del vincolo è: "+Oravincolorimando+":"+Minutivincolorimando);
                                        System.out.println("L'orario del prossimo allenamento è: "+hourOfDay+":"+minuteOfHour);

                                        if ((Oravincolorimando>= hourOfDay && Minutivincolorimando >= minuteOfHour) || (minuteOfHour == 0 && Minutivincolorimando==0 && Oravincolorimando +1 >= hourOfDay)){
                                            //Mando una notifica istantanea dicendo che il prossimo allenamento inzierà a breve e che bisogna completare questo.
                                            // Carica l'immagine dalle risorse come Bitmap
                                            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_2024_10_19_20_18_47);

                                            // Ridimensiona l'immagine alla dimensione desiderata, ad esempio 192x192 per XXXHDPI
                                            Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon, 200, 200, false);

                                            if (finalDailyNotificationCount == 2){
                                                MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.rimandobloccatoprimoallenamento);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                        .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                        .setLargeIcon(resizedLargeIcon)
                                                        .setContentTitle("ACCEDI ORA!!!")
                                                        .setContentText("clicca per accedere")
                                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentIntent(pendingIntent[0])
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Attenzione! Il secondo allenamento giornaliero sta per iniziare. Per ottenere risultati ottimali, è consigliabile completare il primo allenamento. Sei pronto a dare il massimo?"))
                                                        .setAutoCancel(true)
                                                        .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                if (notificationManager != null) {
                                                    notificationManager.notify(1, builder.build());
                                                    mediaPlayersnoze.start();
                                                    Log.d("NotificationReceiver", "Notifica inviata con successo");
                                                } else {
                                                    Log.e("NotificationReceiver", "NotificationManager è null!");
                                                }
                                            } else if (finalDailyNotificationCount == 3) {


                                                MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.rimandobloccatosecondoallenamento);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                        .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                        .setLargeIcon(resizedLargeIcon)
                                                        .setContentTitle("ACCEDI ORA!!!")
                                                        .setContentText("clicca per accedere")
                                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentIntent(pendingIntent[0])
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Il terzo allenamento giornaliero è in arrivo! Per ottenere risultati migliori e massimizzare i tuoi progressi, è consigliabile completare anche il secondo allenamento. Sei pronto a completare il tuo percorso?"))
                                                        .setAutoCancel(true)
                                                        .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                if (notificationManager != null) {
                                                    notificationManager.notify(1, builder.build());
                                                    mediaPlayersnoze.start();
                                                    Log.d("NotificationReceiver", "Notifica inviata con successo");
                                                } else {
                                                    Log.e("NotificationReceiver", "NotificationManager è null!");
                                                }

                                            } else {
                                                MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.rimandobloccatoterzoallenamento);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                        .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                        .setLargeIcon(resizedLargeIcon)
                                                        .setContentTitle("ACCEDI ORA!!!")
                                                        .setContentText("clicca per accedere")
                                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentIntent(pendingIntent[0])
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Il primo allenamento giornaliero sta per iniziare! Per completare il ciclo di allenamenti di oggi e ottenere risultati migliori, ti consigliamo di completare anche il terzo allenamento. Pronto a dare il massimo?"))
                                                        .setAutoCancel(true)
                                                        .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                if (notificationManager != null) {
                                                    notificationManager.notify(1, builder.build());
                                                    mediaPlayersnoze.start();
                                                    Log.d("NotificationReceiver", "Notifica inviata con successo");
                                                } else {
                                                    Log.e("NotificationReceiver", "NotificationManager è null!");
                                                }

                                                return;
                                            }
                                        } else {

                                            //Recupero il contatore snooze
                                            snoozeCount[0] =sharedPreferences.getInt("snooze_count",0);
                                            System.out.println("Il contatore snooze è: "+ snoozeCount[0]);

                                            //RECUPERO IL VALORE DI IS_ACTION_SNOOZE
                                            isactionsnooze[0] =sharedPreferences.getInt("is_action_snooze",0);

                                            //Se l'utente ha gia riprogrammato la notifica allora apparirà un toast
                                            if (isactionsnooze[0] ==1){
                                                Toast.makeText(context, "Hai già riprogrammato la notifica", Toast.LENGTH_LONG).show();
                                                return; // Esci senza rinviare
                                            }else{
                                                //setto il contatore delle notifiche snooze
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putInt("notify_snooze", 1);
                                                editor.apply();
                                                //Riduco il tempo per la prossima notifica
                                                timenextnotify[0] = timenextnotify[0] - 3600000;
                                                System.out.println("Il tempo per la prossima notifica è: "+ timenextnotify[0]);
                                                editor.putInt("time_next_notify", timenextnotify[0]);
                                                editor.apply();
                                            }

                                            // Incrementa il contatore di rinvio
                                            snoozeCount[0]++;
                                            System.out.println("Il nuovo valore del contatore snooze è: "+ snoozeCount[0]);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("snooze_count", snoozeCount[0]);
                                            editor.apply(); // Salva il nuovo valore del contatore

                                            //Setto il valore di is_action_snooze
                                            editor = sharedPreferences.edit();
                                            editor.putInt("is_action_snooze", 1);
                                            editor.apply();

                                            // Imposta l'allarme di nuovo a 30 minuti da ora
                                            Intent alarmIntent = new Intent(context, NotificationReceiver.class);
                                            pendingIntent[0] = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                                            // Calcola l'orario corrente e aggiungi 60 minuti
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.add(Calendar.MINUTE, 60);  // Aggiungi 60 minuti

                                            // Ottieni l'ora e i minuti esatti per il toast
                                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                            int minute = calendar.get(Calendar.MINUTE);

                                            // Formatta l'orario
                                            @SuppressLint("DefaultLocale") String formattedTime = String.format("%02d:%02d", hour, minute);


                                            // Imposta l'AlarmManager per rinviare la notifica di 30 minuti
                                            long triggerTime = calendar.getTimeInMillis();
                                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                            if (alarmManager != null) {
                                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent[0]);
                                                Log.d("NotificationReceiver", "Notifica rinviata di 1 ora con successo");
                                            }


                                            // Mostra il Toast
                                            Toast.makeText(context, "Allenamento riprogrammato alle "+ formattedTime, Toast.LENGTH_LONG).show();

                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    System.out.println("Si è verificato un errore nel recupero dell'ora dell'allenamento");
                                }
                            });

                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        System.out.println("Si è verificato un errore nel recupero dell'ora dell'allenamento");
                    }
                });

            } else if(dailyNotificationCount == 3){

                System.out.println("Il contatore delle notifiche giornaliere è: "+ dailyNotificationCount);

                int finalDailyNotificationCount2 = dailyNotificationCount;

                apiServiceAllenamentoGiornaliero.getOraAllenamentoGiornaliero(LocalDate.now().getDayOfMonth(),3).enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if(response.isSuccessful() && response.body() != null) {
                            Integer hourOfDay = oraTerzoAllenamento;

                            apiServiceAllenamentoGiornaliero.getMinutiAllenamento(LocalDate.now().getDayOfMonth(),3).enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        Integer minuteOfHour = minutiTerzoAllenamento;


                                        //Recuper il tempo di quando dovrebbe arrivare la notifica di rimando

                                        Oravincolorimando = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                                        Minutivincolorimando = Calendar.getInstance().get(Calendar.MINUTE) + 1;
                                        System.out.println("L'orario del vincolo è: "+Oravincolorimando+":"+Minutivincolorimando);

                                        if ((Oravincolorimando>= hourOfDay && Minutivincolorimando >= minuteOfHour) || (minuteOfHour == 0 && Minutivincolorimando==0 && Oravincolorimando +1 >= hourOfDay)){
                                            //Mando una notifica istantanea dicendo che il prossimo allenamento inzierà a breve e che bisogna completare questo.
                                            // Carica l'immagine dalle risorse come Bitmap
                                            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_2024_10_19_20_18_47);

                                            // Ridimensiona l'immagine alla dimensione desiderata, ad esempio 192x192 per XXXHDPI
                                            Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon, 200, 200, false);

                                            if (finalDailyNotificationCount2 == 2){
                                                MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.rimandobloccatoprimoallenamento);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                        .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                        .setLargeIcon(resizedLargeIcon)
                                                        .setContentTitle("ACCEDI ORA!!!")
                                                        .setContentText("clicca per accedere")
                                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentIntent(pendingIntent[0])
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Attenzione! Il secondo allenamento giornaliero sta per iniziare. Per ottenere risultati ottimali, è consigliabile completare il primo allenamento. Sei pronto a dare il massimo?"))
                                                        .setAutoCancel(true)
                                                        .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                if (notificationManager != null) {
                                                    notificationManager.notify(1, builder.build());
                                                    mediaPlayersnoze.start();
                                                    Log.d("NotificationReceiver", "Notifica inviata con successo");
                                                } else {
                                                    Log.e("NotificationReceiver", "NotificationManager è null!");
                                                }

                                                return;

                                            } else if (finalDailyNotificationCount2 == 3) {


                                                MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.rimandobloccatosecondoallenamento);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                        .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                        .setLargeIcon(resizedLargeIcon)
                                                        .setContentTitle("ACCEDI ORA!!!")
                                                        .setContentText("clicca per accedere")
                                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentIntent(pendingIntent[0])
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Il terzo allenamento giornaliero è in arrivo! Per ottenere risultati migliori e massimizzare i tuoi progressi, è consigliabile completare anche il secondo allenamento. Sei pronto a completare il tuo percorso?"))
                                                        .setAutoCancel(true)
                                                        .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                if (notificationManager != null) {
                                                    notificationManager.notify(1, builder.build());
                                                    mediaPlayersnoze.start();
                                                    Log.d("NotificationReceiver", "Notifica inviata con successo");
                                                } else {
                                                    Log.e("NotificationReceiver", "NotificationManager è null!");
                                                }

                                            } else {
                                                MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.rimandobloccatoterzoallenamento);
                                                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                        .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                        .setLargeIcon(resizedLargeIcon)
                                                        .setContentTitle("ACCEDI ORA!!!")
                                                        .setContentText("clicca per accedere")
                                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                        .setContentIntent(pendingIntent[0])
                                                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Il primo allenamento giornaliero sta per iniziare! Per completare il ciclo di allenamenti di oggi e ottenere risultati migliori, ti consigliamo di completare anche il terzo allenamento. Pronto a dare il massimo?"))
                                                        .setAutoCancel(true)
                                                        .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                                if (notificationManager != null) {
                                                    notificationManager.notify(1, builder.build());
                                                    mediaPlayersnoze.start();
                                                    Log.d("NotificationReceiver", "Notifica inviata con successo");
                                                } else {
                                                    Log.e("NotificationReceiver", "NotificationManager è null!");
                                                }

                                                return;
                                            }
                                        } else {


                                            //Recupero il contatore snooze
                                            snoozeCount[0] =sharedPreferences.getInt("snooze_count",0);
                                            System.out.println("Il contatore snooze è: "+ snoozeCount[0]);

                                            //RECUPERO IL VALORE DI IS_ACTION_SNOOZE
                                            isactionsnooze[0] =sharedPreferences.getInt("is_action_snooze",0);

                                            //Se l'utente ha gia riprogrammato la notifica allora apparirà un toast
                                            if (isactionsnooze[0] ==1){
                                                Toast.makeText(context, "Hai già riprogrammato la notifica", Toast.LENGTH_LONG).show();
                                                return; // Esci senza rinviare
                                            }else{
                                                //setto il contatore delle notifiche snooze
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putInt("notify_snooze", 1);
                                                editor.apply();
                                                //Riduco il tempo per la prossima notifica
                                                timenextnotify[0] = timenextnotify[0] -3600000;
                                                editor.putInt("time_next_notify", timenextnotify[0]);
                                                editor.apply();
                                            }

                                            // Incrementa il contatore di rinvio
                                            snoozeCount[0]++;
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("snooze_count", snoozeCount[0]);
                                            editor.apply(); // Salva il nuovo valore del contatore

                                            //Setto il valore di is_action_snooze
                                            editor = sharedPreferences.edit();
                                            editor.putInt("is_action_snooze", 1);
                                            editor.apply();

                                            // Imposta l'allarme di nuovo a 30 minuti da ora
                                            Intent alarmIntent = new Intent(context, NotificationReceiver.class);
                                            pendingIntent[0] = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                                            // Calcola l'orario corrente e aggiungi 60 minuti
                                            Calendar calendar = Calendar.getInstance();
                                            calendar.add(Calendar.MINUTE, 60);  // Aggiungi 60 minuti

                                            // Ottieni l'ora e i minuti esatti per il toast
                                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                            int minute = calendar.get(Calendar.MINUTE);

                                            // Formatta l'orario
                                            @SuppressLint("DefaultLocale") String formattedTime = String.format("%02d:%02d", hour, minute);


                                            // Imposta l'AlarmManager per rinviare la notifica di 30 minuti
                                            long triggerTime = calendar.getTimeInMillis();
                                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                            if (alarmManager != null) {
                                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent[0]);
                                                Log.d("NotificationReceiver", "Notifica rinviata di 1 ora con successo");
                                            }


                                            // Mostra il Toast
                                            Toast.makeText(context, "Allenamento riprogrammato alle "+ formattedTime, Toast.LENGTH_LONG).show();

                                        }

                                    }
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    System.out.println("Si è verificato un errore nel recupero dell'ora dell'allenamento");
                                }
                            });



                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        System.out.println("Si è verificato un errore nel recupero dell'ora dell'allenamento");
                    }

                });

            } else {

                System.out.println("Il contatore delle notifiche giornaliere è: "+ dailyNotificationCount);

                //Recupero il contatore snooze
                snoozeCount[0] =sharedPreferences.getInt("snooze_count",0);
                System.out.println("Il contatore snooze è: "+ snoozeCount[0]);

                //RECUPERO IL VALORE DI IS_ACTION_SNOOZE
                isactionsnooze[0] =sharedPreferences.getInt("is_action_snooze",0);

                //Se l'utente ha gia riprogrammato la notifica allora apparirà un toast
                if (isactionsnooze[0] ==1){
                    Toast.makeText(context, "Hai già riprogrammato la notifica", Toast.LENGTH_LONG).show();
                    return; // Esci senza rinviare
                }else{
                    //setto il contatore delle notifiche snooze
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("notify_snooze", 1);
                    editor.apply();
                    //Riduco il tempo per la prossima notifica
                    timenextnotify[0] = timenextnotify[0] -3600000;
                    editor.putInt("time_next_notify", timenextnotify[0]);
                    editor.apply();
                }

                // Incrementa il contatore di rinvio
                snoozeCount[0]++;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("snooze_count", snoozeCount[0]);
                editor.apply(); // Salva il nuovo valore del contatore

                //Setto il valore di is_action_snooze
                editor = sharedPreferences.edit();
                editor.putInt("is_action_snooze", 1);
                editor.apply();

                // Imposta l'allarme di nuovo a 30 minuti da ora
                Intent alarmIntent = new Intent(context, NotificationReceiver.class);
                pendingIntent[0] = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                // Calcola l'orario corrente e aggiungi 60 minuti
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, 60);  // Aggiungi 60 minuti

                // Ottieni l'ora e i minuti esatti per il toast
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // Formatta l'orario
                @SuppressLint("DefaultLocale") String formattedTime = String.format("%02d:%02d", hour, minute);

                // Imposta l'AlarmManager per rinviare la notifica di 30 minuti
                long triggerTime = calendar.getTimeInMillis();
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmManager != null) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent[0]);
                    Log.d("NotificationReceiver", "Notifica rinviata di 1 ora con successo");
                }


                // Mostra il Toast
                Toast.makeText(context, "Allenamento riprogrammato alle "+ formattedTime, Toast.LENGTH_LONG).show();
            }

        } else {
            //Controlla se la notifica è un rimando
            if (notifysnooze == 1) {

                System.out.println("E' una notifica di rimando");
                //resetto il valore di is_action_snooze
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("is_action_snooze", 0);
                editor.apply();


                //resetto il contatore dele notifiche snooze
                editor = sharedPreferences.edit();
                editor.putInt("notify_snooze", 0);
                editor.apply();

                //Recupero lo snoze count
                snoozeCount[0] = sharedPreferences.getInt("snooze_count", 0);


                if (snoozeCount[0] >= 2) {

                    // Carica l'immagine dalle risorse come Bitmap
                    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_2024_10_19_20_18_47);

                    // Ridimensiona l'immagine alla dimensione desiderata, ad esempio 192x192 per XXXHDPI
                    Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon, 200, 200, false);


                    MediaPlayer mediaPlayersnoze = MediaPlayer.create(context, R.raw.suonoerrorenotifica);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                            .setSmallIcon(R.drawable.iconamyfisiocoach)
                            .setLargeIcon(resizedLargeIcon)
                            .setContentTitle("ACCEDI ORA!!!")
                            .setContentText("clicca per accedere")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent[0])
                            .setStyle(new NotificationCompat.BigTextStyle().bigText("È finito il tempo per rimandare, è ora di allenarti!"))
                            .setAutoCancel(true)
                            .setColor(ContextCompat.getColor(context, R.color.BluFisio)); // Imposta un colore personalizzato

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (notificationManager != null) {
                        notificationManager.notify(1, builder.build());
                        mediaPlayersnoze.start();
                        Log.d("NotificationReceiver", "Notifica inviata con successo");
                    } else {
                        Log.e("NotificationReceiver", "NotificationManager è null!");
                    }

                } else {


                    // Carica l'immagine dalle risorse come Bitmap
                    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_2024_10_19_20_18_47);

                    // Ridimensiona l'immagine alla dimensione desiderata, ad esempio 192x192 per XXXHDPI
                    Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon, 200, 200, false);


                    final MediaPlayer[] mediaPlayerOneSnooze = new MediaPlayer[1];

                    System.out.println("Controllo il contatore delle notifiche giornaliere: " + dailyNotificationCount);
                    if (dailyNotificationCount - 1 == 1) {

                        PendingIntent finalPendingIntent = pendingIntent[0];
                        apiServiceUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer stage = response.body();

                                    System.out.println("Lo stage dell'utente è: "+ stage);

                                    //Se lo stage è maggiore di 2 allora la notifica presenta l'azione per rimandare
                                    if (stage > 0) {
                                        mediaPlayerOneSnooze[0] = MediaPlayer.create(context, R.raw.frasenotificaprimoallenamento);

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Inizia il tuo primo allenamento con determinazione! Ogni passo ti avvicina ai tuoi obiettivi di recupero."))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent)
                                                .setAutoCancel(true)
                                                .addAction(0, "Rimanda di 1 ora", snoozePendingIntent) // Aggiungi il bottone senza icona
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayerOneSnooze[0].start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    } else {
                                        mediaPlayerOneSnooze[0] = MediaPlayer.create(context, R.raw.frasenotificaprimoallenamento);

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Inizia il tuo primo allenamento con determinazione! Ogni passo ti avvicina ai tuoi obiettivi di recupero."))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent)
                                                .setAutoCancel(true)
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayerOneSnooze[0].start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }

                                    }

                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dello stage dell'utente");
                            }
                        });

                    } else if (dailyNotificationCount - 1 == 2) {

                        PendingIntent finalPendingIntent1 = pendingIntent[0];
                        apiServiceUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {

                                    Integer stage = response.body();
                                    //Se lo stage è maggiore di 2 allora la notifica presenta l'azione per rimandare
                                    if (stage > 0) {
                                        mediaPlayerOneSnooze[0] = MediaPlayer.create(context, R.raw.frasenotificasecondoallenamento);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Questo è il tuo secondo allenamento: ogni ripetizione è un passo avanti verso il tuo benessere!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent1)
                                                .setAutoCancel(true)
                                                .addAction(0, "Rimanda di 1 ora", snoozePendingIntent) // Aggiungi il bottone senza icona
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayerOneSnooze[0].start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    } else {

                                        mediaPlayerOneSnooze[0] = MediaPlayer.create(context, R.raw.frasenotificasecondoallenamento);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Questo è il tuo secondo allenamento: ogni ripetizione è un passo avanti verso il tuo benessere!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent1)
                                                .setAutoCancel(true)
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayerOneSnooze[0].start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }

                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dello stage dell'utente");
                            }
                        });

                    } else if (dailyNotificationCount - 1 == 3) {


                        //Se lo stage è maggiore di 2 allora la notifica presenta l'azione per rimandare
                        PendingIntent finalPendingIntent3 = pendingIntent[0];
                        apiServiceUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer stage = response.body();

                                    if (stage > 0) {
                                        mediaPlayerOneSnooze[0] = MediaPlayer.create(context, R.raw.frasenotificaterzoallenamento);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Completa la tua sessione con il terzo allenamento! Questo è il momento di onorare il tuo impegno e i tuoi sforzi!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent3)
                                                .setAutoCancel(true)
                                                .addAction(0, "Rimanda di 1 ora", snoozePendingIntent) // Aggiungi il bottone senza icona
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayerOneSnooze[0].start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    } else {
                                        mediaPlayerOneSnooze[0] = MediaPlayer.create(context, R.raw.frasenotificaterzoallenamento);
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Completa la tua sessione con il terzo allenamento! Questo è il momento di onorare il tuo impegno e i tuoi sforzi!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent3)
                                                .setAutoCancel(true)
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayerOneSnooze[0].start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    }


                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dello stage dell'utente");
                            }
                        });
                    }
                }

                //Riprogrammo la notifica
                scheduleNextNotification(context, timenextnotify[0]);

            } else {
                System.out.println("Non è una notifica di snooze");

                System.out.println("Contatore notifiche giornaliere: " + dailyNotificationCount);

                //resetto il valore di is_action_snooze
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("is_action_snooze", 0);
                editor.apply();

                // Se sono state inviate 3 notifiche oggi, riprogramma la notifica per il giorno successivo
                if (dailyNotificationCount > 3) {

                    int giorno = LocalDate.now().getDayOfMonth() + 1;
                    if (giorno > Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)) {
                        giorno = 1;
                    }

                    int finalGiorno = giorno;
                    apiServiceAllenamentoGiornaliero.getOraAllenamentoGiornaliero(giorno, 1).enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Integer hourOfDay = oraPrimoAllenamento;

                                apiServiceAllenamentoGiornaliero.getMinutiAllenamento(finalGiorno, 1).enqueue(new Callback<Integer>() {
                                    @Override
                                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            Integer minuteOfHour = minutiPrimoAllenamento;
                                            System.out.println("L'orario per il primo allenamento giornaliero del giorno successivo è: " + hourOfDay + ":" + minuteOfHour);
                                            resetDailyNotification(context, sharedPreferences, hourOfDay, minuteOfHour); // Funzione per riprogrammare al giorno successivo

                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Integer> call, Throwable t) {
                                        System.out.println("Si è verificato un errore nel recupero dei minuti dell'allenamento");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                            System.out.println("Si è verificato un errore nel recupero dell'ora dell'allenamento");
                        }
                    });
                } else {
                    //resetto il contatore dei rimandi
                    editor = sharedPreferences.edit();
                    editor.putInt("snooze_count", 0);
                    editor.apply();

                    // Carica l'immagine dalle risorse come Bitmap
                    Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.photo_2024_10_19_20_18_47);

                    // Ridimensiona l'immagine alla dimensione desiderata, ad esempio 192x192 per XXXHDPI
                    Bitmap resizedLargeIcon = Bitmap.createScaledBitmap(largeIcon, 200, 200, false);

                    if (dailyNotificationCount == 1) {

                        //Se lo stage è maggiore di 2 allora la notifica presenta l'azione per rimandare

                        PendingIntent finalPendingIntent2 = pendingIntent[0];
                        apiServiceUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer stage = response.body();

                                    if (stage > 0) {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Inizia il tuo primo allenamento con determinazione! Ogni passo ti avvicina ai tuoi obiettivi di recupero."))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent2)
                                                .setAutoCancel(true)
                                                .addAction(0, "Rimanda di 1 ora", snoozePendingIntent) // Aggiungi il bottone senza icona
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayer.start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    } else {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Inizia il tuo primo allenamento con determinazione! Ogni passo ti avvicina ai tuoi obiettivi di recupero."))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent2)
                                                .setAutoCancel(true)
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayer.start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dello stage dell'utente");
                            }

                        });

                    } else if (dailyNotificationCount == 2) {

                        //Se lo stage è maggiore di 2 allora la notifica presenta l'azione per rimandare

                        PendingIntent finalPendingIntent4 = pendingIntent[0];
                        apiServiceUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer stage = response.body();

                                    if (stage > 0) {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Questo è il tuo secondo allenamento: ogni ripetizione è un passo avanti verso il tuo benessere!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent4)
                                                .setAutoCancel(true)
                                                .addAction(0, "Rimanda di 1 ora", snoozePendingIntent) // Aggiungi il bottone senza icona
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayer.start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    } else {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Questo è il tuo secondo allenamento: ogni ripetizione è un passo avanti verso il tuo benessere!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent4)
                                                .setAutoCancel(true)
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayer.start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    }


                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dello stage dell'utente");
                            }
                        });

                    } else if (dailyNotificationCount == 3) {

                        //Se lo stage è maggiore di 2 allora la notifica presenta l'azione per rimandare

                        PendingIntent finalPendingIntent5 = pendingIntent[0];
                        apiServiceUtente.getStageUtente(email).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer stage = response.body();
                                    if (stage > 0) {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Completa la tua sessione con il terzo allenamento! Questo è il momento di onorare il tuo impegno e i tuoi sforzi!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent5)
                                                .setAutoCancel(true)
                                                .addAction(0, "Rimanda di 1 ora", snoozePendingIntent) // Aggiungi il bottone senza icona
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayer.start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    } else {
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "daily_reminder")
                                                .setSmallIcon(R.drawable.iconamyfisiocoach)
                                                .setLargeIcon(resizedLargeIcon)
                                                .setContentTitle("ACCEDI ORA!!!")
                                                .setContentText("clicca per accedere")
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText("Completa la tua sessione con il terzo allenamento! Questo è il momento di onorare il tuo impegno e i tuoi sforzi!"))
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                .setContentIntent(finalPendingIntent5)
                                                .setAutoCancel(true)
                                                .setColor(ContextCompat.getColor(context, R.color.BluFisio));  // Imposta il colore dell'accento (per l'icona e il testo)

                                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                        if (notificationManager != null) {
                                            notificationManager.notify(1, builder.build());
                                            mediaPlayer.start();

                                            Log.d("NotificationReceiver", "Notifica inviata con successo");
                                        } else {
                                            Log.e("NotificationReceiver", "NotificationManager è null!");
                                        }
                                    }

                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dello stage dell'utente");
                            }
                        });
                        Toast.makeText(context, "I tuoi allenamenti giornalieri sono finiti, ci vediamo domani.", Toast.LENGTH_SHORT).show();

                    }

                    // Aumenta il contatore delle notifiche giornaliere
                    dailyNotificationCount++;
                    editor.putInt("daily_notification_count", dailyNotificationCount);
                    editor.apply(); // Salva il nuovo valore del contatore di notifiche giornaliere

                    if (dailyNotificationCount > 3) {

                        System.out.println("La notifica speciale è stata programmata");

                        HourProgNotifyNextDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        MinutesProgNotifyNextDay = Calendar.getInstance().get(Calendar.MINUTE) + 3;

                        scheduleNextNotification(context, 180000);
                    } else {

                        SharedPreferences.Editor finalEditor = editor;
                        int finalDailyNotificationCount1 = dailyNotificationCount;
                        Integer ora = 0;
                        Integer minuti = 0;
                        if (dailyNotificationCount == 1) {
                             ora = oraPrimoAllenamento;
                             minuti = minutiPrimoAllenamento;
                        } else if (dailyNotificationCount == 2) {
                             ora = oraSecondoAllenamento;
                             minuti = minutiSecondoAllenamento;
                        } else if (dailyNotificationCount == 3) {
                             ora = oraTerzoAllenamento;
                             minuti = minutiTerzoAllenamento;
                        }
                        Integer finalOra = ora;
                        Integer finalMinuti = minuti;
                        apiServiceAllenamentoGiornaliero.getOraAllenamentoGiornaliero(LocalDate.now().getDayOfMonth(), dailyNotificationCount).enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer hourOfDay = finalOra;

                                    apiServiceAllenamentoGiornaliero.getMinutiAllenamento(LocalDate.now().getDayOfMonth(), finalDailyNotificationCount1).enqueue(new Callback<Integer>() {
                                        @Override
                                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                                            if (response.isSuccessful() && response.body() != null) {
                                                Integer minuteOfHour = finalMinuti;


                                                //Calcolo i minuti rimanenti per la prossima notifica
                                                LocalTime currentTime = LocalTime.now();
                                                LocalTime nextTime = LocalTime.of(hourOfDay, minuteOfHour);

                                                timenextnotify[0] = Math.toIntExact(Duration.between(currentTime, nextTime).toMillis());


                                                //Recupero il tempo speso per la notifica
                                                System.out.println("Il tempo con valore Intero per la prossima notifica è: " + timenextnotify[0]);

                                                //Imposto il tempo per la prossima notifica
                                                finalEditor.putInt("time_next_notify", timenextnotify[0]);
                                                finalEditor.apply();

                                                //Recupero il tempo per la prossima notifica
                                                timenextnotify[0] = sharedPreferences.getInt("time_next_notify", 0);

                                                scheduleNextNotification(context, timenextnotify[0]);


                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<Integer> call, Throwable t) {
                                            System.out.println("Si è verificato un errore nel recupero dei minuti dell'allenamento");
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                System.out.println("Si è verificato un errore nel recupero dell'ora dell'allenamento");
                            }
                        });
                    }


                }

            }
        }

    }


    @SuppressLint("ScheduleExactAlarm")
    private void scheduleNextNotification(Context context, int time) {

        System.out.println("Ho riprogrammato la notifica con scheduleNextNotification");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, time);  // Aggiungi minuti


        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        long triggerTime = calendar.getTimeInMillis();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    // Funzione per resettare le notifiche e riprogrammarle al giorno successivo
    @SuppressLint("ScheduleExactAlarm")
    private void resetDailyNotification(Context context, SharedPreferences sharedPreferences, int hourOfDay, int minuteOfHour) {
        // Resetta il contatore delle notifiche giornaliere
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("daily_notification_count", 1);
        editor.apply();

        System.out.println("Sono nella notfica speciale");

        // Configura il calendario per il giorno successivo all'ora desiderata
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.add(Calendar.DAY_OF_YEAR, 1);  // Aggiungi un giorno al giorno corrente
        targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);  // Imposta l'ora desiderata
        targetCalendar.set(Calendar.MINUTE, minuteOfHour);    // Imposta i minuti desiderati
        targetCalendar.set(Calendar.SECOND, 0);               // Imposta i secondi a 0
        targetCalendar.set(Calendar.MILLISECOND, 0);          // Imposta i millisecondi a 0

        // Crea l'intento per la notifica
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Programma l'allarme esattamente per l'ora desiderata
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCalendar.getTimeInMillis(), pendingIntent);
            Log.d("NotificationReceiver", "Notifica riprogrammata per il giorno successivo alle " + hourOfDay + ":" + minuteOfHour);
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Reminder";
            String description = "Channel for daily reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("daily_reminder", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}