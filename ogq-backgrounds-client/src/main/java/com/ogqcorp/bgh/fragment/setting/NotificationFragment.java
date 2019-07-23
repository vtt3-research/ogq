package com.ogqcorp.bgh.fragment.setting;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;
import com.ogqcorp.bgh.spirit.auth.UserManager;

public final class NotificationFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public NotificationFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onResume()
	{
		getActivity().setTitle(R.string.p_category_notification);
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
		boolean isLogin = !UserManager.getInstance().isGuest();

		return isLogin ? R.xml.preferences_notification_login : R.xml.preferences_notification;
	}

	@Override
	protected void onPostPreferences()
	{
		//Nothing
	}

	//========================================================================
	// Constructor
	//========================================================================	

	public static NotificationFragment newInstance()
	{
		return new NotificationFragment();
	}
}
