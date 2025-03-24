package Model;

public class Resoconto {
    String email;
    int minuti;

    public Resoconto(String email, int minuti, int secondi, int numallenamenti, int serie, int recordpersonale) {
        this.email = email;
        this.minuti = minuti;
        this.secondi = secondi;
        this.numallenamenti = numallenamenti;
        this.serie = serie;
        this.recordpersonale = recordpersonale;
    }

    public Resoconto() {
    }

    int secondi;
    int numallenamenti;
    int serie;

    public int getRecordpersonale() {
        return recordpersonale;
    }

    public void setRecordpersonale(int recordpersonale) {
        this.recordpersonale = recordpersonale;
    }

    public int getSerie() {
        return serie;
    }

    public void setSerie(int serie) {
        this.serie = serie;
    }

    public int getNumallenamenti() {
        return numallenamenti;
    }

    public void setNumallenamenti(int numallenamenti) {
        this.numallenamenti = numallenamenti;
    }

    public int getSecondi() {
        return secondi;
    }

    public void setSecondi(int secondi) {
        this.secondi = secondi;
    }

    public int getMinuti() {
        return minuti;
    }

    public void setMinuti(int minuti) {
        this.minuti = minuti;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    int recordpersonale;
}
