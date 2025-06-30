package test.test.bkoclient_tsd_v02.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class SalesLine implements Parcelable {
    private String nomNaryd;
    private String itemId;
    private String configId;
    private String InventSerialId;
    private Float marketingSalesQty;
    private static List<SalesLine> listSalesLine = new ArrayList<>();

    public SalesLine(String _NomNaryd, String itemId, String _configId, Float _MarketingSalesQty, String _InventSerialId)
    {
        this.nomNaryd = _NomNaryd;
        this.itemId = itemId;
        this.configId = _configId;
        this.marketingSalesQty = _MarketingSalesQty;
        this.InventSerialId = _InventSerialId;
    }

    protected SalesLine(Parcel in) {
        nomNaryd = in.readString();
        itemId = in.readString();
        configId = in.readString();
        InventSerialId = in.readString();
        marketingSalesQty = in.readFloat();
    }

    public static final Creator<SalesLine> CREATOR = new Creator<SalesLine>() {
        @Override
        public SalesLine createFromParcel(Parcel in) {
            return new SalesLine(in);
        }

        @Override
        public SalesLine[] newArray(int size) {
            return new SalesLine[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nomNaryd);
        dest.writeString(itemId);
        dest.writeString(configId);
        dest.writeString(InventSerialId);
        dest.writeFloat(marketingSalesQty);
    }

    public String getNomNaryd() {
        return nomNaryd;
    }

    public String getItemId() {
        return itemId;
    }

    public String getConfigId() { return configId; }

    public String getInventSerialId() { return InventSerialId; }

    public Float getMarketingSalesQty() { return marketingSalesQty; }

    public static void addSalesLine(SalesLine salesLine){
        listSalesLine.add(salesLine);
    }
    public static List<SalesLine> getListSalesLine(){
        return listSalesLine;
    }


}
