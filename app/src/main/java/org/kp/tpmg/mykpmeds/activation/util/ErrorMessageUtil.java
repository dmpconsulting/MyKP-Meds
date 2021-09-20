package org.kp.tpmg.mykpmeds.activation.util;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.ActivationError;

public class ErrorMessageUtil {
	public ActivationError getErrorDetails(int statusCode) {
		ActivationError error = new ActivationError();
		error.setErrorCode(statusCode);
		if (statusCode == 0) {
			error.setErrorStatus(AppConstants.NOT_ERROR);
		} else {
			error.setErrorStatus(AppConstants.ERROR);
			switch (statusCode) {
			// TODO: Can we make these property driven ??
			case -1:
				error.setTitle("Network Error");
				error.setMessage("A network connection cannot be established. Please try again later.");
				break;
			case -2:
				error.setTitle("Error");
				error.setMessage("Unable to obtain device Id.");
				break;

			case 1:
			case 2:
				error.setTitle("Error");
				error.setMessage("Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 3:
				error.setTitle("Error");
				error.setMessage("Force Upgrade");
				break;
			case 5:
				error.setTitle("Authorization failed");
				error.setMessage("Authorization failed. Please go to kp.org to activate your account. Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 6:
				error.setTitle("Locked out");
				error.setMessage("There is a problem with your account. Please go to kp.org for details.");
				break;

			case 7:
				error.setTitle("Authorization failed");
				error.setMessage("Authorization failed. Please go to kp.org to activate your account. Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 8:
				error.setTitle("Error");
				error.setMessage("This app is available only to Kaiser Permanente members in Northern California at this time. Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 9:
				error.setTitle("Error");
				error.setMessage("It appears you've changed your kp.org user ID or password.  Your device has been deactivated and your data has been removed. Please reactivate to continue using this app.");
				break;
			case 11:
				error.setTitle("Alert");
				error.setMessage("Non-member caregiver access is not yet available for this product.");
				break;	
			case 20:
				error.setTitle("Error");
				error.setMessage("Your data cannot be retrieved at this time. Please try again later.");
				break;
			case 100:
				error.setTitle("Error");
				error.setMessage("My KP Meds is currently undergoing routine maintenance.  Please try again later.");
				break;
			case 101:
				error.setTitle("Error");
				error.setMessage("App Version Not Supported");
				break;
			case 110:
				error.setTitle("Error");
				error.setMessage("App Security Breach");
				break;
			case 125:
				error.setTitle("Session Expired");
				error.setMessage("For your security, your session has timed out due to inactivity.");
				break;
			default:
				error.setTitle("Error");
				error.setMessage("Your data cannot be retrieved at this time. Please try again later.");
			}
		}
		return error;
	}

	public ActivationError getErrorDetails(int statusCode, String message) {
		ActivationError error = new ActivationError();
		error.setErrorCode(statusCode);
		if (statusCode == 0) {
			error.setErrorStatus(AppConstants.NOT_ERROR);
		} else {
			error.setErrorStatus(AppConstants.ERROR);
			switch (statusCode) {
			// TODO: Can we make these property driven ??
			case -1:
				error.setTitle("Network Error");
				error.setMessage("A network connection cannot be established. Please try again later.");
				break;
			case -2:
				error.setTitle("Error");
				error.setMessage("Unable to obtain device Id.");
				break;

			case 1:
			case 2:
				error.setTitle("Error");
				error.setMessage("Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 3:
				error.setTitle("Error");
				if (message != null) {
					error.setMessage(message);
				} else {
					error.setMessage("Force Upgrade");
				}
				break;
			case 5:
				error.setTitle("Authorization failed");
				error.setMessage("Authorization failed. Please go to kp.org to activate your account. Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 6:
				error.setTitle("Locked out");
				error.setMessage("There is a problem with your account. Please go to kp.org for details.");
				break;

			case 7:
				error.setTitle("Authorization failed");
				error.setMessage("Authorization failed. Please go to kp.org to activate your account. Your device has been deactivated and your data has been cleared from the device.");
				break;
			case 8:
				error.setTitle("App Unavailable");
				if (message != null) {
					error.setMessage(message);
				} else {
					error.setMessage("This app is available only to Kaiser Permanente members in Northern California at this time. Your device has been deactivated and your data has been cleared from the device.");
				}
				break;
			case 9:
				error.setTitle("Error");
				if (message != null) {
					error.setMessage(message);
				} else {
					error.setMessage("It appears you've changed your kp.org user ID or password.  Your device has been deactivated and your data has been removed. Please reactivate to continue using this app.");
				}
				break;
				
			case 11:
				error.setTitle("Alert!");
				error.setMessage("Non-member caregiver access is not yet available for this product.");
				break;	
			case 20:
				error.setTitle("Error");
				error.setMessage("Unable to connect to server, try again later.");
				break;
			case 100:
				error.setTitle("Error");
				error.setMessage("My KP Meds is currently undergoing routine maintenance.  Please try again later.");
				break;
			case 101:
				error.setTitle("Error");
				if (message != null) {
					error.setMessage(message);
				} else {
					error.setMessage("App Version Not Supported");
				}
				break;
			case 110:
				error.setTitle("Error");
				if (message != null) {
					error.setMessage(message);
				} else {
					error.setMessage("App Security Breach");
				}
				break;
			case 125:
				error.setTitle("Session Expired");
				error.setMessage("For your security, your session has timed out due to inactivity.");
				break;
			default:
				error.setTitle("Error");
				error.setMessage("Your data cannot be retrieved at this time. Please try again later.");
			}
		}
		return error;
	}
}
