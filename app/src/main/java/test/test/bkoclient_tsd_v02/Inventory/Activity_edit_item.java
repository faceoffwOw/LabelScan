package test.test.bkoclient_tsd_v02.Inventory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
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
import test.test.bkoclient_tsd_v02.R;

public class Activity_edit_item extends AppCompatActivity {

    TextView txtNumBatch, txtName, txtFrakcia, txtQtyGood;
    EditText editQtyTrayIn;
    int position = -1;
    List<InventLabels> inventLabels = InventLabels.getListInventLabel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        setTitle("Изменить количество шт.");

        txtNumBatch = findViewById(R.id.txtNumBatch);
        txtName = findViewById(R.id.txtName);
        txtFrakcia = findViewById(R.id.txtFrakcia);
        txtQtyGood = findViewById(R.id.txtQtyGood);
        editQtyTrayIn = findViewById(R.id.editQtyTrayIn);

        Intent intent = getIntent();

        txtNumBatch.setText("Партия: " + intent.getStringExtra("NumBatch"));
        txtName.setText("Марка: " + intent.getStringExtra("ItemIdName"));
        txtFrakcia.setText("Фракция: " + intent.getStringExtra("Frakcia"));
        txtQtyGood.setText("Кол-во, т: " + intent.getFloatExtra("QtyGood", 0.0f));
        editQtyTrayIn.setText(String.valueOf(intent.getIntExtra("QtyTrayIn", 0)));
        position = intent.getIntExtra("position", -1);
    }
    //кнопка "изменить"
    public void btnUpdateItem_Click(View v){

        int curQtyTrayIn = Integer.parseInt(editQtyTrayIn.getText().toString());

        if (curQtyTrayIn <= 0){
            Toast.makeText(this, "Пожалуйста, введите кол-во шт. в поддоне!", Toast.LENGTH_SHORT).show();
            return;
        }

        new MyAsyncTask_UpdateLabel(this, curQtyTrayIn).execute();
    }

    class MyAsyncTask_UpdateLabel extends AsyncTask<Void, Void, Void> {

        Context context;
        int curQtyTrayIn;
        float QtyGoodNew = 0.0f;
        String msg = "";
        boolean isSend = false;

        public MyAsyncTask_UpdateLabel(Context _context, int _curQtyTrayIn){
            this.context = _context;
            this.curQtyTrayIn = _curQtyTrayIn;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            //получим бирку из репликационной таблице для обновления бирки
            InventLabel_fromTSDtoAx curInventLabel = getInventLabel_fromTSDtoAx();

            //Обновим выбранную бирку(ШТ и ТОННЫ)
            if(curInventLabel != null)
                isSend = editLabel(curInventLabel);
            else
                isSend = false;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);

            if (isSend)
            {
                //обновим данные в списке бирок
                //и закроем форму изменения
                if(inventLabels.size() > 0) {
                    inventLabels.get(position).setQtyTrayIn(curQtyTrayIn);
                    inventLabels.get(position).setQtyGood(QtyGoodNew);
                }

                Intent data = new Intent();

                setResult(RESULT_OK, data);

                finish();
            }
            else
            {
                MessageAlert.ErrorMessage(context, msg);
            }
        }

        private InventLabel_fromTSDtoAx getInventLabel_fromTSDtoAx(){
            HttpURLConnection conn = null;

            try {
                String inventDate = DateTime_my.Now_Short_OneDayOfMonth();
                String UnitId = inventLabels.get(position).getNumId();

                String Query = ServerProperty.getServer() + "api/Inventory/getLabel/" + inventDate + "/" + UnitId + "/";
                String output;
                conn = ServerProperty.Get(Query);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    output = br.readLine();

                    br.close();

                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();

                    InventLabel_fromTSDtoAx label = gson.fromJson(output, InventLabel_fromTSDtoAx.class);

                    if (conn != null)
                        conn.disconnect();

                    return label;
                }
                else if(HttpURLConnection.HTTP_NOT_FOUND == conn.getResponseCode()){
                    //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + String.valueOf(conn.getResponseCode()));
                    msg = "Не найдено данной бирки в репликационной таблице! httpCode: " + String.valueOf(conn.getResponseCode());
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                msg = "Method - getInventLabel_fromTSDtoAx,  " + ex.toString();
            }
            finally {

                if(conn != null)
                    conn.disconnect();
            }

            return null;
        }

        private boolean editLabel(InventLabel_fromTSDtoAx _invenlabel_fromTSDtoAx){
            boolean isSend = false;

            HttpURLConnection conn = null;
            //InventLabels label = InventLabels.findUtitId("");
            //InventLabels inventLabel = inventLabels.get(position);
            try {
                String Query = ServerProperty.getServer() + "api/Inventory/Update/"+DateTime_my.Now_Short_OneDayOfMonth();

                conn = ServerProperty.Put(Query);

                //возьмем отвес с репликационной таблице по текущей бирки, если нету то посчитаем его
                float curWeight = _invenlabel_fromTSDtoAx.getWeight();

                if(curWeight <= 0)
                {
                    curWeight = (_invenlabel_fromTSDtoAx.getQtyGood() / _invenlabel_fromTSDtoAx.getQtyTrayIn()) * 1000;
                    curWeight = Math_EV.decroundF(curWeight,3);
                }

                String UnitId = _invenlabel_fromTSDtoAx.getUnitId(),
                    InventDate = DateTime_my.Now_Short_OneDayOfMonth(),
                    itemId = _invenlabel_fromTSDtoAx.getItemId(),
                    ItemIdName = _invenlabel_fromTSDtoAx.getItemIdName(),
                    Frakcia = _invenlabel_fromTSDtoAx.getFrakcia(),
                    BatchNum = _invenlabel_fromTSDtoAx.getBatchNum(),
                    BatchDate = _invenlabel_fromTSDtoAx.getBatchDate(),
                    fromInventLocationId = _invenlabel_fromTSDtoAx.getFromInventLocationId(),
                    fromWmsLocationId = _invenlabel_fromTSDtoAx.getFromwMSLocationId(),
                    StandartId = _invenlabel_fromTSDtoAx.getStandardId(),
                    ConfigId = _invenlabel_fromTSDtoAx.getConfigId(),
                    JournalId = _invenlabel_fromTSDtoAx.getJournalId(),
                    DateScan = DateTime_my.Now_Long();
                int Count = 1,
                    QtyTrayIn = Integer.parseInt(editQtyTrayIn.getText().toString());
                QtyGoodNew = Math_EV.decroundF((curWeight * QtyTrayIn) / 1000, 3);

                InventLabel_fromTSDtoAx inventLabel_edit = new InventLabel_fromTSDtoAx(UnitId, InventDate, itemId, ItemIdName, Frakcia,
                        BatchNum, BatchDate, fromInventLocationId, fromWmsLocationId, StandartId,
                        ConfigId, JournalId, Count, QtyGoodNew, QtyTrayIn,
                        DateScan, curWeight);

                Gson gson = new Gson();
                String jsonString  = gson.toJson(inventLabel_edit);
                //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(jsonString);
                out.flush();
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    isSend = true;
                    msg = "Данные изменены!";
                }
                else{
                    isSend = false;
                    msg = "Ошибка при изменении бирки в методе editLabel" + conn.getResponseCode();
                }
            }
            catch(Exception e){
                e.printStackTrace();
                isSend = false;
                msg = "Ошибка соединения!/n " + e.toString();
            }
            finally {

                if(conn != null)
                    conn.disconnect();
            }

            return isSend;
        }
    }
}
