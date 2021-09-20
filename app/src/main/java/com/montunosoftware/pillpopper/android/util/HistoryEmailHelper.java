package com.montunosoftware.pillpopper.android.util;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;

import java.util.List;

public class HistoryEmailHelper
{
	public static void emailHistoryOrComplain(PillpopperActivity activity, List<HistoryEvent> eventList, String selectionName, String doseHistoryDays)
	{
		if (eventList == null || eventList.isEmpty()) {
			String logMessage = String.format(activity.getString(R.string.edit_history_no_entries),
					selectionName, Long.parseLong(doseHistoryDays));
			DialogHelpers.showAlertDialog(activity, logMessage);
		} else {
			Util.sendEmail(activity, eventList, String.format(
					activity.getString(R.string.history_email_description),
					!Util.isEmptyString(selectionName) ? selectionName : "me", doseHistoryDays));
		}
	}
}
