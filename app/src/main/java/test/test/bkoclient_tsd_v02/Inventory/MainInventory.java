package test.test.bkoclient_tsd_v02.Inventory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import test.test.bkoclient_tsd_v02.HelpersClass.DateTime_my;
import test.test.bkoclient_tsd_v02.HelpersClass.Math_EV;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabels;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabel_fromTSDtoAx;
import test.test.bkoclient_tsd_v02.MainActivity;
import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.TsdProperty;
import test.test.bkoclient_tsd_v02.Models.LabelsAx;
import test.test.bkoclient_tsd_v02.Models.LocationAx;
import test.test.bkoclient_tsd_v02.Models.WmsLocation;
import test.test.bkoclient_tsd_v02.R;
import test.test.bkoclient_tsd_v02.Settings.SettingsActivity;

public class MainInventory extends AppCompatActivity {

    CustomBroadcastReceivers CustomBroadcastReceivers = new CustomBroadcastReceivers();

    List<InventLabels> inventLabels = InventLabels.getListInventLabel();

    DataAdapter adapter;

    //склад инвентаризации
    String mainInventLocationId = "ГП-92", prevInventLocationId = "",
            mainWmsLocationId = "00-000";
    //выпадающее списки
    Spinner spinFromLocation, spinFromWmsLocation;
    ArrayAdapter<String> adapterWms;
    //Список складов, Список местоположений
    List<String> listSpinerLocation, listSpinerFromWmsLocation;
    //ИТОГИ
    boolean checkedSum;

    //Кнопки FAB
    FloatingActionButton fabMain, fabAdd, fabGroupBy;
    float translationY = 100f;
    Boolean isMenuOpen = false;
    Boolean isInitLabels = false;

    private static final String constIdMark = "idBKO=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_inventory);

        setTitle("Инвентаризация");

        spinFromLocation = (Spinner)findViewById(R.id.spinFromLocation );
        spinFromWmsLocation = (Spinner) findViewById(R.id.spinCheckpoint);

        //Получаем вид с файла prompt.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(this);
        final View promptsView = li.inflate(R.layout.prompt, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final Spinner userlocationId = (Spinner) promptsView.findViewById(R.id.spinnerLocationId);

        String[] locations = {"ГП-04", "ГП-92", "ГП-07"};
        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, locations);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        userlocationId.setAdapter(adapter);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Вводим текст и отображаем в строке ввода на основном экране:
                                initFabMenu();

                                mainInventLocationId = userlocationId.getSelectedItem().toString();

                                new MyAsyncTask_initLabels(promptsView.getContext()).execute();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                Intent intent = new Intent( MainInventory.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();

        //и отображаем его:
        alertDialog.show();


        //Вводим текст и отображаем в строке ввода на основном экране:
        initFabMenu();

        //new MyAsyncTask_initLabels(this).execute();
    }

    private void initFabMenu (){
        fabMain = findViewById(R.id.fabMain);
        fabAdd = findViewById(R.id.fabAdd);
        fabGroupBy = findViewById(R.id.fabGroupBy);

        fabAdd.setAlpha(0f);
        fabGroupBy.setAlpha(0f);

        fabAdd.setTranslationY(translationY);
        fabGroupBy.setTranslationY(translationY);
    }

    private void openMenu(){
        isMenuOpen = !isMenuOpen;

        fabMain.animate().setInterpolator(new OvershootInterpolator()).rotation(45f).setDuration(300).start();

        fabAdd.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(300).start();
        fabGroupBy.animate().translationY(0f).alpha(1f).setInterpolator(new OvershootInterpolator()).setDuration(300).start();
    }

    private void closeMenu(){
        isMenuOpen = !isMenuOpen;

        fabMain.animate().setInterpolator(new OvershootInterpolator()).rotation(0f).setDuration(300).start();

        fabAdd.animate().translationY(translationY).alpha(0f).setInterpolator(new OvershootInterpolator()).setDuration(300).start();
        fabGroupBy.animate().translationY(translationY).alpha(0f).setInterpolator(new OvershootInterpolator()).setDuration(300).start();
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

        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(CustomBroadcastReceivers);
        ///inventLabels.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void fabMain_Clicked(View view){

        if(isMenuOpen)
            closeMenu();
        else
            openMenu();
    }

    public void fabGroupBy_Clicked(View view){

        checkedSum = checkedSum ? false : true;

        if(checkedSum)
            new MyAsyncTask_initLabelsGroupBy(this).execute();
        else
            new MyAsyncTask_initLabels(this).execute();

        if(isMenuOpen)
            closeMenu();
        else
            openMenu();
    }

