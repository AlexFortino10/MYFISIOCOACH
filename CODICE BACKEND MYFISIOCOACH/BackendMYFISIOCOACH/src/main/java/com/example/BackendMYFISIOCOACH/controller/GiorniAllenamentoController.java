package com.example.BackendMYFISIOCOACH.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendMYFISIOCOACH.servizi.GiorniAllenamentoService;

@RestController
@RequestMapping("/api/giorni-allenamento")
public class GiorniAllenamentoController {

    private final GiorniAllenamentoService service;

    public GiorniAllenamentoController(GiorniAllenamentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<String> inserisciGiorniAllenamento(@RequestParam String email, @RequestParam int giorno,
            @RequestParam int mese) {
        service.inserisciGiorniAllenamento(email, giorno, mese);
        return ResponseEntity.ok("Giorno di allenamento inserito con successo!");
    }

    @GetMapping("/ultimo")
    public ResponseEntity<Integer> getUltimoGiornoAllenamento(@RequestParam String email, @RequestParam int mese) {
        Integer ultimoGiorno = service.getUltimoGiornoAllenamento(email, mese);
        return ResponseEntity.ok(ultimoGiorno);
    }

    @GetMapping
    public ResponseEntity<List<Integer>> getGiorniAllenamento(@RequestParam String email, @RequestParam int mese) {
        List<Integer> giorni = service.getGiorniAllenamento(email, mese);
        return ResponseEntity.ok(giorni);
    }

    @GetMapping("/mese")
    public ResponseEntity<Integer> getNumeroMese(@RequestParam String mese) {
        int numeroMese = service.getNumeroMese(mese);
        return ResponseEntity.ok(numeroMese);
    }

    // Metodo per inserire un giorno di allenamento
    @PostMapping("/inserisci")
    public CompletableFuture<ResponseEntity<Boolean>> insertGiornoAllenamento(@RequestParam String email,
            @RequestParam int giorno,
            @RequestParam int mese) {
        return service.insertGiornoAllenamento(email, giorno, mese).thenApply(ResponseEntity::ok);
    }

    // Metodo per ottenere i giorni di allenamento di un utente di uno specifico
    // mese
    @GetMapping("/allenamentiutente")
    public ArrayList<Integer> getGiorniAllenamentoUtente(@RequestParam String email, @RequestParam int mese) {
        return service.getGiorniAllenamento(email, mese);
    }
}