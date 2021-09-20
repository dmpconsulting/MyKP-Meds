package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.ArgumentPasser;
import com.montunosoftware.pillpopper.android.util.FDADrugDatabase;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.LabelledTextView;
import com.montunosoftware.pillpopper.android.view.PickListView;

import java.util.List;

public class DrugDatabaseNameSearchActivity extends PillpopperActivity
{
	public static void selectDrugNameFromDb(Activity act, int resultCode)
	{
		Intent intent = new Intent(act.getApplicationContext(), DrugDatabaseNameSearchActivity.class);
		act.startActivityForResult(intent, resultCode);
	}
	
	public static FDADrugDatabase.DatabaseDrugVariant getSelectedDrugVariant(PillpopperActivity act, Intent resultIntent)
	{
		try {
			return act.get_globalAppContext().getAndKillArguments(act, resultIntent).getArg(_KEY_SELECTED_DRUG_VARIANT, FDADrugDatabase.DatabaseDrugVariant.class);
		} catch (ArgumentPasser.ArgumentPassException e) {
			return null;
		}
	}
	
	////////////////////
	
	private static final int _MIN_SEARCH_LENGTH = 3;
	
	private static final int _REQ_VARIANT_SELECTED = 1;

	private static final String _KEY_SELECTED_DRUG_VARIANT = "KEY_SELECTED_DRUG_VARIANT";
	
	private DatabaseDrugNameAdapter _adapter;
	EditText _textEntryBox;
	TextView _messageText;
	FDADrugDatabase _fdaDrugDatabase;
	private FDADrugDatabase.SearchType _searchType = FDADrugDatabase.SearchType.DRUG_SEARCH_ALL;
	
	private class DatabaseDrugNameAdapter extends ArrayAdapter<String>
	{
		public DatabaseDrugNameAdapter()
		{
			super(_thisActivity, 0);
		}

		public View getView(int index, View view, ViewGroup viewGroup)
		{
			final LabelledTextView labelledTextView;
			
			if (view == null) {
				labelledTextView = new LabelledTextView(_thisActivity);
				labelledTextView.setNavIcon();
				labelledTextView.setOnClickListener(v -> {
                    PillpopperLog.say("DBSearch: %s selected", labelledTextView.getLabel());
                    List<PickListView.MenuItem> variantMenu = _fdaDrugDatabase.getVariantsAsMenu(labelledTextView.getLabel());

                    if (variantMenu == null) {
                        return;
                    }

                    // if there's only one variant, just return it immediately, without bothering
                    // with the variant selection screen
                    if (variantMenu.size() == 1) {
                        _return((FDADrugDatabase.DatabaseDrugVariant) variantMenu.get(0).getCallbackData());
                        return;
                    }

                    // otherwise, launch a list menu selector to select the variant
                    PillpopperLog.say("Launching variant selector for %s", labelledTextView.getLabel());

                    PickListActivity.Builder variantSelectorActivity = new PickListActivity.Builder(
                            _thisActivity,
                            variantMenu,
                            _REQ_VARIANT_SELECTED)
                    .setTitle(getString(R.string.select_variant))
                    .setHelpTextId(R.string.select_variant_help)
                    .setValidator(selectionList -> {
                        // don't let the user save if they haven't picked a variant
                        if (selectionList.isEmpty()) {
                            return _thisActivity.getString(R.string.pick_a_variant);
                        } else {
                            return null;
                        }
                    })
                    ;

                    variantSelectorActivity.start();
                });
			} else {
				labelledTextView = (LabelledTextView) view;
			}

			labelledTextView.setLabel(this.getItem(index));
			labelledTextView.setValue(null);

			return labelledTextView;
		}
	}
	
	
	private String _latestSearchString = null;
	private FDADrugDatabase.SearchType _latestSearchType = null;
	private int _lastSearchRequested = 0;
	private int _lastSearchDisplayed = 0;
	private boolean _searchRunning = false;

	final class DBSearchTask extends AsyncTask<Void, Void, FDADrugDatabase.DrugNameSearchResults>
	{
		private int _searchSerialNumber;
				
		private DBSearchTask()
		{
			super();
		}
		
		@Override
		protected FDADrugDatabase.DrugNameSearchResults doInBackground(Void... stringArgs)
		{
			String searchString;
			FDADrugDatabase.SearchType searchType;
			
			// Check and see if a search is waiting to be launched. If so, launch one.
			synchronized (_thisActivity) {
				if (_latestSearchString != null) {
					searchString = _latestSearchString;
					searchType = _latestSearchType;
					_searchSerialNumber = _lastSearchRequested;
					
					_latestSearchString = null;
					_latestSearchType = null;
				} else {
					_searchRunning = false;
					return null;
				}
			}

			PillpopperLog.say("searching for %s", searchString);
			FDADrugDatabase.DrugNameSearchResults retval = _fdaDrugDatabase.searchForDrugs(searchString, searchType);
			PillpopperLog.say("search for %s complete", searchString);

			// launch the next search, if there's something waiting
			//new DBSearchTask().execute();

			return retval;
		}

