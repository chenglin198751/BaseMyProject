package main;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PackTools {
    public static String Error_Msg = null;

    public static float formatFloat(float f, int scale) {
        BigDecimal b = new BigDecimal(f);
        return b.setScale(scale, RoundingMode.HALF_UP).floatValue();
    }

    public static final class Printer {
        private static final DateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

        public static void print(String message) {
            String dateStr = format.format(new Date());
            System.out.println("[" + dateStr + "] " + message);
        }
    }


}
