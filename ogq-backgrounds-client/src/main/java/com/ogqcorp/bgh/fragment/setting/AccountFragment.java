package com.ogqcorp.bgh.fragment.setting;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;

public final class AccountFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================	

	@Deprecated
	public AccountFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================	

	@Override
	public void onResume()
	{
		getActivity().setTitle(R.string.p_category_account);
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
		return R.xml.preferences_account;
	}

	@Override
	protected void onPostPreferences()
	{
		// Nothing
	}

	//========================================================================
	// Constructor
	//========================================================================	

	public static AccountFragment newInstance()
	{
		return new AccountFragment();
	}
}
