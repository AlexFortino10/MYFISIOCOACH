package com.example.BackendMYFISIOCOACH.controller;

import com.example.BackendMYFISIOCOACH.entità.ResocontoUtente;
import com.example.BackendMYFISIOCOACH.servizi.ResocontoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/resoconto")
public class ResocontoController {

    @Autowired
    private ResocontoService resocontoService;

    // Metodo per trovare il resoconto di un utente tramite l'email
    @GetMapping("/findByEmail")
    public CompletableFuture<ResponseEntity<Boolean>> findByEmail(@RequestParam String email) {
        return resocontoService.findByEmail(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per creare un resoconto
    @PostMapping("/creaResoconto")
    public CompletableFuture<ResponseEntity<Boolean>> creaResoconto(@RequestParam String email) {
        return resocontoService.creaResoconto(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per ottenere i minuti di un utente
    @GetMapping("/findMinutiByEmail")
    public CompletableFuture<ResponseEntity<Integer>> findMinutiByEmail(@RequestParam String email) {
        return resocontoService.findMinutiByEmail(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per ottenere i secondi di un utente
    @GetMapping("/findSecondiByEmail")
    public CompletableFuture<ResponseEntity<Integer>> findSecondiByEmail(@RequestParam String email) {
        return resocontoService.findSecondiByEmail(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per ottenere il numero di allenamenti di un utente
    @GetMapping("/findNumallenamentiByEmail")
    public CompletableFuture<ResponseEntity<Integer>> findNumallenamentiByEmail(@RequestParam String email) {
        return resocontoService.findNumallenamentiByEmail(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per ottenere il numero di serie di un utente
    @GetMapping("/findSerieByEmail")
    public CompletableFuture<ResponseEntity<Integer>> findSerieByEmail(@RequestParam String email) {
        return resocontoService.findSerieByEmail(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per ottenere il record personale di un utente
    @GetMapping("/findRecordpersonaleByEmail")
    public CompletableFuture<ResponseEntity<Integer>> findRecordpersonaleByEmail(@RequestParam String email) {
        return resocontoService.findRecordpersonaleByEmail(email).thenApply(ResponseEntity::ok);
    }

    // Metodo per aggiornare il resoconto di un utente
    @PutMapping("/updateResoconto")
    public CompletableFuture<ResponseEntity<Boolean>> updateResoconto(@RequestParam String email,
            @RequestParam int minuti,
            @RequestParam int secondi, @RequestParam int numallenamenti, @RequestParam int serie,
            @RequestParam int recordpersonale) {
        return resocontoService.updateResoconto(email, minuti, secondi, numallenamenti, serie, recordpersonale)
                .thenApply(ResponseEntity::ok);
    }

}