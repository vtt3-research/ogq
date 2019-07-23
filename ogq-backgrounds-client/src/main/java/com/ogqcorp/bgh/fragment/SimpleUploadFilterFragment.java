package com.ogqcorp.bgh.fragment;

import static com.facebook.FacebookSdk.getApplicationContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.TermsPoliciesAction;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.filter.FilterManager;
import com.ogqcorp.bgh.gifwallpaper.GifLiveWallpaperFileUtils;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.AppLogger;
import com.ogqcorp.bgh.system.AttachHelper;
import com.ogqcorp.bgh.view.CropView;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.PathUtils;
import com.ogqcorp.commons.utils.ToastUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

public class SimpleUploadFilterFragment extends Fragment implements UploadActivity.OnKeyDownListener, RequestListener<Bitmap>
{
	//========================================================================
	// Public Methods
	//========================================================================

	public static Fragment newInstance(Uri uri, Background background)
	{
		SimpleUploadFilterFragment f = new SimpleUploadFilterFragment();
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
		return inflater.inflate(R.layout.fragment_simple_upload_filter, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadFilter onViewCreated");

			m_firstexe = false;
			m_filterHandler = new FilterInsertHandler();
			m_filteredBitmaps = new ArrayList<Bitmap>();

			m_toolbar = view.findViewById(R.id.toolbar);
			initToolbar(m_toolbar);

			m_uri = getArguments().getParcelable(KEY_URI);
			m_background = getArguments().getParcelable(KEY_BACKGROUND);

			// 예전 시나리오 (사진 갤러리 -> 사진업로드 / 현재 사용 안함)
			if (isPickerMode() == true)
			{
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadFilter PickerMode");

				view.setAlpha(0);

				if (savedInstanceState != null)
				{
					return;
				}
				startPhotoPicker();
				return;
			}

			// 내 게시물 수정 모드
			if (isEditMode() == true)
			{
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadFilter EditMode");

				setContent(Uri.parse(m_background.getImage().getUrl()));
				return;
			}

			// 사진 업로드 모드
			if (isShareMode() == true)
			{
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadFilter ShareMode");

				setContent(m_uri);
				return;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment onViewCreated Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter onViewCreated Exception");
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
				if (isEditMode() == true)
				{
					safetyFinish();
				} else
				{
					getActivity().finish();
				}
				return true;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment onKeyEvent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter onKeyEvent Exception");
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
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadFilter onDestroyView");

			m_unbinder.unbind();

			m_firstexe = false;
			m_filterHandler = null;

			if (m_filteredBitmaps != null)
				m_filteredBitmaps.clear();
			m_filteredBitmaps = null;

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
			FirebaseCrashLog.log("SimpleUploadFilterFragment onDestroyView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter onDestroyView Exception");
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

		try
		{
			switch (requestCode)
			{
				case SELECT_PHOTO:
					if (resultCode == Activity.RESULT_OK)
					{
						getView().setAlpha(1);
						try
						{
							if (isEditMode() == false)
							{
								m_filteredBitmaps.clear();
								m_selectedFilterIndex = 0;
								m_layoutFilters.removeAllViews();
							}
							m_uri = data.getData();
							setContent(m_uri);

							getArguments().putParcelable(KEY_URI, m_uri);
						}
						catch (Exception e)
						{
							ToastUtils.makeErrorToast(getContext(), 0, "ERROR : " + e.toString()).show();
							getActivity().finish();
						}
					} else if (m_uri == null)
					{
						getActivity().finish();
					}
					break;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment onActivityResult Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter onActivityResult Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@OnClick(R.id.image_crop)
	public void onClickCrop(View view)
	{
		if (m_CropMode)
		{
			m_CropMode = false;

			if (m_cropView != null)
				m_cropView.setVisibility(View.GONE);

			if (m_Crop_Button != null)
				m_Crop_Button.setImageResource(R.drawable.ic_upload_crop_off);

			if (m_OriginalResolution != null)
			{
				String quality = "";
				quality += m_OriginalResolution.x + " X " + m_OriginalResolution.y;
				if (m_textResolution != null)
					m_textResolution.setText(quality);
			}
		} else
		{
			m_CropMode = true;

			if (m_cropView != null)
				m_cropView.setVisibility(View.VISIBLE);

			if (m_Crop_Button != null)
				m_Crop_Button.setImageResource(R.drawable.ic_upload_crop_on);
		}
	}

	@OnClick(R.id.preview)
	public void onClickPreview(View view)
	{
		if (isEditMode() == true)
		{
			return;
		}

		new MaterialDialog.Builder(getActivity())
				.content(R.string.upload_content_image_change)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.positiveText(R.string.ok)
				.negativeText(R.string.cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						getActivity().finish();
					}
				})
				.show();
	}

	@OnClick({ R.id.image_info, R.id.photo_error_msg })
	public void onClickImageInfo(View view)
	{
		String content = null;
		if (isWepickMode() == true)
		{
			content = getString(R.string.wepick_uploading_error_image_size);
		} else
		{
			content = getString(R.string.upload_prepare_spec_title) + "\n\n" + getString(R.string.upload_prepare_spec_content);
		}

		new MaterialDialog.Builder(getActivity())
				.content(content)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.positiveText(R.string.ok)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						dialog.dismiss();
					}
				})
				.show();
	}

