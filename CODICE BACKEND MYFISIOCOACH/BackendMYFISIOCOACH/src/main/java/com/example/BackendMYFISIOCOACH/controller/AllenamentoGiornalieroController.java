package com.example.BackendMYFISIOCOACH.controller;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BackendMYFISIOCOACH.servizi.AllenamentoGiornalieroService;

@RestController
@RequestMapping("/api/allenamento")
public class AllenamentoGiornalieroController {

    private final AllenamentoGiornalieroService service;

    public AllenamentoGiornalieroController(AllenamentoGiornalieroService service) {
        this.service = service;
    }

    @GetMapping("/video")
    public ResponseEntity<Optional<String>> getAllenamentoGiornaliero(@RequestParam int giorno,
            @RequestParam int numeroAllenamento) {
        Optional<String> urlVideo = service.getAllenamentoGiornaliero(giorno, numeroAllenamento);
        return urlVideo != null ? ResponseEntity.ok(urlVideo) : ResponseEntity.notFound().build();
    }

    @GetMapping("/istruzioni")
    public ResponseEntity<Optional<String>> getIstruzioni(@RequestParam int giorno,
            @RequestParam int numeroAllenamento) {
        Optional<String> istruzioni = service.getIstruzioniAllenamentoGiornaliero(giorno, numeroAllenamento);
        return istruzioni != null ? ResponseEntity.ok(istruzioni) : ResponseEntity.notFound().build();
    }

    @GetMapping("/benefici")
    public ResponseEntity<Optional<String>> getBenefici(@RequestParam int giorno, @RequestParam int numeroAllenamento) {
        Optional<String> benefici = service.getBeneficiAllenamentoGiornaliero(giorno, numeroAllenamento);
        return benefici != null ? ResponseEntity.ok(benefici) : ResponseEntity.notFound().build();
    }

    @GetMapping("/ora")
    public ResponseEntity<Integer> getOra(@RequestParam int giorno, @RequestParam int numeroAllenamento) {
        Integer ora = service.getOraAllenamento(giorno, numeroAllenamento);
        return ora != null ? ResponseEntity.ok(ora) : ResponseEntity.notFound().build();
    }

    @GetMapping("/minuti")
    public ResponseEntity<Integer> getMinuti(@RequestParam int giorno, @RequestParam int numeroAllenamento) {
        Integer minuti = service.getMinutiAllenamento(giorno, numeroAllenamento);
        return minuti != null ? ResponseEntity.ok(minuti) : ResponseEntity.notFound().build();
    }

    @GetMapping("/successivo/ora")
    public ResponseEntity<Integer> getOraPrimoAllenamentoGiornoSuccessivo() {
        Integer ora = service.getOraPrimoAllenamentoGiornoSuccessivo();
        return ora != null ? ResponseEntity.ok(ora) : ResponseEntity.notFound().build();
    }

    @GetMapping("/successivo/minuti")
    public ResponseEntity<Integer> getMinutiPrimoAllenamentoGiornoSuccessivo() {
        Integer minuti = service.getMinutiPrimoAllenamentoGiornoSuccessivo();
        return minuti != null ? ResponseEntity.ok(minuti) : ResponseEntity.notFound().build();
    }

    // Metodo per inserire un'allenamento giornaliero
    @PostMapping("/inserisci")
    public CompletableFuture<ResponseEntity<Boolean>> insertAllenamentoGiornaliero(@RequestParam int giorno,
            @RequestParam int numero, @RequestParam String urlvideo, @RequestParam String istruzioni,
            @RequestParam String benefici, @RequestParam int ora, @RequestParam int minuti) {
        return service.insertAllenamentoGiornaliero(giorno, numero, urlvideo, istruzioni, benefici, ora, minuti)
                .thenApply(ResponseEntity::ok);
    }

    // Metodo per eliminare un'allenamento giornaliero in base al numero
    @DeleteMapping("/elimina")
    public CompletableFuture<ResponseEntity<Boolean>> deleteAllenamentoGiornalieroByNumero(@RequestParam int numero) {
        return service.deleteAllenamentoGiornalieroByNumero(numero).thenApply(ResponseEntity::ok);
    }

    // Metodo per modificare le istruzioni di un allenamento in base al numero
    @PostMapping("/modifica/istruzioni")
    public CompletableFuture<ResponseEntity<Boolean>> modificaIstruzioniByNumero(@RequestParam int numero,
            @RequestParam String nuoveistruzioni) {
        return service.modificaIstruzioniByNumero(numero, nuoveistruzioni).thenApply(ResponseEntity::ok);
    }

    // Metodo per modificare l'url del video di un allenamento in base al numero
    @PostMapping("/modifica/video")
    public CompletableFuture<ResponseEntity<Boolean>> modificaUrlVideoByNumero(@RequestParam int numero,
            @RequestParam String nuovourlvideo) {
        return service.modificaUrlVideoByNumero(numero, nuovourlvideo).thenApply(ResponseEntity::ok);
    }

    // Metodo per modificare i benefici di un allenamento in base al numero
    @PostMapping("/modifica/benefici")
    public CompletableFuture<ResponseEntity<Boolean>> modificaBeneficiByNumero(@RequestParam int numero,
            @RequestParam String nuovibenefici) {
        return service.modificaBeneficiByNumero(numero, nuovibenefici).thenApply(ResponseEntity::ok);
    }

}