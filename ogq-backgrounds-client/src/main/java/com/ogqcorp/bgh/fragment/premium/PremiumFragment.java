package com.ogqcorp.bgh.fragment.premium;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import java.util.List;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.cardview.widget.CardView;
import androidx.gridlayout.widget.GridLayout;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.GenericTransitionOptions;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.fragment.base.BaseLayoutFragmentEx;
import com.ogqcorp.bgh.gallery.GalleryFragment;
import com.ogqcorp.bgh.gallery.GalleryRecentFragment;
import com.ogqcorp.bgh.model.BackgroundsModel;
import com.ogqcorp.bgh.model.BackgroundsModelData;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Gallery;
import com.ogqcorp.bgh.spirit.data.Premium;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public final class PremiumFragment extends BaseLayoutFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public PremiumFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		m_data = BackgroundsModel.getInstance().get(this, new BaseModel.DataCreator<BackgroundsModelData>()
		{
			@Override
			public BackgroundsModelData newInstance()
			{
				return new BackgroundsModelData();
			}
		});

		BackgroundsModel.getInstance().update(m_data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_premium, container, false);
	}

	@Override
	public void onViewCreated(View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
		{
			m_premium = savedInstanceState.getParcelable(KEY_PREMIUM_INFO);
		}

		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		if (m_premium == null)
		{
			loadData();
		} else
		{
			initView();
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);
		}
		catch (Exception e)
		{
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
		outState.putParcelable(KEY_PREMIUM_INFO, m_premium);
	}

	@Override
	protected void onInitActionBar()
	{
		setActionBarAlpha(isOverlayActionBar() ? 0 : 255);

		Toolbar toolbar = getToolbar();

		if (toolbar != null && isAdded() == true && getUserVisibleHint() == true)
		{
			int color = getResources().getColor(R.color.black);

			toolbar.setBackgroundResource(R.drawable.actionbar_bg);
			if (toolbar.getOverflowIcon() != null)
			{
				toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			toolbar.setElevation(0);
		}
	}

	@Override
	public void onRefresh()
	{
		m_premium = null;
		loadData();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

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
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public void setCurrentItem(int num, boolean smoothScroll)
	{
		m_BannerViewPager.setCurrentItem(num, smoothScroll);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		final Fragment fragment = new PremiumFragment();
		final long dataKey = DATA_KEY;
		return BaseModel.wrap(fragment, dataKey);

		//	return new PremiumFragment();
	}

	@OnClick({ R.id.text_video_all, R.id.btn_video_all })
	protected void onClickViewAllLiveScreen()
	{
		try
		{
			AnalyticsManager.getInstance().premiumEvent(getContext(), "LiveWallPaperMore_Premium");

			String url = m_premium.getLiveScreenUrl();
			onOpenBackgrounds(url);
		}
		catch (Exception e)
		{
		}
	}

	@OnClick({ R.id.text_watch_all, R.id.btn_watch_all })
	protected void onClickViewAllLiveWatch()
	{
		try
		{
			AnalyticsManager.getInstance().premiumEvent(getContext(), "LiveWatchMore_Premium");

			String url = m_premium.getLiveWatchUrl();
			onOpenBackgrounds(url);
		}
		catch (Exception e)
		{
		}
	}

	@OnClick({ R.id.text_gallery_all, R.id.btn_gallery_all })
	protected void onClickViewAllGallery()
	{
		try
		{
			AnalyticsManager.getInstance().premiumEvent(getContext(), "GalleryMore_Premium");

			final String url = UrlFactory.galleryRecentList();
			if (TextUtils.isEmpty(url) == false)
			{
				final Fragment fragment = GalleryRecentFragment.newInstance();
				AbsMainActivity.getTabStackHelper(this).showFragment(fragment);
			}
		}
		catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void loadData()
	{
		if (FragmentUtils.isDestroyed(PremiumFragment.this) == true)
			return;

		if (m_isLoading == true)
			return;

		try
		{
			m_isLoading = true;
			String url = UrlFactory.getPremium();
			if (UserManager.getInstance().isGuest() == true)
			{
				Requests.requestByGet(url, Premium.class, m_response, m_errorResponse);
			} else
			{
				Requests.authRequestByGet(url, Premium.class, m_response, m_errorResponse);
			}

		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void initView()
	{
		constructBanner();
		constructLiveScreen();
		constructLiveWatch();
		constructGallery();
	}

	private void constructBanner()
	{
		if (m_premium == null || m_premium.getBanners() == null || m_premium.getBanners().isEmpty() == true)
		{
			m_bannerContainer.setVisibility(View.GONE);
			return;
		}

		PremiumBannerAdapter adapter = new PremiumBannerAdapter(getContext(), m_premium.getBanners());
		m_BannerViewPager.setAdapter(adapter);
		m_BannerViewPager.setOffscreenPageLimit(1);

		m_tabs.setupWithViewPager(m_BannerViewPager);
		m_bannerContainer.setVisibility(View.VISIBLE);
	}

	private void constructLiveScreen()
	{
		if (m_premium == null || m_premium.getLiveScreens() == null || m_premium.getLiveScreens().isEmpty() == true)
		{
			m_liveScreenContainer.setVisibility(View.GONE);
			return;
		}

		final List<Background> liveScreens = m_premium.getLiveScreens();
		m_liveScreenContainer.setVisibility(View.VISIBLE);

		int rowCount = DeviceUtils.isTablet(getContext()) == true ? 2 : 2;
		int columnCount = DeviceUtils.isTablet(getContext()) == true ? 3 : 3;

		m_liveScreenSample.setRowCount(rowCount);
		m_liveScreenSample.setColumnCount(columnCount);
		m_liveScreenSample.setUseDefaultMargins(true);

		m_liveScreenSample.removeAllViews();
		m_liveScreenProgress.setVisibility(View.GONE);

		int maxSampleCount = Math.min(MAX_LIVESCREEN_COUNT, liveScreens.size());

		int sidePadding = DisplayManager.getInstance().getPixelFromDp(getContext(), 16 * 2);
		int displayWidth = DisplayManager.getInstance().getDisplaySize(getContext()).x;
		int thumbnailSpace = DisplayManager.getInstance().getPixelFromDp(getContext(), 8);

		int size = (displayWidth - sidePadding - columnCount * thumbnailSpace) / columnCount;

		for (int i = 0; i < maxSampleCount; i++)
		{
			final ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_similar, m_liveScreenSample, false);

			final ImageView image = viewGroup.findViewById(R.id.image);
			final Background background = liveScreens.get(i);

			final int index = i;
			viewGroup.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						AnalyticsManager.getInstance().premiumEvent(getContext(), "LiveWallPaper" + (index + 1) + "_Premium");
						setBackgroundsModelData(liveScreens, index);
						onOpenBackground(PremiumFragment.this);
					}
					catch (Exception e)
					{
						ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
					}
				}
			});

			GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED));
			lp.height = lp.width = size;

			m_liveScreenSample.addView(viewGroup, lp);

			if (getUserVisibleHint() == true)
			{
				viewGroup.setAlpha(0);
				viewGroup.animate()
						.alpha(1)
						.setDuration(300)
						.setStartDelay(200)
						.start();
			}

			GlideApp.with(this)
					.load(background.getThumbnail().getUrl())
					.centerCrop()
					.transition(GenericTransitionOptions.with(R.anim.short_fade_in))
					.placeholder(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.grey_300)))
					.transition(withCrossFade(500))
					.into(image);
		}
	}

	private void constructLiveWatch()
	{
		if (m_premium == null || m_premium.getLiveScreens() == null || m_premium.getLiveWatches().isEmpty() == true)
		{
			m_liveWatchContainer.setVisibility(View.GONE);
			return;
		}

		final List<Background> liveWatches = m_premium.getLiveWatches();

		m_liveWatchContainer.setVisibility(View.VISIBLE);

		int rowCount = DeviceUtils.isTablet(getContext()) == true ? 1 : 2;
		int columnCount = DeviceUtils.isTablet(getContext()) == true ? 4 : 2;

		m_liveWatchSample.setRowCount(rowCount);
		m_liveWatchSample.setColumnCount(columnCount);

		m_liveWatchSample.removeAllViews();
		m_liveWatchProgress.setVisibility(View.GONE);

		int maxSampleCount = Math.min(MAX_LIVEWATCH_COUNT, liveWatches.size());

		int sidePadding = DisplayManager.getInstance().getPixelFromDp(getContext(), 16 * 2);
		int displayWidth = DisplayManager.getInstance().getDisplaySize(getContext()).x;
		int thumbnailSpace = DisplayManager.getInstance().getPixelFromDp(getContext(), 8);

		int size = (displayWidth - sidePadding - columnCount * thumbnailSpace) / columnCount;

		for (int i = 0; i < maxSampleCount; i++)
		{
			final ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_similar, m_liveWatchSample, false);

			final ImageView image = viewGroup.findViewById(R.id.image);
			final Background background = liveWatches.get(i);

			final int index = i;
			viewGroup.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						AnalyticsManager.getInstance().premiumEvent(getContext(), "LiveWatch" + (index + 1) + "_Premium");
						setBackgroundsModelData(liveWatches, index);
						onOpenBackground(PremiumFragment.this);
					}
					catch (Exception e)
					{
						ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
					}
				}
			});

			GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED));
			lp.height = lp.width = size;

			m_liveWatchSample.addView(viewGroup, lp);

			if (getUserVisibleHint() == true)
			{
				viewGroup.setAlpha(0);
				viewGroup.animate()
						.alpha(1)
						.setDuration(300)
						.setStartDelay(200)
						.start();
			}

			GlideApp.with(this)
					.load(background.getThumbnail().getUrl())
					.centerCrop()
					.transition(GenericTransitionOptions.with(R.anim.short_fade_in))
					.placeholder(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.grey_300)))
					.transition(withCrossFade(500))
					.into(image);
		}
	}

	private void constructGallery()
	{
		if (m_premium == null || m_premium.getLiveScreens() == null || m_premium.getGalleries().isEmpty() == true)
		{
			m_galleryContainer.setVisibility(View.GONE);
			return;
		}

		List<Gallery> galleries = m_premium.getGalleries();
		m_galleryContainer.setVisibility(View.VISIBLE);

		int rowCount = DeviceUtils.isTablet(getContext()) == true ? 1 : 2;
		int columnCount = DeviceUtils.isTablet(getContext()) == true ? 4 : 2;

		m_gallerySample.setRowCount(rowCount);
		m_gallerySample.setColumnCount(columnCount);

		m_gallerySample.removeAllViews();
		m_galleryProgress.setVisibility(View.GONE);

		int maxSampleCount = Math.min(MAX_GALLERY_COUNT, galleries.size());

		int sidePadding = DisplayManager.getInstance().getPixelFromDp(getContext(), 16 * 2);
		int displayWidth = DisplayManager.getInstance().getDisplaySize(getContext()).x;
		int thumbnailSpace = DisplayManager.getInstance().getPixelFromDp(getContext(), 8);

		int size = (displayWidth - sidePadding - columnCount * thumbnailSpace) / columnCount;

		for (int i = 0; i < maxSampleCount; i++)
		{
			try
			{
				final Gallery gallery = galleries.get(i);

				final ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_premium_gallery, m_gallerySample, false);
				final CardView card = viewGroup.findViewById(R.id.card);
				final TextView title = viewGroup.findViewById(R.id.title);
				final TextView sub_title = viewGroup.findViewById(R.id.sub_title);
				final TextView tag1 = viewGroup.findViewById(R.id.tag1);
				final TextView tag2 = viewGroup.findViewById(R.id.tag2);
				final TextView tag3 = viewGroup.findViewById(R.id.tag3);
				final ImageView image = viewGroup.findViewById(R.id.image);
				final TextView type = viewGroup.findViewById(R.id.type);
				final TextView name = viewGroup.findViewById(R.id.name);
				final ImageView lock = viewGroup.findViewById(R.id.lock);

				title.setText(gallery.getTitle());
				sub_title.setText(gallery.getSubTitle());

				if (TextUtils.isEmpty(gallery.getCoverUrl()) == false)
				{
					StringBuffer url = new StringBuffer().append(gallery.getCoverUrl()).append("?type=h1280");

					GlideApp.with(getContext())
							.asBitmap()
							.load(url.toString())
							.into(image);
				}

				if (TextUtils.isEmpty(gallery.getTags()) == false)
				{
					String[] tags = gallery.getTags().split(",");
					for (int index = 0; index < tags.length; index++)
					{
						switch (index)
						{
							case 0:
								tag1.setText("#" + tags[index]);
								tag1.setVisibility(View.VISIBLE);
								break;
							case 1:
								tag2.setText("#" + tags[index]);
								tag2.setVisibility(View.VISIBLE);
								break;
							case 2:
								tag3.setText("#" + tags[index]);
								tag3.setVisibility(View.VISIBLE);
								break;
						}
					}
				}

				User user = gallery.getUser();

				if (user != null)
				{
					type.setText(user.getArtistType());
				}

				if (user != null)
				{
					name.setText(user.getName());
				}

				if (lock != null)
				{
					lock.setVisibility(gallery.getPublished() ? View.GONE : View.VISIBLE);
				}

				final int index = i;
				card.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						try
						{
							AnalyticsManager.getInstance().premiumEvent(getContext(), "Gallery" + (index + 1) + "_Premium");

							final Fragment fragment = GalleryFragment.newInstance(gallery.getId());
							AbsMainActivity.getTabStackHelper(PremiumFragment.this).showFragment(fragment);
						}
						catch (Exception e)
						{
							ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
						}
					}
				});

				GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED));
				lp.width = size;
				lp.height = (int) (size * 7.0 / 4.0);    //w : h = 4 : 7 ratio

				m_gallerySample.addView(viewGroup, lp);

				if (getUserVisibleHint() == true)
				{
					viewGroup.setAlpha(0);
					viewGroup.animate()
							.alpha(1)
							.setDuration(300)
							.setStartDelay(200)
							.start();
				}
			}
			catch (Exception e)
			{
				// Nothing
			}
		}
	}

	private void setBackgroundsModelData(final List<Background> dataList, int position)
	{
		if (m_data.getBackgroundsList() != null)
		{
			m_data.setBackgroundsList(null);
		}

		m_data.setBackgroundsList(dataList);
		BackgroundsModel.getInstance().update(m_data);
		m_data.setIndex(position);
	}

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<Premium> m_response = new Response.Listener<Premium>()
	{
		@Override
		public void onResponse(Premium premium)
		{
			if (FragmentUtils.isDestroyed(PremiumFragment.this) == true) return;

			m_isLoading = false;

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
				m_rootContainer.scrollTo(0, 0);
				//showActionBarSlide(true, false);
			}

			m_premium = premium;
			initView();
		}

	};

	protected Response.ErrorListener m_errorResponse = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError volleyError)
		{
			if (FragmentUtils.isDestroyed(PremiumFragment.this) == true) return;

			m_isLoading = false;

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
			}

			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_PREMIUM_INFO = "KEY_PREMIUM_INFO";
	public static final long DATA_KEY = 2019;

	private static final int MAX_LIVESCREEN_COUNT = 6;
	private static final int MAX_LIVEWATCH_COUNT = 4;
	private static final int MAX_GALLERY_COUNT = 4;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.scroll) ViewGroup m_rootContainer;
	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	@BindView(R.id.banner_container) ViewGroup m_bannerContainer;
	@BindView(R.id.banner_view_pager) ViewPager m_BannerViewPager;
	@BindView(R.id.banner_tabs) TabLayout m_tabs;

	@BindView(R.id.video_container) ConstraintLayout m_liveScreenContainer;
	@BindView(R.id.video_samples) GridLayout m_liveScreenSample;
	@BindView(R.id.video_loading_progress) View m_liveScreenProgress;

	@BindView(R.id.watch_container) ConstraintLayout m_liveWatchContainer;
	@BindView(R.id.watch_samples) GridLayout m_liveWatchSample;
	@BindView(R.id.watch_loading_progress) View m_liveWatchProgress;

	@BindView(R.id.gallery_container) ConstraintLayout m_galleryContainer;
	@BindView(R.id.gallery_samples) GridLayout m_gallerySample;
	@BindView(R.id.gallery_loading_progress) View m_galleryProgress;

	private boolean m_isLoading;

	private Premium m_premium;
	private PremiumBannerAdapter m_adapter;
	private BackgroundsModelData m_data;
	private Unbinder m_unbinder;
}