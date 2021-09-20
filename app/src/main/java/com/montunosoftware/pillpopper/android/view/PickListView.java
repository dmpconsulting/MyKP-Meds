package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PickListView extends LinearLayout
{
	public static final int ALIGN_CENTER = Gravity.CENTER;
	public static final int ALIGN_LEFT = Gravity.LEFT | Gravity.CENTER_VERTICAL;
	public static final int ALIGN_RIGHT = Gravity.RIGHT | Gravity.CENTER_VERTICAL;

	public enum PickListMode {
		RadioButton,
		CheckBox,
		Menu
	}

	public interface onItemSelectedListener {
		void onItemSelected(MenuItem menuItem);
	}
	
	public static class MenuItem
	{
		private CharSequence _displayItem;
		private Object _callbackData;
		private boolean _isSelected = false;
		private ImageView _tickImageView;
		private int _index;
		
		public MenuItem(CharSequence displayItem, Object callbackData)
		{
			this._displayItem = displayItem;
			this._callbackData = callbackData;
		}
		
		public CharSequence getDisplayItem()
		{
			return _displayItem;
		}
		
		public Object getCallbackData()
		{
			return _callbackData;
		}
		
		public void setSelected(boolean isSelected)
		{
			_isSelected = isSelected;
			updateTickVisibility();
		}
		
		public boolean isSelected()
		{
			return _isSelected;
		}
		
		public void setTickImageView(ImageView v)
		{
			_tickImageView = v;
		}
		
		public ImageView getTickImageView()
		{
			return _tickImageView;
		}
		
		public void updateTickVisibility()
		{
			if (_tickImageView != null) {
				_tickImageView.setVisibility(_isSelected ? View.VISIBLE : View.INVISIBLE);
			}
		}
		
		public void setIndex(int index)
		{
			_index = index;
		}
		
		public int getIndex()
		{
			return _index;
		}
	}
	
	///////////////////////////////////////
	
	private onItemSelectedListener _listener;

	private List<MenuItem> _menuItems;
	private PickListMode _mode = PickListMode.Menu;
	private int _alignment = ALIGN_CENTER;
	
	private LayoutInflater _inflater;


	public PickListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}

	public PickListView(Context context)
	{
		super(context);
		init(context, null);
	}

	public void init(Context context, AttributeSet attrs)
	{
		this.setOrientation(LinearLayout.VERTICAL);
		_inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PickListView);
			CharSequence label = a.getString(R.styleable.PickListView_menuLabel);
			if (label != null) {
				setData(Arrays.asList(new MenuItem(label, label)));
			}
			a.recycle();
		}

	}

	public void setListener(onItemSelectedListener listener)
	{
		this._listener = listener;
	}

	public void setAlignment(int alignment)
	{
		_alignment = alignment;
	}
	
	public void setMode(PickListMode mode)
	{
		_mode = mode;
	}

	// Convert a simple string list (or array) to a list of MenuItems, where the callback data
	// is equal to the string
	public static List<MenuItem> menuItemsFromStringList(List<? extends CharSequence> stringItems)
	{
		List<MenuItem> menuItems = new ArrayList<>();

		for (CharSequence stringItem: stringItems) {
			menuItems.add(new MenuItem(stringItem, stringItem));
		}

		return menuItems;
	}

	public void setData(List<MenuItem> menuItems)
	{
		this._menuItems = menuItems;

		this.removeAllViews();
		this.clearAllSelections();
		
		// stop here if no items -- also avoids crash if list is empty since lastView will be null
		if (menuItems.isEmpty()) {
			return;
		}
		
		View lastView = null;
		
		int i = 0;
		for (final MenuItem item: menuItems) {
			item.setIndex(i++);
			View newItemView = _inflater.inflate(R.layout.picklist_item, null);
			newItemView.findViewById(R.id.picklist_content).setOnClickListener(view -> {
                _setSelection(item);

                if (_listener != null) {
                    _listener.onItemSelected(item);
                }
            });
			TextView tView = newItemView.findViewById(R.id.picklist_text);
			tView.setGravity(_alignment);
			tView.setText(item.getDisplayItem());

			ImageView tickImageView = newItemView.findViewById(R.id.picklist_tick);
			item.setTickImageView(tickImageView);
			
			// the default is a menu-right-arrow; if we're in checkbox or radiobutton mode, 
			// switch it to a tick mark.
			if (_mode == PickListMode.CheckBox || _mode == PickListMode.RadioButton) {
				tickImageView.setImageResource(R.drawable.tick);
				item.updateTickVisibility();
			}

			this.addView(newItemView);
			lastView = newItemView;
		}
		
		// don't put a dividing line between the last element and the end
		if(null != lastView) {
			View sep = lastView.findViewById(R.id.line_view);
			sep.setVisibility(View.GONE);
		}
	}


	private void _setSelection(MenuItem selectedItem)
	{
		switch (_mode) {
		case RadioButton:
			// In radio button mode, clear all selections but the one now selected
			for (MenuItem m: _menuItems) {
				if (selectedItem == m) {
					m.setSelected(true);
				} else {
					m.setSelected(false);
				}
			}
			break;
			
		case CheckBox:
			// In checkbox mode, toggle the selection
			selectedItem.setSelected(!selectedItem.isSelected());
			break;
			
		case Menu:
			// menu mode we'll be doing nothing but calling the callback
			break;
		}
	}

	public void clearAllSelections()
	{
		for (MenuItem m: _menuItems) {
			m.setSelected(false);
		}
	}
	
	public void setSelectionByCallbackData(Object callbackData)
	{
		if (callbackData == null) {
			clearAllSelections();
		} else {
			for (MenuItem m: _menuItems) {
				if (callbackData.equals(m.getCallbackData())) {
					_setSelection(m);
					return;
				}
			}
		}
	}
}
