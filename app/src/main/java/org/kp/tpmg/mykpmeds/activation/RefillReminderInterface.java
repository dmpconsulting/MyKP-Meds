package org.kp.tpmg.mykpmeds.activation;

import org.json.JSONObject;

/**
 * Created by M1024581 on 2/15/2018.
 */

public interface RefillReminderInterface {

    void addLogEntryForRefillReminderUpdate(JSONObject obj);
    void addLogEntryForRefillReminderDelete(JSONObject obj);

}
