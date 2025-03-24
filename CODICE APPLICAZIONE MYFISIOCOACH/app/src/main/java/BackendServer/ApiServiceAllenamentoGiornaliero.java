package BackendServer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiServiceAllenamentoGiornaliero {

    //Metodo per ottenere l'ora dell'allenamento giornaliero
    @GET("/api/allenamento/ora")
    Call<Integer> getOraAllenamentoGiornaliero(
            @Query("giorno") int giorno,
            @Query("numeroAllenamento") int numeroAllenamento
    );

    //Metodo per ottenere i minuti dell'allenamento giornaliero
    @GET("/api/allenamento/minuti")
    Call<Integer> getMinutiAllenamento(
            @Query("giorno") int giorno,
            @Query("numeroAllenamento") int numeroAllenamento
    );

    //Metodo per ottenere le istruzioni dell'allenamento giornaliero
    @GET("/api/allenamento/istruzioni")
    Call<String> getIstruzioniAllenamento(
            @Query("giorno") int giorno,
            @Query("numeroAllenamento") int numeroAllenamento
    );

    //Metodo per ottenere i benefici dell'allenamento giornaliero
    @GET("/api/allenamento/benefici")
    Call<String> getBeneficiAllenamento(
            @Query("giorno") int giorno,
            @Query("numeroAllenamento") int numeroAllenamento
    );

    //Metodo per ottenere il video dell'allenamento giornaliero
    @GET("/api/allenamento/video")
    Call<String> getVideoAllenamento(
            @Query("giorno") int giorno,
            @Query("numeroAllenamento") int numeroAllenamento
    );
}
