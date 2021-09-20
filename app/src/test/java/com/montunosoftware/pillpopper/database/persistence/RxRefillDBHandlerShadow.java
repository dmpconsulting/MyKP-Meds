package com.montunosoftware.pillpopper.database.persistence;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.kp.tpmg.ttg.database.RxRefillDBHandler;
import org.kp.tpmg.ttg.database.RxRefillDBHelper;
import org.kp.tpmg.ttg.utils.RxRefillLoggerUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;

@Implements(RxRefillDBHandler.class)
public class RxRefillDBHandlerShadow {

    private RxRefillDBHelper rxRefillDBHelper;
    private Context mContext= null;
    private final String testDatabase = "/database/prescription.db";
    private SQLiteDatabase mSQLiteDB;
    private String mDBPath;

    public void  __constructor__(Context context) {
        try {
            mDBPath = RxRefillDBHandlerShadow.class.getResource(testDatabase).toURI().getPath();
        } catch (Exception e) {
            RxRefillLoggerUtils.exception(e.getMessage());
        }
    }

    @Implementation
    public Cursor executeRawQuery(String query, String[] selectionArgs) {
        mSQLiteDB =  SQLiteDatabase.openDatabase(new File(mDBPath).getAbsolutePath(), null, 1);
        Cursor cur= mSQLiteDB.rawQuery(query, selectionArgs);
        // testDatabase.close();
        return cur;
    }

    @Implementation
    public Cursor executeQuery(String query) {
        mSQLiteDB =  SQLiteDatabase.openDatabase(new File(mDBPath).getAbsolutePath(), null, 1);
        Cursor cur= mSQLiteDB.rawQuery(query, null);
        //testDatabase.close();
        return cur;

    }
}
