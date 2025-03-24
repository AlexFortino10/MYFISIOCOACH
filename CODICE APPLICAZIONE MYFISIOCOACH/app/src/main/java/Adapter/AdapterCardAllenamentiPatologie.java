package Adapter;

import static android.view.LayoutInflater.from;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myfisiocoach.AllenamentoGiornalieroActivity;
import com.example.myfisiocoach.R;

import java.util.ArrayList;

import Model.Patologie;

public class AdapterCardAllenamentiPatologie extends RecyclerView.Adapter<AdapterCardAllenamentiPatologie.Viewholder> {
    private ArrayList<Patologie> items;
    private Context context;

    // Cache in memoria per le immagini
    private LruCache<String, String> immaginiCache;

    public AdapterCardAllenamentiPatologie(ArrayList<Patologie> items) {
        this.items = items;
        int cacheSize = 5 * 1024 * 1024; // 5 MB di cache
        immaginiCache = new LruCache<>(cacheSize);
    }

    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = from(parent.getContext()).inflate(R.layout.card_allenamentopatologie, parent, false);
        context = parent.getContext();
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        Patologie currentPatologia = items.get(position);
        String patologia = currentPatologia.getPatologia();
        holder.TextAllenamentoPatologie.setText(patologia);

        // Controlla la cache
        String cachedUrl = immaginiCache.get(patologia);
        if (cachedUrl != null) {
            Glide.with(context).load(cachedUrl).into(holder.imageAllenamentoPatologie);
        } else {
            // Carica l'immagine dalla cache persistente (SharedPreferences)
            SharedPreferences sharedPreferences = context.getSharedPreferences("ImmaginiPatologieCache", Context.MODE_PRIVATE);
            String urlPersistente = sharedPreferences.getString(patologia, null);

            if (urlPersistente != null) {
                Glide.with(context).load(urlPersistente).into(holder.imageAllenamentoPatologie);
                immaginiCache.put(patologia, urlPersistente); // Aggiorna la cache in memoria
            }
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AllenamentoGiornalieroActivity.class);
            intent.putExtra("email", currentPatologia.getEmailpaziente());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView TextAllenamentoPatologie;
        ImageView imageAllenamentoPatologie;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            TextAllenamentoPatologie = itemView.findViewById(R.id.textViewallenamentopatologia);
            imageAllenamentoPatologie = itemView.findViewById(R.id.imageViewAllenamentiPatologie);
        }
    }
}
