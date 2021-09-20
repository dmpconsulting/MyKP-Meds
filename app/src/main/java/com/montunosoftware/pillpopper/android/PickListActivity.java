package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.ArgumentPasser;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.PickListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PickListActivity extends PillpopperActivity
{
	public interface PickListValidator
	{
		String validate(List<Object> selectionList);
	}
	
	public static class Builder
	{
		private Intent _i;
		private PillpopperActivity _act;
		private int _resultCode;
		ArgumentPasser _argPasser;
		
		public Builder(PillpopperActivity act, List<PickListView.MenuItem> menuItems, int resultCode)
		{
			_act = act;
			_i = new Intent(act.getApplicationContext(), PickListActivity.class);
			_resultCode = resultCode;
			
			try {
				_argPasser = act.get_globalAppContext().putArguments(act, _i);
				_argPasser.putArg(_KEY_MENU_ITEMS, menuItems);
			} catch (ArgumentPasser.ArgumentPassException e) {
				PillpopperLog.say("ArgumentPassException", e);
			}
		}
		
		public Builder setTitle(String title)
		{
			_i.putExtra(_KEY_TITLE, title);
			return this;
		}

		public Builder setHelpTextId(int helpTextId)
		{
			_i.putExtra(_KEY_HELP_TEXT, helpTextId);
			return this;
		}
		
		public Builder allowClear()
		{
			_i.putExtra(_KEY_ALLOW_CLEAR, true);
			return this;
		}
		
		public Builder setInitialSelection(Object initialSelection)
		{
			List<Object> initialSelectionList = new ArrayList<>();
			initialSelectionList.add(initialSelection);
			return setInitialSelectionList(initialSelectionList);
		}

		public Builder setInitialSelectionList(Collection<?> initialSelectionList)
		{
			try {
				_argPasser.putArg(_KEY_INITIAL_SELECTION_LIST, initialSelectionList);
			} catch (ArgumentPasser.ArgumentPassException e) {
				PillpopperLog.say("PickListActivity::setInitialSelectionList:", e);
			}
			return this;
		}

		public Builder allowMultipleSelections()
		{
			_i.putExtra(_KEY_ALLOW_MULTIPLE_SELECTIONS, true);
			return this;
		}
		
		public Builder setValidator(PickListValidator validator)
		{
			try {
				_argPasser.putArg(_KEY_VALIDATOR, validator);
			} catch (ArgumentPasser.ArgumentPassException e) {
				PillpopperLog.say("PickListActivity::setValidator:", e);
			}
			
			return this;
		}
		
		
		public void start()
		{
			_act.startActivityForResult(_i, _resultCode);
		}
	}
	
	public static List<Object> getReturnCallbackDataList(PillpopperActivity act, Intent resultIntent)
	{
		try {
			return act.get_globalAppContext().getAndKillArguments(act, resultIntent).getListObject(_KEY_RETURNED_SELECTION_LIST);
		} catch (ArgumentPasser.ArgumentPassException e) {
			PillpopperLog.say("ArgumentPassException", e);
			return null;
		}
	}
	
	public static Object getReturnCallbackData(PillpopperActivity act, Intent resultIntent)
	{
		List<Object> selectionList = getReturnCallbackDataList(act, resultIntent);
		
		if (selectionList == null || selectionList.isEmpty()) {
			return null;
		} else {
			return selectionList.get(0);
		}
	}

	public static String getReturnString(PillpopperActivity act, Intent resultIntent)
	{
		return Util.cleanString((String) getReturnCallbackData(act, resultIntent));
	}


	////////////////////////////////////////////////////////////////////////////////////
	
	private static final String _KEY_TITLE = "KEY_TITLE";
	private static final String _KEY_HELP_TEXT = "KEY_HELP_TEXT";
	private static final String _KEY_ALLOW_CLEAR = "KEY_ALLOW_CLEAR";
	private static final String _KEY_MENU_ITEMS = "KEY_MENU_ITEMS";
	private static final String _KEY_INITIAL_SELECTION_LIST = "KEY_INITIAL_SELECTION_LIST";
	private static final String _KEY_ALLOW_MULTIPLE_SELECTIONS = "KEY_ALLOW_MULTIPLE_SELECTIONS";
	private static final String _KEY_VALIDATOR = "KEY_VALIDATOR";
	
	private static final String _KEY_SAVED_SELECTION_LIST = "_KEY_SAVED_SELECTION_LIST";
	private static final String _KEY_RETURNED_SELECTION_LIST = "_KEY_RETURNED_SELECTION_LIST";

	private List<PickListView.MenuItem> _menuItems;

	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		this.setContentView(R.layout.picklist_dialog);

		Collection<Object> initialSelectionList;
		final PickListValidator validator;
		
		// Get the arguments pass to us, or end the activity if they're missing
		try {
			ArgumentPasser argPasser = get_globalAppContext().getArguments(this, getIntent());
			_menuItems = argPasser.getListPickListViewMenuItem(_KEY_MENU_ITEMS);
			validator = argPasser.getOptionalArg(_KEY_VALIDATOR, PickListValidator.class);
			initialSelectionList = argPasser.getOptionalCollectionObject(_KEY_INITIAL_SELECTION_LIST);
		} catch (ArgumentPasser.ArgumentPassException e) {
			PillpopperLog.say("ArgumentPassException", e);
			finish();
			return;
		}
		
		// if we're returning from saved state, get the saved selection list
		if (bundle != null) {
			try {
				ArgumentPasser argPasser = get_globalAppContext().getAndKillArguments(this, bundle);
				initialSelectionList = argPasser.getOptionalCollectionObject(_KEY_SAVED_SELECTION_LIST);
			} catch (ArgumentPasser.ArgumentPassException e) {
				PillpopperLog.say("ArgumentPassException", e);
			}
		}

		// Set the title of the dialog box
		TextView title = this.findViewById(R.id.listmenu_title);
		title.setText(this.getIntent().getStringExtra(_KEY_TITLE));
		
		// Set the help text
		TextView helpText = this.findViewById(R.id.listmenu_help);
		int helpTextId = this.getIntent().getIntExtra(_KEY_HELP_TEXT, R.string.__blank);
		if (helpTextId == R.string.__blank) {
			helpText.setVisibility(View.GONE);
		} else {
			helpText.setVisibility(View.VISIBLE);
			helpText.setText(helpTextId);
		}
		
		final PickListView menu = this.findViewById(R.id.listmenu_content);
		menu.setAlignment(PickListView.ALIGN_LEFT);

		// checkbox vs radio button mode 
		if (this.getIntent().getBooleanExtra(_KEY_ALLOW_MULTIPLE_SELECTIONS, false)) {
			menu.setMode(PickListView.PickListMode.CheckBox);
		} else {
			menu.setMode(PickListView.PickListMode.RadioButton);
		}

		// pass in the menu itself
		menu.setData(_menuItems);

		// If there was an initial selection, show it
		if (initialSelectionList != null) {
			for (Object selection: initialSelectionList) {
				PillpopperLog.say("picklistview: setting object with callback data %s", selection);
				menu.setSelectionByCallbackData(selection);
			}
		}
			
		// Done button
		setSaveButtons(R.id.up_button, R.id.done_button, v -> {
			Intent resultIntent = new Intent();

			List<Object> returnedSelections = new ArrayList<>();

			// Create a list of all the objects selected.
			for (PickListView.MenuItem m: _menuItems) {
				if (m.isSelected())
					returnedSelections.add(m.getCallbackData());
			}

			// If there's a validator, call it
			if (validator != null) {
				String s = validator.validate(returnedSelections);

				if (s != null) {
					DialogHelpers.showAlertDialog(_thisActivity, s);
					return;
				}
			}

			try {
				get_globalAppContext().putArguments(_thisActivity, resultIntent)
						.putArg(_KEY_RETURNED_SELECTION_LIST, returnedSelections);
				_thisActivity.setResult(RESULT_OK, resultIntent);
			} catch (ArgumentPasser.ArgumentPassException e) {
				PillpopperLog.say("ArgumentPassException", e);
				_thisActivity.setResult(RESULT_OK, resultIntent);
			}
			_thisActivity.finish();
		});

		// Clear button
		Button b;
		b = this.findViewById(R.id.listmenu_clear_button);
		if (this.getIntent().getBooleanExtra(_KEY_ALLOW_CLEAR, false)) {
			b.setOnClickListener(v -> menu.clearAllSelections());
		} else {
			b.setVisibility(View.GONE);
		}
	}
	
	@Override 
	public void onSaveInstanceState(Bundle outState) 
	{
		super.onSaveInstanceState(outState);

		List<Object> savedSelections = new ArrayList<>();
		
		// Create a list of all the objects selected.
		for (PickListView.MenuItem m: _menuItems) {
			if (m.isSelected())
				savedSelections.add(m.getCallbackData());
		}
		
		try {
			get_globalAppContext().putArguments(this, outState)
				.putArg(_KEY_SAVED_SELECTION_LIST, savedSelections);
		} catch (ArgumentPasser.ArgumentPassException e) {
			PillpopperLog.say("ArgumentPassException", e);
		}
	}

}
