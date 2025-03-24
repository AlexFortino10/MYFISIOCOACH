package com.example.BackendMYFISIOCOACH.servizi;

import com.example.BackendMYFISIOCOACH.entità.ResocontoUtente;
import com.example.BackendMYFISIOCOACH.repository.ResocontoRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class ResocontoService {
    @Autowired
    private ResocontoRepository resocontoRepository;

    // Metodo per trovare il resoconto di un utente tramite l'email
    public CompletableFuture<Boolean> findByEmail(String email) {
        return CompletableFuture
                .supplyAsync(() -> resocontoRepository.findByEmail(email).isPresent());
    }

    // Metodo per creare un resoconto
    @Transactional
    public CompletableFuture<Boolean> creaResoconto(String email) {
        return CompletableFuture
                .supplyAsync(() -> {
                    resocontoRepository.creaResoconto(email);
                    return true;
                });
    }

    // Metodo per ottenere i minuti di un utente
    public CompletableFuture<Integer> findMinutiByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> resocontoRepository.findMinutiByEmail(email));
    }

    // Metodo per ottenere i secondi di un utente
    public CompletableFuture<Integer> findSecondiByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> resocontoRepository.findSecondiByEmail(email));
    }

    // Metodo per ottenere il numero di allenamenti di un utente
    public CompletableFuture<Integer> findNumallenamentiByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> resocontoRepository.findNumallenamentiByEmail(email));
    }

    // Metodo per ottenere il numero di serie di un utente
    public CompletableFuture<Integer> findSerieByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> resocontoRepository.findSerieByEmail(email));
    }

    // Metodo per ottenere il record personale di un utente
    public CompletableFuture<Integer> findRecordpersonaleByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> resocontoRepository.findRecordpersonaleByEmail(email));
    }

    // Metodo per aggiornare il resoconto di un utente
    @Transactional
    public CompletableFuture<Boolean> updateResoconto(String email, int minuti, int secondi, int numallenamenti,
            int serie,
            int recordpersonale) {
        return CompletableFuture
                .supplyAsync(() -> {
                    resocontoRepository.updateResoconto(email, minuti, secondi, numallenamenti, serie, recordpersonale);
                    return true;
                });
    }

}
