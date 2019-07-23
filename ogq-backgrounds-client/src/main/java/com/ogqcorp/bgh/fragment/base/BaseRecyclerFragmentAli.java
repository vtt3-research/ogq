package com.ogqcorp.bgh.fragment.base;

import android.animation.ValueAnimator;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.AbsListView;

import com.ogqcorp.bgh.fragment.explore.ExploreFragment;
import com.ogqcorp.commons.OnScrollListenerDistributor;
import com.ogqcorp.commons.StaticViewAdapter;

public abstract class BaseRecyclerFragmentAli extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_listView = view.findViewById(android.R.id.list);
		m_listView.addOnScrollListener(m_distributor);

		if (isOverlayActionBar() == true)
		{
			registerOnScrollListener(m_actionBarScrollListener);
			m_listView.setOnHierarchyChangeListener(m_actionBarHierarchyChangeListener);
		}

		if (savedInstanceState != null)
		{
			m_alpha = savedInstanceState.getInt(KEY_TOOLBAR_ALPHA);

			if (isOverlayActionBar() == true)
			{
				getToolbar().setTitle("");
				setToolbarAlpha(m_alpha);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_TOOLBAR_ALPHA, m_alpha);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_listView = null;

		getToolbar().animate().cancel();
		getToolbar().clearAnimation();

		if (getParentFragment() instanceof ExploreFragment == false)
		{
			getToolbar().setTranslationY(0);
		}
	}

	@Override
	public void onScrollTop()
	{
		if (m_listView != null)
		{
			// for fast scrolling to top
			if (getFirstVisiblePosition() > 20)
			{
				m_listView.scrollToPosition(10);
			}

			m_listView.smoothScrollToPosition(0);
		}
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void registerOnScrollListener(RecyclerView.OnScrollListener listener)
	{
		m_distributor.registerListener(listener);
	}

	protected void unregisterOnScrollListener(RecyclerView.OnScrollListener listener)
	{
		m_distributor.unregisterListener(listener);
	}

	//=========================================================================
	// Protected Abstract Methods
	//=========================================================================

	protected abstract int getFirstVisiblePosition();

	//=========================================================================
	// ActionBarScrollListener
	//=========================================================================

	private RecyclerView.OnScrollListener m_actionBarScrollListener = new RecyclerView.OnScrollListener()
	{
		@Override
		public void onScrollStateChanged(RecyclerView recyclerView, int newState)
		{
			switch (newState)
			{
				case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
					// Toolbar auto slide condition
					int translateY = (m_alpha == 0 ? isToolbarVisible() : isToolbarVisibleHalf())
							? 0
							: -getToolbarHeight();

					startToolbarScrollAnimation(translateY);

					if (isToolbarVisible() == true && getToolbarTranslationY() < 0)
					{
						startToolBarFadeTo(255);
					}
					break;

				case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					if (m_translateAnimator != null) m_translateAnimator.cancel();
					break;

				case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
					break;
			}
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy)
		{
			View headerview = getHeaderView();

			if (dy > 0) onScrollUp(dy, headerview);
			if (dy < 0) onScrollDown(dy, headerview);
		}

		private void onScrollUp(int dy, View headerView)
		{
			if (isToolbarVisible() == false)
			{
				setToolbarAlpha(255);
			}

			float translateY = (m_alpha == 0)
					? Math.max(Math.min(0, getHeaderViewTranslationY(headerView) - getToolbarHeight()), -getToolbarHeight())
					: Math.max(Math.min(0, getToolbarTranslationY() - dy), -getToolbarHeight());

			getToolbar().setTranslationY(translateY);
		}

		private void onScrollDown(int dy, View headerView)
		{
			if (getHeaderViewTranslationY(headerView) >= getToolbarHeight())
			{
				if (m_alpha == 255)
				{
					startToolBarFadeTo(0);
				}
			}

			float translateY = Math.min(getToolbarTranslationY() - dy, 0);

			getToolbar().setTranslationY(translateY);
		}
	};

	//========================================================================
	// Private Methods
	//========================================================================

	private View getHeaderView()
	{
		StaticViewAdapter.ViewHolder viewHolder = (StaticViewAdapter.ViewHolder) m_listView.findViewHolderForAdapterPosition(0);
		return (viewHolder != null) ? viewHolder.itemView : null;
	}

	private void setToolbarAlpha(int alpha)
	{
		m_alpha = alpha;
		setActionBarAlpha(alpha);
	}

	private int getHeaderViewTranslationY(View view)
	{
		return view != null ? view.getTop() + view.getHeight() : 0;
	}

	private boolean isToolbarVisible()
	{
		return getToolbarTranslationY() > -getToolbarHeight();
	}

	private boolean isToolbarVisibleHalf()
	{
		return getToolbarTranslationY() > -getToolbarHeight() / 2;
	}

	private float getToolbarTranslationY()
	{
		return getToolbar().getTranslationY();
	}

	private int getToolbarHeight()
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOOLBAR_HEIGHT_DP, getResources().getDisplayMetrics());
	}

	private void startToolbarScrollAnimation(int translateY)
	{
		if (m_translateAnimator != null) m_translateAnimator.cancel();
		m_translateAnimator = getToolbar().animate().setDuration(300).translationY(translateY);
		m_translateAnimator.start();
	}

	private void startToolBarFadeTo(int alpha)
	{
		ValueAnimator fadeAnimator = ValueAnimator.ofInt(m_alpha, alpha);
		fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
		{
			@Override
			public void onAnimationUpdate(ValueAnimator animation)
			{
				int value = (int) animation.getAnimatedValue();
				setActionBarAlpha(value);
			}
		});
		fadeAnimator.start();
		m_alpha = alpha;
	}

	//=========================================================================
	// ActionBarHierarchyChangeListener
	//=========================================================================

	private ViewGroup.OnHierarchyChangeListener m_actionBarHierarchyChangeListener = new ViewGroup.OnHierarchyChangeListener()
	{
		@Override
		public void onChildViewAdded(View parent, View child)
		{
			m_actionBarScrollListener.onScrolled(m_listView, 0, 0);
		}

		@Override
		public void onChildViewRemoved(View parent, View child)
		{
			m_listView.setOnHierarchyChangeListener(null);
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

	protected RecyclerView m_listView;

	private int m_alpha = 0;

	private ViewPropertyAnimator m_translateAnimator;

	private OnScrollListenerDistributor m_distributor = new OnScrollListenerDistributor();
}