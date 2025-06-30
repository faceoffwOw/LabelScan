package test.test.bkoclient_tsd_v02.Models;

public class LocationAx {
    private String inventLocationId;
    private String name;

    public LocationAx(String _InventLocationId, String _Name){
        this.inventLocationId = _InventLocationId;
        this.name = _Name;
    }

    public String getInventLocationId() {
        return inventLocationId + ", " + this.name;
    }

    public String getName() {
        return name;
    }
}
