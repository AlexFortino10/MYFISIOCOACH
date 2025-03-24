package com.example.BackendMYFISIOCOACH.entità;

import jakarta.persistence.*;

@Entity
@Table(name = "giorniallenamento")
public class GiorniAllenamento {

    @Id
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int giorno;

    @Column(nullable = false)
    private int mese;

    // Getters e Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGiorno() {
        return giorno;
    }

    public void setGiorno(int giorno) {
        this.giorno = giorno;
    }

    public int getMese() {
        return mese;
    }

    public void setMese(int mese) {
        this.mese = mese;
    }
}
