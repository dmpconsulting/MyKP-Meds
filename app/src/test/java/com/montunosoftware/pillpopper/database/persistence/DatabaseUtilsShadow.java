package com.montunosoftware.pillpopper.database.persistence;

import android.content.Context;

import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.model.Drug;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M1032896 on 10/25/2017.
 */

@Implements(DatabaseUtils.class)
public class DatabaseUtilsShadow {

    @Implementation
    public List<Drug> getDrugListForOverDue(Context _thisActivity){

        return new ArrayList<Drug>();
    }
}
