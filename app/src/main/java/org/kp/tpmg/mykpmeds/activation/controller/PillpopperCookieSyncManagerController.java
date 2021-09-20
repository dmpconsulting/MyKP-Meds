package org.kp.tpmg.mykpmeds.activation.controller;


public class PillpopperCookieSyncManagerController {
	public static PillpopperCookieSyncInterface getPillpopperCookiesSyncMgr() {
		return pillpopperCookiesSyncMgr;
	}

	public static void setPillpopperCookiesSyncMgr(PillpopperCookieSyncInterface pillpopperCookiesSyncMgr) {
		PillpopperCookieSyncManagerController.pillpopperCookiesSyncMgr = pillpopperCookiesSyncMgr;
	}

	public interface PillpopperCookieSyncInterface {
	     void syncPillpopperCookieStore(String ssoSessionId);
	}

	private static PillpopperCookieSyncInterface pillpopperCookiesSyncMgr = null;

	public static void registerForPillpopperCookieSync(PillpopperCookieSyncInterface appinterface) {
	    setPillpopperCookiesSyncMgr(appinterface);
	}
}
