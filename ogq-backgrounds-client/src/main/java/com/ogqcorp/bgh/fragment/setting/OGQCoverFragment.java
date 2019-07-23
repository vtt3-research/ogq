package com.ogqcorp.bgh.fragment.setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.widget.ListView;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.coverslider.activity.CoverGuideActivity;
import com.ogqcorp.bgh.coverslider.system.CoverConst;
import com.ogqcorp.bgh.fragment.base.BasePreferenceFragment;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.preference.PreferencesManagerKey;

public final class OGQCoverFragment extends BasePreferenceFragment
{
	//========================================================================
	// Constructors
	//========================================================================

	@Deprecated
	public OGQCoverFragment()
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================	

	@Override
	public void onResume()
	{
		super.onResume();
		Context context = getContext();
		boolean isUseCover = PreferencesManager.getInstance().isUseCover(context);
		boolean isCoverSettingRun = PreferencesManager.getInstance().isCoverSettingRun(context);

		m_coverDownloadCompleteReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if (intent.getAction().compareTo(CoverConst.ACTION_COVER_DOWNLOAD_RESULT) == 0)
				{
					updateCoverPreference();
				}

			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CoverConst.ACTION_COVER_DOWNLOAD_RESULT);
		getContext().registerReceiver(m_coverDownloadCompleteReceiver, intentFilter);

		updateCoverPreference();

		if (isCoverSettingRun)
		{
			ListView listView = getListView();
			if (listView == null)
			{
				return;
			}
			listView.requestFocus();
			timerDelayRunForScroll(listView, LIST_AUTO_SCROLL_DELAY_TIME);

			boolean isIntroShown = PreferencesManager.getInstance().isCoverIntroShown(context);
			if (isIntroShown == false && isUseCover == false)
			{
				PreferencesManager.getInstance().setCoverIntroShown(context, true);
				startActivity(new Intent(getContext(), CoverGuideActivity.class));
			}
		}
		PreferencesManager.getInstance().setCoverSetting(getContext(), false);

		getActivity().setTitle(R.string.p_category_cover);
	}

	@Override
	public void onPause()
	{
		if (m_coverDownloadCompleteReceiver != null)
		{
			getContext().unregisterReceiver(m_coverDownloadCompleteReceiver);
		}
		super.onPause();
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
		return R.xml.preferences_ogq_cover;
	}

	@Override
	protected void onPostPreferences()
	{
		updateCoverPreference();
	}

	//========================================================================
	// Private Method
	//========================================================================

	private void updateCoverPreference()
	{
		try
		{
			CheckBoxPreference coverSetPref = (CheckBoxPreference) getPreferenceManager().findPreference(PreferencesManagerKey.KEY_USE_COVER);
			CheckBoxPreference permitDataPref = (CheckBoxPreference) getPreferenceManager().findPreference(PreferencesManagerKey.KEY_COVER_PERMIT_DATA_DOWNLOAD);
			Preference coverImgTypePref = getPreferenceManager().findPreference(PreferencesManagerKey.KEY_COVER_IMAGE_TYPE);
			int imageType = PreferencesManager.getInstance().getCoverImageType(getContext());
			int summaryStrId = 0;
			if (imageType == CoverConst.IMAGE_TYPE_LIKE)
			{
				summaryStrId = R.string.userinfo_tabs_likes;
			}
			else
			{
				summaryStrId = R.string.explore_recent;
			}
			coverImgTypePref.setSummary(summaryStrId);

			boolean isUseCover = PreferencesManager.getInstance().isUseCover(getContext());
			boolean canDownloadData = PreferencesManager.getInstance().canDownloadData(getContext());

			coverSetPref.setChecked(isUseCover);
			permitDataPref.setChecked(canDownloadData);
			permitDataPref.setEnabled(isUseCover);
			coverImgTypePref.setEnabled(isUseCover);
		}
		catch (Exception e)
		{

		}
	}

	private void timerDelayRunForScroll(final ListView listView, long time)
	{
		Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			public void run()
			{
				try
				{
					int h1 = listView.getHeight();
					final int offsetHeight = (int) (h1 / 4) * 3;

					listView.smoothScrollToPositionFromTop(COVER_PREF_POSITION, offsetHeight, LIST_AUTO_SCROLLING_TIME);
					listView.invalidateViews();
				}
				catch (Exception e)
				{

				}
			}
		}, time);
	}

	//========================================================================
	// Constructor
	//========================================================================

	public static OGQCoverFragment newInstance()
	{
		return new OGQCoverFragment();
	}

	//========================================================================
	// Constants
	//========================================================================

	private final int LIST_AUTO_SCROLL_DELAY_TIME = 400;
	private final int LIST_AUTO_SCROLLING_TIME = 100;

	//CAUTION. Cover Preference Position (19) . If Preference list position will be changed, COVER_PREF_POSITION must be changed.
	private final int COVER_PREF_POSITION = 19;

	//========================================================================
	// Variables
	//========================================================================

	private BroadcastReceiver m_coverDownloadCompleteReceiver;
}
