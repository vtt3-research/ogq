package com.ogqcorp.bgh.fragment;

import java.util.List;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.ads.AdRewardLoadListener;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.model.BackgroundsModel;
import com.ogqcorp.bgh.model.BackgroundsModelData;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.data.Backgrounds;
import com.ogqcorp.bgh.system.ParallaxPagerTransformer;

import com.ogqcorp.commons.utils.FragmentUtils;

// Back side of BackgroundPageFragment
// ViewPager with List of BackgroundPageFragment
public final class BackgroundFragment extends BaseActionBarFragment implements BackgroundPageFragment.Callback
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public BackgroundFragment()
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

		m_data = BackgroundsModel.getInstance().get(this, new BaseModel.DataCreator<BackgroundsModelData>()
		{
			@Override
			public BackgroundsModelData newInstance()
			{
				throw new IllegalStateException("The model data does not exist.");
			}
		});
		m_data.setPreAddResponseData(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_background, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_viewPager = view.findViewById(R.id.view_pager);

		final ParallaxPagerTransformer transformer = new ParallaxPagerTransformer(R.id.preview);
		m_viewPager.setPageTransformer(false, transformer);

		final PageAdapter adapter = new PageAdapter(getChildFragmentManager());
		m_viewPager.setAdapter(adapter);

		m_viewPager.addOnPageChangeListener(m_pageChangeListener);
		m_viewPager.setCurrentItem(m_data.getIndex());

		getToolbar().setTranslationY(0);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (m_data != null)
			m_data.cancelRequests();
	}

	@Override
	public void onLoadNext(final Response.Listener<Backgrounds> response, final Response.ErrorListener errorResponse)
	{
		final Response.Listener<Backgrounds> preResponse = new Response.Listener<Backgrounds>()
		{
			@Override
			public void onResponse(Backgrounds backgrounds)
			{
				m_viewPager.getAdapter().notifyDataSetChanged();

				if (response != null)
				{
					response.onResponse(backgrounds);
				}
			}
		};

		m_data.requestNext(preResponse, errorResponse);
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return true;
	}

	@Override
	public void onActionBarAlpha(int position, int alpha)
	{
		if (m_viewPager.getCurrentItem() == position)
		{
			setActionBarAlpha(alpha);
		}

		m_alphaMap.put(position, alpha > 0 ? 255 : 0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		final List<Fragment> fragmentsList = getChildFragmentManager().getFragments();
		for (Fragment fragment : fragmentsList)
		{
			if (FragmentUtils.isDestroyed(fragment) == true) continue;

			if (fragment.getUserVisibleHint() == true)
			{
				fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
			}
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(Fragment sourceFragment)
	{
		final Fragment fragment = new BackgroundFragment();

		final long dataKey = BaseModel.getDataKey(sourceFragment);
		return BaseModel.wrap(fragment, dataKey);
	}

	public void rewardAdLoad(int position)
	{
		try
		{
			final List<Fragment> fragmentsList = getChildFragmentManager().getFragments();

			if (fragmentsList != null)
			{
				for (Fragment fragment : fragmentsList)
				{
					if (fragment instanceof AdRewardLoadListener)
					{
						AdRewardLoadListener callback = (AdRewardLoadListener) fragment;
						callback.OnRewardAdLoad(position);
					}
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	// Func : Auto Video Play(ExoPlayer)
	public void exoplayerResume(int position, int state)
	{
		try
		{
			final List<Fragment> fragmentsList = getChildFragmentManager().getFragments();

			for (Fragment fragment : fragmentsList)
			{
				if (fragment instanceof BackgroundPageFragment)
				{
					BackgroundPageFragment backgroundPageFragment = (BackgroundPageFragment) fragment;

					backgroundPageFragment.CheckVisibleExoPlayer(position, state);
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Page Adapter
	//=========================================================================

	private class PageAdapter extends FragmentStatePagerAdapter
	{
		public PageAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{
			return BackgroundPageFragment.newInstance(BackgroundFragment.this, position);
		}

		@Override
		public int getCount()
		{
			int count = 0;
			boolean hasNext = false;

			if (m_data == null || m_data.getBackgroundsList() == null || m_data.getBackgroundsList().isEmpty())
			{
				count = 0;
			} else
			{
				count = m_data.getBackgroundsList().size();
			}

			if (m_data != null && m_data.hasNext())
			{
				hasNext = true;
			}

			return count + (hasNext ? 1 : 0);
		}

		@Override
		public Parcelable saveState()
		{
			Bundle bundle = (Bundle) super.saveState();

			if (bundle != null)
			{
				bundle.remove("states");
			}
			return bundle;
		}
	}

	//=========================================================================
	// Page Change Listener
	//=========================================================================

	private ViewPager.OnPageChangeListener m_pageChangeListener = new ViewPager.OnPageChangeListener()
	{
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
			final int currentItem = m_viewPager.getCurrentItem();

			final int index = Math.min(position, currentItem);

			final int sourceAlpha = m_alphaMap.get(index + 0);
			final int targetAlpha = m_alphaMap.get(index + 1);

			if (positionOffset != 0)
			{
				final int alpha = Math.round(sourceAlpha - (sourceAlpha - targetAlpha) * positionOffset);
				setActionBarAlpha(alpha);
			}
		}

		@Override
		public void onPageSelected(int position)
		{
			m_data.setIndex(position);
			rewardAdLoad(position);
			exoplayerResume(position, -1);
		}

		@Override
		public void onPageScrollStateChanged(int state)
		{
			// Func : Auto Video Play(ExoPlayer)
			final List<Fragment> fragmentsList = getChildFragmentManager().getFragments();
			for (Fragment fragment : fragmentsList)
			{
				if (fragment instanceof BackgroundPageFragment)
				{
					BackgroundPageFragment backgroundPageFragment = (BackgroundPageFragment) fragment;

					backgroundPageFragment.CheckVisibleExoPlayer(-1, state);

					if (state == ViewPager.SCROLL_STATE_DRAGGING || state == ViewPager.SCROLL_STATE_IDLE)
					{
						if (backgroundPageFragment.getUserVisibleHint() == false)
						{
							backgroundPageFragment.syncToolbarY();
						}
					}

				}
			}
			/*
			if (state == ViewPager.SCROLL_STATE_DRAGGING || state == ViewPager.SCROLL_STATE_IDLE)
			{
				final List<Fragment> fragmentsList = getChildFragmentManager().getFragments();
				for (Fragment fragment : fragmentsList)
				{
					if (fragment instanceof BackgroundPageFragment)
					{
						BackgroundPageFragment backgroundPageFragment = (BackgroundPageFragment) fragment;

						if (backgroundPageFragment.getUserVisibleHint() == false)
						{
							backgroundPageFragment.syncToolbarY();
						}
					}
				}
			}*/
		}
	};

	//=========================================================================
	// Variables
	//=========================================================================

	private ViewPager m_viewPager;
	private SparseIntArray m_alphaMap = new SparseIntArray();
	private BackgroundsModelData m_data;
}