package com.ogqcorp.bgh.fragment.base;

import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import butterknife.ButterKnife;

import com.ogqcorp.commons.OnScrollListenerDistributor;
import com.ogqcorp.commons.utils.ListViewUtils;

public abstract class BaseListFragment extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_listView = view.findViewById(android.R.id.list);
		m_listView.setOnScrollListener(m_distributor);

		if (isOverlayActionBar() == true)
		{
			registerOnScrollListener(m_actionBarScrollListener);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_listView = null;
	}

	@Override
	public void onScrollTop()
	{
		if (m_listView != null)
		{
			ListViewUtils.smoothScrollTop(m_listView);
		}
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void registerOnScrollListener(AbsListView.OnScrollListener listener)
	{
		m_distributor.registerListener(listener);
	}

	protected void unregisterOnScrollListener(AbsListView.OnScrollListener listener)
	{
		m_distributor.unregisterListener(listener);
	}

	//=========================================================================
	// ActionBarScrollListener
	//=========================================================================

	private AbsListView.OnScrollListener m_actionBarScrollListener = new AbsListView.OnScrollListener()
	{
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
		{
			// Nothing
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
		{
			if (firstVisibleItem == 0)
			{
				final View headerView = view.getChildAt(0);

				if (headerView != null)
				{
					if (m_headerHeight == 0)
					{
						m_headerHeight = headerView.getHeight();
					}

					onScroll(-headerView.getTop(), false);
				}
			}
			else if (firstVisibleItem > 0)
			{
				onScroll(Integer.MAX_VALUE, true);
			}
		}

		private void onScroll(int scrollPosition, boolean isOver)
		{
			final int alpha = calculateAlpha(scrollPosition, m_headerHeight, isOver);
			setActionBarAlpha(alpha);
		}
	};

	//=========================================================================
	// Variables
	//=========================================================================

	protected ListView m_listView;

	private int m_headerHeight = 0;
	private OnScrollListenerDistributor m_distributor = new OnScrollListenerDistributor();
}