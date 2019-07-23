package com.ogqcorp.bgh.activity;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import java.io.File;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.ads.AdCenter;
import com.ogqcorp.bgh.ads.AdCenterCache;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.filter.FilterManager;
import com.ogqcorp.bgh.gcm.BusActivityEvent;
import com.ogqcorp.bgh.live.LiveWallpaperServiceCanvas;
import com.ogqcorp.bgh.live.LiveWallpaperServiceGL;
import com.ogqcorp.bgh.live.LiveWallpaperUtils;
import com.ogqcorp.bgh.multiwallpaper.MultiWall;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.bgh.system.IntentLauncher;
import com.ogqcorp.bgh.system.Moho;
import com.ogqcorp.bgh.system.RxBus;
import com.ogqcorp.bgh.toss.TossSendActivity;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.resolve.ResolveDialogFragment;
import com.ogqcorp.commons.utils.BitmapUtils;
import com.ogqcorp.commons.utils.BundleUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

// 단 2가지 경우에만 AttachCompleteActivity를 사용
// 1. LiveWallpaperService가 실행중이지 않은 경우 LiveScreen 배경화면 설정 시
// 2. "내 사진 보관함" 자동배경 설정 완료 시

public final class AttachCompleteActivity extends AppCompatActivity
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (AdCheckManager.getInstance().isAdFree() == true)
		{
			setContentView(R.layout.activity_attach_complete);
		} else
		{
			setContentView(R.layout.activity_attach_complete_with_ads);
		}

		m_unbinder = ButterKnife.bind(this);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		initToolbar(toolbar);

		int value;
		try
		{
			value = Settings.System.getInt(getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES);
		}
		catch (Settings.SettingNotFoundException e)
		{
			value = ALWAYS_FINISH_ACTIVITIES_ON;
			e.printStackTrace();
		}

		m_isAlwaysFinishActivitiesOn = value == ALWAYS_FINISH_ACTIVITIES_ON;

		initValue(savedInstanceState);
		initLayout();

		if (m_wallpaperType == TYPE_VIDEO_COVER)
		{
			Intent action = new Intent();
			BusActivityEvent event = new BusActivityEvent(action, Activity.RESULT_OK, BusActivityEvent.REQUEST_SET_VIDEO_WALLPAPER);
			RxBus.getInstance().onEvent(event);
			m_preview.setImageBitmap(ThumbnailUtils.createVideoThumbnail(m_videoUrl, MediaStore.Video.Thumbnails.MINI_KIND));
		}
		// GIF
		else if (m_wallpaperType == TYPE_GIF)
		{
			GlideApp.with(this)
					.asGif()
					.load(m_videoUrl)
					.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.centerCrop()
					.transition(withCrossFade())
					.into(m_preview);
		} else if (m_wallpaperType == TYPE_LIVEWATCH)
		{
			Intent action = new Intent();
			BusActivityEvent event = new BusActivityEvent(action, Activity.RESULT_OK, BusActivityEvent.REQUEST_SET_LIVEWATCH_WALLPAPER);
			RxBus.getInstance().onEvent(event);

			if (m_videoUrl != null && m_videoUrl.length() > 0)
			{
				m_preview.setScaleType(ImageView.ScaleType.CENTER_CROP);

				GlideApp.with(this)
						.asBitmap()
						.load(m_videoUrl)
						.format(DecodeFormat.PREFER_ARGB_8888)
						.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.centerCrop()
						.transition(GenericTransitionOptions.with(R.anim.abc_fade_in))
						.into(m_preview);
			}
		} else
		{
			if (m_filterIndex == -1)
			{
				GlideApp.with(this)
						.load(m_file)
						.format(DecodeFormat.PREFER_ARGB_8888)
						.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.centerCrop()
						.transition(withCrossFade())
						.into(m_preview);
			} else
			{
				final int SAMPLE_SIZE = 4;
				Point size = BitmapUtils.getBitmapSize(m_file);
				if (m_filterIndex >= 0)
				{
					Bitmap bitmap = BitmapUtils.createBitmapFromFile(m_file, Bitmap.Config.ARGB_8888, size.x * SAMPLE_SIZE, size.y * SAMPLE_SIZE);
					final Bitmap filteredBitmap = FilterManager.getInstance().getFilteredBitmap(this, bitmap, m_filterIndex);
					m_preview.setImageBitmap(filteredBitmap);
					bitmap.recycle();
					bitmap = null;
				} else
				{
					Bitmap bitmap = BitmapUtils.createBitmapFromFile(m_file, Bitmap.Config.RGB_565, size.x * SAMPLE_SIZE, size.y * SAMPLE_SIZE);
					m_preview.setImageBitmap(bitmap);
				}
			}
		}

		showAds();

		if (m_wallpaperType != TYPE_MULTI_WALLPAPER)
		{
			MultiWall.getInstance(this).stop();
		}
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
		catch (Exception e)
		{
		}
	}

	@Override
	protected void onPause()
	{
		m_adCenter.onPause();
		AdCenterCache.getInstance().onPause();
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		m_adCenter.onResume();
		AdCenterCache.getInstance().onResume();

		if (m_background == null || UserManager.getInstance().isGuest() == true || m_isAlwaysFinishActivitiesOn == true)
		{
			m_share.setText(R.string.attach_complete_share);
		} else
		{
			m_share.setText(R.string.attach_complete_toss);
		}
	}

	@Override
	protected void onDestroy()
	{
		m_adCenter.onDestroy();
		AdCenterCache.getInstance().onDestroy();
		super.onDestroy();
		m_unbinder.unbind();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		BundleUtils.putFile(outState, "KEY_MOHO_FILE", m_file);
		outState.putParcelable("KEY_MOHO_BACKGROUND", m_background);
		outState.putInt(WALLPAPER_TYPE, TYPE_SINGLE_WALLPAPER);
		outState.putInt(FILTER_INDEX, m_filterIndex);

	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context, int wallpaperType, String imagePath, String directoryName, int filterIndex)
	{
		Intent intent = new Intent(context.getApplicationContext(), AttachCompleteActivity.class);
		intent.putExtra(WALLPAPER_TYPE, wallpaperType);
		intent.putExtra(IMAGE_PATH, imagePath);
		intent.putExtra(ATTACH_TYPE, directoryName);
		intent.putExtra(FILTER_INDEX, filterIndex);
		return intent;
	}

	@OnClick(R.id.share)
	public void onShare(View view)
	{
		if (m_background == null || UserManager.getInstance().isGuest() == true || m_isAlwaysFinishActivitiesOn == true)
		{
			if (m_file != null)
			{
				final Uri uri = FileProvider.getUriForFile(this, "com.ogqcorp.bgh.fileprovider", m_file);

				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("image/*");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

				new ResolveDialogFragment.Builder(this)
						.addIntent(intent)
						.setTitle(R.string.select_service)
						.start(getSupportFragmentManager());
				AnalyticsManager.getInstance().eventCompleteShare(this);
			}
		} else
		{
			AnalyticsManager.getInstance().eventUserActionToss(this, "SEND");

			final Intent intent = new Intent(this, TossSendActivity.class);
			intent.putExtra(TossSendActivity.KEY_BACKGROUND, m_background);
			startActivity(intent);
			finish();
		}
	}

	@OnClick(R.id.set_as)
	public void onSetAsWallpaper(View view)
	{
		if (LiveWallpaperUtils.isHwAcceleratable(this))
		{
			IntentLauncher.startLiveWallpaperService(this, LiveWallpaperServiceGL.class);
		} else
		{
			IntentLauncher.startLiveWallpaperService(this, LiveWallpaperServiceCanvas.class);
		}

		AnalyticsManager.getInstance().eventCompleteSetAsLiveWallpaper(this);
	}

	@OnClick(R.id.login)
	public void onLogin(View view)
	{
		AnalyticsManager.getInstance().eventStatsSignIn(this, "COMPLETE");
		IntentLauncher.startAuthSignInActivity(this, AuthActivity.SIGN_ACTION_COMPLETED);
	}
	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initToolbar(Toolbar toolbar)
	{
		toolbar.setNavigationIcon(R.drawable.ic_back_white);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});
	}

	private void initValue(Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			m_file = BundleUtils.getFile(savedInstanceState, "KEY_MOHO_FILE");
			m_background = savedInstanceState.getParcelable("KEY_MOHO_BACKGROUND");
			m_wallpaperType = savedInstanceState.getInt(WALLPAPER_TYPE, TYPE_SINGLE_WALLPAPER);
			m_filterIndex = savedInstanceState.getInt(FILTER_INDEX, 0);
		} else
		{
			m_wallpaperType = getIntent().getIntExtra(WALLPAPER_TYPE, TYPE_SINGLE_WALLPAPER);
			String attachType;
			String imageId = "EMPTY";
			if (m_wallpaperType == TYPE_SINGLE_WALLPAPER || m_wallpaperType == TYPE_LOCAL_WALLPAPER)
			{
				attachType = Moho.getAttachType();
				if (TextUtils.isEmpty(Moho.getImageId()) == false)
				{
					imageId = Moho.getImageId();
				}

				m_background = Moho.getBackground();
				m_file = Moho.getAttachFile();
				m_filterIndex = Moho.getFilterIndex();
				Moho.clearAttach();
				AsyncStats.setAsWallpaper(this, imageId);
			} else if (m_wallpaperType == TYPE_MULTI_WALLPAPER)
			{
				attachType = getIntent().getStringExtra(ATTACH_TYPE);
				String imagePath = getIntent().getStringExtra(IMAGE_PATH);
				if (TextUtils.isEmpty(imagePath) == false)
				{
					m_file = new File(imagePath);
				}
				m_filterIndex = -1;
			} else// if(m_wallpaperType == TYPE_VIDEO_COVER)
			{
				attachType = getIntent().getStringExtra(ATTACH_TYPE);
				m_videoUrl = getIntent().getStringExtra(IMAGE_PATH);
			}

			if (TextUtils.isEmpty(attachType) == true)
			{
				AnalyticsManager.getInstance().eventCompleteSetAsUserFile(this);
			} else
			{
				AnalyticsManager.getInstance().eventCompleteSetAsWallpaper(this, attachType);
			}

			AnalyticsManager.getInstance().eventCompleteSetImageFilter(this, m_filterIndex);
		}

	}

	private void initLayout()
	{
		TextView textMessage = findViewById(R.id.text_message);
		Button buttonSetAs = findViewById(R.id.set_as);
		Button buttonShare = findViewById(R.id.share);

		if (UserManager.getInstance().isGuest() == true)
		{
			String msg = getString(R.string.attach_complete_text_2) + "\n" + getString(R.string.live_wallpaper_compeleted);
			textMessage.setText(msg);

			m_login.setVisibility(View.VISIBLE);
			buttonSetAs.setVisibility(View.GONE);
			buttonShare.setVisibility(View.GONE);
			return;
		}

		if (m_wallpaperType == TYPE_MULTI_WALLPAPER)
		{
			String strType = "";
			switch (PreferencesManager.getInstance().getMultiWallpaperType(this))
			{
				case 0:
					strType = getString(R.string.multi_wallpaper_type_screen_off);
					break;
				case 1:
					strType = getString(R.string.multi_wallpaper_type_3times_aday);
					break;
				case 2:
					strType = getString(R.string.multi_wallpaper_type_once_aday);
					break;
			}
			String msg = String.format(getString(R.string.multi_wallpaper_set_message), strType);
			textMessage.setText(msg);
			buttonSetAs.setVisibility(View.GONE);
			buttonShare.setVisibility(View.GONE);
		} else if (m_wallpaperType == TYPE_VIDEO_COVER || m_wallpaperType == TYPE_GIF || m_wallpaperType == TYPE_LIVEWATCH)
		{
			String msg = getString(R.string.attach_complete_text_2) + "\n" + getString(R.string.live_wallpaper_compeleted);
			textMessage.setText(msg);
			buttonSetAs.setVisibility(View.INVISIBLE);
			buttonShare.setVisibility(View.GONE);
		} else if (m_wallpaperType == TYPE_LOCAL_WALLPAPER)
		{
			textMessage.setText(R.string.attach_complete_text_2);
			buttonSetAs.setVisibility(View.VISIBLE);
			buttonShare.setVisibility(View.INVISIBLE);
		} else
		{
			textMessage.setText(R.string.attach_complete_text_2);
			buttonSetAs.setVisibility(View.VISIBLE);
			buttonShare.setVisibility(View.VISIBLE);
		}

		if (UserManager.getInstance().isGuest() == true)
		{
			String msg = getString(R.string.sign_in_guide_1_content);
			textMessage.setText(msg);

			m_login.setVisibility(View.VISIBLE);
			buttonSetAs.setVisibility(View.GONE);
			buttonShare.setVisibility(View.GONE);
		}
	}

	private void showAds()
	{
		AdCenterCache.getInstance().showNativeBanner(this, R.id.contents_container);
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private final static String WALLPAPER_TYPE = "WALLPAPER_TYPE";
	private final static String IMAGE_PATH = "IMAGE_PATH";
	private final static String ATTACH_TYPE = "ATTACH_TYPE";
	private final static String FILTER_INDEX = "FILTER_INDEX";

	public final static int TYPE_SINGLE_WALLPAPER = 0;
	public final static int TYPE_MULTI_WALLPAPER = 1;
	public final static int TYPE_VIDEO_COVER = 2;
	public final static int TYPE_GIF = 3;    // GIF
	public final static int TYPE_LIVEWATCH = 4;
	public final static int TYPE_LOCAL_WALLPAPER = 5;

	public final static int ALWAYS_FINISH_ACTIVITIES_ON = 1;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.preview) ImageView m_preview;
	@BindView(R.id.share) Button m_share;
	@BindView(R.id.login) Button m_login;

	private AdCenter m_adCenter = new AdCenter();
	private Background m_background;
	private File m_file;
	private int m_filterIndex = 0;
	private boolean m_isAlwaysFinishActivitiesOn = true;
	private String m_videoUrl;
	private int m_wallpaperType = TYPE_SINGLE_WALLPAPER;

	private Unbinder m_unbinder;
}
