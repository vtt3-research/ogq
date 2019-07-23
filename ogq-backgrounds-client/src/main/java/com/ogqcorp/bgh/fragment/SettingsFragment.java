package com.ogqcorp.bgh.fragment;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.view.View;
import android.widget.ListView;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;
import com.ogqcorp.bgh.live.LiveWallpaperUtils;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.preference.PreferencesManagerKey;
import com.ogqcorp.bgh.system.ServerSwitcher;

public final class SettingsFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public SettingsFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		View rootView = getView();
		ListView list = rootView.findViewById(android.R.id.list);
		list.setDivider(null);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	protected int getPreferenceResource()
	{
		boolean isLogin = !UserManager.getInstance().isGuest();
		boolean isDevVersion = ServerSwitcher.isDevVersion(getContext());

		if (isDevVersion)
			return isLogin ? R.xml.preferences_login_dev : R.xml.preferences_dev;
		else
			return isLogin ? R.xml.preferences_login : R.xml.preferences;
	}

	@Override
	protected void onPostPreferences()
	{
		updateHwPreferenceFace();
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void updateHwPreferenceFace()
	{
		try
		{
			if (LiveWallpaperUtils.isActivatedOGQLiveWallpaperCanvas(getContext()))
			{
				CheckBoxPreference hwAccPreference = (CheckBoxPreference) getPreferenceManager().findPreference(PreferencesManagerKey.KEY_USE_HW_ACCELERATION);
				hwAccPreference.setEnabled(true);
				hwAccPreference.setChecked(false);
			} else if (LiveWallpaperUtils.isActivatedOGQLiveWallpaperGL(getContext()))
			{
				CheckBoxPreference hwAccPreference = (CheckBoxPreference) getPreferenceManager().findPreference(PreferencesManagerKey.KEY_USE_HW_ACCELERATION);
				hwAccPreference.setEnabled(true);
				hwAccPreference.setChecked(true);
			} else
			{
				Preference hwAccPreference = getPreferenceManager().findPreference(PreferencesManagerKey.KEY_USE_HW_ACCELERATION);
				hwAccPreference.setEnabled(false);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	//========================================================================
	// Public Methods
	//========================================================================

	@SuppressWarnings("deprecation")
	public static SettingsFragment newInstance()
	{
		return new SettingsFragment();
	}
}
