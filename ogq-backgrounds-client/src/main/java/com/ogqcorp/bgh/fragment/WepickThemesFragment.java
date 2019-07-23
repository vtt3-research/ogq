package com.ogqcorp.bgh.fragment;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.ColorUtils;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.adapter.WepickThemeAdapter;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.WepickTheme;
import com.ogqcorp.bgh.spirit.data.WepickThemes;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.FragmentFactory;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.system.PageScrollAdapter;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.utils.FragmentUtils;

public final class WepickThemesFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public WepickThemesFragment()
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_wepick_themes, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
		{
			m_wepickThemes = savedInstanceState.getParcelable(KEY_WEPICK_THEMES);
		}

		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);

		m_layout = new GridLayoutManagerEx(getActivity(), getSpanCount());
		m_listView.setLayoutManager(m_layout);
		m_listView.setAdapter(m_mergeAdapter);

		if (isEmpty() == true)
		{
			loadData();
		}
		else
		{
			m_backgroundView.setVisibility(View.GONE);
			if (hasNext() == false)
			{
				showProgress(false);
			}
		}

		registerOnScrollListener(m_pageScrollAdapter);
	}

	protected int getFirstVisiblePosition()
	{
		return m_layout.findFirstVisibleItemPosition();
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected void onInitActionBar()
	{
	}

	@Override
	protected void setActionBarAlpha(int alpha)
	{
		super.setActionBarAlpha(alpha);
		updateToolbarThemeColor(alpha);
	}

	@Override
	public void onRefresh()
	{
		showProgress(true);
		loadData();
	}

	@Override
	public void onScrollTop()
	{
		if (m_listView != null)
		{
			if (m_layout.findFirstCompletelyVisibleItemPosition() == 0)
			{
				m_swipeRefreshLayout.setRefreshing(true);
				onRefresh();
			}
			else
			{
				m_listView.scrollToPosition(0);
			}
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		//onStartHelper();

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


		if (m_listView != null && isVisibleToUser == false)
		{
			m_listView.stopScroll();
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_WEPICK_THEMES, m_wepickThemes);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		return new WepickThemesFragment();
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private int getSpanCount()
	{
		/*final Context context = getActivity();
		return DeviceUtils.isLandscape(context) == false ? 2 : 4;*/
		return 1;
	}

	private boolean isEmpty()
	{
		if (m_wepickThemes == null || m_wepickThemes.getWepickThemeList().isEmpty() == true)
			return true;

		return false;
	}

	private boolean hasNext()
	{
		if (m_wepickThemes != null && TextUtils.isEmpty(m_wepickThemes.getNextUrl()) == false)
		{
			return true;
		}
		return false;
	}

	protected String getDataUrl()
	{
		return UrlFactory.wepicks();
	}

	private void loadData()
	{
		if (FragmentUtils.isDestroyed(WepickThemesFragment.this) == true)
			return;

		if (m_isLoading == true)
			return;

		try
		{
			m_isLoading = true;
			String dataUrl = getDataUrl();
			if (UserManager.getInstance().isGuest() == true)
			{
				Requests.requestByGet(dataUrl, WepickThemes.class, m_response, m_errorResponse);
			}
			else
			{
				Requests.authRequestByGet(dataUrl, WepickThemes.class, m_response, m_errorResponse);
			}

		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void loadDataNext()
	{
		if (FragmentUtils.isDestroyed(WepickThemesFragment.this) == true)
			return;

		try
		{
			String url = m_wepickThemes.getNextUrl();
			if (UserManager.getInstance().isGuest() == true)
			{
				Requests.requestByGet(url, WepickThemes.class, m_response, m_errorResponse);
			}
			else
			{
				Requests.authRequestByGet(url, WepickThemes.class, m_response, m_errorResponse);
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void showProgress(boolean isShow)
	{
		try
		{
			final View view = m_mergeAdapter.findViewById(R.id.progress);

			if (isShow == true)
			{
				view.setVisibility(View.VISIBLE);
			}
			else
			{
				view.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			//Log.e(e);
		}
	}

	private void updateToolbarThemeColor(int alpha)
	{
		Toolbar toolbar = getToolbar();
		if (toolbar != null && isAdded() == true && getUserVisibleHint() == true)
		{
			int color = getResources().getColor(alpha == 255 ? R.color.black : R.color.white);
			toolbar.setTitleTextColor(ColorUtils.setAlphaComponent(color, alpha));
			toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

			if (toolbar.getOverflowIcon() != null)
			{
				toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}
	}

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<WepickThemes> m_response = new Response.Listener<WepickThemes>()
	{
		@Override
		public void onResponse(WepickThemes wepickThemes)
		{
			if (FragmentUtils.isDestroyed(WepickThemesFragment.this) == true) return;

			m_isLoading = false;

			if (isEmpty() == true)
			{
				m_wepickThemes = wepickThemes;
			}
			else
			{
				if (m_swipeRefreshLayout.isRefreshing() == true)
				{
					m_wepickThemes.setWepickThemeList(wepickThemes.getWepickThemeList());
				}
				else
				{
					m_wepickThemes.getWepickThemeList().addAll(wepickThemes.getWepickThemeList());
				}
				m_wepickThemes.setNextUrl(wepickThemes.getNextUrl());
			}
			m_backgroundView.setVisibility(View.GONE);
			m_adapter.notifyDataSetChanged();

			if (hasNext() == false)
			{
				showProgress(false);
			}

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
				m_listView.scrollToPosition(0);

				showActionBarSlide(true, false);
			}
		}

	};

	protected Response.ErrorListener m_errorResponse = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError volleyError)
		{
			if (FragmentUtils.isDestroyed(WepickThemesFragment.this) == true) return;

			m_isLoading = false;
			if (m_swipeRefreshLayout != null && m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
			}

			try
			{
				showProgress(false);
				VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
				volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				volleyErrorHandler.handleError(volleyError);
			}
			catch (Exception e)
			{
				// Nothing
			}
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private WepickThemeAdapter m_adapter = new WepickThemeAdapter()
	{
		@Override
		public int getItemCount()
		{
			if (isEmpty() == true)
				return 0;

			return m_wepickThemes.getWepickThemeList().size();
		}

		@Override
		protected void onClickWepickTheme(View view, WepickTheme theme)
		{
			switch (theme.getType())
			{
				case WepickTheme.TYPE_UPLOADING:
				{
					Fragment fragment = FragmentFactory.createWepicksUploadFragment(theme);
					AbsMainActivity.getTabStackHelper(WepickThemesFragment.this).showFragment(fragment);

					try
					{
						AnalyticsManager.getInstance().WepickEvent(getContext(), "WePickUpload_WePick", null);
					}
					catch (Exception e)
					{
					}
				}
				break;
				case WepickTheme.TYPE_VOTING:
				case WepickTheme.TYPE_VOTED:
				{
					Fragment fragment = FragmentFactory.createWepicksFragment(theme);
					AbsMainActivity.getTabStackHelper(WepickThemesFragment.this).showFragment(fragment);

					try
					{
						if(theme.getType().equals(WepickTheme.TYPE_VOTING))
							AnalyticsManager.getInstance().WepickEvent(getContext(), "WePickVote_WePick", null);
						else if(theme.getType().equals(WepickTheme.TYPE_VOTED))
							AnalyticsManager.getInstance().WepickEvent(getContext(), "WePickCompleted_WePick", null);
					}
					catch (Exception e)
					{
					}
				}
				break;
				default:
					//nothing
			}
		}

		@Override
		public WepickTheme getItem(int position)
		{
			return isEmpty() ? null : m_wepickThemes.getWepickThemeList().get(position);
		}
	};

	//=========================================================================
	// Page Scroll Adapter
	//=========================================================================

	final PageScrollAdapter m_pageScrollAdapter = new PageScrollAdapter()
	{
		@Override
		protected void onLoadNext()
		{
			loadDataNext();
		}

		@Override
		protected boolean hasNext()
		{
			return WepickThemesFragment.this.hasNext();
		}

		@Override
		protected boolean isLoading()
		{
			return m_isLoading;
		}

		@Override
		protected int findLastVisibleItemPosition()
		{
			return m_layout.findLastVisibleItemPosition();
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_WEPICK_THEMES = "KEY_WEPICK_THEMES";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.background_view) View m_backgroundView;
	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	private boolean m_isLoading;

	private Unbinder m_unbinder;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;

	private WepickThemes m_wepickThemes;
}
