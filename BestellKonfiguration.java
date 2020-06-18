package warenautomat;

public class BestellKonfiguration {
    private int grenze;
    private int anzahlBestellung;

    public BestellKonfiguration(int grenze, int anzahlBestellung) {
        this.grenze = grenze;
        this.anzahlBestellung = anzahlBestellung;
    }

    public int getGrenze() {
        return this.grenze;
    }

    public int getAnzahlBestellung() {
        return this.anzahlBestellung;
    }
}
