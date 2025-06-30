package test.test.bkoclient_tsd_v02.Models;

import java.text.SimpleDateFormat;

public class Labels {
    private Long unitId;
    private String datePrint;
    private String frakciya;
    private String batchNum;
    private String batchData;
    private String itemId;
    private String standardId;
    private String configId;
    private String lineRecId;

    public Labels(Long _UnitId, String _DatePrint, String _frakciya, String _BatchNum, String _BatchData,
                  String _itemId, String _StandardId, String _ConfigId, String _LineRecId){

        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        this.unitId = _UnitId;
        this.datePrint = _DatePrint;
        this.frakciya = _frakciya;
        this.batchNum = _BatchNum;
        this.batchData = _BatchData;
        this.itemId = _itemId;
        this.standardId = _StandardId;
        this.configId = _ConfigId;
        this.lineRecId = _LineRecId;
    }

    public Long getUnitId(){
        return unitId;
    }
    public String getDatePrint() { return datePrint; }
    public String getFrakciya() { return frakciya; }
    public String getBatchNum() { return batchNum; }
    public String getBatchData() { return batchData; }
    public String getItemId() { return itemId; }
    public String getStandardId() { return standardId; }
    public String getConfigId() { return configId; }
    public String getLineRecId() { return lineRecId; }
}
