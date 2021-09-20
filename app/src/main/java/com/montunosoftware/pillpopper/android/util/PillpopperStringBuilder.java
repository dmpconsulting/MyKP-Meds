package com.montunosoftware.pillpopper.android.util;

import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.model.State;

public class PillpopperStringBuilder
{
	private StringBuilder _sb = new StringBuilder();
	private PillpopperAppContext _globalAppContext;
	private Context _context;
	private State _state;
	
	public PillpopperStringBuilder(Context context,PillpopperAppContext globalAppContext)
	{
		_globalAppContext = globalAppContext;
		_context = context;
		_state = globalAppContext.getState(context);
	}
	
	public State getState()
	{
		return _state;
	}

	public Context getContext()
	{
		return _context;
	}

	public PillpopperAppContext getGlobalAppContext()
	{
		return _globalAppContext;
	}

	public boolean isPremium()
	{
		return _globalAppContext.isPremium();
	}
	
	public void append(int stringId)
	{
		_sb.append(_context.getString(stringId));
	}

	public void append(String string)
	{
		_sb.append(string);
	}


	public void append(int stringId, String v)
	{
		_sb.append(_context.getString(stringId));
		_sb.append(": ");
		_sb.append(elegantNull(v));
		_sb.append("\n");
	}

	public void appendColumn(int stringId)
	{
		_sb.append(String.format(
				_context.getString(R.string.email_html_drug_table_column),
				_context.getString(stringId))
		);
	}
	public void appendColumn(String s)
	{
		_sb.append(String.format(_context.getString(R.string.email_html_drug_table_column), elegantNull(s)));
	}

	@ Override
	public String toString()
	{
		return _sb.toString();
	}

	public String elegantNull(String possiblyNullString)
	{
		if (possiblyNullString==null) {
			return _context.getString(R.string.__blank);
		} else {
			return possiblyNullString;
		}
	}

}
