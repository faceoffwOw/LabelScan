package test.test.bkoclient_tsd_v02.ProdLorry.Models;

public class ProdLorryUserProfile {
    private String emplId;
    private String shiftId;
    private String wrkCtrId;
    private String department;
    private boolean isPreScan;

    public ProdLorryUserProfile(String _emplId, String _shiftId, String _wrkCtrId, String _department, boolean _isPreScan)
    {
        emplId = _emplId;
        shiftId = _shiftId;
        wrkCtrId = _wrkCtrId;
        department = _department;
        isPreScan = _isPreScan;
    }

    public boolean IsEmpty()
    {
        return emplId.isEmpty() || shiftId.isEmpty() || wrkCtrId.isEmpty();
    }

    public String getEmplId() { return emplId; }
    public String getShiftId() { return shiftId; }
    public String getWrkCtrId() { return wrkCtrId; }
    public String getDepartment() { return department; }
    public boolean IsPreScan() { return isPreScan; }
}