    public void fabAdd_Clicked(View view) {

        //если не указан склад и местоположение, то ошибка
        if(spinFromLocation == null ||  spinFromLocation.getSelectedItem() == null ||
                spinFromWmsLocation == null || spinFromWmsLocation.getSelectedItem() == null)
        {
            MessageAlert.ErrorMessage(this, "Выберите склад и местоположение!");
            return;
        }
        else if(checkedSum)
        {
            MessageAlert.ErrorMessage(this, "Нельзя добавлять бирку когда смотрим 'Итоги'!");
            return;
        }
        else
        {

            if(isMenuOpen)
                closeMenu();
            else
                openMenu();

            Intent intent = new Intent(this, Activity_add_item.class);
            intent.putExtra("InventLocationId", spinFromLocation.getSelectedItem().toString().split(",")[0]);
            intent.putExtra("WmsLocationId", spinFromWmsLocation.getSelectedItem().toString());
            this.startActivity(intent);
        }
    }

    //получим отсканированные и сгруппированные бирки на сегодня
    class MyAsyncTask_initLabelsGroupBy extends AsyncTask<Void, Void, Void>{

        ProgressDialog mProgressDialog;
        Context context;
        String msg = "";

        public MyAsyncTask_initLabelsGroupBy(Context _context)
        {
            this.context = _context;
            mProgressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute(){

            //mProgressDialog.setIndeterminate(true);
            mProgressDialog.setTitle("Загрузка!");
            mProgressDialog.setMessage("Считаем итоги...");
            mProgressDialog.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection conn = null;

            try {
                inventLabels.clear();

                String Query = ServerProperty.getServer() + "api/Inventory/GetLabelsGroupBy/" + DateTime_my.Now_Short();
                conn = ServerProperty.Get(Query);
                String output;

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    Type collectionType = new TypeToken<Collection<InventLabel_fromTSDtoAx>>(){}.getType();
                    Collection<InventLabel_fromTSDtoAx> inventLabelFromTSDtoAxes = gson.fromJson(output, collectionType);

                    for(InventLabel_fromTSDtoAx label : inventLabelFromTSDtoAxes) {
                        inventLabels.add(0, new InventLabels(label.getUnitId(), label.getFromInventLocationId(), label.getFromwMSLocationId(), label.getBatchNum(), label.getItemIdName(),
                                label.getFrakcia(), label.getQtyTrayIn(), label.getQtyGood(), label.getCount()));
                    }
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
                msg = "Нет подключения к интернету!\n" + ex.toString();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                msg = "Method - MyAsyncTask_initLabelsGroupBy " + ex.toString();
                ///Log.e(TsdProperty.TAG, ex.toString());
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(Void aVoid) {

            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            super.onPostExecute(aVoid);

            if(msg.isEmpty())
            {
                RecyclerView recyclerView = findViewById(R.id.rcList);
                adapter = new DataAdapter(MainInventory.this, inventLabels);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                setTitle("Итоги");
            }
            else {
                MessageAlert.ErrorMessage(context, msg);
            }
        }
    }
    //Получить отсканированные бирки на сегодня
    class MyAsyncTask_initLabels extends AsyncTask<Void, Void, Void>{

         ProgressDialog mProgressDialog;
         Context context;
         String msg = "";

         public MyAsyncTask_initLabels(Context _context)
         {
             this.context = _context;
             mProgressDialog = new ProgressDialog(context);
             //список складов...нужно добавить пусто, иначе не будет выбираться
             listSpinerLocation = new ArrayList<>();
             listSpinerLocation.add("");
         }

         @Override
         protected void onPreExecute(){

             //mProgressDialog.setIndeterminate(true);
             mProgressDialog.setTitle("Загрузка!");
             mProgressDialog.setMessage("Получаем отсканированные бирки...");
             mProgressDialog.show();

             super.onPreExecute();
         }

        @Override
         protected Void doInBackground(Void... params) {
            HttpURLConnection conn = null;
            String splitInventLocationId = "";
            String[] separated;

            try {
                inventLabels.clear();

                separated = mainInventLocationId.split("-");

                if(separated.length > 1)
                    splitInventLocationId = separated[0] + "-" +separated[1];
                else
                    splitInventLocationId = separated[0];

                String Query = ServerProperty.getServer() + "api/Inventory/getLabels_Test/" + DateTime_my.Now_Short()+"/" + splitInventLocationId;
                conn = ServerProperty.Get(Query);
                String output;

                //Log.i(TsdProperty.TAG, splitInventLocationId);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();
                    br.close();
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    Type collectionType = new TypeToken<Collection<InventLabel_fromTSDtoAx>>(){}.getType();
                    Collection<InventLabel_fromTSDtoAx> inventLabelFromTSDtoAxes = gson.fromJson(output, collectionType);

                    for(InventLabel_fromTSDtoAx label : inventLabelFromTSDtoAxes) {
                        inventLabels.add(0, new InventLabels(label.getUnitId(), label.getFromInventLocationId(), label.getFromwMSLocationId(), label.getBatchNum(), label.getItemIdName(),
                                label.getFrakcia(),label.getQtyTrayIn(),label.getQtyGood(), label.getCount()));
                    }
                }
            }
            catch (IOException ex){
                msg = "Нет подключения к интернету!\n" + ex.toString();
            }
            catch (Exception ex)
            {
                //MessageAlert.ErrorMessage(MainInventory.this,ex.toString());
                //Log.e(TsdProperty.TAG, ex.toString());
                msg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }

            return null;
         }

         //В методе onPostExecute() выведем ответ сервера.
         @Override
         protected void onPostExecute(Void aVoid) {

             if(mProgressDialog.isShowing())
                 mProgressDialog.dismiss();

             super.onPostExecute(aVoid);

             if(msg.isEmpty()){
                 RecyclerView recyclerView = findViewById(R.id.rcList);
                 adapter = new DataAdapter(MainInventory.this, inventLabels);
                 recyclerView.setAdapter(adapter);

                 setTitle("Инвентаризация");
             }
             else{
                MessageAlert.ErrorMessage(context, msg);
             }
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

                String Query = ServerProperty.getServer() + "api/labels/Location/" + mainInventLocationId;
                String output;

                Log.i(TsdProperty.TAG, mainInventLocationId);

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
            catch (IOException ex) {
                ex.printStackTrace();
                ErrorMsg = "Нет подключения к интернету!\n" + ex.toString();
            }
            catch (Exception ex) {
                ex.printStackTrace();
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
                //адаптер на склады
                ArrayAdapter<String> adapterLocation = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, listSpinerLocation);
                adapterLocation.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //заполним склады с
                spinFromLocation.setAdapter(adapterLocation);
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

                MessageAlert.SoundOK(context);
            }
            else {
                MessageAlert.ErrorMessage(context, ErrorMsg);
            }
        }
    }
    //получить местоположение с сервера
    class MyAsyncTask_GetWmsLocationServer extends AsyncTask<String, String, String> {
        String LocationId;
        Context context;
        boolean isSend = false;
        String msg = "";

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

