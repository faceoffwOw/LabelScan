package test.test.bkoclient_tsd_v02.Models;

public class WmsLocation {
    private String inventLocationId;
    private String wMSLocationId;
    private String checkText;

    public WmsLocation(String _InventLocationId, String _wmsLocationId, String _checkText){
        this.inventLocationId = _InventLocationId;
        this.wMSLocationId= _wmsLocationId;
        this.checkText = _checkText;
    }

    public String getInventLocationId(){ return inventLocationId; }

    public String getwMSLocationId() { return wMSLocationId + ", " + this.checkText;}

    public String getCheckText() { return checkText; }
}
