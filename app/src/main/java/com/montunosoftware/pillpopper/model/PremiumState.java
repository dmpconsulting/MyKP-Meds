// TODO this advice:
// You should use
// * an obfuscator before storing any information to persistent storage. The
// * obfuscator should use a key that is specific to the device and/or user.
// * Otherwise an attacker could copy a database full of valid purchases and
// * distribute it to others.

package com.montunosoftware.pillpopper.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.montunosoftware.pillpopper.android.util.EnumMarshaller;
import com.montunosoftware.pillpopper.android.util.Pair;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* PremiumState is a singleton that lives as long as the application. */
public class PremiumState
{
	private static final String _PREMIUM_PREFS_NAME_BASE = "com.montunosoftware.dosecast.PremiumPrefs";
	private static final String _GOOGLE_PURCHASES_RESTORED = "GooglePurchasesRestored";
	private static final String _AMAZON_PURCHASES_RESTORED = "AmazonPurchasesRestored";
	// We use this key to know whether anything is recorded at all; if it's
	// false (as it might be on an app reinstall), we'll ask {Google,Amazon} Billing
	// to do a RESTORE_TRANSACTIONS for us. That'll call into our updatePurchase
	// method so we actually know which state we're in.
	private static final String _PURCHASED_STATUS_KEY = "PurchasedStatus";

	public enum SubType {
		DEMO,
		PAID_PREMIUM,
		FREE_PREMIUM,
	}

	@SuppressWarnings("unchecked")
	private static final EnumMarshaller<SubType> _subTypeMarshaller = new EnumMarshaller<>(Arrays.asList(
            new Pair<>(SubType.DEMO, "demo"),
            new Pair<>(SubType.PAID_PREMIUM, "paidPremium"),
            new Pair<>(SubType.FREE_PREMIUM, "freePremium")
    ));

	private SubType _subType = SubType.DEMO;

	private boolean _googleBillingAvailable;
	private boolean _amazonBillingAvailable;
	// The billingAvailable status is ephemeral; we attempt to set it each time
	// we enter the AccountUpgradeActivity. We keep track of it here, in program-global
	// state, because the response can flow in asynchronously.

	private SharedPreferences _sharedPreferences;
	private List<PremiumListenerIfc> _listeners;

	public PremiumState(Context context) {
		Context _context = context;
		String premiumPrefsName = _PREMIUM_PREFS_NAME_BASE + UniqueDeviceId.getHardwareId(context);
		_sharedPreferences = _context.getSharedPreferences(premiumPrefsName, Context.MODE_PRIVATE);
		_listeners = new ArrayList<>();
		_readSharedPreferences();
	}

	private void _readSharedPreferences()
	{
		_subType = _subTypeMarshaller.fromString(_sharedPreferences.getString(_PURCHASED_STATUS_KEY, null), SubType.DEMO);
		PillpopperLog.say("Read SharedPreferences, found us %s", _subType.toString());
		// This message appears twice: first, it appears when we start up to ask
		// what state we're in. Then, it appears after a Google Billing Restore
		// Transactions, which occurs (asynchronously) at startup and rewrites
		// the "paid" flag out (and re-reads it back in).
	}


	public synchronized void setState(SubType subType)
	{
		PillpopperLog.say("Setting premiumState to %s", subType.toString());
		SharedPreferences.Editor edit = _sharedPreferences.edit();
		edit.putString(_PURCHASED_STATUS_KEY, _subTypeMarshaller.toString(subType));
		edit.apply();

		_readSharedPreferences();

		updateListeners();
	}

	// Listener list management
	// Listeners get a void call to notify that PremiumState has changed,
	// at which point we expect they'll do something like updateView,
	// which will re-query the state of this object.
	private void updateListeners() {
		for (PremiumListenerIfc listener: _listeners) {
			listener.update();
		}
	}

	public synchronized boolean isPremium()
	{
		return (_subType==SubType.FREE_PREMIUM) || (_subType==SubType.PAID_PREMIUM);
	}

}
