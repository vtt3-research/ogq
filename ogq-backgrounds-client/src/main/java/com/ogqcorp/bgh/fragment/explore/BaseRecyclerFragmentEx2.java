package com.ogqcorp.bgh.fragment.explore;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.commons.utils.FragmentUtils;

public abstract class BaseRecyclerFragmentEx2 extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_tabs = view.findViewById(R.id.tabs);

		if (isOverlayActionBar() == true)
		{
			return;
		}

		m_toolbar = getToolbar();

		m_moveAnimator = ValueAnimator.ofFloat(0, 1);
		m_moveAnimator.setDuration(150);
		m_moveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				if (FragmentUtils.isDestroyed(BaseRecyclerFragmentEx2.this) == true) return;

				final float value = (float) animation.getAnimatedValue();
				setToolbarY(value);
			}
		});
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		showActionBarSlide(true, false);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	public void showActionBarSlide(boolean enabled, boolean animation)
	{
		if (m_toolbar != null)
		{
			final float targetPosition = (enabled == true) ? 0 : -getActionBarHeight();

			if (animation == true)
			{
				if (m_moveAnimator != null)
				{
					m_moveAnimator.setFloatValues(getToolbarY(), targetPosition);
					m_moveAnimator.setInterpolator(enabled == true ? ACCELERATE_INTERPOLATOR : DECELERATE_INTERPOLATOR);
					m_moveAnimator.start();
				}
			}
			else
			{
				m_moveAnimator.cancel();
				setToolbarY(targetPosition);
			}
		}
	}

	private void setActionBarTranslationY(float translationY)
	{
		if (m_moveAnimator != null && m_moveAnimator.isRunning() == false)
		{
			translationY = Math.min(Math.max(-getActionBarHeight(), getToolbarY() + translationY), 0);
			setToolbarY(translationY);
		}
	}

	public void onScrollStateChanged(RecyclerView recyclerView, int newState)
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
				showActionBarSlide(getToolbarY() > -getActionBarHeight() / 2f, true);
			}

			m_stopTabScroll = false;
		}
	}

	public void onScrolled(RecyclerView recyclerView, int dx, int dy)
	{
		if (m_stopTabScroll == true)
		{
			if (recyclerView != null)
			{
				recyclerView.stopScroll();
			}
			return;
		}

		setActionBarTranslationY(-dy);
	}

	private float getToolbarY()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			return m_toolbar.getTranslationY();
		}
		else
		{
			final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) m_toolbar.getLayoutParams();
			return layoutParams.topMargin;
		}
	}

	private void setToolbarY(float y)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			m_toolbar.setTranslationY(y);
			m_tabs.setTranslationY(getActionBarHeight() + y);
		}
		else
		{
			final ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) m_toolbar.getLayoutParams();
			lpToolbar.topMargin = (int) y;

			final ViewGroup.MarginLayoutParams lpTabs = (ViewGroup.MarginLayoutParams) m_tabs.getLayoutParams();
			lpTabs.topMargin = getActionBarHeight() + (int) y;

			m_tabs.requestLayout();
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
	private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

	//=========================================================================
	// Variables
	//=========================================================================

	private TabLayout m_tabs;

	private Toolbar m_toolbar;
	private ValueAnimator m_moveAnimator;

	protected boolean m_stopTabScroll;
}
