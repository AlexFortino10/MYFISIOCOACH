package com.example.myfisiocoach;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AttivaMicrofonoActivity extends AppCompatActivity {


    ImageView tornaIndietro;

    Button Avanza,Attivainseguito;

    Switch switchbuttonMicrofono;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attiva_microfono);
        //Recuper l'email dal activity precedente
        String email = getIntent().getStringExtra("email");




        //setto il bottone per tornare indietro
        tornaIndietro = findViewById(R.id.tornaMicrofonoIndietroButton);

        //setto il listener per tornare indietro
        tornaIndietro.setOnClickListener(v -> {

            //Se le notifiche o gli allarmi non sono attivi allora torno alla pagina per attivarle, altimenti torno al login
            if (!checkNotificationPermission() || !checkAlarmPermission()) {
                Intent intent = new Intent(AttivaMicrofonoActivity.this, AttivaNotifiche.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            } else {
                Intent intent = new Intent(AttivaMicrofonoActivity.this, LoginActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }

        });

        //setto il bottone per andare avanti
        Avanza = findViewById(R.id.avanzaButtonMicorfono);
        Avanza.setEnabled(false);

        //setto il listener per andare avanti nella hompage
        Avanza.setOnClickListener(v -> {
            Intent intent = new Intent(AttivaMicrofonoActivity.this, HomepageActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });

        //setto il bottone per attivare in seguito
        Attivainseguito = findViewById(R.id.AttivaInSeguitoMicrofonoButton);
        Attivainseguito.setEnabled(true);

        //setto il listener per attivare in seguito e andare alla homepage
        Attivainseguito.setOnClickListener(v -> {
            Intent intent = new Intent(AttivaMicrofonoActivity.this, HomepageActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });


        //setto lo switch per attivare il microfono

        switchbuttonMicrofono = findViewById(R.id.switchbuttonMicrofono);


        //setto il listener per lo switch

        switchbuttonMicrofono.setOnCheckedChangeListener((buttonView, isCheckedMicrofono) -> {
            if (isCheckedMicrofono) {
                //Abilito il bottone per andare avanti
                Avanza.setEnabled(true);
                Avanza.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                Avanza.setTextColor(getResources().getColor(R.color.BluFisio));
                Avanza.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

                //Disabilito il bottone per attivare il microfono in seguito
                Attivainseguito.setEnabled(false);
                Attivainseguito.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                Attivainseguito.setTextColor(getResources().getColor(R.color.white));
                Attivainseguito.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));


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
                            //Disabilito il bottone per andare avanti
                            Avanza.setEnabled(false);
                            Avanza.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                            Avanza.setTextColor(getResources().getColor(R.color.white));
                            Avanza.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

                            //Abilito il bottone per attivare il microfono in seguito
                            Attivainseguito.setEnabled(true);
                            Attivainseguito.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                            Attivainseguito.setTextColor(getResources().getColor(R.color.BluFisio));
                            Attivainseguito.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));
                        });

                        positiveButton.setOnClickListener(v1 -> {
                            alertDialog.dismiss();
                            // Richiedi i permessi per il microfono
                            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                        });


                    }
                }


            } else {

                //Disabilito il bottone per andare avanti
                Avanza.setEnabled(false);
                Avanza.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                Avanza.setTextColor(getResources().getColor(R.color.white));
                Avanza.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));

                //Abilito il bottone per attivare il microfono in seguito
                Attivainseguito.setEnabled(true);
                Attivainseguito.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                Attivainseguito.setTextColor(getResources().getColor(R.color.BluFisio));
                Attivainseguito.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));





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
                        negativeButton.setOnClickListener(v1 -> {
                            alertDialog.dismiss();
                            switchbuttonMicrofono.setChecked(true);
                            switchbuttonMicrofono.setTextColor(getResources().getColor(R.color.white));

                            //Abilito il bottone per andare avanti
                            Avanza.setEnabled(true);
                            Avanza.setBackgroundTintList(getResources().getColorStateList(R.color.white));
                            Avanza.setTextColor(getResources().getColor(R.color.BluFisio));
                            Avanza.setCompoundDrawableTintList(getResources().getColorStateList(R.color.BluFisio));

                            //Disabilito il bottone per attivare il microfono in seguito
                            Attivainseguito.setEnabled(false);
                            Attivainseguito.setBackgroundTintList(getResources().getColorStateList(R.color.GrigioDisattivo));
                            Attivainseguito.setTextColor(getResources().getColor(R.color.white));
                            Attivainseguito.setCompoundDrawableTintList(getResources().getColorStateList(R.color.white));




                        });
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


    @Override
    protected void onResume() {
        super.onResume();
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