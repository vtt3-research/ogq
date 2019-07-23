package com.ogqcorp.bgh.fragment;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.ogqcorp.bgh.adapter.CommentsAdapter.COMMENT_EMPHASIZE_LIMIT;
import it.sephiroth.android.library.tooltip.Tooltip;
import rx.Subscription;
import rx.functions.Action1;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.gridlayout.widget.GridLayout;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdIconView;
import com.facebook.ads.MediaView;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.ogqcorp.bgh.BuildConfig;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.DownloadAction;
import com.ogqcorp.bgh.action.GifPreviewAction;
import com.ogqcorp.bgh.action.LicenseAction;
import com.ogqcorp.bgh.action.LikeAction;
import com.ogqcorp.bgh.action.LiveWatchPreviewAction;
import com.ogqcorp.bgh.action.Mp4DownloadAction;
import com.ogqcorp.bgh.action.Mp4PreviewAction;
import com.ogqcorp.bgh.action.PreviewAction;
import com.ogqcorp.bgh.action.SetAsContactAction;
import com.ogqcorp.bgh.action.SetAsLiveWatchWallpaperAction;
import com.ogqcorp.bgh.action.SetAsVideoWallpaperAction;
import com.ogqcorp.bgh.action.SetAsWallpaperAction;
import com.ogqcorp.bgh.action.TextAction;
import com.ogqcorp.bgh.action.UserReportAction;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.activity.LicenseGuideActivity;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.adapter.CommentsAdapter;
import com.ogqcorp.bgh.ads.AdCenter;
import com.ogqcorp.bgh.ads.AdCenterCache;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.AdRewardListener;
import com.ogqcorp.bgh.ads.AdRewardLoadListener;
import com.ogqcorp.bgh.ads.AdRewardVideoAdMob;
import com.ogqcorp.bgh.ads.AdRewardVideoInmobi;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.cartoon.CartoonActivity;
import com.ogqcorp.bgh.collection.CollectionCreateDialogFragment;
import com.ogqcorp.bgh.collection.CollectionGuideDetailView;
import com.ogqcorp.bgh.collection.CollectionGuideDialogFragment;
import com.ogqcorp.bgh.collection.CollectionSelectDialogFragment;
import com.ogqcorp.bgh.coverslider.system.CoverFLManagerCompatUtils;
import com.ogqcorp.bgh.expression.ExpressionManager;
import com.ogqcorp.bgh.fragment.explore.BaseRecyclerFragmentEx3;
import com.ogqcorp.bgh.fragment.tag.TagInfoFragment;
import com.ogqcorp.bgh.gcm.BusActivityEvent;
import com.ogqcorp.bgh.gifwallpaper.GifLiveWallpaperFileUtils;
import com.ogqcorp.bgh.model.BackgroundsModel;
import com.ogqcorp.bgh.model.BackgroundsModelData;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.pie.PieInfoFragment;
import com.ogqcorp.bgh.shine.ShineButton;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Backgrounds;
import com.ogqcorp.bgh.spirit.data.Collections;
import com.ogqcorp.bgh.spirit.data.Comment;
import com.ogqcorp.bgh.spirit.data.CommentExtData;
import com.ogqcorp.bgh.spirit.data.Comments;
import com.ogqcorp.bgh.spirit.data.Complete;
import com.ogqcorp.bgh.spirit.data.Image;
import com.ogqcorp.bgh.spirit.data.License;
import com.ogqcorp.bgh.spirit.data.Liker;
import com.ogqcorp.bgh.spirit.data.Likeres;
import com.ogqcorp.bgh.spirit.data.Link;
import com.ogqcorp.bgh.spirit.data.PurchaseInfo;
import com.ogqcorp.bgh.spirit.data.SalesPolicy;
import com.ogqcorp.bgh.spirit.data.SimpleUser;
import com.ogqcorp.bgh.spirit.data.Tag;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.spirit.manager.ContextManager;
import com.ogqcorp.bgh.spirit.manager.FollowManager;
import com.ogqcorp.bgh.spirit.manager.LikesManager;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.bgh.system.ActivityResultManager;
import com.ogqcorp.bgh.system.FLManagerCompatUtils;
import com.ogqcorp.bgh.system.FloatingLikeActionHandler;
import com.ogqcorp.bgh.system.GlideUtils;
import com.ogqcorp.bgh.system.KeyboardChecker;
import com.ogqcorp.bgh.system.NestedScrollViewEx;
import com.ogqcorp.bgh.system.OGQTextMergeHelper;
import com.ogqcorp.bgh.system.RxBus;
import com.ogqcorp.bgh.system.SlackMessage;
import com.ogqcorp.bgh.system.SpannableUtils;
import com.ogqcorp.bgh.system.StaticUtils;
import com.ogqcorp.bgh.system.ViewTransitionHelper;
import com.ogqcorp.bgh.toss.TossSendActivity;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.bgh.user.UserLikerFragment;
import com.ogqcorp.bgh.view.SnowImageView;
import com.ogqcorp.bgh.view.tooltip.Overlay;
import com.ogqcorp.bgh.view.tooltip.ToolTip;
import com.ogqcorp.bgh.watchfacewallpaper.LiveWatchDownloadDialogFragment;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.PreventDoubleTap;
import com.ogqcorp.commons.SimpleTextWatcher;
import com.ogqcorp.commons.SizeDeterminer;
import com.ogqcorp.commons.SizeReadyCallback;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.download.DownloadDialogFragment;
import com.ogqcorp.commons.request.volley.ParseErrorEx;
import com.ogqcorp.commons.utils.ActivityUtils;
import com.ogqcorp.commons.utils.CallbackUtils;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.KeyboardUtils;
import com.ogqcorp.commons.utils.ListenerUtils;
import com.ogqcorp.commons.utils.TextViewUtils;
import com.ogqcorp.commons.utils.ToastUtils;
import com.wefika.flowlayout.FlowLayout;

//import android.util.Log;

