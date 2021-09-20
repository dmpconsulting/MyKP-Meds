package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.model.State;

public abstract class PillpopperFragment
	extends Fragment
	implements PillpopperReplyContext
{
	protected final PillpopperFragment _thisFragment = this;
	protected View view = null;
	protected PillpopperActivity _thisActivity = null;	// not filled in until onAttach
	protected PillpopperAppContext _globalAppContext;
	protected LayoutInflater _inflater;
	
	// duck-typed interface
    protected abstract int core_layout();
	protected abstract void core_onCreate(Bundle bundle);
	protected abstract void fragment_onCreateOptionsMenu(Menu menu);	// live for Fragment ducks.
	protected void fragment_onPrepareOptionsMenu(Menu menu) {}  	// live for Fragment ducks.


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle)
    {
        View v = inflater.inflate(core_layout(), container, false);
        setView(v);
        core_onCreate(bundle);
		return v;
    }
	
	public PillpopperAppContext getGlobalAppContext()
	{
		return _globalAppContext;
	}
	
	public PillpopperActivity getPillpopperActivity()
	{
		return (PillpopperActivity) getActivity();
	}
	
	protected void setView(View v)
	{
		this.view = v;
	}
	
	protected View findViewById(int id)
	{
		// NB can't use getView(), because it's not set up yet while we're in onCreateView.
		return view.findViewById(id);
	}
	
	protected void setCancelButton(int cancelButtonId) 
	{
		View v = _thisActivity.findViewById(cancelButtonId);
		
		if (v != null) {
			v.setOnClickListener(v1 -> {
                _thisActivity.setResult(Activity.RESULT_CANCELED);
                _thisActivity.finish();
            });
		}
	}

	// This function sets up a callback to be called when either the "save" button
	// is pressed, or the hardware "back" button is pressed.
	OnClickListener _onSaveListener = null;
	protected void setSaveButton(int saveButtonId, OnClickListener onSaveListener)
	{
		_onSaveListener = onSaveListener;
		
		View v = _thisActivity.findViewById(saveButtonId);
		
		if (v != null) {
			v.setOnClickListener(onSaveListener);
		}
	}
	

	// Post a task to scroll a scrollview to the top of a view.
	protected void scrollTo(final ScrollView scrollView, final View scrollDestination)
	{
		// For some reason, scrollTo can't be run from here; it has to be run later.  maybe because
		// _updateView invalidates the layout, thus scrollto can't be run until the layout has been recomputed.
		//
		// http://stackoverflow.com/questions/3263259/scrollview-scrollto-not-working-saving-scrollview-position-on-rotation
		if (scrollView != null && scrollDestination != null) {
			scrollView.post(() -> scrollView.scrollTo(0, scrollDestination.getTop()));
		}
	}
	
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		
		PillpopperLog.say("Attaching " + this.getClass().getName() + " to "+context);
		this._thisActivity = (PillpopperActivity) context;
	}

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		
		PillpopperLog.say("Creating " + this.getClass().getName());
		_globalAppContext = PillpopperAppContext.getGlobalAppContext(_thisActivity);
		_inflater = (LayoutInflater) _thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		setHasOptionsMenu(true);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		PillpopperLog.say("Resuming " + this.getClass().getName());
	}
	

	@Override
	public void onPause()
	{
		super.onPause();
		PillpopperLog.say("Pausing " + this.getClass().getName());
	}
	
	@Override
	// TODO jonh not certain this object lifecycle sequence makes sense for fragments.
	public void onDestroy()
	{
		super.onDestroy();
		
		PillpopperLog.say("Destroying " + this.getClass().getName());
		
		// kill the arguments that were passed to us
		if (_thisActivity.isFinishing()) {
			_globalAppContext.killArguments(_thisActivity, _thisActivity.getIntent());
		}
	}

	public LayoutInflater getInflater()
	{
		return _inflater;
	}

	public State getState()
	{
		return _globalAppContext.getState(getActivity());
	}

	@Override
	public Context getAndroidContext()
	{
		return _thisActivity;
	}

	@Override
	public PillpopperAppContext getPillpopperContext()
	{
		return _globalAppContext;
	}

	@Override
	public String getDebugName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public Activity getActivityForMenu()
	{
		return _thisActivity;
	}
	
	public PillpopperReplyContext getReplyContext() {
		return this;
	}
	
	MenuInflater _menuInflater;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		_menuInflater = inflater;
		fragment_onCreateOptionsMenu(menu);
		_menuInflater = null;
	}

	protected MenuInflater get_menu_inflater()
	{
		return _menuInflater;
	}

	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		fragment_onPrepareOptionsMenu(menu);
	}

	protected Context getApplicationContext()
	{
		return getActivity().getApplicationContext();
	}
	
	protected boolean view_as_fragment()
	{
		return true;
	}

	@Override
	public Resources getAndroidResources()
	{
		// use cached activity, in case a callback needs this after fragment has been detached
		return _thisActivity.getResources();
	}

}
