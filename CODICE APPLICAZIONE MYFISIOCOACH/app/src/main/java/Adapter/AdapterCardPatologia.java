package Adapter;

import static android.view.LayoutInflater.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfisiocoach.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import BackendServer.ApiServicePatologie;
import Model.Patologie;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdapterCardPatologia extends RecyclerView.Adapter<AdapterCardPatologia.Viewholder> {
    ArrayList<Patologie> items;
    Context context;

    ApiServicePatologie apiServicePatologie;
    Boolean selezionata = false;

    private Set<String> patologieSelezionate = new HashSet<>(); // Set per tracciare le patologie selezionate


    public AdapterCardPatologia(ArrayList<Patologie>items) {
        this.items = items;
    }


    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = from(parent.getContext()).inflate(R.layout.card_patologie,parent,false);
        context = parent.getContext();
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, @SuppressLint("RecyclerView") int position) {
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
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiServicePatologie = retrofitPatologie.create(ApiServicePatologie.class);


        // Imposta il testo del TextView con il nome della patologia
        holder.TextPatologia.setText(items.get(position).getPatologia());
        if (patologieSelezionate.contains(items.get(position).getPatologia())) {
            //Cambia stilizzazione della card
            holder.itemView.setBackgroundResource(R.drawable.card_patologie_selezionate);
            //Cambio colore del testo
            holder.TextPatologia.setTextColor(context.getResources().getColor(R.color.white));
            //Cambio drawableEnd
            holder.TextPatologia.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_cancel_24,0);
        } else {
            //Cambia stilizzazione della card
            holder.itemView.setBackgroundResource(R.drawable.cardpatologia);
            //Cambio colore del testo
            holder.TextPatologia.setTextColor(context.getResources().getColor(R.color.BluFisio));
            //Cambio drawableEnd
            holder.TextPatologia.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.check,0);
        }




        //Vado a gestire l'aggiunta o la rimozione della patologia dalla lista di patologie dell'utente
        //Le patologie se selezionate vengono aggiunte al database altrimenti se deselezionate vengono rimosse
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!patologieSelezionate.contains(items.get(position).getPatologia())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                    builder.setView(dialogView);
                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    title.setText("Attenzione");
                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                    title.setCompoundDrawablePadding(10);
                    message.setText("Vuoi aggiungere la patologia alla tua lista di patologie?");
                    positiveButton.setText("Si");
                    negativeButton.setText("No");
                    negativeButton.setOnClickListener(v1 -> {
                        alertDialog.dismiss();
                    });
                    positiveButton.setOnClickListener(v1 -> {
                        //Aggiungo la patologia al server
                        apiServicePatologie.aggiungiPatologia(items.get(position).getEmailpaziente(), items.get(position).getPatologia()).enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if (response.isSuccessful() && response.body() != null && response.body()) {
                                    //Cambia stilizzazione della card
                                    holder.itemView.setBackgroundResource(R.drawable.card_patologie_selezionate);
                                    //Cambio colore del testo
                                    holder.TextPatologia.setTextColor(context.getResources().getColor(R.color.white));
                                    //Cambio drawableEnd
                                    holder.TextPatologia.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_cancel_24,0);
                                    //Aggiungo la patologia alla lista delle patologie selezionate
                                    patologieSelezionate.add(items.get(position).getPatologia());
                                } else {
                                    alertDialog.show();
                                    title.setText("Errore");
                                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                                    title.setCompoundDrawablePadding(10);
                                    message.setText("Errore durante l'aggiunta della patologia");
                                    positiveButton.setText("Ok");
                                    negativeButton.setVisibility(View.GONE);
                                    positiveButton.setOnClickListener(v2 -> {
                                        alertDialog.dismiss();
                                    });
                                }
                                alertDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                alertDialog.show();
                                title.setText("Errore");
                                title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                                title.setCompoundDrawablePadding(10);
                                message.setText("Errore durante l'aggiunta della patologia");
                                positiveButton.setText("Ok");
                                negativeButton.setVisibility(View.GONE);
                                positiveButton.setOnClickListener(v2 -> {
                                    alertDialog.dismiss();
                                });
                                alertDialog.dismiss();
                            }
                        });
                    });

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.alert_dialog, null);
                    builder.setView(dialogView);
                    TextView title = dialogView.findViewById(R.id.Titlealertdialog);
                    @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView message = dialogView.findViewById(R.id.alertdialogMessage);
                    Button positiveButton = dialogView.findViewById(R.id.PositiveButton);
                    Button negativeButton = dialogView.findViewById(R.id.NegativeButton);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    title.setText("Attenzione");
                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_information_outline_32,0);
                    title.setCompoundDrawablePadding(10);
                    message.setText("Vuoi rimuovere la patologia dalla tua lista di patologie?");
                    positiveButton.setText("Si");
                    negativeButton.setText("No");
                    negativeButton.setOnClickListener(v1 -> {
                        alertDialog.dismiss();
                    });
                    positiveButton.setOnClickListener(v1 -> {
                        //Rimuovo la patologia dal server
                        apiServicePatologie.eliminaPatologia(items.get(position).getEmailpaziente(), items.get(position).getPatologia()).enqueue(new Callback<Boolean>() {
                            @Override
                            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                                if (response.isSuccessful() && response.body() != null && response.body()) {
                                    //Cambia stilizzazione della card
                                    holder.itemView.setBackgroundResource(R.drawable.cardpatologia);
                                    //Cambio colore del testo
                                    holder.TextPatologia.setTextColor(context.getResources().getColor(R.color.BluFisio));
                                    //Cambio drawableEnd
                                    holder.TextPatologia.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.check,0);
                                    //Rimuovo la patologia dalla lista delle patologie selezionate
                                    patologieSelezionate.remove(items.get(position).getPatologia());
                                } else {
                                    alertDialog.show();
                                    title.setText("Errore");
                                    title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                                    title.setCompoundDrawablePadding(10);
                                    message.setText("Errore durante la rimozione della patologia");
                                    positiveButton.setText("Ok");
                                    negativeButton.setVisibility(View.GONE);
                                    positiveButton.setOnClickListener(v2 -> {
                                        alertDialog.dismiss();
                                    });
                                }
                                alertDialog.dismiss();
                            }

                            @Override
                            public void onFailure(Call<Boolean> call, Throwable t) {
                                alertDialog.show();
                                title.setText("Errore");
                                title.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.baseline_error_32,0);
                                title.setCompoundDrawablePadding(10);
                                message.setText("Errore durante la rimozione della patologia");
                                positiveButton.setText("Ok");
                                negativeButton.setVisibility(View.GONE);
                                positiveButton.setOnClickListener(v2 -> {
                                    alertDialog.dismiss();
                                });
                                alertDialog.dismiss();
                            }
                        });
                    });
                }
            }
        });
    }

    @Override

    public int getItemCount() {return items.size();}

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView TextPatologia;
        public Viewholder(@NonNull View itemView) {
            super(itemView);
            TextPatologia = itemView.findViewById(R.id.textViewPatologia);
        }
    }
}
