package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import java.io.IOException;

public class DeleteHtmlFileBroadcastOnDeviceReBoot extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub

		//Boot complete 
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){

			/*Check if the current time is still not exceeded the 24 hours after the HTML file created.
			if not then get the file delete time and set the pending intent to trigger deleting HTML.
			if exceeded delete the delete the HTML file
			 */
			/*			if(timeExceeded){
			 			Util.cleanAttachments(context); 
			}else{
			}
			 */			
			try {
				Util.cleanAttachments(context);
			} catch (IOException e) {
				 PillpopperLog.say("Oops!, IOException" + e.getMessage());
			} catch(Exception e){
	        	 PillpopperLog.say("Oops!, Exception" + e.getMessage());
	        }
		}
	}

}