                String Query = ServerProperty.getServer() + "api/labels/getWmsLocation/" + LocationId;
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
                        listSpinerFromWmsLocation.add(wmsLocation.getwMSLocationId().split(",")[0]);
                        //Log.i(TsdProperty.TAG, wmsLocation.getwMSLocationId());
                    }
                    isSend = true;
                }
                else{
                    msg = "method - GetWmsLocationServer" + String.valueOf(conn.getResponseCode());

                    //MessageAlert.ErrorMessage(context, "method - GetWmsLocationServer" + String.valueOf(conn.getResponseCode()));
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                msg = "method - GetWmsLocationServer" + ex.toString();
                //MessageAlert.ErrorMessage(context, "method - GetWmsLocationServer" + ex.toString());
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

            super.onPostExecute(result);

            if(isSend) {
                //отсортировать список
                Collections.sort(listSpinerFromWmsLocation);

                adapterWms = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, listSpinerFromWmsLocation);

                adapterWms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //заполним местоположение с
                spinFromWmsLocation.setAdapter(adapterWms);
                spinFromWmsLocation.setPrompt("Из ячейки");

                int spinnerPosition = adapterWms.getPosition(mainWmsLocationId);
                spinFromWmsLocation.setSelection(spinnerPosition);
                //MessageAlert.InfoMsg(context, String.valueOf(spinnerPosition + " | " + Arrays.asList(listSpinerFromWmsLocation).indexOf(mainWmsLocationId)));
            }
            else
            {
                MessageAlert.ErrorMessage(context, msg);
            }
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
            if(checkedSum)
            {
                MessageAlert.ErrorMessage(context, "Нельзя сканировать когда смотрим 'Итоги'!");
                return;
            }

