/**
 * Copyright (c) 2008-2020 Zetetic LLC
 *             All rights reserved.
 *
 *             Redistribution and use in source and binary forms, with or without
 *             modification, are permitted provided that the following conditions are met:
 *                 * Redistributions of source code must retain the above copyright
 *                   notice, this list of conditions and the following disclaimer.
 *                 * Redistributions in binary form must reproduce the above copyright
 *                   notice, this list of conditions and the following disclaimer in the
 *                   documentation and/or other materials provided with the distribution.
 *                 * Neither the name of the ZETETIC LLC nor the
 *                   names of its contributors may be used to endorse or promote products
 *                   derived from this software without specific prior written permission.
 *
 *             THIS SOFTWARE IS PROVIDED BY ZETETIC LLC ''AS IS'' AND ANY
 *             EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *             WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *             DISCLAIMED. IN NO EVENT SHALL ZETETIC LLC BE LIABLE FOR ANY
 *             DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *             (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *             LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *             ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *             (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *             SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.montunosoftware.pillpopper.database.persistence;


import android.content.Context;
import android.os.Handler;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.database.DatabaseConstants;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SupportFactory;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.io.File;

import static com.montunosoftware.pillpopper.android.util.PillpopperApplication.DATABASE_NAME;


public class SupportDatabaseHelper {
    private static int dbversion = BuildConfig.VERSION_CODE;
    private static Context mContext = RunTimeData.getInstance().getmContext();
    private static SupportSQLiteDatabase database;

    public SupportDatabaseHelper(){
        initDb();
    }

    public static void initDb() {
        try {
            File sourceDatabase = mContext.getDatabasePath(DATABASE_NAME);

            byte[] passphrase = SQLiteDatabase.getBytes(ActivationUtil.getSecretKey(mContext).toCharArray());
            SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
                @Override
                public void preKey(SQLiteDatabase sqLiteDatabase) {
                }

                @Override
                public void postKey(SQLiteDatabase sqLiteDatabase) {
                    sqLiteDatabase.rawExecSQL("PRAGMA cipher_migrate;");
                }
            };
            SupportFactory factory = new SupportFactory(passphrase, hook);
            SupportSQLiteOpenHelper.Configuration cfg =
                    SupportSQLiteOpenHelper.Configuration.builder(mContext)
                            .name(sourceDatabase.getAbsolutePath())
                            .callback(new SupportSQLiteOpenHelper.Callback(dbversion) {
                                @Override
                                public void onOpen(SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    if (!db.isReadOnly()) {
                                        // Enable foreign key constraints
                                        db.execSQL("PRAGMA foreign_keys=ON;");
                                    }
                                }

                                @Override
                                public void onCreate(SupportSQLiteDatabase database) {
                                    PillpopperLog.say("Inside Oncreate");
                                    if (dbversion > 1) {
                                        PillpopperLog.say("Inside Oncreate Version " + dbversion);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_PILL_QUERY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_PILL_PREFERENCE_QUERY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_USER_PREFERENCE_QUERY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_USER_QUERY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_PILL_SCHEDULE);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_PENDING_IMAGE_SYNC_REQUESTS);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_PILL_HISTORY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_LOG_ENTRY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_USER_REMINDERS_QUERY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_LATE_REMINDERS_QUERY);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_IMAGE_FAILURE_ENTRIES);
                                        database.execSQL(DatabaseConstants.DROP_TABLE_HISTORY_PREFERENCE_QUERY);
                                    }
                                    PillpopperLog.say("Inside Oncreate1 Version " + dbversion);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_PILL_QUERY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_PILL_PREFERENCE_QUERY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_USER_PREFERENCE_QUERY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_USER_QUERY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_PILL_SCHEDULE);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_PENDING_IMAGE_SYNC_REQUESTS);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_PILL_HISTORY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_LOG_ENTRY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_USER_REMINDERS_QUERY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_LATE_REMINDERS_QUERY);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_FDB_IMAGE);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_CUSTOM_IMAGE);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_IMAGE_FAILURE_ENTRIES);
                                    database.execSQL(DatabaseConstants.CREATE_TABLE_HISTORY_PREFERENCES);
                                }

                                @Override
                                public void onDowngrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
                                    super.onDowngrade(db, oldVersion, newVersion);
                                    db.setVersion(newVersion);
                                }

                                @Override
                                public void onUpgrade(SupportSQLiteDatabase database, int oldVersion,
                                                      int newVersion) {
                                    PillpopperLog.say("Upgrade -- in DBHelper onUpgrade");
                                    PillpopperLog.say("Upgrade -- oldVersion - " + oldVersion + "  newVersion " + newVersion);
                                    LoggerUtils.info("---DEBUG---onUpgrade called--");
                                    boolean refreshStateRequired = false;
                                    if (String.valueOf(oldVersion).length() == 8) {
                                        oldVersion = oldVersion / 1000;
                                    }
                                    switch (oldVersion) {
                                        case 40300:
                                        case 40301:
                                            upgradeDBtoV44(database, oldVersion);
                                        case 40400:
                                        case 40500:
                                        case 40501:
                                        case 40600:
                                        case 40600016:
                                            upgradeDBtoV47(database);
                                        case 40700:
                                        case 40701:
                                        case 40702:
                                        case 40800:
                                        case 40900:
                                        case 40901:
                                            upgradeDBto50(database);
                                        case 50000:
                                        case 50100:
                                        case 50200:
                                        case 50300:
                                            upgradeDBto53(database);
                                            refreshStateRequired = true;
                                            break;
                                        default:
                                            if (oldVersion > 30802) {
                                                refreshStateRequired = true;
                                            } else {
                                                PillpopperLog.say("OnUpgrade - Invalid old version found - Calling onCreate");
                                                onCreate(database);
                                            }
                                            break;
                                    }

                                    try {
                                        if (refreshStateRequired) {
                                            new Handler().postDelayed(() -> {
                                                refreshState(mContext);
                                                PillpopperLog.say("Upgrade DB Success");
                                            }, 3000);
                                        }
                                    } catch (Exception ex) {
                                        PillpopperLog.say("Exception onUpgrade DB", ex);
                                    }
                                }
                            })
                            .build();
            SupportSQLiteOpenHelper helper = factory.create(cfg);
            database = (SQLiteDatabase) factory.create(cfg).getWritableDatabase();
            helper.close();

        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    private static void upgradeDBto53(SupportSQLiteDatabase database) {
        LoggerUtils.info("Upgrade -- upgradeDBto53");
        //History preference table is added for history calendar view
        database.execSQL(DatabaseConstants.DROP_TABLE_HISTORY_PREFERENCE_QUERY);
        database.execSQL(DatabaseConstants.CREATE_TABLE_HISTORY_PREFERENCES);
        LoggerUtils.info("Upgrade -- History Preference table updated");
        //pill table alter queries for scheduleGUID
        database.execSQL(DatabaseConstants.CREATE_TABLE_PILL_NEW_QUERY);
        database.execSQL(DatabaseConstants.ALTER_TABLE_INSERT_PILL);
        database.execSQL(DatabaseConstants.DROP_TABLE_PILL_QUERY);
        database.execSQL(DatabaseConstants.ALTER_TABLE_RENAME_PILL);
    }

    private static void upgradeDBto50(SupportSQLiteDatabase database) {
        //user table alter for isTeenToggleEnabled
        database.execSQL(DatabaseConstants.ALTER_TABLE_RENAME_USER);
        database.execSQL(DatabaseConstants.DROP_TABLE_USER_QUERY);
        database.execSQL(DatabaseConstants.CREATE_TABLE_USER_QUERY);
        database.execSQL(DatabaseConstants.ALTER_TABLE_INSERT_USER_FOR_ISTEENTOGGLEENABLED);
        database.execSQL(DatabaseConstants.DROP_TABLE_TEMP_USER);
    }

    public static SupportSQLiteDatabase getWritableDatabase(){
        if(null == database){
            initDb();
        }
        return database;
    }

    private static void upgradeDBtoV47(SupportSQLiteDatabase database) {

        //database.execSQL("ALTER TABLE PILLPREFERENCE ADD COLUMN scheduleChoice VARCHAR(20)");
        database.execSQL(DatabaseConstants.ALTER_TABLE_RENAME_PILL_PREFERENCES);
        database.execSQL(DatabaseConstants.DROP_TABLE_PILL_PREFERENCE_QUERY);
        database.execSQL(DatabaseConstants.CREATE_TABLE_PILL_PREFERENCE_QUERY);
        database.execSQL(DatabaseConstants.ALTER_TABLE_INSERT_PILL_PREFERENCES_SCHEDULE_CHOICE);
        database.execSQL(DatabaseConstants.DROP_TABLE_TEMP_PILL_PREFERENCES);
        // user table alter for US48133 Teen proxy
        database.execSQL(DatabaseConstants.ALTER_TABLE_RENAME_USER);
        database.execSQL(DatabaseConstants.DROP_TABLE_USER_QUERY);
        database.execSQL(DatabaseConstants.CREATE_TABLE_USER_QUERY);
        database.execSQL(DatabaseConstants.ALTER_TABLE_INSERT_USER_FOR_ISTEEN);
        database.execSQL(DatabaseConstants.DROP_TABLE_TEMP_USER);
    }

    private static void upgradeDBtoV44(SupportSQLiteDatabase database, int oldVersion) {
        database.execSQL(DatabaseConstants.ALTER_TABLE_RENAME_USER);
        database.execSQL(DatabaseConstants.DROP_TABLE_USER_QUERY);
        database.execSQL(DatabaseConstants.CREATE_TABLE_USER_QUERY);
        if (oldVersion == 40300 || oldVersion == 40301) {
            database.execSQL(DatabaseConstants.ALTER_TABLE_INSERT_USER_FOR_V43);
        } else {
            database.execSQL(DatabaseConstants.ALTER_TABLE_INSERT_USER);
        }
        database.execSQL(DatabaseConstants.DROP_TABLE_TEMP_USER);
    }

    public static void refreshState(Context mContext) {
        PillpopperLog.say("Upgrade -- in DBHelper refreshState");
        PillpopperAppContext pillpopperAppContext = PillpopperAppContext.getGlobalAppContext(mContext);
        pillpopperAppContext.setState(mContext, pillpopperAppContext.getState(mContext));
        pillpopperAppContext.ensureNoArguments();
    }
}
