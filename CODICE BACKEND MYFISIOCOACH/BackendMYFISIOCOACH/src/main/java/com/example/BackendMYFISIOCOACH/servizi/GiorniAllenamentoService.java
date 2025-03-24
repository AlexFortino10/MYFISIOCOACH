package com.example.BackendMYFISIOCOACH.servizi;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.example.BackendMYFISIOCOACH.entità.GiorniAllenamento;
import com.example.BackendMYFISIOCOACH.repository.GiorniAllenamentoRepository;

import jakarta.transaction.Transactional;

@Service
public class GiorniAllenamentoService {

    private final GiorniAllenamentoRepository repository;

    public GiorniAllenamentoService(GiorniAllenamentoRepository repository) {
        this.repository = repository;
    }

    public void inserisciGiorniAllenamento(String email, int giorno, int mese) {
        GiorniAllenamento nuovoGiorno = new GiorniAllenamento();
        nuovoGiorno.setEmail(email);
        nuovoGiorno.setGiorno(giorno);
        nuovoGiorno.setMese(mese);
        repository.save(nuovoGiorno);
    }

    public Integer getUltimoGiornoAllenamento(String email, int mese) {
        return repository.findUltimoGiornoAllenamento(email, mese);
    }

    public int getNumeroMese(String mese) {
        return switch (mese.toLowerCase()) {
            case "gennaio" -> 1;
            case "febbraio" -> 2;
            case "marzo" -> 3;
            case "aprile" -> 4;
            case "maggio" -> 5;
            case "giugno" -> 6;
            case "luglio" -> 7;
            case "agosto" -> 8;
            case "settembre" -> 9;
            case "ottobre" -> 10;
            case "novembre" -> 11;
            case "dicembre" -> 12;
            default -> 0;
        };
    }

    // Metodo per inserire un giorno di allenamento
    @Transactional
    public CompletableFuture<Boolean> insertGiornoAllenamento(String email, int giorno, int mese) {
        return CompletableFuture.supplyAsync(() -> {
            repository.insertGiornoAllenamento(email, giorno, mese);
            return true;
        });
    }

    // Metodo per ottenere i giorni di allenamento di un utente di uno specifico
    // mese

    public ArrayList<Integer> getGiorniAllenamento(String email, int mese) {
        return repository.getGiorniAllenamento(email, mese);
    }
}