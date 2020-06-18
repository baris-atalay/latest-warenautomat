
package warenautomat;

import java.time.LocalDate;

import warenautomat.SystemSoftware;


public class Drehteller {
  private Fach[] faecher;

    public Drehteller() {
        this.faecher = new Fach[16];

        for (int i = 0; i < this.faecher.length; i++) {
            this.faecher[i] = new Fach();
        }
    }

    public void setWareInFach(int position, Ware ware) throws Exception {
        this.faecher[position].setWare(ware);
    }

    public Ware getWareInFach(int position) {
        return this.faecher[position].getWare();
    }

    public boolean deleteWareInFach(int position) {
        return this.faecher[position].deleteWare();
    }
}
