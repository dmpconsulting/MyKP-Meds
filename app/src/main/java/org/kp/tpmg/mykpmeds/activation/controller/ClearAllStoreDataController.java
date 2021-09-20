package org.kp.tpmg.mykpmeds.activation.controller;

/**
 * Created by M1023050 on 20-Jun-19.
 */

public class ClearAllStoreDataController {

    public interface ClearAllDataInterface {
        void resetData();
    }

    public static ClearAllDataInterface clearAllDataInterface = null;

    public static void registerForClearData(ClearAllDataInterface clearDataInterface) {
        clearAllDataInterface = clearDataInterface;
    }
}
