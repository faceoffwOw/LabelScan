package test.test.bkoclient_tsd_v02.Inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.List;

import test.test.bkoclient_tsd_v02.HelpersClass.DateTime_my;
import test.test.bkoclient_tsd_v02.HelpersClass.Math_EV;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabel_fromTSDtoAx;
import test.test.bkoclient_tsd_v02.Inventory.Models.InventLabels;
import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Models.LabelsAx;
import test.test.bkoclient_tsd_v02.R;

public class Activity_add_item extends AppCompatActivity {

    LabelsAx label;
    EditText editTextNumId;
    LinearLayout lineAdd;

    TextView txtNumId, txtNumBatch, txtName, txtFrakcia, txtQtyGood, txtQtyTrayIn;
    String InvenLocationId, WmsLocationId;
    List<InventLabels> inventLabels = InventLabels.getListInventLabel();
    Switch switchAllowEqualUnitId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        setTitle("Добавить новую бирку");

        editTextNumId = (EditText) findViewById(R.id.editNumId);
        lineAdd = (LinearLayout) findViewById(R.id.Line_add);

        txtNumId = findViewById(R.id.txtNumId);
        txtNumBatch = findViewById(R.id.txtNumBatch);
        txtName = findViewById(R.id.txtName);
        txtFrakcia = findViewById(R.id.txtFrakcia);
        txtQtyGood = findViewById(R.id.txtQtyGood);
        txtQtyTrayIn = findViewById(R.id.txtQtyTrayIn);

        switchAllowEqualUnitId = (Switch) findViewById(R.id.switchAllowEqualUnitId);

