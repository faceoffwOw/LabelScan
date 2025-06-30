package test.test.bkoclient_tsd_v02.Inventory.Models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InventLabels {

    private String NumId;
    private String InventLocationId;
    private String WmsLocationId;
    private String NumBatch;
    private String itemIdName;
    private String Frakcia;
    private float QtyGood ;
    private int QtyTrayIn;
    private int Count;

    public static List<InventLabels> listInventLabels = new ArrayList<>();

    public InventLabels(String NumId, String InventLocationId, String WmsLocationId, String NumBatch,  String itemIdName, String Frakcia, int QtyTrayIn, float QtyGood, int Count){

        this.NumId = NumId;
        this.InventLocationId = InventLocationId;
        this.WmsLocationId = WmsLocationId;
        this.NumBatch = NumBatch;
        this.itemIdName = itemIdName;
        this.Frakcia = Frakcia;
        this.QtyGood = QtyGood;
        this.QtyTrayIn = QtyTrayIn;
        this.Count = Count;
    }

    public String getNumId() { return NumId; }
    public String getInventLocationId() {return InventLocationId; }
    public String getWmsLocationId() {return WmsLocationId; }
    public String getNumBatch() { return NumBatch; }
    public String getItemIdName() { return itemIdName; }
    public String getFrakcia() { return Frakcia; }
    public float getQtyGood() { return QtyGood; }
    public int getQtyTrayIn() { return QtyTrayIn; }
    public int getCount() { return Count; }

    public void setQtyTrayIn(int qtyTrayIn) { QtyTrayIn = qtyTrayIn; }

    public void setQtyGood(float qtyGood) { QtyGood = qtyGood; }
    public void setQtyGood(int Count) { Count = Count; }

    public static List<InventLabels> getListInventLabel(){
        return listInventLabels;
    }

    public static InventLabels findUtitId(String _UnitId){

        for(InventLabels label: listInventLabels)
        {
            if(label.NumId.equals(_UnitId))
                return label;
        }
        return null;
    }
}
