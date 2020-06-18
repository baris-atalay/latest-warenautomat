package warenautomat;


import warenautomat.SystemSoftware;
import warenautomat.util.Calc;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Die Kasse verwaltet das eingenommene Geld sowie das Wechselgeld. <br>
 * Die Kasse hat fünf Münz-Säulen für: <br>
 * - 10 Rappen <br>
 * - 20 Rappen <br>
 * - 50 Rappen <br>
 * - 1 Franken <br>
 * - 2 Franken <br>
 */
public class Kasse {
  private final int maxBestand = 101;
  private final double[] muenzVarianten = new double[] {2.00, 1.00, 0.50, 0.20, 0.10};
  private HashMap<Double, Integer> muenzSaule;
  private LinkedHashMap<Double, Integer> muenzCache;
  private boolean useCache;
  private double verkaufsWert;
  private double guthaben;
  private Automat automat;

  /**
   * Standard-Konstruktor. <br>
   * Führt die nötigen Initialisierungen durch.
   */
  public Kasse() {
    this.muenzSaule = new HashMap<Double, Integer>();
    this.muenzCache = new LinkedHashMap<Double, Integer>();
    this.verkaufsWert = 0;
    this.guthaben = 0.00;
    this.useCache = false;

    for (int i = 0; i < muenzVarianten.length; i++) {
      this.muenzSaule.put(muenzVarianten[i], 0);
      this.muenzCache.put(muenzVarianten[i], 0);
    }
  }

  public void mapAutomat (Automat automat) {
    this.automat = automat;
  }

  /**
   * Diese Methode wird aufgerufen nachdem das Personal beim Verwalten des
   * Wechselgeldbestand die Münzart und die Anzahl der Münzen über die
   * Tastatur eingegeben hat 
   * (siehe Use-Case "Wechselgeldbestand (Münzbestand) verwalten").
   * 
   * @param pMuenzenBetrag Der Betrag der Münzart in Franken.
   * @param pAnzahl Die Anzahl der Münzen. Bei der Entnahme von Münzen als
   *                entsprechender negativer Wert.
   * @return Anzahl der Münzen welche hinzugefügt resp. entnommen werden (bei
   *         Entnahme als negativer Wert). <br>
   *         Im Normalfall entspricht dieser Wert dem Übergabeparameter 
   *         <code>pAnzahl</code>. <br> 
   *         Er kann kleiner sein falls beim Hinzufügen in der Münzsäule zu 
   *         wenig Platz vorhanden ist oder wenn bei der Entnahme ein grössere 
   *         Anzahl angegeben wurde als tatsächlich in der Münzsäule vorhanden 
   *         ist. <br>
   *         Wenn ein nicht unterstützter Münzbetrag übergeben wurde: -200
   */
  public int verwalteMuenzbestand(double pMuenzenBetrag, int pAnzahl) {
    if(!this.muenzSaule.containsKey(pMuenzenBetrag)) {
      return -200;
    }

    this.useCache = true;

    int aktuelleAnzahl = this.muenzSaule.get(pMuenzenBetrag);
    int neueAnzahl = aktuelleAnzahl + pAnzahl;

    neueAnzahl = neueAnzahl > maxBestand - 1 ? maxBestand - 1 : neueAnzahl < 0 ? 0 : neueAnzahl;

    this.muenzCache.replace(pMuenzenBetrag, neueAnzahl - aktuelleAnzahl);

    return (this.muenzCache.get(pMuenzenBetrag));
    
  }

  /**
   * Diese Methode wird aufgerufen nachdem das Personal beim Geldauffüllen den
   * Knopf "Bestätigen" gedrückt hat
   * (siehe Use-Case "Wechselgeldbestand (Münzbestand) verwalten"). <br>
   * Verbucht die Münzen gemäss dem vorangegangenen Aufruf der Methode 
   * <code>verwalteMuenzbestand()</code>.
   */
  public void verwalteMuenzbestandBestaetigung() {

    this.muenzCache.forEach((key, value) -> {
      this.muenzSaule.replace(key, this.muenzSaule.get(key) + value);
      this.muenzCache.replace(key, 0);
    });

    this.useCache = false;
  }
 
