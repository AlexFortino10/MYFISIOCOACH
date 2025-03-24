package com.example.BackendMYFISIOCOACH.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.BackendMYFISIOCOACH.servizi.ConnessioneDBService;

public class ConnessioneDBController {

    @Autowired
    private ConnessioneDBService connessioneDBService;

    @GetMapping("/verifica-connessione")
    public ResponseEntity<String> verificaConnessione() {
        connessioneDBService.verificaConnessioneDB();
        return ResponseEntity.ok("Verifica connessione eseguita!");
    }

}
