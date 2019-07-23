package com.ogqcorp.bgh.fragment;

import java.io.File;
import java.util.ArrayList;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.adapter.ImageBoxAdapter;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.imagewarehouse.ImageWarehouse;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.MediaBox;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by ogq on 2017. 7. 13..
 */

public class UploadImageBoxFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
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
			m_mode = bundle.getInt(UploadActivity.UPLOAD_MODE, UploadActivity.MODE_IMAGE);
			m_imageBoxId = bundle.getString(IMAGE_BOX_ID);
			m_wepickId = bundle.getString(WEPICK_ID);
		}

		m_imageBox = ImageWarehouse.getInstance().getImageBox(m_imageBoxId);

		m_rotationAdUnit = PreferencesManager.getInstance().getCountryCountAdList(getContext());
		m_firstAdIndex = DeviceUtils.isTablet(getContext()) ? FIRST_NATIVE_AD_INDEX_FOR_TABLET : FIRST_NATIVE_AD_INDEX;

		//AdCheckManager.getInstance().checkNativeAds(new AdCheckManager.AdAvailabilityCallback()
		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				m_nativeAds = AdCheckManager.getInstance().getShuffledNativeAdList(UploadImageBoxFragment.this);

				if (m_nativeAds == null || m_nativeAds.size() == 0)
				{
					onNotAvailable();
				}
				else
				{
					m_isAvailableNativeAds = true;
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
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_image_boxes_empty);
		m_listView.setAdapter(m_mergeAdapter);

		updateHeaderViews();

		initWarehouse();

		if (savedInstanceState == null || ImageWarehouse.getInstance().size() <= 0)
		{
			loadData();
		}
	}

	@Override
	protected void onInitActionBar()
	{
		super.onInitActionBar();

		if (m_imageBox == null)
			return;

		setToolbarTitle();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onResume()
	{
		super.onResume();
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

		ImageWarehouse.getInstance().removeCallback(m_callback);

		AdCheckManager.getInstance().removeShuffledNativeAdList(this);
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
	protected int getFirstVisiblePosition()
	{
		return m_layout.findFirstVisibleItemPosition();
	}

	@Override
	public void onRefresh()
	{
		loadData();
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Fragment newInstance(String imageBoxId, int mode, String wepickId)
	{
		final Fragment fragment = new UploadImageBoxFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(UploadActivity.UPLOAD_MODE, mode);
		bundle.putString(WEPICK_ID, wepickId);
		bundle.putString(IMAGE_BOX_ID, imageBoxId);
		fragment.setArguments(bundle);
		return BaseModel.wrap(fragment);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initWarehouse()
	{
		m_callback = new ImageWarehouse.StorageCallback()
		{
			@Override
			public void onCompleted()
			{
				if (FragmentUtils.isDestroyed(UploadImageBoxFragment.this) == true)
				{
					return;
				}

				m_imageBox = ImageWarehouse.getInstance().getImageBox(m_imageBoxId);

				if (m_imageBox != null)
				{
					setToolbarTitle();

					if (m_imageBox.getImages() == null || m_imageBox.getImages().size() <= 0)
					{
						showEmptyView(true);
					}
					else
					{
						showEmptyView(false);
					}
				}
				else
				{
					showEmptyView(true);
				}
				m_swipeRefreshLayout.setRefreshing(false);
				m_mergeAdapter.notifyDataSetChanged();
			}

			@Override
			public void onForceRefreshing()
			{
				if (FragmentUtils.isDestroyed(UploadImageBoxFragment.this) == true)
				{
					return;
				}
				m_mergeAdapter.notifyDataSetChanged();
				m_swipeRefreshLayout.setRefreshing(true);
			}
		};

		ImageWarehouse.getInstance().init(getContext());
		ImageWarehouse.getInstance().setCallback(m_callback);

		if (m_imageBox == null || m_imageBox.getImages() == null || m_imageBox.getImages().size() <= 0)
		{
			showEmptyView(true);
		}
		else
		{
			showEmptyView(false);
		}
	}

	private int getSpanCount()
	{
		final Context context = getActivity();

		//return DeviceUtils.isLandscape(context) == false ? 2 : 4;
		return DeviceUtils.isTablet(context) == true ? 4 : 2;
	}

	private void updateHeaderViews()
	{
		final ViewGroup headersView = (ViewGroup) m_mergeAdapter.findViewById(R.id.headers);

		headersView.removeAllViews();
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

	private void onClickImage(int position)
	{
		if (ImageWarehouse.getInstance().isLoading() == false)
		{
			int contentPosition = getContentPosition(position);
			uploadPhoto(m_imageBox.getId(), contentPosition);
		}
	}

	private boolean checkIsAvailableNativeAds()
	{
		return AdCheckManager.getInstance().isAvailableNativeAds();
	}

	private void uploadPhoto(String id, int position)
	{
		MediaBox imageBox = ImageWarehouse.getInstance().getImageBox(id);

		Intent intent = new Intent(getContext(), UploadActivity.class);
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(UploadActivity.UPLOAD_MODE, m_mode);
		intent.putExtra(UploadActivity.WEPICK_ID, m_wepickId);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(imageBox.getImages().get(position))));
		intent.putExtra("mimeType", "image/*");
		startActivity(intent);
	}

	private void loadData()
	{
		m_swipeRefreshLayout.setRefreshing(true);

		ImageWarehouse.getInstance().loadImage(getContext());
	}

	private void showEmptyView(boolean isEmpty)
	{
		if (isEmpty == true)
		{
			try
			{
				final View view = m_mergeAdapter.findViewById(R.id.empty);
				view.setVisibility((isEmpty == true) ? View.VISIBLE : View.GONE);
			}
			catch (Exception e)
			{
			}
		}
	}

	private void setToolbarTitle()
	{
		String title = null;
		if (m_mode == UploadActivity.MODE_WEPICK)
		{
			title = getString(R.string.wepick_uploading_title);
		}
		else
		{
			String storageName = m_imageBox.getDirectory();
			if (ImageWarehouse.STG_TYPE_OGQ.equals(storageName) && m_imageBox.isSdcardContent() == false)
			{
				storageName = getContext().getString(R.string.ogq_storage);
			}
			title = storageName;
		}

		getToolbar().setTitle(title);
	}

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
				else if (position > m_firstAdIndex + 1 && (position - addedAdsIndex) % (m_rotationAdUnit + 1) == 0 && m_isAvailableNativeAds)
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

	private int getContentPosition(int position)
	{
		int addedAdsCount = 0;

		if (checkIsAvailableNativeAds() == true)
		{
			if (m_isFirstPageAds)
			{
				if (position > m_firstAdIndex)
				{
					addedAdsCount = 1;
				}

				addedAdsCount += (position + 1 - addedAdsCount) / (m_rotationAdUnit + 1);
			}
			else
			{
				addedAdsCount = (position + 1) / (m_rotationAdUnit + 1);
			}
		}

		int contentPosition = position - addedAdsCount;

		return contentPosition;
	}

	//=========================================================================
	// Adapter
	//=========================================================================

	private ImageBoxAdapter m_adapter = new ImageBoxAdapter()
	{
		@Override
		public int getItemViewType(int position)
		{

			if (checkIsAvailableNativeAds())
			{
				if (m_isFirstPageAds)
				{
					int addedAdsCount = 0;

					if (position == m_firstAdIndex)
					{
						Background b = new Background();
						b.setIsNativeAd(true);

						return R.layout.item_background_native_ads;
					}

					if (position > m_firstAdIndex)
						addedAdsCount = 1;

					if ((position + 1 - addedAdsCount) % (m_rotationAdUnit + 1) == 0)
					{
						Background b = new Background();
						b.setIsNativeAd(true);

						return R.layout.item_background_native_ads;
					}
				}
				else
				{
					if ((position + 1) % (m_rotationAdUnit + 1) == 0)
					{
						Background b = new Background();
						b.setIsNativeAd(true);

						return R.layout.item_background_native_ads;
					}
				}

			}

			return R.layout.item_background;
		}

		@Override
		public int getItemCount()
		{
			if (m_imageBox == null || m_imageBox.getImages() == null || m_imageBox.getImages().size() <= 0)
			{
				return 0;
			}

			int addedAdsCount = 0;

			if (m_isAvailableNativeAds)
			{
				addedAdsCount = m_imageBox.getImages().size() / m_rotationAdUnit; // TODO : Need check

				if (m_isFirstPageAds)
				{
					if (m_imageBox.getImages().size() >= m_firstAdIndex)
						addedAdsCount += 1;
				}
			}
			return m_imageBox.getImages().size() + addedAdsCount;
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position)
		{
			onBindViewHolder(getActivity(), holder, position);
		}

		@Override
		protected void onClickAdFree()
		{
			UploadImageBoxFragment.this.onClickAdFree();
		}

		@Override
		protected void onClickImage(int index)
		{
			UploadImageBoxFragment.this.onClickImage(index);
		}

		@Override
		protected ArrayList<IntegrateNativeAd> getImageNativeAdsList()
		{
			return m_nativeAds;
		}

		@Override
		protected String getImageUrl(int position)
		{
			if (m_imageBox == null || m_imageBox.getImages() == null || m_imageBox.getImages().size() <= 0)
			{
				return null;
			}

			int contentPosition = getContentPosition(position);

			return m_imageBox.getImages().get(contentPosition);
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	public static final String WEPICK_ID = "WEPICK_ID";
	private static final String IMAGE_BOX_ID = "IMAGE_BOX_ID";

	public static final int FIRST_NATIVE_AD_INDEX = 6;
	public static final int FIRST_NATIVE_AD_INDEX_FOR_TABLET = 12;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;
	@BindView(android.R.id.list) RecyclerView m_listView;

	private int m_rotationAdUnit;
	private int m_firstAdIndex;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;
	private ArrayList<IntegrateNativeAd> m_nativeAds;
	private MediaBox m_imageBox = new MediaBox();
	private boolean m_isAvailableNativeAds = false;
	private ImageWarehouse.StorageCallback m_callback = null;
	private String m_imageBoxId = null;

	private int m_mode;
	private String m_wepickId;

	private Unbinder m_unbinder;
	private boolean m_isFirstPageAds = true;
}
