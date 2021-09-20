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
package com.montunosoftware.pillpopper.android.refillreminder.database;

import android.content.Context;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SupportFactory;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.io.File;

public class SupportRefillReminderDbHelper {
    private static int dbversion = 1;
    public final static String MYMEDS_REFILL_DATABASE = "mykpmedrefill.db";
    private static Context mContext;
    private static SupportSQLiteDatabase database;
    private static int DATABASE_VERSION = 1;

    public SupportRefillReminderDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super();
        initDb();
    }

    public static void initDb() {
        try {
            mContext = RunTimeData.getInstance().getmContext();
            DATABASE_VERSION = Util.getAppVersionCode(mContext);
            File sourceDatabase = mContext.getDatabasePath(MYMEDS_REFILL_DATABASE);

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
                            .callback(new SupportSQLiteOpenHelper.Callback(DATABASE_VERSION) {


                                @Override
                                public void onOpen(SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    if (!db.isReadOnly()) {
                                        db.execSQL("PRAGMA foreign_keys=ON;");
                                    }
                                }

                                @Override
                                public void onCreate(SupportSQLiteDatabase sqLiteDatabase) {
                                    PillpopperLog.say("Inside Oncreate");
                                    if (version > 1) {
                                        PillpopperLog.say("Inside Oncreate Version " + version);
                                        sqLiteDatabase.execSQL(RefillReminderDbConstants.DROP_TABLE_REFILL_REMINDER);
                                    }
                                    PillpopperLog.say("Inside Oncreate1 Version " + version);
                                    sqLiteDatabase.execSQL(RefillReminderDbConstants.CREATE_TABLE_REFILL_REMINDER);
                                }

                                @Override
                                public void onUpgrade(SupportSQLiteDatabase sqLiteDatabase, int oldVersion,
                                                      int newVersion) {
                                    PillpopperLog.say("Upgrading database structure");
                                    PillpopperLog.say("Upgrade -- oldVersion - " + oldVersion + "  newVersion " + newVersion);
                                    // this needs to be changed when DB Create table schema is changed.
                                    // if there is any change, write appropriate alter table queries which should be called from each version going upwards from 3.6
                                    // no changes in Refill reminder table from v3.6 to v3.7
                                    // if the app is upgraded from any of the below version, calling onCreate
                                    if(oldVersion < 30600) {
                                        PillpopperLog.say("OnUpgrade - Invalid old version found - Calling onCreate");
                                        onCreate(sqLiteDatabase);
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

    public static SupportSQLiteDatabase getWritableDatabase() {
        if (null == database) {
            initDb();
        }
        return database;
    }
}
