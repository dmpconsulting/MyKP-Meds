package com.montunosoftware.pillpopper.refillreminder;

import android.content.Context;

import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;

import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M1032896 on 6/6/2018.
 */

@Implements(RefillReminderController.class)
public class RefillReminderControllerShadow {

    public void __constructor__(Context context){

    }

    public List<RefillReminder> getOverdueRefillRemindersForCards(){
        return new ArrayList<>();
    }

    public List<RefillReminder> getRefillReminders(){
        return new ArrayList<>();
    }
}
