package com.example.BackendMYFISIOCOACH.entità;

import jakarta.persistence.*;

@Entity
@Table(name = "resocontoutente")
public class ResocontoUtente {

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int minuti;

    @Column(nullable = false)
    private int secondi;

    @Column(name = "numallenamenti", nullable = false)
    private int numallenamenti;

    @Column(nullable = false)
    private int serie;

    @Column(name = "recordpersonale", nullable = false)
    private int recordpersonale;

    public ResocontoUtente() {
    }

    public ResocontoUtente(String email, int minuti, int secondi, int numallenamenti, int serie, int recordpersonale) {
        this.email = email;
        this.minuti = minuti;
        this.secondi = secondi;
        this.numallenamenti = numallenamenti;
        this.serie = serie;
        this.recordpersonale = recordpersonale;
    }

    // Getters e Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMinuti() {
        return minuti;
    }

    public void setMinuti(int minuti) {
        this.minuti = minuti;
    }

    public int getSecondi() {
        return secondi;
    }

    public void setSecondi(int secondi) {
        this.secondi = secondi;
    }

    public int getNumAllenamenti() {
        return numallenamenti;
    }

    public void setNumAllenamenti(int numallenamenti) {
        this.numallenamenti = numallenamenti;
    }

    public int getSerie() {
        return serie;
    }

    public void setSerie(int serie) {
        this.serie = serie;
    }

    public int getRecordPersonale() {
        return recordpersonale;
    }

    public void setRecordPersonale(int recordpersonale) {
        this.recordpersonale = recordpersonale;
    }
}
