package test.test.bkoclient_tsd_v02;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import test.test.bkoclient_tsd_v02.HelpersClass.Math_EV;
import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.TsdProperty;
import test.test.bkoclient_tsd_v02.Models.InventJournalTransTSD;
import test.test.bkoclient_tsd_v02.Models.Labels;
import test.test.bkoclient_tsd_v02.Models.LabelsAx;
import test.test.bkoclient_tsd_v02.Models.LocationAx;
import test.test.bkoclient_tsd_v02.Models.SalesLine;
import test.test.bkoclient_tsd_v02.Models.WmsLocation;


public class QrCodeActivity extends AppCompatActivity {

    private String Server = ServerProperty.getServer();

    private static final String constIdMark = "idBKO=";

    CustomBroadcastReceivers CustomBroadcastReceivers = new CustomBroadcastReceivers();
    List<SalesLine> ListSalesLines = new ArrayList<SalesLine>();//получим список с нарядов
    ArrayList<SalesLine> ListSalesLinesUpdate = new ArrayList<SalesLine>();
    //ArrayList<SalesLine> ListSalesLines;
    LabelsAx labelAx;//данные по бирки из аксапты
    Labels labels;//данные по бирки от гриши

    String numLabel_qrCode, batchData_qrCode, SalesQty_qrCode;

    TextView txtNomNaryd, txtNumber, txtName, txtFrakcia, txtWeight, txtBatch, txtDate, txtCntScan, txtCntForShip;;
    Spinner spinFromLocation, spinFromWmsLocation;//выпадающее списки
    List<String> listSpinerLocation;//Список складов
    List<String> listSpinerFromWmsLocation;//Список местоположений

    String NomNaryd;//номер наряда
    String mainInventLocationId = "ГП-04";//склад отгрузки

    Boolean oldQrCode = false;
    Boolean QrCodeCust = false;

    Button btnSend;//кнопка отправить
    Switch switchAuto;//переключатель на авто

    double TotalSalesQty;//Итого тонн
    int TotalSht;//Итого шт.
    int TotalCntScan;//Итого поддонов
    TableLayout tableResult;
    Integer SalesType;//статус наряда

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        spinFromLocation = (Spinner)findViewById(R.id.spinFromLocation );
        spinFromWmsLocation = (Spinner) findViewById(R.id.spinCheckpoint);

        //получим список с аксапты, с прошлой формы
        ListSalesLines = getIntent().getParcelableArrayListExtra("ListSalesLines");
        //Получим номер наряда, с прошлой формы
        NomNaryd = getIntent().getExtras().getString("NomNaryd");//номер наряда
        //Получим склад, с прошлой формы
        mainInventLocationId  = getIntent().getExtras().getString("InventLocationId");//Склад отгрузки

        //получим кол-во отсканированных бирок
        new MyAsyncTask_GetCntScanServer(this, NomNaryd, false).execute();
        //заполним список складов
        new MyAsyncTask_GetLocationServer(this).execute();

