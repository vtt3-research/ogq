package com.ogqcorp.bgh.fragment.setting;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;

public final class WidgetFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public WidgetFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================	

	@Override
	public void onResume()
	{
		getActivity().setTitle(R.string.p_category_widgets);
		super.onResume();
	}

	@Override
	public void onStop()
	{
		getActivity().setTitle(R.string.p_settings);
		super.onStop();
	}

	@Override
	protected int getPreferenceResource()
	{
		return R.xml.preferences_widget;
	}

	@Override
	protected void onPostPreferences()
	{
		// Nothing
	}

	//========================================================================
	// Constructor
	//========================================================================	

	public static WidgetFragment newInstance()
	{
		return new WidgetFragment();
	}
}
