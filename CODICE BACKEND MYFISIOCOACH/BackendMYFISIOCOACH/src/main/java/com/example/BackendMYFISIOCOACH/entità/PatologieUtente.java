package com.example.BackendMYFISIOCOACH.entità;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patologieutente")
public class PatologieUtente {

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String patologia;

    // Costruttore vuoto richiesto da JPA
    public PatologieUtente() {
    }

    // Costruttore con parametri
    public PatologieUtente(String email, String patologia) {
        this.email = email;
        this.patologia = patologia;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPatologia() {
        return patologia;
    }

    public void setPatologia(String patologia) {
        this.patologia = patologia;
    }

    @Override
    public String toString() {
        return "PatologieUtente{" +
                "email='" + email + '\'' +
                ", patologia='" + patologia + '\'' +
                '}';
    }
}