        //Данные с главной формы
        Intent intent = getIntent();
        InvenLocationId = intent.getStringExtra("InventLocationId");
        WmsLocationId = intent.getStringExtra("WmsLocationId");
    }
    //кнопка найти бирку
    public void searchLabel_Clicked(View view) {
        new MyAsyncTask_SearchLabel(this).execute();
    }
    //кнопка добавить бирку
    public void addNewLabel_Clicked(View view) {
        new MyAsyncTask_AddLabel(this).execute();
    }
    //найти бирку в диминой таблице ProdLabelsNum
    class MyAsyncTask_SearchLabel extends AsyncTask<String, String, String> {
        Context _context;

        String NumId = addNeedCntZero(editTextNumId.getText().toString());
        boolean isSend = false;
        String msg;

        ProgressDialog mProgressDialog;

        public MyAsyncTask_SearchLabel(Context _context)
        {
            this._context = _context;
            mProgressDialog = new ProgressDialog(_context);
            label = null;
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute() {

            mProgressDialog.setTitle("Загрузка!");
            mProgressDialog.setMessage("Ищем бирку...");
            mProgressDialog.show();

            lineAdd.setVisibility(View.INVISIBLE);

            super.onPreExecute();
        }
        //Выполняем основыне действия, нельзя работать с контролами, с контролами работаем в post Execute
        @Override
        protected  String doInBackground(String... params) {

            //получить бирку из аксапты с диминой таблицы
            label = getlabel();

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            super.onPostExecute(result);

            if(label != null && isSend){

                lineAdd.setVisibility(View.VISIBLE);
                txtNumId.setText("Номер бирки: " + label.getUnitId());
                txtNumBatch.setText("Партия: " + label.getBatchNum());
                txtName.setText("Марка: " + label.getItemName());
                txtFrakcia.setText("Фракция: " + label.getItemNumName());
                txtQtyGood.setText("Кол-во, т." + label.getQtyGood());
                txtQtyTrayIn.setText("Кол-во на поддоне ш. " + label.getQtyTrayIn());

                //проиграем звук сканирования
                MessageAlert.SoundOK(_context);
            }
            else{
                lineAdd.setVisibility(View.INVISIBLE);
                MessageAlert.ErrorMessage(_context, msg);
            }
        }

        //получить бирку из аксапты с диминой таблицы
        private LabelsAx getlabel(){
            HttpURLConnection conn = null;

            try {
                String Query = ServerProperty.getServer() + "api/labels/LabelAx/" + NumId;
                String output;
                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();

                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    LabelsAx labelAx = gson.fromJson(output, LabelsAx.class);


                    isSend = true;

                    return labelAx;
                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND){
                    msg = "Бирка №: " + NumId + "\nДанной бирки нет в аксапте!";
                    isSend = false;
                }
                else{
                    msg = "Method - getLabel(),  " + String.valueOf(conn.getResponseCode());
                    isSend = false;
                }
            }
            catch (IOException ex){
                msg = "Method - getLabel(),  Возможно проблемы с интернетом, попробойте поискать еще раз!" + ex.toString();
                isSend = false;
            }
            catch (Exception ex) {
                msg = "Method - getLabel() " + ex.toString();
                isSend = false;
            }
            finally {

                if (conn != null)
                    conn.disconnect();
            }

            return  null;
        }

        //добавим столько нулей сколько это не обходимо к номеру бирки
        private String addNeedCntZero(String _str){
            //сколько всего знаком должно быть (на данный момент расчитываем, что 10 знаков хватит)
            int SignsZero = 10;
            String newStr = "";
            //добавим столько нулей сколько не хватает до 10 знаков
            for (int i = 0; i < SignsZero - _str.length(); i++)
            {
                newStr += "0";
            }
            //к нулям прибавим, то что ввел пользователь
            newStr += _str;

            return newStr;
        }
    }
    //Добавить бирку в репликационную таблицу  InventLabel_fromTSDtoAx
    class MyAsyncTask_AddLabel extends AsyncTask<String, String, String> {
        Context _context;
        boolean isSend = false;
        String msg;

        ProgressDialog mProgressDialog;

        public MyAsyncTask_AddLabel(Context _context)
        {
            this._context = _context;
            mProgressDialog = new ProgressDialog(_context);
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute() {

            mProgressDialog.setTitle("Загрузка!");
            mProgressDialog.setMessage("Ищем бирку...");
            mProgressDialog.show();

            super.onPreExecute();
        }
        //Выполняем основыне действия, нельзя работать с контролами, с контролами работаем в post Execute
        @Override
        protected  String doInBackground(String... params) {

            PostInventTSDtoAx(label);

            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){

            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

            super.onPostExecute(result);

            if(isSend){
                Intent data = new Intent();
                setResult(RESULT_OK, data);
                finish();
            }
            else{
                MessageAlert.ErrorMessage(_context, msg);
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

                String UnitId = "";
                //если нажата галка одинаковые бирки, то разрешим им добавить, т.к при тестировании встречались поддоны с одними и теми же бирками
                if(switchAllowEqualUnitId.isChecked())
                    UnitId = _labelsAx.getUnitId() + " d";
                else
                    UnitId = _labelsAx.getUnitId();

                //Log.i(TsdProperty.TAG, UnitId);

                InventLabel_fromTSDtoAx inventLabelAdd = new InventLabel_fromTSDtoAx(UnitId, DateTime_my.Now_Short_OneDayOfMonth(), _labelsAx.getItemId(), _labelsAx.getItemName(), _labelsAx.getItemNumName(),
                        _labelsAx.getBatchNum(), _labelsAx.getBatchDate(), InvenLocationId, WmsLocationId, "",
                        _labelsAx.getConfigId(), _labelsAx.getJournalId(), 1, _labelsAx.getQtyGood(), _labelsAx.getQtyTrayIn(),
                        DateTime_my.Now_Long(), Weight);
                Gson gson = new Gson();
                String jsonString  = gson.toJson(inventLabelAdd);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(),"UTF-8"));

                out.write(jsonString);
                out.flush();
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // OK
                    isSend = true;
                    msg = String.format("Номер бирки: %s", inventLabelAdd.getUnitId());
                    //добавим бирку
                    inventLabels.add(0, new InventLabels(inventLabelAdd.getUnitId(), InvenLocationId, WmsLocationId, inventLabelAdd.getBatchNum(), inventLabelAdd.getItemIdName(),
                            inventLabelAdd.getFrakcia(), inventLabelAdd.getQtyTrayIn(), inventLabelAdd.getQtyGood(), 1));
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
                if (conn != null)
                    conn.disconnect();
            }
        }
    }
}
