package BackendServer;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiServicePatologie {

    //Ritorna la foto della patologia
    @GET("api/patologie/immagine")
    Call<String> getImmaginePatologia(
            @Query("nomePatologia") String patologia
    );

    //Ritorna le patologie dell'utente
    @GET("api/patologie/utente")
    Call<ArrayList<String>> getPatologieUtente(
            @Query("email") String email
    );

    //Ritorna tutte le patologie
    @GET("api/patologie/nomi")
    Call<List<String>> getNomePatologie();

    //Modifica l'email dell'utente
    @PUT("api/patologie/modificaemail")
    Call<Boolean> modificaEmailUtente(
            @Query("oldEmail") String oldEmail,
            @Query("newEmail") String newEmail
    );

    //Aggiungi una patologia all'utente
    @POST("api/patologie/aggiungi")
    Call<Boolean> aggiungiPatologia(
            @Query("email") String email,
            @Query("patologia") String patologia
    );

    //Rimuovi una patologia all'utente
    @DELETE("api/patologie/elimina")
    Call<Boolean> eliminaPatologia(
            @Query("email") String email,
            @Query("patologia") String patologia
    );
}
