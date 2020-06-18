package warenautomat;

import java.time.LocalDate;

public class Ware {
    private String name;
    private double preis;
    private LocalDate datum;

    public Ware(String name, double preis, LocalDate datum) {
        this.name = name;
        this.preis = preis;
        this.datum = datum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPreis() {
        return preis;
    }

    public void setPreis(double preis) {
        this.preis = preis;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }
}