            //Получаем штрих-код
            barcode = intent.getStringExtra(TsdProperty.SCAN_DECODING_DATA);
            //нужна для того чтобы проверить какая это бирка, по местоположению или продукции
            checkBarcode = barcode.substring(0, 2);

            //бирка по складам
            if (checkBarcode.contains("ГП"))
            {
                subStr = barcode.split("\\|");
                //получим склад с бирки
                mainInventLocationId = subStr[0];
                //получим местоположение с бирки
                mainWmsLocationId = subStr[1];

                //если склады равны, то будем выбирать уже из загруженного списка
                //если склад поменяется, то подзрузим заново
                if(prevInventLocationId.equals(mainInventLocationId) )
                {
                    if(!adapterWms.isEmpty())
                    {
                        //выбрать в спиннере позицию
                        int spinnerPosition = adapterWms.getPosition(mainWmsLocationId);
                        spinFromWmsLocation.setSelection(spinnerPosition);

                        MessageAlert.SoundOK(context);
                    }
                }
                else{//заполним список складов и месположений
                    new MyAsyncTask_GetLocationServer(context).execute();
                    new MyAsyncTask_initLabels(context).execute();
                }

                prevInventLocationId = mainInventLocationId;

            }//бирка по продукции
            else if(barcode.contains("idBKO="))
            {
                addLabel(context, true);
            }
            else if (checkBarcode.contains("АО")) {
                addLabel(context, false);
            }

