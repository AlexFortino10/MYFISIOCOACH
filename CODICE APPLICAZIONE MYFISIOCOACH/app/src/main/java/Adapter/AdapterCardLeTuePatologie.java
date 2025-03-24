package Adapter;

import static android.view.LayoutInflater.from;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfisiocoach.R;

import java.util.ArrayList;
import Model.Patologie;

public class AdapterCardLeTuePatologie extends RecyclerView.Adapter<AdapterCardLeTuePatologie.Viewholder>{
    ArrayList<Patologie> items;
    Context context;

    public AdapterCardLeTuePatologie(ArrayList<Patologie>items) {
        this.items = items;
    }


    @NonNull
    @Override
    public AdapterCardLeTuePatologie.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = from(parent.getContext()).inflate(R.layout.card_patologie,parent,false);
        context = parent.getContext();
        return new AdapterCardLeTuePatologie.Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCardLeTuePatologie.Viewholder holder, @SuppressLint("RecyclerView") int position) {
        // Imposta il testo del TextView con il nome della patologia
        holder.TextPatologia.setText(items.get(position).getPatologia());
            //Cambia stilizzazione della card
            holder.itemView.setBackgroundResource(R.drawable.card_patologie_selezionate);
            //Cambio colore del testo
            holder.TextPatologia.setTextColor(context.getResources().getColor(R.color.white));
            //Cambio drawableEnd
            holder.TextPatologia.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
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
