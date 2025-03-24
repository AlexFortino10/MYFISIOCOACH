package com.example.myfisiocoach;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Calendar;

public class ImpostazioniActivity extends AppCompatActivity {

    BottomNavigationView navigationmenu;
    Switch switchbuttonNotifiche, switchbuttonMicrofono;


    TextView OraPrimoAllenamento, MinutiPrimoAllenamento,OraSecondoAllenamento, MinutiSecondoAllenamento,OraTerzoAllenamento, MinutiTerzoAllenamento;
    ImageButton CambiaOraPrimoAllenamento, CambiaOraSecondoAllenamento, CambiaOraTerzoAllenamento;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impostazioni);

        // Setto il menu di navigazione
        navigationmenu = findViewById(R.id.bottomNavigationViewNotifiche);
        navigationmenu.setSelectedItemId(R.id.Impostazioni);

        // Ricevo l'email dell'utente
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        //Setto le informazioni per i primi tre allenamenti
        OraPrimoAllenamento = findViewById(R.id.OraPrimoAllenamento);
        MinutiPrimoAllenamento = findViewById(R.id.MinutiPrimoAllenamento);
        OraSecondoAllenamento = findViewById(R.id.OraSecondoAllenamento);
        MinutiSecondoAllenamento = findViewById(R.id.MinutiSecondoAllenamento);
        OraTerzoAllenamento = findViewById(R.id.OraTerzoAllenamento);
        MinutiTerzoAllenamento = findViewById(R.id.MinutiTerzoAllenamento);

        //Setto i bottoni per cambiare l'ora degli allenamenti
        CambiaOraPrimoAllenamento = findViewById(R.id.imageButtonCambiaOraPrimoAllenamento);
        CambiaOraSecondoAllenamento = findViewById(R.id.imageButtonCambiaOraSecondoAllenamento);
        CambiaOraTerzoAllenamento = findViewById(R.id.imageButtonCambiaOraTerzoAllenamento);

        //Creo uno shared preference per salvare le ore degli allenamenti
        SharedPreferences sharedPref = getSharedPreferences("Allenamenti", Context.MODE_PRIVATE);
        //Recuper ore e minuti degli allenamenti dallo shared preference
        int oraPrimoAllenamento = sharedPref.getInt("OraPrimoAllenamento", 9);
        int minutiPrimoAllenamento = sharedPref.getInt("MinutiPrimoAllenamento", 0);
        int oraSecondoAllenamento = sharedPref.getInt("OraSecondoAllenamento", 13);
        int minutiSecondoAllenamento = sharedPref.getInt("MinutiSecondoAllenamento", 0);
        int oraTerzoAllenamento = sharedPref.getInt("OraTerzoAllenamento", 17);
        int minutiTerzoAllenamento = sharedPref.getInt("MinutiTerzoAllenamento", 0);

        //Setto le ore e i minuti degli allenamenti nei textview
        OraPrimoAllenamento.setText(String.valueOf(oraPrimoAllenamento));
        MinutiPrimoAllenamento.setText(String.valueOf(minutiPrimoAllenamento));
        OraSecondoAllenamento.setText(String.valueOf(oraSecondoAllenamento));
        MinutiSecondoAllenamento.setText(String.valueOf(minutiSecondoAllenamento));
        OraTerzoAllenamento.setText(String.valueOf(oraTerzoAllenamento));
        MinutiTerzoAllenamento.setText(String.valueOf(minutiTerzoAllenamento));



        //Se clicco sul bottone per cambiare l'ora del primo allenamento si apre un timepicker che mi permette di cambiare l'orario sia nel textview che nello shared preference
        CambiaOraPrimoAllenamento.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(ImpostazioniActivity.this, R.style.CustomTimePickerDialog,(view, hourOfDay, minute) -> {
                //Se sommando 3 ore all'orario scelto arriviamo o superiamo l'orario del secondo allenamento o se l'ora scelta è minore di quella corrente, mostro una dialog che mi dice che gli allenamenti devono essere distanziati di almeno 3 ore
                //Recupero l'ora del secondo allenamento dallo shared preference

                int temporasecondoallenamento = sharedPref.getInt("OraSecondoAllenamento", 13);

                if(hourOfDay+2>=temporasecondoallenamento || hourOfDay< Calendar.getInstance().get(Calendar.HOUR_OF_DAY)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                    builder.setView(dialogView);
                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                    TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);

                    title.setText("Errore");
                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                    title.setCompoundDrawablePadding(10);
                    message.setText("Gli allenamenti devono essere distanziati di almeno 3 ore. Cambia l'orario del primo allenamento considerando che l'ordine di orario deve essere: \n 1° primo allenamento \n 2° secondo allanemento \n 3° terzo allenamento.");
                    positiveButton.setText("Ok");
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setOnClickListener(v1 -> alertDialog.dismiss());
                } else {

                    OraPrimoAllenamento.setText(String.valueOf(hourOfDay));
                    MinutiPrimoAllenamento.setText(String.valueOf(minute));
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("OraPrimoAllenamento", hourOfDay);
                    editor.putInt("MinutiPrimoAllenamento", minute);
                    editor.apply();
                    if (checkNotificationPermission() && checkAlarmPermission()) {
                        //Controllo se l'orario è già passato, se non è passato allora pianifica la notifica

                        System.out.println("Pianifico la notifica");
                        scheduleDailyNotification(ImpostazioniActivity.this, hourOfDay, minute);

                    }

                }
            }, oraPrimoAllenamento, minutiPrimoAllenamento, true);
            timePickerDialog.show();
        });

        //Se clicco sul bottone per cambiare l'ora del secondo allenamento si apre un timepicker che mi permette di cambiare l'orario sia nel textview che nello shared preference
        CambiaOraSecondoAllenamento.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(ImpostazioniActivity.this, R.style.CustomTimePickerDialog,(view, hourOfDay, minute) -> {

                //Se sommando 3 ore all'orario scelto arriviamo o superiamo l'orario del terzo allenamento o se sottraendo tre ore arriviamo all'ora del primo allenamento, mostro una dialog che mi dice che gli allenamenti devono essere distanziati di almeno 3 ore
                int temporaprimoallenamento = sharedPref.getInt("OraPrimoAllenamento", 9);
                int temporaterzoallenamento = sharedPref.getInt("OraTerzoAllenamento", 17);

                if(hourOfDay+2>=temporaterzoallenamento || hourOfDay-2<=temporaprimoallenamento){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                    builder.setView(dialogView);
                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                    TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);

                    title.setText("Errore");
                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                    title.setCompoundDrawablePadding(10);
                    message.setText("Gli allenamenti devono essere distanziati di almeno 3 ore. Cambia l'orario del primo allenamento considerando che l'ordine di orario deve essere: \n 1° primo allenamento \n 2° secondo allanemento \n 3° terzo allenamento.");
                    positiveButton.setText("Ok");
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setOnClickListener(v1 -> alertDialog.dismiss());
                } else {

                OraSecondoAllenamento.setText(String.valueOf(hourOfDay));
                MinutiSecondoAllenamento.setText(String.valueOf(minute));
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("OraSecondoAllenamento", hourOfDay);
                editor.putInt("MinutiSecondoAllenamento", minute);
                editor.apply();
                }
            }, oraSecondoAllenamento, minutiSecondoAllenamento, true);
            timePickerDialog.show();
        });

        //Se clicco sul bottone per cambiare l'ora del terzo allenamento si apre un timepicker che mi permette di cambiare l'orario sia nel textview che nello shared preference
        CambiaOraTerzoAllenamento.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(ImpostazioniActivity.this, R.style.CustomTimePickerDialog,(view, hourOfDay, minute) -> {
                //Se sottraendo 3 ore all'orario scelto arriviamo o superiamo l'orario del secondo allenamento o se sommando tre ore arriviamo all'ora del primo allenamento, mostro una dialog che mi dice che gli allenamenti devono essere distanziati di almeno 3 ore
                int temporaPrimoAllenamento = sharedPref.getInt("OraPrimoAllenamento", 9);
                int temporaSecondoAllenamento = sharedPref.getInt("OraSecondoAllenamento", 13);
                if(hourOfDay-2<=temporaSecondoAllenamento){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                    builder.setView(dialogView);
                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                    TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    alertDialog.setCancelable(false);

                    title.setText("Errore");
                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                    title.setCompoundDrawablePadding(10);
                    message.setText("Gli allenamenti devono essere distanziati di almeno 3 ore. Cambia l'orario del primo allenamento considerando che l'ordine di orario deve essere: \n 1° primo allenamento \n 2° secondo allanemento \n 3° terzo allenamento.");
                    positiveButton.setText("Ok");
                    negativeButton.setVisibility(View.GONE);
                    positiveButton.setOnClickListener(v1 -> alertDialog.dismiss());
                } else {
                OraTerzoAllenamento.setText(String.valueOf(hourOfDay));
                MinutiTerzoAllenamento.setText(String.valueOf(minute));
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("OraTerzoAllenamento", hourOfDay);
                editor.putInt("MinutiTerzoAllenamento", minute);
                editor.apply();
                }
            }, oraTerzoAllenamento, minutiTerzoAllenamento, true);
            timePickerDialog.show();
        });

        // Setto il listener per il menu di navigazione
        navigationmenu.setOnNavigationItemSelectedListener(item -> {
            if (R.id.Home == item.getItemId()) {
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
                return true;
            } else if (R.id.Profilo == item.getItemId()) {
                Intent intent3 = new Intent(getApplicationContext(), ProfiloActivity.class);
                intent3.putExtra("email", email);
                startActivity(intent3);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (R.id.MyFisio == item.getItemId()) {
                //Mostro un dialog all'utente che informa che il dialogo con l'avatar è possibile solo nella homepage
                AlertDialog.Builder builder = new AlertDialog.Builder(ImpostazioniActivity.this);
                LayoutInflater inflater = ImpostazioniActivity.this.getLayoutInflater();
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

        // Setto il bottone switch per le notifiche
        switchbuttonNotifiche = findViewById(R.id.switchbuttonNotifiche);

        // Setto il bottone switch per il microfono
        switchbuttonMicrofono = findViewById(R.id.switchbuttonMicrofono);

        // Controlla i permessi all'avvio e aggiorna lo stato dello switch
        checkPermissionsAndSetSwitchNotifiche();
        checkPermissionMicrofonoAndSetSwitchMicrofono();

        // Setto il listener per il bottone switch
        switchbuttonNotifiche.setOnCheckedChangeListener((buttonView, isCheckedNotifiche) -> {
            if (isCheckedNotifiche) {
                switchbuttonNotifiche.setTextColor(getResources().getColor(R.color.white));

                // Controlla i permessi per notifiche e sveglie
                if (!checkNotificationPermission() || !checkAlarmPermission()) {
                    // Reindirizza l'utente alle impostazioni per concedere i permessi
                    requestNotificationAndAlarmPermissions();
                }

            } else {
                // Dialog per confermare la disattivazione delle notifiche
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

                title.setText("Disabilitare le notifiche?");
                title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                title.setCompoundDrawablePadding(10);
                message.setText("Per disabilitare completamente le notifiche e gli allarmi, devi rimuovere i permessi dalle impostazioni del dispositivo.");
                positiveButton.setText("Vai alle impostazioni");
                negativeButton.setText("Annulla");
                negativeButton.setOnClickListener(v1 -> {alertDialog.dismiss(); switchbuttonNotifiche.setChecked(true);});
                positiveButton.setOnClickListener(v1 -> {
                    // Reindirizza l'utente alle impostazioni delle notifiche
                    Intent intent1 = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                    startActivity(intent1);

                    // Reindirizza anche alla schermata delle sveglie e degli allarmi (Android 12+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent2 = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent2.setData(Uri.parse("package:" + getPackageName()));  // Porta l'utente direttamente alla schermata delle impostazioni della tua app
                        startActivity(intent2);
                    }
                    alertDialog.dismiss();
                });
            }
        });


        // Setto il listener per il bottone switch del microfono

        switchbuttonMicrofono.setOnCheckedChangeListener((buttonView, isCheckedMicrofono) -> {
            if (isCheckedMicrofono) {
                // Controlla i permessi per il microfono
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.white));

                        //Mostro una dialog per informare l'utente di come attivare il microfono
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        LayoutInflater inflater = this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                        builder.setView(dialogView);
                        TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                        TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                        Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                        Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        alertDialog.setCancelable(false);

                        title.setText("Attivare il microfono?");
                        title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_information_outline_32, 0);
                        title.setCompoundDrawablePadding(10);
                        message.setText("Per attivare il microfono, segui questi passaggi:\n1. Premi sul bottone 'Attiva microfono'\n2. Premi su 'Mentre usi l\'app '. ");
                        positiveButton.setText("Attiva microfono");
                        negativeButton.setText("Annulla");
                        negativeButton.setOnClickListener(v1 -> {
                            alertDialog.dismiss();
                            switchbuttonMicrofono.setChecked(false);
                            switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
                        });
                        positiveButton.setOnClickListener(v1 -> {
                            alertDialog.dismiss();
                            // Richiedi i permessi per il microfono
                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                        });


                    }
                }


            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

                        switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.GrigioDisattivo));


                        // Dialog per confermare la disattivazione del microfono
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        LayoutInflater inflater = this.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                        builder.setView(dialogView);
                        TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                        TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                        Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                        Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        alertDialog.setCancelable(false);

                        title.setText("Disabilitare il microfono?");
                        title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                        title.setCompoundDrawablePadding(10);
                        message.setText("Per disabilitare completamente il microfono, segui questi passaggi:\n1. Premi su 'Autorizzazioni'\n2. Premi su 'Microfono'\n3. Seleziona 'Chiedi ogni volta'.");
                        positiveButton.setText("Vai alle impostazioni");
                        negativeButton.setText("Annulla");
                        negativeButton.setOnClickListener(v1 -> {alertDialog.dismiss(); switchbuttonMicrofono.setChecked(true); switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.white));});
                        positiveButton.setOnClickListener(v1 -> {
                            // Reindirizza l'utente alle impostazioni del microfono
                            Intent intent1 = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent1);

                            // Mostra un Toast per ricordare all'utente i passaggi
                            Toast.makeText(this, "Autorizzazioni -> Microfono -> Seleziona 'Chiedi ogni volta'", Toast.LENGTH_LONG).show();


                            alertDialog.dismiss();
                        });





                    }
                }
            }
        });
    }

    private void checkPermissionMicrofonoAndSetSwitchMicrofono() {
        // Implementa la logica per verificare i permessi del microfono
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                switchbuttonMicrofono.setChecked(true);
                switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.white));
            } else {
                switchbuttonMicrofono.setChecked(false);
                switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Controlla lo stato dei permessi quando si ritorna all'activity
        boolean notificationPermission = checkNotificationPermission();
        boolean alarmPermission = checkAlarmPermission();


        if(notificationPermission && alarmPermission){
            switchbuttonNotifiche.setChecked(true);
        } else if (!notificationPermission && alarmPermission) {
            // Mostra la dialog per i permessi incompleti
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog, null);
            builder.setView(dialogView);
            TextView title = dialogView.findViewById(R.id.Titlealertdialog);
            TextView message = dialogView.findViewById(R.id.alertdialogMessage);
            Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
            Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);

            title.setText("Permessi incompleti");
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_information_outline_32, 0);
            title.setCompoundDrawablePadding(10);
            message.setText("Per far funzionare correttamente le notifiche, è necessario attivare il permesso delle notifiche. Se invece non vuoi ricevere le notifiche disattiva il permesso delle sveglie.");
            positiveButton.setText("Attiva notifiche");
            negativeButton.setText("Disattiva sveglie");

            positiveButton.setOnClickListener(v -> {
                switchbuttonNotifiche.setChecked(true);
                // Reindirizza l'utente alle impostazioni delle notifiche
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                alertDialog.dismiss();
            });

            negativeButton.setOnClickListener(v -> {
                switchbuttonNotifiche.setChecked(false);
                // Reindirizza l'utente alle impostazioni delle sveglie
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                alertDialog.dismiss();
            });

        } else if (notificationPermission && !alarmPermission) {
            // Mostra la dialog per i permessi incompleti
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog, null);
            builder.setView(dialogView);
            TextView title = dialogView.findViewById(R.id.Titlealertdialog);
            TextView message = dialogView.findViewById(R.id.alertdialogMessage);
            Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
            Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            alertDialog.setCancelable(false);

            title.setText("Permessi incompleti");
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_information_outline_32, 0);
            title.setCompoundDrawablePadding(10);
            message.setText("Per far funzionare correttamente le notifiche, è necessario attivare il permesso delle sveglie. Se invece non vuoi ricevere le notifiche disattiva il permesso delle notifiche.");
            positiveButton.setText("Attiva sveglie");
            negativeButton.setText("Disattiva notifiche");
            positiveButton.setOnClickListener(v -> {
                switchbuttonNotifiche.setChecked(true);
                // Reindirizza anche alla schermata delle sveglie e degli allarmi (Android 12+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent intent2 = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent2.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent2);
                }
                alertDialog.dismiss();
            });

            negativeButton.setOnClickListener(v -> {
                switchbuttonNotifiche.setChecked(false);
                // Reindirizza l'utente alle impostazioni delle notifiche
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                alertDialog.dismiss();
            });

        } else {
            switchbuttonNotifiche.setChecked(false);
            switchbuttonNotifiche.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
        }

        //Gestisco lo switch del microfono quando si ritorna all'activity

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                switchbuttonMicrofono.setChecked(true);
                switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.white));
            } else {
                switchbuttonMicrofono.setChecked(false);
                switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
            }
        }
    }


    // Metodo per controllare se i permessi sono già stati concessi e aggiornare lo stato dello switch
    private void checkPermissionsAndSetSwitchNotifiche() {
        boolean notificationPermission = checkNotificationPermission();
        boolean alarmPermission = checkAlarmPermission();

        if (notificationPermission && alarmPermission) {
            switchbuttonNotifiche.setChecked(true);
            switchbuttonNotifiche.setTextColor(getResources().getColor(R.color.white));
            Log.d("ImpostazioniActivity", "Permessi attivi, switch attivato");
        } else {
            switchbuttonNotifiche.setChecked(false);
            switchbuttonNotifiche.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
        }
    }

    // Metodi di controllo dei permessi
    private boolean checkNotificationPermission() {
        // Implementa la logica per verificare i permessi di notifica
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Permessi non necessari su versioni Android precedenti
    }

    private boolean checkAlarmPermission() {
        // Implementa la logica per verificare i permessi di sveglia
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true; // Permessi non necessari su versioni Android precedenti
    }

    private void requestNotificationAndAlarmPermissions() {
        // Implementa la logica per richiedere i permessi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
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







}
