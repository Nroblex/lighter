package device;

import org.joda.time.DateTime;

public class SchemaDevice {

    private Integer ID;
    private Integer deviceID;
    private DateTime timePoint;
    private String action;
    private DateTime updatedAt;
    private Integer dayOfWeek;
    private String nameOfWeekday;

    public SchemaDevice() {

    }

    public SchemaDevice setTimePoint(DateTime timePoint){
        this.timePoint=timePoint;
        return this;
    }

    public SchemaDevice setDeviceID(Integer deviceId){
        this.deviceID=deviceId;
        return this;
    }

    public SchemaDevice setAction(String action){
        this.action=action;
        return this;
    }

    public SchemaDevice setUpdatedAt(DateTime updated){
        this.updatedAt=updated;
        return this;
    }

    public SchemaDevice setDayOfWeek(Integer dayOfWeek){
        this.dayOfWeek=dayOfWeek;
        return this;
    }

    public SchemaDevice setID(Integer id){
        this.ID = id;
        return this;
    }

    public SchemaDevice setNameOfWeekday(String nameOfWeekday){
        this.nameOfWeekday=nameOfWeekday;
        return this;
    }
    public Integer getDayOfWeek() {
        return dayOfWeek;
    }


    public Integer getID() {
        return ID;
    }
    public DateTime getTimePoint() {
        return timePoint;
    }
    public DateTime getUpdatedAt() {
        return updatedAt;
    }
    public String getAction() {
        return action;
    }
    public Integer getDeviceID() {
        return deviceID;
    }
    public String getNameOfWeekday() {
        return nameOfWeekday;
    }
}
