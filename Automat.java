package warenautomat;

import warenautomat.util.Calc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Der Automat besteht aus 7 Drehtellern welche wiederum je aus 16 Fächer
 * bestehen. <br>
 * Der erste Drehteller und das jeweils erste Fach haben jeweils die Nummer 1
 * (nicht 0!). <br>
 * Im Weitern hat der Automat eine Kasse. Diese wird vom Automaten instanziert.
 */
public class Automat {
  private static final int NR_DREHTELLER = 7;
  private Drehteller[] mDrehteller;
  private Kasse mKasse;
  private double warenWert;
  private int automatPosition;
  private ArrayList<Ware> waren;
  private LinkedHashMap<String, ArrayList<LocalDate>> verkaufsStatistik;
  private HashMap<String, BestellKonfiguration> bestellKonfiguration;
  private HashMap<String, HashMap<Integer, Integer>> bestellungen;
  private HashMap<String, Integer> anzahlVonWare;

  /**
   * Der Standard-Konstruktor. <br>
   * Führt die nötigen Initialisierungen durch (u.a. wird darin die Kasse
   * instanziert).
   */
  public Automat() {
    this.mDrehteller = new Drehteller[NR_DREHTELLER];
    this.mKasse = new Kasse();
    this.mKasse.mapAutomat(this);
    this.automatPosition = 0;
    this.waren = new ArrayList<Ware>();
    this.verkaufsStatistik = new LinkedHashMap<String, ArrayList<LocalDate>>();
    this.bestellKonfiguration = new HashMap<String, BestellKonfiguration>();
    this.bestellungen = new HashMap<String, HashMap<Integer, Integer>>();
    this.anzahlVonWare = new HashMap<String, Integer>();

    for (int i = 0; i < this.mDrehteller.length; i++) {
      this.mDrehteller[i] = new Drehteller();
    }
  }

  /**
   * Füllt ein Fach mit Ware. <br>
   * Wenn das Service-Personal den Automaten füllt, wird mit einem
   * Bar-Code-Leser zuerst die Ware gescannt. <br>
   * Daraufhin wird die Schiebe-Tür geöffnet. <br>
   * Das Service-Personal legt die neue Ware ins Fach und schliesst das Fach. <br>
   * Die Hardware resp. System-Software ruft die Methode
   * <code> Automat.neueWareVonBarcodeLeser() </code> auf.
   * 
   * @param pDrehtellerNr Der Drehteller bei welchem das Fach hinter der
   *          Schiebe-Türe gefüllt wird. <br>
   *          Nummerierung beginnt mit 1 (nicht 0)!
   * @param pWarenName Der Name der neuen Ware.
   * @param pPreis Der Preis der neuen Ware.
   * @param pVerfallsDatum Das Verfallsdatum der neuen Ware.
   */
  public void neueWareVonBarcodeLeser(int pDrehtellerNr, String pWarenName,
                                      double pPreis, LocalDate pVerfallsDatum) {
    Ware newWare = new Ware(pWarenName, pPreis, pVerfallsDatum);
    this.waren.add(newWare);

    try{
      this.mDrehteller[pDrehtellerNr - 1].setWareInFach(automatPosition, newWare);
      this.addWarenWert(newWare.getPreis());

      if(this.anzahlVonWare.get(pWarenName) == null) {
        this.anzahlVonWare.put(pWarenName, 1);
      } else {
        this.anzahlVonWare.replace(pWarenName, this.anzahlVonWare.get(pWarenName) + 1);
      }

      // SystemSoftware.zeigeWareInGui(pDrehtellerNr, newWare.getName(), newWare.getDatum());

      SystemSoftware.zeigeWarenPreisAn(pDrehtellerNr, newWare.getPreis());

      if(newWare.getDatum().isBefore(SystemSoftware.gibAktuellesDatum())) {
        SystemSoftware.zeigeVerfallsDatum(pDrehtellerNr,2);
      } else {
        SystemSoftware.zeigeVerfallsDatum(pDrehtellerNr,1);
      }


    } catch (Throwable e) {
      System.err.println(e);
    }
    
  }

  /**
   * Gibt die Objekt-Referenz auf die <em> Kasse </em> zurück.
   */
  public Kasse gibKasse() {
    return this.mKasse;
  }

