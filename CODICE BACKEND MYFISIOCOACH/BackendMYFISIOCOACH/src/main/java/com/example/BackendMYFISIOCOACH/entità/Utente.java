package com.example.BackendMYFISIOCOACH.entità;

import jakarta.persistence.*;

@Entity
@Table(name = "utente")
public class Utente {

    @Id
    @Column(nullable = false, unique = true)
    private String email; // Usa email come chiave primaria

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String sesso;

    @Column(nullable = false)
    private int peso;

    @Column(nullable = false)
    private int altezza;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cognome;

    @Column(nullable = false, name = "Eta")
    private int eta;

    @Column(nullable = false)
    private int stage;

    public Utente() {
    }

    // Costruttore completo
    public Utente(String email, String password, String sesso, int peso, int altezza, String nome, String cognome,
            int eta, int stage) {
        this.email = email;
        this.password = password;
        this.sesso = sesso;
        this.peso = peso;
        this.altezza = altezza;
        this.nome = nome;
        this.cognome = cognome;
        this.eta = eta;
        this.stage = stage;
    }

    // Getters e Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getAltezza() {
        return altezza;
    }

    public void setAltezza(int altezza) {
        this.altezza = altezza;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }
}
