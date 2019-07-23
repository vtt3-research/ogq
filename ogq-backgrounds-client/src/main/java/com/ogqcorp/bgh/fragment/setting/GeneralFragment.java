package com.ogqcorp.bgh.fragment.setting;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;

public final class GeneralFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public GeneralFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================	

	@Override
	public void onResume()
	{
		getActivity().setTitle(R.string.p_category_app);
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
		return R.xml.preferences_general;
	}

	@Override
	protected void onPostPreferences()
	{
		// Nothing
	}

	//========================================================================
	// Constructor
	//========================================================================	

	public static GeneralFragment newInstance()
	{
		return new GeneralFragment();
	}
}
