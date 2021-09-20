package org.kp.tpmg.mykpmeds.activation.controller;

public class CookieResetController {
	
	public interface CookieResetInterface {
	     void resetCoockies();
	  }

	  static CookieResetInterface myapp = null;

	 public static void registerApp(CookieResetInterface appinterface) {
	    myapp = appinterface;
	  }

}
