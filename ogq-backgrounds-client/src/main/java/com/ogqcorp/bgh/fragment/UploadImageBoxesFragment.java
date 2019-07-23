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
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

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
import com.ogqcorp.bgh.adapter.ImageBoxesAdapter;
import com.ogqcorp.bgh.fragment.base.BaseRecyclerFragmentEx;
import com.ogqcorp.bgh.imagewarehouse.ImageWarehouse;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.MediaBox;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;

/**
 * Created by ogq on 2017. 7. 13..
 */

public class UploadImageBoxesFragment extends BaseRecyclerFragmentEx implements SwipeRefreshLayout.OnRefreshListener
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_upload_categories, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unBinder = ButterKnife.bind(this, view);

		Bundle bundle = getArguments();
		if (bundle != null)
		{
			m_mode = bundle.getInt(UploadActivity.UPLOAD_MODE, UploadActivity.MODE_IMAGE);
			m_wePickId = bundle.getString(WEPICK_ID);
		}

		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		m_layout = new GridLayoutManagerEx(getActivity(), getSpanCount());
		m_layout.setSpanSizeLookup(m_spanSizeLookup);

		m_listView.setLayoutManager(m_layout);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_space);
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_image_boxes_empty);
		m_listView.setAdapter(m_mergeAdapter);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unBinder.unbind();

		if (m_mergeAdapter != null)
		{
			m_mergeAdapter.clear();
		}

		ImageWarehouse.getInstance().removeCallback(m_callback);
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
		if (m_isLoaded == false && ImageWarehouse.getInstance().size() <= 0)
		{
			loadData();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		getToolbar().setTitle(m_mode == UploadActivity.MODE_WEPICK ? R.string.wepick_uploading_title : R.string.upload_content_toolbar_title);
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
		return newInstance(UploadActivity.MODE_IMAGE, "-1");
	}

	public static Fragment newInstance(int mode, String wepickId)
	{
		final Fragment fragment = new UploadImageBoxesFragment();

		Bundle bundle = new Bundle();
		bundle.putInt(UploadActivity.UPLOAD_MODE, mode);
		bundle.putString(WEPICK_ID, wepickId);
		fragment.setArguments(bundle);

		return BaseModel.wrap(fragment);
	}

	private void uploadPhoto(String id, int position)
	{
		MediaBox mediaBox = ImageWarehouse.getInstance().getImageBox(id);

		Intent intent = new Intent(getContext(), UploadActivity.class);
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(UploadActivity.UPLOAD_MODE, m_mode);
		intent.putExtra(UploadActivity.WEPICK_ID, m_wePickId);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(mediaBox.getImages().get(position))));
		intent.putExtra("mimeType", "image/*");
		startActivity(intent);

		/*if(getFragmentManager().getBackStackEntryCount() > 0)
		{
			FragmentManager fm = getFragmentManager();
			fm.popBackStack();
		}*/
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			m_hasPermission = true;
			onStartHelper();

			try
			{
				if (m_isPermissionGuide == false)
					AnalyticsManager.getInstance().RequestPermission(getContext(), "Upload_Auth_OK");
			}
			catch (Exception ignored)
			{
			}
		} else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0]) == false && m_isPermissionGuide == false)
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
		if (ImageWarehouse.getInstance().isLoading() == false)
		{
			TabStackActivity activity = (TabStackActivity) getActivity();
			activity.getTabStackHelper().showFragment(UploadImageBoxFragment.newInstance(storage.getId(), m_mode, m_wePickId));
		}
	}

	protected void onClickImage(String id, int position)
	{
		if (ImageWarehouse.getInstance().isLoading() == false)
		{
			uploadPhoto(id, position);
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
				m_isPermissionGuide = true;
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
				m_isPermissionGuide = false;
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
		m_callback = new ImageWarehouse.StorageCallback()
		{
			@Override
			public void onCompleted()
			{
				if (FragmentUtils.isDestroyed(UploadImageBoxesFragment.this) == true)
				{
					return;
				}

				showProgress(false);
				m_mergeAdapter.notifyDataSetChanged();

				m_isLoaded = true;
				showEmptyView((ImageWarehouse.getInstance().size() <= 0));
			}

			@Override
			public void onForceRefreshing()
			{
				if (FragmentUtils.isDestroyed(UploadImageBoxesFragment.this) == true)
				{
					return;
				}

				m_mergeAdapter.notifyDataSetChanged();
				showProgress(true);
			}
		};
		ImageWarehouse.getInstance().init(getContext());
		ImageWarehouse.getInstance().setCallback(m_callback);

		showEmptyView((ImageWarehouse.getInstance().size() <= 0));
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
		ImageWarehouse.getInstance().loadImage(getContext());
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

	private ImageBoxesAdapter m_adapter = new ImageBoxesAdapter()
	{
		@Override
		public int getItemCount()
		{
			return ImageWarehouse.getInstance().size();
		}

		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(@NotNull ViewHolder holder, int position)
		{
			onBindViewHolder(getActivity(), getImageBox(position), holder);
		}

		@Override
		protected void onClickImageBox(MediaBox storage)
		{
			UploadImageBoxesFragment.this.onClickStorage(storage);
		}

		@Override
		protected void onClickImage(String id, int position)
		{
			UploadImageBoxesFragment.this.onClickImage(id, position);
		}

		private MediaBox getImageBox(int position)
		{
			return ImageWarehouse.getInstance().getImageBox(position);
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	public static final String WEPICK_ID = "WEPICK_ID";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;
	@BindView(R.id.permission_guide) FrameLayout m_permissionGuide;
	@BindView(R.id.setting) Button m_settingBtn;

	private boolean m_isLoaded = false;
	private boolean m_hasPermission = false;
	private boolean m_isPermissionGuide = false;

	private int m_mode;
	private String m_wePickId;

	private Unbinder m_unBinder;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;
	private ImageWarehouse.StorageCallback m_callback = null;
}
