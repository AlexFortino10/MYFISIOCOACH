package com.example.BackendMYFISIOCOACH.servizi;

import com.example.BackendMYFISIOCOACH.repository.UtenteRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class UtenteService {

    @Autowired
    private UtenteRepository utenteRepository;

    // metodo per verificare l'utente
    public CompletableFuture<Boolean> verificaUtente(String email, String password) {
        return CompletableFuture
                .supplyAsync(() -> utenteRepository.findByEmailAndPassword(email, password).isPresent());
    }

    // Nuovo metodo per aceddere alla modifica della password
    public CompletableFuture<Boolean> AccessoModificaPassword(String email, String nome, String cognome) {
        return CompletableFuture
                .supplyAsync(() -> utenteRepository.findByDatiDiVerifica(email, nome, cognome).isPresent());
    }

    // Nuovo metodo per verificare l'email dell'utente
    public CompletableFuture<Boolean> verificaEmail(String email) {
        return CompletableFuture
                .supplyAsync(() -> utenteRepository.findByEmail(email).isPresent());
    }

    // Nuovo metodo per modificare la password
    @Transactional
    public CompletableFuture<Boolean> modificaPassword(String email, String nuovapassword) {
        return CompletableFuture
                .supplyAsync(() -> {
                    utenteRepository.modificaPassword(email, nuovapassword);
                    return true;
                });
    }

    // Nuovo metodo per ottenere il nome dell'utente
    public CompletableFuture<String> findNomeByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findNomeByEmail(email));
    }

    // Metodo per ottenere lo stage dell'utente
    public CompletableFuture<Integer> findStageByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findStageByEmail(email));
    }

    // Metodo per modificare lo stage
    @Transactional
    public CompletableFuture<Boolean> modificaStage(String email, int stage) {
        return CompletableFuture
                .supplyAsync(() -> {
                    utenteRepository.modificaStage(email, stage);
                    return true;
                });
    }

    // Metodo per ottenere il cognome dell'utente
    public CompletableFuture<String> findCognomeByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findCognomeByEmail(email));
    }

    // Metodo per ottenere il peso dell'utente
    public CompletableFuture<Integer> findPesoByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findPesoByEmail(email));
    }

    // Metodo per ottenere l'altezza dell'utente
    public CompletableFuture<Integer> findAltezzaByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findAltezzaByEmail(email));
    }

    // Metodo per ottenere l'età dell'utente
    public CompletableFuture<Integer> findEtaByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findEtaByEmail(email));
    }

    // Metodo per ottenere il sesso dell'utente
    public CompletableFuture<String> findSessoByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findSessoByEmail(email));
    }

    // Metodo per ottenere la password dell'utente
    public CompletableFuture<String> findPasswordByEmail(String email) {
        return CompletableFuture.supplyAsync(() -> utenteRepository.findPasswordByEmail(email));
    }

    // Metodo per modificare email e password dell'utente
    @Transactional
    public CompletableFuture<Boolean> modificaEmailPassword(String email, String nuovaemail, String nuovapassword) {
        return CompletableFuture
                .supplyAsync(() -> {
                    utenteRepository.modificaEmailPassword(email, nuovaemail, nuovapassword);
                    return true;
                });
    }

    // Metodo per registrare un nuovo utente con stage 1
    @Transactional
    public CompletableFuture<Boolean> registraUtente(String email, String password, String nome, String cognome,
            int peso,
            int altezza, int eta, String sesso) {
        return CompletableFuture
                .supplyAsync(() -> {
                    utenteRepository.registraUtente(email, password, nome, cognome, peso, altezza, eta, sesso);
                    return true;
                });
    }

}
