package com.ogqcorp.bgh.fragment;

import java.io.File;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.adapter.VideoBoxAdapter;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.VideoBox;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.videowarehouse.VideoWarehouse;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

/**
 * Created by ogq on 2017. 7. 13..
 */

public class UploadVideoBoxFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
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
			m_videoBoxId = bundle.getString(IMAGE_BOX_ID);
		}

		m_videoBox = VideoWarehouse.getInstance().getVideoBox(m_videoBoxId);

		m_rotationAdUnit = PreferencesManager.getInstance().getCountryCountAdList(getContext());
		m_firstAdIndex = DeviceUtils.isTablet(getContext()) ? FIRST_NATIVE_AD_INDEX_FOR_TABLET : FIRST_NATIVE_AD_INDEX;

		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				m_nativeAds = AdCheckManager.getInstance().getShuffledNativeAdList(UploadVideoBoxFragment.this);

				if (m_nativeAds == null || m_nativeAds.size() == 0)
				{
					onNotAvailable();
				} else
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
	public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
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
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_video_boxes_empty);
		m_listView.setAdapter(m_mergeAdapter);

		updateHeaderViews();

		initWarehouse();

		if (savedInstanceState == null || VideoWarehouse.getInstance().size() <= 0)
		{
			loadData();
		}
	}

	@Override
	protected void onInitActionBar()
	{
		super.onInitActionBar();

		if (m_videoBox == null)
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

		VideoWarehouse.getInstance().removeCallback(m_callback);

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
		catch (Exception ignored)
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

	public static Fragment newInstance(String imageBoxId)
	{
		final Fragment fragment = new UploadVideoBoxFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(UploadActivity.UPLOAD_MODE, UploadActivity.MODE_VIDEO);
		bundle.putString(IMAGE_BOX_ID, imageBoxId);
		fragment.setArguments(bundle);
		return BaseModel.wrap(fragment);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initWarehouse()
	{
		m_callback = new VideoWarehouse.StorageCallback()
		{
			@Override
			public void onCompleted()
			{
				if (FragmentUtils.isDestroyed(UploadVideoBoxFragment.this) == true)
				{
					return;
				}

				m_videoBox = VideoWarehouse.getInstance().getVideoBox(m_videoBoxId);

				if (m_videoBox != null)
				{
					setToolbarTitle();

					if (m_videoBox.getImages() == null || m_videoBox.getImages().size() <= 0)
					{
						showEmptyView(true);
					} else
					{
						showEmptyView(false);
					}
				} else
				{
					showEmptyView(true);
				}
				m_swipeRefreshLayout.setRefreshing(false);
				m_mergeAdapter.notifyDataSetChanged();
			}

			@Override
			public void onForceRefreshing()
			{
				if (FragmentUtils.isDestroyed(UploadVideoBoxFragment.this) == true)
				{
					return;
				}
				m_mergeAdapter.notifyDataSetChanged();
				m_swipeRefreshLayout.setRefreshing(true);
			}
		};

		VideoWarehouse.getInstance().init(getContext());
		VideoWarehouse.getInstance().setCallback(m_callback);

		if (m_videoBox == null || m_videoBox.getImages() == null || m_videoBox.getImages().size() <= 0)
		{
			showEmptyView(true);
		} else
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
		if (VideoWarehouse.getInstance().isLoading() == false)
		{
			int contentPosition = getContentPosition(position);
			uploadVideo(m_videoBox.getId(), contentPosition);
		}
	}

	private boolean checkIsAvailableNativeAds()
	{
		return AdCheckManager.getInstance().isAvailableNativeAds();
	}

	private void uploadVideo(String id, int position)
	{
		VideoBox videoBox = VideoWarehouse.getInstance().getVideoBox(id);

		long duration = videoBox.getDurations().get(position);
		if (duration >= MAX_DURATION)
		{
			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.upload_video_duration_conditon).show();
			return;
		}

		File file = new File(videoBox.getImages().get(position));
		if (file.length() > MAX_SIZE)
		{
			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.upload_video_size_conditon).show();
			return;
		}

		showLicenseDialog(Uri.fromFile(file));
	}

	private void showLicenseDialog(final Uri uri)
	{
		final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
				.title(R.string.upload_video_license_select_title)
				.customView(R.layout.dialog_upload_video_license, false)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.show();

		final ViewGroup customView = (ViewGroup) dialog.getCustomView();
		final AppCompatRadioButton btnPublic = customView.findViewById(R.id.license_public);
		final AppCompatRadioButton btnGeneral = customView.findViewById(R.id.license_general);

		final Button ok = customView.findViewById(R.id.ok);
		final Button cancel = customView.findViewById(R.id.cancel);

		btnPublic.setChecked(true);
		btnPublic.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				btnPublic.setChecked(true);
				btnGeneral.setChecked(false);
			}
		});

		btnGeneral.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				btnPublic.setChecked(false);
				btnGeneral.setChecked(true);
			}
		});

		ok.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				int license = btnGeneral.isChecked() ? UploadActivity.LICENSE_GENERAL : UploadActivity.LICENSE_PUBLIC;
				dialog.dismiss();

				Intent intent = new Intent(getContext(), UploadActivity.class);
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(UploadActivity.UPLOAD_MODE, UploadActivity.MODE_VIDEO);
				intent.putExtra(UploadActivity.UPLOAD_LICENSE, license);
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.putExtra("mimeType", "video/*");
				startActivity(intent);
			}
		});

		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
			}
		});
	}

	private void loadData()
	{
		m_swipeRefreshLayout.setRefreshing(true);

		VideoWarehouse.getInstance().loadVideo(getContext());
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
			catch (Exception ignored)
			{
			}
		}
	}

	private void setToolbarTitle()
	{
		String title;
		String storageName = m_videoBox.getDirectory();
		if (VideoWarehouse.STG_TYPE_OGQ.equals(storageName) && m_videoBox.isSdcardContent() == false)
		{
			storageName = getContext().getString(R.string.ogq_storage);
		}
		title = storageName;

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
			} else if (m_isFirstPageAds && m_isAvailableNativeAds)
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
				} else if (position > m_firstAdIndex + 1 && (position - addedAdsIndex) % (m_rotationAdUnit + 1) == 0 && m_isAvailableNativeAds)
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
			} else
			{
				addedAdsCount = (position + 1) / (m_rotationAdUnit + 1);
			}
		}

		return position - addedAdsCount;
	}

	//=========================================================================
	// Adapter
	//=========================================================================

	private VideoBoxAdapter m_adapter = new VideoBoxAdapter()
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
				} else
				{
					if ((position + 1) % (m_rotationAdUnit + 1) == 0)
					{
						Background b = new Background();
						b.setIsNativeAd(true);

						return R.layout.item_background_native_ads;
					}
				}

			}

			return R.layout.item_video_background;
		}

		@Override
		public int getItemCount()
		{
			if (m_videoBox == null || m_videoBox.getImages() == null || m_videoBox.getImages().size() <= 0)
			{
				return 0;
			}

			int addedAdsCount = 0;

			if (m_isAvailableNativeAds)
			{
				addedAdsCount = m_videoBox.getImages().size() / m_rotationAdUnit; // TODO : Need check

				if (m_isFirstPageAds)
				{
					if (m_videoBox.getImages().size() >= m_firstAdIndex)
						addedAdsCount += 1;
				}
			}
			return m_videoBox.getImages().size() + addedAdsCount;
		}

		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(@NotNull final ViewHolder holder, final int position)
		{
			onBindViewHolder(getActivity(), holder, position);
		}

		@Override
		protected void onClickAdFree()
		{
			UploadVideoBoxFragment.this.onClickAdFree();
		}

		@Override
		protected void onClickImage(int index)
		{
			UploadVideoBoxFragment.this.onClickImage(index);
		}

		@Override
		protected ArrayList<IntegrateNativeAd> getImageNativeAdsList()
		{
			return m_nativeAds;
		}

		@Override
		protected String getImageUrl(int position)
		{
			if (m_videoBox == null || m_videoBox.getImages() == null || m_videoBox.getImages().size() <= 0)
			{
				return null;
			}

			int contentPosition = getContentPosition(position);

			return m_videoBox.getImages().get(contentPosition);
		}

		@Override
		protected long getDuration(int position)
		{
			if (m_videoBox == null || m_videoBox.getImages() == null || m_videoBox.getImages().size() <= 0)
			{
				return 0;
			}

			int contentPosition = getContentPosition(position);

			return m_videoBox.getDurations().get(contentPosition);
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final long MAX_SIZE = 104857600;    // 100MB (1024 * 1024 * 100)
	private static final long MAX_DURATION = 16000;    // 15 sec	(16초 미만의 사이즈의 경우 15초로 표기 되므로 15초로 표기된 영상은 업로드가 가능해야 할 것으로 판단되어 16초 미만을 기준으로 함)
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
	private VideoBox m_videoBox = new VideoBox();
	private boolean m_isAvailableNativeAds = false;
	private VideoWarehouse.StorageCallback m_callback = null;
	private String m_videoBoxId = null;

	private Unbinder m_unbinder;
	private boolean m_isFirstPageAds = true;
}