public final class BackgroundPageFragment extends BaseRecyclerFragmentEx3 implements SwipeRefreshLayout.OnRefreshListener,
		DownloadDialogFragment.StatusCallback, AdRewardLoadListener, FollowManager.FollowListListener,
		LiveWatchDownloadDialogFragment.StatusCallback, AbsMainActivity.OnKeyBackPressedListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public BackgroundPageFragment()
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
		((AbsMainActivity) activity).addOnKeyBackListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
		{
			m_position = getArguments().getInt(KEY_POSITION);
		}

		final long dataKey = getArguments().getLong(KEY_DATA_KEY);

		m_data = BackgroundsModel.getInstance().get(dataKey, new BaseModel.DataCreator<BackgroundsModelData>()
		{
			@Override
			public BackgroundsModelData newInstance()
			{
				final String dataUrl = getArguments().getString(KEY_DATA_URL);
				Crashlytics.setString("last_func", "BackgroundPageFragment::onCreate() | dataUrl = " + dataUrl);

				if (dataUrl != null)
				{
					final Background background = new Background();
					background.setDataUrl(dataUrl);

					return new BackgroundsModelData(Arrays.asList(background));
				}

				final Background background = getArguments().getParcelable(KEY_BACKGROUND);
				if (background != null)
				{
					return new BackgroundsModelData(Arrays.asList(background));
				}

				Crashlytics.setString("last_func", "BackgroundPageFragment::onCreate() | background = null");
				throw new IllegalStateException("The model data does not exist.");
			}
		});

		BackgroundsModel.getInstance().update(m_data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_background_page, container, false);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		m_handler = new DbclkHandler(this);

		if (m_data.getBackgroundsList().size() > m_position)
		{
			onViewCreatedHelper(view, savedInstanceState);
		} else
		{
			m_scrollView.setVisibility(View.GONE);

			final Response.Listener<Backgrounds> response = new Response.Listener<Backgrounds>()
			{
				@Override
				public void onResponse(Backgrounds backgrounds)
				{
					if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

					onViewCreatedHelper(view, savedInstanceState);
				}
			};

			if (getArguments().getString(KEY_DATA_URL) != null)
			{
				m_position = 0;
				onRefresh();
			} else
			{
				onLoadNext(response, null);
			}
		}

		if (savedInstanceState != null)
		{
			isShowWallpaperButton = savedInstanceState.getBoolean(KEY_IS_SHOW_FAVORITE);
			m_comments = savedInstanceState.getParcelable(KEY_COMMENTS);
			m_similars = savedInstanceState.getParcelable(KEY_SIMILARS);
			m_likeres = savedInstanceState.getParcelable(KEY_LIKERES);

			if (isSelectedFragment() == true)
			{
				getToolbar().setTitle(m_background.getTitle());
			}

			if (isShowWallpaperButton == false)
			{
				m_wallpaperButton.setScaleX(0.1f);
				m_wallpaperButton.setScaleY(0.1f);
				m_wallpaperButton.setAlpha(0f);
			}

			if (hasSimilarBackgrounds() == true)
			{
				constructSimilarBackgrounds(m_similars.getBackgroundsList());
			}

			if (hasLikeres() == true)
			{
				constructLikeres(m_likeres.getLikerlist());
			}
		}

		setLikerVisibleListener();
		setCommentVisibleListener();

		if (UserManager.getInstance().isGuest() == false)
		{
			FollowManager.getInstance().registerFollowListener(this);
		}

		m_keyboardChecker = new KeyboardChecker(getActivity(), getView(), new KeyboardChecker.OnKeyboardVisibleListener()
		{
			@Override
			public void onKeyboardVisible(boolean isVisible)
			{
				if (isVisible == true && m_scrollView != null && getUserVisibleHint() == true)
				{
					m_scrollView.post(new Runnable()
					{
						@Override
						public void run()
						{
							if (m_scrollView != null)
							{
								m_scrollView.smoothScrollBy(0, m_scrollView.getHeight());
							}
						}
					});
				}
			}
		});
		syncToolbarY();

		ActivityResultManager.registerCallback(getContext(), m_resultCallback);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onStart()");

		try
		{
			if (getUserVisibleHint() == true)
			{
				String screenName = getClass().getSimpleName();
				AnalyticsManager.getInstance().screen(getContext(), screenName);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onResume()");

		autoPlayRetry();
	}

	@Override
	public void onPause()
	{
		super.onPause();
		//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onPause()");

		autoPlayPause();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		if (m_background != null)
		{
			menu.clear();
			inflater.inflate(R.menu.fragment_background, menu);
			m_menu = menu;

			updateOptionMenu();

			getToolbar().setTitle(m_background.getTitle());

			AsyncStats.statsBackgroundsDetail(m_background);
			AnalyticsManager.getInstance().eventPageVisit(getActivity());

			loadSimilarBackgrounds();
			loadLikeres();
			loadComments();
			setBottomBuyBar();

			onInitActionBar();
		}
	}

	private void updateOptionMenu()
	{
		if (m_menu != null)
		{
			boolean isLiveScreen = m_background.isLiveScreen();
			boolean isMe = UserManager.getInstance().isMe(m_background.getUser());

			m_menu.findItem(R.id.action_edit_posts).setVisible(isMe);
			m_menu.findItem(R.id.action_set_as_contact).setVisible(!isLiveScreen);
			m_menu.findItem(R.id.action_text).setVisible(isLiveScreen == false && OGQTextMergeHelper.isEnabled(getContext()) == true);
			m_menu.findItem(R.id.action_delete).setVisible(isMe);
			m_menu.findItem(R.id.action_report).setVisible(!isMe);
			m_menu.findItem(R.id.action_report).setShowAsAction(!isMe && isLiveScreen ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int menuId = item.getItemId();

		switch (menuId)
		{
			case R.id.action_set_as_contact:
				onClickSetAsContact();
				return true;

			case R.id.action_report:
				onClickImageReport(m_background);
				return true;

			case R.id.action_edit_posts:
				onClickEditPosts();
				return true;

			case R.id.action_text:
				onClickText();
				return true;

			case R.id.action_go_home:
				onClickGoHome();
				return true;

			case R.id.action_delete:
				onClickDelete();
				return true;
		}

		return false;
	}

	@Override
	public void onDestroyView()
	{
		//Log.d("CBK","BackgroundPageFragment - onDestroyView()");
		releaseExoPlayer();

		try
		{
			AnalyticsManager.getInstance().DetailEvent(getActivity(), "Back_Detail");
		}
		catch (Exception ignored)
		{
		}

		((AbsMainActivity) getActivity()).removeOnKeyBackListener(this);

		if (m_subscription != null)
			m_subscription.unsubscribe();

		if (m_progressRewardAdDialog != null)
			m_progressRewardAdDialog.dismiss();

		if (m_popupRewardAdDialog != null)
			m_popupRewardAdDialog.dismiss();

		if (m_rewardAdListener != null)
			m_rewardAdListener = null;

		removeRewardAdToolTip();

		if (UserManager.getInstance().isGuest() == false)
			FollowManager.getInstance().unregisterFollowListener(this);

		if (m_resultCallback != null)
		{
			ActivityResultManager.unregisterCallback(getContext(), m_resultCallback);
		}

		m_keyboardChecker.clear();

		KeyboardUtils.hideSoftKeyboard(getActivity());
		m_handler.release();

		super.onDestroyView();
		m_unbinder.unbind();
	}

	@Override
	public void onRelease()
	{
		// Nothing
	}

	@Override
	public boolean onBackPressed()
	{
		if (m_CollectionGuide != null)
		{
			m_CollectionGuide.cleanUp();
			m_CollectionGuide = null;
			return true;
		}

		return false;
	}

	@Override
	public void onRefresh()
	{
		if (m_contentLayout.getDescendantFocusability() != ViewGroup.FOCUS_BLOCK_DESCENDANTS)
		{
			m_contentLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		}

		final Response.Listener<Background> response = new Response.Listener<Background>()
		{
			@Override
			public void onResponse(Background background)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				m_data.getBackgroundsList().set(m_position, background);
				/* COVER */
				if (CoverFLManagerCompatUtils.isCoverLikeUpdated())
				{
					CoverFLManagerCompatUtils.getInstance().coverSyncLikeOne(background);
				}

				onViewCreatedHelper(getView(), null);

				if (getUserVisibleHint() == true)
				{
					updateOptionMenu();
					getToolbar().setTitle(m_background.getTitle());

					loadSimilarBackgrounds();
					loadLikeres();
					loadComments();
				}
			}
		};

		final Response.ErrorListener errorResponse = new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError volleyError)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				try
				{
					showErrorDialog(volleyError);
				}
				catch (Exception e)
				{
					// Nothing
				}
			}
		};

		m_background = m_data.getBackgroundsList().get(m_position);

		m_data.invalidate();
		m_data.requestUpdate(m_background.getDataUrl(), response, errorResponse);

		m_comments = null;
		m_isCommentPosting = false;

		m_commentsProgress.setVisibility(View.VISIBLE);
		m_commentsContainer.setVisibility(View.GONE);
		m_scrollView.smoothScrollTo(0, 0);

		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	protected void setActionBarAlpha(int alpha)
	{
		final Callback callback = CallbackUtils.getCallback(this, Callback.class);

		if (callback != null)
		{
			callback.onActionBarAlpha(m_position, alpha);
		} else
		{
			super.setActionBarAlpha(alpha);
		}
		updateToolbarThemeColor(alpha);
	}

	@Override
	protected void scrollChange(int dy)
	{
		if (isShowWallpaperButton == true)
		{
			if (m_userContainer.getHeight() + m_previewLayout.getHeight() - m_scrollView.getScrollY() < getToolbarHeight() + m_wallpaperButton.getHeight() / 2)
			{
				m_wallpaperButton.animate()
						.setInterpolator(OVERSHOOT_INTERPOLATOR)
						.setDuration(300).scaleX(0.1f).scaleY(0.1f).alpha(0.0f).start();

				isShowWallpaperButton = false;
				removeRewardAdToolTip();
			}
		} else
		{
			if (m_userContainer.getHeight() + m_previewLayout.getHeight() - m_scrollView.getScrollY() > getToolbarHeight() + m_wallpaperButton.getHeight() / 2)
			{
				m_wallpaperButton.animate()
						.setInterpolator(OVERSHOOT_INTERPOLATOR)
						.setDuration(300).scaleX(1.0f).scaleY(1.0f).alpha(1.0f).start();

				isShowWallpaperButton = true;
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_IS_SHOW_FAVORITE, isShowWallpaperButton);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, String[] permissions, int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						switch (requestCode)
						{
							case 101:
								onClickSetAsWallpaper();

								if (m_isPremissionGuide == false)
									AnalyticsManager.getInstance().RequestPermission(getContext(), "SetAsBackground_Detail_Auth_OK");
								break;
							case 102:
								onClickSetAsContact();

								if (m_isPremissionGuide == false)
									AnalyticsManager.getInstance().RequestPermission(getContext(), "SetAsContact_OverFlow_Detail_Auth_OK");
								break;
							case 103:
								onClickDownload();

								if (m_isPremissionGuide == false)
									AnalyticsManager.getInstance().RequestPermission(getContext(), "Download_Detail_Auth_OK");
								break;
							case 105:
								onClickText();

								if (m_isPremissionGuide == false)
									AnalyticsManager.getInstance().RequestPermission(getContext(), "OGQText_Detail_Auth_OK");
								break;
							case 106:
								onClickPreview();

								if (m_isPremissionGuide == false)
									AnalyticsManager.getInstance().RequestPermission(getContext(), "Preview_Detail_Auth_OK");
								break;
							case 107:
								playPreview(true);
								break;
						}
					}
					catch (Exception ignored)
					{
					}
				}
			});
		} else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0]) == false && m_isPremissionGuide == false)
			{
				final MaterialDialog.SingleButtonCallback onNegative = new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						final Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
						final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
						startActivity(intent);
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

				MaterialDialog dialog = new MaterialDialog.Builder(getContext())
						.customView(R.layout.fragment_permission_storage_retry, true)
						.negativeText(R.string.str_setting)
						.onNegative(onNegative)
						.positiveText(R.string.ok)
						.onPositive(onPositive)
						.show();

				// OGQ Text
				if (requestCode == 105)
				{
					TextView desc = dialog.getCustomView().findViewById(R.id.description);
					desc.setText(R.string.need_write_storage_permission_ogqtext_description);
				}
			}
		}

	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(Fragment sourceFragment, int position)
	{
		final Fragment fragment = new BackgroundPageFragment();

		final Bundle args = new Bundle();
		args.putInt(KEY_POSITION, position);
		fragment.setArguments(args);

		final long dataKey = BaseModel.getDataKey(sourceFragment);
		return BaseModel.wrap(fragment, dataKey);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(String dataUrl)
	{
		final Fragment fragment = new BackgroundPageFragment();

		final Bundle args = new Bundle();
		args.putInt(KEY_POSITION, Integer.MAX_VALUE);
		args.putString(KEY_DATA_URL, dataUrl);
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(Background background)
	{
		final Fragment fragment = new BackgroundPageFragment();

		final Bundle args = new Bundle();
		args.putInt(KEY_POSITION, 0);
		args.putParcelable(KEY_BACKGROUND, background);
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	@OnClick(R.id.preview)
	public void onClickPreview()
	{
		//Log.d("[CBK] BackgroundPageFragment", "onClickPreview()");

		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Preview_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 106) == true)
			return;
		// [WatchFace Live WallPaper]
/*
		if (m_background.isExtensionLiveWatchType() == true)
		{
			if (m_background.getExtension() != null &&
					m_background.getExtension().getPreview() != null &&
					m_background.getExtension().getPreview().getisWeather() == true)
			{
				if (requestWeatherPermission(106) == true)
					return;
			}
		} else
		{
			if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 106) == true)
				return;
		}
*/
		playPreview(false);
	}

	public void onClickLike()
	{
		removeRewardAdToolTip();

		if (UserManager.getInstance().isGuest() == true)
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAGE_LIKE");
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_LIKE));
			m_likeButton.setChecked(false);
			return;
		}

		new LikeAction().setLikeBtn(m_likeButton).run(this, m_background);
		updateLikeCount();
	}

	public void onClickDownload()
	{
		removeRewardAdToolTip();

		onClickDownloadHelper();
		AnalyticsManager.getInstance().eventPageDownload(getActivity());
	}

	public void onClickToss()
	{
		removeRewardAdToolTip();

		if (UserManager.getInstance().isGuest() == true)
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAGE_TOSS");
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_TOSS));
			return;
		}

		AnalyticsManager.getInstance().eventUserActionToss(getContext(), "SEND");

		final Intent intent = new Intent(getActivity(), TossSendActivity.class);
		intent.putExtra(KEY_BACKGROUND, m_background);
		getActivity().startActivity(intent);
	}

	@OnClick(R.id.license)
	public void onClickLicense()
	{
		removeRewardAdToolTip();

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			new LicenseAction().run(this, m_background);
		}
	}

	@OnClick(R.id.likes_count)
	public void onClickLikeCount()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "LikeList_Detail");
		}
		catch (Exception e)
		{
		}

		removeRewardAdToolTip();

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			AnalyticsManager.getInstance().eventUserActionLikersMore(getContext(), m_background.getUuid());
			AbsMainActivity.getTabStackHelper(this).showFragment(UserLikerFragment.newInstance(m_background));
		}
	}

	@OnClick({ R.id.user, R.id.user_name, R.id.user_username })
	public void onClickUser()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "CreatorProfile_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		AnalyticsManager analyticsManager = AnalyticsManager.getInstance();

		analyticsManager.eventPageUser(getActivity());

		String username = m_background.getUser().getUsername();

		analyticsManager.eventUserActionProfile(getContext(), "PAGE");
		analyticsManager.eventUserTrackingProfile(getContext(), username);

		if (UserManager.getInstance().isMe(username) == false)
		{
			analyticsManager.eventUserActionOtherProfile(getActivity(), "PAGE");
		}

		Fragment fragment = UserInfoFragment.newInstance(UrlFactory.usersInfo(username));
		AbsMainActivity.getTabStackHelper(this).showFragment(fragment);
	}

	@OnClick(R.id.fallow)
	public void onClickFollow()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Follow_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_C) == false)
			return;

		if (UserManager.getInstance().isGuest() == true)
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAGE_FALLOW");
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_FOLLOW));
		} else
		{
			m_follow.setText("...");
		}

		final FollowManager.OnFollowCallback callback = new FollowManager.OnFollowCallback()
		{
			@Override
			public void onIsFollowing(SimpleUser user, boolean isFollower)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				if (isFollower == true)
				{
					m_follow.setVisibility(View.GONE);
					AnalyticsManager.getInstance().eventUserActionFollow(getContext(), "PAGE");
				} else
				{
					m_follow.setText(R.string.userinfo_follow);
				}
			}
		};

		FollowManager.getInstance().toggleFollow(SimpleUser.convert(m_background.getUser()), callback);
	}

	public void onClickDownloadHelper()
	{
		removeRewardAdToolTip();

		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 103) == true)
		{
			return;
		}

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			final DownloadAction action = (m_background.isLiveScreen() == true) ? new Mp4DownloadAction() : new DownloadAction();

			action.setDownloadMode(0);
			action.run(this, m_background);
		}
	}

	@OnClick(R.id.adfree)
	public void onClickAdFree()
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_EDIT_POST)
		{
			if (resultCode == SimpleUploadFilterFragment.RESULT_EDIT_OK)
			{
				ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_update_delay).show();
				onRefresh();
			} else if (resultCode == SimpleUploadFilterFragment.RESULT_DELETE_OK)
			{
				getActivity().onBackPressed();
				m_modifiedThis = true;
			}
		}
	}

	public static boolean m_modifiedThis = false;

	public void onClickEditPosts()
	{
		removeRewardAdToolTip();

		AnalyticsManager.getInstance().eventUserActionEditPost(getActivity(), "PAGE");

		startActivityForResult(new Intent(UploadActivity.createIntent(getActivity(), m_background)), REQUEST_EDIT_POST);
	}

	private static int REQUEST_EDIT_POST = 7000;

	public void onClickText()
	{
		removeRewardAdToolTip();

		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 105) == true)
		{
			return;
		}

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			DownloadAction action = new TextAction();
			action.setDownloadMode(5);
			action.run(this, m_background);
		}
	}

	public void onClickGoHome()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Home_Detail");
		}
		catch (Exception ignored)
		{
		}

		((AbsMainActivity) getActivity()).onClickDrawerExplore();
	}

	public void onClickDelete()
	{
		AnalyticsManager.getInstance().eventUserActionDeletePost(getContext(), "PAGE");
		new MaterialDialog.Builder(getActivity())
				.content(R.string.upload_content_delete_confirm)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.positiveText(R.string.ok)
				.negativeText(R.string.cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						requestDeletePost();
					}
				})
				.show();
	}

	@Override
	protected void onInitActionBar()
	{
		setActionBarAlpha(255);

		Toolbar toolbar = getToolbar();

		if (toolbar != null && isAdded() == true && getUserVisibleHint() == true)
		{
			int color = getResources().getColor(R.color.black);

			toolbar.setBackgroundResource(R.drawable.actionbar_bg);
			if (toolbar.getOverflowIcon() != null)
			{
				toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			toolbar.setElevation(0);
		}
	}

	private void updateToolbarThemeColor(int alpha)
	{
		Toolbar toolbar = getToolbar();
		if (toolbar != null && isAdded() == true && getUserVisibleHint() == true)
		{
			int color = getResources().getColor(alpha == 255 ? R.color.black : R.color.white);
			toolbar.setTitleTextColor(ColorUtils.setAlphaComponent(color, alpha));
			toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

			if (toolbar.getOverflowIcon() != null)
			{
				toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}
	}

	//========================================================================
	// DownloadStatus Callback
	//========================================================================

	@Override
	public void onCancel(DownloadDialogFragment fragment, String url, File file, Bundle bundle)
	{
		// Nothing
	}

	@Override
	public void onComplete(DownloadDialogFragment fragment, String url, final File file, Bundle bundle)
	{
		if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false)
		{
			final Background background = bundle.getParcelable(KEY_BACKGROUND);
			final int downloadMode = bundle.getInt("KEY_MODE");

			try
			{
				switch (downloadMode)
				{
					case 0: // download
						//Log.d("[CBK] BackgroundPageFragment", "onComplete() - download");
						if (m_background.isLiveScreen() == false)
						{
							new DownloadAction().downloadFile(BackgroundPageFragment.this, background, file);
						}
						showInterstitialAd();
						break;

					case 1: // preview
						//Log.d("[CBK] BackgroundPageFragment", "onComplete() - preview");
						DownloadAction previewAction = null;
						if (m_background.isLiveScreen() == true)
						{
							previewAction = new Mp4PreviewAction();
						} else
						{
							// GIF
							if (m_background.getImage() != null &&
									m_background.getImage().getUrl() != null && m_background.getImage().getUrl().isEmpty() == false &&
									m_background.getImage().getUrl().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION) == true)
								previewAction = new GifPreviewAction();
							else
								previewAction = new PreviewAction();
						}

						previewAction.downloadFile(BackgroundPageFragment.this, background, file);
						//new PreviewAction().downloadFile(BackgroundPageFragment.this, background, file);
						break;

					case 2: // set as wallpaper
						//Log.d("[CBK] BackgroundPageFragment", "onComplete() - set as wallpaper");
						DownloadAction setAs = (m_background.isLiveScreen() == true) ? new SetAsVideoWallpaperAction() : new SetAsWallpaperAction();
						setAs.downloadFile(BackgroundPageFragment.this, background, file);
						break;

					case 5: // text
						new TextAction().run(this, m_background);
						break;
				}
			}
			catch (Exception e)
			{
				ToastUtils.makeErrorToast(fragment.getActivity(), Toast.LENGTH_SHORT,
						"%s\n%s", fragment.getString(R.string.error_has_occurred), e.toString()/**/).show();
			}
		}
	}

	@Override
	public void onLiveWatchDownloadCancel(LiveWatchDownloadDialogFragment fragment, File ldwFile, File backgroundFile, Bundle bundle)
	{
		try
		{
			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false)
			{
				if (m_background.isExtensionLiveWatchType() == true)
				{
					FileUtils.deleteQuietly(ldwFile);
					FileUtils.deleteQuietly(backgroundFile);

					PreferencesManager.getInstance().setLiveWatchDownloadInfo(getContext(), m_background.getUuid(),
							null, null, false);
					PreferencesManager.getInstance().setSetAsWallpaperLDWFilePath(getContext(), null);
					PreferencesManager.getInstance().setSetAsWallpaperBackgroundFilePath(getContext(), null);
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment onLiveWatchDownloadCancel Exception");
			FirebaseCrashLog.logException(e);
		}
	}

	@Override
	public void onLiveWatchDownloadComplete(LiveWatchDownloadDialogFragment fragment, File ldwFile, File backgroundFile, Bundle bundle)
	{
		if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false)
		{
			Background background = bundle.getParcelable(KEY_BACKGROUND);
			int downloadMode = bundle.getInt("KEY_MODE");

			if (m_background.isExtensionLiveWatchType() == true)
			{
				switch (downloadMode)
				{
					case 1: // preview
						DownloadAction previewAction = new LiveWatchPreviewAction();
						previewAction.downloadFile(BackgroundPageFragment.this, background, ldwFile);
						break;
					case 2: // set as wallpaper
						DownloadAction setAs = new SetAsLiveWatchWallpaperAction();
						setAs.downloadFile(BackgroundPageFragment.this, background, ldwFile);
						break;
				}
			}
		}
	}

	@OnClick(R.id.wallpaper)
	public void onClickSetAsWallpaper()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "SetAsBackground_Detail");
		}
		catch (Exception ignored)
		{
		}

		try
		{
			// 동영상
			if (m_background.isLiveScreen())
			{
				setAsLiveScreenWallpaper();
			}
			// [WatchFace Live WallPaper] 워치페이스
			else if (m_background.isExtensionLiveWatchType())
			{
				setAsLiveWatchWallpaper();
			}
			// 이미지
			else
			{
				setAsWallpaper();
			}
		}
		catch (Exception ignored)
		{
		}
	}

	public void onClickPurchase()
	{
		if (m_purchaseInfo == null)
			return;

		final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
				.title(R.string.gallery_dialog_buy_license)
				.customView(R.layout.dialog_purchase_license, false)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.show();

		final List<SalesPolicy> licenseList = m_purchaseInfo.getLicenseList();

		final ViewGroup customView = (ViewGroup) dialog.getCustomView();

		final Button buy = customView.findViewById(R.id.buy);
		final Button cancel = customView.findViewById(R.id.cancel);
		final TextView description = customView.findViewById(R.id.description);
		final ViewGroup radioGroup = customView.findViewById(R.id.radio_container);

		boolean hasLicense = false;
		for (SalesPolicy policy : licenseList)
		{
			if (policy.getLicense().equals(SalesPolicy.LICENSE_BACKGROUND))
			{
				hasLicense = true;
				LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.item_license, null);
				view.setPadding(0, 0, 0, 0);

				RadioButton btn_background = view.findViewById(R.id.title);
				btn_background.setText(getLicenseTitle(policy.getLicense()));
				btn_background.setChecked(true);
				btn_background.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_button_diable_selector, 0, 0, 0);

				TextView price = view.findViewById(R.id.price);
				price.setText(policy.getPrice());
				radioGroup.addView(view, 0);

			}
		}

		if (hasLicense == false)
		{
			dialog.dismiss();
			return;
		}

		buy.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
				purchase();
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

		if (description != null)
		{
			String content = getString(R.string.gallery_dialog_select_buy_license_description);
			String word = getString(R.string.gallery_dialog_select_license_description_key);

			int start = content.lastIndexOf(word);
			int end = start + word.length();

			ClickableSpan clickableSpan = new ClickableSpan()
			{
				@Override
				public void onClick(View widget)
				{
					final Intent intent = new Intent(getContext(), LicenseGuideActivity.class);
					intent.putExtra(LicenseGuideActivity.LAYOUT_RES_ID, R.layout.activity_livescreen_license_guide);
					getContext().startActivity(intent);
				}
			};

			try
			{
				Spannable span = (Spannable) description.getText();
				span.setSpan(clickableSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				span.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				span.setSpan(new ForegroundColorSpan(0xFF0AE27B), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

				description.setMovementMethod(LinkMovementMethod.getInstance());
			}
			catch (Exception e)
			{
				e.toString();
			}
		}
	}

	private String getLicenseTitle(String license)
	{
		Context context = getContext();
		switch (license)
		{
			case SalesPolicy.LICENSE_BACKGROUND:
				return context.getString(R.string.gallery_dialog_select_license_background);
			default:
				return null;
		}
	}

	public void onClickSetAsContact()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "SetAsContact_OverFlow_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 102) == true)
		{
			return;
		}

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			new SetAsContactAction().run(this, m_background);

			AnalyticsManager.getInstance().eventPageSetAsContact(getActivity());
		}
	}

	@CalledByReflection
	public void onClickTag(View tagView)
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Tag_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		final Tag tag = (Tag) tagView.getTag();

		Fragment fragment = TagInfoFragment.newInstance(tag);
		AbsMainActivity.getTabStackHelper(BackgroundPageFragment.this).showFragment(fragment);

		/*final String dataUrl = tag.getDataUrl();
		onOpenBackgrounds(dataUrl);*/

		AnalyticsManager.getInstance().eventPageTag(getActivity());
	}

	// Function : RewardAd(Admob/Vungle/Unity)
	// 배경화면 스와이프에 따른 리워드 광고 미리 로딩
	@Override
	public void OnRewardAdLoad(int position)
	{
		removeRewardAdToolTip();
		setRewardAdShowCheck(false, null);
		constructRewardAd(position);

		try
		{
			if (position != -1 && getUserVisibleHint() == true)
			{
				String screenName = getClass().getSimpleName();
				AnalyticsManager.getInstance().screen(getContext(), screenName);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	// Function : RewardAd(Admob/Vungle/Unity)
	// 동영상 리워드 광고 시청 여부 팝업 체크 박스
	/*
	@CalledByReflection
	public void onRewardAdCheckBox(View view)
	{
		try
		{
			if(view != null){
				CheckBox checkBox = (CheckBox) view;
				boolean ischeck = checkBox.isChecked();
				PreferencesManager.getInstance().setAdRequestPopupShow(getContext(), !ischeck);
			}
		}  catch (Exception e)
		{
		}
	}
	*/

	public void unLockContent()
	{
		String completeUrl = "";

		// [WatchFace Live WallPaper]
		if (m_background.isLiveScreen() == true)
		{
			completeUrl = m_background.getLiveScreen().getComplete().getUrl();
		} else if (m_background.isExtensionLiveWatchType() == true)
		{
			completeUrl = m_background.getExtension().getComplete().getUrl();
		}

		Requests.requestByPut(completeUrl, null, Complete.class, new Response.Listener<Complete>()
		{
			@Override
			public void onResponse(Complete response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				setRewardAdShowCheck(true, response.getUrl());
				showRewardAdToolTip();
				if (m_progressRewardAdDialog != null)
					m_progressRewardAdDialog.dismiss();

			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				if (m_progressRewardAdDialog != null)
					m_progressRewardAdDialog.dismiss();

				ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void purchase()
	{
		showProgressDialog();

		String url = null;
		HashMap params = null;

		//현재 버전에서는 Download("WEB") License 만 구매 가능함
		url = UrlFactory.purchaseImage();
		params = ParamFactory.purchaseImage("google", m_background.getUuid(), "WEB");

		Requests.authRequestByPost(url, params, Object.class, new Response.Listener<Object>()
		{
			@Override
			public void onResponse(Object response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				m_progressDialog.dismiss();
				//unLockContent();
				loadPurchaseLicense(true);
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				m_progressDialog.dismiss();

				try
				{
					switch (error.networkResponse.statusCode)
					{
						case 400:
						{
							ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.already_purchased_content).show();
							break;
						}
						case 403:
						{
							Fragment fragment = PieInfoFragment.newInstance();
							AbsMainActivity.getTabStackHelper(BackgroundPageFragment.this).showFragment(fragment);
							break;
						}
						default:
						{
							ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
						}
					}
				}
				catch (Exception e)
				{
					ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
				}
			}
		});
	}

	private void showProgressDialog()
	{
		m_progressDialog = new MaterialDialog.Builder(getActivity())
				.content(R.string.processing)
				.progress(true, 0)
				.cancelable(false)
				.show();
	}

	private void requestDeletePost()
	{
		showProgressDialog();

		Requests.authRequestByDelete(UrlFactory.delete(m_background.getUuid()), null, Empty.class, new Response.Listener<Empty>()
		{
			@Override
			public void onResponse(Empty response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true)
				{
					return;
				}

				m_progressDialog.dismiss();

				ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.detele_post_success).show();
				getActivity().onBackPressed();
				m_modifiedThis = true;
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				m_progressDialog.dismiss();

				ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	private void setAsLiveScreenWallpaper()
	{
		//무료 상품인 경우
		if (m_background.isPublicLiveScreen() == true)
		{
			setAsWallpaper();
		}
		//유료 상품인 경우
		else if (m_background.isPaidContent() == true)
		{
			if (UserManager.getInstance().isGuest() == true)
			{
				//AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAID_LIVE_SCREEN");
				startActivity(AuthActivity.createIntent(getContext(), AuthActivity.SIGN_ACTION_NONE));
				return;
			}

			SalesPolicy licenseBG = m_purchaseInfo.getBackgroundLicense();

			boolean isPurchased = false;
			if (licenseBG != null && licenseBG.isPurchased() == true)
			{
				isPurchased = true;
			}

			// 광고를  안본 경우
			if (isPurchased == false)
			{
				onClickPurchase();
			} else
			{
				setAsWallpaper();
			}
		}
		// 리워드 광고 시청이 필요한 상품인 경우
		else
		{
			removeRewardAdToolTip();

			boolean isGuest = UserManager.getInstance().isGuest();
			if (isGuest)
			{
				AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_GUEST");
			} else
			{
				AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_NOT_GUEST");
			}

			boolean Adshow = PreferencesManager.getInstance().isRewardAdShowContents(getContext(), m_background.getUuid());
			//Adshow = true;

			// 광고를  안본 경우
			if (Adshow == false)
			{
				setRewardAd();
			} else
			{
				setAsWallpaper();
			}
		}
	}

	private void setAsLiveWatchWallpaper()
	{
		removeRewardAdToolTip();

		boolean isGuest = UserManager.getInstance().isGuest();
		if (isGuest)
		{
			AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_GUEST");
		} else
		{
			AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_NOT_GUEST");
		}

		boolean Adfree = m_background.isFree();
		boolean Adshow = PreferencesManager.getInstance().isRewardAdShowContents(getContext(), m_background.getUuid());

		//무료 상품인 경우
		if (Adfree == true)
		{
			setAsWallpaper();
		}
		//파이 구매 상품인 경우
		else if (m_background.isPaidContent() == true)
		{
			if (UserManager.getInstance().isGuest() == true)
			{
				//AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAID_LIVE_WATCH");
				startActivity(AuthActivity.createIntent(getContext(), AuthActivity.SIGN_ACTION_NONE));
				return;
			}

			// 배경화면 라이센스 구매한 경우
			if (m_purchaseInfo.isPurchaseBackgroundLicense() == true)
			{
				setAsWallpaper();
			} else
			{
				onClickPurchase();
			}
		} else
		{
			/*
				// LIVE WATCH 광고 안보고 테스트
				if(Adshow == false)
				{
					String completeUrl = m_background.getExtension().getComplete().getUrl();
					//String completeUrl2 = PreferencesManager.getInstance().getRewardAdRequestUrl(getContext(), m_background.getUuid());

					//if(completeUrl2 != null && completeUrl2.length() > 0)
					//	completeUrl = completeUrl2;
					Requests.requestByPut(completeUrl, null, Complete.class, new Response.Listener<Complete>()
					{
						@Override
						public void onResponse(Complete response)
						{
							if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

							setRewardAdShowCheck(true, response.getUrl());
						}
					}, new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
						}
					});
				}
			*/

			// 광고를  안본 경우
			if (Adshow == false)
			{
				setRewardAd();
			} else
			{
				setAsWallpaper();
			}
		}
	}

	private void setAsWallpaper()
	{
		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101) == true)
			return;
		// [WatchFace Live WallPaper]
/*
		if (m_background.isExtensionLiveWatchType() == true)
		{
			if (m_background.getExtension() != null &&
					m_background.getExtension().getPreview() != null &&
					m_background.getExtension().getPreview().getisWeather() == true)
			{
				if (requestWeatherPermission(101) == true)
					return;
			}
		} else
		{
			if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101) == true)
				return;
		}
*/
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			final DownloadAction action;

			if (m_background.isLiveScreen() == true)
			{
				action = new SetAsVideoWallpaperAction();
			} else if (m_background.isExtensionLiveWatchType() == true)
			{
				action = new SetAsLiveWatchWallpaperAction();
			} else
			{
				action = new SetAsWallpaperAction();
			}

			action.setDownloadMode(2);
			action.run(BackgroundPageFragment.this, m_background);

			if (m_background.isLiveScreen() == true)
			{
				try
				{
					AdCenterCache.getInstance().cacheNativeBanner(getContext());
				}
				catch (Exception ignored)
				{
				}

				AnalyticsManager.getInstance().eventPageSetAsWallpaper(getActivity(), "VIDEO");
			} else if (m_background.isExtensionLiveWatchType() == true)
			{
				try
				{
					AdCenterCache.getInstance().cacheNativeBanner(getContext());
				}
				catch (Exception ignored)
				{
				}

				AnalyticsManager.getInstance().eventPageSetAsWallpaper(getActivity(), "LIVEWATCH");
			} else
			{
				// GIF
				if (m_background.getImage() != null &&
						m_background.getImage().getUrl() != null && m_background.getImage().getUrl().isEmpty() == false &&
						m_background.getImage().getUrl().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION) == true)
				{
					try
					{
						AdCenterCache.getInstance().cacheNativeBanner(getContext());
					}
					catch (Exception ignored)
					{
					}
					AnalyticsManager.getInstance().eventPageSetAsWallpaper(getActivity(), "GIF");
				} else
				{
					AnalyticsManager.getInstance().eventPageSetAsWallpaper(getActivity(), "MY");
				}
			}

		}
	}

	private void setRewardAd()
	{
		try
		{
			//boolean popupShow = PreferencesManager.getInstance().getAdRequestPopupShow(getContext());

			//if(popupShow)
			//{
			AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_POPUP");
			if (m_popupRewardAdDialog == null)
			{
				m_popupRewardAdDialog = new MaterialDialog.Builder(getActivity())
						//.customView(R.layout.rewardad_popup_dialog, false)
						.content(R.string.rewardad_license_dialog)
						.autoDismiss(true)
						.cancelable(true)
						.canceledOnTouchOutside(true)
						.positiveText(R.string.rewardad_license_dialog_button)
						.negativeText(R.string.cancel)
						.cancelListener(new DialogInterface.OnCancelListener()
						{
							@Override
							public void onCancel(DialogInterface dialog)
							{
								AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_CANCEL");

								if (m_popupRewardAdDialog != null)
								{
									m_popupRewardAdDialog.dismiss();
									m_popupRewardAdDialog = null;
								}
							}
						})
						.onPositive(new MaterialDialog.SingleButtonCallback()
						{
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
							{
								playRewardAd();

								if (m_popupRewardAdDialog != null)
								{
									m_popupRewardAdDialog.dismiss();
									m_popupRewardAdDialog = null;
								}
							}
						})
						.onNegative(new MaterialDialog.SingleButtonCallback()
						{
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
							{
								AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_CANCEL");

								if (m_popupRewardAdDialog != null)
								{
									m_popupRewardAdDialog.dismiss();
									m_popupRewardAdDialog = null;
								}
							}
						})
						.build();

				final ViewGroup customView = (ViewGroup) m_popupRewardAdDialog.getCustomView();

				//ListenerUtils.setOnClickListener(customView, R.id.again_question_box, this, "onRewardAdCheckBox");

				m_popupRewardAdDialog.show();
			} else
			{
				m_popupRewardAdDialog.show();
			}
			//} else
			//{
			//	playRewardAd();
			//}
		}
		catch (Exception ignored)
		{
		}
	}

	private void playRewardAd()
	{
		try
		{
			//Log.d("CBK","BackgroundPageFragment - playRewardAd()");

			AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_REQUEST");
			showRewardAdProgress();

			if (ContextManager.getInstance().isChinaCountry() == true)
			{
				AdRewardVideoInmobi.getInstance().ShowRewardAd(getContext(), m_rewardAdListener);
			} else
			{
				AdRewardVideoAdMob.getInstance().ShowRewardAd(getContext(), m_rewardAdListener);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private void CreateRewardAdListener()
	{
		try
		{
			if (m_rewardAdListener == null)
			{
				m_rewardAdListener = new AdRewardListener()
				{

					@Override
					public void onRewardAdClosed()
					{
						//Log.d("CBK","BackgroundPageFragment - onRewardAdClosed()");
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

						AdRewardVideoAdMob.getInstance().removeRewardAdListener();

						if (m_progressRewardAdDialog != null)
							m_progressRewardAdDialog.dismiss();

						if (ActivityUtils.isDestroyed(getActivity()) == false)
						{
							AdRewardVideoAdMob.getInstance().loadRewardAd(getActivity());
						}
					}

					@Override
					public void onRewardedLeftApp()
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
						//Log.d("CBK","BackgroundPageFragment - onRewardedLeftApp()");

						if (m_progressRewardAdDialog != null)
							m_progressRewardAdDialog.dismiss();
					}

					@Override
					public void onRewarded()
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
						//Log.d("CBK","BackgroundPageFragment - onRewarded()");
						if (getContext() != null)
						{
							AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_REWARD");
						}

						unLockContent();
						/*String completeUrl = "";

						// [WatchFace Live WallPaper]
						if (m_background.isLiveScreen() == true)
						{
							completeUrl = m_background.getLiveScreen().getComplete().getUrl();
						}
						else if (m_background.isExtensionLiveWatchType() == true)
						{
							completeUrl = m_background.getExtension().getComplete().getUrl();
						}

						Requests.requestByPut(completeUrl, null, Complete.class, new Response.Listener<Complete>()
						{
							@Override
							public void onResponse(Complete response)
							{
								if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

								setRewardAdShowCheck(true, response.getUrl());
								showRewardAdToolTip();
								if (m_progressRewardAdDialog != null)
									m_progressRewardAdDialog.dismiss();

							}
						}, new Response.ErrorListener()
						{
							@Override
							public void onErrorResponse(VolleyError error)
							{
								if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

								if (m_progressRewardAdDialog != null)
									m_progressRewardAdDialog.dismiss();

								if (error != null && error.networkResponse != null && error.networkResponse.statusCode == 403)
								{
									ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_403).show();
								}
								else
								{
									ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
								}
							}
						});*/
					}

					@Override
					public void onRewardedFail(int errorCode)
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

						//Log.d("CBK","BackgroundPageFragment - onRewardedFail()");
						if (getContext() != null)
						{
							AnalyticsManager.getInstance().eventStatsRewardAdNotAvailable(getContext(), errorCode);
						}

						String errorMessage = "";

						switch (errorCode)
						{
							case AdRewardVideoAdMob.ERROR_LOAD_FAIL:
							case AdRewardVideoInmobi.ERROR_LOAD_FAIL:
							case AdRewardVideoInmobi.ERROR_NOT_READY:
								errorMessage += getResources().getString(R.string.rewardad_error_lack_inventory);
								break;

							default:
								errorMessage += getResources().getString(R.string.rewardad_error_network);

						}

						ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, errorMessage).show();
						//ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, errorMessage + " Code = " + errorCode).show();

						if (m_progressRewardAdDialog != null)
							m_progressRewardAdDialog.dismiss();

						if (ActivityUtils.isDestroyed(getActivity()) == false)
						{
							AdRewardVideoAdMob.getInstance().loadRewardAd(getActivity());
						}
					}

					@Override
					public void onRewardedOpen()
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
						//Log.d("CBK","BackgroundPageFragment - onRewardedOpen()");
						setRewardAdShowCheck(false, null);
						if (m_progressRewardAdDialog != null)
							m_progressRewardAdDialog.dismiss();
					}

					@Override
					public void onRewardedLoaded()
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
						//Log.d("CBK","BackgroundPageFragment - onRewardedLoaded()");
						if (m_progressRewardAdDialog != null)
							m_progressRewardAdDialog.dismiss();
					}

					@Override
					public void onRewardedStarted()
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
						//Log.d("CBK","BackgroundPageFragment - onRewardedStarted()");
						if (m_progressRewardAdDialog != null)
							m_progressRewardAdDialog.dismiss();
					}
				};

				if (getUserVisibleHint() == true)
					AdRewardVideoAdMob.getInstance().setRewardAdListener(m_rewardAdListener);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private void constructRewardAd(int position)
	{
		try
		{
			if (m_background.isLiveScreen())
			{
				if (m_progressRewardAdDialog != null)
					m_progressRewardAdDialog.dismiss();

				// 1. 리스트에서 선택 시, 화면에 보이는 배경화면이 동영상 배경화면인 경우
				// 2. 스와이프로 선택 시, 화면에 보이는 배경화면이 동영상 배경화면인 경우
				if ((position == -1 && getUserVisibleHint() == true) || (position != -1 && m_position == position))
				{

					// 한번 동영상 광고를 본 컨텐츠는 제외
					//if(m_adRewardAd.isRewardAd())
					//	return;

					//if(position != -1 && m_position == position )
					//	showRewardAdToolTip();

					if (ActivityUtils.isDestroyed(getActivity()) == false)
					{
						if (ContextManager.getInstance().isChinaCountry() == true)
						{
							AdRewardVideoInmobi.getInstance().loadRewardAd(getActivity());
						} else
						{
							AdRewardVideoAdMob.getInstance().loadRewardAd(getActivity());
						}
					}
				}
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private void setRewardAdShowCheck(boolean check, String url)
	{
		try
		{
			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

			if (m_wallpaperButton == null)
				return;

			if (m_background.isLiveScreen() == true)
			{
				setLiveScreenRewardAdShowCheck(check, url);
			} else if (m_background.isExtensionLiveWatchType() == true)
			{
				setLiveWatchRewardAdShowCheck(check, url);
			} else
			{
				m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_white);
			}
		}
		catch (Exception ignored)
		{
		}
	}

	private void setLiveScreenRewardAdShowCheck(boolean check, String url)
	{
		// 무료 상품인 경우
		//if (m_background.isFree() == true)
		if (m_background.isFree() == true && m_background.isPublicLiveScreen())
		{
			m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_white);
		}
		// Paid 상품인 경우
		else if (m_background.isPaidContent() == true)
		{
			if (m_purchaseInfo.isPurchaseBackgroundLicense() == true)
			{
				m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_white);
			} else
			{
				m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_pie);
			}
		}
		// Premium 상품인 경우 (리워드 광고 시청 필요)
		else
		{
			// 리워드 광고를 보고 리워드가 지급된 경우 화면 잠금 풀림
			if (check)
			{
				PreferencesManager.getInstance().setRewardAdShowContents(getContext(), m_background.getUuid(), url);

				m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_white);
			}
			// 리워드 광고를 보지 않았으나 해당 컨텐츠를 예전에 봤는지 확인
			else
			{
				if (PreferencesManager.getInstance().isRewardAdShowContents(getContext(), m_background.getUuid()))
				{
					m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_white);
				} else
				{
					// rewardAd_button_color 녹색
					m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_lock);
				}
			}
		}
	}

	private void setLiveWatchRewardAdShowCheck(boolean check, String url)
	{
		// 무료 상품인 경우
		if (m_background.isFree() == true)
		{
			m_wallpaperButton.setImageResource(R.drawable.ic_livewatch_play);
		}
		// 파이 구매 상품인 경우
		else if (m_background.isPaidContent() == true)
		{
			if (m_purchaseInfo.isPurchaseBackgroundLicense() == true)
			{
				m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_white);
			} else
			{
				m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_pie);
			}
		}
		// Premium 상품인 경우 (리워드 광고 시청 필요)
		else
		{
			// 리워드 광고를 보고 리워드가 지급된 경우 화면 잠금 풀림
			if (check)
			{
				PreferencesManager.getInstance().setRewardAdShowContents(getContext(), m_background.getUuid(), url);
				m_wallpaperButton.setImageResource(R.drawable.ic_livewatch_play);
			}
			// 리워드 광고를 보지 않았으나 해당 컨텐츠를 예전에 봤는지 확인
			else
			{
				if (PreferencesManager.getInstance().isRewardAdShowContents(getContext(), m_background.getUuid()))
				{
					m_wallpaperButton.setImageResource(R.drawable.ic_livewatch_play);
				} else
				{ // rewardAd_button_color 녹색
					m_wallpaperButton.setImageResource(R.drawable.ic_fab_wallpaper_lock);
				}
			}
		}
	}

	private void removeRewardAdToolTip()
	{
		if (m_background != null && (m_background.isLiveScreen() == true || m_background.isExtensionLiveWatchType() == true))
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

						if (mTooltip != null)
						{
							Tooltip.removeAll(getActivity());
						}
						mTooltip = null;
					}
					catch (Exception ignored)
					{
					}
				}
			});
		}
	}

	private void showRewardAdToolTip()
	{
		new Handler().post(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

					if (mTooltip != null)
					{
						Tooltip.removeAll(getActivity());
					}
					mTooltip = null;

					DisplayMetrics metrics = getResources().getDisplayMetrics();

					if (mTooltip == null)
					{
						int stypeRes = R.style.ToolTipStyle;
						String msg = "";

						if (m_background.isExtensionLiveWatchType() == true)
							msg += getResources().getString(R.string.rewardad_now_show_dialog_lvewatch);
						else
							msg += getResources().getString(R.string.rewardad_now_show_dialog);

						mTooltip = new Tooltip.Builder()
								.anchor(m_wallpaperButton, Tooltip.Gravity.LEFT)
								.closePolicy(new Tooltip.ClosePolicy()
										.insidePolicy(false, false)
										.outsidePolicy(false, false), 60000)
								.activateDelay(2000)
								.fitToScreen(true)
								.text(msg)
								.maxWidth(metrics.widthPixels / 2)
								//.withArrow(true)
								.withOverlay(true)
								.withStyleId(stypeRes)
								.floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
								.build();
					}

					Tooltip.make(getActivity(), mTooltip).show();
				}
				catch (Exception ignored)
				{
				}
			}
		});
	}

	private void showRewardAdProgress()
	{
		try
		{
			if (m_progressRewardAdDialog != null && m_progressRewardAdDialog.isShowing())
			{
				m_progressRewardAdDialog.dismiss();
			}

			if (m_progressRewardAdDialog != null)
			{
				m_progressRewardAdDialog.dismiss();
				m_progressRewardAdDialog = null;
			}

			m_progressRewardAdDialog = new MaterialDialog.Builder(getActivity())
					.content(R.string.processing)
					.progress(true, 0)
					.cancelable(true)
					.cancelListener(new DialogInterface.OnCancelListener()
					{
						@Override
						public void onCancel(DialogInterface dialog)
						{
							AnalyticsManager.getInstance().eventStatsRewardAdAvailable(getContext(), "AD_CANCEL");
							if (ContextManager.getInstance().isChinaCountry() == true)
							{
								AdRewardVideoInmobi.getInstance().onCancel(getContext());
							} else
							{
								AdRewardVideoAdMob.getInstance().onCancel(getContext());
							}
						}
					})
					.show();
		}
		catch (Exception ignored)
		{
		}
	}

	private void playPreview(boolean click)
	{
		if (checkCollectionGuideCondition() == true && m_CollectionGuide != null)
			return;

		if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 107) == true)
			return;
		// [WatchFace Live WallPaper]