  /**
   * Wird von der System-Software jedesmal aufgerufen wenn der gelbe Dreh-Knopf
   * gedrückt wird. <br>
   * Die Applikations-Software führt die Drehteller-Anzeigen nach (Warenpreis,
   * Verfallsdatum). <br>
   * Das Ansteuern des Drehteller-Motors übernimmt die System-Software (muss
   * nicht von der Applikations-Software gesteuert werden.). <br>
   * Die System-Software stellt sicher, dass <em> drehen </em> nicht durchgeführt wird
   * wenn ein Fach offen ist.
   */
  public void drehen() {
    this.automatPosition = this.automatPosition == 15 ? 0 : this.automatPosition + 1;


    for (int i = 0; i < this.mDrehteller.length; i++) {
      Ware wareInFach = this.mDrehteller[i].getWareInFach(this.automatPosition);

      if(wareInFach == null) {
        SystemSoftware.zeigeWarenPreisAn(i + 1, 0.00);
        SystemSoftware.zeigeVerfallsDatum(i + 1,0);
      } else {
        SystemSoftware.zeigeWarenPreisAn(i + 1, wareInFach.getPreis());

        if(wareInFach.getDatum().isBefore(SystemSoftware.gibAktuellesDatum())) {
          SystemSoftware.zeigeVerfallsDatum(i + 1,2);
        } else {
          SystemSoftware.zeigeVerfallsDatum(i + 1,1);
        }
      }
    }

    this.checkWarenNachBestellung();
  }

  /**
   * Beim Versuch eine Schiebetüre zu öffnen ruft die System-Software die
   * Methode <code> oeffnen() </code> der Klasse <em> Automat </em> mit der
   * Drehteller-Nummer als Parameter auf. <br>
   * Es wird überprüft ob alles o.k. ist: <br>
   * - Fach nicht leer <br>
   * - Verfallsdatum noch nicht erreicht <br>
   * - genug Geld eingeworfen <br>
   * - genug Wechselgeld vorhanden <br>
   * Wenn nicht genug Geld eingeworfen wurde, wird dies mit
   * <code> SystemSoftware.zeigeZuWenigGeldAn() </code> signalisiert. <br>
   * Wenn nicht genug Wechselgeld vorhanden ist wird dies mit
   * <code> SystemSoftware.zeigeZuWenigWechselGeldAn() </code> signalisiert. <br>
   * Wenn o.k. wird entriegelt (<code> SystemSoftware.entriegeln() </code>) und
   * positives Resultat zurückgegeben, sonst negatives Resultat. <br>
   * Es wird von der System-Software sichergestellt, dass zu einem bestimmten
   * Zeitpunkt nur eine Schiebetüre offen sein kann.
   * 
   * @param pDrehtellerNr Der Drehteller bei welchem versucht wird die
   *          Schiebe-Türe zu öffnen. <br>
   *          Nummerierung beginnt mit 1 (nicht 0)!
   * @return Wenn alles o.k. <code> true </code>, sonst <code> false </code>.
   */
  public boolean oeffnen(int pDrehtellerNr) {

    Ware wareInFach = this.mDrehteller[pDrehtellerNr - 1].getWareInFach(this.automatPosition);

    if(
      wareInFach == null ||
      wareInFach.getDatum().isBefore(SystemSoftware.gibAktuellesDatum())
    ) {
      return false;
    }

    if(wareInFach.getPreis() > this.gibKasse().getGuthaben()) {
      SystemSoftware.zeigeZuWenigGeldAn();
      return false;
    }

    if(!this.gibKasse().checkWechselGeld(wareInFach.getPreis())) {
      SystemSoftware.zeigeZuWenigWechselGeldAn();
      return false;
    }

    SystemSoftware.entriegeln(pDrehtellerNr);
    this.gibKasse().setGuthaben(this.gibKasse().getGuthaben() - wareInFach.getPreis());
    this.gibKasse().setUseCache(false);
    this.subWarenWert(wareInFach.getPreis());

    ArrayList<LocalDate> warenStatistik = this.verkaufsStatistik.get(wareInFach.getName());

    if(warenStatistik == null) {
      ArrayList<LocalDate> neueWarenStatistik = new ArrayList<LocalDate>();
      neueWarenStatistik.add(SystemSoftware.gibAktuellesDatum());

      this.verkaufsStatistik.put(wareInFach.getName(), neueWarenStatistik);
    } else {
      warenStatistik.add(SystemSoftware.gibAktuellesDatum());

      this.verkaufsStatistik.replace(wareInFach.getName(), warenStatistik);
    }

    if(this.anzahlVonWare.get(wareInFach.getName()) != null) {
       this.anzahlVonWare.replace(wareInFach.getName(), this.anzahlVonWare.get(wareInFach.getName()) - 1);
    }

    this.mDrehteller[pDrehtellerNr - 1].deleteWareInFach(this.automatPosition);

    SystemSoftware.zeigeWarenPreisAn(pDrehtellerNr, 0.00);
    SystemSoftware.zeigeVerfallsDatum(pDrehtellerNr,0);

    this.checkWarenNachBestellung();

    return true;
    
  }