            else{
                MessageAlert.ErrorMessage(context, "Неизвестный QR-Code");
            }
        }

        private void addLabel(Context _context, boolean _isCustLabel)
        {
            //если не указан склад и местоположение, то ошибка
            if(spinFromLocation == null ||  spinFromLocation.getSelectedItem() == null ||
                    spinFromWmsLocation == null || spinFromWmsLocation.getSelectedItem() == null)
            {
                MessageAlert.ErrorMessage(_context, "Не указан склад или местопол., отсканируйте бирку с местопол.!\nБирка печается из аксапты!");
                return;
            }

            new MyAsyncTask_AddLabel(_context, barcode, _isCustLabel).execute();
        }
    }
    //добавить бирку в репликационную таблицу
    class MyAsyncTask_AddLabel extends AsyncTask<String, String, String>{
        private Context _context;
        private String barcode;
        private String[] subStr;
        private String NumId, NumBatch, ItemIdName, Frakcia, InvenLocationId, WmsLocationId, batchData_qrCode;
        private boolean isSend = false;
        private boolean duplicate = false;
        private String msg;
        boolean isCustLabel;
        Date dateBatch;

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        ProgressDialog mProgressDialog;

        public MyAsyncTask_AddLabel(Context _context, String _barcode, boolean _isCustLabel)
        {
            this._context = _context;
            this.barcode = _barcode;
            this.isCustLabel = _isCustLabel;

            mProgressDialog = new ProgressDialog(_context);
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute() {

            mProgressDialog.setTitle("Загрузка!");
            mProgressDialog.setMessage("Отправка данных...");
            mProgressDialog.show();

            super.onPreExecute();

            if(!isCustLabel)
            {
                //разберем строку сканирования по |
                subStr = barcode.split("\\|");

                NumId = subStr[7];
                NumBatch = subStr[1];
                ItemIdName = subStr[2];
                Frakcia = subStr[3];
                batchData_qrCode = subStr[6];//получим дату выпуска партии
                InvenLocationId = spinFromLocation.getSelectedItem().toString().split(",")[0];
                WmsLocationId = spinFromWmsLocation.getSelectedItem().toString();
            }
            else
            {
                int posNumLabel;
                posNumLabel = barcode.indexOf(constIdMark) + constIdMark.length();

                NumId = barcode.substring(posNumLabel, barcode.length());
                InvenLocationId = spinFromLocation.getSelectedItem().toString().split(",")[0];
                WmsLocationId = spinFromWmsLocation.getSelectedItem().toString();
            }

            //проверим на дупликат бирок
            if(checkDuplicateNumId(NumId)) {
                MessageAlert.ErrorMessage(_context, "Данная бирка уже была отсканирована!");
                duplicate = true;
            }
        }
        //Выполняем основыне действия, нельзя работать с контролами, с контролами работаем в post Execute
        @Override
        protected  String doInBackground(String... params) {
            //флаг для проверки на дубликат бирок
            if(duplicate)
                return null;
            //получить бирку из аксапты с диминой таблицы
            getlabel();

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            super.onPostExecute(result);
            //флаг для проверки на дубликат бирок
            if(duplicate)
                return;

            /*if(custLabelAx != null)
            {

            }*/

            if(isSend)
            {
                //обновим данные
                adapter.notifyDataSetChanged();
                //проиграем звук сканирования
                MessageAlert.SoundOK(_context);
                //MessageAlert.SuccesMsg(_context, msg, true);
            }
            else
                MessageAlert.ErrorMessage(_context, msg);
        }

        //получить бирку из аксапты с диминой таблицы
        private void getlabel(){
            HttpURLConnection conn = null;
            try {
                String Query = ServerProperty.getServer() + "api/labels/LabelAx/" + NumId;
                String output;
                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();

                    br.close();

                    if(conn != null)
                        conn.disconnect();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    LabelsAx labelAx = gson.fromJson(output, LabelsAx.class);

                    //Клиентская бирка
                    if(labelAx != null && isCustLabel)
                    {
                        //custLabelAx = new LabelsAx(labelAx);
                        NumBatch = labelAx.getBatchNum();
                        ItemIdName = labelAx.getItemName();
                        Frakcia = labelAx.getItemNumName();
                        //Преобразуем дату, т.к. в JSON из SQL дата формата "2013-09-11T00:00:00"
                        dateBatch = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(labelAx.getBatchDate());
                        batchData_qrCode = format.format(dateBatch);
                    }

                    //добавить бирку в список и в аксапту
                    if(labelAx != null)
                        PostInventTSDtoAx(labelAx);


                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
                {
                    //Log.e(TsdProperty.TAG, "Данной бирки нет в аксапте, позвоните 41-40");
                    msg = "Бирка№: " + NumId + " данной бирки нет в аксапте, позвоните 41-40";
                    isSend = false;
                }
                else{
                    //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + String.valueOf(conn.getResponseCode()));
                    msg = "Method - getLabel(),  " + String.valueOf(conn.getResponseCode());
                    isSend = false;
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getLabel(),  Возможно проблемы с интернетом, попробойте отсканировать еще раз!" + ex.toString();
                isSend = false;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getLabel() " + ex.toString();
                isSend = false;
            }
            finally {

                if(conn != null)
                    conn.disconnect();
            }
        }

        //добавить бирку в список и в аксапту
        private void PostInventTSDtoAx(LabelsAx _labelsAx){
            isSend = false;
            HttpURLConnection conn = null;

            try {
                String Query = ServerProperty.getServer() + "api/Inventory";
                conn = ServerProperty.Put(Query);
                float Weight = Math_EV.decroundF(_labelsAx.getQtyGood() / _labelsAx.getQtyTrayIn() * 1000,3);

                InventLabel_fromTSDtoAx inventLabelAdd = new InventLabel_fromTSDtoAx(_labelsAx.getUnitId(), DateTime_my.Now_Short_OneDayOfMonth(), _labelsAx.getItemId(), ItemIdName, Frakcia,
                                                                                  _labelsAx.getBatchNum(), DateTime_my.DateLong(batchData_qrCode), InvenLocationId, WmsLocationId, "",
                                                                                  _labelsAx.getConfigId(), _labelsAx.getJournalId(), 1, _labelsAx.getQtyGood(), _labelsAx.getQtyTrayIn(),
                                                                                  DateTime_my.Now_Long(), Weight);
                Gson gson = new Gson();
                String jsonString  = gson.toJson(inventLabelAdd);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),"UTF-8"));
                //добавим бирку в репликационную таблицу, если все норм то ниже добавляем в список
                out.write(jsonString);
                out.flush();
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    isSend = true;
                    msg = String.format("Номер бирки: %s", _labelsAx.getUnitId());
                    //добавим бирку лист
                    inventLabels.add(0, new InventLabels(NumId, InvenLocationId, WmsLocationId, NumBatch, ItemIdName, Frakcia, _labelsAx.getQtyTrayIn(), _labelsAx.getQtyGood(), 1));
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
                ex.printStackTrace();
                msg = ex.toString();
            }
            finally {
                if(conn != null)
                    conn.disconnect();
            }
        }

        //поискать одинаковые бирки
        private boolean checkDuplicateNumId(String _NumId){
            for (InventLabels labels: inventLabels){

                if(labels.getNumId().equals(_NumId))
                    return true;
            }
            return false;
        }
    }
}
