package org.kp.tpmg.mykpmeds.activation;

import android.content.Context;
import android.os.PowerManager;

import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.Timer;
import java.util.TimerTask;

public class TimerDelegate{
	private static TimerDelegate timerDelegate;
	private Timer timer;
	private static TimerTask task;
	private static final int duration = AppConstants.TIMEOUT_PERIOD;
	
	public static synchronized TimerDelegate getInstance() {
		if (timerDelegate==null)
			timerDelegate = new TimerDelegate();
		return timerDelegate;
	}

	private TimerDelegate(){
		timer = new Timer();
	}
	
	public void startTimerTask(Context context) {

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = isScreenOn(pm);
		if(isScreenOn){
		resetTimerTask(false);
			task = new MyTask(context);

		timer.schedule(task, duration);
		if (RunTimeData.getInstance().getNewLockTime() == Long.MAX_VALUE) {
			RunTimeData.getInstance()
					.setOldLockTime(System.currentTimeMillis());
		} else {
			RunTimeData.getInstance().setOldLockTime(
					RunTimeData.getInstance().getNewLockTime());
		}
		RunTimeData.getInstance().setNewLockTime(System.currentTimeMillis());
		}
	}

	public void restartTimerTask(Context context) {

		LoggerUtils.info("Resetting timer : context " + context);
		PowerManager pm = null;
		boolean isScreenOn = false;
		if(null!= context){
			pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			isScreenOn = isScreenOn(pm);
		}
		if(isScreenOn){
		resetTimerTask(false);
			task = new MyTask(context);
		timer.schedule(task, duration);
		if (RunTimeData.getInstance().getNewLockTime() == Long.MAX_VALUE) {
			RunTimeData.getInstance()
					.setOldLockTime(System.currentTimeMillis());
		} else {
			RunTimeData.getInstance().setOldLockTime(
					RunTimeData.getInstance().getNewLockTime());
		}
		RunTimeData.getInstance().setNewLockTime(System.currentTimeMillis());
		}
	}

	private boolean isScreenOn(PowerManager pm) {
		return pm.isInteractive();
	}

	public void resetTimerTask(boolean nullify) {
		stopTimer(nullify);
		RunTimeData.getInstance()
		.setNewLockTime(Long.MAX_VALUE);
	}

	private void stopTimer(boolean nullify) {
		if (task != null) {
			task.cancel();
			((MyTask)task).closeContext();
		}

		task = null;

		if (nullify)
		timerDelegate=null;
	}


	static class MyTask extends TimerTask {
      private Context context;
        public  MyTask(Context context){
			this.context=context;
		}

		public void closeContext(){
			context=null;
		}
		@Override
		public void run() {
			/*if (task == null) {
				task = new MyTask();
				task.run();
			} else {*/
				task.cancel();
				task = null;
				AppConstants.setIsTappedMedication(false);
				SharedPreferenceManager manager = SharedPreferenceManager.getInstance(context,AppConstants.AUTH_CODE_PREF_NAME);
				manager.putBoolean("timeOut", true, true);
				if (RunTimeData.getInstance().isAppVisible() ) {
						new ActivationController().performSignoffAndShowAlert(context);
						RunTimeData.getInstance()
						.setOldLockTime(Long.MAX_VALUE);
				RunTimeData.getInstance()
						.setNewLockTime(Long.MAX_VALUE);
					}
					LoggerUtils.info( "Time out Occured");
					LoggerUtils.info( "Time out detected.");

				//}
		}
	}
}
