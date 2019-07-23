package com.ogqcorp.bgh.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;

public final class ShuffleFragment extends BackgroundsFragment
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public ShuffleFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onInitActionBar()
	{
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		AnalyticsManager.getInstance().eventExploreShuffle(getContext());
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		try
		{
			if(isVisibleToUser)
			{
				String screenName = getClass().getSimpleName();

				Context context = getContext();
				if(context == null)
					context = Application.getCurrentContext();

				AnalyticsManager.getInstance().screen(context, screenName);
			}
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected String getExtra(Background background)
	{
		return background.getPrettyDownloadsCount();
	}

	@Override
	protected String getDataUrl()
	{
		return UrlFactory.shuffle();
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		final Fragment fragment = new ShuffleFragment();
		return BaseModel.wrap(fragment);
	}
}
