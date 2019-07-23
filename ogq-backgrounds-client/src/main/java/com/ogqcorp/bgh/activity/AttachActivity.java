package com.ogqcorp.bgh.activity;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import static com.ogqcorp.bgh.fragment.AttachCompleteFragment.TYPE_GALLERY;
import static com.ogqcorp.bgh.fragment.AttachCompleteFragment.TYPE_SINGLE_WALLPAPER;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import android.animation.Animator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.Report2Action;
import com.ogqcorp.bgh.ads.AdCenterCache;
import com.ogqcorp.bgh.filter.FilterManager;
import com.ogqcorp.bgh.gifwallpaper.GifLiveWallpaperFileUtils;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Gallery;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.bgh.system.AttachHelper;
import com.ogqcorp.bgh.system.ImageIdRestore;
import com.ogqcorp.bgh.system.Moho;
import com.ogqcorp.bgh.view.CropView;
import com.ogqcorp.bgh.view.DeviceView;
import com.ogqcorp.commons.AsyncProcess;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.MediaScanner;
import com.ogqcorp.commons.resolve.BaseBuilder;
import com.ogqcorp.commons.utils.ActivityUtils;
import com.ogqcorp.commons.utils.BitmapUtils;
import com.ogqcorp.commons.utils.PathUtils;
import com.ogqcorp.commons.utils.ToastUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

