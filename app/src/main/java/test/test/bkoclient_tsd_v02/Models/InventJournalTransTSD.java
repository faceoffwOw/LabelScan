package test.test.bkoclient_tsd_v02.Models;

import com.google.gson.annotations.SerializedName;

public class InventJournalTransTSD {

    private String UnitId;
    private String itemId;
    @SerializedName("batchNum")
    private String batchNum;

    private String BatchDate;
    private String fromInventLocationId;
    private String toInventLocationId;
    private String fromwMSLocationId;
    private String towMSLocationId;
    private String StandardId;
    private String configId;
    private String JournalId;
    private int count;
    private int qtyTrayIn;
    private double saleQty;
    public String DateScan;
    public String NomNaryd;

    public InventJournalTransTSD(String _UnitId, String _itemId, String _batchNum, String _BatchDate, String _fromInventLocationId,
                                 String _fromwMSLocationId, String _configId, String _JournalId, int _count, String _DateScan, String _NomNaryd, double _saleQty, int _qtyTrayIn){
        this.UnitId = _UnitId;
        this.itemId = _itemId;
        this.batchNum = _batchNum;
        this.BatchDate = _BatchDate;
        this.fromInventLocationId = _fromInventLocationId;
        this.fromwMSLocationId = _fromwMSLocationId;
        this.configId = _configId;
        this.JournalId = _JournalId;
        this.count = _count;
        this.DateScan = _DateScan;
        this.NomNaryd = _NomNaryd;
        this.saleQty = _saleQty;
        this.qtyTrayIn = _qtyTrayIn;
    }

    public String getBatchNum (){
        return batchNum;
    }
    public String getConfigId (){
        return configId;
    }
    public int getCount() {
        return count;
    }
    public double getSalesQty() {
        return saleQty;
    }
    public int getQtyTrayIn () {
        return qtyTrayIn;
    }
}
