package com.ogqcorp.bgh.fragment.explore;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.system.NestedScrollViewEx;
import com.ogqcorp.commons.OnScrollListenerDistributor;
import com.ogqcorp.commons.utils.FragmentUtils;

public abstract class BaseRecyclerFragmentEx3 extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_toolbar = getToolbar();
		m_profileView = view.findViewById(R.id.user);

		m_scrollView = view.findViewById(R.id.scroll);
		m_scrollView.setOnScrollChangeListener(m_distributor);
		m_scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		m_scrollView.setScrollListener(m_scrollStateListener);
		m_scrollView.setFillViewport(true);

		registerOnScrollListener(m_actionBarScrollListener);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		unregisterOnScrollListener(m_actionBarScrollListener);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private NestedScrollView.OnScrollChangeListener m_actionBarScrollListener = new NestedScrollView.OnScrollChangeListener()
	{
		@Override
		public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
		{
			int dy = scrollY - oldScrollY;
			scrollChange(dy);

			if (scrollY > m_profileView.getHeight() && dy < 0)
				return;

			float translationY = Math.min(Math.max(-getActionBarHeight(), getToolbarY() - dy), 0);
			setToolbarY(translationY);
		}
	};

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void registerOnScrollListener(NestedScrollView.OnScrollChangeListener listener)
	{
		m_distributor.registerListener(listener);
	}

	protected void unregisterOnScrollListener(NestedScrollView.OnScrollChangeListener listener)
	{
		m_distributor.unregisterListener(listener);
	}

	protected View getHeaderView()
	{
		return m_profileView == null ? null : m_profileView;
	}

	public void syncToolbarY()
	{
		setToolbarY(getToolbarY());
	}

	protected float getToolbarY()
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

	protected void setToolbarY(float translationY)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			m_toolbar.setTranslationY(translationY);
			m_profileView.setTranslationY(getActionBarHeight() + translationY);
			//m_scrollView.setTranslationY(getActionBarHeight() + translationY);
		}
		else
		{
			final ViewGroup.MarginLayoutParams lpToolbar = (ViewGroup.MarginLayoutParams) m_toolbar.getLayoutParams();
			lpToolbar.topMargin = (int) translationY;

			final ViewGroup.MarginLayoutParams lpProfileView = (ViewGroup.MarginLayoutParams) m_profileView.getLayoutParams();
			lpProfileView.topMargin = getActionBarHeight() + (int) translationY;

			/*final ViewGroup.MarginLayoutParams lpSwipeLayout = (ViewGroup.MarginLayoutParams) m_scrollView.getLayoutParams();
			lpSwipeLayout.topMargin = getActionBarHeight() + (int) translationY;*/

			m_profileView.requestLayout();
		}
	}

	protected boolean isToolbarVisible()
	{
		return getToolbarY() > -getToolbarHeight();
	}

	protected boolean isToolbarVisibleHalf()
	{
		return getToolbarY() > -getToolbarHeight() / 2;
	}

	protected int getToolbarHeight()
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOOLBAR_HEIGHT_DP, getResources().getDisplayMetrics());
	}

	protected int getHeaderViewScrollY()
	{
		View view = getHeaderView();
		return view != null ? -m_scrollView.getScrollY() + view.getHeight() : 0;
	}

	protected abstract void scrollChange(int dy);

	//=========================================================================
	// ScrollViewEx OnScrollStateListener
	//=========================================================================

	private NestedScrollViewEx.OnScrollStateListener m_scrollStateListener = new NestedScrollViewEx.OnScrollStateListener()
	{
		@Override
		public void onScrollViewStateChanged(int state)
		{
			if (state == NestedScrollViewEx.SCROLL_STATE_IDLE)
			{
				m_handler.removeCallbacks(m_postRunnable);
				m_handler.postDelayed(m_postRunnable, 150);
			}
		}
	};

	//=========================================================================
	// Toolbar Runner
	//=========================================================================

	private Runnable m_postRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			// Toolbar autoslide condition when touch up
			if (FragmentUtils.isDestroyed(BaseRecyclerFragmentEx3.this)) return;

			// out of header area
			if (isToolbarVisible() == true)
			{
				int translateY = isToolbarVisibleHalf() == false ? -getToolbarHeight() : 0;
				setToolbarY(translateY);
			}
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final int TOOLBAR_HEIGHT_DP = 56;

	//=========================================================================
	// Variables
	//=========================================================================

	private NestedScrollViewEx m_scrollView;
	private ViewGroup m_profileView;
	private Toolbar m_toolbar;

	private Handler m_handler = new Handler();

	private OnScrollListenerDistributor m_distributor = new OnScrollListenerDistributor();
}
