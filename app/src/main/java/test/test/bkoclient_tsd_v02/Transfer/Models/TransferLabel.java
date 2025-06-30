package test.test.bkoclient_tsd_v02.Transfer.Models;

public class TransferLabel {
    private String journalId;
    private String fromInventLocationId;
    private String toInventLocationId;
    private  String checkPointName;
    public String dateScan;
    private boolean isAllowDeparture;

    public TransferLabel(String _journalId, String _fromInventLocationId, String _toInventLocationId, String _checkPointName, String _dateScan, boolean _isAllowDeparture){
        this.journalId = _journalId;
        this.fromInventLocationId = _fromInventLocationId;
        this.toInventLocationId = _toInventLocationId;
        this.checkPointName = _checkPointName;
        this.dateScan = _dateScan;
        this.isAllowDeparture = _isAllowDeparture;
    }

    public String getJournalId() { return journalId; }
    public String getFromInventLocationId() { return fromInventLocationId; }
    public String getToInventLocationId() { return toInventLocationId; }
    public String getCheckPoint() { return checkPointName; }
    public String getDateScan() { return dateScan; }
    public boolean IsAllowDeparture() { return isAllowDeparture; }
}
