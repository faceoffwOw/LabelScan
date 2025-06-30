package test.test.bkoclient_tsd_v02.Models;

import android.icu.util.ULocale;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class LabelsAx {
    private String labelsNumId;
    private String journalId;
    private String itemId;
    private String itemNumberId;
    //private String InventSerialId;
    private String batchId;
    private int qtyTrayIn;
    private float qtyGood;
    private String itemName;
    private String itemNumName;
    private String batchDate;

    public LabelsAx(String _LabelsNumId, String _BatchId,  String _itemId, String _itemNumberId, String _JournalId, int _qtyTrayIn, float _qtyGood, String _itemName, String _itemNumName, String _batchDate, String _InventSerialId){

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        this.labelsNumId = _LabelsNumId;
        this.journalId = _JournalId;
        this.itemId = _itemId;
        this.itemNumberId = _itemNumberId;
        //this.InventSerialId = _InventSerialId;
        this.batchId = _BatchId;
        this.qtyTrayIn = _qtyTrayIn;
        this.qtyGood = _qtyGood;
        this.itemName = _itemName;
        this.itemNumName = _itemNumName;
        this.batchDate = _batchDate;
    }
    public LabelsAx(LabelsAx _labelAx){

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        this.labelsNumId = _labelAx.labelsNumId;
        this.journalId = _labelAx.journalId;
        this.itemId = _labelAx.itemId;
        this.itemNumberId = _labelAx.itemNumberId;
        //this.InventSerialId = _InventSerialId;
        this.batchId = _labelAx.batchId;
        this.qtyTrayIn = _labelAx.qtyTrayIn;
        this.qtyGood = _labelAx.qtyGood;
        this.itemName = _labelAx.itemName;
        this.itemNumName = _labelAx.itemNumName;
        this.batchDate = _labelAx.batchDate;
    }

    public String getUnitId() { return labelsNumId; }
    public String getJournalId() { return journalId; }
    public String getItemId() { return itemId; }
    public String getConfigId() { return itemNumberId; }
    //public String getInventSerialId() { return InventSerialId; }
    public String getBatchNum() { return batchId; }
    public int getQtyTrayIn() { return qtyTrayIn; }
    public float getQtyGood() { return qtyGood; }
    public String getItemName() { return itemName; }
    public String getItemNumName() { return itemNumName; }
    public String getBatchDate() { return batchDate; }
}
