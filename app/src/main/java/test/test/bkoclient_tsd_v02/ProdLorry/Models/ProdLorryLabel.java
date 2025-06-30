package test.test.bkoclient_tsd_v02.ProdLorry.Models;

import com.google.gson.annotations.SerializedName;

public class ProdLorryLabel {
    private String stoveReplId;
    private String lorryNum;
    private String emplId;
    private String wrkCtrId;
    private String shiftId;
    private String department;
    private boolean isSortCompleted;
    private String dateScan;
    private boolean isPreScan;

    @SerializedName("scanId")
    private int scanId;

    public ProdLorryLabel(String _stoveReplId, String _lorryNum, String _emplId, String _wrkCtrId, String _shiftId,
                          String _department, boolean _isSortCompleted, String _dateScan, boolean _isPreScan){
        this.stoveReplId = _stoveReplId;
        this.lorryNum = _lorryNum;
        this.emplId = _emplId;
        this.wrkCtrId = _wrkCtrId;
        this.shiftId = _shiftId;
        this.department = _department;
        this.isSortCompleted = _isSortCompleted;
        this.dateScan = _dateScan;
        this.isPreScan = _isPreScan;
    }

    public String getStoveReplId() { return stoveReplId; }
    public String getLorryNum() { return lorryNum; }
    public String getEmplId() { return emplId; }
    public String getWrkCtrId() { return wrkCtrId; }
    public String getShiftId(){ return shiftId; }
    public String getDepartment() { return department; }
    public boolean IsSortCompleted() { return isSortCompleted; }
    public String getDateScan() { return dateScan; }
    public boolean IsPreScan() { return isPreScan; }

    public int getScanId() { return scanId; }
    public void setScanId(int scanId) { this.scanId = scanId; }
    public void setDateScan(String dateScan) { this.dateScan = dateScan; }
}
