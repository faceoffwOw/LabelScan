package test.test.bkoclient_tsd_v02.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import test.test.bkoclient_tsd_v02.BuildConfig;
import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.Settings;
import test.test.bkoclient_tsd_v02.Manager.TsdProperty;
import test.test.bkoclient_tsd_v02.R;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    SharedPreferences mSettings;
    EditText editIPserver;
    TextView txtcheckInthernet, txtCheckServer, txtConnectDb, txtGetVersion;

    private String apkVerServer = "";
    private String apkVerCur = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //IP адресс
        editIPserver = findViewById(R.id.editIPadress);

        //Подключение к интернету
        txtcheckInthernet = findViewById(R.id.txtcheckInthernet);
        //Проверка подключения к серверу
        txtCheckServer = findViewById(R.id.txtCheckServer);
        //Проверка подключения базы данных
        txtConnectDb = findViewById(R.id.txtConnectDb);
        //версия приложения и версия приложения на сервере
        txtGetVersion = findViewById(R.id.txtGetVersion);

        //получить текущую версию приложения и версию приложения на сервере
        try {
            new GetVersionApp_Server(this).execute();
        }
        catch (Exception ex){
            MessageAlert.ErrorMessage(this, ex.toString() + "getVersionApp_server");
        }

        //Шара приложения
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, this.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mSettings.contains(Settings.APP_PREFERENCES_SERVER))
        {
            // Получаем число из настроек
            String Server = mSettings.getString(Settings.APP_PREFERENCES_SERVER, Settings.defServer);
            // Выводим на экран данные из настроек
            editIPserver.setText(Server);
        }
    }

    //Применить настройки IP адреса
    public void btnApplyServer_Click(View v){

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(Settings.APP_PREFERENCES_SERVER, editIPserver.getText().toString());
        editor.apply();

        MessageAlert.ShowToast(this,"IP адрес сохранен: " + editIPserver.getText().toString());
    }

    //Проверить подключение сервера
    public void btnCheckConnect_Click(View v) {
        try {
            //Проверка есть ли вообще интернет
            isNetworkConn();
            //Проверка есть ли подключение к моему web сервису
            new isServerConn().execute();
            //Проверка есть подключение к базам данных чере web сервис
            new isServer_dbConnect().execute();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            //Log.e(TsdProperty.TAG, ex.toString());
        }
    }

    //обновить приложение
    public void btnUpdateApp_Click(View v) throws PackageManager.NameNotFoundException {

        //включить хранилище на андройд этому приложению
        verifyStoragePermissions(this);

        updateApp updateApp = new updateApp(this);
        updateApp.execute();
    }

    private void isNetworkConn(){
        if(isNetworkConnected(this))
        {
            txtcheckInthernet.setText("Интернет: Есть");
            txtcheckInthernet.setTextColor(getResources().getColor(R.color.colorResultGreen));
        }
        else
        {
            txtcheckInthernet.setText("Интернет: Нет");
            txtcheckInthernet.setTextColor(getResources().getColor(R.color.colorErrorRed));
        }
    }
    //Проверка есть ли вообще интернет
    public static boolean isNetworkConnected(Context context) {
        boolean result = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cm != null) {

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    result = true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    result = true;
                }
            }
        }

        return result;
    }
    //есть ли подключение к моему web сервису
    private class isServerConn extends AsyncTask<Void , Void , Void> {
        boolean isConnect = false;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(ServerProperty.getServer() + "api/labels/");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(5000); // Timeout 2 seconds.
                urlc.setReadTimeout(5000);
                urlc.connect();

                if(urlc.getResponseCode() == 200) // Successful response.
                {
                    //Log.d(TsdProperty.TAG, String.valueOf(urlc.getResponseCode()));
                    isConnect = true;
                }
                else {
                    //Log.d(TsdProperty.TAG, "Нет интернета");
                    isConnect = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                //Log.d(TsdProperty.TAG, ex.toString());
                isConnect = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(isConnect)
            {
                txtCheckServer.setText("Сервер: Есть");
                txtCheckServer.setTextColor(getResources().getColor(R.color.colorResultGreen));
            }
            else
            {
                txtCheckServer.setText("Сервер: Нет");
                txtCheckServer.setTextColor(getResources().getColor(R.color.colorErrorRed));
            }
        }
    }
    //есть ли подключение к базам данных
    private class isServer_dbConnect extends AsyncTask<Void , Void , Void> {
        boolean isConnect = false;

        JSONArray  jsonarray;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(ServerProperty.getServer() + "api/labels/checkDbConnect");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Connection", "close");
                con.setConnectTimeout(5000); // Timeout 5 seconds.
                con.setReadTimeout(5000);

                con.connect();

                if(con.getResponseCode() == HttpURLConnection.HTTP_OK) // Successful response.
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String response = br.readLine();

                    jsonarray = new JSONArray(response);

                    //Log.d(TsdProperty.TAG, String.valueOf(con.getResponseCode()));
                    isConnect = true;
                }
                else {
                    //Log.d(TsdProperty.TAG, "Нет интернета");
                    isConnect = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                //Log.d(TsdProperty.TAG, ex.toString());
                isConnect = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(isConnect)
            {
                String outputDb = "База данных:\n";
                try {
                    for(int i=0; i < jsonarray.length(); i++) {
                        JSONObject jsonobject = jsonarray.getJSONObject(i);
                        String nameDb = jsonobject.getString("nameDb");
                        String isConnect = String.valueOf(jsonobject.getBoolean("isConnect"));

                        if(isConnect.equals("true"))
                            isConnect = "Есть";
                        else if(isConnect.equals("false"))
                            isConnect = "Нет";
                        else
                            isConnect = "Неизвестно";

                        outputDb += "       " + nameDb + ": " + isConnect.toString() + "\n";
                    }

                    txtConnectDb.setText(outputDb);
                    txtConnectDb.setTextColor(getResources().getColor(R.color.colorResultGreen));
                }
                catch (Exception ex)
                {
                    txtConnectDb.setText(ex.toString());
                    txtConnectDb.setTextColor(getResources().getColor(R.color.colorErrorRed));
                }
            }
            else
            {
                txtConnectDb.setText("База данных: Нет");
                txtConnectDb.setTextColor(getResources().getColor(R.color.colorErrorRed));
            }

            MessageAlert.ShowToast(SettingsActivity.this, "Проверка завершена!");
        }
    }
    //Обновить приложение
    public class updateApp extends AsyncTask<Void , Void , Void> {

        Context context;
        boolean newVersion = false;

        public updateApp(Context _context) throws PackageManager.NameNotFoundException {
            this.context = _context;

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
            apkVerCur = pInfo.versionName;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            newVersion = isNewVersion();

            if(newVersion)
                getApk();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if(!newVersion)
                MessageAlert.InfoMsg(context, "Обновления не найдены!");

            super.onPostExecute(result);
        }

        private boolean isNewVersion(){

            //если не пусто и если версии не равные, то берем последнию с сервера
            if(!apkVerCur.isEmpty() && !apkVerServer.equals(apkVerCur))
                return true;
            else
                return false;
        }

        private void getApk(){
            try {
                String NameApp = "app-release.apk";
                URL url = new URL(ServerProperty.getServer() + "api/update/GetApk");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setConnectTimeout(5000); // Timeout 5 seconds.
                conn.setReadTimeout(5000);
                conn.connect();

                //ЕСЛИ БУДЕТ ПИСАТЬ, ЧТО ДОСТУП ЗАКРЫТ, ТО НАДО В САМОМ АНДРОЙДЕ РАЗРЕШИТЬ ХРАНИЛИЩЕ:
                // НАСТРОЙКИ - ПРИЛОЖЕНИЯ - СКАН.БИРОК 2.0 - Разрешения - вкл.хранилище
                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String PATH = Environment.getExternalStorageDirectory() + "/Download/";
                    File myDir = new File(PATH);
                    myDir.mkdirs();
                    File outputFile = new File(myDir, NameApp);

                    if (outputFile.exists()) {
                        outputFile.delete();
                    }

                    FileOutputStream fos = new FileOutputStream(outputFile);

                    InputStream is = conn.getInputStream();

                    byte[] buffer = new byte[1024];
                    int len1 = 0;

                    while ((len1 = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len1);
                    }

                    fos.close();
                    is.close();

                    File file = new File(Environment.getExternalStorageDirectory() + "/Download/" + NameApp);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri downloaded_apk  = FileProvider.getUriForFile(SettingsActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                    intent.setDataAndType(downloaded_apk, "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intent);
                }
                else{
                    MessageAlert.ErrorMessage(context, "Ошибка при обновление приложения: httpError - " + conn.getResponseCode() );
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                MessageAlert.ErrorMessage(context, "Ошибка при обновление приложения: " + ex.toString() );
            }
        }
    }
    //получить текущую версию приложения и версию приложения на сервере
    private class GetVersionApp_Server extends AsyncTask<Void , Void , Void> {

        private Context context;
        String error = "";

        public GetVersionApp_Server(Context _context) throws PackageManager.NameNotFoundException {
            this.context = _context;

            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
            apkVerCur = pInfo.versionName;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            setVersion();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(error.isEmpty())
            {
                txtGetVersion.setText("Текущая версия:\n" + apkVerCur + " | " + apkVerServer);
            }
            else {
                MessageAlert.ErrorMessage(context, error);
            }
        }

        private void setVersion() {
            HttpURLConnection conn = null;

            try {
                URL url = new URL(ServerProperty.getServer() + "/api/update/curVersion");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Connection", "close");
                conn.setConnectTimeout(5000); // Timeout 5 seconds.
                conn.setReadTimeout(5000);
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) // Successful response.
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    apkVerServer = br.readLine();
                    br.close();
                } else {
                    error = "Ошибка при получении версии apk файла c сервера: " + String.valueOf(conn.getResponseCode());
                    //MessageAlert.ErrorMessage(context, "Ошибка при получении версии apk файла c сервера: " + String.valueOf(conn.getResponseCode()));

                }
            } catch (Exception ex) {
                ex.printStackTrace();
                error = "Ошибка при получении версии apk файла c сервера: " + ex.toString();
                //MessageAlert.ErrorMessage(context, "Ошибка при получении версии apk файла c сервера: " + ex.toString());
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }
    }

    //включить хранилище на андройд этому приложению
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