		@Override
		protected void onPostExecute(FDADrugDatabase.DrugNameSearchResults results)
		{
			if (results != null && _searchSerialNumber > _lastSearchDisplayed) {
				_lastSearchDisplayed = _searchSerialNumber;
				_adapter.clear();
				
				for (String result: results.getResults()) {
					_adapter.add(result);
				}

				_adapter.notifyDataSetChanged();
				_updateView();
			}
		}
	}

	
	private void _launchSearch()
	{
		_lastSearchRequested++;
		
		String text = _textEntryBox.getText().toString();
		
		if (text.length() >= _MIN_SEARCH_LENGTH) {
			// If the search string is long enough, launch a search
			_latestSearchString = text;
			_latestSearchType = _searchType;
			
			synchronized (_thisActivity) {
				if (_searchRunning == false) {
					_searchRunning = true;
					new DBSearchTask().execute();
				}
			}
		} else {
			// otherwise, clear the display
			_lastSearchDisplayed = _lastSearchRequested;
			_adapter.clear();
			_adapter.notifyDataSetChanged();
		}

		_updateView();
	}

	private void _updateView()
	{
		int message;
		
		if (_lastSearchDisplayed < _lastSearchRequested)
			message = R.string._searching;
		else if (_textEntryBox.getText().length() < _MIN_SEARCH_LENGTH)
			message = R.string.enter_more_search_text;
		else if (_adapter.getCount() == 0)
			message = R.string.no_drugs_found;
		else
			message = -1;

		if (message == -1) {
			_messageText.setVisibility(View.GONE);
		} else {
			_messageText.setVisibility(View.VISIBLE);
			_messageText.setText(message);
		}
	
	}
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);

		this.setContentView(R.layout.drug_database_name_search_dialog);

		// Get a handle to the drug database
		_fdaDrugDatabase = _thisActivity.get_globalAppContext().getFDADrugDatabase();
		// Hook up text entry box
		_textEntryBox = this.findViewById(R.id.drug_database_name_search_text_entry_field);
		Util.activateSoftKeyboard(_textEntryBox);
		_textEntryBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence textboxValue, int start, int before, int count)
			{
				_launchSearch();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		// Message box
		_messageText = this.findViewById(R.id.drug_database_name_search_message);
		
		// List View
		_adapter = new DatabaseDrugNameAdapter();		
		((ListView) this.findViewById(R.id.drug_database_name_search_listview)).setAdapter(_adapter);
		
		// OTC/RX selector buttons
		((RadioButton) this.findViewById(R.id.drug_database_name_search_all_button)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                _searchType = FDADrugDatabase.SearchType.DRUG_SEARCH_ALL;
                _launchSearch();
            }
        });

		((RadioButton) this.findViewById(R.id.drug_database_name_search_prescription_button)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                _searchType = FDADrugDatabase.SearchType.DRUG_SEARCH_RX;
                _launchSearch();
            }
        });
		
		((RadioButton) this.findViewById(R.id.drug_database_name_search_otc_button)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                _searchType = FDADrugDatabase.SearchType.DRUG_SEARCH_OTC;
                _launchSearch();
            }
        });


		// Cancel button
		setCancelButton(R.id.up_button);
		
		_updateView();
	}


	private void _return(FDADrugDatabase.DatabaseDrugVariant variantSelected)
	{
		PillpopperLog.say("Variant selected: %s", variantSelected.toString());
		Intent resultIntent = new Intent();
		
		try {
			get_globalAppContext().putArguments(_thisActivity, resultIntent)
					.putArg(_KEY_SELECTED_DRUG_VARIANT, variantSelected);
			_thisActivity.setResult(RESULT_OK, resultIntent);
		} catch (ArgumentPasser.ArgumentPassException e) {
			_thisActivity.setResult(RESULT_OK, resultIntent);
		}
		_thisActivity.finish();
	}
	

	@Override
	// Called when a sub-activity returns
	protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent)
	{
		super.onActivityResult(requestCode, resultCode, resultIntent);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case _REQ_VARIANT_SELECTED:
				FDADrugDatabase.DatabaseDrugVariant variantSelected = (FDADrugDatabase.DatabaseDrugVariant) PickListActivity.getReturnCallbackData(_thisActivity, resultIntent);

				// if no selection was made, go back to the search screen
				if (variantSelected == null)
					return;
				
				_return(variantSelected);
				break;
			}
		}
	}

}
