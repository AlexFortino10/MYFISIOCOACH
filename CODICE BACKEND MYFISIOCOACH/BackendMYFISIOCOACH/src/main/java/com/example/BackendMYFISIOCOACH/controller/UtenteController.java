package com.example.BackendMYFISIOCOACH.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.BackendMYFISIOCOACH.servizi.UtenteService;

@RestController
@RequestMapping("/api/utente")
public class UtenteController {

        @Autowired
        private UtenteService utenteService;

        // Metodo per verificare l'utente
        @PostMapping("/verifica")
        public CompletableFuture<ResponseEntity<Boolean>> verificaUtente(@RequestParam String email,
                        @RequestParam String password) {
                return utenteService.verificaUtente(email, password)
                                .thenApply(ResponseEntity::ok);
        }

        // Nuovo metodo per verificare i dati di verifica
        @PostMapping("/datidiVerifica")
        public CompletableFuture<ResponseEntity<Boolean>> verificaDatiDiVerifica(@RequestParam String email,
                        @RequestParam String nome, @RequestParam String cognome) {
                System.out.println("Post request received for verificaDatiDiVerifica");
                return utenteService.AccessoModificaPassword(email, nome, cognome).thenApply(ResponseEntity::ok);
        }

        // Metodo per verificare l'email dell'utente
        @GetMapping("/verificaEmail")
        public CompletableFuture<ResponseEntity<Boolean>> verificaEmail(@RequestParam String email) {
                return utenteService.verificaEmail(email).thenApply(ResponseEntity::ok);
        }

        // Nuovo metodo per modificare la password
        @PutMapping("/modificaPassword")
        public CompletableFuture<ResponseEntity<Boolean>> modificaPassword(@RequestParam String email,
                        @RequestParam String nuovapassword) {
                return utenteService.modificaPassword(email, nuovapassword).thenApply(ResponseEntity::ok);
        }

        // Nuovo metodo per ottenere il nome dell'utente
        @GetMapping("/nome")
        public CompletableFuture<ResponseEntity<String>> findNomeByEmail(@RequestParam String email) {
                return utenteService.findNomeByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere lo stage dell'utente
        @GetMapping("/stage")
        public CompletableFuture<ResponseEntity<Integer>> findStageByEmail(@RequestParam String email) {
                return utenteService.findStageByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per modificare lo stage
        @PutMapping("/modificaStage")
        public CompletableFuture<ResponseEntity<Boolean>> modificaStage(@RequestParam String email,
                        @RequestParam int stage) {
                return utenteService.modificaStage(email, stage).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere il cognome dell'utente
        @GetMapping("/cognome")
        public CompletableFuture<ResponseEntity<String>> findCognomeByEmail(@RequestParam String email) {
                return utenteService.findCognomeByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere il peso dell'utente
        @GetMapping("/peso")
        public CompletableFuture<ResponseEntity<Integer>> findPesoByEmail(@RequestParam String email) {
                return utenteService.findPesoByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere l'altezza dell'utente
        @GetMapping("/altezza")
        public CompletableFuture<ResponseEntity<Integer>> findAltezzaByEmail(@RequestParam String email) {
                return utenteService.findAltezzaByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere l'età dell'utente
        @GetMapping("/eta")
        public CompletableFuture<ResponseEntity<Integer>> findEtaByEmail(@RequestParam String email) {
                return utenteService.findEtaByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere il sesso dell'utente
        @GetMapping("/sesso")
        public CompletableFuture<ResponseEntity<String>> findSessoByEmail(@RequestParam String email) {
                return utenteService.findSessoByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per ottenere la password dell'utente
        @GetMapping("/password")
        public CompletableFuture<ResponseEntity<String>> findPasswordByEmail(@RequestParam String email) {
                return utenteService.findPasswordByEmail(email).thenApply(ResponseEntity::ok);
        }

        // Metodo per modificare email e password dell'utente
        @PutMapping("/modificaEmailPassword")
        public CompletableFuture<ResponseEntity<Boolean>> modificaEmailPassword(@RequestParam String email,
                        @RequestParam String nuovaemail, @RequestParam String nuovapassword) {
                return utenteService.modificaEmailPassword(email, nuovaemail, nuovapassword)
                                .thenApply(ResponseEntity::ok);
        }

        // Metodo per registrare un nuovo utente con stage 1
        @PostMapping("/registraUtente")
        public CompletableFuture<ResponseEntity<Boolean>> registraUtente(@RequestParam String email,
                        @RequestParam String password, @RequestParam String nome, @RequestParam String cognome,
                        @RequestParam int peso, @RequestParam int altezza, @RequestParam int eta,
                        @RequestParam String sesso) {
                return utenteService.registraUtente(email, password, nome, cognome, peso, altezza, eta, sesso)
                                .thenApply(ResponseEntity::ok);
        }

}
