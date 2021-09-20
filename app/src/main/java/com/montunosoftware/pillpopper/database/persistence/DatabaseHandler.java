package com.montunosoftware.pillpopper.database.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.model.PillList;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.TimeZone;

import static com.montunosoftware.pillpopper.database.DatabaseConstants.EFF_LAST_TAKEN;
import static com.montunosoftware.pillpopper.database.DatabaseConstants.LAST_TAKEN;
import static com.montunosoftware.pillpopper.database.DatabaseConstants.MISSED_DOSES_LAST_CHECKED;
import static com.montunosoftware.pillpopper.database.DatabaseConstants.NOTIFY_AFTER;

/***
 * This class is used to handle all database related operations. This class will
 * be called by all the classes for any insert, delete or execute of queries.
 * <p/>
 * When the application is first time installed, this class will call the
 * Database helper class and creates the database with the name and version
 * specified below.
 *
 * @author Madan S
 */
public class DatabaseHandler {
    private static final String TZSECS = "_TZSECS";
    private static DatabaseHandler databaseHandler;
    private ArrayList<String> tzSecsParams;
    private SupportSQLiteDatabase database;
   // private Context appContext;
    private Context mContext;


    /**
     * @param context context of the class
     */
    private DatabaseHandler(Context context) {
        tzSecsParams =null;
        tzSecsParams = new ArrayList<>();
        tzSecsParams.add(LAST_TAKEN);
        tzSecsParams.add(EFF_LAST_TAKEN);
        tzSecsParams.add(NOTIFY_AFTER);
        tzSecsParams.add(MISSED_DOSES_LAST_CHECKED);
        mContext = context;
        initDB();

    }

    private void initDB() {
        try{

            if(null == mContext){
                mContext = RunTimeData.getInstance().getmContext();
            }
            if(null!=mContext && null!=mContext.getApplicationContext()){
                database = SupportDatabaseHelper.getWritableDatabase();
            }

        }catch (Exception  e){
            LoggerUtils.info("Exception while getWritableDatabase -- " + e.getMessage());
        }

    }

    /***
     * Singleton instance of database handler.
     *
     * @return instance of database handler
     */
    public synchronized static DatabaseHandler getInstance(Context context) {
        if (null == databaseHandler) {
            initiateDatabaseHandler(context);
        }
        return databaseHandler;
    }

    /**
     * @param context context of the class
     */
    public synchronized static void initiateDatabaseHandler(Context context) {

         databaseHandler = new DatabaseHandler(context);

    }

    public synchronized static void resetAppToNewVersion(Context context) {
		/*if (DATABASE_VERSION > 1) {
			FrontController.getInstance(context).resetAppToNewVersion();
		}*/
    }

    /**
     * Inserts the content to the table
     **/
    public synchronized void insert(Context context, String tableName, Object objectToInsert,
                                    String columnHack, String userId) {
        ContentValues contentValues = DatabaseUtils.getContentValues(context, tableName,
                objectToInsert, userId);
        try {
            if (contentValues.size() > 0) {

                insertInDB(tableName, columnHack, contentValues);

            } else {
                PillpopperLog.say("No values found to insert the data into database");
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }
    }

    public void insertInDB(String tableName, String columnHack, ContentValues contentValues) {
        includeTZFields(contentValues);

       // if(null == databaseHandler && null == database) {
            initiateDatabaseHandler(mContext);
       // }

        if(null != database) {
            database.insert(tableName, 0, contentValues);
        }

    }

    private void includeTZFields(ContentValues contentValues) {
        for(String key:tzSecsParams) {
            if (contentValues != null && contentValues.containsKey(key)&& (contentValues.get(key)==null||contentValues.get(key).toString().isEmpty())) {
                contentValues.put(key.concat(TZSECS), Util.getTzOffsetSecs(TimeZone.getDefault()));
            }
        }
    }

    /**
     * Inserts a pill schedule entry into DB
     */
    public synchronized void insertPillSchedule(String tableName, PillList pillListToInsert,
                                                String columnHack) {
        ContentValues contentValues = new ContentValues();
        try {
            if (null != pillListToInsert) {

                if (pillListToInsert.getSchedule() != null) {
                    for (int i = 0; i < pillListToInsert.getSchedule().length; i++) {
                        if(!("-1").equalsIgnoreCase(pillListToInsert.getSchedule()[i])) {
                            contentValues = DatabaseUtils.setContentValues_PillSchedule(pillListToInsert.getPillId(),
                                    Util.convertHHMMtoTimeFormat(pillListToInsert.getSchedule()[i]));
                            if (contentValues.size() > 0) {

                                insertInDB(tableName, columnHack, contentValues);
//                            }
                                PillpopperLog.say("DatabaseUtils - insertPillSchedule() - Value inserted in to Pill Schedule table -- " + contentValues.toString());
                            } else {
                                PillpopperLog.say("No values found to insert the data into database");
                            }
                        }
                    }
                } else {
                    contentValues.put(DatabaseConstants.PILL_ID, pillListToInsert.getPillId());

                    insertInDB(tableName, columnHack, contentValues);

                }

            }


        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }
    }

    /**
     * Inserts the content to the table
     **/
    public synchronized void insert(String tableName, ContentValues contentValues) {

        insertInDB(tableName, null, contentValues);

    }

    /**
     * Delete the contents of the table
     **/
    public void delete(String tableName, String where, String[] whereClause) {
        try {
        /*
		* Uncomment the below commented lines while running the test cases related to DB
		*/

                database.delete(tableName, where, whereClause);

        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }
    }

    public void deleteTableData(String tableName, Context context) {
        try {
            if (null == database) {
                //error handling for firebase crash. reInitialize the db handler
                initiateDatabaseHandler(context);
            }
            database.execSQL("DELETE from " + tableName);
        }catch(Exception ex){
            PillpopperLog.say(ex.getMessage());
        }
    }

    /**
     * Any query execution which needs a result
     **/
    public Cursor executeQuery(String query) {
        Cursor cursor = null;
        if (database != null) {
            cursor = database.query(query,null);
        }
        return cursor;
    }

    /**
     * Any query execution which doesn't need a result
     */
    public void executeSQL(String query) {

            database.execSQL(query);


    }

    /**
     * @param table table name
     * @param values content values
     * @param whereClause where clause
     * @param where       value for where clause
     * @return             result of update
     */
    public int update(String table, ContentValues values, String whereClause,
                      String[] where) {
        int status = -1;
        includeTZFields(values);
        if(null == database)
        {
            initDB();
        }
            status = database.update(table,0, values, whereClause, where);

        return status;
    }

    public int update(Context context, String table, Object objectToInsert, String whereClause,
                      String[] where) {
        int status = -1;
        ContentValues contentValues = DatabaseUtils.getContentValues(context, table,
                objectToInsert, "");
        includeTZFields(contentValues);
            status = database
                    .update(table, 0,contentValues, whereClause, where);

        return status;
    }

    public Cursor executeRawQuery(String query, String[] selectionArgs) {
        Cursor cursor = null;
        if (database != null) {
            cursor = database.query(query, selectionArgs);
        }
        return cursor;
    }

    public void beginTransaction() {
        database.beginTransaction();
    }

    public void setTransactionSuccessful(){
        database.setTransactionSuccessful();
    }

    public void endTransaction(){
        database.endTransaction();
    }

    public void commit() {
        database.execSQL("COMMIT");
    }
}
