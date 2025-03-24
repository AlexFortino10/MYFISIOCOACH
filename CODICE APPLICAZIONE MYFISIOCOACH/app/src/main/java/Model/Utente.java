package Model;

public class Utente {

    public Utente(String email, String password, String nome, String cognome, String sesso, int altezza, int peso, int eta) {
        this.email = email;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.sesso = sesso;
        this.altezza = altezza;
        this.peso = peso;
        this.eta = eta;
    }


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

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public int getAltezza() {
        return altezza;
    }

    public void setAltezza(int altezza) {
        this.altezza = altezza;
    }

    public int getPeso() {
        return peso;
    }

    public void setPeso(int peso) {
        this.peso = peso;
    }

    public int getEta() {
        return eta;
    }

    public void setEta(int eta) {
        this.eta = eta;
    }

    private String email,password,nome,cognome,sesso;
    private int altezza,peso,eta;


}