  /**
   * Diese Methode wird aufgerufen wenn ein Kunde eine Münze eingeworfen hat. <br>
   * Führt den eingenommenen Betrag entsprechend nach. <br>
   * Stellt den nach dem Einwerfen vorhandenen Betrag im Kassen-Display dar. <br>
   * Eingenommenes Geld steht sofort als Wechselgeld zur Verfügung. <br>
   * Die Münzen werden von der Hardware-Kasse auf Falschgeld, Fremdwährung und
   * nicht unterstützte Münzarten geprüft, d.h. diese Methode wird nur
   * aufgerufen wenn ein Münzeinwurf soweit erfolgreich war. <br>
   * Ist die Münzsäule voll (d.h. 100 Münzen waren vor dem Einwurf bereits darin
   * enthalten), so wird mittels
   * <code> SystemSoftware.auswerfenWechselGeld() </code> unmittelbar ein
   * entsprechender Münz-Auswurf ausgeführt. <br>
   * Hinweis: eine Hardware-Münzsäule hat jeweils effektiv Platz für 101 Münzen.
   * 
   * @param pMuenzenBetrag Der Betrag der neu eingeworfenen Münze in Franken.
   * @return <code> true </code>, wenn er Einwurf erfolgreich war. <br>
   *         <code> false </code>, wenn Münzsäule bereits voll war.
   */
  public boolean einnehmen(double pMuenzenBetrag) {

    if(this.muenzSaule.get(pMuenzenBetrag) == maxBestand) {
      SystemSoftware.auswerfenWechselGeld(pMuenzenBetrag);
      return false;
    }

    this.useCache = true;

    this.guthaben = Calc.add(this.guthaben, pMuenzenBetrag);

    this.muenzCache.replace(pMuenzenBetrag, this.muenzCache.get(pMuenzenBetrag) + 1);
    this.muenzSaule.replace(pMuenzenBetrag, this.muenzSaule.get(pMuenzenBetrag) + 1);

    SystemSoftware.zeigeBetragAn(this.guthaben);
    this.automat.checkWarenNachBestellung();

    return true;
  }

  /**
   * Bewirkt den Auswurf des Restbetrages.
   */
  public void gibWechselGeld() {

    for (int i = 0; i < muenzVarianten.length; i++) {
      double variante = muenzVarianten[i];

      if (this.guthaben == 0.00) {
        break;
      }
      if(this.guthaben < variante || this.muenzSaule.get(variante) == 0) {
        continue;
      }


      int count = 0;
      int safeLoopMax = maxBestand; // Limit loop number, to avoid recursion issues.

      while(this.guthaben >= variante) {
        if(count > safeLoopMax ||  this.muenzSaule.get(variante) == 0) {
          break;
        }

        if(this.useCache){
          if(this.muenzCache.get(variante) == 0) {
            continue;
          }
          this.muenzCache.replace(variante, this.muenzCache.get(variante) - 1);
        }

        this.muenzSaule.replace(variante, this.muenzSaule.get(variante) - 1);

        SystemSoftware.auswerfenWechselGeld(variante);
        this.setGuthaben(Calc.sub(this.guthaben, variante));

        count++;
      }
    }

    this.automat.checkWarenNachBestellung();
  }

  public boolean checkWechselGeld(double preis) {
    double rest = Calc.sub(this.guthaben, preis);

    for (int i = 0; i < muenzVarianten.length; i++) {
      double variante = muenzVarianten[i];

      if (rest == 0.00) {
        break;
      }
      if(rest < variante || this.muenzSaule.get(variante) == 0) {
        continue;
      }


      int count = 0;
      int safeLoopMax = maxBestand; // Limit loop number, to avoid recursion issues.

      while(rest >= variante) {
        if(count > safeLoopMax ||  this.muenzSaule.get(variante) == 0) {
          break;
        }

        rest = Calc.sub(rest, variante);
        count++;
      }
    }

    return rest == 0.00;
  }

  /**
   * Gibt den Gesamtbetrag der bisher verkauften Waren zurück. <br>
   * Analyse: Abgeleitetes Attribut.
   * 
   * @return Gesamtbetrag der bisher verkauften Waren.
   */
  public double gibBetragVerkaufteWaren() {
    return this.verkaufsWert;
  }

  public void addVerkaufsWert(double wert) {
    this.verkaufsWert = Calc.add(this.verkaufsWert, wert);
  }

  public double getGuthaben() {
    return this.guthaben;
  }

  public void setGuthaben(double guthaben) {
    this.guthaben = guthaben;
    SystemSoftware.zeigeBetragAn(this.guthaben);
  }

  public void setUseCache(boolean useCache) {
    this.useCache = useCache;
    if(!useCache) {
      this.resetCache();
    }
  }

  public void resetCache() {
    for (int i = 0; i < this.muenzVarianten.length; i++) {
      this.muenzCache.put(muenzVarianten[i], 0);
    }
  }

  public HashMap<Double, Integer> getMuenzSaule() {
    return this.muenzSaule;
  }

  public HashMap<Double, Integer> getMuenzCache() {
    return this.muenzCache;
  }
}
