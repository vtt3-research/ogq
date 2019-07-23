package com.ogqcorp.bgh.fragment;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdIconView;
import com.facebook.ads.MediaView;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.ads.AdCenter;
import com.ogqcorp.bgh.ads.AdCenterCache;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.filter.FilterManager;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.gallery.GalleryAdapter;
import com.ogqcorp.bgh.gallery.GalleryFragment;
import com.ogqcorp.bgh.gcm.BusActivityEvent;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.multiwallpaper.MultiWall;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Backgrounds;
import com.ogqcorp.bgh.spirit.data.Gallery;
import com.ogqcorp.bgh.spirit.data.GalleryList;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.bgh.system.IntentLauncher;
import com.ogqcorp.bgh.system.Moho;
import com.ogqcorp.bgh.system.RxBus;
import com.ogqcorp.bgh.system.ShareManager;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.utils.BitmapUtils;
import com.ogqcorp.commons.utils.BundleUtils;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public final class AttachCompleteFragment extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		initValue(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_attach_complete, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		setHasOptionsMenu(true);

		initLayout(view);

		switch (m_wallpaperType)
		{
			case TYPE_VIDEO_COVER:
				constructLiveScreen();
				break;
			case TYPE_LIVEWATCH:
				constructLiveWatch();
				break;
			case TYPE_GIF:
				constructGif();
				break;
			case TYPE_GALLERY:
				constructGallery();
				break;
			default:
				constructImage();
		}

		boolean isAdFree = AdCheckManager.getInstance().isAdFree();

		if (m_wallpaperType != TYPE_MULTI_WALLPAPER)
		{
			if (isAdFree == false)
			{
				constructNativeAd(view);
			}

			switch (m_wallpaperType)
			{
				case TYPE_GALLERY:
				{
					loadRelatedGallery();
				}
				break;
				default:
					loadSimilarBackgrounds();
			}
		} else
		{
			if (isAdFree == false)
			{
				constructNativeBannerAd();
			}

			m_backgroundsView.setVisibility(View.GONE);
			MultiWall.getInstance(getContext()).stop();
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
	public void onPause()
	{
		m_adCenter.onPause();
		AdCenterCache.getInstance().onPause();
		super.onPause();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		m_adCenter.onResume();
		AdCenterCache.getInstance().onResume();
	}

	@Override
	public void onDestroyView()
	{
		m_adCenter.onDestroy();
		AdCenterCache.getInstance().onDestroy();
		m_unbinder.unbind();

		super.onDestroyView();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		BundleUtils.putFile(outState, "KEY_MOHO_FILE", m_file);
		outState.putParcelable("KEY_MOHO_BACKGROUND", m_background);
		outState.putParcelable("KEY_MOHO_GALLERY", m_gallery);
		outState.putInt(WALLPAPER_TYPE, m_wallpaperType);
		outState.putInt(FILTER_INDEX, m_filterIndex);
		outState.putString(IMAGE_PATH, m_videoUrl);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_attach_complete, menu);
		initToolbar();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.action_share:
			{
				onShare();
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(Bundle args)
	{
		final Fragment fragment = new AttachCompleteFragment();
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		final Fragment fragment = new AttachCompleteFragment();

		return BaseModel.wrap(fragment);
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

	@OnClick(R.id.login)
	public void onLogin(View view)
	{
		AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "COMPLETE");
		IntentLauncher.startAuthSignInActivity(getActivity(), AuthActivity.SIGN_ACTION_COMPLETED);
	}

	@OnClick({ R.id.text_view_all, R.id.btn_view_all })
	protected void onClickViewAllSimilars()
	{
		final String similarUrl = UrlFactory.similarBackgrounds(m_background.getUuid());
		onOpenBackgrounds(similarUrl);
		AnalyticsManager.getInstance().eventPageAllSimilar(getActivity());
	}

	public void onClickGallery(Gallery gallery)
	{
		try
		{
			AnalyticsManager.getInstance().galleryEvent(getContext(), "Similar_Detail_Gallery");
		}
		catch (Exception e)
		{
		}

		Fragment fragment = GalleryFragment.newInstance(gallery.getId());
		AbsMainActivity.getTabStackHelper(this).showFragment(fragment);
	}

	public void onShare()
	{
		if (m_wallpaperType == TYPE_GALLERY)
		{
			ShareManager.getInstance().share(this, ShareManager.TYPE_GALLERY, m_gallery.getId(), "");
		} else
		{
			ShareManager.getInstance().share(this, ShareManager.TYPE_BACKGROUND, m_background.getUuid(), "");
		}
	}

	public void constructLiveScreen()
	{
		Intent action = new Intent();
		BusActivityEvent event = new BusActivityEvent(action, Activity.RESULT_OK, BusActivityEvent.REQUEST_SET_VIDEO_WALLPAPER);
		RxBus.getInstance().onEvent(event);

		m_preview.setImageBitmap(ThumbnailUtils.createVideoThumbnail(m_videoUrl, MediaStore.Video.Thumbnails.MINI_KIND));
	}

	public void constructLiveWatch()
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
	}

	public void constructGif()
	{
		GlideApp.with(this)
				.asGif()
				.load(m_videoUrl)
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				.diskCacheStrategy(DiskCacheStrategy.DATA)
				.centerCrop()
				.transition(withCrossFade())
				.into(m_preview);
	}

	public void constructImage()
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
				final Bitmap filteredBitmap = FilterManager.getInstance().getFilteredBitmap(getContext(), bitmap, m_filterIndex);
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

	public void constructGallery()
	{
		constructImage();
		/*if (m_filterIndex == -1)
		{
			GlideApp.with(this).load(m_file).centerCrop().transition(withCrossFade()).into(m_preview);
		}
		else
		{
			Point size = BitmapUtils.getBitmapSize(m_file);
			Bitmap bitmap = BitmapUtils.createBitmapFromFile(m_file, Bitmap.Config.ARGB_8888, size.x, size.y);
			final Bitmap filteredBitmap = FilterManager.getInstance().getFilteredBitmap(getContext(), bitmap, m_filterIndex);
			m_preview.setImageBitmap(filteredBitmap);
			bitmap.recycle();
			bitmap = null;
		}*/

		m_backgroundsView.setVisibility(View.GONE);
		m_galleriesView.setVisibility(View.VISIBLE);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initValue(Bundle savedInstanceState)
	{
		Context context = getContext();
		if (savedInstanceState != null)
		{
			m_file = BundleUtils.getFile(savedInstanceState, "KEY_MOHO_FILE");
			m_background = savedInstanceState.getParcelable("KEY_MOHO_BACKGROUND");
			m_gallery = savedInstanceState.getParcelable("KEY_MOHO_GALLERY");
			m_wallpaperType = savedInstanceState.getInt(WALLPAPER_TYPE, TYPE_SINGLE_WALLPAPER);
			m_filterIndex = savedInstanceState.getInt(FILTER_INDEX, 0);
			m_videoUrl = savedInstanceState.getString(IMAGE_PATH);
		} else
		{
			//m_wallpaperType = getArguments().getInt(WALLPAPER_TYPE, TYPE_SINGLE_WALLPAPER);
			m_wallpaperType = Moho.getWallpaerType();

			String attachType;
			String imageId = "EMPTY";

			switch (m_wallpaperType)
			{
				case TYPE_SINGLE_WALLPAPER:
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
					AsyncStats.setAsWallpaper(context, imageId);
				}
				break;
				case TYPE_MULTI_WALLPAPER:
				{
					attachType = getArguments().getString(ATTACH_TYPE);
					String imagePath = getArguments().getString(IMAGE_PATH);
					if (TextUtils.isEmpty(imagePath) == false)
					{
						m_file = new File(imagePath);
					}
					m_filterIndex = -1;
				}
				break;
				case TYPE_VIDEO_COVER:
				{
					attachType = Moho.getAttachType();
					m_videoUrl = Moho.getImageUrl();
					m_background = Moho.getBackground();
					Moho.clearAttach();
					AsyncStats.setAsWallpaper(context, imageId);
				}
				break;
				case TYPE_LIVEWATCH:
				{
					attachType = Moho.getAttachType();
					m_background = Moho.getBackground();
					m_videoUrl = m_background.getPreview().getUrl();
					Moho.clearAttach();
					AsyncStats.setAsWallpaper(context, imageId);
				}
				break;
				case TYPE_GALLERY:
				{
					attachType = Moho.getAttachType();
					m_gallery = Moho.getGallery();
					m_file = Moho.getAttachFile();
					m_filterIndex = Moho.getFilterIndex();
					Moho.clearAttach();
					AsyncStats.setAsWallpaper(context, imageId);
				}
				break;
				default:
				{
					attachType = getArguments().getString(ATTACH_TYPE);
					m_videoUrl = getArguments().getString(IMAGE_PATH);
				}

			}

			if (TextUtils.isEmpty(attachType) == true)
			{
				AnalyticsManager.getInstance().eventCompleteSetAsUserFile(context);
			} else
			{
				AnalyticsManager.getInstance().eventCompleteSetAsWallpaper(context, attachType);
			}

			AnalyticsManager.getInstance().eventCompleteSetImageFilter(context, m_filterIndex);
		}

	}

	private void initLayout(View view)
	{
		Context context = getContext();
		TextView textMessage = view.findViewById(R.id.text_message);

		if (UserManager.getInstance().isGuest() == true)
		{
			//String msg = getString(R.string.sign_in_guide_1_content);
			String msg = getString(R.string.attach_complete_text_2) + "\n" + getString(R.string.live_wallpaper_compeleted);
			textMessage.setText(msg);

			m_login.setVisibility(View.VISIBLE);
			return;
		}

		switch (m_wallpaperType)
		{
			case TYPE_MULTI_WALLPAPER:
			{
				String strType = "";
				switch (PreferencesManager.getInstance().getMultiWallpaperType(context))
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
			}
			break;
			case TYPE_GIF:
			case TYPE_VIDEO_COVER:
			case TYPE_LIVEWATCH:
			{
				String msg = getString(R.string.attach_complete_text_2) + "\n" + getString(R.string.live_wallpaper_compeleted);
				textMessage.setText(msg);
			}
			break;
			case TYPE_GALLERY:
			{
				String msg = getString(R.string.gallery_attach_complete_text_1);
				textMessage.setText(msg);
			}
			break;
			default:
			{
				textMessage.setText(R.string.attach_complete_text_2);
			}
		}
	}

	private void initToolbar()
	{
		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
		toolbar.setTitle("");
		toolbar.setBackgroundResource(R.color.transparent);
		toolbar.setNavigationIcon(R.drawable.ic_back_white_shadow);
		toolbar.setTranslationY(0);

		if (toolbar.getOverflowIcon() != null)
		{
			int color = getResources().getColor(R.color.white);
			toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_more);
			toolbar.setOverflowIcon(icon);
		}
	}

	private void constructSimilarBackgrounds(List<Background> similarBackgrounds)
	{
		int rowCount = DeviceUtils.isTablet(getContext()) == true ? 1 : 2;
		int columnCount = DeviceUtils.isTablet(getContext()) == true ? 4 : 2;

		m_backgroundsSamples.setRowCount(rowCount);
		m_backgroundsSamples.setColumnCount(columnCount);

		m_backgroundsSamples.removeAllViews();
		m_backgroundsProgress.setVisibility(View.GONE);

		int maxSampleCount = Math.min(MAX_SIMILAR_BACKGROUNDS_SIZE, similarBackgrounds.size());

		int sidePadding = DisplayManager.getInstance().getPixelFromDp(getContext(), 16 * 2);
		int displayWidth = DisplayManager.getInstance().getDisplaySize(getContext()).x;
		int thumbnailSpace = DisplayManager.getInstance().getPixelFromDp(getContext(), 4);

		int size = (displayWidth - sidePadding - (MAX_SIMILAR_BACKGROUNDS_SIZE - 1) * thumbnailSpace) / columnCount;

		for (int i = 0; i < maxSampleCount; i++)
		{
			final ViewGroup similarItem = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_similar, m_backgroundsSamples, false);

			final ImageView image = similarItem.findViewById(R.id.image);
			final Background background = similarBackgrounds.get(i);

			final int index = i;
			similarItem.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					try
					{
						final String similarUrl = UrlFactory.similarBackgrounds(m_background.getUuid());
						final Fragment fragment = SimilarBackgroundFragment.newInstance(similarUrl, index);
						AbsMainActivity.getTabStackHelper(AttachCompleteFragment.this).showFragment(fragment);
					}
					catch (Exception e)
					{
						ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
					}
				}
			});

			GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED));
			lp.height = lp.width = size;

			m_backgroundsSamples.addView(similarItem, lp);

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

	private void constructNativeBannerAd()
	{
		AdCenterCache.getInstance().showNativeBanner(getActivity(), R.id.contents_container);
	}

	private void constructNativeAd(final View view)
	{
		final View nativeAdContainer = view.findViewById(R.id.native_ad_container);

		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				Fragment fragment = AttachCompleteFragment.this;

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

				int size = nativeAdsList.size();
				int index = new Random().nextInt(size);

				m_nativeAd = nativeAdsList.get(index < 0 ? 0 : index);

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
				if (FragmentUtils.isDestroyed(AttachCompleteFragment.this) == true) return;

				nativeAdContainer.setVisibility(View.GONE);
			}
		});
	}

	private boolean hasSimilarBackgrounds()
	{
		return m_relatedBackgrounds != null &&
				m_relatedBackgrounds.getBackgroundsList() != null &&
				m_relatedBackgrounds.getBackgroundsList().size() > 0;
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
				if (FragmentUtils.isDestroyed(AttachCompleteFragment.this) == true) return;

				m_relatedBackgrounds = response;

				if (hasSimilarBackgrounds() == true)
				{
					constructSimilarBackgrounds(m_relatedBackgrounds.getBackgroundsList());
				} else
				{
					m_backgroundsView.setVisibility(View.GONE);
				}
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(AttachCompleteFragment.this) == true) return;

				ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
			}
		});
	}

	private boolean hasRelatedGallery()
	{
		return m_relatedGalleries != null &&
				m_relatedGalleries.getGalleries() != null &&
				m_relatedGalleries.getGalleries().size() > 0;
	}

	private void constructRelatedGallery()
	{
		final LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
		DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL);
		Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.vertical_divider_white);
		horizontalDecoration.setDrawable(divider);

		m_galleriesSamples.setLayoutManager(layout);
		m_galleriesSamples.setAdapter(m_relatedGalleryAdapter);
		m_galleriesSamples.addItemDecoration(horizontalDecoration);
		m_galleriesView.setVisibility(View.VISIBLE);
		m_galleriesProgress.setVisibility(View.GONE);
	}

	private void loadRelatedGallery()
	{
		final String url = UrlFactory.galleryRelatedList(m_gallery.getId());

		Requests.requestByGet(url, GalleryList.class, new Response.Listener<GalleryList>()
		{
			@Override
			public void onResponse(GalleryList response)
			{
				if (FragmentUtils.isDestroyed(AttachCompleteFragment.this) == true) return;
				m_relatedGalleries = response;

				if (hasRelatedGallery() == true)
				{
					constructRelatedGallery();
				} else
				{
					m_backgroundsView.setVisibility(View.GONE);
				}

			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(AttachCompleteFragment.this) == true) return;

				VolleyErrorHandler errorHandler = new VolleyErrorHandler(getActivity());
				errorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				errorHandler.handleError(error);
			}
		});
	}

	//=========================================================================
	// GalleryAdapter
	//=========================================================================

	private GalleryAdapter m_relatedGalleryAdapter = new GalleryAdapter()
	{
		@Override
		public int getItemViewType(int position)
		{
			return R.layout.item_related_gallery;
		}

		@Override
		protected Gallery getItem(int position)
		{
			return m_relatedGalleries.getGalleries().get(position);
		}

		@Override
		protected void onClickGallery(View view, Gallery gallery)
		{
			AttachCompleteFragment.this.onClickGallery(gallery);
		}

		@Override
		public int getItemCount()
		{
			if (m_relatedGalleries == null || m_relatedGalleries.getGalleries() == null) return 0;

			return m_relatedGalleries.getGalleries().size();
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	public final static String WALLPAPER_TYPE = "WALLPAPER_TYPE";
	public final static String IMAGE_PATH = "IMAGE_PATH";
	public final static String ATTACH_TYPE = "ATTACH_TYPE";
	public final static String FILTER_INDEX = "FILTER_INDEX";

	public final static int TYPE_SINGLE_WALLPAPER = 0;
	public final static int TYPE_MULTI_WALLPAPER = 1;
	public final static int TYPE_VIDEO_COVER = 2;
	public final static int TYPE_GIF = 3;
	public final static int TYPE_LIVEWATCH = 4;
	public final static int TYPE_GALLERY = 5;

	private static final int MAX_SIMILAR_BACKGROUNDS_SIZE = 4;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.preview) ImageView m_preview;
	@BindView(R.id.login) Button m_login;

	@BindView(R.id.similar) View m_backgroundsView;
	@BindView(R.id.similar_samples) GridLayout m_backgroundsSamples;
	@BindView(R.id.similar_progress) View m_backgroundsProgress;

	@BindView(R.id.gallery_container) View m_galleriesView;
	@BindView(R.id.similar_gallery) RecyclerView m_galleriesSamples;
	@BindView(R.id.similar_gallery_progress) View m_galleriesProgress;

	private AdCenter m_adCenter = new AdCenter();
	private IntegrateNativeAd m_nativeAd;

	private Gallery m_gallery;
	private GalleryList m_relatedGalleries;

	private Background m_background;
	private Backgrounds m_relatedBackgrounds;

	private File m_file;
	private int m_filterIndex = 0;

	private String m_videoUrl;
	private int m_wallpaperType = TYPE_SINGLE_WALLPAPER;

	private Unbinder m_unbinder;
}
