package test.test.bkoclient_tsd_v02;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import test.test.bkoclient_tsd_v02.Inventory.MainInventory;
import test.test.bkoclient_tsd_v02.Manager.ServerProperty;
import test.test.bkoclient_tsd_v02.Manager.Settings;
import test.test.bkoclient_tsd_v02.ProdLorry.MainProdLorry;
import test.test.bkoclient_tsd_v02.Settings.SettingsActivity;
import test.test.bkoclient_tsd_v02.Transfer.MainTransfer;

public class MainActivity extends AppCompatActivity {

    SharedPreferences mSettings;
    Button btnComing, btnInventory, btnTransfer, btnProdLorry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Вы передаёте в указанный метод название вашего файла (он будет создан автоматически) и стандартное разрешение,
        // дающее доступ только компонентам приложения.
        mSettings = getSharedPreferences(Settings.APP_PREFERENCES, this.MODE_PRIVATE);

        btnComing = (Button)findViewById(R.id.btnComing);
        btnInventory = (Button)findViewById(R.id.btnInventory);
        btnTransfer = (Button)findViewById(R.id.btnTransfer);
        btnProdLorry = (Button)findViewById(R.id.btnProdLorry);
    }

    @Override
    protected void onResume() {

        super.onResume();

        //Настройка IP адреса
        InitSharedPreferences();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        switch (keyCode){
            case KeyEvent.KEYCODE_1://кнопка 1 на клавиатуре ("ОТГРУЗКА")
                btnComing.performClick();//произвести нажатие кнопки
                return true;
            case KeyEvent.KEYCODE_2://кнопка 2 на клавиатуре ("ИНВЕНТАРИЗАЦИЯ")
                btnInventory.performClick();//произвести нажатие кнопки
                return true;
            case KeyEvent.KEYCODE_3://кнопка 3 на клавиатуре ("Перемещение")
                btnTransfer.performClick();
                return true;
            case KeyEvent.KEYCODE_4://кнопка 4 на клавиатуре ("Туннельные вагонетки")
                btnProdLorry.performClick();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    //ОТГРУЗКА
    public void btnComingStatus_Click(View v){

        //переходим на новое Activity QrCode
        Intent intent = new Intent(this, EnterOrderNumber.class);

        startActivity(intent);
    }
    //ИНВЕНТАРИЗАЦИЯ
    public void btnInventory_Click(View v){

        //переходим на новое Activity QrCode
        Intent intent = new Intent(this, MainInventory.class);

        startActivity(intent);
    }

    //ПЕРЕМЕЩЕНИЕ
    public void btnTransfer_Click(View v)
    {
        //System.out.println("Btn click");
        Intent intent = new Intent(this, MainTransfer.class);

        startActivity(intent);
    }

    //Туннельные вагонетки
    public void btnProdLorry_Click(View v)
    {
        //System.out.println("Btn click");
        Intent intent = new Intent(this, MainProdLorry.class);

        startActivity(intent);
    }

    //НАСТРОЙКИ
    public void btnSettings_Click(View v){
        //переходим на новое Activity QrCode
        Intent intent = new Intent(this, SettingsActivity.class);

        startActivity(intent);
    }

    public void InitSharedPreferences()
    {
        //Запишем в шару настройки сервера
        //если есть такой ключ в шаре, то запишем в глобальную переменную Server значение из шары
        //если нет то возьмем данные по умолчанию из класса ServerProperty, и запишем их в шару и глобальную переменную
        if(mSettings.contains(Settings.APP_PREFERENCES_SERVER))
        {
            // Получаем число из настроек
            String Server = mSettings.getString(Settings.APP_PREFERENCES_SERVER, Settings.defServer);
            //Сохраним ip адрес в глобальную переменную Server из шары
            ServerProperty.setServer(Server);
        }
        else {
            // Запоминаем данные
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(Settings.APP_PREFERENCES_SERVER, Settings.defServer);
            editor.apply();

            ServerProperty.setServer(Settings.defServer);
        }
    }
}
