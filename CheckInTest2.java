//==============================================================================
// Project   : Master of Advanced Studies in Software-Engineering 2020
// Modul     : Projektarbeit OO Softwareentwicklung "Warenautomat"
//             Teil: Design&Implementation
// Title     : CheckIn-Test-Applikation
// Tab-Width : 2
/*///===========================================================================
* Description: Test-Applikation um den Check-In des Waren-Automaten zu testen, 
               incl. konfiguriereBestellung().
$Revision    : 1.16 $  $Date: 2020/06/09 16:34:37 $ 
/*///===========================================================================
//       1         2         3         4         5         6         7         8
//345678901234567890123456789012345678901234567890123456789012345678901234567890
//==============================================================================


import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import warenautomat.*;

public class CheckInTest2 {
  
  static final DateTimeFormatter FORMATTER = 
      DateTimeFormatter.ofPattern("dd.MM.yyyy");

  public static void main(String pArgs[]) throws ParseException {

  // Test der Automaten-Schnittstelle:
  {
    Automat automat = new Automat(); 
    automat.neueWareVonBarcodeLeser(1, "Name", 1.0, 
                                    LocalDate.parse("01.01.2100", FORMATTER));
    Kasse kasse = automat.gibKasse();
    automat.drehen();
    boolean b = automat.oeffnen(1);
    SystemSoftware.output(false);
    for (int i = 0; i < 14; i++) { 
      automat.drehen();
    }
    // nochmals 14 mal gedreht: somit jetzt 16.Fach vor Schiebetüre:
    automat.oeffnen(7);
    double d = automat.gibTotalenWarenWert();
    int anzahl = automat.gibVerkaufsStatistik(
                         "Name", LocalDate.parse("01.01.2100", FORMATTER));
    automat.konfiguriereBestellung("Name", 1, 2);
  }
  
  // Test der Kassen-Schnittstelle:
  {
    Automat automat = new Automat(); 
    Kasse kasse = automat.gibKasse();
    if (kasse == null) {
      kasse = new Kasse();
    }
    int result = kasse.verwalteMuenzbestand(1.0, 10);
    if (result != 10) {
      System.out.flush();
      System.err.println("ERROR: bad result: "+result);
      System.exit(1);
    }
    kasse.verwalteMuenzbestandBestaetigung();
    kasse.einnehmen(1.0);
    kasse.gibWechselGeld();
    kasse.gibBetragVerkaufteWaren();
  }


  } // main()

} // CheckInTest

      

/* Session-Log:

SystemSoftware::zeigeWarenPreisAn():  1: 1.0
SystemSoftware::zeigeVerfallsDatum(): 1: 1
SystemSoftware::zeigeWarenPreisAn():  1: 0.0
SystemSoftware::zeigeVerfallsDatum(): 1: 0
SystemSoftware::zeigeWarenPreisAn():  2: 0.0
SystemSoftware::zeigeVerfallsDatum(): 2: 0
SystemSoftware::zeigeWarenPreisAn():  3: 0.0
SystemSoftware::zeigeVerfallsDatum(): 3: 0
SystemSoftware::zeigeWarenPreisAn():  4: 0.0
SystemSoftware::zeigeVerfallsDatum(): 4: 0
SystemSoftware::zeigeWarenPreisAn():  5: 0.0
SystemSoftware::zeigeVerfallsDatum(): 5: 0
SystemSoftware::zeigeWarenPreisAn():  6: 0.0
SystemSoftware::zeigeVerfallsDatum(): 6: 0
SystemSoftware::zeigeWarenPreisAn():  7: 0.0
SystemSoftware::zeigeVerfallsDatum(): 7: 0
Drehteller::oeffnen(): mDrehtellerNr = 1 / mFachVorOeffnung = 2
Drehteller::oeffnen(): Fach ist leer !
SystemSoftware::output(): false
Drehteller::oeffnen(): mDrehtellerNr = 7 / mFachVorOeffnung = 16
Drehteller::oeffnen(): Fach ist leer !
SystemSoftware::zeigeBetragAn(): 1.0
SystemSoftware::auswerfenWechselGeld(): 1.0
SystemSoftware::zeigeBetragAn(): 0.0

*/

