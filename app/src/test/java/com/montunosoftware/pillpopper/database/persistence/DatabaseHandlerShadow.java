package com.montunosoftware.pillpopper.database.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;
import java.net.URISyntaxException;

import androidx.test.core.app.ApplicationProvider;

@Implements(DatabaseHandler.class)
public class DatabaseHandlerShadow {


    private SQLiteDatabase testDatabase;


    private final int version = 1;
    private final String testDataBase = "/database/mykpmeds_V380.db";
    private String databasePath;

    public void __constructor__(Context context) {
        try {
            databasePath = DatabaseHandlerShadow.class.getResource(testDataBase).toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Implementation
    public Cursor executeRawQuery(String query, String[] selectionArgs) {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        Cursor cursor = null;
        if(null!=testDatabase) {
            cursor = testDatabase.rawQuery(query, selectionArgs);
        }
        return cursor;

    }

    @Implementation
    public void insertInDB(String tableName, String columnHack, ContentValues contentValues) {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        testDatabase.insert(tableName, null, contentValues);
        testDatabase.close();

    }

    @Implementation
    public Cursor getQuery(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        // testDatabase.close();
        return testDatabase.query(DatabaseConstants.PILL_TABLE, null, DatabaseConstants.PILL_USER_ID + "=?", selectionArgs, null, null, null);
    }

    @Implementation
    public void delete(String tableName, String where, String[] whereClause) {
        try {
            testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
            testDatabase.delete(tableName, where, whereClause);
            testDatabase.close();
        } catch (Exception e) {
            //PillpopperLog.say(e.getMessage());
        }
    }

    @Implementation
    public void deleteTableData(String tableName) {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        testDatabase.execSQL("DELETE from " + tableName);
        testDatabase.close();

    }

    @Implementation
    public Cursor executeQuery(String query) {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        Cursor cur;
        cur = testDatabase.rawQuery(query, null);
        //testDatabase.close();
        return cur;

    }

    @Implementation
    public void executeSQL(String query) {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        testDatabase.execSQL(query);
        testDatabase.close();

    }

    /**
     * Method to close the database after the operation
     */
    @Implementation
    public void closeDatabase() {
        /*
        * Uncomment the below commented lines while running the test cases related to DB
		*/

        if (testDatabase != null && testDatabase.isOpen()) {
            try {
                testDatabase.close();
            } catch (Exception e) {
                PillpopperLog.say(e.getMessage());
            }
        }


    }

    /**
     * @param table       table name
     * @param values      content values
     * @param whereClause where clause
     * @param where       value for where clause
     * @return result of update
     */
    @Implementation
    public int update(String table, ContentValues values, String whereClause,
                      String[] where) {
        int status = -1;
        /*
		* Uncomment the below commented lines while running the test cases related to DB
		*/
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        status = (int) testDatabase.update(table, values, whereClause, where);
        testDatabase.close();
        return status;
    }

    @Implementation
    public int update(String table, Object objectToInsert, String whereClause,
                      String[] where) {
        int status = -1;
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        ContentValues contentValues = DatabaseUtils.getContentValues(ApplicationProvider.getApplicationContext(), table,
                objectToInsert, "");

        status = (int) testDatabase
                .update(table, contentValues, whereClause, where);
        testDatabase.close();

        return status;
    }

    @Implementation
    public void beginTransaction() {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        testDatabase.execSQL("BEGIN TRANSACTION");

    }

    @Implementation
    public void commit() {
        /*
		* Uncomment the below commented lines while running the test cases related to DB
		*/
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        testDatabase.execSQL("COMMIT");

    }

    @Implementation
    public void rollback() {
        testDatabase = SQLiteDatabase.openDatabase(new File(databasePath).getAbsolutePath(), null, version);
        testDatabase.execSQL("ROLLBACK");

    }

}
