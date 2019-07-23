package com.ogqcorp.bgh.fragment;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.adapter.WepickAdapter;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.data.Wepick;
import com.ogqcorp.bgh.spirit.data.WepickTheme;
import com.ogqcorp.bgh.spirit.data.Wepicks;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.system.PageScrollAdapter;
import com.ogqcorp.bgh.system.ShareManager;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.utils.FragmentUtils;

public final class WepicksFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public WepicksFragment()
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

		Bundle bundle = getArguments();
		if (bundle != null)
		{
			m_id = bundle.getString(KEY_WEPICK_ID);
			m_targetId = bundle.getString(KEY_WEPICK_TARGET_ID);
			m_title = bundle.getString(KEY_WEPICK_TITLE);
		}

		if (TextUtils.isEmpty(m_title))
		{
			m_title = "Wepick!";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_wepicks, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
		{
			m_wepicks = savedInstanceState.getParcelable(KEY_WEPICKS);
			m_targetId = savedInstanceState.getString(KEY_WEPICK_TARGET_ID);
		}

		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		m_layout = new GridLayoutManagerEx(getActivity(), getSpanCount());
		m_listView.setLayoutManager(m_layout);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(m_adapter);

		if (isEmpty() == true)
		{
			m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);
		}

		m_listView.setAdapter(m_mergeAdapter);
		getToolbar().setTitle(m_title);

		registerOnScrollListener(m_pageScrollAdapter);
	}

	@Override
	protected int getFirstVisiblePosition()
	{
		return 0;
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		onStartHelper();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);
		}
		catch (Exception e)
		{
		}
	}

	private void onStartHelper()
	{
		if (getUserVisibleHint() == true && getView() != null)
		{
			if (isEmpty() == true)
			{
				loadData();
			}
			else
			{
				m_pageScrollAdapter.check(m_listView);
				m_backgroundLayout.setVisibility(View.GONE);
				scrollToTargetItem();
			}
		}
	}

	@Override
	public void onRefresh()
	{
		showProgress(true);
		loadData();
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
		outState.putParcelable(KEY_WEPICKS, m_wepicks);
		outState.putString(KEY_WEPICK_TARGET_ID, m_targetId);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(WepickTheme theme)
	{
		WepicksFragment fragment = new WepicksFragment();

		if (theme != null)
		{
			Bundle bundle = new Bundle();
			bundle.putString(KEY_WEPICK_ID, theme.getId());
			bundle.putString(KEY_WEPICK_TITLE, theme.getTitle());
			fragment.setArguments(bundle);
		}

		return fragment;
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(String id)
	{
		Bundle bundle = new Bundle();
		bundle.putString(KEY_WEPICK_ID, id);

		WepicksFragment fragment = new WepicksFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(String id, String itemId)
	{
		Bundle bundle = new Bundle();
		bundle.putString(KEY_WEPICK_ID, id);
		bundle.putString(KEY_WEPICK_TARGET_ID, itemId);

		WepicksFragment fragment = new WepicksFragment();
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		onStartHelper();

		if (m_listView != null && isVisibleToUser == false)
		{
			m_listView.stopScroll();
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private int getSpanCount()
	{
		return 1;
	}

	private boolean isEmpty()
	{
		if (m_wepicks == null || m_wepicks.getWepickList().isEmpty() == true)
			return true;

		return false;
	}

	private boolean hasNext()
	{
		return false;
	}

	protected String getDataUrl()
	{
		return UrlFactory.wepicksWithId(m_id);
	}

	private void loadData()
	{
		if (FragmentUtils.isDestroyed(WepicksFragment.this) == true)
			return;

		if (m_isLoading == true)
			return;

		try
		{
			m_isLoading = true;
			String dataUrl = getDataUrl();
			if (UserManager.getInstance().isGuest() == true)
			{
				Requests.requestByGet(dataUrl, Wepicks.class, m_response, m_errorResponse);
			}
			else
			{
				Requests.authRequestByGet(dataUrl, Wepicks.class, m_response, m_errorResponse);
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

	private void share(Background background)
	{
		try
		{
			AnalyticsManager.getInstance().shareEvent(getContext(), "Share_WePickCompleted");
		}
		catch (Exception e)
		{
		}

		ShareManager.getInstance().share(this, ShareManager.TYPE_WEPICK, background.getUuid(), "");
	}

	private int findPosition(String itemId)
	{
		int index = 0;
		for (Wepick wepick : m_wepicks.getWepickList())
		{
			if (wepick.getItemId().equals(itemId))
			{
				return index;
			}
			index++;
		}
		return -1;
	}

	private void scrollToTargetItem()
	{
		try
		{
			if (TextUtils.isEmpty(m_targetId) == false)
			{
				final int position = findPosition(m_targetId);

				if (position >= 0)
				{
					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							int offset = DisplayManager.getInstance().getPixelFromDp(getContext(), 80);
							m_layout.scrollToPositionWithOffset(position, offset);

							m_listView.setOnScrollListener(new RecyclerView.OnScrollListener()
							{
								@Override
								public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
								{
									super.onScrollStateChanged(recyclerView, newState);
									if (newState == RecyclerView.SCROLL_STATE_DRAGGING)
									{
										m_targetId = null;

										m_adapter.notifyDataSetChanged();
										m_listView.setOnScrollListener(null);
									}
								}
							});
						}
					}, 100);
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<Wepicks> m_response = new Response.Listener<Wepicks>()
	{
		@Override
		public void onResponse(Wepicks wepicks)
		{
			if (FragmentUtils.isDestroyed(WepicksFragment.this) == true) return;

			m_isLoading = false;

			if (wepicks != null)
			{
				if (m_wepicks == null)
				{
					m_wepicks = wepicks;
				}
				else
				{
					m_wepicks.setWepickList(wepicks.getWepickList());
				}

				m_title = m_wepicks.getTitle();
				getToolbar().setTitle(m_title);

				m_backgroundLayout.setVisibility(View.GONE);
				m_adapter.notifyDataSetChanged();
			}

			showProgress(false);
			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
				m_listView.scrollToPosition(0);

				showActionBarSlide(true, false);
			}

			scrollToTargetItem();
		}

	};

	protected Response.ErrorListener m_errorResponse = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError volleyError)
		{
			if (FragmentUtils.isDestroyed(WepicksFragment.this) == true) return;

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

	private WepickAdapter m_adapter = new WepickAdapter()
	{
		@Override
		public int getItemCount()
		{
			return isEmpty() ? 0 : m_wepicks.getWepickList().size();
		}

		@Override
		protected void onClickProfile(User user)
		{
			Fragment fragment = UserInfoFragment.newInstance(UrlFactory.usersInfo(user.getUsername()));
			AbsMainActivity.getTabStackHelper(WepicksFragment.this).showFragment(fragment);
		}

		@Override
		protected boolean drawDim(String itemId)
		{
			if (TextUtils.isEmpty(m_targetId) == true)
				return false;

			return m_targetId.equals(itemId) ? false : true;
		}

		@Override
		protected void onClickImage(Background background)
		{
			Fragment fragment = BackgroundPageFragment.newInstance(background);
			AbsMainActivity.getTabStackHelper(WepicksFragment.this).showFragment(fragment);
		}

		@Override
		protected void onClickShare(Background background)
		{
			WepicksFragment.this.share(background);
		}

		@Override
		public Wepick getItem(int position)
		{
			return isEmpty() ? null : m_wepicks.getWepickList().get(position);
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
			//loadDataNext();
		}

		@Override
		protected boolean hasNext()
		{
			return WepicksFragment.this.hasNext();
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

	private static final String KEY_WEPICKS = "KEY_WEPICKS";
	private static final String KEY_WEPICK_ID = "KEY_WEPICK_ID";
	private static final String KEY_WEPICK_TARGET_ID = "KEY_WEPICK_TARGET_ID";
	private static final String KEY_WEPICK_TITLE = "KEY_WEPICK_TITLE";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.background_layout) View m_backgroundLayout;
	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	private boolean m_isLoading;

	private Unbinder m_unbinder;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;

	private String m_id;
	private String m_targetId;

	private String m_title;
	private Wepicks m_wepicks;
}
