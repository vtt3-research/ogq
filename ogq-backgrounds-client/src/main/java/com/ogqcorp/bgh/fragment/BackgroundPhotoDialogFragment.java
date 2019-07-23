package com.ogqcorp.bgh.fragment;

import uk.co.senab.photoview.IPhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.BaseAction;
import com.ogqcorp.bgh.action.DownloadAction;
import com.ogqcorp.bgh.action.SetAsWallpaperAction;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.ads.AdCenter;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.toss.TossSendActivity;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.PhotoDialogFragment;
import com.ogqcorp.commons.utils.FragmentUtils;

public final class BackgroundPhotoDialogFragment extends PhotoDialogFragment implements View.OnTouchListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	@SuppressWarnings("deprecation")
	public BackgroundPhotoDialogFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		m_screenSize = DisplayManager.getInstance().getDisplaySize(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

		m_background = getArguments().getParcelable(KEY_BACKGROUND);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		m_NavigationBarView = view.findViewById(R.id.navigation_bar);
		m_NavigationImageView = view.findViewById(R.id.navigation_image);
		m_NavigationLayout = view.findViewById(R.id.navigation_layout);
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
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getActivity(), "Back_Preview_Detail");
		}
		catch (Exception e)
		{
		}

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
	public boolean onMenuItemClick(MenuItem menuItem)
	{
		final int menuId = menuItem.getItemId();

		switch (menuId)
		{
			case R.id.action_set_as_wallpaper:
			{
				try
				{
					AnalyticsManager.getInstance().DetailEvent(getContext(), "SetAsBackground_Preview_Detail");
				}
				catch (Exception e)
				{
				}

				final BaseAction action = new SetAsWallpaperAction();
				action.run(this, m_background);
				return true;
			}

			case R.id.action_download:
			{
				try
				{
					AnalyticsManager.getInstance().DetailEvent(getContext(), "Download_Preview_Detail");
				}
				catch (Exception e)
				{
				}

				final DownloadAction action = new DownloadAction();
				action.setAfterAdAction(this, new Runnable()
				{
					@Override
					public void run()
					{
						if (FragmentUtils.isDestroyed(BackgroundPhotoDialogFragment.this) == false)
						{
							showInterstitialAd();
						}
					}
				});

				action.run(this, m_background);
				return true;
			}

			case R.id.action_share:
				try
				{
					AnalyticsManager.getInstance().DetailEvent(getContext(), "Toss_Preview_Detail");
					AnalyticsManager.getInstance().DetailEvent(getContext(), "Share_Preview_Detail");
					AnalyticsManager.getInstance().shareEvent(getContext(), "Share_Preview_Detail");
				}
				catch (Exception e)
				{
				}

				if (UserManager.getInstance().isGuest() == true)
				{
					AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAGE_TOSS");
					getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_TOSS));
					return true;
				}

				AnalyticsManager.getInstance().eventUserActionToss(getContext(), "SEND");

				final Intent intent = new Intent(getActivity(), TossSendActivity.class);
				intent.putExtra(KEY_BACKGROUND, m_background);
				getActivity().startActivity(intent);
				return true;
		}

		return false;
	}

	@Override
	protected int getLayout()
	{
		return R.layout.fragment_photo_with_navigation;
	}

	@Override
	protected void onInitToolbar(Toolbar toolbar)
	{
		super.onInitToolbar(toolbar);

		toolbar.inflateMenu(R.menu.fragment_background_photo);
	}

	@Override
	protected void onResourceReady(Bitmap bitmap, Object model)
	{
		super.onResourceReady(bitmap, model);

		final PhotoViewAttacher viewAttacher = getPhotoViewAttacher();

		final float suitableScale = calcSuitableScale(bitmap);

		if (suitableScale > IPhotoView.DEFAULT_MIN_SCALE)
		{
			viewAttacher.setMaximumScale(Float.MAX_VALUE);
			viewAttacher.setMediumScale(suitableScale * 1);
			viewAttacher.setMaximumScale(suitableScale * 2);
		}

		viewAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener()
		{
			@Override
			public void onMatrixChanged(RectF rect)
			{
				updateNavigation(rect);

				if (m_isInit == false)
				{
					m_isInit = true;
					viewAttacher.setScale(suitableScale);
				}
			}
		});

		initNavigation(bitmap);
		getImageView().setOnTouchListener(this);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		return getPhotoViewAttacher().onTouch(v, event);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initNavigation(Bitmap bitmap)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		String ratio = String.valueOf(width) + ":" + String.valueOf(height);

		ConstraintLayout layout = (ConstraintLayout) m_NavigationLayout.getParent();
		ConstraintSet set = new ConstraintSet();
		set.clone(layout);
		set.setDimensionRatio(m_NavigationLayout.getId(), ratio);
		set.applyTo(layout);

		m_NavigationImageView.setImageBitmap(bitmap);
	}

	private void updateNavigation(RectF rect)
	{
		final Point displaySize = DisplayManager.getInstance().getRealDisplaySize(getContext());

		if (rect.left >= 0 && rect.top >= 0 && rect.right <= displaySize.x && rect.bottom <= displaySize.y)
		{
			m_NavigationLayout.setVisibility(View.GONE);
		}
		else
		{
			m_NavigationLayout.setVisibility(View.VISIBLE);

			float canvasW = rect.right - rect.left;
			float canvasH = rect.bottom - rect.top;

			float naviBarW = displaySize.x * m_NavigationLayout.getWidth() / canvasW;
			float naviBarH = displaySize.y * m_NavigationLayout.getHeight() / canvasH;

			ViewGroup.LayoutParams lp = m_NavigationBarView.getLayoutParams();
			lp.width = (int) naviBarW;
			lp.height = (int) naviBarH;
			m_NavigationBarView.setLayoutParams(lp);

			float ratio = canvasW / m_NavigationLayout.getWidth();
			float y = Math.abs(rect.top) / ratio;
			float x = Math.abs(rect.left) / ratio;

			m_NavigationBarView.setX(x);
			if (rect.top >= 0 && rect.bottom <= displaySize.y)
			{
				m_NavigationBarView.setY(0);
			}
			else
			{
				m_NavigationBarView.setY(y);
			}

			if (rect.left >= 0 && rect.right <= displaySize.x)
			{
				m_NavigationBarView.setX(0);
			}
			else
			{
				m_NavigationBarView.setX(x);
			}
		}
	}

	private float calcSuitableScale(Bitmap bitmap)
	{
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();

		return (width * (float) m_screenSize.y) / (height * (float) m_screenSize.x);
	}

	private void showInterstitialAd()
	{
		PreferencesManager preferencesManager = PreferencesManager.getInstance();

		final AbsMainActivity activity = (AbsMainActivity) getActivity();
		int interstitialAdChance = preferencesManager.getInterstitialAdChance(activity) + 1;

		if (AdCenter.checkHitConditionInterstitialAd(activity) == true)
		{
			preferencesManager.setInterstitialAdChance(activity, 0);
			activity.showInterstitialAd(BackgroundPhotoDialogFragment.this, new Runnable()
			{
				@Override
				public void run()
				{
					activity.prepareInterstitialAd();
				}
			});
		}
		else if(AdCenter.checkHitConditionInterstitiaAdFreeDialog(activity) == true)
		{
			if (UserManager.getInstance().isGuest() == false)
			{
				activity.showInterstitialAd(BackgroundPhotoDialogFragment.this, new Runnable()
				{
					@Override
					public void run()
					{
						activity.prepareInterstitialAd();
						PurchaseAdFreeDialogFragment.start(getActivity().getSupportFragmentManager(), "Fullscreenad_Detail");
					}
				});
			}

			preferencesManager.setInterstitialAdChance(activity, ++interstitialAdChance);
		}
		else
		{
			preferencesManager.setInterstitialAdChance(activity, interstitialAdChance);
		}
	}

	//=========================================================================
	// Builder
	//=========================================================================

	public static class Builder extends BaseBuilder<Builder>
	{
		public Builder(Background background)
		{
			m_background = background;
		}

		protected Bundle buildArguments()
		{
			final Bundle args = super.buildArguments();
			args.putParcelable(KEY_BACKGROUND, m_background);
			return args;
		}

		@SuppressWarnings("deprecation")
		public BackgroundPhotoDialogFragment start(FragmentManager fragmentManager)
		{
			final BackgroundPhotoDialogFragment fragment = new BackgroundPhotoDialogFragment();
			final Bundle args = buildArguments();
			fragment.setArguments(args);
			fragment.show(fragmentManager, TAG_FRAGMENT);
			return fragment;
		}

		private final Background m_background;
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String TAG_FRAGMENT = "BACKGROUND_PHOTO_DIALOG_FRAGMENT";
	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";

	//=========================================================================
	// Variables
	//=========================================================================

	private Background m_background;
	private Point m_screenSize;

	private View m_NavigationBarView;
	private ImageView m_NavigationImageView;
	private FrameLayout m_NavigationLayout;

	private Unbinder m_unbinder;

	private boolean m_isInit;
}
