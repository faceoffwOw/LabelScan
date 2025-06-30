package test.test.bkoclient_tsd_v02.Transfer;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import test.test.bkoclient_tsd_v02.EnterOrderNumber;
import test.test.bkoclient_tsd_v02.HelpersClass.DateTime_my;
import test.test.bkoclient_tsd_v02.HelpersClass.Math_EV;
import test.test.bkoclient_tsd_v02.Inventory.DataAdapter;
import test.test.bkoclient_tsd_v02.Inventory.MainInventory;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabel_fromTSDtoAx;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabels;
import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.TsdProperty;
import test.test.bkoclient_tsd_v02.Models.LabelsAx;
import test.test.bkoclient_tsd_v02.QrCodeActivity;
import test.test.bkoclient_tsd_v02.R;
import test.test.bkoclient_tsd_v02.Settings.SettingsActivity;
import test.test.bkoclient_tsd_v02.Transfer.Models.TransferLabel;

public class MainTransfer extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private String Server = ServerProperty.getServer();

    CustomBroadcastReceivers CustomBroadcastReceivers = new CustomBroadcastReceivers();
    //DataTransferAdapter adapterTransfer;
    Spinner spinCheckpoint;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    TransferDataAdapter transferDataAdapter;
    List<TransferLabel> transferLabels = new ArrayList<>();
    private List<String> listCheckpoints = new ArrayList<String>();
    private String checkPointName;
    String msg;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_transfer);

        setTitle("Перемещение");

        spinCheckpoint = (Spinner)findViewById(R.id.spinCheckpoint);

        RecyclerView recyclerView = findViewById(R.id.rvTransferLabels);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        transferDataAdapter = new TransferDataAdapter(MainTransfer.this, transferLabels);
        recyclerView.setAdapter(transferDataAdapter);

        try {
            //Ожидание выполнения doInBackground иначе выполняется асинхронно
            String result = new MyAsyncTask_GetCheckpointList(this).execute().get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Create an adapter as shown below
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_list, listCheckpoints);
        adapter.setDropDownViewResource(R.layout.spinner_list);

        // Set the adapter to the Spinner
        spinCheckpoint.setAdapter(adapter);
        spinCheckpoint.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(TsdProperty.SCAN_DECODING_BROADCAST);
        registerReceiver(CustomBroadcastReceivers, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(CustomBroadcastReceivers);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        checkPointName = parent.getItemAtPosition(pos).toString();
        //MessageAlert.InfoMsg(this, "Выбрано " + checkPointName);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback.
    }

    class MyAsyncTask_GetCheckpointList extends AsyncTask<String, String, String> {
        Context context;
        ProgressDialog mProgressDialog;

        public MyAsyncTask_GetCheckpointList(Context _context)
        {
           this.context = _context;
            mProgressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            //mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Получаем список КПП...");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection conn = null;

            try {
                String output;
                String Query = Server + "api/transfer/getCheckpointList/";
                conn = ServerProperty.Get(Query);

                //MessageAlert.InfoMsg(context, String.valueOf(conn.getResponseCode()));

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    listCheckpoints = gson.fromJson(output, List.class);
                    listCheckpoints.add(0, "Не выбрано");

                    conn.disconnect();
                }
            }
            catch (SocketTimeoutException exTimeOut){
                msg = "Вышло время подключение! Попробуйте перезайти в 'Перемещение' eще раз!\n" + exTimeOut;
                MessageAlert.ErrorMessage(context, msg);
            }
            catch (Exception ex) {
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getCheckpointList(),  " + ex.toString();
            } finally {
                conn.disconnect();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        }
    }

    //обработка нажатия кнопки скан на ТСД
    public class CustomBroadcastReceivers extends BroadcastReceiver {
        private String barcode;
        private String[] subStr;

        public String dateScan;

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if(!SettingsActivity.isNetworkConnected(context))
                {
                    MessageAlert.ErrorMessage(context, "Проверьте подключение к интернету!");
                    return;
                }

                if(checkPointName == null || listCheckpoints.isEmpty())
                {
                    MessageAlert.ErrorMessage(context, "Не загрузился список КПП! Проверьте подключение к интернету и перезайдите! ");
                    return;
                }

                if(checkPointName.isEmpty() || checkPointName == "Не выбрано")
                {
                    MessageAlert.ErrorMessage(context, "Не указан № КПП!");
                    return;
                }

                // Получаем тип и сам штрих код помещаем в переменные
                barcode = intent.getStringExtra(TsdProperty.SCAN_DECODING_DATA);
                subStr = barcode.split("\\|");

                if(subStr[0].contains("СК4"))
                {
                    new MyAsyncTask_AddTransferLabel(context, barcode).execute();
                }
                else
                {
                    MessageAlert.ErrorMessage(context, "Неизвестный QR-Code");
                }

            }
            catch (Exception ex) {
                MessageAlert.ErrorMessage(MainTransfer.this, "Method - Transfer onReceive() " + ex.toString());
            }
        }
    }

    class MyAsyncTask_AddTransferLabel extends AsyncTask<String, String, String> {
        private Context context;
        private String barcode;
        private TransferLabel label;
        private boolean isSend = false;
        ProgressDialog mProgressDialog;
        String dateScanOutput;

        public MyAsyncTask_AddTransferLabel(Context _context, String _barcode)
        {
            this.context = _context;
            this.barcode = _barcode;

            mProgressDialog = new ProgressDialog(_context);
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute() {
            mProgressDialog.setTitle("Загрузка!");
            mProgressDialog.setMessage("Отправка данных...");
            mProgressDialog.show();

            super.onPreExecute();

            String[] subStr = barcode.split("\\|");

            String journalId = subStr[0];
            String fromInventLocationId = subStr[1];
            String toInventLocationId = subStr[2];

            Date dateScan = new Date();
            DateFormat formatSQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            DateFormat formatOutput = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String strDateScan = formatSQL.format(dateScan);
            dateScanOutput = formatOutput.format(dateScan);

            label = new TransferLabel(journalId, fromInventLocationId, toInventLocationId, checkPointName, strDateScan, false);
        }

        @Override
        protected String doInBackground(String... strings) {
            if(label != null)
                PostLabelCheckPointTSDtoAx(label);
            else
                MessageAlert.ErrorMessage(context, "Ошибка обработки Qr-кода!");

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            super.onPostExecute(result);

            if(isSend)
            {
                //обновим данные
                if(transferLabels.size() >= 10)
                {
                    transferLabels.remove(9);
                }

                transferLabels.add(0, label);
                linearLayoutManager.scrollToPositionWithOffset(0, 0);

                transferDataAdapter.notifyDataSetChanged();

                //проиграем звук сканирования
                MessageAlert.SoundOK(context);
                //MessageAlert.SuccesMsg(_context, msg, true);
            }
            else
                MessageAlert.ErrorMessage(context, msg);
        }

        private void PostLabelCheckPointTSDtoAx(TransferLabel _label) {
            isSend = false;

            HttpURLConnection conn = null;

            try {
                String Query = ServerProperty.getServer() + "api/Transfer";
                conn = ServerProperty.Post(Query);

                Gson gson = new Gson();
                String jsonString  = gson.toJson(_label);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),"UTF-8"));
                //добавим бирку в репликационную таблицу, если все норм то ниже добавляем в список
                out.write(jsonString);
                out.flush();
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    isSend = true;
                    msg = String.format("Требование-накладная №: %s", _label.getJournalId());
                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){//проверка на дубликат бирок
                    msg = String.format("Требование-накладная %s уже была отсканирована!", _label.getJournalId(), conn.getResponseCode());
                    isSend = false;
                }
                else {
                    msg = String.format("Произошла ошибка при отправке, HttpCode=%s", conn.getResponseCode());
                    isSend = false;
                }
            }
            catch (SocketTimeoutException exTimeOut){
                isSend = false;
                msg = "Вышло время подключение! Попробуйте еще раз!\n" + exTimeOut;
            }
            catch (Exception ex){
                ex.printStackTrace();
                msg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }
    }
}
