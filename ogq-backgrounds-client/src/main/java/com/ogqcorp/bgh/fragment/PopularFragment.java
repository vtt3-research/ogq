package com.ogqcorp.bgh.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;

import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;

public final class PopularFragment extends BackgroundsFragment
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public PopularFragment()
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

		if (getArguments() != null)
		{
			m_popularMode = getArguments().getInt(KEY_POPULAR_MODE);
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

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Fragment newInstance(int popularMode)
	{
		final Fragment fragment = new PopularFragment();

		final Bundle args = new Bundle();
		args.putInt(KEY_POPULAR_MODE, popularMode);
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_POPULAR_MODE = "KEY_POPULAR_MODE";

	/* @formatter:x */
	public static final int POPULAR_MODE_DAILY   = 0;
	public static final int POPULAR_MODE_WEEKLY  = 1;
	public static final int POPULAR_MODE_MONTHLY = 2;
	public static final int POPULAR_MODE_ALL     = 3;
	/* @formatter:o */

	//=========================================================================
	// Variables
	//=========================================================================

	private int m_popularMode;
}