  /**
   * Gibt den aktuellen Wert aller im Automaten enthaltenen Waren in Franken
   * zurück. <br>
   * Analyse: <br>
   * Abgeleitetes Attribut. <br>
   * 
   * @return Der totale Warenwert des Automaten.
   */
  public double gibTotalenWarenWert() {
    
    return this.warenWert;
    
  }

  /**
   * Gibt die Anzahl der verkauften Ware <em> pName </em> seit (>=)
   * <em> pDatum </em> zurück.
   * 
   * @param pName Der Name der Ware nach welcher gesucht werden soll.
   * @param pDatum Das Datum seit welchem gesucht werden soll.
   * @return Anzahl verkaufter Waren.
   */
  public int gibVerkaufsStatistik(String pName, LocalDate pDatum) {
    ArrayList<LocalDate> warenStatistik = this.verkaufsStatistik.get(pName);

    if(warenStatistik == null) {
      return 0;
    }

    int position = -1;
    for (int i = 0; i < warenStatistik.size(); i++) {
      if(warenStatistik.get(i).isAfter(pDatum) || warenStatistik.get(i).isEqual(pDatum)) {
        position = i;
        break;
      }
    }

    return position == -1 ? 0 : warenStatistik.size() - position;
  }

  public void addWarenWert(double wert) {
    this.warenWert = Calc.add(this.warenWert, wert);
  }

  public void subWarenWert(double wert) {
    this.warenWert = Calc.sub(this.warenWert, wert);
  }

  /**
   * Konfiguration einer automatischen Bestellung. <br>
   * Der Automat setzt automatisch Bestellungen ab mittels
   * <code> SystemSoftware.bestellen() </code> wenn eine Ware ausgeht.
   *
   * @param pWarenName
   *          Warenname derjenigen Ware, für welche eine automatische
   *          Bestellung konfiguriert wird.
   * @param pGrenze
   *          Ab welcher Anzahl von verkaufbarer Ware jeweils eine
   *          Bestellung abgesetzt werden soll.
   * @param pAnzahlBestellung
   *          Wieviele neue Waren jeweils bestellt werden sollen.
   */
  public void konfiguriereBestellung(String pWarenName, int pGrenze,
                                     int pAnzahlBestellung) {
    this.bestellKonfiguration.put(pWarenName, new BestellKonfiguration(pGrenze, pAnzahlBestellung));

  }

  public void checkWarenNachBestellung () {
    this.anzahlVonWare.forEach((key, value) -> {
      this.checkBestellung(key, value);
    });
  }

  public void checkBestellung(String name, int bestand) {
    BestellKonfiguration konfiguation = this.bestellKonfiguration.get(name);
    if(konfiguation == null || bestand >= konfiguation.getGrenze()){
      return;
    }

    HashMap warenBestellungen = this.bestellungen.get(name);
    if(warenBestellungen != null && warenBestellungen.get(bestand) != null) {
      return;
    }
    if(warenBestellungen == null) {
      this.bestellungen.put(name, new HashMap<Integer, Integer>());
      warenBestellungen = this.bestellungen.get(name);
    }

    warenBestellungen.put(bestand, konfiguation.getAnzahlBestellung());
    this.bestellungen.put(name, warenBestellungen);
    SystemSoftware.bestellen(name, konfiguation.getAnzahlBestellung(), bestand);
  }

  public HashMap<String, HashMap<Integer, Integer>> getBestellungen() {
    return this.bestellungen;
  }
}
