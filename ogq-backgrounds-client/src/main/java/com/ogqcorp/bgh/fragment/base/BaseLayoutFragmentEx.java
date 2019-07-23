package com.ogqcorp.bgh.fragment.base;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.explore.BaseRecyclerFragmentEx2;
import com.ogqcorp.bgh.fragment.explore.ExploreFragment;
import com.ogqcorp.bgh.view.SwipeRefreshLayoutEx;

import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public abstract class BaseLayoutFragmentEx extends BaseLayoutFragmentAli
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

		m_scrollView.setClipToPadding(false);
		m_scrollView.setPadding(0, getActionBarHeight(), 0, 0);

		registerOnScrollListener(m_actionBarScrollListener);

		if (getParentFragment() instanceof ExploreFragment)
		{
			final int tabHeight = getResources().getDimensionPixelSize(R.dimen.tab_height);

			SwipeRefreshLayoutEx.setCircleViewPadding(swipeRefreshLayout, getActionBarHeight() + tabHeight);
			m_scrollView.setPadding(0, getActionBarHeight() + tabHeight, 0, 0);
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

	private NestedScrollView.OnScrollChangeListener m_actionBarScrollListener = new NestedScrollView.OnScrollChangeListener()
	{
		@Override
		public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
		{
			int dy = scrollY - oldScrollY;

			if (oldScrollY == 0) return; // Needs for Pass SwipeRefresh.onLayout()

			if (getParentFragment() instanceof BaseRecyclerFragmentEx2)
			{
				final BaseRecyclerFragmentEx2 exploreFragment = (BaseRecyclerFragmentEx2) getParentFragment();
				exploreFragment.onScrolled(null, 0, dy);
			}
			else
			{
				//setActionBarTranslationY(-dy);
				if (dy > 0) onScrollUp(dy);
				if (dy < 0) onScrollDown(dy);

				onTopScrollY(scrollY);
			}
		}

		private void onScrollUp(int dy)
		{
			if (isToolbarVisible() == false)
			{
				setToolbarAlpha(255);
			}

			float translateY = (m_toolbarAlpha == 0)
					? Math.max(Math.min(0, getHeaderViewScrollY() - getToolbarHeight()), -getToolbarHeight())
					: Math.max(Math.min(0, getToolbarTranslationY() - dy), -getToolbarHeight());

			getToolbar().setTranslationY(translateY);
		}

		private void onScrollDown(int dy)
		{
			if (getHeaderViewScrollY() >= getToolbarHeight())
			{
				if (m_toolbarAlpha == 255)
				{
					startToolBarFadeTo(0);
				}
			}

			float translateY = Math.min(getToolbarTranslationY() - dy, 0);

			getToolbar().setTranslationY(translateY);
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
