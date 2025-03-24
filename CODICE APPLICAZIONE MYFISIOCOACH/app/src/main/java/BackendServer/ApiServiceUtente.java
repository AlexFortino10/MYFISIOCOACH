package BackendServer;
import java.util.concurrent.CompletableFuture;

import Model.Utente;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiServiceUtente {

    // Verifica utente con email e password
    @POST("api/utente/verifica")
    Call<Boolean> verificaUtente(
            @Query("email") String email,
            @Query("password") String password
    );

    // Modifica la password di un utente
    @PUT("api/utente/modificaPassword")
    Call<Boolean> modificaPassword(
            @Query("email") String email,
            @Query("nuovapassword") String nuovapassword
    );

    //Accedo alla modifica della password
    @POST("api/utente/datidiVerifica")
    Call<Boolean> AccessToModificaPassword(
            @Query("email") String email,
            @Query("nome") String nome,
            @Query("cognome") String cognome
    );

    //Ottengo il nome dell'utente
    @GET("api/utente/nome")
    Call<String> getNomeUtente(
            @Query("email") String email
    );

    //Ottengo lo stage dell'utente
    @GET("api/utente/stage")
    Call<Integer> getStageUtente(
            @Query("email") String email
    );

    //Modifica lo stage dell'utente
    @PUT("api/utente/modificaStage")
    Call<Boolean> modificaStageUtente(
            @Query("email") String email,
            @Query("stage") int stage
    );


    //Ottengo il cognome dell'utente
    @GET("api/utente/cognome")
    Call<String> getCognomeUtente(
            @Query("email") String email
    );

    //Ottengo l'età dell'utente
    @GET("api/utente/eta")
    Call<Integer> getEtaUtente(
            @Query("email") String email
    );

    //Ottengo il peso dell'utente
    @GET("api/utente/peso")
    Call<Integer> getPesoUtente(
            @Query("email") String email
    );

    //Ottengo l'altezza dell'utente
    @GET("api/utente/altezza")
    Call<Integer> getAltezzaUtente(
            @Query("email") String email
    );

    //Ottengo il sesso dell'utente
    @GET("api/utente/sesso")
    Call<String> getSessoUtente(
            @Query("email") String email
    );

    //Ottengo la password dell'utente

    @GET("api/utente/password")
    Call<String> getPasswordUtente(
            @Query("email") String email
    );

    //Modifico email e password dell'utente
    @PUT("api/utente/modificaEmailPassword")
    Call<Boolean> modificaEmailPassword(
            @Query("email") String email,
            @Query("nuovaemail") String nuovaemail,
            @Query("nuovapassword") String nuovapassword
    );

    //Verifico l'email dell'utente
    @GET("api/utente/verificaEmail")
    Call<Boolean> verificaEmail(
            @Query("email") String email
    );

    //Registrazione di un nuovo utente
    @POST("api/utente/registraUtente")
    Call<Boolean> registraUtente(
            @Query("email") String email,
            @Query("password") String password,
            @Query("nome") String nome,
            @Query("cognome") String cognome,
            @Query("peso") int peso,
            @Query("altezza") int altezza,
            @Query("eta") int eta,
            @Query("sesso") String sesso
    );


}

