package BackendServer;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiServiceGiorniAllenamento {

    //Metodo che ritora l'ultimo giorno di allenamento del mese
    @GET("/api/giorni-allenamento/ultimo")
    Call<Integer> getUltimoGiornoAllenamento(
            @Query("email") String email,
            @Query("mese") int mese
    );

    //Metodo per inserire un giorno di allenamento
    @POST("/api/giorni-allenamento/inserisci")
    Call<Boolean> inserisciGiornoAllenamento(
            @Query("email") String email,
            @Query("giorno") int giorno,
            @Query("mese") int mese
    );

    //Metodo per ottenere i giorni di allenamento di un utente di uno specifico mese
    @GET("/api/giorni-allenamento/allenamentiutente")
    Call<ArrayList<Integer>> getGiorniAllenamentoUtente(
            @Query("email") String email,
            @Query("mese") int mese
    );
}
