package com.ogqcorp.bgh.fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.UploadLicenseGuideActivity;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.gallery.GalleryCardsFragment;
import com.ogqcorp.bgh.gallery.GalleryEditCoverFragment;
import com.ogqcorp.bgh.gallery.GallerySaveUtil;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Gallery;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.system.ActivityResultManager;
import com.ogqcorp.commons.PreventDoubleTap;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UploadContentsFragment extends BaseActionBarFragment
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_upload_contents, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		initToolbar();
		ActivityResultManager.registerCallback(getContext(), resultCallback);
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

		ActivityResultManager.unregisterCallback(getContext(), resultCallback);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		getToolbar().setTitle(R.string.top_upload);
		onInitActionBar();
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void onInitActionBar()
	{
		setActionBarAlpha(isOverlayActionBar() ? 0 : 255);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int elevation = getResources().getDimensionPixelSize(R.dimen.appbar_elevation);
			getToolbar().setElevation(elevation);
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static UploadContentsFragment newInstance()
	{
		UploadContentsFragment fragment = new UploadContentsFragment();
		return fragment;
	}

	@OnClick({ R.id.image, R.id.image_title })
	public void onClickImage()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == false) return;

		boolean isShow = PreferencesManager.getInstance().getUploadLicenseGuideShown(getContext());
		if (isShow == false)
		{
			PreferencesManager.getInstance().setUploadLicenseGuideShown(getContext(), true);
			Intent intent = new Intent(getContext(), UploadLicenseGuideActivity.class);
			startActivityForResult(intent, REQUEST_CODE_UPLOAD_IMAGE);
		}
		else
		{
			Fragment fragment = UploadImageBoxesFragment.newInstance();
			AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
		}
	}

	@OnClick({ R.id.video, R.id.video_title })
	public void onClickVideo()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_B) == false) return;

		boolean isShow = PreferencesManager.getInstance().getUploadLicenseGuideShown(getContext());
		if (isShow == false)
		{
			PreferencesManager.getInstance().setUploadLicenseGuideShown(getContext(), true);
			Intent intent = new Intent(getContext(), UploadLicenseGuideActivity.class);
			startActivityForResult(intent, REQUEST_CODE_UPLOAD_VIDEO);
		}
		else
		{
			Fragment fragment = UploadVideoBoxesFragment.newInstance();
			AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
		}
	}

	@OnClick({ R.id.gallery, R.id.gallery_title })
	public void onClickGallery()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_C) == false) return;

		if (GallerySaveUtil.isExist(getContext()) == true)
		{
			onContinuousWriteDialog();
			return;
		}

		Fragment fragment = GalleryEditCoverFragment.newInstance();
		AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	public void onContinuousWriteDialog()
	{
		final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
				.customView(R.layout.dialog_gallery_continuous_write, false)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.show();

		final ViewGroup customView = (ViewGroup) dialog.getCustomView();

		final Button btnNew = customView.findViewById(R.id.write_new);
		final Button btnContinue = customView.findViewById(R.id.write_continue);

		btnContinue.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Gallery gallery = GallerySaveUtil.readFromFile(getContext());
				Fragment fragment = GalleryCardsFragment.newInstance(gallery);
				AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
				//GallerySaveUtil.remove(getContext());

				dialog.dismiss();
			}
		});

		btnNew.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Fragment fragment = GalleryEditCoverFragment.newInstance();
				AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
				GallerySaveUtil.remove(getContext());

				dialog.dismiss();
			}
		});
	}

	private void initToolbar()
	{
		int color = getResources().getColor(R.color.black);
		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

		toolbar.setBackgroundResource(R.drawable.actionbar_bg);
		if (toolbar.getOverflowIcon() != null)
		{
			toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}
	}

	//=========================================================================
	// Callback
	//=========================================================================
	final ActivityResultManager.Callback resultCallback = new ActivityResultManager.Callback()
	{
		@Override
		public boolean onActivityResult(int requestCode, int resultCode, Intent intent)
		{
			switch (requestCode)
			{
				case REQUEST_CODE_UPLOAD_IMAGE:
				{
					Fragment fragment = UploadImageBoxesFragment.newInstance();
					AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
				}
				break;
				case REQUEST_CODE_UPLOAD_VIDEO:
				{
					Fragment fragment = UploadVideoBoxesFragment.newInstance();
					AbsMainActivity.getTabStackHelper(UploadContentsFragment.this).showFragment(fragment);
				}
				break;
				default:
					break;
			}
			return false;
		}

		@Override
		public boolean onDestroy()
		{
			return false;
		}
	};

	//========================================================================
	// Constants
	//========================================================================

	public static final int REQUEST_CODE_UPLOAD_IMAGE = 100;
	public static final int REQUEST_CODE_UPLOAD_VIDEO = 200;


	//=========================================================================
	// Variables
	//=========================================================================

	private Unbinder m_unbinder;
}
