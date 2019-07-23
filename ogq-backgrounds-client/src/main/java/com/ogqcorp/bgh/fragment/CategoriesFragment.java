package com.ogqcorp.bgh.fragment;

import java.util.ArrayList;
import java.util.Collections;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.adapter.CategoriesAdapter;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.model.CategoriesModel;
import com.ogqcorp.bgh.model.CategoriesModelData;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Categories;
import com.ogqcorp.bgh.spirit.data.Category;
import com.ogqcorp.bgh.spirit.data.HeaderBannerCell;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.system.HeadersInflater;
import com.ogqcorp.bgh.system.SlackMessage;
import com.ogqcorp.commons.Log;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.request.volley.ParseErrorEx;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class CategoriesFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		m_rotationAdUnit = PreferencesManager.getInstance().getCountryCountAdList(getContext());
		m_firstAdIndex = DeviceUtils.isTablet(getContext()) ? FIRST_NATIVE_AD_INDEX_FOR_TABLET : FIRST_NATIVE_AD_INDEX;

		m_data = CategoriesModel.getInstance().get(this, new BaseModel.DataCreator<CategoriesModelData>()
		{
			@Override
			public CategoriesModelData newInstance()
			{
				return new CategoriesModelData();
			}
		});

		final String dataUrl = getArguments().getString(KEY_DATA_URL);
		m_data.setDataUrl(dataUrl);

		//AdCheckManager.getInstance().checkNativeAds(new AdCheckManager.AdAvailabilityCallback()
		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				m_nativeAds = AdCheckManager.getInstance().getShuffledNativeAdList(CategoriesFragment.this);

				if (m_nativeAds == null || m_nativeAds.size() == 0)
				{
					onNotAvailable();
				}
				else
				{
					m_isAvailableNativeAds = true;
					onRefresh();
				}
			}

			@Override
			public void onNotAvailable()
			{
				m_isAvailableNativeAds = false;
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_categories, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		m_layout = new GridLayoutManagerEx(getActivity(), getSpanCount()/**/);
		m_layout.setSpanSizeLookup(m_spanSizeLookup);

		m_listView.setLayoutManager(m_layout);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_headers);
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);

		m_listView.setAdapter(m_mergeAdapter);

		showProgress(true);

		if (m_data.isLoaded() == true)
		{
			updateHeaderViews();
			showProgress(false);
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		onStartHelper();

		try
		{
			if (isVisibleToUser)
			{
				String screenName = getClass().getSimpleName();

				Context context = getContext();
				if (context == null)
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
	public void onStart()
	{
		super.onStart();
		onStartHelper();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		m_data.cancelRequests();
	}

	@Override
	public void onRelease()
	{
		CategoriesModel.getInstance().release(this);
	}

	@Override
	protected void onInitActionBar()
	{
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected int getFirstVisiblePosition()
	{
		return m_layout.findFirstVisibleItemPosition();
	}

	@Override
	public void onRefresh()
	{
		m_data.invalidate();

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
				showActionBarSlide(true, false);
			}
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		final Fragment fragment = new CategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(KEY_DATA_URL, UrlFactory.categories());
		fragment.setArguments(bundle);

		return BaseModel.wrap(fragment);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(String dataUrl)
	{
		final Fragment fragment = new CategoriesFragment();

		Bundle bundle = new Bundle();
		bundle.putString(KEY_DATA_URL, dataUrl);
		fragment.setArguments(bundle);

		return BaseModel.wrap(fragment);
	}

	@CalledByReflection
	public void onClickHeaderBanner(View view)
	{
		final HeaderBannerCell headerBannerCell = (HeaderBannerCell) view.getTag();

		final String uri = headerBannerCell.getUri();
		if (TextUtils.isEmpty(uri) == false)
		{
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			getActivity().startActivity(intent);
		}
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void onClickAdFree()
	{
		if (UserManager.getInstance().isGuest())
		{
			if (UserManager.getInstance().isGuest() == true)
			{
				getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_NONE));
				return;
			}
		}

		PurchaseAdFreeDialogFragment.start(getActivity().getSupportFragmentManager());
	}

	protected void onClickCategory(View view, Category category)
	{
		final String dataUrl = category.getDataUrl();
		onOpenBackgrounds(dataUrl);

		try
		{
			if (category != null && category.getTitle() != null)
				AnalyticsManager.getInstance().CategoryEvent(getContext(), category.getTitle() + "_Category");
		}
		catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private int getSpanCount()
	{
		final Context context = getActivity();

		//return DeviceUtils.isLandscape(context) == false ? 2 : 4;
		return DeviceUtils.isTablet(context) == true ? 4 : 2;
	}

	private void updateHeaderViews()
	{
		final ViewGroup headersView = (ViewGroup) m_mergeAdapter.findViewById(R.id.headers);

		if (m_data.getHeadersList() == null || m_data.getHeadersList().size() == 0)
		{
			headersView.removeAllViews();
		}
		else if (headersView.getChildCount() == 0)
		{
			HeadersInflater.inflate(this, getLayoutInflater(), m_data.getHeadersList(), headersView);
		}
	}

	private void onStartHelper()
	{
		if (getUserVisibleHint() == true && getView() != null)
		{
			if (m_data.isLoaded() == false)
			{
				loadData();
			}
		}
	}

	private void loadData()
	{
		m_data.request(m_response, m_errorResponse);
	}

	private void showProgress(boolean isShow)
	{
		try
		{
			final View view = m_mergeAdapter.findViewById(R.id.progress);

			if (isShow == true)
			{
				view.setVisibility(View.VISIBLE);

				if (m_spaceViews[0] != null)
				{
					m_spaceViews[0].getChildAt(0).setVisibility(View.GONE);
				}
				if (m_spaceViews[1] != null)
				{
					m_spaceViews[1].getChildAt(0).setVisibility(View.GONE);
				}
			}
			else
			{
				view.setVisibility(View.GONE);

				if (m_spaceViews[0] != null)
				{
					m_spaceViews[0].getChildAt(0).setVisibility(View.VISIBLE);
				}
				if (m_spaceViews[1] != null)
				{
					m_spaceViews[1].getChildAt(0).setVisibility(View.VISIBLE);
				}
			}
		}
		catch (Exception e)
		{
			Log.e(e);
		}
	}

	private void showErrorDialog(final Exception e)
	{
		ErrorDialogFragment.start(getChildFragmentManager(), e, new ErrorDialogFragment.DialogCallback()
		{
			@Override
			public void onShow(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(CategoriesFragment.this) == true) return;

				try
				{
					if (e.toString().contains("hostname") == true)
					{
						SlackMessage.sendMessageForHostnameIssue(getActivity(), "*Categories URL*");
					}
					else if (e instanceof ParseErrorEx)
					{
						SlackMessage.sendMessageForJsonParseIssue(getActivity(), e);
					}
				}
				catch (Exception e)
				{
					// Nothing
				}
			}

			@Override
			public void onRetry(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(CategoriesFragment.this) == true) return;

				onRefresh();
			}
		});
	}

	private boolean checkIsAvailableNativeAds()
	{
		return AdCheckManager.getInstance().isAvailableNativeAds();
	}

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<Categories> m_response = new Response.Listener<Categories>()
	{
		@Override
		public void onResponse(Categories categories)
		{
			if (FragmentUtils.isDestroyed(CategoriesFragment.this) == true) return;

			showProgress(false);

			updateHeaderViews();

			//m_adapter.onSortCategories();
			m_adapter.notifyDataSetChanged();

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
			if (FragmentUtils.isDestroyed(CategoriesFragment.this) == true) return;

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
			}

			try
			{
				showProgress(false);
				showErrorDialog(volleyError);
			}
			catch (Exception e)
			{
				// Nothing
			}

		}
	};

	//=========================================================================
	// Span Size Lookup
	//=========================================================================

	private GridLayoutManager.SpanSizeLookup m_spanSizeLookup = new GridLayoutManager.SpanSizeLookup()
	{
		@Override
		public int getSpanSize(int position)
		{
			final int viewType = m_mergeAdapter.getItemViewType(position);

			if (StaticViewAdapter.isStaticView(viewType) == true) return m_layout.getSpanCount();

			if (position != 0 && position % (m_rotationAdUnit + 1) == 0 && m_isAvailableNativeAds && m_isFirstPageAds == false)
			{
				if (DeviceUtils.isLandscape(getContext()) == true)
				{
					return m_layout.getSpanCount() / 2;
				}

				return m_layout.getSpanCount();
			}
			else if (m_isFirstPageAds && m_isAvailableNativeAds)
			{
				int addedAdsIndex = 0;

				if (position > m_firstAdIndex + 1)
					addedAdsIndex = 1;

				if (position == m_firstAdIndex + 1)
				{
					if (DeviceUtils.isLandscape(getContext()) == true)
						return m_layout.getSpanCount() / 2;
					else
						return m_layout.getSpanCount();
				}
				else if (position > m_firstAdIndex + 1 && (position - addedAdsIndex) % (m_rotationAdUnit + 1) == 0)
				{
					if (DeviceUtils.isLandscape(getContext()) == true)
					{
						return m_layout.getSpanCount() / 2;
					}

					return m_layout.getSpanCount();
				}
			}

			return 1;
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private CategoriesAdapter m_adapter = new CategoriesAdapter()
	{

		@Override
		public int getItemViewType(int position)
		{
			if (getCategory(position).isNativeAd())
			{
				return R.layout.item_background_native_ads;
			}

			return R.layout.item_category_neo;
		}

		@Override
		public int getItemCount()
		{
			if (m_data.isLoaded() == false) return 0;

			int addedAdsCount = 0;

			if (m_isAvailableNativeAds)
			{
				addedAdsCount = m_data.getCategoriesList().size() / m_rotationAdUnit; // TODO : Need check

				if (m_isFirstPageAds)
				{
					if (m_data.getCategoriesList().size() >= m_firstAdIndex)
						addedAdsCount += 1;
				}
			}

			return m_data.getCategoriesList().size() + addedAdsCount;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position)
		{
			onBindViewHolder(getActivity(), getCategory(position), holder, position);
		}

		@Override
		protected void onClickAdFree()
		{
			CategoriesFragment.this.onClickAdFree();
		}

		@Override
		protected void onClickCategory(View view, Category category)
		{
			CategoriesFragment.this.onClickCategory(view, category);
		}

		@Override
		protected void sortCategories()
		{
			Collections.sort(m_data.getCategoriesList());
		}

		@Override
		protected ArrayList<IntegrateNativeAd> getCategoryNativeAdsList()
		{
			return m_nativeAds;
		}

		private Category getCategory(int position)
		{
			if (m_data.isLoaded() == false) return null;

			int addedAdsCount = 0;

			m_isAvailableNativeAds = checkIsAvailableNativeAds();

			if (m_isAvailableNativeAds)
			{
				if (m_isFirstPageAds)
				{
					if (position == m_firstAdIndex)
					{
						Category c = new Category();
						c.setIsNativeAd(true);

						return c;
					}

					// addedAdsCount는 3번째 줄 광고 추가
					// nextindex는 3번째 줄에 광고가 추가됨에 따라 2개(Tablet 4개)의 position이 밀림
					if (position > m_firstAdIndex)
						addedAdsCount = 1;

					if ((position > m_firstAdIndex) && (position + 1 - addedAdsCount) % (m_rotationAdUnit + 1) == 0)
					{
						Category c = new Category();
						c.setIsNativeAd(true);

						return c;
					}

					addedAdsCount += (position + 1 - addedAdsCount) / (m_rotationAdUnit + 1);
				}
				else
				{
					if ((position + 1) % (m_rotationAdUnit + 1) == 0)
					{
						Category c = new Category();
						c.setIsNativeAd(true);

						return c;
					}
					addedAdsCount = (position + 1) / (m_rotationAdUnit + 1);
				}
			}

			return m_data.getCategoriesList().get(position - addedAdsCount);
		}
	};

	//========================================================================
	// Constants
	//========================================================================

	private static final String KEY_DATA_URL = "KEY_DATA_URL";

	public static final int FIRST_NATIVE_AD_INDEX = 6;
	public static final int FIRST_NATIVE_AD_INDEX_FOR_TABLET = 12;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	private int m_rotationAdUnit;
	private int m_firstAdIndex;
	private Unbinder m_unbinder;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;
	private CategoriesModelData m_data;
	private ViewGroup[] m_spaceViews = new ViewGroup[2];
	private ArrayList<IntegrateNativeAd> m_nativeAds;
	private boolean m_isAvailableNativeAds = false;
	private boolean m_isFirstPageAds = true;
}
