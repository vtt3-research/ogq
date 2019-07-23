package com.ogqcorp.bgh.fragment.base;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.XmlRes;
import androidx.fragment.app.FragmentManager;
import com.ogqcorp.commons.preference.PreferenceFragment;
import com.ogqcorp.commons.utils.FragmentUtils;

public abstract class BasePreferenceFragment extends PreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public BasePreferenceFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onCreate(Bundle paramBundle)
	{
		super.onCreate(paramBundle);

		getFragmentManager().addOnBackStackChangedListener(m_backStackChangedListener);

		new Handler().post(m_runnable);
	}

	@Override
	public void onDestroy()
	{
		getFragmentManager().removeOnBackStackChangedListener(m_backStackChangedListener);

		super.onDestroy();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getListView().invalidateViews();
	}

	//========================================================================
	// Private Methods
	//========================================================================
	private Runnable m_runnable = new Runnable()
	{
		@Override
		public void run()
		{
			if (FragmentUtils.isDestroyed(BasePreferenceFragment.this) == false)
			{
				addPreferencesFromResource(getPreferenceResource());
				onPostPreferences();
			}
		}
	};

	private FragmentManager.OnBackStackChangedListener m_backStackChangedListener = new FragmentManager.OnBackStackChangedListener()
	{
		@Override
		public void onBackStackChanged()
		{
			if (getView() != null)
			{
				getListView().invalidateViews();
			}
		}
	};

	//========================================================================
	// Protected Methods
	//========================================================================

	protected abstract @XmlRes int getPreferenceResource();

	protected abstract void onPostPreferences();

	//========================================================================
	// Callback
	//========================================================================

	public interface Callback
	{
		void showPreferenceFragment(PreferenceFragment fragment);
	}
}
