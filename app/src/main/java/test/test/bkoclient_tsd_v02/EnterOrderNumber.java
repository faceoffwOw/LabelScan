package test.test.bkoclient_tsd_v02;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.TsdProperty;
import test.test.bkoclient_tsd_v02.Models.SalesLine;

public class EnterOrderNumber extends AppCompatActivity {

    private String Server = ServerProperty.getServer();

    EditText editTxtInventLocation, editTextNomNaryd;

    String NomNaryd;
    String InventLocationId;
    Button btnSubmit;
    ArrayList<SalesLine> ListSalesLines = new ArrayList<SalesLine>();

    CustomBroadcastReceivers CustomBroadcastReceivers = new CustomBroadcastReceivers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_order_number);

        editTxtInventLocation = (EditText)findViewById(R.id.editTxtInventLocation);
        editTextNomNaryd = (EditText)findViewById(R.id.editTextNomNaryd);

        //editTxtInventLocation.setText("Гп-92");
        //editTextNomNaryd.setText("327142.0");

        btnSubmit = (Button) findViewById(R.id.btnOK);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode){
            case KeyEvent.KEYCODE_1://кнопка 1 на клавиатуре ("Потвердить")
                btnSubmit.performClick();//произвести нажатие кнопки
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(TsdProperty.SCAN_DECODING_BROADCAST);
        registerReceiver(CustomBroadcastReceivers, filter);

        //ОТСКАНИРУЕМ БИРКУ
        //registerReceiver(CustomBroadcastReceivers, new IntentFilter(TsdProperty.SCAN_DECODING_BROADCAST));
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(CustomBroadcastReceivers);
    }

    public void btnSubmit_Click(View v) throws ExecutionException, InterruptedException {

        NomNaryd = editTextNomNaryd.getText().toString();
        InventLocationId = editTxtInventLocation.getText().toString();

        if (editTextNomNaryd.length() > 10) {
            MessageAlert.ErrorMessage(EnterOrderNumber.this, "Номер наряда не должен превышать 10 знаков!");
            return;
        }
        if (editTextNomNaryd.length() <= 0) {
            MessageAlert.ErrorMessage(EnterOrderNumber.this, "Укажите номер наряда!");
            return;
        }
        if (editTxtInventLocation.length() <= 0) {
            MessageAlert.ErrorMessage(EnterOrderNumber.this, "Укажите склад!");
            return;
        }

        new MyAsyncTask_getSalesLine(EnterOrderNumber.this, NomNaryd).execute().get();
    }


    class MyAsyncTask_getSalesLine extends AsyncTask<String, String, String> {

        String NumNaryd;
        Context context;
        String msg;
        Boolean isSend = false;
        private ProgressDialog progress;

        public MyAsyncTask_getSalesLine(Context _context, String _NumNaryd) {

            this.NumNaryd = _NumNaryd;//номер бирки
            this.context = _context;
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {

            getSalesLine(context, NumNaryd);

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            if(isSend){
                //переходим на новое Activity QrCode
                Intent intent = new Intent(context, QrCodeActivity.class);
                intent.putExtra("NomNaryd", NomNaryd);
                intent.putExtra("InventLocationId", InventLocationId);

                intent.putParcelableArrayListExtra("ListSalesLines", ListSalesLines);

                startActivity(intent);
            }
            else{
                MessageAlert.ErrorMessage(context, msg);
            }
        }

        private Boolean getSalesLine(Context _context, String _NomNaryd){

            //Для того чтобы не тянуть данные каждый раз с сервера, нам они нужны только на первый пик с ТСД
            //а дальше уже будем проверять по заполненому списку
            HttpURLConnection conn = null;

            try {
                //поищем по всем нарядам с точками, пока только для ОП-32 цеха
                /*if(InventLocationId.toUpperCase().equals("ГП-92"))
                {
                    _NomNaryd = _NomNaryd.substring(0, _NomNaryd.indexOf("."));
                }*/

                String Query = Server + "api/labels/getSalesLine/" + _NomNaryd;

                String output;
                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    Type collectionType = new TypeToken<Collection<SalesLine>>(){}.getType();
                    ListSalesLines = gson.fromJson(output, collectionType);

                    for (SalesLine salesLine : ListSalesLines)
                    {
                        SalesLine.addSalesLine(salesLine);
                    }

                    isSend = true;
                }
                else if (conn.getResponseCode() == 404)
                {
                    isSend = false;
                    msg = "Проверте номер наряда, он должен быть таким же как в аксапте!\nПроверьте статус наряда, cтатус должен быть 'Наряд'!";
                }
                else
                {
                    isSend = false;
                    msg = "Method - getSalesLine(),  " + String.valueOf(conn.getResponseCode());
                }
            }
            catch (SocketTimeoutException exTimeOut){
                isSend = false;
                msg = "Вышло время подключение! Попробуйте еще раз!\n" + exTimeOut;
            }
            catch (Exception ex)
            {
                isSend = false;
                msg = "Method - getSalesLine() " + ex;
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
            return isSend;
        }
    }

    //обработка нажатия кнопки скан на ТСД
    public class CustomBroadcastReceivers extends BroadcastReceiver
    {
        // Получаем тип и сам штрих код помещаем в переменные
        private String barcode;
        private String[] subStr;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            try {
               // Получаем тип и сам штрих код помещаем в переменные
                barcode = intent.getStringExtra(TsdProperty.SCAN_DECODING_DATA);

                //Зз1-280644|311501.0|ГП-07
                subStr = barcode.split("\\|");

                if(subStr.length <=2)//разные бирки на нарядах
                {
                    EnterOrderNumber.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            editTextNomNaryd.setText(subStr[1]);//получим номер наряда
                        }
                    });

                    MessageAlert.SuccesMsg(EnterOrderNumber.this, "Наряд отсканирован", false);
                }
                else
                {
                    EnterOrderNumber.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            editTxtInventLocation.setText(subStr[2]);//Получим склад
                            editTextNomNaryd.setText(subStr[1]);//получим номер наряда

                            MessageAlert.SuccesMsg(EnterOrderNumber.this, "Наряд отсканирован", false);
                        }
                    });
                }
            }
            catch (Exception ex){
                MessageAlert.ErrorMessage(EnterOrderNumber.this, ex.toString());
            }
        }
    }
}