        //поиск контроллов на форме, чтобы к ним можно обращаться
        InitfindViewById();
    }
    //нажатие горячих клавиш на ТСД
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode){
            /*case KeyEvent.KEYCODE_0://кнопка 0 на клавиатуре (вкл "авто")
                if(switchAuto.isChecked())
                    switchAuto.setChecked(false);
                else
                    switchAuto.setChecked(true);

                return true;
             */
            case KeyEvent.KEYCODE_1://кнопка 1 на клавиатуре ("Потвердить")
                btnSend.performClick();//произвести нажатие кнопки
                return true;

            case KeyEvent.KEYCODE_3:
                //new MyAsyncTask_getSalesLineUpdate(this,NomNaryd).getSalesLineUpdate(this,NomNaryd);
                new MyAsyncTask_GetCntScanServer(this, NomNaryd, true).execute();

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

    //КНОПКА ЧТОБЫ ОТПРАВИТЬ ДАННЫЕ НА СЕРВЕР (Отправить)
    public void btnSend_Click(View v){
        ArrayList<String> arrayList = new ArrayList<String>();

        arrayList.add(batchData_qrCode);

        if (txtNomNaryd.length() <= 0 ||  spinFromLocation.getSelectedItem().toString().isEmpty() || spinFromWmsLocation.getSelectedItem().toString().isEmpty())
            MessageAlert.ErrorMessage(this, "Проверте, что указан номер наряда, склад и местоположение!");
        else
        {
            if(QrCodeCust)
                MessageAlert.ErrorMessage(this, "Новый QR-код не отправлен!");
            else if(!oldQrCode && !QrCodeCust)
                new MyAsyncTask_GetLabelAndPostInventJourServer(numLabel_qrCode, batchData_qrCode,this).execute();
            else
                new MyAsyncTask_OldQrCodeGetLabelAndPostInventJourServer(numLabel_qrCode, this).execute();
        }
    }
    //Удалить все отсканированные бирки
    public void btnDeleteLabels_Click(View v) {
        //Создаем AlertDialog
        final AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(false)
                .setMessage("Удалить все отсканированные бирки?")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new MyAsyncTask_deleteLabels(mDialogBuilder.getContext()).execute();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();

        //и отображаем его:
        alertDialog.show();
    }
    //инициализация переменных
    private void InitfindViewById(){
        txtNomNaryd = (TextView) findViewById(R.id.txtNomNaryd);
        txtNumber = (TextView)findViewById(R.id.txtNumber);//номер бирки
        txtName = (TextView)findViewById(R.id.txtName);//Наименование
        txtFrakcia = (TextView)findViewById(R.id.txtFrakcia);//Фракция
        txtWeight = (TextView)findViewById(R.id.txtWeight);//масса
        txtBatch = (TextView)findViewById(R.id.txtBatch);//Партия
        txtDate = (TextView)findViewById(R.id.txtDate);//Дата
        txtCntScan = (TextView)findViewById(R.id.txtCntScan);//Кол-во отсканированных бирок
        txtCntForShip = (TextView)findViewById(R.id.txtCntForShip);//Кол-во бирок по наряду
        tableResult = (TableLayout)findViewById(R.id.tableResult);

        switchAuto = (Switch) findViewById(R.id.switchAuto);//переключение на авто
        btnSend = (Button)findViewById(R.id.btnSend);//кнопка отправить

        txtNomNaryd.setText("Номер наряда: " + NomNaryd);
    }

    //получить кол-во отсканированных бирок
    class MyAsyncTask_GetCntScanServer extends AsyncTask<String, String, String> {

        boolean isSend = false;
        String ErrorMsg;
        String NomNaryd;
        String outputCntScan = "";
        Double outputCntForShip = 0.0d;

        boolean update;

        Context context;

        Collection<InventJournalTransTSD> Counts = null;

        public MyAsyncTask_GetCntScanServer(Context _context, String _NomNaryd, boolean _update)
        {
            this.context = _context;
            this.NomNaryd = _NomNaryd;
            this.update = _update;
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {

            HttpURLConnection conn = null;

            try {
                String Query = Server + "api/labels/cntScanAxV2/" + NomNaryd;

                conn = ServerProperty.Get(Query);
                String output;

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    Type collectionType = new TypeToken<Collection<InventJournalTransTSD>>(){}.getType();
                    Counts = gson.fromJson(output, collectionType);

                    //Занулим итоги, для пересчета
                    if(update){
                        TotalSalesQty = 0;
                        TotalCntScan = 0;
                        TotalSht = 0;
                    }

                    for (InventJournalTransTSD jour : Counts) {
                        //mapCntScan.put(jour.getBatchNum(), jour.getCount());
                        /*mapSaleQtyScan.put(jour.getBatchNum(), Math_EV.decround(jour.getSalesQty(), 3));
                        mapCntScan.put(jour.getBatchNum(), jour.getCount());
                        */
                        //mapCntScan.put(jour.getBatchNum() /*+ "           " + jour.getConfigId()*/, jour.getCount());

                        TotalSalesQty += Math_EV.decround(jour.getSalesQty(),3);
                        TotalCntScan += jour.getCount();
                        TotalSht += jour.getQtyTrayIn();
                    }

                    isSend = true;
                }
            }
            catch (Exception ex) {
                //Log.e(TsdProperty.TAG, ex.toString());
                ErrorMsg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            super.onPostExecute(result);

            if(isSend) {
                //удалим все строки и добавим заново. ГП-92 жаловались на то, что итоги правильные, а строчек не хватает
                if(update){
                    while (tableResult.getChildCount() > 1) {
                        TableRow row =  (TableRow)tableResult.getChildAt(1);
                        tableResult.removeView(row);
                    }
                }

                //Выведем при загрузке кол-во отсканированных бирок
                if(Counts != null && Counts.size() > 0) {
                    for (InventJournalTransTSD jour : Counts) {
                        //Log.i(TsdProperty.TAG, String.valueOf(jour.getQtyTrayIn()));
                        addRow(context, jour.getBatchNum(), jour.getConfigId(), jour.getCount(), jour.getQtyTrayIn(), jour.getSalesQty(), true);
                    }
                }
                //outputCntScan += String.format("ИТОГО: %f т.; %d шт.; %d подд.;", Math_EV.decround(TotalSalesQty, 3), TotalSht, TotalCntScan);

                outputCntScan += "ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; " + TotalCntScan + " подд.; ";
                //outputCntScan += "ИТОГО: " + TotalCntScan + " подд.";
                //Кол-во тонн на отгрузку
                for (SalesLine lines: ListSalesLines)
                {
                    outputCntForShip += lines.getMarketingSalesQty();
                }

                if(txtCntScan != null)
                    txtCntScan.setText(outputCntScan);

                if(txtCntForShip != null)
                    txtCntForShip.setText("кол-во: " + Math_EV.decround(outputCntForShip, 3));
            }
            else
                MessageAlert.ErrorMessage(context, ErrorMsg);
        }
    }
    //получить склады с сервера
    class MyAsyncTask_GetLocationServer extends AsyncTask<String, String, String> {

        boolean isSend = false;
        String ErrorMsg;

        ProgressDialog mProgressDialog;
        Context context;

        public MyAsyncTask_GetLocationServer(Context _context)
        {
            this.context = _context;
            mProgressDialog = new ProgressDialog(context);
            //список складов...нужно добавить пусто, иначе не будет выбираться
            listSpinerLocation = new ArrayList<>();
            listSpinerLocation.add("");

        }
        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute(){

            //mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle("Загрузка!");
            mProgressDialog.setMessage("Получаем склады...");
            mProgressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {

            HttpURLConnection conn = null;

            try {

                String Query = Server + "api/labels/Location/" + mainInventLocationId;
                String output;

                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    Type collectionType = new TypeToken<Collection<LocationAx>>(){}.getType();
                    Collection<LocationAx> locations = gson.fromJson(output, collectionType);

                    for (LocationAx location : locations) {
                        listSpinerLocation.add(location.getInventLocationId());
                    }

                    isSend = true;
                }
            }
            catch (Exception ex) {
                //Log.e(TsdProperty.TAG, ex.toString());
                ErrorMsg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

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
                //заполним местоположение
                //адаптер на склады
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, listSpinerLocation);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //заполним склады с
                spinFromLocation.setAdapter(adapter);
                spinFromLocation.setPrompt("Со склада");
                spinFromLocation.setSelection(1);

                //событие срабатывающее на смену склада "со склада" и заполняющее местоположение "с"
                spinFromLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {

                        if(spinFromLocation.getSelectedItemId() == 0)
                            return;

                        new MyAsyncTask_GetWmsLocationServer(context).execute();
                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            else {
                MessageAlert.ErrorMessage(context, ErrorMsg);
            }
        }
    }
    //получить местоположение с сервера
    class MyAsyncTask_GetWmsLocationServer extends AsyncTask<String, String, String> {
        String msg = "";
        String LocationId;
        boolean fromWmsLocation;
        Context context;
        boolean isSend = false;

        public MyAsyncTask_GetWmsLocationServer(Context _context)
        {
            this.LocationId = spinFromLocation.getSelectedItem().toString().split(",")[0];
            this.context = _context;

            //список местоположений "с"
            listSpinerFromWmsLocation = new ArrayList<>();
            listSpinerFromWmsLocation.add("");

        }
        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {


            HttpURLConnection conn = null;

            try {
                listSpinerFromWmsLocation.clear();
                listSpinerFromWmsLocation.add("");

                String Query = Server + "api/labels/getWmsLocation/" + LocationId;
                String output;

                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    Type collectionType = new TypeToken<Collection<WmsLocation>>(){}.getType();
                    Collection<WmsLocation> WmsLocations = gson.fromJson(output, collectionType);

                    for (WmsLocation wmsLocation : WmsLocations) {
                        listSpinerFromWmsLocation.add(wmsLocation.getwMSLocationId());
                        //Log.i(TsdProperty.TAG, wmsLocation.getwMSLocationId());
                    }

                    isSend = true;
                }
                else{
                    msg = "method - GetWmsLocationServer" + String.valueOf(conn.getResponseCode());
                }

            }
            catch (Exception ex) {
                msg = "method - GetWmsLocationServer" + ex.toString();
                //Log.e(TsdProperty.TAG, ex.toString());
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(isSend) {
                ArrayAdapter<String> adapterWms = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, listSpinerFromWmsLocation);
                adapterWms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //заполним местоположение с
                spinFromWmsLocation.setAdapter(adapterWms);
                spinFromWmsLocation.setPrompt("Из ячейки");

                super.onPostExecute(result);

                spinFromWmsLocation.setSelection(1, false );
            }
            else
            {
                MessageAlert.ErrorMessage(context, msg);
            }
        }
    }

    //Получим бирку из аксапты из-за клиентских QR-кодов
    class MyAsyncTask_QrCodeCustGetLabel extends AsyncTask<String, String, String> {

        String numLabel;//номер бирки
        Context context;
        ProgressDialog mProgressDialog;
        String msg;
        LabelsAx custLabelAx;
        Date dateBatch;

        ArrayList<String> arrCustLabel = new ArrayList<String>();

        public MyAsyncTask_QrCodeCustGetLabel(String _numLabel_qrCode, Context _context) {
            this.numLabel = _numLabel_qrCode;
            this.context = _context;
            mProgressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {

            try {
                getCustLabel();//Найдем бирку в аксапте(ProdLabelsNum)
            }
            catch (Exception ex)
            {
                msg = ex.toString();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

            labelAx = new LabelsAx(custLabelAx);

            numLabel_qrCode = custLabelAx.getUnitId();//получим номер бирки
            batchData_qrCode = format.format(dateBatch);//получим дату выпуска партии
            SalesQty_qrCode = Float.toString(custLabelAx.getQtyGood());//получим нетто

            txtNumber.setText("Номер бирки: " + numLabel_qrCode);
            txtBatch.setText("Партия: "+ custLabelAx.getBatchNum());
            txtName.setText("Марка: "+ custLabelAx.getItemName());
            txtFrakcia.setText("Фракция: " + custLabelAx.getItemNumName());
            txtWeight.setText("Отвес: "+ SalesQty_qrCode);
            //txtDate.setText("Дата выпуска: "+ batchData_qrCode);

            //MessageAlert.InfoMsg(context, batchData_qrCode);
            new MyAsyncTask_GetLabelAndPostInventJourServer(numLabel_qrCode, batchData_qrCode, context).execute();
        }

        private void getCustLabel() {

            HttpURLConnection conn = null;

            try {
                String output;
                String Query = Server + "api/labels/LabelAx/" + numLabel;
                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    custLabelAx = gson.fromJson(output, LabelsAx.class);

                    conn.disconnect();

                    dateBatch = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(custLabelAx.getBatchDate());
                } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    //Log.e(TsdProperty.TAG, "Данной бирки нет в аксапте, позвоните 41-40");
                    msg = "Данной бирки нет в аксапте, позвоните 41-40";
                } else {
                    //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + String.valueOf(conn.getResponseCode()));
                    msg = "Method - getCustLabel(),  " + String.valueOf(conn.getResponseCode());
                }
            } catch (Exception ex) {
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getLabel(),  " + ex.toString();
            } finally {
                conn.disconnect();
            }
        }
    }

    //Получить бирку с аксапты, провереть в аксапте что все соответсвует наряду, все хорошо, то записать бирку в таблицу для дальнейшей обработке в наряд в аксапте
    class MyAsyncTask_GetLabelAndPostInventJourServer extends AsyncTask<String, String, String> {

        String numLabel;//номер бирки
        String BatchDate;//дата выпуска партии с qr Code
        Context context;
        ProgressDialog mProgressDialog;
        String fromLocation, fromWmsLocation;
        Boolean isSend = false;
        String msg;

        public MyAsyncTask_GetLabelAndPostInventJourServer(String _numLabel_qrCode, String _batchData_qrCode, Context _context)
        {
            this.numLabel = _numLabel_qrCode;//номер бирки
            this.BatchDate = _batchData_qrCode;//дата партии
            this.context = _context;
            mProgressDialog = new ProgressDialog(context);
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute(){
            //mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Отправка данных...");
            mProgressDialog.show();

            //обрубим по запятой и возьмем первый элемент массива
            fromLocation = spinFromLocation.getSelectedItem().toString().split(",")[0];
            fromWmsLocation = spinFromWmsLocation.getSelectedItem().toString().split(",")[0];

            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {

            try {
                if(!QrCodeCust) {
                    getLabel();//получим бирку из аксапты(диминой таблицы)
                }
                else{
                    postCustLabel();
                }
            }
            catch (Exception ex)
            {
                msg = ex.toString();
                isSend = false;
            }

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            super.onPostExecute(result);
            //внес изменения 30.06.2021
            //на ГП-07 иногда не правильно считало количество
            try {
                if(isSend){
                    toCountScanLabel(labelAx);

                    txtNumber.setTextColor(getResources().getColor(R.color.colorResultGreen));
                    MessageAlert.SuccesMsg(context, msg, false);
                }
                else
                {
                    MessageAlert.ErrorMessage(context, msg);
                    txtNumber.setTextColor(getResources().getColor(R.color.colorErrorRed));
                }
            }
            catch (Exception ex)
            {
                MessageAlert.ErrorMessage(context, msg + " " + ex.getMessage());
            }
        }

        private void getLabel(){
            //MessageAlert.InfoMsg(context, "Клиентская бирка!");
            //return;

            HttpURLConnection conn = null;

            try {
                String output;
                String Query = Server + "api/labels/LabelAx/" + numLabel;
                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    labelAx = gson.fromJson(output, LabelsAx.class);

                    conn.disconnect();

                    //проверим таже марка и номер изделия стоит в наряде в аксапте, что и в бирке...чтобы не отгрузили другое
                    isSend = getSalesLine(labelAx);

                    if (!isSend)
                        return;

                    //создадим строку для аксапты, перенос
                    PostJour(labelAx);
                } else if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    //Log.e(TsdProperty.TAG, "Данной бирки нет в аксапте, позвоните 41-40");
                    msg = "Данной бирки нет в аксапте, позвоните 41-40";
                } else {
                    //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + String.valueOf(conn.getResponseCode()));
                    msg = "Method - getLabel(),  " + String.valueOf(conn.getResponseCode());
                }
            } catch (Exception ex) {
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getLabel(),  " + ex.toString();
            } finally {
                conn.disconnect();
            }
        }

        private void postCustLabel()
        {
            try {
                isSend = getSalesLine(labelAx);

                if(!isSend)
                    return;

                //создадим строку для аксапты, перенос
                PostJour(labelAx);
            }
            catch (Exception ex)
            {
                msg = "Method - PostCustLabel(),  " + ex.toString();
            }
        }

        private void getNaryd(){
            HttpURLConnection conn = null;

            try {
                String output;
                String Query = Server + "api/labels/getNaryd/" + NomNaryd;
                conn = ServerProperty.Get(Query);

                if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    // Считываем json
                    JSONArray jsonArray = new JSONArray(output);

                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    SalesType = jsonObject.getInt("salesType");
                }
                else
                {
                    SalesType = 0;
                }
            }
            catch (Exception ex){
                msg = ex.toString();
            }
            finally {
                conn.disconnect();
            }
        }

        private boolean getSalesLine(LabelsAx _labelAx){
            boolean isAcces = false;

            for (SalesLine salesLine : ListSalesLines)
            {
                if(_labelAx.getItemId().equals(salesLine.getItemId()) && _labelAx.getConfigId().equals(salesLine.getConfigId()) /*&& _labelAx.getInventSerialId().equals(salesLine.getInventSerialId())*/)
                    isAcces = true;
                else
                    msg = "Вы пытаетесь отгрузить не то, что стоит в наряде!";
            }

            return isAcces;
        }

        private void PostJour(LabelsAx label){
            isSend = false;
            HttpURLConnection conn = null;
            try {
                String Query = Server + "api/labels";
                conn = ServerProperty.Put(Query);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));

                //создадим строку для журнала переноса
                InventJournalTransTSD jourTrans = new InventJournalTransTSD(label.getUnitId(), label.getItemId(), label.getBatchNum(), DateTime_my(BatchDate),
                        fromLocation, fromWmsLocation, label.getConfigId(), label.getJournalId(), 1, DateTimeNow_my(), NomNaryd, Math_EV.parseDouble(SalesQty_qrCode), label.getQtyTrayIn());

                Gson gson = new Gson();
                String jsonString  = gson.toJson(jourTrans);

                out.write(jsonString);
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // OK
                    isSend = true;
                    msg = String.format("Номер бирки: %s", label.getUnitId());
                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){//проверка на дубликат бирок
                    msg = String.format("Бирка уже была отсканирована!", conn.getResponseCode());
                    isSend = false;
                }
                else {
                    msg = String.format("Произошла ошибка при отправке, HttpCode=%s", conn.getResponseCode());
                    isSend = false;
                }
            }
            catch (Exception ex){
                //Log.e(TsdProperty.TAG, "new Method - PostJour: " + ex.toString());
                msg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }

        private void toCountScanLabel(LabelsAx _label) throws ParseException {

            TableLayout table = (TableLayout) findViewById(R.id.tableResult);

            //добавим один раз когда еще наряд ниразу не отсканирован, и выйдем
            if(table.getChildCount() <= 1) {
                addRow(context, _label.getBatchNum(), _label.getConfigId(), 1, _label.getQtyTrayIn(), _label.getQtyGood(), false);
                //Log.i(TsdProperty.TAG, "AddOne");
                TotalSalesQty += _label.getQtyGood();
                TotalSht += _label.getQtyTrayIn();
                TotalCntScan++;

                if(txtCntScan != null)
                    txtCntScan.setText("ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; "+ TotalCntScan + " подд.");

                return;
            }
            //поищем такую же партию
            TableRow findRow = FindTableRows(table, _label);

            //если нашли такую партию, то увеличем кол-во
            if(findRow != null) {
                TextView txtBatchNum = (TextView) findRow.getChildAt(0);
                TextView txtConfigId = (TextView) findRow.getChildAt(1);
                TextView txtCnt = (TextView) findRow.getChildAt(2);
                TextView txtSht = (TextView) findRow.getChildAt(3);
                TextView txtQty = (TextView) findRow.getChildAt(4);

                //Log.i(TsdProperty.TAG, "Update");
                int Cnt = Integer.parseInt(txtCnt.getText().toString()) + 1;
                int Sht = Integer.parseInt(txtSht.getText().toString()) + _label.getQtyTrayIn();
                double Qty = Double.parseDouble(txtQty.getText().toString()) + _label.getQtyGood();

                txtCnt.setText(String.valueOf(Cnt));
                txtSht.setText(String.valueOf(Sht));
                txtQty.setText(String.valueOf(Math_EV.decround(Qty, 3)));

                //outputCntScan += "ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalCntScan + " подд.";
                TotalSalesQty += _label.getQtyGood();
                TotalSht += _label.getQtyTrayIn();
                TotalCntScan++;

                if(txtCntScan != null)
                    txtCntScan.setText("ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; "+ TotalCntScan + " подд.");


            }//иначе добавим новую строчку с новой партией
            else
            {
                //Log.i(TsdProperty.TAG, "AddNew");
                addRow(context, _label.getBatchNum(), _label.getConfigId(), 1, _label.getQtyTrayIn(), _label.getQtyGood(), false);

                TotalCntScan++;
                TotalSalesQty += _label.getQtyGood();
                TotalSht += _label.getQtyTrayIn();

                if(txtCntScan != null)
                    txtCntScan.setText("ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; "+ TotalCntScan + " подд.");
            }
        }
    }
    //ПО СТАРЫМ БИРКАМ Получить бирку с аксапты, провереть в аксапте что все соответсвует наряду, все хорошо, то записать бирку в таблицу для дальнейшей обработке в наряд в аксапте
    class MyAsyncTask_OldQrCodeGetLabelAndPostInventJourServer extends AsyncTask<String, String, String> {

        String numLabel;//номер бирки
        Context context;
        ProgressDialog mProgressDialog;
        String fromLocation, fromWmsLocation;
        Boolean isSend = false;
        String msg;

        public MyAsyncTask_OldQrCodeGetLabelAndPostInventJourServer(String _numLabel, Context _context)
        {
            this.numLabel = _numLabel;//номер бирки
            this.context = _context;
            mProgressDialog = new ProgressDialog(context);
        }
        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute(){
            //mProgressDialog.setIndeterminate(true);
            mProgressDialog.setMessage("Отправка данных...");
            mProgressDialog.show();

            //обрубим по запятой и возьмем первый элемент массива
            fromLocation = spinFromLocation.getSelectedItem().toString().split(",")[0];
            fromWmsLocation = spinFromWmsLocation.getSelectedItem().toString().split(",")[0];

            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {

            getLabel();

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){
            try
            {
                if(mProgressDialog.isShowing())
                    mProgressDialog.dismiss();

                super.onPostExecute(result);

                if(isSend){
                   if(txtCntScan != null)
                        toCountScanLabel(labels);

                    txtNumber.setTextColor(getResources().getColor(R.color.colorResultGreen));
                    MessageAlert.SuccesMsg(context, msg, false);
                }
                else{
                    MessageAlert.ErrorMessage(context, msg);
                    txtNumber.setTextColor(getResources().getColor(R.color.colorErrorRed));
                }
            }
            catch (Exception ex)
            {
                MessageAlert.ErrorMessage(context, ex.toString());
            }
        }

        private void getLabel(){

            HttpURLConnection conn = null;

            try {
                String Query = Server + "api/labels/" + numLabel;
                URL url = new URL(Query);
                String output;

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);//15000
                conn.setConnectTimeout(10000);//15000
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    labels = gson.fromJson(output, Labels.class);
                    conn.disconnect();

                    //проверим таже марка и номер изделия стоит в наряде в аксапте, что и в бирке...чтобы не отгрузили другое
                    isSend = getSalesLine(labels, NomNaryd);

                    if(!isSend)
                        return;

                    //создадим строку для аксапты, перенос
                    PostJour(labels);

                    //Date currentDate = new Date();
                    // Форматирование времени как "день.месяц.год"
                    //DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy hh:mm:ss", Locale.getDefault());
                    //Log.i(TAG, String.valueOf(dateFormat.format(currentDate)));
                }
                else{
                    //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + String.valueOf(conn.getResponseCode()));
                    msg = "Method - getLabel(),  " + String.valueOf(conn.getResponseCode());
                }
            }
            catch (Exception ex) {
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getLabel(),  " + ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }

        private boolean getSalesLine(Labels _label, String _NomNaryd){
            boolean isAcces = false;

            for (SalesLine salesLine : ListSalesLines)
            {
                if(_label.getItemId().equals(salesLine.getItemId()) && _label.getConfigId().equals(salesLine.getConfigId()))
                    isAcces = true;
                else
                    msg = "Вы пытаетесь отгрузить не то, что стоит в наряде!";
            }

            return isAcces;
        }

        private void PostJour(Labels label){
            isSend = false;
            HttpURLConnection conn = null;

            try {
                String Query = Server + "api/labels";
                conn = ServerProperty.Put(Query);

                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
                //создадим строку для журнала переноса

                InventJournalTransTSD jourTrans = new InventJournalTransTSD(label.getUnitId().toString(), label.getItemId(), label.getBatchNum(), label.getBatchData(),
                        fromLocation, fromWmsLocation, label.getConfigId(), label.getLineRecId(), 1, DateTimeNow_my(), NomNaryd, Math_EV.parseDouble(SalesQty_qrCode), 1);

                Gson gson = new Gson();
                String jsonString  = gson.toJson(jourTrans);

                out.write(jsonString);
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // OK
                    isSend = true;
                    msg = String.format("Номер бирки: %s", label.getUnitId());
                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){//проверка на дубликат бирок
                    msg = String.format("Бирка уже была отсканирована!", conn.getResponseCode());
                    isSend = false;
                }
                else {
                    msg = String.format("Произошла ошибка при отправке, HttpCode=%s", conn.getResponseCode());
                    isSend = false;
                }
            }
            catch (Exception ex){
                //Log.e(TsdProperty.TAG, "OlD Method - PostJour: " + ex.toString());
                msg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }

        private void toCountScanLabel(Labels _label) throws ParseException {

            TableLayout table = (TableLayout) findViewById(R.id.tableResult);

            //добавим один раз когда еще наряд ниразу не отсканирован, и выйдем
            if(table.getChildCount() <= 1) {
                addRow(context, _label.getBatchNum(), _label.getConfigId(), 1, 1,1, false);
                //Log.i(TsdProperty.TAG, "AddOne");
                TotalSalesQty ++;
                TotalCntScan++;
                TotalSht++;

                if(txtCntScan != null)
                    txtCntScan.setText("ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; "+ TotalCntScan + " подд.");

                return;
            }
            //поищем такую же партию
            TableRow findRow = OldQrCodeFindTableRows(table, _label);

            //если нашли такую партию, то увеличем кол-во
            //если нашли такую партию, то увеличем кол-во
            if(findRow != null) {
                TextView txtBatchNum = (TextView) findRow.getChildAt(0);
                TextView txtConfigId = (TextView) findRow.getChildAt(1);
                TextView txtCnt = (TextView) findRow.getChildAt(2);
                TextView txtSht = (TextView) findRow.getChildAt(3);
                TextView txtQty = (TextView) findRow.getChildAt(4);

                //Log.i(TsdProperty.TAG, "Update");
                int Cnt = Integer.parseInt(txtCnt.getText().toString()) + 1;
                int Sht = Integer.parseInt(txtSht.getText().toString()) + 1;
                double Qty = Double.parseDouble(txtQty.getText().toString()) + 1;

                txtCnt.setText(String.valueOf(Cnt));
                txtSht.setText(String.valueOf(Sht));
                txtQty.setText(String.valueOf(Math_EV.decround(Qty, 3)));

                //outputCntScan += "ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalCntScan + " подд.";
                TotalCntScan++;
                TotalSht++;
                TotalSalesQty ++;

                if(txtCntScan != null)
                    txtCntScan.setText("ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; "+ TotalCntScan + " подд.");


            }//иначе добавим новую строчку с новой партией
            else
            {
                //Log.i(TsdProperty.TAG, "AddNew");
                addRow(context, _label.getBatchNum(), _label.getConfigId(), 1, 1,1, false);

                TotalCntScan++;
                TotalSht++;
                TotalSalesQty ++;

                if(txtCntScan != null)
                    txtCntScan.setText("ИТОГО: " + Math_EV.decround(TotalSalesQty, 3) + " т; " + TotalSht + " шт.; "+ TotalCntScan + " подд.");
            }
        }
    }
    //УДАЛИТЬ ОТСКАНИРОВАННЫЕ БИРКИ
    class MyAsyncTask_deleteLabels extends AsyncTask<String, String, String>{
        String msg = "";
        boolean send = false;
        Context context;

        public MyAsyncTask_deleteLabels(Context _context)
        {
            this.context = _context;
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected  String doInBackground(String... params) {
            HttpURLConnection conn = null;
            try {
                String Query = Server + "api/labels/delete/" + NomNaryd;
                conn = ServerProperty.Put(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    msg = "Удаление завершено!";
                    send  = true;
                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                {
                    msg = "Нет отсканированных бирок, удаление не завершено!";
                    send = false;
                }
                else {
                    msg = conn.getResponseCode() + "";
                    send = false;
                }
            }
            catch (Exception ex){
                msg = ex.toString() + "";
                send = false;
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

            return null;
        }
        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(send)
            {
                MessageAlert.SuccesMsg(context, msg, true);
                txtCntScan.setText("0.0 т.");
            }
            else
            {
                MessageAlert.ErrorMessage(context, msg );
            }
        }
    }

    class MyAsyncTask_getSalesLineUpdate extends AsyncTask<String, String, String> {

        String NumNaryd;
        Context context;
        String msg;
        Boolean isSend = false;
        private ProgressDialog progress;

        public MyAsyncTask_getSalesLineUpdate(Context _context, String _NumNaryd) {

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

            getSalesLineUpdate(context, NumNaryd);

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
                //intent.putExtra("InventLocationId", InventLocationId);

                intent.putParcelableArrayListExtra("ListSalesLinesUpdate", ListSalesLinesUpdate);

                startActivity(intent);
            }
            else{
                MessageAlert.ErrorMessage(context, msg);
            }
        }

        private Boolean getSalesLineUpdate(Context _context, String _NomNaryd){

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
        private String barcode;
        private String[] subStr;
        private String checkBarcode;

        @Override
        public void onReceive(Context context, Intent intent)
        {
            // Получаем тип и сам штрих код помещаем в переменные
            barcode = intent.getStringExtra(TsdProperty.SCAN_DECODING_DATA);
            checkBarcode = barcode.substring(0, 3);//нужна для того чтобы проверить какая это бирка, старая или новая

            //Log.e(TsdProperty.TAG, checkBarcode);

            //if(checkBarcode == "idBKO=")
            if(barcode.contains("idBKO="))
            {
                ScanCustLabel(context, intent, barcode);
                QrCodeCust = true;

            }
            else if (checkBarcode.contains("АО "))
            {
                ScanStandart(context, intent, barcode);
                oldQrCode = false;
                QrCodeCust = false;
            }
            else if (checkBarcode.contains("JSC")) {
                ScanGP07_OldQrCode(context, intent, barcode);
                oldQrCode = true;
                QrCodeCust = false;
                //Log.i(TsdProperty.TAG, "sds");
            }
            else
            {
                MessageAlert.ErrorMessage(context, "Неизвестный QR-Code");
            }
        }

        private  void ScanCustLabel(Context _context, Intent _intent, String _barcode)
        {
            try {
                String msg;
                int posNumLabel;
                posNumLabel = _barcode.indexOf(constIdMark) + constIdMark.length();

                numLabel_qrCode = _barcode.substring(posNumLabel, _barcode.length());

                if(switchAuto.isChecked()) {
                    if (txtNomNaryd.length() <= 0 || spinFromLocation.getSelectedItem().toString() == "" || spinFromWmsLocation.getSelectedItem().toString() == "")
                        MessageAlert.ErrorMessage(_context, "Проверте, что указан номер наряда, склад и местоположение!");
                    else
                        new MyAsyncTask_QrCodeCustGetLabel(numLabel_qrCode, _context).execute();
                }
            }
            catch (Exception ex)
            {
                //Log.e(TsdProperty.TAG, ex.toString());
                MessageAlert.ErrorMessage(_context, ex.toString());
            }
        }

        private  void ScanStandart(Context _context, Intent _intent, String _barcode)
        {
            try {

                subStr = _barcode.split("\\|");

                numLabel_qrCode = subStr[7];//получим номер бирки
                batchData_qrCode = subStr[6];//получим дату выпуска партии

                SalesQty_qrCode = subStr[5];//получим нетто

                QrCodeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //txtQrCodeResult.setText(barcode);
                        txtNumber.setText("Номер бирки: " + numLabel_qrCode);
                        txtBatch.setText("Партия: "+ subStr[1]);
                        txtName.setText("Марка: "+ subStr[2] );
                        txtFrakcia.setText("Фракция: " + subStr[3]);
                        txtWeight.setText("Отвес: "+ SalesQty_qrCode);
                        txtDate.setText("Дата выпуска: "+ batchData_qrCode );

                        txtNumber.setTextColor(getResources().getColor(R.color.colorDefaultGray));
                    }
                });

                if(switchAuto.isChecked()) {
                    if (txtNomNaryd.length() <= 0 || spinFromLocation.getSelectedItem().toString() == "" || spinFromWmsLocation.getSelectedItem().toString() == "")
                        MessageAlert.ErrorMessage(_context,"Проверте, что указан номер наряда, склад и местоположение!");
                    else
                        new MyAsyncTask_GetLabelAndPostInventJourServer(numLabel_qrCode, batchData_qrCode, _context).execute();
                }
            }
            catch (Exception ex)
            {
                //Log.e(TsdProperty.TAG, ex.toString());
                MessageAlert.ErrorMessage(_context, ex.toString());
            }
        }
        private void ScanGP07_OldQrCode(Context _context, Intent _intent, String _barcode)
        {
            try {
                subStr = barcode.split(" ");

                numLabel_qrCode = subStr[1];//получим номер бирки

                SalesQty_qrCode = subStr[4];//получим нетто
                SalesQty_qrCode = String.valueOf(Double.valueOf(SalesQty_qrCode) / 1000);//получим в тоннах

                QrCodeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //txtQrCodeResult.setText(barcode);
                        txtNumber.setText("Номер бирки: " + subStr[1]);
                        txtBatch.setText("Партия: "+ subStr[5]);
                        txtName.setText("Марка: "+ subStr[2]);
                        txtFrakcia.setText("Фракция: "+ subStr[3]);
                        txtWeight.setText("Отвес: "+ subStr[4]);
                        txtDate.setText("Дата выпуска: "+ subStr[6]);

                        txtNumber.setTextColor(getResources().getColor(R.color.colorDefaultGray));
                    }
                });

                if(switchAuto.isChecked()) {
                    if (txtNomNaryd.length() <= 0 || spinFromLocation.getSelectedItem().toString() == "" || spinFromWmsLocation.getSelectedItem().toString() == "")
                        MessageAlert.ErrorMessage(_context,"Проверте, что указан номер наряда, склад и местоположение!");
                    else
                        new MyAsyncTask_OldQrCodeGetLabelAndPostInventJourServer(numLabel_qrCode, _context).execute();
                }
            }
            catch (Exception ex)
            {
                Log.e(TsdProperty.TAG, ex.toString());
                MessageAlert.ErrorMessage(_context, ex.toString());
            }
        }
    }

    private String DateTimeNow_my() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String DateTime_my(String _Date) throws ParseException {

        DateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        String inputText = _Date;
        Date date = inputFormat.parse(inputText);
        String outputText = outputFormat.format(date);

        return outputText;
    }

    private void addRow(Context _context, String _BatchNum, String _ConfigId, int _Count, int _Sht, double _Qty, boolean check){

        TableRow row = new TableRow(_context);
        //партия
        TextView tvBatch = new TextView(_context);
        tvBatch.setTypeface(null, Typeface.BOLD);
        tvBatch.setGravity(Gravity.CENTER);
        tvBatch.setText(_BatchNum);
        row.addView(tvBatch);
        //номер изделия
        TextView tvConfig = new TextView(_context);
        tvConfig.setTypeface(null, Typeface.BOLD);
        tvConfig.setGravity(Gravity.CENTER);
        tvConfig.setText(_ConfigId);
        row.addView(tvConfig);
        //поддонов
        TextView tvCnt = new TextView(_context);
        tvCnt.setTypeface(null, Typeface.BOLD);
        tvCnt.setGravity(Gravity.CENTER);
        tvCnt.setText(String.valueOf(_Count));
        row.addView(tvCnt);
        //шт
        TextView tvSht = new TextView(_context);
        tvSht.setTypeface(null, Typeface.BOLD);
        tvSht.setGravity(Gravity.CENTER);
        tvSht.setText(String.valueOf(_Sht));
        row.addView(tvSht);
        //Тонн
        TextView tvQty = new TextView(_context);
        tvQty.setTypeface(null, Typeface.BOLD);
        tvQty.setGravity(Gravity.CENTER);
        tvQty.setText(String.valueOf(Math_EV.decround(_Qty, 3)));
        row.addView(tvQty);
        tableResult.addView(row);
    }

    private TableRow FindTableRows(TableLayout _table, LabelsAx _label){

        TableRow row = null;
        //TableLayout table = (TableLayout) findViewById(R.id.tableResult);

        for(int i = 1, Rows = _table.getChildCount(); i < Rows ; i++) {
            row = (TableRow) _table.getChildAt(i);
            TextView txtBatchNum = (TextView) row.getChildAt(0);
            TextView txtConfigId = (TextView) row.getChildAt(1);

            //если нет такой партии, то занулим строку
            if(!(_label.getBatchNum().equals(txtBatchNum.getText().toString()) && (_label.getConfigId().equals(txtConfigId.getText().toString()))))
                row = null;
            else
                return  row;
        }

        return row;
    }

    private TableRow OldQrCodeFindTableRows(TableLayout _table, Labels _label){

        TableRow row = null;
        //TableLayout table = (TableLayout) findViewById(R.id.tableResult);

        for(int i = 1, Rows = _table.getChildCount(); i < Rows ; i++) {
            row = (TableRow) _table.getChildAt(i);
            TextView txtBatchNum = (TextView) row.getChildAt(0);
            TextView txtConfigId = (TextView) row.getChildAt(1);
            //если нет такой партии, то занулим строку
            if(!(_label.getBatchNum().equals(txtBatchNum.getText().toString()) && (_label.getConfigId().equals(txtConfigId.getText().toString()))))
                row = null;
            else
                return  row;
        }

        return row;
    }
}
