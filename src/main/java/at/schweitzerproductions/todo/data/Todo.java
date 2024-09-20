package at.schweitzerproductions.todo.data;

import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class Todo extends AbstractEntity {

    private LocalDate datum;
    private String status;
    private String wichtigkeit;
    private String dringlichkeit;
    private String tags;
    private String titel;
    private String detailBeschreibung;
    private String bearbeitungKommentar;

    public LocalDate getDatum() {
        return datum;
    }
    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getWichtigkeit() {
        return wichtigkeit;
    }
    public void setWichtigkeit(String wichtigkeit) {
        this.wichtigkeit = wichtigkeit;
    }
    public String getDringlichkeit() {
        return dringlichkeit;
    }
    public void setDringlichkeit(String dringlichkeit) {
        this.dringlichkeit = dringlichkeit;
    }
    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
    public String getTitel() {
        return titel;
    }
    public void setTitel(String titel) {
        this.titel = titel;
    }
    public String getDetailBeschreibung() {
        return detailBeschreibung;
    }
    public void setDetailBeschreibung(String detailBeschreibung) {
        this.detailBeschreibung = detailBeschreibung;
    }
    public String getBearbeitungKommentar() {
        return bearbeitungKommentar;
    }
    public void setBearbeitungKommentar(String bearbeitungKommentar) {
        this.bearbeitungKommentar = bearbeitungKommentar;
    }

}