/*
		if (m_background.isExtensionLiveWatchType() == true)
		{
			if (m_background.getExtension() != null &&
					m_background.getExtension().getPreview() != null &&
					m_background.getExtension().getPreview().getisWeather() == true)
			{
				if (requestWeatherPermission(107) == true)
					return;
			}
		} else
		{
			if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 107) == true)
				return;
		}
*/
		m_handler.onClick(m_previewView, m_floatingLike, m_likeButton, m_likesCountView, m_background, 0);
	}

	private void showInterstitialAd()
	{
		PreferencesManager preferencesManager = PreferencesManager.getInstance();

		final AbsMainActivity activity = (AbsMainActivity) getActivity();
		int interstitialAdChance = preferencesManager.getInterstitialAdChance(activity) + 1;

		if (AdCenter.checkHitConditionInterstitialAd(activity) == true)
		{
			preferencesManager.setInterstitialAdChance(activity, 0);
			activity.showInterstitialAd(BackgroundPageFragment.this, new Runnable()
			{
				@Override
				public void run()
				{
					//nothing
					activity.prepareInterstitialAd();
				}
			});
		} else if (AdCenter.checkHitConditionInterstitiaAdFreeDialog(activity) == true)
		{
			if (UserManager.getInstance().isGuest() == false)
			{
				activity.showInterstitialAd(BackgroundPageFragment.this, new Runnable()
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
		} else
		{
			preferencesManager.setInterstitialAdChance(activity, interstitialAdChance);
		}
	}

	private void onViewCreatedHelper(View view, Bundle savedInstanceState)
	{
		m_pageProgressView.setVisibility(View.GONE);

		m_scrollView.setVisibility(View.VISIBLE);
		m_scrollView.setPadding(0, m_userContainer.getHeight(), 0, 0);

		registerOnScrollListener(new NestedScrollView.OnScrollChangeListener()
		{
			@Override
			public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
			{
				m_previewLayout.setTranslationY(scrollY / 2f);
				m_floatingLike.setTranslationY(scrollY / 2f);
				m_btnVideo.setTranslationY(scrollY / 2f);
			}
		});

		constructContent(view);

		if (m_data.getIndex() == m_position && m_background.isLiveScreen() != true)
		{
			startTransitionAnimation();
		}

		setLikerVisibleListener();
		setCommentVisibleListener();
	}

	private void constructContent(View view)
	{
		m_background = m_data.getBackgroundsList().get(m_position);

		if (m_background != null)
		{
			loadPurchaseLicense(false);
		}

		constructPreview(view);
		constructAction(view);
		constructBasic(view);
		constructNativeAd(view);
		constructUser(view);
		constructTags(view, false);
		constructComments(view);
		constructRewardAd(-1);
	}

	private Link getADLink()
	{
		final List<Link> linkList = m_background.getLinksList();

		Link link = null;

		if (linkList != null && linkList.size() > 0)
		{
			for (int i = 0; i < linkList.size(); i++)
			{
				if (ExpressionManager.getInstance().evaluateCondition(linkList.get(i).getCondition()) == true)
				{
					link = linkList.get(0);
				}
			}
		}

		return link;
	}

	private void setBottomBuyBar()
	{
		Link link = getADLink();

		if (link == null) return;

		final Link finalLink = link;

		final View buyView = getView().findViewById(R.id.buy_container);

		final TextView title = buyView.findViewById(R.id.title);
		final TextView subtitle = buyView.findViewById(R.id.subtitle);

		title.setText(link.getTitle());
		subtitle.setText(link.getSubtitle());

		buyView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onClickBuyButton(finalLink);
			}
		});

		if (buyView.getVisibility() == View.GONE)
		{
			buyView.setVisibility(View.VISIBLE);
			buyView.setAlpha(0);
			buyView.setTranslationY(DisplayManager.getInstance().getPixelFromDp(getContext(), 120));

			final NestedScrollView.OnScrollChangeListener listener = new NestedScrollView.OnScrollChangeListener()
			{
				@Override
				public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY)
				{
					if (isShowing() == true || m_keyboardChecker.isVisible() == true) return;

					int diff = scrollY - oldScrollY;

					if (diff > 0)
					{
						buyView.animate()
								.alpha(0)
								.translationY(DisplayManager.getInstance().getPixelFromDp(getContext(), 120))
								//.setInterpolator(new AccelerateDecelerateInterpolator())
								.setDuration(500)
								.start();
					} else
					{
						buyView.animate()
								.alpha(1)
								.translationY(0)
								//.setInterpolator(new AccelerateDecelerateInterpolator())
								.setDuration(500)
								.start();
					}
				}

				private boolean isShowing()
				{
					final int hiddenTranslationY = DisplayManager.getInstance().getPixelFromDp(getContext(), 120);
					final int buyViewTranslationY = (int) buyView.getTranslationY();

					return buyViewTranslationY > 0 && buyViewTranslationY < hiddenTranslationY;
				}
			};

			new SizeDeterminer(m_previewLayout).getSize(new SizeReadyCallback()
			{
				@Override
				public void onSizeReady(View view, int width, int height)
				{
					listener.onScrollChange(m_scrollView, 0, 0, 0, 0);
				}
			});

			//registerOnScrollListener(listener);
		}
	}

	private void onClickBuyButton(Link link)
	{
		AsyncStats.statsLink(m_background, link);
		if (link.getTypeId() == Link.TYPE_ID_COMIC)
		{
			AnalyticsManager.getInstance().eventStatsCartoonClick(getContext(), m_background.getUuid());
			CartoonActivity.createInstance(getContext(), link, m_background.getUuid());
		} else
		{
			String title = link.getTitle();
			if (title == null)
				title = "";

			new WebDialogFragmentEx.Builder(link.getUri())
					.setTheme(R.style.BG_Theme_Activity)
					.setLayout(R.layout.fragment_web_dialog)
					.setToolbarNavigationIcon(R.drawable.ic_back)
					.setTitle(title)
					.addExtraHeader("referer", m_background.getShareUrl())
					.setJavascriptEnabled(true)
					.start(getChildFragmentManager());
		}
	}

	private void constructPreview(View view)
	{
		Image preview = null;

		preview = m_background.getPreview();

		if (preview == null) return;

		final boolean isTablet = DeviceUtils.isTablet(getContext());

		//하단 Banner 광고가 보이는지 체크 하는 로직
		/*ViewGroup adBanner = getActivity().findViewById(R.id.ad_view_container);
		if (adBanner.getHeight() > 0)
		{
			m_previewView.setRatio(isTablet ? 1.0f : 0.9f);
		} else
		{
			m_previewView.setRatio(isTablet ? 0.9f : 0.8f);
		}*/

		m_previewView.setRatio(0.9f);
		m_previewView.setScaleType(ImageView.ScaleType.CENTER_CROP);

		// GIF
		if (m_background.getImage() != null &&
				m_background.getImage().getUrl() != null && m_background.getImage().getUrl().isEmpty() == false &&
				m_background.getImage().getUrl().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION) == true)
		{
			// Glide
			final RequestListener<GifDrawable> listenerGif = new RequestListener<GifDrawable>()
			{
				@Override
				public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource)
				{
					return false;
				}

				@Override
				public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource)
				{
					m_previewProgressView.setVisibility(View.GONE);
					return false;
				}
			};

			GlideUtils.setImageAsGif(this, preview)
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.listener(listenerGif)
					.into(m_previewView);

			// droidsonroids.gif:android-gif-drawable
			/*
			try
			{
				Uri uri = null;
				//if (preview.getUrl().toString().contains("file://") == false)
				////{
				//	uri = Uri.parse("file://" + preview.getUrl().toString());
				//}
				//else
				{
					uri = Uri.parse(preview.getUrl().toString());
				}
				GifDrawable drawable = new GifDrawable(this.getContext().getContentResolver(), uri);
				drawable.setLoopCount(0);
				m_previewGifView.setTag(drawable);
				m_previewGifView.setImageDrawable(drawable);
				m_previewGifView.setScaleType(ImageView.ScaleType.CENTER_CROP);

				if(m_previewView != null)
					m_previewView.setVisibility(View.GONE);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			*/
		}
		else
		{
			//if(m_previewGifView != null)
			//	m_previewGifView.setVisibility(View.GONE);

			final RequestListener<Bitmap> listener = new RequestListener<Bitmap>()
			{
				@Override
				public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
				{
					return false;
				}

				@Override
				public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
				{
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
					{
						if(m_background.isLiveScreen() == false)
							m_previewProgressView.setVisibility(View.GONE);
						else
						{
							//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onResourceReady() File : " + m_background.getLiveScreen().getPreview());
							//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onResourceReady() - getHeight1 : " + m_previewVideoView.getHeight() + " / getWidth1 : " + m_previewVideoView.getWidth());
							//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onResourceReady() - getHeight2 : " + m_previewView.getHeight() + " / getWidth2 : " + m_previewView.getWidth() + " / " +
							//		"isFirstResource : " + isFirstResource);

							if(isFirstResource)
								videoLayoutSetting(m_previewView.getWidth(), m_previewView.getHeight());
						}
					}
					else
					{
						m_previewProgressView.setVisibility(View.GONE);
					}

					return false;
				}
			};

			preview = m_background.getImage();

			GlideUtils.setImageAsBitmap(this, preview)
					.format(DecodeFormat.PREFER_ARGB_8888)
					.thumbnail(0.3f)
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.listener(listener)
					.into(m_previewView);
		}
	}

	private void constructAction(final View view)
	{
		boolean selected = LikesManager.getInstance().contains(m_background.getUuid());
		m_likeButton.init(getActivity());
		m_likeButton.setChecked(selected);
		m_likeButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				try
				{
					AnalyticsManager.getInstance().DetailEvent(getContext(), "Like_Detail");
				}
				catch (Exception ignored)
				{
				}

				AnalyticsManager.getInstance().eventUserActionLike(getActivity(), "PAGE");
				BackgroundPageFragment.this.onClickLike();
			}
		});

		m_downloadButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_C) == true)
				{
					view.setScaleX(0.7f);
					view.setScaleY(0.7f);

					view.animate().setInterpolator(new OvershootInterpolator())
							.scaleX(1)
							.scaleY(1)
							.setDuration(300)
							.start();

					try
					{
						AnalyticsManager.getInstance().DetailEvent(getContext(), "Download_Detail");
					}
					catch (Exception ignored)
					{
					}

					BackgroundPageFragment.this.onClickDownload();
				}
			}
		});

		m_tossButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_B) == true)
				{
					view.setScaleX(0.7f);
					view.setScaleY(0.7f);

					view.animate().setInterpolator(new OvershootInterpolator())
							.scaleX(1)
							.scaleY(1)
							.setDuration(300)
							.start();

					try
					{
						AnalyticsManager.getInstance().DetailEvent(getContext(), "Toss_Detail");
						AnalyticsManager.getInstance().DetailEvent(getContext(), "Share_Detail");
						AnalyticsManager.getInstance().shareEvent(getContext(), "Share_Detail");
					}
					catch (Exception ignored)
					{
					}

					BackgroundPageFragment.this.onClickToss();
				}
			}
		});

		if (m_background.isLiveScreen() == true)
		{
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				m_btnVideo.setVisibility(View.GONE);
			else
				m_btnVideo.setVisibility(View.VISIBLE);

			m_downloadButton.setVisibility(View.GONE);
		} else if (m_background.isExtensionLiveWatchType() == true)
		{
			m_btnVideo.setVisibility(View.GONE);
			m_downloadButton.setVisibility(View.GONE);
		} else
		{
			m_btnVideo.setVisibility(View.GONE);
			m_downloadButton.setVisibility(View.VISIBLE);
		}

		setRewardAdShowCheck(false, null);

		if (m_background.isLiveScreen() == true || m_background.isExtensionLiveWatchType() == true)
		{
			CreateRewardAdListener();

			m_subscription = RxBus.getInstance().registerListenerMainThread(BusActivityEvent.class, new Action1<BusActivityEvent>()
			{
				@Override
				public void call(BusActivityEvent event)
				{
					if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

					if (event != null)
					{
						if (event.getRequestCode() == BusActivityEvent.REQUEST_SET_VIDEO_WALLPAPER ||
								event.getRequestCode() == BusActivityEvent.REQUEST_SET_LIVEWATCH_WALLPAPER)
						{
							if (getUserVisibleHint() == true)
							{
								removeRewardAdToolTip();
								PreferencesManager.getInstance().setRewardAdRemoveContents(getContext(), m_background.getUuid());
								setRewardAdShowCheck(false, null);
							}
						}
					}
				}
			});
		}
	}

	private void constructBasic(final View view)
	{
		StaticUtils.setText(view, R.id.title, m_background.getTitle());
		StaticUtils.setText(view, R.id.description, m_background.getDescription(), true);

		constructLicense(view);

		StaticUtils.setText(view, R.id.downloads_count, m_background.getPrettyDownloadsCount());
		view.findViewById(R.id.downloads_count).setVisibility(m_background.getDownloadCount() == 0 ? View.GONE : View.VISIBLE);

		StaticUtils.setText(view, R.id.views_count, m_background.getPrettyViewsCount());

		int likesCount = m_background.getLikesCount();
		likesCount = likesCount < 0 ? 0 : likesCount;
		StaticUtils.setText(view, R.id.likes_count, m_background.getPrettyLikesCountPlus(), true);
		view.findViewById(R.id.likes_count).setVisibility(likesCount == 0 ? View.GONE : View.VISIBLE);

		int commentsCount = m_background.getCommentsCount();
		commentsCount = commentsCount < 0 ? 0 : commentsCount;
		StaticUtils.setText(view, R.id.comments_count, m_background.getPrettyCommentsCountPlus(), true);
		view.findViewById(R.id.comments_count).setVisibility(commentsCount == 0 ? View.GONE : View.VISIBLE);

		final boolean selected = FLManagerCompatUtils.contain(m_background);
		m_likeButton.setChecked(selected);
	}

	private void constructNativeAd(final View view)
	{
		final View nativeAdContainer = view.findViewById(R.id.native_ad_container);

		//AdCheckManager.getInstance().checkNativeAds(new AdCheckManager.AdAvailabilityCallback()
		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				Fragment fragment = BackgroundPageFragment.this;

				if (FragmentUtils.isDestroyed(fragment) == true) return;

				if (fragment.getParentFragment() != null)
				{
					fragment = fragment.getParentFragment();
				}

				final ArrayList<IntegrateNativeAd> nativeAdsList = AdCheckManager.getInstance().getShuffledNativeAdList(fragment);

				if (nativeAdsList == null || nativeAdsList.size() == 0)
				{
					onNotAvailable();
					return;
				}

				int index = m_position % nativeAdsList.size();
				m_nativeAd = nativeAdsList.get(index);

				TextView adfree = nativeAdContainer.findViewById(R.id.adfree);
				View adView = nativeAdContainer.findViewById(R.id.native_ad_container);
				TextView body = nativeAdContainer.findViewById(R.id.native_ad_body);
				TextView title = nativeAdContainer.findViewById(R.id.native_ad_title);
				TextView button = nativeAdContainer.findViewById(R.id.native_ad_call_to_action);
				AdIconView icon = nativeAdContainer.findViewById(R.id.native_ad_icon);
				MediaView media = nativeAdContainer.findViewById(R.id.native_ad_media);
				ViewGroup adChoice = nativeAdContainer.findViewById(R.id.ad_choice);

				body.setText(m_nativeAd.getAdBody());
				title.setText(m_nativeAd.getAdTitle());
				button.setText(m_nativeAd.getAdCallToAction());

				AdChoicesView adChoicesView = new AdChoicesView(getContext(), m_nativeAd.getNativeAd(), true);
				adChoice.addView(adChoicesView);

				final String adBodyText = m_nativeAd.getAdBody();

				if (TextUtils.isEmpty(adBodyText != null ? adBodyText.trim() : ""))
				{
					body.setVisibility(View.GONE);
					title.setMaxLines(2);
				}

				List<View> clickableViews = new ArrayList<>();
				clickableViews.add(body);
				clickableViews.add(title);
				clickableViews.add(button);
				clickableViews.add(icon);
				m_nativeAd.registerViewForInteraction(adView, media, icon, clickableViews);

				if (adfree != null)
				{
					String content = new StringBuffer()
							.append(getString(R.string.adfree_title)).append(" ")
							.append(getString(R.string.pieinfo_tabs_charge)).toString();
					String word = getString(R.string.pieinfo_tabs_charge);

					adfree.setText(content);

					int start = content.lastIndexOf(word);
					int end = start + word.length();

					try
					{
						Spannable span = (Spannable) adfree.getText();
						span.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
						span.setSpan(new ForegroundColorSpan(0xFF9B9B9B), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

						//adfree.setMovementMethod(LinkMovementMethod.getInstance());
					}
					catch (Exception e)
					{
						e.toString();
					}
				}

				nativeAdContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNotAvailable()
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				nativeAdContainer.setVisibility(View.GONE);
			}
		});
	}

	private void constructLicense(View view)
	{
		final License license = m_background.getLicense();
		if (license == null) return;

		final ViewGroup licenseView = view.findViewById(R.id.license);
		licenseView.setVisibility(View.VISIBLE);

		final TextView licenseTextView = licenseView.findViewById(R.id.lc_text);

		final String licenseType = license.getType();

		// Copyright
		if (licenseType.startsWith("copy") == true)
		{
			licenseView.findViewById(R.id.lc_copyright).setVisibility(View.VISIBLE);
			licenseTextView.setText(R.string.license_copyright);
		}

		// Public Domain
		else if (licenseType.equals("public") == true)
		{
			licenseView.findViewById(R.id.lc_public).setVisibility(View.VISIBLE);
			licenseTextView.setText(R.string.license_public);
		}

		// Creative Commons
		else if (licenseType.startsWith("cc") == true)
		{
			licenseView.findViewById(R.id.lc_by).setVisibility(View.VISIBLE);

			final String[] tokens = licenseType.split("-");

			for (String token : tokens)
			{
				if (token.equals("nc") == true) licenseView.findViewById(R.id.lc_nc).setVisibility(View.VISIBLE);
				if (token.equals("nd") == true) licenseView.findViewById(R.id.lc_nd).setVisibility(View.VISIBLE);
				if (token.equals("sa") == true) licenseView.findViewById(R.id.lc_sa).setVisibility(View.VISIBLE);
			}

			licenseTextView.setText(R.string.license_cc);
		}
	}

	private void constructUser(View view)
	{
		final User user = m_background.getUser();
		if (user == null) return;

		final ViewGroup userView = view.findViewById(R.id.user);
		userView.setVisibility(View.VISIBLE);

		final ImageView userAvatarView = userView.findViewById(R.id.user_avatar);
		final TextView userNameView = userView.findViewById(R.id.user_username);

		GlideUtils.setImageByCenterCrop(this, user.getAvatar()).into(userAvatarView);

		StaticUtils.setText(userView, R.id.user_name, user.getName());
		StaticUtils.setText(userView, R.id.user_username, "@" + user.getUsername(), true);

		if (TextUtils.isEmpty(user.getUsername()) == true)
		{
			userNameView.setVisibility(View.GONE);
		}

		if (FollowManager.getInstance().isFollowing(user.getUsername()) == false &&
				UserManager.getInstance().isMe(user) == false)
		{
			m_follow.setText(R.string.userinfo_follow);
			m_follow.setVisibility(View.VISIBLE);
		} else
		{
			m_follow.setVisibility(View.GONE);
		}
	}

	private void constructTags(View view, boolean enableViewAll)
	{
		removeRewardAdToolTip();

		final List<Tag> tagList = m_background.getTagsList();

		if (tagList == null || tagList.size() == 0) return;

		Point displaySize = DisplayManager.getInstance().getDisplaySize(getContext());

		int layoutMargin = DisplayManager.getInstance().getPixelFromDp(getContext(), 12 * 2);
		int tagMargin = DisplayManager.getInstance().getPixelFromDp(getContext(), 4 * 2);
		int lineWidth = displaySize.x - layoutMargin;

		final FlowLayout tagsView = view.findViewById(R.id.tags);
		tagsView.removeAllViews();

		int moreWidth = DisplayManager.getInstance().getPixelFromDp(getContext(), 36 + 8);

		int currentWidth = 0;

		for (Tag tag : tagList)
		{
			final TextView tagView = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_tag, tagsView, false);

			tagView.setText(tag.getPrettyTag());
			tagView.setTag(tag);
			tagView.measure(0, 0);
			currentWidth += (tagView.getMeasuredWidth() + tagMargin);

			if (enableViewAll == false && (tagsView.getChildCount() + 1) != tagList.size() && currentWidth + moreWidth > lineWidth)
			{
				View moreView = LayoutInflater.from(getActivity()).inflate(R.layout.item_tag_more, tagsView, false);
				moreView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						try
						{
							AnalyticsManager.getInstance().DetailEvent(getContext(), "TagMore_Detail");
						}
						catch (Exception ignored)
						{
						}

						constructTags(getView(), true);
					}
				});

				tagsView.addView(moreView);
				break;
			}

			tagsView.addView(tagView);
			ListenerUtils.setOnClickListener(tagView, this, "onClickTag");
		}

		if (tagsView.getChildCount() > 0)
		{
			tagsView.setVisibility(View.VISIBLE);
		}
	}

	private void constructSimilarBackgrounds(List<Background> similarBackgrounds)
	{
		int rowCount = DeviceUtils.isTablet(getContext()) == true ? 1 : 2;
		int columnCount = DeviceUtils.isTablet(getContext()) == true ? 4 : 2;

		m_similarContainer.setRowCount(rowCount);
		m_similarContainer.setColumnCount(columnCount);

		m_similarContainer.removeAllViews();
		m_similarProgress.setVisibility(View.GONE);

		int maxSampleCount = Math.min(MAX_SIMILAR_BACKGROUNDS_SIZE, similarBackgrounds.size());

		int sidePadding = DisplayManager.getInstance().getPixelFromDp(getContext(), 16 * 2);
		int displayWidth = DisplayManager.getInstance().getDisplaySize(getContext()).x;
		int thumbnailSpace = DisplayManager.getInstance().getPixelFromDp(getContext(), 4);

		int size = (displayWidth - sidePadding - (MAX_SIMILAR_BACKGROUNDS_SIZE - 1) * thumbnailSpace) / columnCount;

		for (int i = 0; i < maxSampleCount; i++)
		{
			final ViewGroup similarItem = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_similar, m_similarContainer, false);

			final ImageView image = similarItem.findViewById(R.id.image);
			final Background background = similarBackgrounds.get(i);

			final int index = i;
			similarItem.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onClickSimilarBackground(m_background, index);
				}
			});

			GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED));
			lp.height = lp.width = size;

			m_similarContainer.addView(similarItem, lp);

			if (getUserVisibleHint() == true)
			{
				similarItem.setAlpha(0);
				similarItem.animate()
						.alpha(1)
						.setDuration(300)
						.setStartDelay(200)
						.start();
			}

			GlideApp.with(this)
					.load(background.getThumbnail().getUrl())
					.centerCrop()
					.transition(GenericTransitionOptions.with(R.anim.short_fade_in))
					.placeholder(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.grey_300)))
					.transition(withCrossFade(500))
					.into(image);
		}
	}

	private void constructLikeres(List<Liker> likeres)
	{
		m_likerContainer.removeAllViews();

		int count = calcLikerCount();
		int maxSampleCount = Math.min(count, likeres.size());

		for (int i = 0; i < maxSampleCount; i++)
		{
			final ViewGroup likerItem = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_page_liker, m_likerContainer, false);

			final ImageView image = likerItem.findViewById(R.id.profile_image);
			final ImageView more = likerItem.findViewById(R.id.more);

			final Liker liker = likeres.get(i);

			boolean isLastItem = i == maxSampleCount - 1 && maxSampleCount < likeres.size();

			if (isLastItem == true)
			{
				more.setVisibility(View.VISIBLE);
				likerItem.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
						{
							try
							{
								AnalyticsManager.getInstance().DetailEvent(getContext(), "LikedUserMore_Detail");
							}
							catch (Exception ignored)
							{
							}

							removeRewardAdToolTip();

							AnalyticsManager.getInstance().eventUserActionLikersIconMore(getContext(), m_background.getUuid());
							AbsMainActivity.getTabStackHelper(BackgroundPageFragment.this).showFragment(UserLikerFragment.newInstance(m_background));
						}
					}
				});
			} else
			{
				likerItem.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						try
						{
							AnalyticsManager.getInstance().DetailEvent(getContext(), "LikedUser_Detail");
						}
						catch (Exception ignored)
						{
						}

						String username = liker.getUsername();

						if (UserManager.getInstance().isGuest() == false)
						{
							AnalyticsManager.getInstance().eventUserActionLikersIcon(getContext(), m_background.getUuid());
						}

						AnalyticsManager.getInstance().eventUserActionProfile(getContext(), "PAGE_LIKERES");
						AnalyticsManager.getInstance().eventUserTrackingProfile(getContext(), username);

						if (UserManager.getInstance().isMe(username) == false)
						{
							AnalyticsManager.getInstance().eventUserActionOtherProfile(getContext(), "PAGE_LIKERES");
						}
						onClickProfile(liker.getUsername());
					}
				});
			}

			m_likerContainer.addView(likerItem);
			if (getUserVisibleHint() == true)
			{
				likerItem.setAlpha(0);
				likerItem.animate()
						.alpha(1)
						.setDuration(300)
						.setStartDelay(200)
						.start();
			}

			Glide.with(this).load(liker.getAvataurl()).into(image);
		}
	}

	private int calcLikerCount()
	{
		int profileWidth = DisplayManager.getInstance().getPixelFromDp(getContext(), 40 + 4 * 2);    // image : 40 / margin : 4 * 2
		int dividerWidth = DisplayManager.getInstance().getPixelFromDp(getContext(), 4);            // divider : 4
		int sidePadding = DisplayManager.getInstance().getPixelFromDp(getContext(), 16 * 2);
		int displayWidth = DisplayManager.getInstance().getDisplaySize(getContext()).x;

		int width = displayWidth - sidePadding;
		int count = Math.abs(width / (profileWidth + dividerWidth));

		return count;
	}

	private void constructComments(View view)
	{
		ViewCompat.setNestedScrollingEnabled(m_commentsListView, false);

		final LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
		DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider);
		horizontalDecoration.setDrawable(horizontalDivider);

		m_commentsListView.setLayoutManager(layout);
		m_commentsListView.setAdapter(m_commentsAdapter);
		m_commentsListView.addItemDecoration(horizontalDecoration);

		if (UserManager.getInstance().isGuest() == false)
		{
			makeCommentsInputLayout();
		} else
		{
			makeCommentsInputLayoutForGuest();
		}
	}

	private void setVisibilityCommentsViews()
	{
		TextView allComments = m_commentsContainer.findViewById(R.id.view_all_comments);
		TextView noComments = m_commentsContainer.findViewById(R.id.no_comments);

		final int commentsCount = m_comments.getTotalCount();

		TextViewUtils.setTextWithArgsRaw(allComments, R.string.comment_view_all_comments, commentsCount);
		allComments.setVisibility(commentsCount > COMMENTS_SHOW_ALL_COUNT ? View.VISIBLE : View.GONE);

		if (commentsCount == 0)
		{
			noComments.setVisibility(View.VISIBLE);
			m_commentsContainer.setVisibility(View.VISIBLE);
			m_commentsProgress.setVisibility(View.GONE);
		} else
		{
			noComments.setVisibility(View.GONE);
		}

		if (m_contentLayout.getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS)
		{
			m_contentLayout.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}
	}

	private void makeCommentsInputLayoutForGuest()
	{
		final View guestClickable = m_commentInputView.findViewById(R.id.guest_clickable);
		guestClickable.setVisibility(View.VISIBLE);

		guestClickable.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				removeRewardAdToolTip();

				AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "COMMENT");
				getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_COMMENT));
			}
		});
	}

	private void makeCommentsInputLayout()
	{
		final TextView send = m_commentInputView.findViewById(R.id.send);
		final EditText inputComment = m_commentInputView.findViewById(R.id.input_comment);
		final View inputProgress = m_commentInputView.findViewById(R.id.comment_enter_progress);

		if (TextUtils.isEmpty(inputComment.getText()) == true)
		{
			send.setTextColor(getResources().getColor(R.color.date_text_color));
		}

		inputComment.addTextChangedListener(new SimpleTextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				final String str = s.toString().trim();

				if (before == 0 && TextUtils.isEmpty(str) == false)
				{
					send.setTextColor(getResources().getColor(R.color.color_accent));
				}

				if (TextUtils.isEmpty(str) == true)
				{
					send.setTextColor(getResources().getColor(R.color.date_text_color));
				}
			}
		});

		send.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				removeRewardAdToolTip();

				String comment = inputComment.getText().toString().trim();

				if (m_isCommentPosting == true)
				{
					ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.processing).show();
					return;
				}
				if (TextUtils.isEmpty(comment) == true)
				{
					return;
				}

				if (comment.length() > 500)
				{
					ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.comment_too_long).show();
					return;
				}

				m_isCommentPosting = true;

				inputComment.setEnabled(false);
				inputProgress.setVisibility(View.VISIBLE);
				send.setVisibility(View.INVISIBLE);

				String commentPostUrl = UrlFactory.commentPost();

				HashMap params = ParamFactory.commentPost(m_background.getUuid(), comment);

				Requests.authRequestByPost(commentPostUrl, params, Object.class, new Response.Listener<Object>()
				{
					@Override
					public void onResponse(Object response)
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

						m_isCommentPosting = false;

						inputComment.setEnabled(true);
						inputComment.setText("");

						send.setVisibility(View.VISIBLE);
						inputProgress.setVisibility(View.GONE);

						loadComments();
					}
				}, new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

						m_isCommentPosting = false;

						inputComment.setEnabled(true);

						send.setVisibility(View.VISIBLE);
						inputProgress.setVisibility(View.GONE);

						ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
					}
				});

				AnalyticsManager.getInstance().eventUserActionComment(getActivity(), "PAGE");
			}
		});

	}

	@SuppressWarnings("ConstantConditions")
	private void startTransitionAnimation()
	{
		try
		{
			new SizeDeterminer(m_previewLayout).getSize(new SizeReadyCallback()
			{
				@Override
				public void onSizeReady(View view, int width, int height)
				{
					if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

					final Animator.AnimatorListener postAnimatorListener = new AnimatorListenerAdapter()
					{
						@Override
						public void onAnimationStart(Animator animation)
						{
							m_wallpaperButton.setScaleX(0.1f);
							m_wallpaperButton.setScaleY(0.1f);
							m_wallpaperButton.setAlpha(0f);
						}

						@Override
						public void onAnimationEnd(Animator animation)
						{
							if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

							m_wallpaperButton.animate()
									.setInterpolator(OVERSHOOT_INTERPOLATOR)
									.setDuration(500)
									.setStartDelay(100)
									.scaleX(1.0f)
									.scaleY(1.0f)
									.alpha(1.0f)
									.start();

							initCollectionGuide();
						}
					};

					final ImageView heroView = getView().findViewById(R.id.hero);
					ViewTransitionHelper.getInstance().startTransition(heroView, m_previewLayout, m_scrollView, postAnimatorListener);
				}
			});
		}
		catch (Exception ignored)
		{
		}
	}

	private void onLoadNext(Response.Listener<Backgrounds> response, Response.ErrorListener errorResponse)
	{
		final Callback callback = CallbackUtils.getCallback(this, Callback.class);
		if (callback != null) callback.onLoadNext(response, errorResponse);
	}

	private boolean isSelectedFragment()
	{
		return m_data.getIndex() == m_data.getBackgroundsList().indexOf(m_background);
	}

	private void showErrorDialog(final Exception e)
	{
		ErrorDialogFragment.start(getChildFragmentManager(), e, new ErrorDialogFragment.DialogCallback()
		{
			@Override
			public void onShow(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				try
				{
					if (e.toString().contains("hostname") == true)
					{
						SlackMessage.sendMessageForHostnameIssue(getActivity(), m_background.getDataUrl()/**/);
					} else if (e instanceof ParseErrorEx)
					{
						SlackMessage.sendMessageForJsonParseIssue(getActivity(), e);
					}
				}
				catch (Exception e)
				{
					// Nothing
				}
			}

			@Override
			public void onRetry(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				onRefresh();
			}
		});
	}

	/*
		private boolean requestWeatherPermission(final int requestCode)
		{
			final String[] permissions = new String[] {
					Manifest.permission.ACCESS_COARSE_LOCATION,
					Manifest.permission.ACCESS_FINE_LOCATION
			};

			boolean isPermission = false;

			for (int i = 0; i < permissions.length; i++)
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getContext(), permissions[i]) != PackageManager.PERMISSION_GRANTED)
				{
					if (PreferencesManager.getInstance().isPermissionWeatherFirstPopup(getContext()) == true ||
							ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[i]) == true)
					{
						PreferencesManager.getInstance().setPermissionWeatherFirstPopup(getContext(), false);

						m_isPremissionGuide = true;
						final MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
						{
							@Override
							public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
							{
								if (isAdded() == false)
									return;

								dialog.dismiss();
								getParentFragment().requestPermissions(permissions, requestCode);
							}
						};

						String msg = getString(R.string.need_write_livewatch_permissio_first);
						new MaterialDialog.Builder(getContext())
								.title(R.string.app_name)
								.content(msg)
								.positiveText(R.string.ok)
								.onPositive(onPositive)
								.show();

						isPermission = true;
						break;
					} else
					{
						if (getParentFragment() == null)
						{
							requestPermissions(permissions, requestCode);
						} else
						{
							getParentFragment().requestPermissions(permissions, requestCode);
						}

						m_isPremissionGuide = false;
						getParentFragment().requestPermissions(permissions, requestCode);

						isPermission = true;
						break;
					}
				}
			}

			return isPermission;
		}
	*/
	private boolean requestPermission(final String permission, final int requestCode)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED)
		{
			if (getParentFragment() == null)
			{
				requestPermissions(new String[] { permission }, requestCode);
				return true;
			}

			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission) == true)
			{
				m_isPremissionGuide = true;
				final MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						if (isAdded() == false)
							return;

						dialog.dismiss();
						getParentFragment().requestPermissions(new String[] { permission }, requestCode);
					}
				};

				MaterialDialog dialog = new MaterialDialog.Builder(getContext())
						.customView(R.layout.fragment_permission_storage, true)
						.positiveText(R.string.ok)
						.onPositive(onPositive)
						.show();

				// OGQ Text
				if (requestCode == 105)
				{
					TextView desc = dialog.getCustomView().findViewById(R.id.description);
					desc.setText(R.string.need_write_storage_permission_ogqtext_description);
				}
			} else
			{
				m_isPremissionGuide = false;
				getParentFragment().requestPermissions(new String[] { permission }, requestCode);

				try
				{
					switch (requestCode)
					{
						case 101:
							AnalyticsManager.getInstance().RequestPermission(getContext(), "SetAsBackground_Detail_Auth");
							break;
						case 102:
							AnalyticsManager.getInstance().RequestPermission(getContext(), "SetAsContact_OverFlow_Detail_Auth");
							break;
						case 103:
							AnalyticsManager.getInstance().RequestPermission(getContext(), "Download_Detail_Auth");
							break;
						case 105:
							AnalyticsManager.getInstance().RequestPermission(getContext(), "OGQText_Detail_Auth");
							break;
						case 106:
							AnalyticsManager.getInstance().RequestPermission(getContext(), "Preview_Detail_Auth");
							break;
					}
				}
				catch (Exception ignored)
				{
				}
			}

			return true;
		}
		return false;
	}

	private void loadPurchaseLicense(final boolean showAnimation)
	{
		final String url = UrlFactory.purchaseImageinfo(m_background.getUuid());

		Requests.requestByGet(url, PurchaseInfo.class, new Response.Listener<PurchaseInfo>()
		{
			@Override
			public void onResponse(PurchaseInfo response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				try
				{
					m_purchaseInfo = response;

					if (m_background.isPaidContent())
					{
						String contentUrl = m_purchaseInfo.getBackgroundLicense().getContentUrl();

						if (m_background.isLiveScreen() == true)
						{
							m_background.getLiveScreen().getComplete().setUrl(contentUrl);
						} else if (m_background.isExtensionLiveWatchType() == true)
						{
							m_background.getExtension().getComplete().setUrl(contentUrl);
						}
					}

					if (showAnimation == true)
					{
						showRewardAdToolTip();
					}

					setRewardAdShowCheck(false, null);
				}
				catch (Exception ignored)
				{
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	private void loadSimilarBackgrounds()
	{
		if (hasSimilarBackgrounds() == true || TextUtils.isEmpty(m_background.getUuid()) == true)
		{
			return;
		}

		final String similarUrl = UrlFactory.similarBackgrounds(m_background.getUuid());

		Requests.requestByGet(similarUrl, Backgrounds.class, new Response.Listener<Backgrounds>()
		{
			@Override
			public void onResponse(Backgrounds response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				m_similars = response;

				if (hasSimilarBackgrounds() == true)
				{
					constructSimilarBackgrounds(m_similars.getBackgroundsList());
				} else
				{
					m_layoutSimilar.setVisibility(View.GONE);
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	private void onClickSimilarBackground(Background background, int index)
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Similar_Detail");
			removeRewardAdToolTip();

			final String similarUrl = UrlFactory.similarBackgrounds(m_background.getUuid());
			final Fragment fragment = SimilarBackgroundFragment.newInstance(similarUrl, index);
			AbsMainActivity.getTabStackHelper(BackgroundPageFragment.this).showFragment(fragment);
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		}
	}

	private boolean hasSimilarBackgrounds()
	{
		return m_similars != null &&
				m_similars.getBackgroundsList() != null &&
				m_similars.getBackgroundsList().size() > 0;
	}

	private void loadLikeres()
	{
		if (m_background == null || TextUtils.isEmpty(m_background.getUuid()) == true)
		{
			return;
		}

		final String likerUrl = UrlFactory.liker(m_background.getUuid());

		Requests.requestByGet(likerUrl, Likeres.class, new Response.Listener<Likeres>()
		{
			@Override
			public void onResponse(Likeres response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				if (hasNewLikeres(response) == true)
				{
					m_likeres = response;
					constructLikeres(m_likeres.getLikerlist());
				}

				if (response == null || response.getLikerlist() == null || response.getLikerlist().size() <= 0)
				{
					m_likeres = null;
				}

				setLikerVisibleListener();
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				ToastUtils.makeInfoToast(BackgroundPageFragment.this.getActivity(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	private boolean hasLikeres()
	{
		return m_likeres != null &&
				m_likeres.getLikerlist() != null &&
				m_likeres.getLikerlist().size() > 0;
	}

	private boolean hasNewLikeres(Likeres likeres)
	{
		if (likeres == null ||
				likeres.getLikerlist() == null ||
				likeres.getLikerlist().size() <= 0)
			return false;

		if (hasLikeres() == false)
			return true;

		return m_likeres.getLikerlist().get(0).getRegDate() != likeres.getLikerlist().get(0).getRegDate();
	}

	private void setLikerVisibleListener()
	{
		if (hasLikeres() == true)
		{
			m_layoutLiker.setVisibility(View.VISIBLE);
			m_likerProgress.setVisibility(View.VISIBLE);
		} else
		{
			m_layoutLiker.setVisibility(View.GONE);
		}

	}

	private boolean hasComments()
	{
		return m_comments != null && m_comments.getCommentsList() != null && m_comments.getCommentsList().size() != 0;
	}

	private void setCommentVisibleListener()
	{
		if (hasComments() == true)
		{
			m_commentsProgress.setVisibility(View.GONE);
			m_commentsContainer.setVisibility(View.VISIBLE);
			setVisibilityCommentsViews();
			return;
		}
	}

	private void loadComments()
	{
		if (m_background == null || TextUtils.isEmpty(m_background.getCommentsUrl()) == true)
		{
			return;
		}

		String commentsUrl = m_background.getCommentsUrl();

		Requests.requestByGet(commentsUrl, Comments.class, new Response.Listener<Comments>()
		{
			@Override
			public void onResponse(Comments response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				m_comments = response;

				m_commentsAdapter.notifyDataSetChanged();

				m_commentsContainer.setVisibility(View.VISIBLE);
				m_commentsProgress.setVisibility(View.GONE);

				setVisibilityCommentsViews();
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();

				m_commentsProgress.setVisibility(View.GONE);
			}
		});
	}

	private void onClickProfile(String username)
	{
		removeRewardAdToolTip();

		String userInfoUrl = UrlFactory.usersInfo(username);

		Fragment fragment = UserInfoFragment.newInstance(userInfoUrl);
		AbsMainActivity.getTabStackHelper(BackgroundPageFragment.this).showFragment(fragment);
	}

	private CommentsAdapter m_commentsAdapter = new CommentsAdapter()
	{
		@Override
		public int getItemViewType(int position)
		{
			return R.layout.item_page_comment;
		}

		@Override
		protected Comment getItem(int position)
		{
			return m_comments.getCommentsList().get(position);
		}

		@Override
		protected void onClickUser(View view, User user)
		{
			onClickUsername(user.getUsername());
		}

		@Override
		protected void onClickTranslate(View view, Comment comment)
		{
			onClickCommentTranslate(view, comment);
		}

		@Override
		protected void onClickReply(View view, Comment comment)
		{
			onClickCommentReply(comment);
		}

		@Override
		protected void onClickMenu(View view, Comment comment)
		{
			showCommentMenu(view, comment);
		}

		@Override
		protected void onClickUsername(String username)
		{
			AnalyticsManager.getInstance().eventUserActionProfile(getContext(), "PAGE_COMMENT");
			AnalyticsManager.getInstance().eventUserTrackingProfile(getContext(), username);

			if (UserManager.getInstance().isMe(username) == false)
			{
				AnalyticsManager.getInstance().eventUserActionOtherProfile(getContext(), "PAGE_COMMENT");
			}

			BackgroundPageFragment.this.onClickProfile(username);
		}

		@Override
		public int getItemCount()
		{
			if (m_comments == null || m_comments.getCommentsList() == null) return 0;

			if (m_comments.getCommentsList().size() > MAX_COMMENT_COUNT)
			{
				return MAX_COMMENT_COUNT;
			}

			return m_comments.getCommentsList().size();
		}
	};

	private void showCommentMenu(final View view, final Comment comment)
	{
		removeRewardAdToolTip();

		final PopupMenu popup = new PopupMenu(getContext(), view);
		popup.getMenuInflater().inflate(R.menu.comment, popup.getMenu());

		if (UserManager.getInstance().isMe(m_background.getUser()) == false &&
				UserManager.getInstance().isMe(comment.getUser()) == false)
		{
			popup.getMenu().findItem(R.id.comment_delete).setVisible(false);
		}

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				final int menuId = item.getItemId();

				switch (menuId)
				{
					case R.id.comment_report:
						onClickCommentReport(comment);
						return true;
					case R.id.comment_delete:
						onClickCommentDelete(comment);
						return true;
				}
				return false;
			}
		});
		popup.show();
	}

	public void onClickImageReport(final Background background)
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Report_OverFlow_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		if (UserManager.getInstance().isGuest() == true)
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "REPORT");
			startActivity(AuthActivity.createIntent(getContext(), AuthActivity.SIGN_ACTION_REPORT));
			return;
		}

		HashMap<String, String> itemsMap = new LinkedHashMap<>();
		itemsMap.put(getString(R.string.report_type_adult), "A");
		itemsMap.put(getString(R.string.report_type_spam), "S");
		itemsMap.put(getString(R.string.report_type_copyright), "C");
		itemsMap.put(getString(R.string.report_type_etc), "E");

		final UserReportAction.OnResultListener listener = new UserReportAction.OnResultListener()
		{
			@Override
			public void onResult(Boolean isSuccessed)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				String message = isSuccessed == true
						? getString(R.string.report_image_success)
						: getString(R.string.report_image_fail);

				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		};

		new UserReportAction.Builder(getContext())
				.setUrl(UrlFactory.reportImage())
				.setUuid(background.getUuid())
				.setButtonTextMap(itemsMap)
				.setDefaultItem("A")
				.setListener(listener)
				.run();
	}

	private synchronized void onClickCommentTranslate(final View view, final Comment comment)
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Translation_Detail");
		}
		catch (Exception ignored)
		{
		}

		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == false)
			return;

		final String origin = comment.getContent();
		final String language = Locale.getDefault().getLanguage();

		final TextView translate = view.findViewById(R.id.translate);
		final TextView description = view.findViewById(R.id.description);

		if (UserManager.getInstance().isGuest())
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAGE_COMMENT_TRANSLATE");
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_TRANSLATION));
			return;
		}

		if (comment.isTranslated() == true)
		{
			String content = comment.getContent();
			translate.setText(R.string.comment_translate);
			comment.setTranslated(false);

			if (content.contains("@") == false)
			{
				description.setText(content);
			} else
			{
				SpannableUtils.setUsernameClickable(description,
						content,
						"@",
						ContextCompat.getColor(getContext(), R.color.user_id_color),
						COMMENT_EMPHASIZE_LIMIT,
						new SpannableUtils.Clickable()
						{
							@Override
							public void onClick(String username)
							{
								BackgroundPageFragment.this.onClickProfile(username.substring(1));
							}
						});
			}
		} else
		{
			AnalyticsManager.getInstance().eventUserActionCommentTranslate(getActivity(), "PAGE_COMMENTS");
			AnalyticsManager.getInstance().eventUserTrackingTranslate(getContext(), language);
			if (TextUtils.isEmpty(comment.getTranslateContent()) == false)
			{
				translate.setText(R.string.comment_original);
				description.setText(comment.getTranslateContent());
				comment.setTranslated(true);
				return;
			}

			translate.setText(R.string.comment_translation);
			Requests.requestByPut(
					UrlFactory.commentTranslate(),
					ParamFactory.commentTranslate(language, origin, comment.getUuid()),
					CommentExtData.class,
					new Response.Listener<CommentExtData>()
					{
						@Override
						public void onResponse(CommentExtData commentExtData)
						{
							if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

							try
							{
								String content = commentExtData.getData().getComment();
								description.setText(content);
								comment.setTranslated(true);
								comment.setTranslateContent(content);
								translate.setText(R.string.comment_original);
							}
							catch (Exception e)
							{
								translate.setText(R.string.comment_translate);
								ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, R.string.comment_translate_fail).show();
							}
						}
					}, new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

							translate.setText(R.string.comment_translate);
							ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, R.string.comment_translate_fail).show();
						}
					});
		}
	}

	private void onClickCommentReport(final Comment comment)
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "ReportComment_Detail");
		}
		catch (Exception ignored)
		{
		}

		if (UserManager.getInstance().isGuest() == true)
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "REPORT");
			startActivity(AuthActivity.createIntent(getContext(), AuthActivity.SIGN_ACTION_REPORT_COMMENT));
			return;
		}

		HashMap<String, String> itemsMap = new LinkedHashMap<>();
		itemsMap.put(getString(R.string.report_type_adult), "A");
		itemsMap.put(getString(R.string.report_type_spam), "S");
		itemsMap.put(getString(R.string.report_type_censure), "C");
		itemsMap.put(getString(R.string.report_type_etc), "E");

		final UserReportAction.OnResultListener listener = new UserReportAction.OnResultListener()
		{
			@Override
			public void onResult(Boolean isSuccessed)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				String message = isSuccessed == true
						? getString(R.string.report_comment_success)
						: getString(R.string.report_comment_fail);

				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		};

		new UserReportAction.Builder(getContext())
				.setUrl(UrlFactory.reportComment())
				.setUuid(comment.getUuid())
				.setButtonTextMap(itemsMap)
				.setDefaultItem("C")
				.setListener(listener)
				.run();
	}

	private void onClickCommentReply(Comment comment)
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "Reply_Detail");
		}
		catch (Exception ignored)
		{
		}

		removeRewardAdToolTip();

		String text = "@" + comment.getUser().getUsername() + " ";

		final EditText inputComment = m_commentInputView.findViewById(R.id.input_comment);
		inputComment.append(text);
		inputComment.requestFocus();
	}

	private void onClickCommentDelete(final Comment comment)
	{
		removeRewardAdToolTip();

		MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
		{
			@Override
			public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
			{
				onCommentDelete(comment);
			}
		};

		new MaterialDialog.Builder(getContext())
				.title(R.string.comment_menu_delete_title)
				.content(R.string.comment_menu_delete_content)
				.autoDismiss(true)
				.negativeText(R.string.cancel)
				.positiveText(R.string.comment_menu_delete_button)
				.onPositive(onPositive)
				.show();
	}

	private void onCommentDelete(Comment comment)
	{
		removeRewardAdToolTip();

		onCommentDeleteHelper(comment, m_comments.getCommentsList(), m_commentsAdapter);
	}

	public void onCommentDeleteHelper(final Comment comment, final List<Comment> commentsList, final RecyclerView.Adapter adapter)
	{
		final int index = commentsList.indexOf(comment);

		if (index != -1)
		{
			commentsList.remove(index);
			adapter.notifyItemRemoved(index);
		}

		Requests.authRequestByDelete(
				UrlFactory.commentDelete(comment.getUuid()),
				null,
				Empty.class,
				new Response.Listener<Empty>()
				{
					@Override
					public void onResponse(Empty response)
					{
						if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true)
							return;

						try
						{
							m_comments.setTotalCount(m_comments.getCommentsList().size());
							setVisibilityCommentsViews();
						}
						catch (Exception e)
						{
							//nothing
						}
					}
				}, null);
	}

	@OnClick(R.id.view_all_comments)
	protected void onClickViewAllComments()
	{
		try
		{
			AnalyticsManager.getInstance().DetailEvent(getContext(), "CommentList_Detail");
		}
		catch (Exception ignored)
		{
		}

		ViewAllComments();
	}

	@OnClick({ R.id.text_view_all, R.id.btn_view_all })
	protected void onClickViewAllSimilars()
	{
		final String similarUrl = UrlFactory.similarBackgrounds(m_background.getUuid());
		onOpenBackgrounds(similarUrl);
		AnalyticsManager.getInstance().eventPageAllSimilar(getActivity());
	}

	@OnClick(R.id.action_collection)
	protected void onClickCollection()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_B) == false)
			return;

		AnalyticsManager.getInstance().DetailEvent(getContext(), "Collection_Detail");

		if (m_CollectionGuide != null)
		{
			m_CollectionGuide.cleanUp();
			m_CollectionGuide = null;
			PreferencesManager.getInstance().setCollectionGuideDetailShown(getContext(), true);
		}

		if (UserManager.getInstance().isGuest() == true)
		{
			//AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "PAGE_COMMENT_TRANSLATE");
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_TRANSLATION));
			return;
		}

		final String url = UrlFactory.collections();

		Requests.requestByGet(url, Collections.class, new Response.Listener<Collections>()
		{
			@Override
			public void onResponse(Collections response)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				if (response != null && response.getCollectionList() != null && response.getCollectionList().isEmpty() == false)
				{
					showSelectCollectionDialog();
				} else
				{
					showCreateCollectionDialog();
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				showCreateCollectionDialog();
			}
		});
	}

	private void showCreateCollectionDialog()
	{
		CollectionCreateDialogFragment.DialogCallback callback = new CollectionCreateDialogFragment.DialogCallback()
		{
			@Override
			public void onDismiss(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
			}

			@Override
			public void onClose(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
			}

			@Override
			public void onClickDone(final Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				AnalyticsManager.getInstance().DetailEvent(getContext(), "Confirm_Collection_Detail");

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{

							String title = fragment.getArguments().getString(CollectionCreateDialogFragment.KEY_TITLE);
							String msg = String.format(getString(R.string.toast_create_collection_with_title), title);

							CollectionGuideDialogFragment.DialogCallback callback = new CollectionGuideDialogFragment.DialogCallback()
							{
								public void onClickLink(Fragment fragment)
								{
									AnalyticsManager.getInstance().DetailEvent(getContext(), "Profie_Collection_Detail");
								}
							};

							CollectionGuideDialogFragment.start(getActivity().getSupportFragmentManager(), msg,
									CollectionGuideDialogFragment.LINK_TYPE_COLLECDTION, callback);
						}
						catch (Exception ignored)
						{
						}
					}
				}, 300);
			}
		};

		CollectionCreateDialogFragment.start(getActivity().getSupportFragmentManager(),
				m_background.getImage().getUrl(),
				m_background.getUuid(),
				CollectionCreateDialogFragment.TYPE_IMAGE,
				callback);
	}

	private void showSelectCollectionDialog()
	{
		CollectionSelectDialogFragment.DialogCallback callback = new CollectionSelectDialogFragment.DialogCallback()
		{
			@Override
			public void onDismiss(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
			}

			@Override
			public void onClose(Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;
			}

			@Override
			public void onClickCollection(final Fragment fragment)
			{
				if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true) return;

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							String title = fragment.getArguments().getString(CollectionSelectDialogFragment.KEY_TITLE);
							String msg = String.format(getString(R.string.toast_select_collection_with_title), title);
							ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, msg).show();
						}
						catch (Exception ignored)
						{
						}
					}
				}, 300);
			}

			@Override
			public void onClickAdd(Fragment fragment)
			{
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							showCreateCollectionDialog();
						}
						catch (Exception ignored)
						{
						}
					}
				}, 300);
			}
		};

		CollectionSelectDialogFragment.start(getActivity().getSupportFragmentManager(), m_background.getUuid(), CollectionSelectDialogFragment.TYPE_IMAGE, callback);
	}

	@OnClick(R.id.comments_count)
	protected void onClickCommentsCount()
	{
		ViewAllComments();
	}

	private void ViewAllComments()
	{
		removeRewardAdToolTip();

		Fragment fragment = CommentsFragment.newInstance(m_background);
		AbsMainActivity.getTabStackHelper(this).showFragment(fragment);
	}

	private void updateLikeCount()
	{
		int count = m_background.getLikesCount();
		boolean isLiked = LikesManager.getInstance().contains(m_background.getUuid());

		m_background.setLikesCount(isLiked ? --count : ++count);

		StaticUtils.setText(m_likesCountView, R.id.likes_count, m_background.getPrettyLikesCountPlus(), true);
		m_likesCountView.findViewById(R.id.likes_count).setVisibility(m_background.getLikesCount() == 0 ? View.GONE : View.VISIBLE);

		StaticUtils.setText(m_commentsCountView, R.id.comments_count, m_background.getPrettyCommentsCountPlus(), true);
		m_commentsCountView.findViewById(R.id.comments_count).setVisibility(m_background.getCommentsCount() == 0 ? View.GONE : View.VISIBLE);
	}

	private boolean checkCollectionGuideCondition()
	{
		int viewCount = PreferencesManager.getInstance().getBackgroundPageViewCount(getContext());
		boolean isShow = PreferencesManager.getInstance().getCollectionGuideDetailShown(getContext());

		if (viewCount >= 2 && isShow == false)
			return true;

		return false;
	}

	private void initCollectionGuide()
	{
		final Link adLink = getADLink();
		if (getUserVisibleHint() == false || adLink != null)
			return;

		if (UserManager.getInstance().isGuest() == true)
			return;

		if (checkCollectionGuideCondition() == true)
		{
			if (m_CollectionGuide != null)
			{
				m_CollectionGuide.cleanUp();
				m_CollectionGuide = null;
			}

			final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
			ViewGroup view = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.view_tooltip_collection_detail, null);
			ToolTip toolTip = new ToolTip()
					.setEnterAnimation(anim)
					.setCustomView(view);

			m_CollectionGuide = CollectionGuideDetailView.init(getActivity())
					.setToolTip(toolTip)
					.setOverlay(new Overlay())
					.playOn(m_collectionButton);
		}
	}

	public static class Empty
	{
		// Nothing
	}

	// Func : Auto Video Play(ExoPlayer)
	public void initExoPlayer()
	{
		try
		{
			if(isAutoVideoPlayerSupported() == false)
				return;

			if (m_Exoplayer == null)
			{
				String userAgent = Util.getUserAgent(getContext(), getString(R.string.app_name));
				Uri uri = Uri.parse(m_background.getLiveScreen().getPreview());

				DefaultTrackSelector trackSelector = new DefaultTrackSelector();

				m_Exoplayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
				m_Exoplayer.setVolume(0);
				m_Exoplayer.setRepeatMode(Player.REPEAT_MODE_ALL);
/*
				m_Exoplayer.addVideoListener(new VideoListener()
				{
					@Override
					public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio)
					{
						AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onVideoSizeChanged() File : " + m_background.getLiveScreen().getPreview());
						AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onVideoSizeChanged() - width : " + width + " / height : " + height + " / pixelWidthHeightRatio : " + pixelWidthHeightRatio);
					}

					@Override
					public void onSurfaceSizeChanged(int width, int height)
					{
						AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onSurfaceSizeChanged() File : " + m_background.getLiveScreen().getPreview());
						AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onSurfaceSizeChanged() - width : " + width + " / height : " + height);
					}
				});
*/
				m_ExoplayerListener = new Player.EventListener()
				{
					@Override
					public void onPlayerStateChanged(boolean playWhenReady, int playbackState)
					{
						//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onPlayerStateChanged() File : " + m_background.getLiveScreen().getPreview());
						//AppLogger.getInstance().d (AppLogger.TAG.UI, "TEST onPlayerStateChanged() - playbackState : " + playbackState + " / playWhenReady : " + playWhenReady);
						//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onPlayerStateChanged() getUserVisibleHint : " + getUserVisibleHint() + " / m_isPlaying : " + m_isPlaying);

						try
						{
							if(playWhenReady && playbackState == Player.STATE_READY)
							{
								m_previewProgressView.setVisibility(View.GONE);

								m_previewView.setVisibility(View.GONE);

								// Viewpager "우우좌좌우" 100%발생하는 이슢(화면 갱신안됨)
								// 이렇게 하면 단점은 onVideoSizeChanged 가 호출되어 사이즈 계산이 다시됨 / 이렇게되면 성능이 떨어지는 단말에서는 느림
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
								{
									//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onPlayerStateChanged() View Visibility Change!!!");
									m_previewVideoView.setVisibility(View.GONE);
								}
								m_previewVideoView.setVisibility(View.VISIBLE);

								//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST ===========================1");
							}

							switch (playbackState)
							{
								case Player.STATE_IDLE: // 1
									//재생 실패
									m_previewProgressView.setVisibility(View.VISIBLE);
									break;
								case Player.STATE_BUFFERING: // 2
									// 재생 준비
									m_previewProgressView.setVisibility(View.VISIBLE);
									break;
								case Player.STATE_READY: // 3
									m_ExoplayerLoadingFail = false;
									// 재생 준비 완료
									break;
								case Player.STATE_ENDED: // 4
									// 재생 마침
									break;
								default:
									break;
							}
						}
						catch (Exception e)
						{
							FirebaseCrashLog.log("BackgroundPageFragment initExoPlayer onPlayerStateChanged Exception");
							FirebaseCrashLog.logException(e);

							if (BuildConfig.DEBUG)
								e.printStackTrace();
						}
					}

					@Override
					public void onPlayerError(ExoPlaybackException error)
					{
						//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onPlayerError()");
						//AppLogger.getInstance().e(AppLogger.TAG.UI, error);

						try
						{
							ExoPlayLoadingFail();
						}
						catch (Exception e)
						{
							FirebaseCrashLog.log("BackgroundPageFragment initExoPlayer onPlayerError Exception");
							FirebaseCrashLog.logException(e);

							if (BuildConfig.DEBUG)
								e.printStackTrace();
						}
					}
				};

				m_Exoplayer.addListener(m_ExoplayerListener);

				m_previewVideoView.setOnTouchListener(new View.OnTouchListener()
				{
					private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener()
					{
						@Override
						public boolean onDoubleTap(MotionEvent e)
						{
							//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onDoubleTap()");
							setVideoFloatingLike();
							return super.onDoubleTap(e);
						}

						@Override
						public boolean onSingleTapConfirmed(MotionEvent e)
						{
							//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST onSingleTapConfirmed()");
							if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false)
								onClickPreview();

							return super.onSingleTapConfirmed(e);
						}
					});

					@Override
					public boolean onTouch(View v, MotionEvent event)
					{
						gestureDetector.onTouchEvent(event);
						return true;
					}
				});

				m_previewVideoView.setPlayer(m_Exoplayer);

				// Fill의 경우, app:resize_mode="fill" or and m_Exoplayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
				m_previewVideoView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

				// 캐시 폴더가 없는 경우 생성
				File isExoplayerCacheDir = new File(getContext().getCacheDir(), "exoplayer/" + m_background.getUuid());
				if(isExoplayerCacheDir != null && isExoplayerCacheDir.isDirectory() == false)
				{
					//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST initExoPlayer() not cache dir!!");
					isExoplayerCacheDir.mkdirs();
				}

				if(isCachingCheck())
				{
					// 캐시는 컨텐츠별 UUID 명으로 생성됨(cache/exoplayer/uuid)
					// Android OS 별로 캐시 사이즈 정함
					//LeastRecentlyUsedCacheEvictor evictor = null;
					//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					//	evictor = new LeastRecentlyUsedCacheEvictor(5 * 1024 * 1024);
					//else
					//	evictor = new LeastRecentlyUsedCacheEvictor(5 * 1024 * 1024);
					LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor(10 * 1024 * 1024);

					m_ExoplayerCache = new SimpleCache(new File(getContext().getCacheDir(), "exoplayer/" + m_background.getUuid()), evictor);
					CacheDataSourceFactory cache = new CacheDataSourceFactory(m_ExoplayerCache, new DefaultHttpDataSourceFactory(userAgent));
					m_MediaSource = new ExtractorMediaSource.Factory(cache).createMediaSource(uri);
				}
				else
				{
					m_MediaSource = new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(userAgent)).createMediaSource(uri);
				}

				m_Exoplayer.prepare(m_MediaSource, true, true);
				m_Exoplayer.setPlayWhenReady(m_isPlaying);

				//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST initExoPlayer() File : " + m_background.getLiveScreen().getPreview());
				//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST initExoPlayer() Cache UUID : " + m_background.getUuid());
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment initExoPlayer Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();

			ExoPlayLoadingFail();
		}
	}

	// Func : Auto Video Play(ExoPlayer)
	public void releaseExoPlayer()
	{
		try
		{
			if(isAutoVideoPlayerSupported() == false)
				return;

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST releaseExoPlayer() File : " + m_background.getLiveScreen().getPreview()) ;
			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST releaseExoPlayer()1");

			m_previewVideoView.setVisibility(View.INVISIBLE);

			if (m_Exoplayer != null)
			{
				m_Exoplayer.removeListener(m_ExoplayerListener);
				m_Exoplayer.release();

				m_Exoplayer = null;
				m_MediaSource = null;
				m_previewVideoView.setPlayer(null);

				if(m_ExoplayerCache != null)
					m_ExoplayerCache.release();
			}

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST releaseExoPlayer()2");
			File exoplayerCacheDir = new File(getContext().getCacheDir(), "exoplayer/" + m_background.getUuid());
			FileUtils.deleteQuietly(exoplayerCacheDir);

		} catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment releaseExoPlayer Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	private void ExoPlayLoadingFail()
	{
		try
		{
			releaseExoPlayer();
			m_ExoplayerLoadingFail = true;
			m_btnVideo.setVisibility(View.VISIBLE);
			m_previewProgressView.setVisibility(View.GONE);
			m_previewView.setVisibility(View.VISIBLE);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment ExoPlayLoadingFail Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	// Func : Auto Video Play(ExoPlayer)
	// ViewPager 좌우 스크롤 시 이벤트
	// 주의 사항 : videoLayoutSetting() 보다 좌우 이동을 통해 CheckVisibleExoPlayer()가 먼저 호출될 수 있음
	// 좌우 움직이고 페이지 이동을 하지 않는 경우, SCROLL_STATE_DRAGGING(1) -> SCROLL_STATE_SETTLING(2) -> SCROLL_STATE_IDLE(0)
	// 좌우 움직이고 페이지 이동을 한 경우, SCROLL_STATE_DRAGGING(1) -> SCROLL_STATE_SETTLING(2) -> selectedPosition -> SCROLL_STATE_IDLE(0)
	public void CheckVisibleExoPlayer(int selectedPosition, int state)
	{
		try
		{
			if(isAutoVideoPlayerSupported() == false)
				return;

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST CheckVisibleExoPlayer() File : " + m_background.getLiveScreen().getPreview()) ;
			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST CheckVisibleExoPlayer() getUserVisibleHint : " + getUserVisibleHint() + " / selectedPosition : " + selectedPosition + " / state : " +
			//		state);
			//AppLogger.getInstance().d(AppLogger.TAG.UI,
			//		"TEST CheckVisibleExoPlayer()1 m_position : " + m_position + " / m_DragPosition : " + m_DragPosition + " / m_isDragCompleted : " + m_isDragCompleted);

			// 상세화면에서 페이지 좌우 스크롤 중인 상태
			if(selectedPosition == -1)
			{
				// 좌우 페이지 이동을 한경우
				if (m_isDragCompleted)
				{
					m_isDragCompleted = false;

					if( m_DragPosition == m_position )
						m_isPlaying = true;
					else
						m_isPlaying = false;
				}
				// 좌우 페이지 이동 중인 경우
				else
				{
					if(state == ViewPager.SCROLL_STATE_IDLE)
					{
						if( getUserVisibleHint() )
							m_isPlaying = true;
						else
							m_isPlaying = false;

						m_isDragCompleted = false;
					}
					else
						m_isPlaying = false;
				}
			}
			// 상세화면에서 페이지 좌우 스크롤 완료 상태
			else
			{
				m_isDragCompleted = true;
				if( selectedPosition == m_position )
					m_DragPosition = selectedPosition;
				else
					m_DragPosition = -1;
				//AppLogger.getInstance().d(AppLogger.TAG.UI,
				//		"TEST CheckVisibleExoPlayer()2 m_position : " + m_position + " / m_DragPosition : " + m_DragPosition + " / m_isDragCompleted : " + m_isDragCompleted);

				return;
			}

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST CheckVisibleExoPlayer() m_isPlaying : " + m_isPlaying);

			if(m_isPlaying)
			{
				if(m_Exoplayer != null)
				{
					//setExoPlayerStart();
					m_Exoplayer.setPlayWhenReady(true);

					if(m_ExoplayerLoadingFail)
						m_Exoplayer.retry();
				}

				//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST ===========================2");
			}
			else
			{
				if(m_Exoplayer != null)
					m_Exoplayer.setPlayWhenReady(false);

				if(m_ExoplayerLoadingFail == false)
				{
					m_previewProgressView.setVisibility(View.VISIBLE);
					m_previewView.setVisibility(View.VISIBLE);
					m_btnVideo.setVisibility(View.GONE);
				}

				//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST ===========================3");
			}

		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment CheckVisibleExoPlayer Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	// Func : Auto Video Play(ExoPlayer)
	// 이미지 사이즈를 구한 후, 비디오 사이즈 셋팅 후 비디오 정보 초기화 작업 진행
	// 이유는 비디오 사이즈를 변경하면, onVideoSizeChanged()가 호출되고 다시 비디오가 로딩되기 때문에 재작업하여 느려짐
	private void videoLayoutSetting(int width, int height)
	{
		try
		{
			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST videoLayoutSetting() - width : " + width + " / height : " + height);

			if(isAutoVideoPlayerSupported() == false)
				return;

			m_previewVideoView.setVisibility(View.VISIBLE);
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) m_previewVideoView.getLayoutParams();
			lp.width = width; //FrameLayout.LayoutParams.MATCH_PARENT;
			lp.height = height;
			m_previewVideoView.setLayoutParams(lp);

			initExoPlayer();

			if(m_Exoplayer != null && m_Exoplayer.getPlayWhenReady() == false)
			{
				//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST videoLayoutSetting() File : " + m_background.getLiveScreen().getPreview());
				//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST videoLayoutSetting() - getHeight : " + m_previewVideoView.getHeight() + " / getWidth : " + m_previewVideoView.getWidth());

				if(getUserVisibleHint())
				{
					//setExoPlayerStart();
					m_Exoplayer.setPlayWhenReady(true);
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment videoLayoutSetting Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();

			ExoPlayLoadingFail();
		}
	}

	private void autoPlayRetry()
	{
		try
		{
			if(isAutoVideoPlayerSupported() == false)
				return;

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST autoPlayRetry() File : " + m_background.getLiveScreen().getPreview());
			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST autoPlayRetry() getUserVisibleHint() : " + getUserVisibleHint());
			if(m_Exoplayer != null && getUserVisibleHint())
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST autoPlayRetry() View Visibility Change!!!");
					//m_previewVideoView.setVisibility(View.VISIBLE);
					m_Exoplayer.retry();

				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment autoPlayRetry Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	private void autoPlayPause()
	{
		try
		{
			if(isAutoVideoPlayerSupported() == false)
				return;

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST autoPlayPause() File : " + m_background.getLiveScreen().getPreview());
			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST autoPlayPause() getUserVisibleHint() : " + getUserVisibleHint());
			if(m_Exoplayer != null && getUserVisibleHint())
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
				{
					//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST autoPlayPause() View Visibility Change!!!");
					//m_previewVideoView.setVisibility(View.INVISIBLE);
					m_Exoplayer.stop();
				}
			}

		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment autoPlayPause Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	// Internal 메모리 사용량이 300MB 이상일 경우 Cache 처리하도록 함
	public boolean isCachingCheck()
	{
		try
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
				return false;

			String path = Environment.getDataDirectory().getAbsolutePath();
			StatFs stat = new StatFs(path);

			//API 18부터 지원됨
			long blockSize = stat.getBlockSizeLong();

			//사용가능한 Internal Storage 크기
			long availableSize = stat.getAvailableBlocksLong() * blockSize;

			//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST isCachingCheck() availableSize : " + availableSize);

			if(availableSize >= 300 * 1024 * 1024)
				return true;
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment isCachingCheck Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}

		return false;
	}

	// 이 함수는 실제 동영상이 플레이될 경우만 호출되어야함.(동영상 선택 시 or 좌우 이동 시 호출됨)
	// 일단 주석 처리함
	// 장점 : 동영상이 중첩되어 보여지는 이슈 해결 / PlayerView를 GONE을 하더라도 발생함
	// 단점 : 동영상 좌우 이동 시 깜빡임
	private void setExoPlayerStart()
	{
		//AppLogger.getInstance().d(AppLogger.TAG.UI, "TEST setExoPlayerStart()");

		try
		{
			if( m_previewVideoView.getPlayer() == null)
				m_previewVideoView.setPlayer(m_Exoplayer);

			if(m_Exoplayer != null)
				m_Exoplayer.setPlayWhenReady(true);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment setExoPlayerStart Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();
		}
	}

	private void setVideoFloatingLike()
	{
		try
		{
			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false)
			{

				m_floatingLike.setVisibility(View.VISIBLE);
				m_floatingLike.setScaleX(0.3f);
				m_floatingLike.setScaleY(0.3f);

				m_floatingLike.animate().setInterpolator(new OvershootInterpolator())
						.scaleX(1.0f)
						.scaleY(1.0f)
						.setDuration(500)
						.setListener(new Animator.AnimatorListener()
						{
							@Override
							public void onAnimationStart(Animator animation)
							{

							}

							@Override
							public void onAnimationEnd(Animator animation)
							{
								if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false && m_floatingLike != null)
								{
									try
									{
										m_floatingLike.setChecked(true, true);
										new Handler().postDelayed(new Runnable()
										{
											@Override
											public void run()
											{
												if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false && m_floatingLike != null)
												{
													m_floatingLike.setVisibility(View.GONE);
													m_floatingLike.setChecked(false);
												}
											}
										}, 300);
									}
									catch (Exception ex)
									{
										if (BuildConfig.DEBUG)
											ex.printStackTrace();
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

			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false && LikesManager.getInstance().contains(m_background.getUuid()) == false)
			{
				try
				{
					AnalyticsManager.getInstance().DetailEvent(getContext(), "DoubleTapActionLike_Detail");
				}
				catch (Exception ignored)
				{
				}

				AnalyticsManager.getInstance().eventUserActionLike(getContext(), "PAGE_IMAGE");
				onClickLike();
			}
		}
		catch (Exception exp)
		{
			FirebaseCrashLog.log("BackgroundPageFragment setVideoFloatingLike Exception");
			FirebaseCrashLog.logException(exp);

			if (BuildConfig.DEBUG)
				exp.printStackTrace();
		}
	}
	
	private boolean isAutoVideoPlayerSupported()
	{
		try
		{
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
				return false;

			if(m_background.isLiveScreen() == false)
				return false;

			if(m_background.getLiveScreen() == null)
				return false;
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("BackgroundPageFragment isAutoVideoPlayerSupported Exception");
			FirebaseCrashLog.logException(e);

			if (BuildConfig.DEBUG)
				e.printStackTrace();

			return false;
		}

		return true;
	}

	//=========================================================================
	// DbclkHandler
	//=========================================================================

	private class DbclkHandler extends FloatingLikeActionHandler
	{
		public DbclkHandler(Fragment fragment)
		{
			super(fragment);
		}

		@Override
		protected void onSingleClick(View view, Background background)
		{
			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false)
			{
				DownloadAction action = null;
				if (m_background.isLiveScreen() == true)
				{
					action = new Mp4PreviewAction();
				}
				// [WatchFace Live WallPaper]
				else if (m_background.isExtensionLiveWatchType() == true)
				{
					action = new LiveWatchPreviewAction();
				} else
				{
					// GIF
					if (m_background.getImage() != null &&
							m_background.getImage().getUrl() != null && m_background.getImage().getUrl().isEmpty() == false &&
							m_background.getImage().getUrl().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION) == true)
						action = new GifPreviewAction();
					else
						action = new PreviewAction();
				}

				action.setDownloadMode(1);
				action.run(BackgroundPageFragment.this, m_background);
			}
		}

		@Override
		protected void onDoubleClick(View actionView, TextView countView, Background background, int position)
		{
			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == false && LikesManager.getInstance().contains(background.getUuid()) == false)
			{
				try
				{
					AnalyticsManager.getInstance().DetailEvent(getContext(), "DoubleTapActionLike_Detail");
				}
				catch (Exception ignored)
				{
				}

				AnalyticsManager.getInstance().eventUserActionLike(getContext(), "PAGE_IMAGE");
				onClickLike();
			}
		}
	}

	//========================================================================
	// FollowListListener
	//========================================================================

	@Override
	public void onSuccess()
	{
		if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true)
			return;

		if (m_background == null || m_background.getUser() == null ||
				FollowManager.getInstance().isFollowing(m_background.getUser().getUsername()) == true)
			m_follow.setVisibility(View.GONE);
	}

	@Override
	public void onFail(Exception e)
	{
		// Nothing
	}

	//=========================================================================
	// Callback
	//=========================================================================

	public interface Callback
	{
		void onActionBarAlpha(int position, int alpha);

		void onLoadNext(Response.Listener<Backgrounds> response, Response.ErrorListener errorResponse);
	}

	//=========================================================================
	// ActivityResultManager Callback
	//=========================================================================

	final ActivityResultManager.Callback m_resultCallback = new ActivityResultManager.Callback()
	{
		@Override
		public boolean onActivityResult(int requestCode, int resultCode, Intent intent)
		{
			if (FragmentUtils.isDestroyed(BackgroundPageFragment.this) == true || getUserVisibleHint() == false)
				return false;

			if (resultCode != Activity.RESULT_OK) return false;

			switch (requestCode)
			{
				case SetAsWallpaperAction.REQUEST_CODE_SET_AS_WALLPAPER:
				case SetAsVideoWallpaperAction.REQUEST_CODE_SET_AS_VIDEO_WALLPAPER:
				case SetAsLiveWatchWallpaperAction.REQUEST_CODE_SET_AS_LIVEWATCH_WALLPAPER:
					final Fragment fragment = AttachCompleteFragment.newInstance();
					AbsMainActivity.getTabStackHelper(BackgroundPageFragment.this).showFragment(fragment);
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

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_IS_SHOW_FAVORITE = "KEY_IS_SHOW_FAVORITE";
	private static final String KEY_COMMENTS = "KEY_COMMENTS";
	private static final String KEY_DATA_KEY = "KEY_DATA_KEY";
	private static final String KEY_POSITION = "KEY_POSITION";
	private static final String KEY_DATA_URL = "KEY_DATA_URL";
	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";
	private static final String KEY_SIMILARS = "KEY_SIMILARS";
	private static final String KEY_LIKERES = "KEY_LIKERES";

	private static final int MAX_SIMILAR_BACKGROUNDS_SIZE = 4;
	private static final int MAX_COMMENT_COUNT = 3;
	private static final int COMMENTS_SHOW_ALL_COUNT = 3;

	private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
	private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(android.R.id.content) LinearLayout m_contentLayout;
	@BindView(R.id.comments_container) ViewGroup m_commentsContainer;
	@BindView(R.id.comments_process) View m_commentsProgress;
	@BindView(R.id.scroll) NestedScrollViewEx m_scrollView;
	@BindView(R.id.comment_input) ViewGroup m_commentInputView;
	@BindView(R.id.page_progress) View m_pageProgressView;
	@BindView(R.id.preview_progress) View m_previewProgressView;
	@BindView(R.id.preview_layout) FrameLayout m_previewLayout;
	@BindView(R.id.preview) SnowImageView m_previewView;
	@BindView(R.id.preview_video) PlayerView m_previewVideoView;
	// droidsonroids.gif:android-gif-drawable
	//@BindView(R.id.preview_gif) GifImageView m_previewGifView;
	@BindView(R.id.floating_like) ShineButton m_floatingLike;
	@BindView(R.id.btn_video) ImageView m_btnVideo;
	@BindView(R.id.wallpaper) FloatingActionButton m_wallpaperButton;
	@BindView(R.id.fallow) Button m_follow;
	@BindView(R.id.likes_count) TextView m_likesCountView;
	@BindView(R.id.comments_count) TextView m_commentsCountView;
	@BindView(R.id.comments_list) RecyclerView m_commentsListView;
	@BindView(R.id.similar) ConstraintLayout m_layoutSimilar;
	@BindView(R.id.similar_samples) GridLayout m_similarContainer;
	@BindView(R.id.similar_progress) View m_similarProgress;
	@BindView(R.id.liker) RelativeLayout m_layoutLiker;
	@BindView(R.id.liker_progress) View m_likerProgress;
	@BindView(R.id.liker_samples) LinearLayoutCompat m_likerContainer;
	@BindView(R.id.action_like) ShineButton m_likeButton;
	@BindView(R.id.action_collection) ImageButton m_collectionButton;
	@BindView(R.id.action_download) ImageButton m_downloadButton;
	@BindView(R.id.action_toss) ImageButton m_tossButton;
	@BindView(R.id.user) RelativeLayout m_userContainer;

	// Func : Auto Video Play(ExoPlayer)
	private SimpleExoPlayer m_Exoplayer = null;					// ExpPlayer
	private SimpleCache m_ExoplayerCache = null;				// ExpPlayer 캐쉬 처리
	private ExtractorMediaSource m_MediaSource = null;
	private Player.EventListener m_ExoplayerListener = null; 	// ExpPlayer 이벤트 리슨너
	private boolean m_isDragCompleted = false; 					// ViewPager 좌우 이동 완료
	private int m_DragPosition = -1;							// ViewPager 좌우 이동 시 position
	private boolean m_isPlaying = false;						// ExpPlayer Play & Stop 처리
	private boolean m_ExoplayerLoadingFail = false;				// ExpPlayer 실패 처리

	private int m_position;
	private Background m_background;
	private BackgroundsModelData m_data;
	private PurchaseInfo m_purchaseInfo;
	private boolean isShowWallpaperButton = true;
	private IntegrateNativeAd m_nativeAd;
	private Likeres m_likeres;
	private Comments m_comments;
	private Backgrounds m_similars;
	private KeyboardChecker m_keyboardChecker = null;
	private boolean m_isPremissionGuide;
	private boolean m_isCommentPosting = false;
	private DbclkHandler m_handler = null;

	private MaterialDialog m_progressDialog;
	private MaterialDialog m_progressRewardAdDialog = null;
	private MaterialDialog m_popupRewardAdDialog;
	private Tooltip.Builder mTooltip = null;
	private CollectionGuideDetailView m_CollectionGuide;
	private Subscription m_subscription;
	private AdRewardListener m_rewardAdListener = null;

	private Menu m_menu;
	private Unbinder m_unbinder;
}