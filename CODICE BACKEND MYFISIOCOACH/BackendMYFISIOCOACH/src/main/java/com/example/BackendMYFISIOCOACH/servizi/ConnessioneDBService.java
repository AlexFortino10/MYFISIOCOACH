package com.example.BackendMYFISIOCOACH.servizi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.PostConstruct;

public class ConnessioneDBService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void verificaConnessioneDB() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result != null && result == 1) {
                System.out.println("Connessione al database riuscita!");
            }
        } catch (Exception e) {
            System.err.println("Errore durante la connessione al database: " + e.getMessage());
        }
    }

}
