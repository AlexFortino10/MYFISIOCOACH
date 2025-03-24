package Model;

public class Patologie {

    String Patologia,emailpaziente;

    public Patologie(String patologia) {
        Patologia = patologia;
    }

    public Patologie(String patologia, String emailpaziente) {
        Patologia = patologia;
        this.emailpaziente = emailpaziente;
    }

    public String getPatologia() {
        return Patologia;
    }

    public void setPatologia(String patologia) {
        Patologia = patologia;
    }

    public String getEmailpaziente() {
        return emailpaziente;
    }

    public void setEmailpaziente(String emailpaziente) {
        this.emailpaziente = emailpaziente;
    }
}
