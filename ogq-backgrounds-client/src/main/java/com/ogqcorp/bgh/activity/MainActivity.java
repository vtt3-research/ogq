package com.ogqcorp.bgh.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BaseFragment;
import com.ogqcorp.bgh.fragment.explore.ExploreFragment;
import com.ogqcorp.bgh.fragment.explore.MainFragment;
import com.ogqcorp.bgh.system.FragmentFactory;
import com.ogqcorp.bgh.user.MyInfoFragment;
import com.ogqcorp.commons.utils.ToastUtils;

public final class MainActivity extends SchemeActivity implements BaseFragment.Callback
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onOpenBackground(Fragment sourceFragment)
	{
		try
		{
			final Fragment fragment = FragmentFactory.createBackgroundFragment(sourceFragment);
			getTabStackHelper().showFragment(fragment);
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		}
	}

	@Override
	public void onOpenBackgrounds(String dataUrl)
	{
		try
		{
			final Fragment fragment = FragmentFactory.createBackgroundsFragment(dataUrl);
			getTabStackHelper().showFragment(fragment);
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		}
	}

	@Override
	protected void onExplorerWithTabNumber(final int tabNumber)
	{
		final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.tab_content);

		final Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.tab_content);

					for (Fragment f : mainFragment.getChildFragmentManager().getFragments())
					{
						if (f instanceof ExploreFragment)
						{
							((ExploreFragment) f).setCurrentItem(tabNumber, true);
							return;
						}
					}
				}
				catch (Exception e)
				{

				}
			}
		};

		if ((fragment instanceof ExploreFragment) == false)
		{
			onClickDrawerExplore();
		}

		new Handler().postDelayed(runnable, 500);
	}

	@Override
	protected void onProfileWithTabNumber(final int tabNumber)
	{
		final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.tab_content);

		final Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					MainFragment mainFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.tab_content);

					for (Fragment f : mainFragment.getChildFragmentManager().getFragments())
					{
						if (f instanceof MyInfoFragment)
						{
							((MyInfoFragment) f).setCurrentTab(tabNumber);
							return;
						}
					}
				}
				catch (Exception e)
				{
				}
			}
		};

		if ((fragment instanceof MyInfoFragment) == false)
		{
			onClickDrawerExplore();
			onClickAvatar();
		}
		new Handler().postDelayed(runnable, 500);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context)
	{
		return new Intent(context.getApplicationContext(), MainActivity.class);
	}
}