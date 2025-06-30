package test.test.bkoclient_tsd_v02.Inventory.Models;

import java.util.Date;

public class InventLabel_fromTSDtoAx {
    private String unitId;
    private String inventDate;
    private String itemId;
    private String itemIdName;
    private String frakcia;
    private String batchNum;
    private String batchDate;
    private String fromInventLocationId;
    private String fromwMSLocationId;
    private String standardId;
    private String configId;
    private String journalId;
    private int count;
    private float qtyGood;
    private int qtyTrayIn;
    private String dateScan;
    private float weight;

    public InventLabel_fromTSDtoAx(String UnitId, String inventDate, String itemId, String ItemIdName,  String Frakcia, String BatchNum, String BatchDate, String fromInventLocationId,
                                   String fromwMSLocationId, String StandardId, String ConfigId, String JournalId, int Count, float QtyGood, int QtyTrayIn, String DateScan, float Weight){
        this.unitId = UnitId;
        this.inventDate = inventDate;
        this.itemId = itemId;
        this.itemIdName = ItemIdName;
        this.frakcia = Frakcia;
        this.batchNum = BatchNum;
        this.batchDate = BatchDate;
        this.fromInventLocationId = fromInventLocationId;
        this.fromwMSLocationId = fromwMSLocationId;
        this.standardId = StandardId;
        this.configId = ConfigId;
        this.journalId = JournalId;
        this.count = Count;
        this.qtyGood = QtyGood;
        this.qtyTrayIn = QtyTrayIn;
        this.dateScan = DateScan;
        this.weight = Weight;
    }

    public String getUnitId() { return unitId; }
    public String getInventDate() { return inventDate; }
    public String getItemId() { return itemId; }
    public String getItemIdName() { return itemIdName; }
    public String getFrakcia() { return frakcia; }
    public String getBatchNum() { return batchNum;}
    public String getBatchDate() { return batchDate; }
    public String getFromInventLocationId() { return fromInventLocationId; }
    public String getFromwMSLocationId() { return fromwMSLocationId; }
    public String getStandardId() { return standardId; }
    public String getConfigId() { return configId; }
    public String getJournalId() { return journalId; }
    public int getCount() { return count; }
    public float getQtyGood() { return qtyGood; }
    public int getQtyTrayIn() { return qtyTrayIn; }
    public String getDateScan() { return dateScan; }
    public float getWeight() { return weight; }
}
