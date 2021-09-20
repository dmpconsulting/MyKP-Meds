package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import java.io.IOException;

public class DeleteHtmlFileBroadcase extends BroadcastReceiver
{

	@Override
	public void onReceive(Context context, Intent intent)
	{
 		try {
			Util.cleanAttachments(context);
		} catch (IOException e) {
			PillpopperLog.say("Oops!, IOException" + e.getMessage());
		} catch (Exception e) {
			PillpopperLog.say("Oops!, Exception" + e.getMessage());
		}
	}
}
