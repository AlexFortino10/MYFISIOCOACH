package Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfisiocoach.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import BackendServer.ApiServiceGiorniAllenamento;
import Daos.GiorniAllenamentoDao;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> daysOfMonth;
    private final OnItemListener onItemListener;
    private final String Mese,email;
    private GiorniAllenamentoDao giorniAllenamentoDao = new GiorniAllenamentoDao();
    ArrayList<Integer> giorniAllenamento = null;

    int numeromese = 0;

    ApiServiceGiorniAllenamento apiServiceGiorniAllenamento;

    public CalendarAdapter(ArrayList<String> daysOfMonth, String Mese,String email,OnItemListener onItemListener)
    {
        this.daysOfMonth = daysOfMonth;
        this.onItemListener = onItemListener;
        this.Mese = Mese;
        this.email = email;
        String mese = Mese.substring(0,Mese.length()-5);
        numeromese = giorniAllenamentoDao.getNumeroMese(mese);
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_day, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
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

        apiServiceGiorniAllenamento = retrofit.create(ApiServiceGiorniAllenamento.class);

        apiServiceGiorniAllenamento.getGiorniAllenamentoUtente(email,numeromese).enqueue(new Callback<ArrayList<Integer>>() {
            @Override
            public void onResponse(Call<ArrayList<Integer>> call, Response<ArrayList<Integer>> response) {
                giorniAllenamento = response.body();

                holder.dayOfMonth.setText(daysOfMonth.get(position));
                for (int i = 0; i < giorniAllenamento.size(); i++)
                {
                    if ((holder.dayOfMonth.getText().equals(String.valueOf(giorniAllenamento.get(i)))))
                    {
                        holder.dayOfMonth.setTextColor(holder.itemView.getResources().getColor(R.color.white));
                        holder.dayOfMonth.setBackgroundResource(R.drawable.background_giorni_allenamento);
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Integer>> call, Throwable t) {
                throw new RuntimeException(t);
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return daysOfMonth.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}