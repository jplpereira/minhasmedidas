package com.jplpereira.minhasmedidas.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jplpereira.minhasmedidas.database.model.Measurement;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "measurements.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Measurement.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Measurement.TABLE_NAME);

        onCreate(db);
    }

    public long insertPresentMeasurement(int glucose, int systolic, int diastolic){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Measurement.COLUMN_GLUCOSE, glucose);
        values.put(Measurement.COLUMN_SYSTOLIC, systolic);
        values.put(Measurement.COLUMN_DIASTOLIC, diastolic);

        long id = db.insert(Measurement.TABLE_NAME, null, values);

        db.close();

        return id;
    }

    public long insertPastMeasurement(String timestamp, int glucose, int systolic, int diastolic){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Measurement.COLUMN_TIMESTAMP, timestamp);
        values.put(Measurement.COLUMN_GLUCOSE, glucose);
        values.put(Measurement.COLUMN_SYSTOLIC, systolic);
        values.put(Measurement.COLUMN_DIASTOLIC, diastolic);

        long id = db.insert(Measurement.TABLE_NAME, null, values);

        db.close();

        return id;
    }

    public Measurement getMeasurement(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Measurement.TABLE_NAME, new String[]{
                        Measurement.COLUMN_ID,
                        Measurement.COLUMN_TIMESTAMP,
                        Measurement.COLUMN_GLUCOSE,
                        Measurement.COLUMN_SYSTOLIC,
                        Measurement.COLUMN_DIASTOLIC},
                Measurement.COLUMN_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null,null);

        if (cursor != null)
            cursor.moveToFirst();

        // int id, String timestamp, int glucose, int systolic, int diastolic
        Measurement expense = new Measurement(
                cursor.getInt(cursor.getColumnIndex(Measurement.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(Measurement.COLUMN_TIMESTAMP)),
                cursor.getInt(cursor.getColumnIndex(Measurement.COLUMN_GLUCOSE)),
                cursor.getInt(cursor.getColumnIndex(Measurement.COLUMN_SYSTOLIC)),
                cursor.getInt(cursor.getColumnIndex(Measurement.COLUMN_DIASTOLIC)));

        cursor.close();

        return expense;
    }

    public List<Measurement> getAllMeasurements() {
        List<Measurement> measurements = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + Measurement.TABLE_NAME + " ORDER BY " +
                Measurement.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Measurement measurement = new Measurement();
                measurement.setId(cursor.getInt(cursor.getColumnIndex(Measurement.COLUMN_ID)));
                measurement.setTimestamp(cursor.getString(cursor.getColumnIndex(
                        Measurement.COLUMN_TIMESTAMP)));
                measurement.setGlucose(cursor.getInt(cursor.getColumnIndex(
                        Measurement.COLUMN_GLUCOSE)));
                measurement.setSystolic(cursor.getInt(cursor.getColumnIndex(
                        Measurement.COLUMN_SYSTOLIC)));
                measurement.setDiastolic(cursor.getInt(cursor.getColumnIndex(
                        Measurement.COLUMN_DIASTOLIC)));

                measurements.add(measurement);
            } while (cursor.moveToNext());

            cursor.close();
            db.close();
        }

        return measurements;
    }

    public int getMeasurementsCount() {
        String countQuery = "SELECT * FROM " + Measurement.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public int updateMeasurement(Measurement measurement){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Measurement.COLUMN_GLUCOSE, measurement.getGlucose());
        values.put(Measurement.COLUMN_SYSTOLIC, measurement.getSystolic());
        values.put(Measurement.COLUMN_DIASTOLIC, measurement.getDiastolic());
        values.put(Measurement.COLUMN_TIMESTAMP, measurement.getTimestamp());

        return db.update(Measurement.TABLE_NAME, values, Measurement.COLUMN_ID + " = ?",
                new String[]{String.valueOf(measurement.getId())});
    }

    public void deleteMeasurement(Measurement expense){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Measurement.TABLE_NAME, Measurement.COLUMN_ID + " = ?",
                new String[]{String.valueOf(expense.getId())});
        db.close();
    }
}