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


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;


public class RefillReminderDbHandler {

    private SupportSQLiteDatabase database;
    private static RefillReminderDbHandler databaseHandler;

    public static RefillReminderDbHandler getInstance(Context context) {
        if (null == databaseHandler) {
            databaseHandler = new RefillReminderDbHandler(context);
        }
        return databaseHandler;
    }

    private RefillReminderDbHandler(Context context) {
        try {
            database = SupportRefillReminderDbHelper.getWritableDatabase();
        } catch (UnsatisfiedLinkError | Exception ne){
            PillpopperLog.exception(ne.getMessage());
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] where) {
        int status = -1;
        status = database.update(table,0, values, whereClause, where);
        return status;
    }

    public void insertInDB(String tableName, String columnHack, ContentValues contentValues) {
        database.insert(tableName, 0, contentValues);
    }


    public Cursor executeRawQuery(String query, String[] selectionArgs) {
        Cursor cursor = null;
        database = SupportRefillReminderDbHelper.getWritableDatabase();
        if (database != null) {
            cursor = database.query(query, selectionArgs);
        }
        return cursor;
    }

    public synchronized void insert(Context context, String tableName, Object objectToInsert, String columnHack) {
        ContentValues contentValues = RefillReminderDbUtils.getContentValues(tableName, objectToInsert);
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

    public void deleteTableData(String tableName) {

        database.execSQL("DELETE from " + tableName);

    }

    /**
     * Delete the contents of the table
     **/
    public void delete(String tableName, String where, String[] whereClause) {
        try {
            database.delete(tableName, where, whereClause);
        } catch (Exception e) {
            PillpopperLog.say(e.getMessage());
        }
    }
}
