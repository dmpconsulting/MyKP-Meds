package com.montunosoftware.pillpopper.service.getstate;

import android.content.Intent;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.model.GetHistoryEvents;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import androidx.test.core.app.ApplicationProvider;

import static com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService.*;


/**
 * Created by M1028309 on 5/9/2017.
 */
@Implements(StateDownloadIntentService.class)
public class StateDownloadIntentServiceShadow {

    public void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            switch(action) {
                case ACTION_GET_STATE:
                   // handleActionGetState();
                    break;
                case ACTION_GET_HISTORY_EVENTS:
                    try {
                        try {
                            handleActionGetHistoryEvents();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_INTERMEDIATE_GET_STATE:
                   // handleActionIntermediateGetState();
                    break;
                default:
                    break;
            }
        }
    }

    private void handleActionGetHistoryEvents() throws JSONException, FileNotFoundException, URISyntaxException {
      //  DatabaseHandler.getInstance(ApplicationProvider.getApplicationContext()).deleteTableData(DatabaseConstants.HISTORY_TABLE);
      //  JSONArray historyEventsArray = new JSONArray(TestUtil.readFromResource("HsitoryEventResponse.json"));//.getJSONArray("historyEvents");
        Gson gson = new Gson();
        JSONArray historyEventsArray = TestUtil.getJsonObject("/HsitoryEventResponse.json").getJSONObject("pillpopperResponse").getJSONArray("historyEvents");
       // PillpopperResponse pr=gson.fromJson(TestUtil.readFromResource("HsitoryEventResponse.json"), PillpopperResponse.class);
        for (int i = 0; i < historyEventsArray.length(); i++) {
            GetHistoryEvents historyEventsObject = gson.fromJson(historyEventsArray.getJSONObject(i).toString(), GetHistoryEvents.class);
            DatabaseHandler.getInstance(ApplicationProvider.getApplicationContext()).insert(ApplicationProvider.getApplicationContext(), DatabaseConstants.HISTORY_TABLE, historyEventsObject, "", "");
            PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(true);
        }
    }

}
