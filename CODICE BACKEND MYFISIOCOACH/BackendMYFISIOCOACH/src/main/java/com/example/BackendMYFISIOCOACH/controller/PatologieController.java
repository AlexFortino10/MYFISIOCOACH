package com.example.BackendMYFISIOCOACH.controller;

import com.example.BackendMYFISIOCOACH.entità.Patologie;
import com.example.BackendMYFISIOCOACH.servizi.PatologieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/patologie")
public class PatologieController {

    @Autowired
    private PatologieService patologieService;

    @GetMapping("/nomi")
    public List<String> getNomePatologie() {
        return patologieService.getNomePatologie();
    }

    @GetMapping("/numero")
    public int getNumeroPatologieUtente(@RequestParam String email) {
        return patologieService.getNumeroPatologieUtente(email);
    }

    @GetMapping("/mancanti")
    public List<Patologie> getPatologieMancantiUtente(@RequestParam String email) {
        return patologieService.getPatologieMancantiUtente(email);
    }

    @GetMapping("/utente")
    public ArrayList<String> getPatologieUtente(@RequestParam String email) {
        return patologieService.getPatologieUtente(email);
    }

    @GetMapping("/immagine")
    public String getImmaginePatologia(@RequestParam String nomePatologia) {
        return patologieService.getImmaginePatologia(nomePatologia);
    }

    // Metodo per modificare l'email
    @PutMapping("/modificaemail")
    public CompletableFuture<ResponseEntity<Boolean>> updateEmail(@RequestParam String oldEmail,
            @RequestParam String newEmail) {
        return patologieService.updateEmail(oldEmail, newEmail).thenApply(ResponseEntity::ok);
    }

    // Metodo per aggiungere una patologia a un determinato utente
    @PostMapping("/aggiungi")
    public CompletableFuture<ResponseEntity<Boolean>> addPatologiaUtente(@RequestParam String email,
            @RequestParam String patologia) {
        return patologieService.addPatologiaUtente(email, patologia).thenApply(ResponseEntity::ok);
    }

    // Metodo per eliminare una patologia da un determinato utente
    @DeleteMapping("/elimina")
    public CompletableFuture<ResponseEntity<Boolean>> deletePatologiaUtente(@RequestParam String email,
            @RequestParam String patologia) {
        return patologieService.deletePatologiaUtente(email, patologia).thenApply(ResponseEntity::ok);
    }
}