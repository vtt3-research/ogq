package com.ogqcorp.bgh.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.system.AppLogger;
import com.ogqcorp.commons.GlideApp;
import com.pnikosis.materialishprogress.ProgressWheel;

public class SimpleUploadVideoPreviewFragment extends Fragment implements UploadActivity.OnKeyDownListener, RequestListener<Bitmap>
{
	//========================================================================
	// Public Methods
	//========================================================================

	public static Fragment newInstance(Uri uri, Background background)
	{
		SimpleUploadVideoPreviewFragment f = new SimpleUploadVideoPreviewFragment();
		Bundle b = new Bundle();
		b.putParcelable(KEY_URI, uri);
		b.putParcelable(KEY_BACKGROUND, background);
		f.setArguments(b);
		return f;
	}

	public static class Empty
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_simple_upload_preview, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoPreview onViewCreated");

			m_toolbar = view.findViewById(R.id.toolbar);
			initToolbar(m_toolbar);

			m_uri = getArguments().getParcelable(KEY_URI);
			m_background = getArguments().getParcelable(KEY_BACKGROUND);

			// 내 게시물 수정 모드
			if (isEditMode() == true)
			{
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoPreview EditMode");

				setGlideView(Uri.parse(m_background.getImage().getUrl()));
				return;
			}

			// 사진 업로드 모드
			if (isShareMode() == true)
			{
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoPreview ShareMode");

				setGlideView(m_uri);
				return;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoPreviewFragment onViewCreated Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoPreview onViewCreated Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public boolean onKeyEvent(int keyCode, KeyEvent event)
	{
		try
		{

			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
				if (getFragmentManager().getBackStackEntryCount() > 0)
				{
					FragmentManager fm = getFragmentManager();
					fm.popBackStack();
					return true;
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoPreviewFragment onKeyEvent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoPreview onKeyEvent Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return false;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoPreview onDestroyView");

			m_unbinder.unbind();

			if (m_preview != null)
			{
				Drawable drawable = m_preview.getDrawable();
				if (drawable instanceof Animatable)
				{
					Animatable gif = (Animatable) drawable;
					if (gif != null && gif.isRunning())
						gif.stop();
				}

				m_preview.setImageResource(0);
				m_preview.destroyDrawingCache();
				m_preview.setImageBitmap(null);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoPreviewFragment onDestroyView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoPreview onDestroyView Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
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
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
	{
		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		return true;
	}

	@Override
	public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
	{
		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		return false;
	}

	//========================================================================
	// Private Methods
	//========================================================================
	private boolean isEditMode()
	{
		return m_uri == null && m_background != null;
	}

	private boolean isShareMode()
	{
		return m_uri != null && m_background == null;
	}

	private void setGlideView(Uri uri)
	{
		try
		{

			if (m_progressImage != null)
				m_progressImage.setVisibility(View.VISIBLE);

			if (isEditMode() == false && uri.toString().startsWith("file://") == false)
			{
				if (uri.toString().startsWith("content://") == false)
					uri = Uri.parse("file://" + uri.toString());
			}

			GlideApp.with(SimpleUploadVideoPreviewFragment.this)
					.asBitmap()
					.load(uri)
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.listener(SimpleUploadVideoPreviewFragment.this)
					.into(m_preview);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoPreviewFragment setGlideView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoPreview setGlideView Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void initToolbar(Toolbar toolbar)
	{
		try
		{
			toolbar.setTitle(getString(R.string.upload_video_toolbar_title));
			toolbar.setNavigationIcon(R.drawable.ic_back);
			toolbar.setNavigationOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onKeyEvent(KeyEvent.KEYCODE_BACK, null);
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoPreviewFragment initToolbar Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoPreview initToolbar Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private String getMIMETypeFromUri(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Images.Media.MIME_TYPE };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoPreviewFragment getMIMETypeFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoPreview getMIMETypeFromUri Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}

		return null;
	}

	//========================================================================
	// Constants
	//========================================================================
	private static final String KEY_URI = "KEY_URI";
	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";

	//========================================================================
	// Variables
	//========================================================================
	private Uri m_uri;
	private Background m_background;
	private Unbinder m_unbinder;

	@BindView(R.id.preview) ImageView m_preview;
	@BindView(R.id.progress_image) ProgressWheel m_progressImage;
	@BindView(R.id.toolbar) Toolbar m_toolbar;

}
