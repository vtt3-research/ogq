package com.ogqcorp.bgh.fragment.setting;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;

public final class PrivacyFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public PrivacyFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================	

	@Override
	public void onResume()
	{
		getActivity().setTitle(R.string.p_category_Privacy);
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
		return R.xml.preferences_privacy;
	}

	@Override
	protected void onPostPreferences()
	{
		// Nothing
	}

	//========================================================================
	// Constructor
	//========================================================================	

	public static PrivacyFragment newInstance()
	{
		return new PrivacyFragment();
	}
}
