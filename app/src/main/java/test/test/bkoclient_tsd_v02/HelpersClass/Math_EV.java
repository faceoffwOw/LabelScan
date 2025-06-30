package test.test.bkoclient_tsd_v02.HelpersClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Math_EV {

    public static double decround (double d, int newScale) {

        return new BigDecimal(d).setScale(newScale, RoundingMode.HALF_UP).doubleValue();
    }

    public static float decroundF (float d, int newScale) {

        return new BigDecimal(d).setScale(newScale, RoundingMode.HALF_UP).floatValue();
    }

    public static double parseDouble(String _value) throws ParseException {

        return Double.parseDouble(_value.replace(",","."));
    }
}
