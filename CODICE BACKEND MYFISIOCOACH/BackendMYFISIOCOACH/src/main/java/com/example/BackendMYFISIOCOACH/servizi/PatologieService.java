package com.example.BackendMYFISIOCOACH.servizi;

import com.example.BackendMYFISIOCOACH.entità.Patologie;
import com.example.BackendMYFISIOCOACH.repository.PatologieRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class PatologieService {

    @Autowired
    private PatologieRepository patologieRepository;

    public List<String> getNomePatologie() {
        return patologieRepository.getNomePatologie();
    }

    public int getNumeroPatologieUtente(String email) {
        return patologieRepository.getPatologieUtente(email).size();
    }

    public List<Patologie> getPatologieMancantiUtente(String email) {
        return patologieRepository.getPatologieMancantiUtente(email);
    }

    public ArrayList<String> getPatologieUtente(String email) {
        return patologieRepository.getPatologieUtente(email);
    }

    public String getImmaginePatologia(String nomePatologia) {
        return patologieRepository.getImmaginePatologia(nomePatologia).orElse(null);
    }

    // Metodo per modificare l'email
    @Transactional
    public CompletableFuture<Boolean> updateEmail(String oldEmail, String newEmail) {
        return CompletableFuture
                .supplyAsync(() -> {
                    patologieRepository.updateEmail(oldEmail, newEmail);
                    return true;
                });
    }

    // Metodo per aggiungere una patologia a un determinato utente
    @Transactional
    public CompletableFuture<Boolean> addPatologiaUtente(String email, String patologia) {
        return CompletableFuture
                .supplyAsync(() -> {
                    patologieRepository.addPatologiaUtente(email, patologia);
                    return true;
                });
    }

    // Metodo per eliminare una patologia da un determinato utente
    @Transactional
    public CompletableFuture<Boolean> deletePatologiaUtente(String email, String patologia) {
        return CompletableFuture
                .supplyAsync(() -> {
                    patologieRepository.deletePatologiaUtente(email, patologia);
                    return true;
                });
    }
}
