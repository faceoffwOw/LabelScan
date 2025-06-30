package test.test.bkoclient_tsd_v02.HelpersClass;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTime_my {

    public static String DateLong(String _Date) throws ParseException {

        DateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String inputText = _Date;
        Date date = inputFormat.parse(inputText);
        String outputText = outputFormat.format(date);

        return outputText;
    }

    public static String Now_Long() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String Now_Short() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        Date date = new Date();

        return dateFormat.format(date);
    }

    public static String Now_Short_OneDayOfMonth() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        return dateFormat.format(calendar.getTime());
    }

    public static String DateLongRus(String _Date) throws ParseException {

        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        String inputText = _Date;
        Date date = inputFormat.parse(inputText);
        String outputText = outputFormat.format(date);

        return outputText;
    }

    /*public static String Now_Short() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd.MM.yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }*/
}