public final class AttachActivity extends AppCompatActivity implements RequestListener<Bitmap>, Toolbar.OnMenuItemClickListener
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try
		{
			setContentView(R.layout.activity_attach);
			m_unbinder = ButterKnife.bind(this);

			initViews();

			AdCenterCache.getInstance().cacheNativeBanner(this);
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
			finish();
		}
	}

	public static <T extends Parcelable> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator)
	{
		Parcel parcel = unmarshall(bytes);
		return creator.createFromParcel(parcel);
	}

	public static Parcel unmarshall(byte[] bytes)
	{
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(bytes, 0, bytes.length);
		parcel.setDataPosition(0);
		return parcel;
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem)
	{
		switch (menuItem.getItemId())
		{
			case R.id.action_apply:
				onActionApply();
				break;

			case R.id.action_help:
				onActionHelp();
				break;
		}

		return false;
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(this, screenName);
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (getIntent() != null && TextUtils.isEmpty(getIntent().getStringExtra(ATTACH_TYPE)) == true)
		{
			AnalyticsManager.getInstance().eventAppStart(this);
			AsyncStats.statsLaunch(this);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (m_unbinder != null)
		{
			m_unbinder.unbind();
		}

		m_resolveInfosList.clear();

		if (m_filteredBitmaps != null && m_filteredBitmaps.size() > 0)
		{
			for (Bitmap bm : m_filteredBitmaps)
			{
				if (bm != null)
				{
					bm.recycle();
					bm = null;
				}
			}
			m_filteredBitmaps.clear();
		}
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

		try
		{
			AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "Back_SetAsBackground");
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
	{
		ToastUtils.makeErrorToast(this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		finish();

		return false;
	}

	@Override
	public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
	{
		target.onResourceReady(resource, null);

		m_progressImage.setVisibility(View.GONE);

		if (resource.getWidth() <= MIN_IMAGE_HEIGHT && resource.getHeight() <= MIN_IMAGE_HEIGHT)
		{
			int resizeWidth = (int) (((float) resource.getWidth() / (float) resource.getHeight()) * MIN_IMAGE_HEIGHT);
			m_originBitmap = BitmapUtils.resizeBitmap(resource, resizeWidth, MIN_IMAGE_HEIGHT, false);
			m_imageView.setImageBitmap(m_originBitmap);
		} else
		{
			m_originBitmap = resource;
		}
		m_isImageLoaded = true;
		m_cropView.attachImageView(m_imageView);
		m_deviceView.attachImageView(m_imageView);

		m_scrollableStandardView.setEnabled(true);
		m_scrollableEntireView.setEnabled(true);
		m_scrollableFreeView.setEnabled(true);
		m_fixedFitView.setEnabled(true);
		m_fixedEntireView.setEnabled(true);
		m_fixedFreeView.setEnabled(true);

		if (m_isShowFilter == true)
		{
			initFilter(resource);
		} else
		{
			showFilterLayout();
		}

		updateMode();

		return true;
	}

	//=========================================================================
	// Default Methods
	//=========================================================================

	@OnClick({ R.id.scrollable_standard, R.id.scrollable_entire, R.id.scrollable_free })
	void onScrollableButtons(View v)
	{
		m_scrollableStandardView.setSelected(false);
		m_scrollableEntireView.setSelected(false);
		m_scrollableFreeView.setSelected(false);

		v.setSelected(true);

		updateMode();
	}

	@OnClick({ R.id.fixed_fit, R.id.fixed_entire, R.id.fixed_free })
	void onFixedFit(View v)
	{
		m_fixedFitView.setSelected(false);
		m_fixedEntireView.setSelected(false);
		m_fixedFreeView.setSelected(false);

		v.setSelected(true);

		updateMode();
	}

	@OnClick(R.id.ic_filter)
	void onClickVisibleFilter()
	{
		try
		{
			AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "FilterFAB_SetAsBackground");
		}
		catch (Exception ignored)
		{
		}

		if (m_isShowFilter == true)
		{
			return;
		}

		m_isShowFilter = true;
		m_filterIndex = 0;

		Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);
		m_buttonFilter.startAnimation(slideOut);
		slideOut.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				if (ActivityUtils.isDestroyed(AttachActivity.this) == true)
				{
					return;
				}

				m_buttonFilter.hide();

				initFilter(m_originBitmap);
				m_cropView.update(m_imageView);
				m_deviceView.update(m_imageView);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});
	}

	void onActionApply()
	{
		try
		{
			AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "Confirm_SetAsBackground");
		}
		catch (Exception ignored)
		{
		}

		if (m_modePosition == 2)
		{
			return;
		}

		try
		{
			if (isGalleryType() == true)
			{
				Gallery gallery = getIntent().getParcelableExtra(GALLERY);
				Moho.setAttachBackground(getIntent().getStringExtra(ATTACH_TYPE), null, gallery,
						ImageIdRestore.getImageId(this, getIntent().getData()), m_filterIndex, TYPE_GALLERY);
			} else
			{
				Background background = getIntent().getParcelableExtra(BACKGROUND);
				Moho.setAttachBackground(getIntent().getStringExtra(ATTACH_TYPE), background, null,
						ImageIdRestore.getImageId(this, getIntent().getData()), m_filterIndex, TYPE_SINGLE_WALLPAPER);
			}

		}
		catch (Exception e)
		{
			// Nothing
		}

		try
		{
			Moho.setAttachFile(m_file);
		}
		catch (Exception e)
		{
			// Nothing
		}

		switch (m_modePosition)
		{
			case 0:
				if (isGalleryType() == true)
				{
					AttachHelper.onApplyScrollableForGallery(this, m_file, m_filterIndex, m_cropView);
				} else
				{
					AttachHelper.onApplyScrollable(this, m_file, m_filterIndex, m_cropView);
				}
				break;
			case 1:
				if (isGalleryType() == true)
				{
					AttachHelper.onApplyFixedForGallery(this, m_file, m_filterIndex, m_deviceView);
				} else
				{
					AttachHelper.onApplyFixed(this, m_file, m_filterIndex, m_deviceView);
				}
				break;
		}
	}

	private boolean isGalleryType()
	{
		final Intent intent = getIntent();

		if (intent == null || intent.hasExtra(ATTACH_TYPE) == false ||
				TextUtils.isEmpty(intent.getStringExtra(ATTACH_TYPE)) == true ||
				intent.getStringExtra(ATTACH_TYPE).equals(ATTACH_TYPE_OGQ_GALLERY) == false)
			return false;

		return true;
	}

	private void onActionHelp()
	{
		try
		{
			AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "Question_SetAsBackground");
		}
		catch (Exception ignored)
		{
		}

		new MaterialDialog.Builder(this)
				.title(R.string.problem_guide)
				.content(R.string.problem_dialog_subject)
				.callback(new MaterialDialog.ButtonCallback()
				{
					@Override
					public void onPositive(MaterialDialog dialog)
					{
						new Report2Action().run(AttachActivity.this, getSupportFragmentManager(), getWindow().getDecorView(), "ERR_ATTACH");
					}

					@Override
					public void onNeutral(MaterialDialog dialog)
					{
						dialog.dismiss();
					}
				})
				.positiveText(R.string.problem_dialog_positive_button)
				.neutralText(R.string.close)
				.show();
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initViews()
	{
		m_progressImage.setVisibility(View.VISIBLE);

		initToolbar();
		initModeButtons();
		initAnotherApps();

		final AsyncProcess<Void, Void, Object> asyncProcess = new AsyncProcess<Void, Void, Object>()
		{
			@Override
			protected Object doInBackground(Void... params)
			{
				try
				{
					return getFileFromIntent();
				}
				catch (Exception e)
				{
					return e;
				}
			}

			@Override
			protected void onPostExecute(Object object)
			{
				super.onPostExecute(object);

				if (ActivityUtils.isDestroyed(AttachActivity.this) == true)
				{
					return;
				}

				if (object instanceof File)
				{
					m_file = (File) object;

					try
					{
						GlideApp.with(AttachActivity.this)
								.asBitmap()
								.load(m_file)
								.listener(AttachActivity.this)
								.into(m_imageView);
					}
					catch (Exception e)
					{

					}
				} else if (object instanceof Exception)
				{
					ToastUtils.makeErrorToast(AttachActivity.this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
					finish();
				}
			}
		};

		asyncProcess.start(this, R.string.processing);
	}

	private void initToolbar()
	{
		int color = getResources().getColor(R.color.black);
		m_toolbar.setTitle(R.string.action_set_as_wallpaper);

		m_toolbar.setNavigationIcon(R.drawable.ic_back);
		m_toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});

		m_toolbar.inflateMenu(R.menu.fragment_attach);
		m_toolbar.setOnMenuItemClickListener(this);

		m_toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		if (m_toolbar.getOverflowIcon() != null)
		{
			m_toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}
	}

	private void initModeButtons()
	{
		final CustomPagerAdapter pagerAdapter = new CustomPagerAdapter();
		m_viewPager.setAdapter(pagerAdapter);
		m_viewPager.setOffscreenPageLimit(3);

		m_tabLayout.setupWithViewPager(m_viewPager);

		final int size = DisplayManager.getInstance().getPixelFromDp(this, MODE_DRAWABLE_SIZE_DP);

		setModeDrawable(size,
				R.drawable.ic_mode_scrollable_standard,
				m_scrollableStandardView);
		setModeDrawable(size,
				R.drawable.ic_mode_scrollable_entire,
				m_scrollableEntireView);
		setModeDrawable(size,
				R.drawable.ic_mode_scrollable_free,
				m_scrollableFreeView);

		setModeDrawable(size,
				R.drawable.ic_mode_fixed_fit,
				m_fixedFitView);
		setModeDrawable(size,
				R.drawable.ic_mode_fixed_entire,
				m_fixedEntireView);
		setModeDrawable(size,
				R.drawable.ic_mode_fixed_free,
				m_fixedFreeView);

		m_scrollableStandardView.setEnabled(false);
		m_scrollableEntireView.setEnabled(false);
		m_scrollableFreeView.setEnabled(false);
		m_fixedFitView.setEnabled(false);
		m_fixedEntireView.setEnabled(false);
		m_fixedFreeView.setEnabled(false);

		setInitialTabPosition();
	}

	private void initFilter(Bitmap originBitmap)
	{
		m_frameFilter.setVisibility(View.VISIBLE);
		FilterManager.getInstance().getResizeFilteredBitmaps(this, originBitmap, new FilterManager.OnResizeBitmapsCallback()
		{
			@Override
			public void onResult(Bitmap bitmap)
			{
				if (ActivityUtils.isDestroyed(AttachActivity.this) == true)
				{
					return;
				}

				m_filterHandler.insert(bitmap);
			}
		});
	}

	private void popFilterName(final int filterIndex)
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
						if (ActivityUtils.isDestroyed(AttachActivity.this) == false)
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

	private void changeImage(int filterIndex)
	{
		if (filterIndex == 0)
		{
			m_imageView.setImageBitmap(m_originBitmap);
			return;
		}

		m_progressImage.setVisibility(View.VISIBLE);

		m_filterIndex = filterIndex;

		FilterManager.getInstance().getFilteredBitmap(this, m_originBitmap, m_filterIndex, new FilterManager.OnBitmapCallback()
		{
			@Override
			public void onResult(Bitmap filteredBitmap)
			{
				if (ActivityUtils.isDestroyed(AttachActivity.this) == true)
				{
					return;
				}
				m_imageView.setImageBitmap(filteredBitmap);
				m_progressImage.setVisibility(View.GONE);
			}
		});
	}

	private void setInitialTabPosition()
	{
		int attachTabIndex1 = PreferencesManager.getInstance().getAttachTabIndex(this);
		int attachTabIndex2 = PreferencesManager.getInstance().getAttachTabSubIndex(this);

		switch (attachTabIndex1)
		{
			case 0:
				m_viewPager.setCurrentItem(0);
				m_fixedFitView.setSelected(true);
				switch (attachTabIndex2)
				{
					case 0:
						m_scrollableStandardView.setSelected(true);
						break;
					case 1:
						m_scrollableEntireView.setSelected(true);
						break;
					case 2:
						m_scrollableFreeView.setSelected(true);
						break;
					default:
						m_scrollableStandardView.setSelected(true);
				}
				break;

			case 1:
				m_viewPager.setCurrentItem(1);
				m_scrollableStandardView.setSelected(true);
				switch (attachTabIndex2)
				{
					case 0:
						m_fixedFitView.setSelected(true);
						break;
					case 1:
						m_fixedEntireView.setSelected(true);
						break;
					case 2:
						m_fixedFreeView.setSelected(true);
						break;
					default:
						m_fixedFitView.setSelected(true);
				}
				break;
		}
	}

	private void initAnotherApps()
	{
		final Intent intent = createSetAsWallpaperIntent(getIntent().getData());
		List<ResolveInfo> resolveInfosList = getPackageManager().queryIntentActivities(intent, 0);
		final HashSet<String> excludePackagesSet = new HashSet<>();

		excludePackagesSet.add("com.android.contacts");
		excludePackagesSet.add("com.android.htccontacts");
		excludePackagesSet.add("com.google.android.contacts");
		excludePackagesSet.add("com.ogqcorp.backgrounds");
		final BaseBuilder.Filter excludeFilter = new BaseBuilder.Filter()
		{
			@Override
			public boolean filter(ResolveInfo resolveInfo)
			{
				return excludePackagesSet.contains(resolveInfo.activityInfo.packageName) == true;
			}
		};

		if (excludeFilter != null)
		{
			for (Iterator<ResolveInfo> iter = resolveInfosList.iterator(); iter.hasNext(); )
			{
				final ResolveInfo resolveInfo = iter.next();

				if (excludeFilter.filter(resolveInfo) == true)
				{
					iter.remove();
				}
			}
		}

		for (ResolveInfo resolveInfo : resolveInfosList)
		{
			if (getPackageName().equals(resolveInfo.activityInfo.packageName) == false)
			{
				m_resolveInfosList.add(resolveInfo);
				final LinearLayout view = createAnotherAppView(resolveInfo);
				view.setTag(resolveInfo);
				view.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						ResolveInfo info = (ResolveInfo) v.getTag();
						intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
						startActivity(intent);
					}
				});
				m_anotherAppsAttachView.addView(view);
			}
		}

		resolveInfosList.clear();
	}

	private LinearLayout createAnotherAppView(ResolveInfo resolveInfo)
	{
		LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.inc_another_app, null);
		ImageView icon = view.findViewById(R.id.icon_app);
		TextView name = view.findViewById(R.id.text_name);
		Drawable drawable = resolveInfo.activityInfo.loadIcon(getPackageManager());

		icon.setImageDrawable(drawable);
		/* 아래 코드는 Xperia 단말에서 죽음
		   UncaughtException: java.lang.IllegalArgumentException: path must be convex
		    at android.graphics.Outline.setConvexPath(Outline.java:284)
		*/
		//icon.setBackground(drawable);

		name.setText(resolveInfo.loadLabel(getPackageManager()));
		return view;
	}

	private Intent createSetAsWallpaperIntent(Uri uri)
	{
		Uri mediaUri = uri;
		String scheme = uri.getScheme();

		if (scheme.equalsIgnoreCase("file"))
		{
			mediaUri = MediaScanner.scanFile(this, new File(uri.getPath()));
		}

		Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setDataAndType(mediaUri, "image/*");
		intent.putExtra("mimeType", "image/*");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		return intent;
	}

	private void setModeDrawable(int size, @DrawableRes int drawableRes, TextView textView)
	{
		final Drawable normalDrawable = createModeDrawable(size, 0x00000000, drawableRes, 0xFF757575);
		final Drawable pressedDrawable = createModeDrawable(size, 0x22000000, drawableRes, 0xFF000000);
		final Drawable selectedDrawable = createModeDrawable(size, 0xFF0AE27B, drawableRes, 0xFFFFFFFF);

		final StateListDrawable drawable = new StateListDrawable();
		drawable.addState(new int[] { -android.R.attr.state_selected, +android.R.attr.state_pressed }, pressedDrawable);
		drawable.addState(new int[] { +android.R.attr.state_selected }, selectedDrawable);
		drawable.addState(new int[] {}, normalDrawable);

		textView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
	}

	private Drawable createModeDrawable(int size, int backgroundColor, @DrawableRes int icon, int iconColor)
	{
		final GradientDrawable backgroundDrawable = new GradientDrawable();
		backgroundDrawable.setShape(GradientDrawable.OVAL);
		backgroundDrawable.setColor(backgroundColor);
		backgroundDrawable.setSize(size, size);

		final BitmapDrawable iconDrawable = (BitmapDrawable) ResourcesCompat.getDrawable(getResources(), icon, null);
		iconDrawable.mutate();
		iconDrawable.setGravity(Gravity.CENTER);
		iconDrawable.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);

		return new CustomLayerDrawable(new Drawable[] { backgroundDrawable, iconDrawable });
	}

	private File getFileFromIntent() throws Exception
	{
		final Uri uri = getIntent().getData();
		final InputStream inputStream = getContentResolver().openInputStream(uri);

		File file = null;

		// GIF
		if (uri != null && uri.toString() != null && uri.toString().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION))
		{
			file = PathUtils.createTempFileInExternal(this, "backgrounds", GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION);
		} else
		{
			file = PathUtils.createTempFileInExternal(this, "backgrounds", ".jpg");
		}

		FileUtils.copyInputStreamToFile(inputStream, file);

		return file;
	}

	private void updateMode()
	{
		final Point displaySize = DisplayManager.getInstance().getRealDisplaySize(this);

		if (m_modePosition == 0)
		{
			m_toolbar.getMenu().findItem(R.id.action_help).setVisible(true);
			m_toolbar.getMenu().findItem(R.id.action_apply).setVisible(true);
			m_cropView.setVisibility(View.VISIBLE);
			m_deviceView.setVisibility(View.GONE);
			PreferencesManager.getInstance().setAttachTabIndex(this, 0);

			m_imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			if (m_scrollableStandardView.isSelected() == true)
			{
				m_cropView.setFreeMode(false);
				m_cropView.setCropRatio(displaySize.x * 2 / (float) displaySize.y);
				PreferencesManager.getInstance().setAttachTabSubIndex(this, 0);

				try
				{
					AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "ScrollStandard_SetAsBackground");
				}
				catch (Exception ignored)
				{
				}
			} else if (m_scrollableEntireView.isSelected() == true)
			{
				m_cropView.setFreeMode(false);
				m_cropView.setCropRatio(0);
				PreferencesManager.getInstance().setAttachTabSubIndex(this, 1);

				try
				{
					AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "ScrollFull_SetAsBackground");
				}
				catch (Exception ignored)
				{
				}
			} else if (m_scrollableFreeView.isSelected() == true)
			{
				m_cropView.setFreeMode(true);
				PreferencesManager.getInstance().setAttachTabSubIndex(this, 2);

				try
				{
					AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "ScrollFree_SetAsBackground");
				}
				catch (Exception ignored)
				{
				}
			}
		} else if (m_modePosition == 1)
		{
			m_toolbar.getMenu().findItem(R.id.action_help).setVisible(true);
			m_toolbar.getMenu().findItem(R.id.action_apply).setVisible(true);
			m_cropView.setVisibility(View.GONE);
			m_deviceView.setVisibility(View.VISIBLE);
			PreferencesManager.getInstance().setAttachTabIndex(this, 1);

			if (m_fixedFitView.isSelected() == true)
			{
				m_deviceView.setZoomable(false);
				m_deviceView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				PreferencesManager.getInstance().setAttachTabSubIndex(this, 0);

				try
				{
					AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "FixCustom_SetAsBackground");
				}
				catch (Exception ignored)
				{
				}
			} else if (m_fixedEntireView.isSelected() == true)
			{
				m_deviceView.setZoomable(false);
				m_deviceView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				PreferencesManager.getInstance().setAttachTabSubIndex(this, 1);

				try
				{
					AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "FixFull_SetAsBackground");
				}
				catch (Exception ignored)
				{
				}
			} else if (m_fixedFreeView.isSelected() == true)
			{
				m_deviceView.setZoomable(true);
				m_deviceView.setScaleType(ImageView.ScaleType.MATRIX);
				PreferencesManager.getInstance().setAttachTabSubIndex(this, 2);

				try
				{
					AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "FixFree_SetAsBackground");
				}
				catch (Exception ignored)
				{
				}
			}
		} else if (m_modePosition == 2)
		{
			m_toolbar.getMenu().findItem(R.id.action_help).setVisible(false);
			m_toolbar.getMenu().findItem(R.id.action_apply).setVisible(false);
			m_cropView.setVisibility(View.GONE);
			m_deviceView.setVisibility(View.GONE);
			if (m_unfilterView != null && m_selectedFilterView != null)
			{
				m_selectedFilterView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
				m_selectedFilterView = m_unfilterView;
				m_unfilterView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_800));
			}

			try
			{
				AnalyticsManager.getInstance().SetAsBackgroundEvent(this, "Others_SetAsBackground");
			}
			catch (Exception ignored)
			{
			}
		}
	}

	private void showFilterLayout()
	{
		if (m_isShowFilter == true)
		{
			//	m_buttonFilter.setVisibility(View.GONE);
			m_buttonFilter.hide();
			m_frameFilter.setVisibility(View.VISIBLE);
			m_cropView.update(m_imageView);
			m_deviceView.update(m_imageView);
		} else
		{
			m_frameFilter.setVisibility(View.GONE);
			Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);
			m_buttonFilter.startAnimation(slideUp);
			//	m_buttonFilter.setVisibility(View.VISIBLE);
			m_buttonFilter.show();
		}
	}

	private void hideFilterLayout()
	{
		if (m_isShowFilter == true)
		{
			m_filterIndex = 0;
			changeImage(0);
			slideOutAnimation(m_frameFilter);
		} else
		{
			slideOutAnimation(m_buttonFilter);
		}
	}

	private void slideOutAnimation(final View view)
	{
		Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_out_bottom);
		view.startAnimation(slideOut);
		slideOut.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				if (ActivityUtils.isDestroyed(AttachActivity.this) == true)
				{
					return;
				}

				view.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{

			}
		});
	}

	//=========================================================================
	// Custom PagerAdapter
	//=========================================================================

	private class CustomPagerAdapter extends PagerAdapter
	{
		@Override
		public int getCount()
		{
			return 3;
		}

		@Override
		public Object instantiateItem(@NotNull ViewGroup collection, int position)
		{
			switch (position)
			{
				case 0:
					return m_modeScrollableView;
				case 1:
					return m_modeFixedView;
				case 2:
					return m_modeAnotherAppsView;
			}
			return null;
		}

		@Override
		public void destroyItem(@NotNull ViewGroup collection, int position, @NotNull Object view)
		{
			// Nothing
		}

		@Override
		public boolean isViewFromObject(@NotNull View view, @NotNull Object object)
		{
			return view == object;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			switch (position)
			{
				case 0:
					return getString(R.string.mode_scrollable);
				case 1:
					return getString(R.string.mode_fixed);
				case 2:
					return getString(R.string.mode_another_apps);
			}

			return "";
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object)
		{
			super.setPrimaryItem(container, position, object);

			if (m_modePosition != position)
			{
				if (position == 2)
				{
					hideFilterLayout();
				} else if (m_modePosition == 2)
				{
					showFilterLayout();
				}

				m_modePosition = position;

				if (m_isImageLoaded == true)
				{
					updateMode();
				}
			}
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
			Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.abc_slide_in_bottom);

			int index = msg.what;
			if (index == 0)//m_progressFilter.getVisibility() == View.VISIBLE)
			{
				//m_progressFilter.setVisibility(View.GONE);
				m_scrollFilters.startAnimation(slideUp);
				m_scrollFilters.setVisibility(View.VISIBLE);
			}

			int width = DisplayManager.getInstance().getPixelFromDp(getApplicationContext(), 50);
			int height = DisplayManager.getInstance().getPixelFromDp(getApplicationContext(), 50);
			FrameLayout filterLayout = (FrameLayout) getLayoutInflater().inflate(R.layout.item_filter, null);
			ImageView filterImage = filterLayout.findViewById(R.id.image_filter);
			filterImage.setImageBitmap(m_filteredBitmaps.get(index));
			filterImage.startAnimation(slideUp);
			filterImage.setVisibility(View.VISIBLE);
			if (index == 0)
			{
				m_unfilterView = filterLayout;
			}

			if (m_filterIndex == index)
			{
				filterLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_800));
				m_selectedFilterView = filterLayout;
			}
			filterLayout.setTag(index);
			filterLayout.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if (m_progressImage.getVisibility() == View.VISIBLE || m_modePosition == 2)
					{
						return;
					}

					popFilterName((int) v.getTag());

					if (((int) m_selectedFilterView.getTag()) == ((int) v.getTag()))
					{
						return;
					}

					m_selectedFilterView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
					m_selectedFilterView = (FrameLayout) v;
					m_selectedFilterView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_800));

					changeImage((int) v.getTag());
				}
			});
			m_layoutFilters.addView(filterLayout, width, height);
		}

		private void insert(Bitmap bitmap)
		{
			m_filteredBitmaps.add(bitmap);
			sendEmptyMessage(m_filteredBitmaps.size() - 1);
		}
	}

	//=========================================================================
	// Custom LayerDrawable
	//=========================================================================

	private static class CustomLayerDrawable extends LayerDrawable
	{
		public CustomLayerDrawable(Drawable[] layers)
		{
			super(layers);
		}

		@Override
		public void setColorFilter(ColorFilter cf)
		{
			// Nothing
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final int MIN_IMAGE_HEIGHT = 600;
	private static final int MODE_DRAWABLE_SIZE_DP = 50;
	public static final String ATTACH_TYPE = "ATTACH_TYPE";
	public static final String GALLERY = "GALLERY";
	public static final String BACKGROUND = "BACKGROUND";

	public static final String ATTACH_TYPE_OGQ = "OGQ";
	public static final String ATTACH_TYPE_OGQ_GALLERY = "OGQ_GALLERY";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.toolbar) Toolbar m_toolbar;
	@BindView(R.id.image) ImageView m_imageView;
	@BindView(R.id.progress_image) ProgressWheel m_progressImage;
	@BindView(R.id.crop) CropView m_cropView;
	@BindView(R.id.device) DeviceView m_deviceView;
	@BindView(R.id.text_filter_name) TextView m_filterName;
	@BindView(R.id.ic_filter) FloatingActionButton m_buttonFilter;
	@BindView(R.id.frame_filter) FrameLayout m_frameFilter;
	@BindView(R.id.progress_filter) ProgressWheel m_progressFilter;
	@BindView(R.id.scroll_filter) HorizontalScrollView m_scrollFilters;
	@BindView(R.id.layout_filter) LinearLayout m_layoutFilters;
	@BindView(R.id.tab_layout) TabLayout m_tabLayout;
	@BindView(R.id.view_pager) ViewPager m_viewPager;
	@BindView(R.id.mode_scrollable) View m_modeScrollableView;
	@BindView(R.id.mode_fixed) View m_modeFixedView;
	@BindView(R.id.mode_another_apps) View m_modeAnotherAppsView;
	@BindView(R.id.scrollable_standard) TextView m_scrollableStandardView;
	@BindView(R.id.scrollable_entire) TextView m_scrollableEntireView;
	@BindView(R.id.scrollable_free) TextView m_scrollableFreeView;
	@BindView(R.id.fixed_fit) TextView m_fixedFitView;
	@BindView(R.id.fixed_entire) TextView m_fixedEntireView;
	@BindView(R.id.fixed_free) TextView m_fixedFreeView;
	@BindView(R.id.another_apps_attach_view) LinearLayout m_anotherAppsAttachView;

	private File m_file;
	private Bitmap m_originBitmap = null;
	private int m_modePosition = -1;
	private final ArrayList<ResolveInfo> m_resolveInfosList = new ArrayList<ResolveInfo>();
	private boolean m_isImageLoaded = false;

	private boolean m_isShowFilter = false;
	FilterInsertHandler m_filterHandler = new FilterInsertHandler();
	private int m_filterIndex = -1;
	private FrameLayout m_unfilterView = null;
	private FrameLayout m_selectedFilterView = null;
	private List<Bitmap> m_filteredBitmaps = new ArrayList<Bitmap>();
	private int m_popCount = 0;

	private Unbinder m_unbinder;
}