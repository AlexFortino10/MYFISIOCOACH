package BackendServer;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiServiceResocontoUtente {

    //Metodo per trovare il resento dell'utente tramite email
    @GET("/api/resoconto/findByEmail")
    Call<Boolean> findByEmail(
            @Query("email") String email
    );

    //Metodo per creare un resoconto
    @POST("/api/resoconto/creaResoconto")
    Call<Boolean> creaResoconto(
            @Query("email") String email
    );

    //Metodo per ottenere i minuti di un utente
    @GET("/api/resoconto/findMinutiByEmail")
    Call<Integer> findMinutiByEmail(
            @Query("email") String email
    );

    //Metodo per ottenere i secondi di un utente
    @GET("/api/resoconto/findSecondiByEmail")
    Call<Integer> findSecondiByEmail(
            @Query("email") String email
    );

    //Metodo per ottenere il numero di allenamenti di un utente
    @GET("/api/resoconto/findNumallenamentiByEmail")
    Call<Integer> findNumallenamentiByEmail(
            @Query("email") String email
    );

    //Metodo per ottenere il numero di serie di un utente
    @GET("/api/resoconto/findSerieByEmail")
    Call<Integer> findSerieByEmail(
            @Query("email") String email
    );

    //Metodo per ottenere il record personale di un utente
    @GET("/api/resoconto/findRecordpersonaleByEmail")
    Call<Integer> findRecordpersonaleByEmail(
            @Query("email") String email
    );

    //Metodo per aggiornare il resoconto di un utente
    @PUT("/api/resoconto/updateResoconto")
    Call<Boolean> updateResoconto(
            @Query("email") String email,
            @Query("minuti") int minuti,
            @Query("secondi") int secondi,
            @Query("numallenamenti") int numallenamenti,
            @Query("serie") int serie,
            @Query("recordpersonale") int recordpersonale
    );


}
