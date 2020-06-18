package warenautomat;

public class Fach {
    private Ware ware;

    public Fach() {
        this.ware = null;
    }

    public Ware getWare() {
        return ware;
    }

    public void setWare(Ware ware) throws Exception {
        if(this.ware == null) {
            this.ware = ware;
        } else {
            throw new Exception("Auf dieser Position gibt es schon Ware.");
        }
    }

    public boolean deleteWare() {
        this.ware = null;
        return true;
    }
}
