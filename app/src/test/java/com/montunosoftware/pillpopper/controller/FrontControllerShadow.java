package com.montunosoftware.pillpopper.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.montunosoftware.pillpopper.android.PillpopperActivity;

import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.ScheduleListItemDataWrapper;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M1032896 on 11/7/2017.
 */


@Implements(FrontController.class)
public class FrontControllerShadow {


    private String mockScheduleData = "[{\"possibleNextActiveListItem\":true,\"userId\":\"83dd9fd137b2abfe31a21aa7e85a6882\",\"userFirstName\":\"WPPMRNFAIAGIGBEFDFN\",\"userType\":\"primary\",\"pillTime\":\"2000\",\"showThreeDotAction\":true,\"scheduleMainTimeHeader\":{\"hours\":8,\"minutes\":0,\"amPm\":\"PM\",\"headerPillpopperTime\":{\"_gmtSeconds\":1510065000}},\"pillIdsForTakenAction\":[\"068BA1CF-EBCA-473C-8B07-A9C4A081AD85\"],\"drugList\":[{\"pillId\":\"068BA1CF-EBCA-473C-8B07-A9C4A081AD85\",\"pillName\":\"dasatinib\",\"genericName\":\"dasatinib\",\"scheduleType\":\"scheduled\",\"dayPeriod\":\"1\",\"dose\":\"140mg\",\"notifyAfter\":{\"_gmtSeconds\":1509460200},\"effectiveLastTaken\":{\"_gmtSeconds\":-1},\"start\":{\"_gmtSeconds\":1509388200},\"end\":{\"_gmtSeconds\":-1},\"pillTime\":\"2000\",\"drugPreference\":{\"archived\":\"0\",\"invisible\":\"0\",\"deleted\":\"0\",\"dosageType\":\"custom\",\"weekdays\":\"\",\"activeOnWeekday\":[true,true,true,true,true,true,true,true]},\"user\":{\"userId\":\"83dd9fd137b2abfe31a21aa7e85a6882\",\"userType\":\"primary\",\"enabled\":\"Y\",\"firstName\":\"WPPMRNFAIAGIGBEFDFN\"},\"userPreference\":{\"preventEarlyDosesWarning\":\"1\"}}]}]";

    @Implementation
    public List<ScheduleListItemDataWrapper> getMedicationScheduleForDay(PillpopperActivity _thisActivity, PillpopperDay focusDay) {
        Gson gson = new Gson();
        if(focusDay.before(PillpopperDay.today())){
            return new ArrayList<>();
        }else{
            return gson.fromJson(mockScheduleData, new TypeToken<List<ScheduleListItemDataWrapper>>(){}.getType());
        }
    }

}
