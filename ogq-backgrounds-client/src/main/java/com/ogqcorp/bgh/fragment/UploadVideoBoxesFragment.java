package com.ogqcorp.bgh.fragment;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.TabStackActivity;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.adapter.VideoBoxesAdapter;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.MediaBox;
import com.ogqcorp.bgh.spirit.data.VideoBox;
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

public class UploadVideoBoxesFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_upload_categories, container, false);
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
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_space);
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_video_boxes_empty);
		m_listView.setAdapter(m_mergeAdapter);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();

		if (m_mergeAdapter != null)
		{
			m_mergeAdapter.clear();
		}

		VideoWarehouse.getInstance().removeCallback(m_callback);
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

	private void onStartHelper()
	{
		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101) == true)
		{
			m_permissionGuide.setVisibility(View.VISIBLE);
			return;
		}

		m_permissionGuide.setVisibility(View.GONE);
		initWarehouse();
		if (m_isLoaded == false && VideoWarehouse.getInstance().size() <= 0)
		{
			loadData();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		getToolbar().setTitle(R.string.upload_video_toolbar_title);
		onInitActionBar();
		showActionBarSlide(true, true);
		onStartHelper();
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	protected int getFirstVisiblePosition()
	{
		return m_layout.findFirstVisibleItemPosition();
	}

	@Override
	public void onRefresh()
	{
		loadData();
	}

	@Override
	public void onScrollTop()
	{
		m_listView.scrollToPosition(0);
		showActionBarSlide(true, false);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		if (m_listView != null && isVisibleToUser == false)
		{
			m_listView.stopScroll();
		}

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
		catch (Exception ignored)
		{
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Fragment newInstance()
	{
		return newInstance(UploadActivity.MODE_IMAGE);
	}

	public static Fragment newInstance(int mode)
	{
		final Fragment fragment = new UploadVideoBoxesFragment();

		Bundle bundle = new Bundle();
		bundle.putInt(UploadActivity.UPLOAD_MODE, mode);
		fragment.setArguments(bundle);

		return BaseModel.wrap(fragment);
	}

	private void uploadVideo(String id, int position)
	{
		try
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
		catch (Exception ignored)
		{
		}
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

	@Override
	public void onRequestPermissionsResult(final int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			m_hasPermission = true;
			onStartHelper();

			try
			{
				if (m_isPremissionGuide == false)
					AnalyticsManager.getInstance().RequestPermission(getContext(), "Upload_Auth_OK");
			}
			catch (Exception ignored)
			{
			}
		} else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0]) == false && m_isPremissionGuide == false)
			{
				final MaterialDialog.SingleButtonCallback onNegative = new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						goSettings();
						dialog.dismiss();
					}
				};

				final MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						dialog.dismiss();
					}
				};

				new MaterialDialog.Builder(getContext())
						.customView(R.layout.fragment_permission_storage_retry, true)
						.negativeText(R.string.str_setting)
						.onNegative(onNegative)
						.positiveText(R.string.ok)
						.onPositive(onPositive)
						.show();
			}
		}
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	@OnClick(R.id.setting)
	public void onClickSetting()
	{
		goSettings();
	}

	protected void onClickStorage(MediaBox storage)
	{
		if (VideoWarehouse.getInstance().isLoading() == false)
		{
			TabStackActivity activity = (TabStackActivity) getActivity();
			activity.getTabStackHelper().showFragment(UploadVideoBoxFragment.newInstance(storage.getId()));
		}
	}

	protected void onClickImage(String id, int position)
	{
		if (VideoWarehouse.getInstance().isLoading() == false)
		{
			uploadVideo(id, position);
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private boolean requestPermission(final String permission, final int requestCode)
	{
		if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission) == true)
			{
				m_isPremissionGuide = true;
				final MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						dialog.dismiss();
						requestPermissions(new String[] { permission }, requestCode);
					}
				};

				new MaterialDialog.Builder(getContext())
						.customView(R.layout.fragment_permission_storage, true)
						.positiveText(R.string.ok)
						.onPositive(onPositive)
						.show();
			} else
			{
				m_isPremissionGuide = false;
				requestPermissions(new String[] { permission }, requestCode);

				try
				{
					AnalyticsManager.getInstance().RequestPermission(getContext(), "Upload_Auth");
				}
				catch (Exception ignored)
				{
				}
			}

			m_hasPermission = false;
			return true;
		}

		m_hasPermission = true;
		return false;
	}

	private void initWarehouse()
	{
		m_callback = new VideoWarehouse.StorageCallback()
		{
			@Override
			public void onCompleted()
			{
				if (FragmentUtils.isDestroyed(UploadVideoBoxesFragment.this) == true)
				{
					return;
				}

				showProgress(false);
				m_mergeAdapter.notifyDataSetChanged();

				m_isLoaded = true;
				showEmptyView((VideoWarehouse.getInstance().size() <= 0));
			}

			@Override
			public void onForceRefreshing()
			{
				if (FragmentUtils.isDestroyed(UploadVideoBoxesFragment.this) == true)
				{
					return;
				}

				m_mergeAdapter.notifyDataSetChanged();
				showProgress(true);
			}
		};
		VideoWarehouse.getInstance().init(getContext());
		VideoWarehouse.getInstance().setCallback(m_callback);

		showEmptyView((VideoWarehouse.getInstance().size() <= 0));
	}

	private int getSpanCount()
	{
		final Context context = getActivity();
		return DeviceUtils.isLandscape(context) == false ? 1 : 2;
	}

	private void loadData()
	{
		if (m_hasPermission == false)
			return;

		m_isLoaded = false;
		showProgress(true);
		VideoWarehouse.getInstance().loadVideo(getContext());
	}

	private void showEmptyView(boolean isEmpty)
	{
		if (isEmpty == true)
		{
			try
			{
				final View view = m_mergeAdapter.findViewById(R.id.empty);
				if (m_isLoaded == false)
				{
					view.setVisibility(View.GONE);
				} else
				{
					view.setVisibility((isEmpty == true) ? View.VISIBLE : View.GONE);
				}
			}
			catch (Exception ignored)
			{
			}
		}
	}

	private void showProgress(boolean isShow)
	{
		try
		{
			if (isShow == true)
			{
				m_swipeRefreshLayout.setRefreshing(true);
			} else
			{
				m_swipeRefreshLayout.setRefreshing(false);
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void goSettings()
	{
		final Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
		final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
		startActivity(intent);
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

			return 1;
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private VideoBoxesAdapter m_adapter = new VideoBoxesAdapter()
	{
		@Override
		public int getItemCount()
		{
			return VideoWarehouse.getInstance().size();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position)
		{
			onBindViewHolder(getActivity(), getVideoBox(position), holder);
		}

		@Override
		protected void onClickVideoBox(VideoBox storage)
		{
			UploadVideoBoxesFragment.this.onClickStorage(storage);
		}

		@Override
		protected void onClickVideo(String id, int position)
		{
			UploadVideoBoxesFragment.this.onClickImage(id, position);
		}

		private VideoBox getVideoBox(int position)
		{
			return VideoWarehouse.getInstance().getVideoBox(position);
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final long MAX_SIZE = 104857600;    // 100MB (1024 * 1024 * 100)
	private static final long MAX_DURATION = 16000;    // 15 sec

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;
	@BindView(R.id.permission_guide) FrameLayout m_permissionGuide;
	@BindView(R.id.setting) Button m_settingBtn;

	private boolean m_isLoaded = false;
	private boolean m_hasPermission = false;
	private boolean m_isPremissionGuide = false;

	private Unbinder m_unbinder;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;
	private VideoWarehouse.StorageCallback m_callback = null;
}
