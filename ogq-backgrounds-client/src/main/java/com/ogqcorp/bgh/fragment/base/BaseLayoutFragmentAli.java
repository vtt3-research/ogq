package com.ogqcorp.bgh.fragment.base;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

import com.ogqcorp.bgh.R;
import com.ogqcorp.commons.OnScrollListenerDistributor;
import com.ogqcorp.commons.utils.FragmentUtils;

public abstract class BaseLayoutFragmentAli extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_scrollView = view.findViewById(R.id.scroll);
		m_scrollView.setOnScrollChangeListener(m_distributor);
		m_scrollView.setOnTouchListener(m_scrollTouchListener);

		m_viewGroup = view.findViewById(android.R.id.content);

		if (isOverlayActionBar() == true)
		{
			registerOnScrollListener(m_actionBarScrollListener);
			m_scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		}

		if (savedInstanceState != null)
		{
			m_toolbarAlpha = savedInstanceState.getInt(KEY_TOOLBAR_ALPHA);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_TOOLBAR_ALPHA, m_toolbarAlpha);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_scrollView = null;
		m_viewGroup = null;

		getToolbar().clearAnimation();
		getToolbar().animate().cancel();
		getToolbar().setTranslationY(0);
	}

	@Override
	public void onScrollTop()
	{
		if (m_scrollView != null)
		{
			m_scrollView.smoothScrollTo(0, 0);
		}
	}

	//========================================================================
	// Protected Methods
	//========================================================================

	protected View getHeaderView()
	{
		if (m_viewGroup != null)
		{
			return m_viewGroup.getChildAt(0);
		}

		return null;
	}

	protected void setToolbarAlpha(int alpha)
	{
		m_toolbarAlpha = alpha;
		setActionBarAlpha(alpha);
	}

	protected boolean isToolbarVisible()
	{
		return getToolbarTranslationY() > -getToolbarHeight();
	}

	protected boolean isToolbarVisibleHalf()
	{
		return getToolbarTranslationY() > -getToolbarHeight() / 2;
	}

	protected float getToolbarTranslationY()
	{
		return getToolbar().getTranslationY();
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

	protected void onTopScrollY(int translateY)
	{
		cancelToolbarAnimation();

		m_handler.removeCallbacks(m_postRunnable);
		m_handler.postDelayed(m_postRunnable, 100);
	}

	protected void startToolbarScrollAnimation(int translateY)
	{
		cancelToolbarAnimation();

		m_translateAnimator = getToolbar().animate().translationY(translateY);
		m_translateAnimator.start();
	}

	protected void startToolBarFadeTo(int alpha)
	{
		ValueAnimator fadeAnimator = ValueAnimator.ofInt(m_toolbarAlpha, alpha);
		fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int value = (int) animation.getAnimatedValue();
				setActionBarAlpha(value);
			}
		});
		fadeAnimator.setDuration(300).start();

		m_toolbarAlpha = alpha;
	}

	private void cancelToolbarAnimation()
	{
		if (m_translateAnimator != null)
		{
			m_translateAnimator.cancel();
		}
	}

	protected void registerOnScrollListener(NestedScrollView.OnScrollChangeListener listener)
	{
		m_distributor.registerListener(listener);
	}

	protected void unregisterOnScrollListener(NestedScrollView.OnScrollChangeListener listener)
	{
		m_distributor.unregisterListener(listener);
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

			if (dy > 0) onScrollUp(dy);
			if (dy < 0) onScrollDown(dy);

			onTopScrollY(scrollY);
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
	// Toolbar PostAnimation Runner
	//=========================================================================

	private Runnable m_postRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			// Toolbar autoslide condition when touch up
			if (isTouchUp == false || FragmentUtils.isDestroyed(BaseLayoutFragmentAli.this)) return;

			// out of header area
			int translateY = (getHeaderViewScrollY() < 0 && isToolbarVisibleHalf() == false) ? -getToolbarHeight() : 0;
			startToolbarScrollAnimation(translateY);

			// in headerview area, toolbar does not anchored headerview bottom
			if (getHeaderViewScrollY() <= getToolbarHeight() && getToolbarTranslationY() > -getToolbarHeight())
			{
				startToolBarFadeTo(255);
			}
		}
	};

	//=========================================================================
	// ScrollView TouchListener
	//=========================================================================

	private View.OnTouchListener m_scrollTouchListener = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			cancelToolbarAnimation();

			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				if (isToolbarVisible() == true && getToolbarTranslationY() < 0)
				{
					startToolBarFadeTo(255);
				}

				m_handler.postDelayed(m_postRunnable, 100);
			}

			isTouchUp = event.getAction() == MotionEvent.ACTION_UP;

			return false;
		}
	};

	//========================================================================
	// Constants
	//========================================================================

	private static final String KEY_TOOLBAR_ALPHA = "KEY_TOOLBAR_ALPHA";

	private static final int TOOLBAR_HEIGHT_DP = 56;

	//=========================================================================
	// Variables
	//=========================================================================

	protected NestedScrollView m_scrollView;
	protected ViewGroup m_viewGroup;
	protected int m_toolbarAlpha = 0;

	private boolean isTouchUp = false;

	private Handler m_handler = new Handler();

	private ViewPropertyAnimator m_translateAnimator;

	private OnScrollListenerDistributor m_distributor = new OnScrollListenerDistributor();
}