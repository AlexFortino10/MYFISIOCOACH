package Llama;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LlamaService {
    // URL del server Railway
    private static final String SERVER_URL = "https://fitnessbackend-production-f767.up.railway.app/generate";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public String sendPromptToLlama(String prompt) throws IOException {
        // Configura OkHttpClient con timeout estesi
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)  // Timeout di connessione
                .readTimeout(300, TimeUnit.SECONDS)     // Timeout di lettura
                .writeTimeout(300, TimeUnit.SECONDS)    // Timeout di scrittura
                .build();

        // Crea il corpo della richiesta JSON (gestione sicura di caratteri speciali)
        String json = String.format("{\"prompt\": \"%s\"}", prompt.replace("\"", "\\\""));

        RequestBody body = RequestBody.create(json, JSON);

        // Crea la richiesta POST
        Request request = new Request.Builder()
                .url(SERVER_URL)
                .post(body)
                .build();

        // Invia la richiesta e gestisci la risposta
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();  // Risposta del server in caso di successo
            } else {
                return "Errore dal server: " + response.code() + " " + response.message();
            }
        } catch (IOException e) {
            throw new IOException("Errore durante la connessione al server: " + e.getMessage(), e);
        }
    }
}
