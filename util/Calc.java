package warenautomat.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Calc {

    public static double add(double a, double b) {
        BigDecimal _a = new BigDecimal(a);
        BigDecimal _b = new BigDecimal(b);

        return _a.add(_b).setScale(2, RoundingMode.HALF_UP).doubleValue();
    };

    public static double sub(double a, double b) {
        BigDecimal _a = new BigDecimal(a);
        BigDecimal _b = new BigDecimal(b);

        return _a.subtract(_b).setScale(2, RoundingMode.HALF_UP).doubleValue();
    };
}
