package test.test.bkoclient_tsd_v02.Manager;

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerProperty {
    private static String Server;

    public static String getServer()
    {
        return "http://" + Server + "/";
    }

    public static void setServer(String _Server)
    {
        Server = _Server;
    }

    public static HttpURLConnection Get(String _Query) throws IOException {

        URL url = new URL(_Query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);//10 sec
        conn.setConnectTimeout(15000);//10 sec
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        return conn;
    }

    public static HttpURLConnection Put(String _Query) throws IOException {

        URL url = new URL(_Query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        //conn.setDoInput(true);
        conn.setConnectTimeout(10000); //10 sec
        conn.setReadTimeout(10000); //10 sec
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        return conn;
    }

    public static HttpURLConnection Post(String _Query) throws IOException {
        URL url = new URL(_Query);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000); // 10 секунд
        conn.setReadTimeout(10000); // 10 секунд
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }
}
