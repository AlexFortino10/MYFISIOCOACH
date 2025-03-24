package com.example.BackendMYFISIOCOACH.entità;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "allenamentogiornaliero")
public class AllenamentoGiornaliero {

    @Id
    @Column(nullable = false)
    private String benefici;

    @Column(nullable = false)
    private Long numero;

    @Column(name = "urlvideo", nullable = false)
    private String urlvideo;

    @Column(nullable = false)

    private int giorno;

    @Column(nullable = false)
    private String istruzioni;

    @Column(nullable = false)
    private int ora;

    @Column(nullable = false)
    private int minuti;

    // Getters e Setters
    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public String getUrlVideo() {
        return urlvideo;
    }

    public void setUrlVideo(String urlvideo) {
        this.urlvideo = urlvideo;
    }

    public int getGiorno() {
        return giorno;
    }

    public void setGiorno(int giorno) {
        this.giorno = giorno;
    }

    public String getIstruzioni() {
        return istruzioni;
    }

    public void setIstruzioni(String istruzioni) {
        this.istruzioni = istruzioni;
    }

    public String getBenefici() {
        return benefici;
    }

    public void setBenefici(String benefici) {
        this.benefici = benefici;
    }

    public int getOra() {
        return ora;
    }

    public void setOra(int ora) {
        this.ora = ora;
    }

    public int getMinuti() {
        return minuti;
    }

    public void setMinuti(int minuti) {

        this.minuti = minuti;
    }
}
