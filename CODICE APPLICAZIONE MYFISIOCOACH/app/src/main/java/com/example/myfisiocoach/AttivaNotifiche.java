package com.example.myfisiocoach;

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
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class AttivaNotifiche extends AppCompatActivity {

    ImageView tornaIndietro;

    Switch switchbutton;

    Button AvanzaButton,AttivaInSeguitoButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attiva_notifiche);


        // Ricevo l'email dell'utente
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");

        //Setto il bottone per tornare indietro
        tornaIndietro = findViewById(R.id.tornaLoginIndietroButton2);

        //Quando clicco sul bottone per tornare indietro torno alla schermata di login
        tornaIndietro.setOnClickListener(v -> {
            startActivity(new Intent(AttivaNotifiche.this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });


        //Setto il bottone per attivare le notifiche in seguito
        AttivaInSeguitoButton = findViewById(R.id.AttivaInSeguitoButton);


        //Quando clicco sul bottone per attivare le notifiche in seguito vado alla HomePage passando l'email dell'utente
        AttivaInSeguitoButton.setOnClickListener(v -> {
            //Se il microfono non è attivo allora vado alla pagina per attivarlo
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                Intent intent1 = new Intent(AttivaNotifiche.this, AttivaMicrofonoActivity.class);
                intent1.putExtra("email", email);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }else {
                Intent intent1 = new Intent(AttivaNotifiche.this, HomepageActivity.class);
                intent1.putExtra("email", email);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });


        //Setto il bottone per andare avanti
        AvanzaButton = findViewById(R.id.avanzaButtonNotifiche);

        //Disabilito il bottone per andare avanti
        AvanzaButton.setEnabled(false);

        //Quando clicco sul bottone per andare avanti vado alla HomePage passando l'email dell'utente
        AvanzaButton.setOnClickListener(v -> {

            //Se il microfono non è attivo allora vado alla pagina per attivarlo
            if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                Intent intent1 = new Intent(AttivaNotifiche.this, AttivaMicrofonoActivity.class);
                intent1.putExtra("email", email);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }else {
                Intent intent1 = new Intent(AttivaNotifiche.this, HomepageActivity.class);
                intent1.putExtra("email", email);
                startActivity(intent1);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });


        // Setto il bottone switch
        switchbutton = findViewById(R.id.switchbuttonNotifiche);

        // Controlla i permessi all'avvio e aggiorna lo stato dello switch
        checkPermissionsAndSetSwitch();

        // Setto il listener per il bottone switch
        switchbutton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                switchbutton.setTextColor(getResources().getColor(R.color.white));

                // Controlla i permessi per notifiche e sveglie
                if (!checkNotificationPermission() || !checkAlarmPermission()) {
                    // Reindirizza l'utente alle impostazioni per concedere i permessi
                    requestNotificationAndAlarmPermissions();
                }

                //Abilito il bottone per andare avanti
                AvanzaButton.setEnabled(true);
                AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                AvanzaButton.setTextColor(getResources().getColor(R.color.BluFisio));
                AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

                //Disabilito il bottone per attivare le notifiche in seguito
                AttivaInSeguitoButton.setEnabled(false);
                AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.white));
                AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));




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
                negativeButton.setOnClickListener(v1 -> {alertDialog.dismiss(); switchbutton.setChecked(true);});
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


                //Disabilito il bottone per andare avanti
                AvanzaButton.setEnabled(false);
                AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                AvanzaButton.setTextColor(getResources().getColor(R.color.white));
                AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

                //Abilito il bottone per attivare le notifiche in seguito
                AttivaInSeguitoButton.setEnabled(true);
                AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.BluFisio));
                AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));


            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Controlla lo stato dei permessi quando si ritorna all'activity
        boolean notificationPermission = checkNotificationPermission();
        boolean alarmPermission = checkAlarmPermission();


        if(notificationPermission && alarmPermission){
            switchbutton.setChecked(true);
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
                switchbutton.setChecked(true);
                // Reindirizza l'utente alle impostazioni delle notifiche
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                alertDialog.dismiss();

                //Attivo il bottone per andare avanti
                AvanzaButton.setEnabled(true);
                AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                AvanzaButton.setTextColor(getResources().getColor(R.color.BluFisio));
                AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

                //Disattivo il bottone per attivare le notifiche in seguito
                AttivaInSeguitoButton.setEnabled(false);
                AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.white));
                AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));
            });

            negativeButton.setOnClickListener(v -> {
                switchbutton.setChecked(false);
                // Reindirizza l'utente alle impostazioni delle sveglie
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                alertDialog.dismiss();

                //Disattivo il bottone per andare avanti
                AvanzaButton.setEnabled(false);
                AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                AvanzaButton.setTextColor(getResources().getColor(R.color.white));
                AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

                //Attivo il bottone per attivare le notifiche in seguito
                AttivaInSeguitoButton.setEnabled(true);
                AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.BluFisio));
                AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));


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
                switchbutton.setChecked(true);
                // Reindirizza anche alla schermata delle sveglie e degli allarmi (Android 12+)
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                alertDialog.dismiss();

                //Attivo il bottone per andare avanti
                AvanzaButton.setEnabled(true);
                AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                AvanzaButton.setTextColor(getResources().getColor(R.color.BluFisio));
                AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

                //Disattivo il bottone per attivare le notifiche in seguito
                AttivaInSeguitoButton.setEnabled(false);
                AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.white));
                AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

            });

            negativeButton.setOnClickListener(v -> {
                switchbutton.setChecked(false);
                // Reindirizza l'utente alle impostazioni delle notifiche
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
                alertDialog.dismiss();

                //Disattivo il bottone per andare avanti
                AvanzaButton.setEnabled(false);
                AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                AvanzaButton.setTextColor(getResources().getColor(R.color.white));
                AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

                //Attivo il bottone per attivare le notifiche in seguito
                AttivaInSeguitoButton.setEnabled(true);
                AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.BluFisio));
                AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
            });

        } else {
            switchbutton.setChecked(false);
            switchbutton.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
        }
    }





    // Metodo per controllare se i permessi sono già stati concessi e aggiornare lo stato dello switch
    private void checkPermissionsAndSetSwitch() {
        boolean notificationPermission = checkNotificationPermission();
        boolean alarmPermission = checkAlarmPermission();

        if (notificationPermission && alarmPermission) {
            switchbutton.setChecked(true);
            switchbutton.setTextColor(getResources().getColor(R.color.white));


            //Abilito il bottone per andare avanti
            AvanzaButton.setEnabled(true);
            AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            AvanzaButton.setTextColor(getResources().getColor(R.color.BluFisio));
            AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

            //Disabilito il bottone per attivare le notifiche in seguito
            AttivaInSeguitoButton.setEnabled(false);
            AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
            AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.white));
            AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));



            Log.d("ImpostazioniActivity", "Permessi attivi, switch attivato");
        } else {
            switchbutton.setChecked(false);
            switchbutton.setTextColor(getResources().getColor(R.color.GrigioDisattivo));
            Log.d("ImpostazioniActivity", "Permessi non attivi, switch disattivato");

            //Disabilito il bottone per andare avanti
            AvanzaButton.setEnabled(false);
            AvanzaButton.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
            AvanzaButton.setTextColor(getResources().getColor(R.color.white));
            AvanzaButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

            //Abilito il bottone per attivare le notifiche in seguito
            AttivaInSeguitoButton.setEnabled(true);
            AttivaInSeguitoButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));
            AttivaInSeguitoButton.setTextColor(getResources().getColor(R.color.BluFisio));
            AttivaInSeguitoButton.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

        }
    }

    // Controlla il permesso per le notifiche (Android 13+)
    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
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

    // Richiede i permessi per notifiche e sveglie
    private void requestNotificationAndAlarmPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());

            startActivity(intent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));  // Porta l'utente direttamente alla schermata delle impostazioni della tua app
            startActivity(intent);

        }

    }

    // Gestione del risultato della richiesta di permessi
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("ImpostazioniActivity", "Permesso per le notifiche concesso");
                checkPermissionsAndSetSwitch(); // Aggiorna lo stato dello switch
            } else {
                Log.d("ImpostazioniActivity", "Permesso per le notifiche negato");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}