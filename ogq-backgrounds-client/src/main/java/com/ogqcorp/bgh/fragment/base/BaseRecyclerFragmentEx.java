package com.ogqcorp.bgh.fragment.base;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.explore.BaseRecyclerFragmentEx2;
import com.ogqcorp.bgh.fragment.explore.ExploreFragment;
import com.ogqcorp.bgh.view.SwipeRefreshLayoutEx;

import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

public abstract class BaseRecyclerFragmentEx extends BaseRecyclerFragmentAli
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		if (isOverlayActionBar() == true)
		{
			return;
		}

		m_toolbar = getToolbar();

		final SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
		SwipeRefreshLayoutEx.setCircleViewPadding(swipeRefreshLayout, getActionBarHeight());

		m_listView.setClipToPadding(false);
		m_listView.setPadding(0, getActionBarHeight(), 0, 0);

		registerOnScrollListener(m_actionBarScrollListener);

		if (getParentFragment() instanceof ExploreFragment)
		{
			final int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);

			SwipeRefreshLayoutEx.setCircleViewPadding(swipeRefreshLayout, getActionBarHeight() + tabHeight);
			m_listView.setPadding(0, getActionBarHeight() + tabHeight, 0, 0);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		if (getParentFragment() instanceof BaseRecyclerFragmentEx2 == false)
		{
			showActionBarSlide(true, false);
		}
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void showActionBarSlide(boolean enabled, boolean animation)
	{
		if (getParentFragment() instanceof BaseRecyclerFragmentEx2)
		{
			final BaseRecyclerFragmentEx2 exploreFragment = (BaseRecyclerFragmentEx2) getParentFragment();
			exploreFragment.showActionBarSlide(enabled, animation);
		}
		else
		{

			if (m_toolbar != null)
			{
				cancelActionBarAnimation();
				final int targetPosition = (enabled == true) ? 0 : -getActionBarHeight();

				if (animation == true)
				{
					m_toolbar.animate()
							.setInterpolator(enabled == true ? ACCELERATE_INTERPOLATOR : DECELERATE_INTERPOLATOR)
							.translationY(targetPosition)
							.start();
				}
				else
				{
					m_toolbar.setTranslationY(targetPosition);
				}
			}
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void cancelActionBarAnimation()
	{
		m_toolbar.animate().cancel();
	}

	private void setActionBarTranslationY(float translationY)
	{
		cancelActionBarAnimation();
		translationY = Math.min(Math.max(-getActionBarHeight(), getToolbar().getTranslationY() + translationY), 0);
		getToolbar().setTranslationY(translationY);
	}

	//=========================================================================
	// ActionBarScrollListener
	//=========================================================================

	private RecyclerView.OnScrollListener m_actionBarScrollListener = new RecyclerView.OnScrollListener()
	{
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState)
		{
			if (getParentFragment() instanceof BaseRecyclerFragmentEx2)
			{
				final BaseRecyclerFragmentEx2 exploreFragment = (BaseRecyclerFragmentEx2) getParentFragment();
				exploreFragment.onScrollStateChanged(recyclerView, newState);
			}
			else
			{
				if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE)
				{
					final View firstView = recyclerView.getLayoutManager().findViewByPosition(0);

					if (firstView != null && firstView.getTop() > 0)
					{
						showActionBarSlide(true, true);
					}
					else
					{
						showActionBarSlide(m_toolbar.getTranslationY() > -getActionBarHeight() / 2f, true);
					}
				}
			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy)
		{
			if (getParentFragment() instanceof BaseRecyclerFragmentEx2)
			{
				final BaseRecyclerFragmentEx2 exploreFragment = (BaseRecyclerFragmentEx2) getParentFragment();
				exploreFragment.onScrolled(recyclerView, dx, dy);
			}
			else
			{
				setActionBarTranslationY(-dy);
			}
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
	private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

	//=========================================================================
	// Variables
	//=========================================================================

	private Toolbar m_toolbar;
}