	@Override
	public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
	{
		if (m_frameFilters != null)
			m_frameFilters.setVisibility(View.INVISIBLE);

		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		return true;
	}

	@Override
	public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
	{
		try
		{
			if (m_progressImage != null)
				m_progressImage.setVisibility(View.GONE);

			if (isEditMode() == false && m_cropView != null)
			{
				initFilter(resource);

				m_cropView.attachImageView(m_preview);
				if (m_CropMode)
					m_cropView.setVisibility(View.VISIBLE);
				else
					m_cropView.setVisibility(View.GONE);
				m_preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
				m_cropView.setFreeModeBoxIcon(R.drawable.crop_free_box_upload);
				m_cropView.setResolutionCallback(new ResolutionCallback()
				{
					@Override
					public void onResolution(RectF rect)
					{
						int cropRecttop = (int) (rect.top * m_OriginalResolution.y);
						int cropRectleft = (int) (rect.left * m_OriginalResolution.x);
						int cropRectright = (int) (rect.right * m_OriginalResolution.x);
						int cropRectbottom = (int) (rect.bottom * m_OriginalResolution.y);

						String quality = "";
						quality += (cropRectright - cropRectleft) + " X " + (cropRectbottom - cropRecttop);

						if (m_CropResolution != null)
							m_CropResolution.set(cropRectright - cropRectleft, cropRectbottom - cropRecttop);

						if (m_textResolution != null)
							m_textResolution.setText(quality);
					}
				});
				m_cropView.setFreeMode(true);

				setTextResolution();
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment onResourceReady Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter onResourceReady Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return false;
	}

	private void initFilter(Bitmap originBitmap)
	{
		try
		{

			m_frameFilters.setVisibility(View.VISIBLE);
			m_originBitmap = originBitmap;
			FilterManager.getInstance().getResizeFilteredBitmaps(getActivity(), originBitmap, new FilterManager.OnResizeBitmapsCallback()
			{
				@Override
				public void onResult(Bitmap bitmap)
				{
					if (FragmentUtils.isDestroyed(SimpleUploadFilterFragment.this) == true)
					{
						return;
					}

					if(bitmap == null)
					{
						m_progressFilter.setVisibility(View.INVISIBLE);
						return;
					}

					if (m_filterHandler != null)
						m_filterHandler.insert(bitmap);
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment initFilter Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter initFilter Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private boolean isEditMode()
	{
		return m_uri == null && m_background != null;
	}

	private boolean isWepickMode()
	{
		return ((UploadActivity) getActivity()).getUploadMode() == UploadActivity.MODE_WEPICK;
	}

	private boolean isPickerMode()
	{
		return m_uri == null && m_background == null;
	}

	private boolean isShareMode()
	{
		return m_uri != null && m_background == null;
	}

	private boolean isGifMode()
	{
		boolean isGif = false;

		try
		{
			if (UrlFactory.isStagingServer(getContext()) == true)
			{
				// GIF
				if (m_uri != null && m_uri.toString() != null && m_uri.toString().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION))
				{
					isGif = true;
				} else if (m_uri != null && m_uri.toString() != null && m_uri.toString().startsWith("content://") == true)
				{
					String MIMEType = getMIMETypeFromUri(getContext(), m_uri);

					if (MIMEType != null && MIMEType.contains("gif"))
						isGif = true;
				}

				if (isGif)
					return true;
				else
				{
					if (m_background != null && m_background.getImage() != null &&
							m_background.getImage().getUrl() != null && m_background.getImage().getUrl().toString() != null &&
							m_background.getImage().getUrl().toString().isEmpty() == false)
					{
						if (m_background.getImage().getUrl().toString().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION))
							return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment isGifMode Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter isGifMode Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return false;
	}

	private void startPhotoPicker()
	{
		try
		{
			final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment startPhotoPicker Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter startPhotoPicker Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void insertValueToViews()
	{
		try
		{
			m_toolbar.setTitle(R.string.action_edit_post);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment insertValueToViews Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter insertValueToViews Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void popFilterName(int filterIndex)
	{
		try
		{
			if (filterIndex <= 0)
			{
				m_filterName.setText(getString(R.string.no_filter));
			} else
			{
				m_filterName.setText(FilterManager.getInstance().getFilterName(filterIndex));
			}
			m_popCount++;
			m_filterName.setVisibility(View.VISIBLE);
			m_filterName.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
			m_filterName.setScaleX(0.7f);
			m_filterName.setScaleY(0.7f);
			m_filterName.animate().setInterpolator(new OvershootInterpolator())
					.scaleX(2.0f)
					.scaleY(2.0f)
					.setDuration(1500)
					.setListener(new Animator.AnimatorListener()
					{
						@Override
						public void onAnimationStart(Animator animation)
						{

						}

						@Override
						public void onAnimationEnd(Animator animation)
						{
							if (FragmentUtils.isDestroyed(SimpleUploadFilterFragment.this) == false)
							{
								m_popCount--;
								if (m_popCount <= 0)
								{
									m_filterName.setVisibility(View.GONE);
								}
							}
						}

						@Override
						public void onAnimationCancel(Animator animation)
						{

						}

						@Override
						public void onAnimationRepeat(Animator animation)
						{

						}
					})
					.start();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment popFilterName Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter popFilterName Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void changeImage(int filterIndex)
	{
		try
		{
			if (m_progressImage != null)
				m_progressImage.setVisibility(View.VISIBLE);
			m_selectedFilterIndex = filterIndex;

			FilterManager.getInstance().getFilteredBitmap(getActivity(), m_originBitmap, m_selectedFilterIndex, new FilterManager.OnBitmapCallback()
			{
				@Override
				public void onResult(Bitmap filteredBitmap)
				{
					if (FragmentUtils.isDestroyed(SimpleUploadFilterFragment.this) == true)
					{
						return;
					}

					if(filteredBitmap == null)
						return;

					m_preview.setImageResource(0);
					m_preview.destroyDrawingCache();
					m_preview.setImageBitmap(null);
					m_preview.setImageBitmap(filteredBitmap);

					if (m_progressImage != null)
						m_progressImage.setVisibility(View.GONE);
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment changeImage Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter changeImage Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void safetyFinish()
	{
		try
		{
			new MaterialDialog.Builder(getActivity())
					.content(R.string.upload_content_exit_confirm_update)
					.autoDismiss(true)
					.canceledOnTouchOutside(true)
					.positiveText(R.string.ok)
					.negativeText(R.string.cancel)
					.onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							getActivity().finish();
						}
					})
					.show();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment safetyFinish Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter safetyFinish Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void setContent(Uri uri)
	{
		try
		{
			if (isEditMode() == true)
			{
				insertValueToViews();
				//m_frameFilters.setVisibility(View.GONE);
				m_toolbar.setNavigationOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						safetyFinish();
					}
				});

				uri = Uri.parse(m_background.getImage().getUrl());

				m_EditFilterCropLayout.setVisibility(View.GONE);
				m_layoutFilters.setVisibility(View.GONE);
				m_frameFilters.setVisibility(View.GONE);
			}

			//if(isGifMode() == true)
			//{
			//	m_layoutFilters.setVisibility(View.GONE);
			//	m_frameFilters.setVisibility(View.GONE);
			//}

			setGlideView(uri);

			String result = checkImageValidation(uri);

			if (TextUtils.isEmpty(result) == false)
			{
				m_photoErrorLayout.setVisibility(View.VISIBLE);
				m_photoErrorMsg.setText(result);
				onClickImageInfo(null);
			} else
			{
				m_photoErrorLayout.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment setContent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter setContent Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void setGlideView(Uri uri)
	{
		try
		{
			if (isGifMode() == true)
			{
				RequestListener<GifDrawable> listenerGif = new RequestListener<GifDrawable>()
				{
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource)
					{
						if (m_frameFilters != null)
							m_frameFilters.setVisibility(View.INVISIBLE);

						if (m_progressImage != null)
							m_progressImage.setVisibility(View.GONE);
						return true;
					}

					@Override
					public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource)
					{
						try
						{
							if (m_progressImage != null)
								m_progressImage.setVisibility(View.GONE);

							if (isEditMode() == false && m_cropView != null)
							{
								initFilter(resource.getFirstFrame());

								m_cropView.attachImageView(m_preview);
								if (m_CropMode)
									m_cropView.setVisibility(View.VISIBLE);
								else
									m_cropView.setVisibility(View.GONE);

								m_preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
								m_cropView.setFreeModeBoxIcon(R.drawable.crop_free_box_upload);
								m_cropView.setResolutionCallback(new ResolutionCallback()
								{
									@Override
									public void onResolution(RectF rect)
									{
										int cropRecttop = (int) (rect.top * m_OriginalResolution.y);
										int cropRectleft = (int) (rect.left * m_OriginalResolution.x);
										int cropRectright = (int) (rect.right * m_OriginalResolution.x);
										int cropRectbottom = (int) (rect.bottom * m_OriginalResolution.y);

										String quality = "";
										quality += (cropRectright - cropRectleft) + " X " + (cropRectbottom - cropRecttop);
										if (m_CropResolution != null)
											m_CropResolution.set(cropRectright - cropRectleft, cropRectbottom - cropRecttop);
										if (m_textResolution != null)
											m_textResolution.setText(quality);
									}
								});
								m_cropView.setFreeMode(true);

								//m_gifDecoder = resource.getDecoder();

								setTextResolution();
							}
						}
						catch (Exception e)
						{
							FirebaseCrashLog.log("SimpleUploadFilterFragment onResourceReady(GIF) Exception");
							FirebaseCrashLog.logException(e);
						}

						return false;
					}
				};

				GlideApp.with(SimpleUploadFilterFragment.this)
						.asGif()
						.load(uri)
						//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
						.fitCenter()
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.listener(listenerGif)
						.into(m_preview);
			} else
			{
				GlideApp.with(SimpleUploadFilterFragment.this)
						.asBitmap()
						.load(uri)
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.listener(SimpleUploadFilterFragment.this)
						.into(m_preview);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment setGlideView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter setGlideView Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void initToolbar(Toolbar toolbar)
	{
		try
		{
			int color = getResources().getColor(R.color.black);
			toolbar.setTitle(getString(isWepickMode() ? R.string.wepick_uploading_title : R.string.upload_content_toolbar_title));
			toolbar.setTitleTextColor(ColorUtils.setAlphaComponent(color, 255));

			toolbar.setNavigationIcon(R.drawable.ic_back);
			toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			toolbar.setNavigationOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onKeyEvent(KeyEvent.KEYCODE_BACK, null);
				}
			});

			toolbar.inflateMenu(R.menu.fragment_simple_upload_filter);
			toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
			{
				@Override
				public boolean onMenuItemClick(MenuItem item)
				{
					switch (item.getItemId())
					{
						case R.id.next:
							showNext();
							break;
					}
					return true;
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment initToolbar Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter initToolbar Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void showUploadInfo()
	{
		try
		{
			View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_upload_prepare_info, null);
			TextView terms = view.findViewById(R.id.terms);
			terms.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					TermsPoliciesAction action = new TermsPoliciesAction((AppCompatActivity) getActivity());
					action.run(null, null);
				}
			});

			new MaterialDialog.Builder(getActivity())
					.customView(view, true)
					.canceledOnTouchOutside(true)
					.autoDismiss(true)
					.positiveText(R.string.ok)
					.onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							dialog.dismiss();
						}
					})
					.show();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment showUploadInfo Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter showUploadInfo Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	// 이미지 사이즈 체크 (사이즈가 MIN_SIZE_WIDTH / MIN_SIZE_HEIGHT 값보다 작은 경우 예외처리)
	private String checkImageValidation(Uri uri)
	{
		try
		{
			if (uri.toString().startsWith("http"))
			{
				return null;
			}

			if (m_CropMode == true && m_CropResolution != null)
			{
				if (isWepickMode() == true)
				{
					if (m_CropResolution.x < MIN_SIZE_WEPICK && m_CropResolution.y < MIN_SIZE_WEPICK) return getString(R.string.wepick_uploading_error_image_size);
				} else
				{
					if (m_CropResolution.x < MIN_SIZE_WIDTH) return getString(R.string.upload_prepare_choose_fail_min_width, String.valueOf(m_CropResolution.x));
					if (m_CropResolution.y < MIN_SIZE_HEIGHT) return getString(R.string.upload_prepare_choose_fail_min_height, String.valueOf(m_CropResolution.y));
				}

				return null;
			} else
			{
				if (uri.toString().contains(getContext().getPackageName()) == true && uri.toString().contains("data") == true)
				{
					uri = Uri.parse("file://" + uri.toString());
				}

				InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
				PathUtils.clearTempDirectory(getActivity(), "check");
				//File o_file = PathUtils.createTempFile(getActivity(), "check", ".jpg"); // original copy
				//FileUtils.copyInputStreamToFile(inputStream, o_file);
				Point size = new Point();
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				options.inSampleSize = 4;
				//BitmapFactory.decodeFile(o_file.getAbsolutePath(), options);
				BitmapFactory.decodeResourceStream(getResources(), new TypedValue(), inputStream, new Rect(), options);

				size.x = options.outWidth * 4;
				size.y = options.outHeight * 4;

				if (inputStream != null)
					inputStream.close();
				inputStream = null;

				if (isWepickMode() == true)
				{
					if (size.x < MIN_SIZE_WEPICK && size.y < MIN_SIZE_WEPICK) return getString(R.string.wepick_uploading_error_image_size);
				} else
				{
					if (size.x < MIN_SIZE_WIDTH) return getString(R.string.upload_prepare_choose_fail_min_width, String.valueOf(size.x));
					if (size.y < MIN_SIZE_HEIGHT) return getString(R.string.upload_prepare_choose_fail_min_height, String.valueOf(size.y));
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment checkImageValidation Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter checkImageValidation Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);

			return e.toString();
		}
		return null;
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
			FirebaseCrashLog.log("SimpleUploadFilterFragment getMIMETypeFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter getMIMETypeFromUri Exception");
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

	private String getFilePathFromUri(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment getFilePathFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter getFilePathFromUri Exception");
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

	private void showNext()
	{
		try
		{
			String result = checkImageValidation(m_uri);

			if (TextUtils.isEmpty(result) == false)
			{
				//m_photoErrorLayout.setVisibility(View.VISIBLE);
				//m_photoErrorMsg.setText(result);
				//m_cropView.update(m_preview);
				//m_cropView.setFreeMode(true);
				//setTextResolution();
				onClickImageInfo(null);
				return;
			}
			//else if(m_photoErrorLayout != null && m_photoErrorLayout.getVisibility() == View.VISIBLE)
			//{
			//	m_photoErrorLayout.setVisibility(View.INVISIBLE);
			//}

			if (m_firstexe == true)
				return;

			m_firstexe = true;

			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadFilter FilterIndex : " + m_selectedFilterIndex);

			if (isGifMode() == true)
			{
				if ((isEditMode() == true || m_CropMode == false) && m_selectedFilterIndex < 1)
				{
					((UploadActivity) getActivity()).showWriteStepUpload(m_uri, false);
					m_firstexe = false;
				} else
				{
					if (m_progressGifDialog == null)
					{
						m_progressGifDialog = new MaterialDialog.Builder(getContext())
								.content(R.string.processing)
								.progress(true, 0)
								.show();

						m_progressGifDialog.setCancelable(false);
					}

					AttachHelper.onApplyUploadGif(getActivity(), m_uri, m_CropMode, m_selectedFilterIndex, m_gifDecoder, m_cropView, new CropGifCallback()
					{
						@Override
						public void onCompleted(Uri uri)
						{

							if (m_progressGifDialog != null)
							{
								m_progressGifDialog.dismiss();
								m_progressGifDialog = null;
							}

							boolean crop = false;
							if (m_OriginalResolution != null && m_CropResolution != null && m_CropMode == true)
							{
								crop = m_OriginalResolution.x != m_CropResolution.x || m_OriginalResolution.y != m_CropResolution.y;
							}

							((UploadActivity) getActivity()).showWriteStepUpload(uri, crop);
							m_firstexe = false;
						}

						@Override
						public void onFailed()
						{
							if (m_progressGifDialog != null)
							{
								m_progressGifDialog.dismiss();
								m_progressGifDialog = null;
							}

							m_firstexe = false;

							ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_LONG, getString(R.string.error_code_xxx) + " (" + ERROR_FILTER_GIF_CREATE + ")").show();
						}

						@Override
						public void onProgress(int progress, int max)
						{
							if (m_progressGifDialog != null)
							{
								m_progressGifDialog.setContent(getString(R.string.processing) + "\n (" + progress + " / " + max + ")");
							}
						}
					});
				}
			} else
			{

				if (m_uri != null)
				{
					AttachHelper.onApplyUploadImage(getActivity(), m_uri, m_selectedFilterIndex, m_cropView, m_CropMode, new CropCallback()
					{

						@Override
						public void onCompleted(Uri uri)
						{
							m_firstexe = false;
							boolean crop = false;
							if (m_OriginalResolution != null && m_CropResolution != null && m_CropMode == true)
							{
								crop = m_OriginalResolution.x != m_CropResolution.x || m_OriginalResolution.y != m_CropResolution.y;
							}

							((UploadActivity) getActivity()).showWriteStepUpload(uri, crop);
						}

						@Override
						public void onFailed()
						{
							m_firstexe = false;
							ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
						}
					});
				} else
				{
					boolean crop = false;
					if (m_OriginalResolution != null && m_CropResolution != null && m_CropMode == true)
					{
						crop = m_OriginalResolution.x != m_CropResolution.x || m_OriginalResolution.y != m_CropResolution.y;
					}

					((UploadActivity) getActivity()).showWriteStepUpload(m_uri, crop);
					m_firstexe = false;
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment showNext Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter showNext Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void setTextResolution()
	{

		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		String quality = "";
		int width = 0;
		int height = 0;
		try
		{
			if (m_background != null)
			{
				width = m_background.getImage().getWidth();
				height = m_background.getImage().getHeight();
				//quality += m_background.getImage().getWidth() + " X " + m_background.getImage().getHeight();
			} else
			{
				if (isGifMode())
				{
					Uri uri = null;
					if (m_uri.toString().contains(getContext().getPackageName()) == true &&
							m_uri.toString().contains("data") == true &&
							m_uri.toString().startsWith("file://") == false)
					{
						uri = Uri.parse("file://" + m_uri.toString());
					} else
						uri = m_uri;

					pl.droidsonroids.gif.GifDrawable drawable = new pl.droidsonroids.gif.GifDrawable(getContext().getContentResolver(), uri);
					width = drawable.getIntrinsicWidth();
					height = drawable.getIntrinsicHeight();
					//quality += drawable.getIntrinsicWidth() + " X " + drawable.getIntrinsicHeight();
				} else
				{
					/*
					String filepath = m_uri.toString();

					if(m_uri.toString().startsWith("file://") == true)
					{
						if(	m_uri.toString().contains(getContext().getPackageName()) == true &&
								m_uri.toString().contains("data") == true)
						{
						}
						else
						{
							filepath = m_uri.toString().replaceFirst("file://", "");
						}
					}
					else
					{
						if(m_uri.toString().startsWith("content://") == true)
						{
							filepath = getFilePathFromUri(getContext(), m_uri);
						}
					}
					File o_file = new File(filepath);
					*/
					Uri uri = null;
					if (m_uri.toString().contains(getContext().getPackageName()) == true &&
							m_uri.toString().contains("data") == true &&
							m_uri.toString().startsWith("file://") == false)
					{
						uri = Uri.parse("file://" + m_uri);
					} else
					{
						uri = Uri.parse(m_uri.toString());
					}

					InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
					final BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					options.inSampleSize = 4;
					//BitmapFactory.decodeFile(o_file.getAbsolutePath(), options);
					BitmapFactory.decodeResourceStream(getResources(), new TypedValue(), inputStream, new Rect(), options);
					width = options.outWidth * 4;
					height = options.outHeight * 4;

					if (inputStream != null)
						inputStream.close();
					inputStream = null;
				}
			}

			if (m_OriginalResolution == null)
			{
				m_OriginalResolution = new Point();
			}

			if (m_CropResolution == null)
			{
				m_CropResolution = new Point();
			}

			m_OriginalResolution.set(width, height);
			m_CropResolution.set(width, height);

			if (m_OriginalResolution.x == 0 && m_OriginalResolution.y == 0)
			{
				m_textResolution.setVisibility(View.GONE);
			} else
			{
				quality += m_OriginalResolution.x + " X " + m_OriginalResolution.y;
				m_textResolution.setText(quality);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadFilterFragment setTextResolution Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter setTextResolution Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	//=========================================================================
	// FilterInsertHandler
	//=========================================================================
	class FilterInsertHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			try
			{
				if (FragmentUtils.isDestroyed(SimpleUploadFilterFragment.this) == true) return;

				Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);

				int index = msg.what;

				int width = DisplayManager.getInstance().getPixelFromDp(getApplicationContext(), 100);
				int height = DisplayManager.getInstance().getPixelFromDp(getApplicationContext(), 100);
				FrameLayout filterLayout = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.item_upload_filter, null);
				ImageView filterImage = filterLayout.findViewById(R.id.image_filter);
				filterImage.setImageBitmap(m_filteredBitmaps.get(index));
				filterImage.startAnimation(slideUp);
				filterImage.setVisibility(View.VISIBLE);

				if (m_selectedFilterView == null)
				{
					m_selectedFilterView = filterLayout;
					m_selectedFilterIndex = index;
				}

				if (m_selectedFilterIndex == index)
				{
					filterLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
					m_selectedFilterView = filterLayout;
				}
				filterLayout.setTag(index);
				filterLayout.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						popFilterName((int) v.getTag());
						if (((int) m_selectedFilterView.getTag()) == ((int) v.getTag()))
						{
							return;
						}

						m_selectedFilterView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
						m_selectedFilterView = (FrameLayout) v;
						m_selectedFilterView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

						changeImage((int) v.getTag());
					}
				});
				m_layoutFilters.addView(filterLayout, width, height);

				if (m_filteredBitmaps != null && m_filteredBitmaps.size() == 1)
					m_cropView.update(m_preview);
			}
			catch (Exception e)
			{
				if (m_frameFilters != null)
					m_frameFilters.setVisibility(View.GONE);

				FirebaseCrashLog.log("SimpleUploadFilterFragment FilterInsertHandler handleMessage Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter FilterInsertHandler handleMessage Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			}
		}

		private void insert(Bitmap bitmap)
		{
			try
			{
				if (FragmentUtils.isDestroyed(SimpleUploadFilterFragment.this) == true) return;

				if (m_frameFilters != null && m_frameFilters.getVisibility() == View.GONE)
				{
					return;
				}

				if (m_frameFilters != null)
				{
					m_filteredBitmaps.add(bitmap);
					sendEmptyMessage(m_filteredBitmaps.size() - 1);
				}
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadFilterFragment insert Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadFilter insert Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			}
		}
	}

	//=========================================================================
	// Interface
	//=========================================================================

	public interface CropGifCallback
	{
		void onCompleted(Uri uri);

		void onFailed();

		void onProgress(int progress, int max);
	}

	public interface CropCallback
	{
		void onCompleted(Uri uri);

		void onFailed();
	}

	public interface ResolutionCallback
	{
		void onResolution(RectF rect);
	}

	//========================================================================
	// Constants
	//========================================================================
	private static final String TAG_FRAGMENT = "UPLOAD_COMPLETE_FRAGMENT";

	private static final String KEY_URI = "KEY_URI";
	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";
	private static final String KEY_TAG_LIST = "KEY_TAG_LIST";

	public static final int RESULT_EDIT_OK = 6000;
	public static final int RESULT_DELETE_OK = 7000;

	private static final int MIN_SIZE_WIDTH = 48;
	private static final int MIN_SIZE_HEIGHT = 48;

	private static final int MIN_SIZE_WEPICK = 1920;

	public static final int SELECT_PHOTO = 100;

	public static final int ERROR_FILTER_GIF_CREATE = 200;
	public static final int ERROR_FILTER_IMAGE_CREATE = 201;

	//========================================================================
	// Variables
	//========================================================================

	@BindView(R.id.preview) ImageView m_preview;
	@BindView(R.id.crop) CropView m_cropView;
	@BindView(R.id.progress_image) ProgressWheel m_progressImage;
	@BindView(R.id.text_filter_name) TextView m_filterName;
	@BindView(R.id.frame_filter) FrameLayout m_frameFilters;
	@BindView(R.id.layout_filter) LinearLayout m_layoutFilters;
	@BindView(R.id.progress_filter) ProgressWheel m_progressFilter;
	@BindView(R.id.photo_error_layout) LinearLayout m_photoErrorLayout;
	@BindView(R.id.photo_error_msg) TextView m_photoErrorMsg;
	@BindView(R.id.toolbar) Toolbar m_toolbar;
	@BindView(R.id.edit_tool_layout) RelativeLayout m_EditFilterCropLayout;
	@BindView(R.id.image_crop) ImageView m_Crop_Button;
	@BindView(R.id.image_resolution) TextView m_textResolution;

	private Uri m_uri;
	private Background m_background;

	private GifDecoder m_gifDecoder = null;
	private MaterialDialog m_progressGifDialog = null;

	private boolean m_firstexe = false;
	private boolean m_CropMode = false;
	private Point m_OriginalResolution = null;
	private Point m_CropResolution = null;

	FilterInsertHandler m_filterHandler = null;
	private Bitmap m_originBitmap = null;
	private int m_selectedFilterIndex = -1;
	private FrameLayout m_selectedFilterView = null;
	private List<Bitmap> m_filteredBitmaps = null;
	private int m_popCount = 0;

	private Unbinder m_unbinder;
}
