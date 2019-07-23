package com.ogqcorp.bgh.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.adapter.BackgroundsAdapter;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.checker.NetSpeedChecker;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.model.BackgroundsModel;
import com.ogqcorp.bgh.model.BackgroundsModelData;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Backgrounds;
import com.ogqcorp.bgh.spirit.data.Header;
import com.ogqcorp.bgh.spirit.data.HeaderBannerCell;
import com.ogqcorp.bgh.spirit.data.HeaderDescription;
import com.ogqcorp.bgh.spirit.data.TopBanner;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.spirit.manager.LikesManager;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.system.HeadersInflater;
import com.ogqcorp.bgh.system.PageScrollAdapter;
import com.ogqcorp.bgh.system.SlackMessage;
import com.ogqcorp.bgh.system.ViewTransitionHelper;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.request.volley.ParseErrorEx;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;

// Image List Viewing Fragment
// All of Services Image List (GridView)
public class BackgroundsFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener, LikesManager.SyncListener
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

		m_data = BackgroundsModel.getInstance().get(this, new BaseModel.DataCreator<BackgroundsModelData>()
		{
			@Override
			public BackgroundsModelData newInstance()
			{
				return new BackgroundsModelData();
			}
		});

		BackgroundsModel.getInstance().update(m_data);
		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				m_nativeAds = AdCheckManager.getInstance().getShuffledNativeAdList(BackgroundsFragment.this);

				if (m_nativeAds == null || m_nativeAds.size() == 0)
				{
					onNotAvailable();
				} else
				{
					m_isAvailableNativeAds = true;
					refresh();
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
	public void onResume()
	{
		super.onResume();

		if (m_mergeAdapter != null)
		{
			m_mergeAdapter.notifyDataSetChanged();
		}

		if (UserManager.getInstance().isGuest() == false)
		{
			LikesManager.getInstance().registerSyncListener(this);
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();

		if (UserManager.getInstance().isGuest() == false)
		{
			LikesManager.getInstance().unregisterSyncListener(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_backgrounds, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		m_layout = new GridLayoutManagerEx(getActivity(), getSpanCount());
		m_layout.setSpanSizeLookup(m_spanSizeLookup);

		m_listView.setLayoutManager(m_layout);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_headers);

		if (hasHeaderTags() == true)
		{
			m_mergeAdapter.add(getLayoutInflater(), R.layout.item_featured_tags);
		}
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);

		m_listView.setAdapter(m_mergeAdapter);

		if (m_data.hasNext() == false)
		{
			showProgress(false);
		}

		registerOnScrollListener(m_pageScrollAdapter);

		updateHeaderViews();
		keepContext();
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

		if (m_data != null)
			m_data.cancelRequests();

		m_adapter = null;
		m_response = null;
		m_errorResponse = null;

		AdCheckManager.getInstance().removeShuffledNativeAdList(this);
	}

	@Override
	public void onRelease()
	{
		BackgroundsModel.getInstance().release(this);
	}

	@Override
	protected void onInitActionBar()
	{
		super.onInitActionBar();

		getToolbar().setTitle(null);
	}

	@Override
	protected void setActionBarAlpha(int alpha)
	{
		super.setActionBarAlpha(alpha);
		updateToolbarThemeColor(alpha);
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return true;
	}

	@Override
	protected int getFirstVisiblePosition()
	{
		return m_layout.findFirstVisibleItemPosition();
	}

	@Override
	public void onRefresh()
	{
		refresh();

		try
		{
			String className = getClass().getSimpleName();

			if (className != null && className.equals("ShuffleFragment"))
				AnalyticsManager.getInstance().ShuffleEvent(getContext(), "Refresh_Shuffle");
		}
		catch (Exception e)
		{
		}
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
			} else
			{
				m_listView.scrollToPosition(0);
			}
		}
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

	protected boolean hasHeaderTags()
	{
		return false;
	}

	protected int getHeaderCount()
	{
		return HEADER_COUNT;
	}

	protected boolean isTopBanner()
	{
		return false;
	}

	protected boolean isTopBannerOpen()
	{
		return false;
	}

	protected TopBanner getTopBanner()
	{
		return null;
	}

	protected void onClickTopBanner(View view, BackgroundsAdapter adapter)
	{

	}

	protected void onClickTopBannerClose(View view, BackgroundsAdapter adapter)
	{

	}

	protected void updateList()
	{
		if (m_adapter != null && m_adapter.getItemCount() > 0)
		{
			m_adapter.notifyDataSetChanged();
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(String dataUrl)
	{
		final Fragment fragment = new BackgroundsFragment();

		final Bundle args = new Bundle();
		args.putString(KEY_DATA_URL, dataUrl);
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(String dataUrl, Backgrounds backgrounds)
	{
		final Fragment fragment = new BackgroundsFragment();

		final Bundle args = new Bundle();
		args.putString(KEY_DATA_URL, dataUrl);
		fragment.setArguments(args);

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

	@CalledByReflection
	public void onClickHeaderTagFollow(View view)
	{
		final Button button = (Button) view;
		if (button == null)
			return;

		if (button.isSelected())
		{
			tagFollow(button);
		} else
		{
			tagUnFollow(button);
		}
	}

	public void tagFollow(final Button button)
	{
		final HeaderDescription header = (HeaderDescription) button.getTag();

		final String origin = button.getText().toString();
		button.setText("...");

		String url = UrlFactory.tagFollow();
		HashMap params = ParamFactory.tagFollow(header.getTagId());

		Requests.authRequestByPost(url, params, Object.class, new Response.Listener<Object>()
		{
			@Override
			public void onResponse(Object response)
			{
				if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

				button.setText(R.string.userinfo_following);
				button.setSelected(false);
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

				button.setText(origin);
				VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
				volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				volleyErrorHandler.handleError(error);
			}
		});
	}

	public void tagUnFollow(final Button button)
	{
		final HeaderDescription header = (HeaderDescription) button.getTag();

		final String origin = button.getText().toString();
		button.setText("...");

		String url = UrlFactory.tagUnFollow(header.getTagId());

		Requests.authRequestByDelete(url, null, Object.class, new Response.Listener<Object>()
		{
			@Override
			public void onResponse(Object response)
			{
				if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

				button.setText(R.string.userinfo_follow);
				button.setSelected(true);
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

				button.setText(origin);
				VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
				volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				volleyErrorHandler.handleError(error);
			}
		});
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void onClickBackground(View view, Background background)
	{
		final int position = m_listView.getChildAdapterPosition(view) - getHeaderCount();

		int addedAdsCount = 0;
		int addedNewFeatureCount = 0;

		if (isTopBanner() == true && isTopBannerOpen() == true)
		{
			addedNewFeatureCount = 1;
		}

		// 페이스북 광고가 있는 경우
		if (m_isAvailableNativeAds)
		{
			// 배너가 없는 경우
			if (addedNewFeatureCount == 0)
			{
				if (m_isFirstPageAds)
				{
					// addedAdsCount는 3번째 줄 광고 추가
					// nextindex는 3번째 줄에 광고가 추가됨에 따라 2개(Tablet 4개)의 position이 밀림
					if (position > m_firstAdIndex)
					{
						addedAdsCount = 1;
					}

					addedAdsCount += (position + 1 - addedAdsCount) / (m_rotationAdUnit + 1);
				} else
				{
					addedAdsCount = (position + 1) / (m_rotationAdUnit + 1);
				}
			}
			// 배너가 있는 경우
			else
			{
				if (m_isFirstPageAds)
				{
					if (position > m_firstAdIndex)
						addedAdsCount = 1;

					addedAdsCount += (position + 1 - addedNewFeatureCount - addedAdsCount) / (m_rotationAdUnit + 1);
				} else
				{
					addedAdsCount = (position + 1 - addedNewFeatureCount) / (m_rotationAdUnit + 1);
				}
			}
		}

		//Log.d("[BACKGROUNDS ADAPTER] onClickBackground() position " + position
		//		+ ", addedAdsCount: " + addedAdsCount
		//		+ ", addedNewFeatureCount: " + addedNewFeatureCount
		//		+ ", isTopBanner: " + isTopBanner()
		//		+ ", isTopBannerOpen: " + isTopBannerOpen());
		int index = position - addedAdsCount;
		//Log.d("cbk", "onClickBackground index: " + index + ", position : " + position);
		if (index < 0)
		{
			index = 0;
		} else if (index >= m_data.getBackgroundsList().size() && m_data.getBackgroundsList().size() > 0)
		{
			index = m_data.getBackgroundsList().size() - 1;
		}

		m_data.setIndex(index);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			final View heroView = view.findViewById(R.id.image);
			ViewTransitionHelper.getInstance().setHeroView(heroView);
		}

		onOpenBackground(this);

		try
		{
			final String api = getApiForStats();
			AnalyticsManager.getInstance().eventStatsApiIndex(getContext(), api, position);

			String ClassName = getClass().getSimpleName();
			if (ClassName != null && ClassName.length() > 0 && ClassName.equals("RecentFragment"))
			{
				switch (index)
				{
					case 0:
						AnalyticsManager.getInstance().RecentEvent(getContext(), "Thumbnail1_Recent", null);
						break;
					case 1:
						AnalyticsManager.getInstance().RecentEvent(getContext(), "Thumbnail2_Recent", null);
						break;
					case 2:
						AnalyticsManager.getInstance().RecentEvent(getContext(), "Thumbnail3_Recent", null);
						break;
					case 3:
						AnalyticsManager.getInstance().RecentEvent(getContext(), "Thumbnail4_Recent", null);
						break;
					case 4:
						AnalyticsManager.getInstance().RecentEvent(getContext(), "Thumbnail5_Recent", null);
						break;
					case 5:
						AnalyticsManager.getInstance().RecentEvent(getContext(), "Thumbnail6_Recent", null);
						break;
				}
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

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

	protected String getDataUrl()
	{
		final String dataUrl = getArguments().getString(KEY_DATA_URL);

		if (TextUtils.isEmpty(dataUrl) == true)
		{
			throw new IllegalArgumentException("KEY_DATA_URL == null");
		}

		return dataUrl;
	}

	protected String getExtra(Background background)
	{
		return null;
	}

	protected void clearData()
	{
		if (m_data != null && m_data.getBackgroundsList() != null)
		{
			m_data.getBackgroundsList().clear();
			m_data.setBackgroundsList(null);
			m_mergeAdapter.notifyDataSetChanged();
		}
	}

	protected boolean isNew(Background background)
	{
		return false;
	}

	protected void updateHeaderViews()
	{
		final ViewGroup headersView = (ViewGroup) m_mergeAdapter.findViewById(R.id.headers);

		if (m_data.getHeadersList() == null || m_data.getHeadersList().size() == 0)
		{
			headersView.removeAllViews();
		} else if (headersView.getChildCount() == 0)
		{
			HeadersInflater.inflate(this, getLayoutInflater(), m_data.getHeadersList(), headersView);

			final TextView titleView = headersView.findViewWithTag("actionbar_title");

			if (titleView != null)
			{
				final String title = titleView.getText().toString();
				getToolbar().setTitle(title);
			}
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

	private void onStartHelper()
	{
		if (getUserVisibleHint() == true && getView() != null)
		{
			if (m_data.isLoaded() == false)
			{
				loadData();
			} else
			{
				m_pageScrollAdapter.check(m_listView);
			}
		}
	}

	private String getApiForStats()
	{
		return getDataUrl().replaceFirst("https?://bgh.ogqcorp.com/api/v4/", "");
	}

	private void loadData()
	{
		try
		{
			final String api = getApiForStats();
			AnalyticsManager.getInstance().eventStatsApi(getContext(), api);
		}
		catch (Exception e)
		{
			// Nothing
		}

		String dataUrl = getDataUrl();

		if (UrlFactory.recent().equals(dataUrl) == true)
		{
			NetSpeedChecker.getInstance().start(getContext(), NetSpeedChecker.TYPE_RECENT);
		}

		if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true)
			return;

		try
		{
			Request request = m_data.request(dataUrl, m_response, m_errorResponse);
			if (request == null)
			{
				if (m_errorResponse != null)
					m_errorResponse.onErrorResponse(new VolleyError(getString(R.string.error_code_4xx)));
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void loadDataNext()
	{
		if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true)
			return;

		try
		{
			Request request = m_data.requestNext(m_response, m_errorResponse);

			if (request == null)
			{
				if (m_errorResponse != null)
					m_errorResponse.onErrorResponse(new VolleyError(getString(R.string.error_code_4xx)));
			}

			AnalyticsManager.getInstance().RecentEvent(getContext(), "Pages_Recent", null);
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

			view.setVisibility(isShow ? View.VISIBLE : View.GONE);
		}
		catch (Exception e)
		{
			//Log.e(e);
		}
	}

	private void showErrorDialog(final Exception e)
	{
		ErrorDialogFragment.start(getChildFragmentManager(), e, new ErrorDialogFragment.DialogCallback()
		{
			@Override
			public void onShow(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

				try
				{
					if (e.toString().contains("hostname") == true)
					{
						SlackMessage.sendMessageForHostnameIssue(getActivity(), getDataUrl()/**/);
					} else if (e instanceof ParseErrorEx)
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
				if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

				refresh();
			}
		});
	}

	private void keepContext()
	{
		final int index = m_data.getIndex();

		if (index != BackgroundsModelData.INVALID_INDEX)
		{
			m_listView.post(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (m_listView != null)
						{
							int addedAdsCount = 0;

							if (m_isAvailableNativeAds)
							{
								addedAdsCount = (index + 1 - 1) / (m_rotationAdUnit + 1);
							}

							m_listView.scrollToPosition(index + getHeaderCount() + addedAdsCount);
						}
					}
					catch (Exception e)
					{
						// Nothing
					}
				}
			});

			m_data.setIndex(BackgroundsModelData.INVALID_INDEX);
		}
	}

	private void setRandomHeaderBackground(Backgrounds backgrounds)
	{
		if (backgrounds.getHeadersList() == null || backgrounds.getBackgroundsList().size() == 0)
			return;

		for (Header header : backgrounds.getHeadersList())
		{
			if (header == null) continue;
			if (header.getBackground() == null || TextUtils.isEmpty(header.getBackground().getUrl()) == true)
			{
				int size = backgrounds.getBackgroundsList().size();
				int rdm = (int) (Math.random() * size);

				Background background = backgrounds.getBackgroundsList().get(rdm);

				header.setBackground(background.getImagesList().get(1));
			}
		}
	}

	private boolean checkIsAvailableNativeAds()
	{
		return AdCheckManager.getInstance().isAvailableNativeAds();
	}

	private void refresh()
	{
		showProgress(true);
		m_data.invalidate();
		loadData();
	}

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<Backgrounds> m_response = new Response.Listener<Backgrounds>()
	{
		@Override
		public void onResponse(Backgrounds backgrounds)
		{
			if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

			if (UrlFactory.recent().equals(getDataUrl()) == true)
			{
				NetSpeedChecker.getInstance().end(getContext(), NetSpeedChecker.TYPE_RECENT, backgrounds);
			}

			setRandomHeaderBackground(backgrounds);

			if (m_data.hasNext() == false)
			{
				showProgress(false);
			}

			updateHeaderViews();
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
			if (FragmentUtils.isDestroyed(BackgroundsFragment.this) == true) return;

			if (m_swipeRefreshLayout != null && m_swipeRefreshLayout.isRefreshing() == true)
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

			//Log.d("[BACKGROUNDS ADAPTER] getSpanSize(" + position + ") -> isTopBanner: " + isTopBanner() + ", isNewFeatureOpen: " + isNewFeatureOpen());

			if (StaticViewAdapter.isStaticView(viewType) == true)
			{
				//Log.d("[BACKGROUNDS ADAPTER] getSpanSize(" + position + ") -> isStaticView is true => " + m_layout.getSpanCount());
				return m_layout.getSpanCount();
			}

			if (isTopBanner() == true && isTopBannerOpen() == true)
			{
				//Log.d("[BACKGROUNDS ADAPTER] getSpanSize(" + position + ") NEW-> " + m_layout.getSpanCount());
				if (hasHeaderTags() == false && position == 1)
					return m_layout.getSpanCount();

				if (hasHeaderTags() == true && position == 2)
					return m_layout.getSpanCount();
			}

			int adCheckSum = (isTopBanner() == true && isTopBannerOpen() == true) ? 1 : 0;

			if (hasHeaderTags() == true)
			{
				adCheckSum++;
			}

			if (position != 0 && position % (m_rotationAdUnit + 1) == adCheckSum && m_isAvailableNativeAds && m_isFirstPageAds == false)
			{
				//Log.d("[BACKGROUNDS ADAPTER] getSpanSize(" + position + ") FB-> " + m_layout.getSpanCount());
				return m_layout.getSpanCount();
			} else if (m_isFirstPageAds && m_isAvailableNativeAds)
			{
				int addedAdsIndex = 0;

				// 배너가 없는 경우
				if (adCheckSum == 0)
				{
					if (position > m_firstAdIndex + 1)
						addedAdsIndex = 1;

					if (position == m_firstAdIndex + 1)
					{
						return m_layout.getSpanCount();
					} else if (position > m_firstAdIndex + 1 && (position - addedAdsIndex) % (m_rotationAdUnit + 1) == 0)
					{
						return m_layout.getSpanCount();
					}
				} else
				{
					if (position - adCheckSum > m_firstAdIndex + 1)
						addedAdsIndex = 1;

					if (position - adCheckSum == m_firstAdIndex + 1)
					{
						return m_layout.getSpanCount();
					} else if (position - adCheckSum > m_firstAdIndex + 1 && (position - adCheckSum - addedAdsIndex) % (m_rotationAdUnit + 1) == 0)
					{
						return m_layout.getSpanCount();
					}
				}

			}

			return 1;
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private BackgroundsAdapter m_adapter = new BackgroundsAdapter()
	{
		@Override
		public int getItemViewType(int position)
		{
			if (position == 0 && isTopBanner() == true && isTopBannerOpen() == true)
			{
				//Log.d("[BACKGROUNDS ADAPTER] getItemViewType(" + position + ") -> item_top_banner");
				return R.layout.item_top_banner;
			} else if (getBackground(position).isNativeAd())
			{
				//Log.d("[BACKGROUNDS ADAPTER] getItemViewType(" + position + ") -> item_background_native_ads");
				return R.layout.item_background_native_ads;
			}

			//Log.d("[BACKGROUNDS ADAPTER] getItemViewType(" + position + ") -> item_background");
			return R.layout.item_background;
		}

		@Override
		public int getItemCount()
		{
			if (m_data.isLoaded() == false)
			{
				//Log.d("[BACKGROUNDS ADAPTER] getItemCount() -> " + 0);
				return 0;
			}

			int addedAdsCount = 0;

			if (m_isAvailableNativeAds)
			{
				addedAdsCount = m_data.getBackgroundsList().size() / m_rotationAdUnit; // TODO : Need check

				if (m_isFirstPageAds)
				{
					if (m_data.getBackgroundsList().size() >= m_firstAdIndex)
						addedAdsCount += 1;
				}
			}

			if (isTopBanner() == true && isTopBannerOpen() == true)
			{
				addedAdsCount += 1;
			}

			//Log.d("CBK","getItemCount() size " + m_data.getFeedList().size()
			//		+ ", addedAdsCount: " + addedAdsCount);

			return m_data.getBackgroundsList().size() + addedAdsCount;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position)
		{
			onBindViewHolder(getActivity(), getBackground(position), holder, position);
		}

		@Override
		protected void onClickBackground(View view, Background background)
		{
			BackgroundsFragment.this.onClickBackground(view, background);
		}

		@Override
		protected void onClickAdFree()
		{
			BackgroundsFragment.this.onClickAdFree();
		}

		@Override
		protected void onClickTopBanner(View view)
		{
			BackgroundsFragment.this.onClickTopBanner(view, this);
		}

		@Override
		protected void onClickTopBannerClose(View view)
		{
			BackgroundsFragment.this.onClickTopBannerClose(view, this);
		}

		@Override
		protected boolean isTopBannerOpen()
		{
			return BackgroundsFragment.this.isTopBannerOpen();
		}

		@Override
		protected TopBanner getTopBanner()
		{
			return BackgroundsFragment.this.getTopBanner();
		}

		@Override
		protected String getExtra(Background background)
		{
			return BackgroundsFragment.this.getExtra(background);
		}

		@Override
		protected ArrayList<IntegrateNativeAd> getBackgroundNativeAdsList()
		{
			return m_nativeAds;
		}

		@Override
		protected boolean isNewBackground(Background background)
		{
			return isNew(background);
		}

		private Background getBackground(int position)
		{
			if (m_data.isLoaded() == false) return null;

			if (position == 0 && isTopBanner() == true && isTopBannerOpen() == true)
			{
				//Log.d("[BACKGROUNDS ADAPTER] getBackground(" + position + ")");
				return null;
			}

			int addedAdsCount = 0;
			int addedNewFeatureCount = 0;

			if (isTopBanner() == true && isTopBannerOpen() == true)
			{
				addedNewFeatureCount = 1;
			}

			m_isAvailableNativeAds = checkIsAvailableNativeAds();

			if (m_isAvailableNativeAds)
			{
				// 배너가 없는 경우
				if (addedNewFeatureCount == 0)
				{
					// 4번째 index에 광고 표시(3번째 줄)
					if (m_isFirstPageAds)
					{
						if (position == m_firstAdIndex)
						{
							Background b = new Background();
							b.setIsNativeAd(true);
							return b;
						}

						// addedAdsCount는 3번째 줄 광고 추가
						// nextindex는 3번째 줄에 광고가 추가됨에 따라 2개(Tablet 4개)의 position이 밀림
						if (position > m_firstAdIndex)
							addedAdsCount = 1;

						if ((position > m_firstAdIndex) && (position + 1 - addedAdsCount) % (m_rotationAdUnit + 1) == 0)
						{
							Background b = new Background();
							b.setIsNativeAd(true);
							return b;
						}

						addedAdsCount += (position + 1 - addedAdsCount) / (m_rotationAdUnit + 1);
					} else
					{
						if ((position + 1) % (m_rotationAdUnit + 1) == 0)
						{
							Background b = new Background();
							b.setIsNativeAd(true);
							return b;
						}

						addedAdsCount = (position + 1) / (m_rotationAdUnit + 1);
					}
				}
				// 배너가 있는 경우
				else
				{
					if (m_isFirstPageAds)
					{
						if (position - addedNewFeatureCount == m_firstAdIndex)
						{
							Background b = new Background();
							b.setIsNativeAd(true);
							return b;
						}

						// addedAdsCount는 3번째 줄 광고 추가
						// nextindex는 3번째 줄에 광고가 추가됨에 따라 2개(Tablet 4개)의 position이 밀림
						if (position - addedNewFeatureCount > m_firstAdIndex)
							addedAdsCount = 1;

						if ((position - addedNewFeatureCount > m_firstAdIndex) && (position + 1 - addedNewFeatureCount - addedAdsCount) % (m_rotationAdUnit + 1) == 0)
						{
							Background b = new Background();
							b.setIsNativeAd(true);
							return b;
						}

						addedAdsCount += (position + 1 - addedNewFeatureCount - addedAdsCount) / (m_rotationAdUnit + 1);
					} else
					{
						if ((position + 1) % (m_rotationAdUnit + 1) == 1)
						{
							Background b = new Background();
							b.setIsNativeAd(true);
							return b;
						}
						addedAdsCount = (position + 1 - addedNewFeatureCount) / (m_rotationAdUnit + 1);
					}
				}
			}
/*
			Log.d("CBK", "getBackground() isFacebook: " + m_isAvailableNativeAds
					+ ", addedNewFeatureCount: " + addedNewFeatureCount
					+ ", position: " + position
					+ ", addedAdsCount: " + addedAdsCount);
*/
			int index = position - (addedAdsCount + addedNewFeatureCount);
			//Log.d("cbk","position: " + position + ", addedAdsCount: " + addedAdsCount + ", addedNewFeatureCount: " + addedNewFeatureCount + ", index: " + index + ", size : " + (m_data
			//	.getFeedList().size() - 1));
			//if(index == 479 || index == 480)
			//{
			//	String msgtemp = "BackgroundsFragment position: " + position + ", size: " + (m_data.getFeedList().size() - 1) + ", addedAdsCount: " + addedAdsCount + " , " +
			//	"addedNewFeatureCount: " +
			//			addedNewFeatureCount + " , index: " + index;
			//	Log.d("cbk",msgtemp);
			//}

			if (index < 0)
			{
				String msg = "BackgroundsFragment wrong index: " + index + ", Index: 0";
				//Log.e("cbkCrash",msg);
				FirebaseCrashLog.logException(new Throwable(msg));
				index = 0;
			} else if (index >= m_data.getBackgroundsList().size() && m_data.getBackgroundsList().size() > 0)
			{
				String msg = "BackgroundsFragment wrong index: " + index + ", Index: " + (m_data.getBackgroundsList().size() - 1);
				//Log.e("cbkCrash",msg);
				FirebaseCrashLog.logException(new Throwable(msg));
				index = m_data.getBackgroundsList().size() - 1;
			}

			return m_data.getBackgroundsList().get(index);
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
			return m_data.hasNext();
		}

		@Override
		protected boolean isLoading()
		{
			return m_data.isLoading();
		}

		@Override
		protected int findLastVisibleItemPosition()
		{
			return m_layout.findLastVisibleItemPosition();
		}
	};

	//========================================================================
	// Likes Listener
	//========================================================================

	@Override
	public void onSuccess()
	{
		try
		{
			m_mergeAdapter.notifyDataSetChanged();
			updateHeaderViews();
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	@Override
	public void onFail(Exception e)
	{
		try
		{
			m_mergeAdapter.notifyDataSetChanged();
		}
		catch (Exception ee)
		{
			// Nothing
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_DATA_URL = "KEY_DATA_URL";
	protected static final int HEADER_COUNT = 1;

	public static final int FIRST_NATIVE_AD_INDEX = 6;
	public static final int FIRST_NATIVE_AD_INDEX_FOR_TABLET = 12;

	//=========================================================================
	// Variables
	//=========================================================================

	public @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	private int m_rotationAdUnit;
	private int m_firstAdIndex;
	private Unbinder m_unbinder;
	private GridLayoutManager m_layout;
	private BackgroundsModelData m_data;
	private ArrayList<IntegrateNativeAd> m_nativeAds;

	private boolean m_isAvailableNativeAds = false;
	private boolean m_isFirstPageAds = true;

	protected MergeRecyclerAdapter m_mergeAdapter;
}