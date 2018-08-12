package com.jplpereira.minhasmedidas.database.model;

public class Measurement {

    public static final String TABLE_NAME = "measurements";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_GLUCOSE = "glucose";
    public static final String COLUMN_SYSTOLIC = "systolic";
    public static final String COLUMN_DIASTOLIC = "diastolic";

    private int id;
    private String timestamp;
    private int glucose;
    private int systolic;
    private int diastolic;

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_GLUCOSE + " INTEGER NOT NULL,"
            + COLUMN_SYSTOLIC + " INTEGER NOT NULL,"
            + COLUMN_DIASTOLIC + " INTEGER NOT NULL"
            + ")";

    public Measurement() {

    }

    public Measurement(int id, String timestamp, int glucose, int systolic, int diastolic) {
        this.id = id;
        this.timestamp = timestamp;
        this.glucose = glucose;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getGlucose() {
        return glucose;
    }

    public void setGlucose(int glucose) {
        this.glucose = glucose;
    }

    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }
}
