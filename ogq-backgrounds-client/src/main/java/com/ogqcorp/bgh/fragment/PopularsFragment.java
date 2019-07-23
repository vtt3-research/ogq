package com.ogqcorp.bgh.fragment;

import rx.functions.Action1;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.fragment.explore.ExploreFragment;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.RxBus;

public final class PopularsFragment extends BackgroundsFragment
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public PopularsFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null)
		{
			m_popularMode = savedInstanceState.getInt(KEY_POPULAR_MODE, POPULAR_MODE_DAILY);
		}
		else if (getArguments() != null)
		{
			m_popularMode = getArguments().getInt(KEY_POPULAR_MODE);
		}

		m_isPopularMode = true;
		setPopularModeAnalytics();

		RxBus.getInstance().registerListener(ExploreFragment.BusPopularMode.class, new Action1<ExploreFragment.BusPopularMode>()
		{
			@Override
			public void call(ExploreFragment.BusPopularMode popularMode)
			{
				int mode = popularMode.getPopularMode();

				if (mode != m_popularMode)
				{
					m_popularMode = mode;
					clearData();
					onRefresh();
					setPopularModeAnalytics();
				}
			}
		});
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		RxBus.getInstance().unregisterListener(ExploreFragment.BusPopularMode.class);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		try
		{
			if (isVisibleToUser)
			{
				String screenName = getClass().getSimpleName();

				Context context = getContext();
				if (context == null)
					context = Application.getCurrentContext();

				AnalyticsManager.getInstance().screen(context, screenName);

				setPopularModeAnalytics();
			}
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected void onInitActionBar()
	{
		// Nothing
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected String getDataUrl()
	{
		switch (m_popularMode)
		{
			case POPULAR_MODE_ALL:
				return UrlFactory.popularAll();

			case POPULAR_MODE_MONTHLY:
				return UrlFactory.popularMonthly();

			case POPULAR_MODE_WEEKLY:
				return UrlFactory.popularWeekly();

			case POPULAR_MODE_DAILY:
				return UrlFactory.popularDaily();
		}

		return UrlFactory.popularAll();
	}

	@Override
	protected String getExtra(Background background)
	{
		switch (m_popularMode)
		{
			case POPULAR_MODE_ALL:
				return background.getPrettyDownloadsCount();

			case POPULAR_MODE_MONTHLY:
				return background.getPrettyDownloadsMonthlyCount();

			case POPULAR_MODE_WEEKLY:
				return background.getPrettyDownloadsWeeklyCount();

			case POPULAR_MODE_DAILY:
				return background.getPrettyDownloadsDailyCount();
		}

		return null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_POPULAR_MODE, m_popularMode);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Fragment newInstance(int popularMode)
	{
		final Fragment fragment = new PopularsFragment();

		final Bundle args = new Bundle();
		args.putInt(KEY_POPULAR_MODE, popularMode);
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================
	private void setPopularModeAnalytics()
	{
		try
		{
			if (m_isPopularMode)
			{
				switch (m_popularMode)
				{
					case POPULAR_MODE_ALL:
						AnalyticsManager.getInstance().PopularEvent(getActivity(), "AllTime_Popular");
						break;
					case POPULAR_MODE_MONTHLY:
						AnalyticsManager.getInstance().PopularEvent(getActivity(), "Monthly_Popular ");
						break;
					case POPULAR_MODE_WEEKLY:
						AnalyticsManager.getInstance().PopularEvent(getActivity(), "Weekly_Popular");
						break;
					case POPULAR_MODE_DAILY:
						AnalyticsManager.getInstance().PopularEvent(getActivity(), "Daily_Popular");
						break;
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_POPULAR_MODE = "KEY_POPULAR_MODE";

	/* @formatter:x */
	public static final int POPULAR_MODE_DAILY = 0;
	public static final int POPULAR_MODE_WEEKLY = 1;
	public static final int POPULAR_MODE_MONTHLY = 2;
	public static final int POPULAR_MODE_ALL = 3;
	/* @formatter:o */

	//=========================================================================
	// Variables
	//=========================================================================

	private int m_popularMode = POPULAR_MODE_DAILY;
	private boolean m_isPopularMode = false;

}
