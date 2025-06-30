package test.test.bkoclient_tsd_v02.ProdLorry;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import test.test.bkoclient_tsd_v02.Inventory.MainInventory;
import test.test.bkoclient_tsd_v02.MainActivity;
import test.test.bkoclient_tsd_v02.Manager.MessageAlert;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.Settings;
import test.test.bkoclient_tsd_v02.Manager.TsdProperty;
import test.test.bkoclient_tsd_v02.ProdLorry.Models.ProdLorryLabel;
import test.test.bkoclient_tsd_v02.ProdLorry.Models.ProdLorryUserProfile;
import test.test.bkoclient_tsd_v02.R;
import test.test.bkoclient_tsd_v02.Settings.SettingsActivity;
import test.test.bkoclient_tsd_v02.Transfer.MainTransfer;


public class MainProdLorry extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private MyAsyncTask_AddProdLorryLabel currentTask;
    private String Server = ServerProperty.getServer();
    CustomBroadcastReceivers CustomBroadcastReceivers = new CustomBroadcastReceivers();
    List<ProdLorryLabel> prodLorryLabels = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    ProdLorryDataAdapter prodLorryDataAdapter;
    String msg;
    Spinner spinnerShift, spinnerWrkCtr;

    ProdLorryUserProfile profile;

    private List<String> listWrkCtrs = new ArrayList<String>();
    private List<String> listShifts = new ArrayList<String>();
    LocalDateTime startShiftTime;
    LocalDateTime endShiftTime;

    boolean hasShiftTime = false;
    private boolean isProfileDialogOpen = false;  // Флаг для отслеживания состояния диалога

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_prodlorry);

        setTitle("Туннельные вагонетки");

        RecyclerView recyclerView = findViewById(R.id.rvProdLorryLabels);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        prodLorryDataAdapter = new ProdLorryDataAdapter(MainProdLorry.this, prodLorryLabels);
        recyclerView.setAdapter(prodLorryDataAdapter);

        new MainProdLorry.MyAsyncTask_GetLists(this).execute();

        //showProdLorrySetupDialog();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e("UncaughtException", "В потоке " + thread.getName(), throwable);
        });
    }

    @Override
    protected void onDestroy() {
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }

        super.onDestroy();
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
        //checkPointName = parent.getItemAtPosition(pos).toString();
        //MessageAlert.InfoMsg(this, "Выбрано " + checkPointName);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback.
    }

    public void btnEditUserScanSettings_click(View v)
    {
        showProdLorrySetupDialog();
    }

    public void showProdLorrySetupDialog()
    {
        isProfileDialogOpen = true;
        //Получаем вид с файла prompt_prodlorry.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(this);
        final View promptsView = li.inflate(R.layout.prompt_prodlorry, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем prompt_prodlorry.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        final EditText emplIdDialog = promptsView.findViewById(R.id.inputEmplId);
        final Spinner spinShiftDialog = (Spinner) promptsView.findViewById(R.id.spinnerShift);
        final Spinner spinWrkCtrDialog = (Spinner) promptsView.findViewById(R.id.spinnerWrkCtr);
        final CheckBox cbPreScanDialog = (CheckBox) promptsView.findViewById(R.id.cbPreScan);

        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_list, listShifts);
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(R.layout.spinner_list);
        // Применяем адаптер к элементу spinner
        spinShiftDialog.setAdapter(adapter);

        // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
        ArrayAdapter<String> adapterWrkCtr = new ArrayAdapter<String>(this, R.layout.spinner_list, listWrkCtrs);
        // Определяем разметку для использования при выборе элемента
        adapterWrkCtr.setDropDownViewResource(R.layout.spinner_list);
        // Применяем адаптер к элементу spinner
        spinWrkCtrDialog.setAdapter(adapterWrkCtr);

        if(profile != null && !profile.IsEmpty())
        {
            emplIdDialog.setText(profile.getEmplId());

            int spinnerPosition = adapter.getPosition(profile.getShiftId());
            if(spinnerPosition >= 0) {
                spinShiftDialog.setSelection(spinnerPosition);
            } else {
                // Если значение не найдено, можно установить позицию по умолчанию
                spinShiftDialog.setSelection(0);
            }

            spinnerPosition = adapterWrkCtr.getPosition(profile.getWrkCtrId());
            if(spinnerPosition >= 0) {
                spinWrkCtrDialog.setSelection(spinnerPosition);
            } else {
                // Если значение не найдено, можно установить позицию по умолчанию
                spinWrkCtrDialog.setSelection(0);
            }
        }

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String emplId = emplIdDialog.getText().toString().trim();
                                String shiftId = spinShiftDialog.getSelectedItem().toString();
                                String wrkCtrId = spinWrkCtrDialog.getSelectedItem().toString();
                                boolean isPreScan = cbPreScanDialog.isChecked();

                                // Создаем новый профиль с введенными данными
                                profile = new ProdLorryUserProfile(emplId, shiftId, wrkCtrId, "ОП-32", isPreScan);

                                // Обновляем UI с новыми данными профиля
                                updateProfileUI(profile);

                                hasShiftTime = false;
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                                if(profile == null || profile.IsEmpty()) {
                                    Intent intent = new Intent(MainProdLorry.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });

        //Создаем AlertDialog:
        AlertDialog alertDialog = mDialogBuilder.create();

        alertDialog.setOnDismissListener(dialog -> {
            isProfileDialogOpen = false;  // Диалог закрыт
        });

        //отображаем AlertDialog:
        alertDialog.show();
    }

    // Метод для обновления UI с данными профиля
    private void updateProfileUI(ProdLorryUserProfile profile) {
        // Находим все TextView в основном layout
        TextView txtEmplIdInfo = findViewById(R.id.txtEmplIdInfo);
        TextView txtShiftInfo = findViewById(R.id.txtShiftInfo);
        TextView txtWrkCtrIdInfo = findViewById(R.id.txtWrkCtrIdInfo);
        CheckBox cbPreScanModeInfo = findViewById(R.id.cbPreScanMode);

        // Устанавливаем значения из профиля
        txtEmplIdInfo.setText(profile.getEmplId());
        txtShiftInfo.setText(profile.getShiftId());
        txtWrkCtrIdInfo.setText(profile.getWrkCtrId());
        cbPreScanModeInfo.setChecked(profile.IsPreScan());
    }

    class MyAsyncTask_GetLists extends AsyncTask<String, String, String> {
        private final Context context;
        private final ProgressDialog mProgressDialog;
        private String errorMessage;

        public MyAsyncTask_GetLists(Context context) {
            this.context = context;
            //this.mProgressDialog = new ProgressDialog(context);
            this.mProgressDialog = new ProgressDialog(MainProdLorry.this);
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog.setMessage("Загрузка списков рабочих центров и смен...");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                loadWorkCenters();
                loadShifts();
            } catch (SocketTimeoutException ex) {
                errorMessage = "Ошибка: время подключения истекло. Попробуйте снова.";
            } catch (Exception ex) {
                errorMessage = "Ошибка загрузки данных: " + ex.getMessage();
            }
            return null;
        }

        private void loadWorkCenters() throws Exception {
            listWrkCtrs = fetchServerData("api/prodlorry/getWrkCtrlist/");
        }

        private void loadShifts() throws Exception {
            listShifts = fetchServerData("api/prodlorry/getShiftlist/");
        }

        private List<String> fetchServerData(String endpoint) throws Exception {
            HttpURLConnection conn = null;
            try {
                conn = ServerProperty.Get(Server + endpoint);
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP-код: " + conn.getResponseCode());
                }
                return parseJson(readStream(conn));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        private String readStream(HttpURLConnection conn) throws IOException {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return br.readLine();
            }
        }

        private List<String> parseJson(String json) {
            return new Gson().fromJson(json, List.class);
        }

        @Override
        protected void onPostExecute(String result) {
            if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
            if (errorMessage != null)
            {
                MessageAlert.ErrorMessage(context, errorMessage);
            }
            else {
                ((MainProdLorry)context).showProdLorrySetupDialog();
            }
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
                if (isProfileDialogOpen) {
                    return;  // Игнорируем сканирование, если диалог c настройкой открыт
                }

                if(!SettingsActivity.isNetworkConnected(context))
                {
                    MessageAlert.ErrorMessage(context, "Проверьте подключение к интернету!");
                    return;
                }

                if(profile.IsEmpty())
                {
                    MessageAlert.ErrorMessage(context, "Настройте профиль для сканирования! Введите таб. номер, смену, рабочее место");
                    return;
                }

                // Получаем тип и сам штрих код помещаем в переменные
                barcode = intent.getStringExtra(TsdProperty.SCAN_DECODING_DATA);
                subStr = barcode.split("\\|");

                // Строка содержит только цифры
                if(subStr[0].matches("\\d+"))
                {
                    if (currentTask != null && currentTask.getStatus() == AsyncTask.Status.RUNNING) {
                        currentTask.cancel(true);
                    }
                    if(MainProdLorry.this.isFinishing() || MainProdLorry.this.isDestroyed())
                    {
                        MessageAlert.ErrorMessage(context, "Неизвестная ошибка MainProdLorry!");
                        return;
                    }

                    currentTask = new MyAsyncTask_AddProdLorryLabel(context, barcode);
                    currentTask.execute();
                }
                else
                {
                    MessageAlert.ErrorMessage(context, "Неизвестный QR-Code");
                }

            }
            catch (Exception ex) {
                MessageAlert.ErrorMessage(MainProdLorry.this, "Method - ProdLorry onReceive() " + ex.toString());
            }
        }
    }

    class MyAsyncTask_AddProdLorryLabel extends AsyncTask<String, String, String> {
        private Context context;
        private String barcode;
        private ProdLorryLabel label;
        private ProdLorryLabel responseLabel;
        private boolean isSend = false;
        ProgressDialog mProgressDialog;
        String dateScanOutput;

        public MyAsyncTask_AddProdLorryLabel(Context _context, String _barcode)
        {
            this.context = _context;
            this.barcode = _barcode;

            //mProgressDialog = new ProgressDialog(_context);
            mProgressDialog = new ProgressDialog(MainProdLorry.this);
        }

        //В методе onPreExecute() считаем данные из полей ввода.
        @Override
        protected void onPreExecute() {
            // Запускаем ProgressDialog в UI-потоке
            runOnUiThread(() -> {
                mProgressDialog.setTitle("Загрузка!");
                mProgressDialog.setMessage("Отправка данных...");
                mProgressDialog.show();
            });

            super.onPreExecute();

            String[] subStr = barcode.split("\\|");

            String stoveReplId = subStr[0];
            String lorryNum = subStr[1];

            Date dateScan = new Date();
            DateFormat formatSQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            DateFormat formatOutput = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            String strDateScan = formatSQL.format(dateScan);
            dateScanOutput = formatOutput.format(dateScan);

            label = new ProdLorryLabel(stoveReplId, lorryNum, profile.getEmplId(),  profile.getWrkCtrId(), profile.getShiftId(),
                                        profile.getDepartment(), false, strDateScan, profile.IsPreScan());
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if(label != null) {
                    PostLabelLorryNumTSDtoAx(label);
                } else {
                    throw new Exception("Label is null");
                }
            } catch (Exception e) {
                Log.e("AsyncTask", "Error in doInBackground", e);
                msg = "Ошибка: " + e.getMessage();
                isSend = false;
            }
            return null;
        }

        //В методе onPostExecute() выведем ответ сервера.
        @Override
        protected void onPostExecute(String result){
            // Все UI-операции — в главном потоке
            runOnUiThread(() -> {
                try {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }

                    if (isSend) {
                        // Обновляем RecyclerView
                        if (prodLorryLabels.size() >= 10) {
                            prodLorryLabels.remove(9);
                        }
                        prodLorryLabels.add(0, responseLabel);
                        prodLorryDataAdapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPositionWithOffset(0, 0);

                        MessageAlert.SoundOK(context);
                    } else {
                        MessageAlert.ErrorMessage(context, msg);
                    }
                } catch (Exception ex) {
                    Log.e("onPostExecute", "Ошибка UI: " + ex.getMessage());
                } finally {
                    currentTask = null; // Очищаем ссылку после завершения
                }

            });
        }

        @SuppressLint("NewApi")
        private void PostLabelLorryNumTSDtoAx(ProdLorryLabel label) throws Exception {
            isSend = false;
            HttpURLConnection conn = null;

            try {
                //если нет закешированного времени начала и конца смены, то получим от сервера это время
                if (!hasShiftTime) {
                    getShiftTimeBoundaries(profile.getShiftId());
                    hasShiftTime = true;
                }

                if(startShiftTime == null || endShiftTime == null)
                {
                    msg = "Не удалось загрузить время начала и окончания смены!";
                    isSend = false;
                    return;
                }

                LocalDateTime timeNow = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd.MM.yyyy");

                //Мастер или начальник участка сканируют заранее, чтобы подготовить карты заданий. Поэтому время смены не проверяем
                if(!profile.IsPreScan())
                {
                    if (timeNow.isBefore(startShiftTime) || timeNow.isAfter(endShiftTime)) {
                        msg = "Текущее время " + timeNow.format(formatter) + " не входит в смену " + profile.getShiftId() + " "
                                + startShiftTime.format(formatter) + " – " + endShiftTime.format(formatter);
                        isSend = false;
                        return;
                    }
                }

                String Query = ServerProperty.getServer() + "api/prodlorry/scan";
                conn = ServerProperty.Post(Query);

                String jsonInputString = convertLabelToJson(label);
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    // Читаем ответ сервера
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Парсим ответ в объект ProdLorryLabel
                    Gson gson = new Gson();
                    responseLabel = gson.fromJson(response.toString(), ProdLorryLabel.class);
                    responseLabel.setDateScan(label.getDateScan());

                    isSend = true;
                }
                else if(conn.getResponseCode() == HttpURLConnection.HTTP_CONFLICT){//проверка на дубликат бирок
                    msg = String.format("Карточка туннельной вагонетки %s в смену %s уже была отсканирована!", label.getStoveReplId(), profile.getShiftId());
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

        @SuppressLint("NewApi")
        private void getShiftTimeBoundaries(String shiftId)
        {
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                String query = Server + "api/prodlorry/shift-time?shiftId=" + shiftId;
                conn = ServerProperty.Get(query);

                System.out.println(query);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Парсим JSON ответ
                    JSONObject json = new JSONObject(response.toString());
                    String shiftStartStr = json.getString("shiftStart");
                    String shiftEndStr = json.getString("shiftEnd");

                    // Преобразуем строки в LocalDateTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    startShiftTime = LocalDateTime.parse(shiftStartStr, formatter);
                    endShiftTime = LocalDateTime.parse(shiftEndStr, formatter);

                    // Выводим для отладки
                    System.out.println("Shift Start: " + startShiftTime);
                    System.out.println("Shift End: " + endShiftTime);
                }
                else
                {
                    msg = "HTTP error: " + responseCode;
                    MessageAlert.ErrorMessage(context, msg);
                }
            }
            catch (SocketTimeoutException exTimeOut){
                msg = "Вышло время подключение! Попробуйте перезайти в 'Туннельные вагонетки' eще раз!\n" + exTimeOut;
                MessageAlert.ErrorMessage(context, msg);
            }
            catch (Exception ex) {
                //Log.e(TsdProperty.TAG, "Method - getLabel(),  " + ex.toString());
                msg = "Method - getShiftTimeBoundaries(),  " + ex.toString();
                InputStream errorStream = conn.getErrorStream();
                ex.printStackTrace();
            } finally {
                if (conn != null) conn.disconnect();
            }
        }

        @SuppressLint("NewApi")
        private String convertLabelToJson(ProdLorryLabel label) {
            Gson gson = new Gson();
            Map<String, Object> requestBody = new HashMap<>();

            Map<String, Object> lorryData = new HashMap<>();
            lorryData.put("StoveReplId", label.getStoveReplId());
            lorryData.put("LorryNum", label.getLorryNum());
            lorryData.put("EmplId", label.getEmplId());
            lorryData.put("ShiftId", label.getShiftId());
            lorryData.put("WrkCtrId", label.getWrkCtrId());
            LocalDateTime dateTime = LocalDateTime.parse(label.getDateScan(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            lorryData.put("DateScan", formattedDate);
            lorryData.put("Department", label.getDepartment());
            lorryData.put("isSortCompleted", label.IsSortCompleted());
            lorryData.put("isPreScan", label.IsPreScan());

            requestBody.put("LorryData", lorryData);

            return gson.toJson(requestBody);
        }
    }
}
