package com.ogqcorp.bgh.fragment.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.NotNull;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdIconView;
import com.facebook.ads.MediaView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.adapter.BackgroundsAdapter;
import com.ogqcorp.bgh.ads.AdCheckManager;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.fragment.BackgroundPageFragment;
import com.ogqcorp.bgh.fragment.PurchaseAdFreeDialogFragment;
import com.ogqcorp.bgh.fragment.UploadContentsFragment;
import com.ogqcorp.bgh.fragment.base.BaseLayoutFragmentEx;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.SimpleCreator;
import com.ogqcorp.bgh.spirit.data.Tag;
import com.ogqcorp.bgh.spirit.data.TagInfo;
import com.ogqcorp.bgh.spirit.data.TagInfoData;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;
import com.ogqcorp.commons.view.MeasuredFrameLayout;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public final class TagInfoFragment extends BaseLayoutFragmentEx implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public TagInfoFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
			m_tag = getArguments().getParcelable(KEY_TAG);
		initToolbar();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_taginfo, container, false);
	}

	@Override
	public void onViewCreated(View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
			m_tagInfo = savedInstanceState.getParcelable(KEY_TAG_INFO);

		m_swipeRefreshLayout.setPadding(0, -getActionBarHeight(), 0, 0);
		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		if (m_tagInfo == null)
			loadData();
		else
			initView();

		m_nativeAdContainer.setVisibility(View.GONE);
		AdCheckManager.getInstance().checkAdFree(new AdCheckManager.AdAvailabilityCallback()
		{
			@Override
			public void onAvailable()
			{
				m_nativeAds = AdCheckManager.getInstance().getShuffledNativeAdList(TagInfoFragment.this);

				if (m_nativeAds == null || m_nativeAds.size() == 0)
					onNotAvailable();
				else
					constructNativeAd();
			}

			@Override
			public void onNotAvailable()
			{
			}
		});
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

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_TAG_INFO, m_tagInfo);
	}

	@Override
	public void onRefresh()
	{
		loadData();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

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

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(Tag tag)
	{
		Fragment fragment = new TagInfoFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_TAG, tag);
		fragment.setArguments(bundle);

		return fragment;
	}

	public void onClickCreator(SimpleCreator creator)
	{
		if (creator.getUsername().equals(CreatorAdapter.KEY_UPLOAD))
		{
			AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "Upload1_TagComm");

			uploadContents();
		} else
		{
			AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "BestCreator_TagComm");

			String userInfoUrl = UrlFactory.usersInfo(creator.getUsername());

			Fragment fragment = UserInfoFragment.newInstance(userInfoUrl);
			AbsMainActivity.getTabStackHelper(TagInfoFragment.this).showFragment(fragment);
		}
	}

	public void onClickBackground(Background background)
	{
		AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "Content_TagComm");

		Fragment fragment = BackgroundPageFragment.newInstance(background);
		AbsMainActivity.getTabStackHelper(TagInfoFragment.this).showFragment(fragment);
	}

	@OnClick({ R.id.text_contents_all, R.id.btn_contents_all })
	protected void onClickViewAllContents()
	{
		try
		{
			AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "More_TagComm");

			Fragment fragment = TagContentsFragment.newInstance(m_tag.getTag());
			AbsMainActivity.getTabStackHelper(TagInfoFragment.this).showFragment(fragment);
		}
		catch (Exception ignored)
		{
		}
	}

	@OnClick(R.id.header_follow)
	public void onClickHeaderTagFollow()
	{
		if (UserManager.getInstance().isGuest() == true)
		{
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_NONE));
			return;
		}

		if (m_headerfollow == null)
			return;

		AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "Follow_TagComm");

		if (m_headerfollow.isSelected())
			tagFollow(m_headerfollow);
		else
			tagUnFollow(m_headerfollow);
	}

	public void tagFollow(final Button button)
	{
		final String origin = button.getText().toString();

		try
		{
			button.setText("...");

			String url = UrlFactory.tagFollow();
			HashMap params = ParamFactory.tagFollow(m_tagInfo.getId());

			Requests.authRequestByPost(url, params, Object.class, new Response.Listener<Object>()
			{
				@Override
				public void onResponse(Object response)
				{
					if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true) return;

					m_tagInfo.setFollow(true);
					button.setText(R.string.userinfo_following);
					button.setSelected(false);

					try
					{
						TagFollowGuideDialogFragment.start(getActivity().getSupportFragmentManager(), null);
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
					if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true) return;

					button.setText(origin);
					ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
				}
			});
		}
		catch (Exception e)
		{
			button.setText(origin);
			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
		}
	}

	public void tagUnFollow(final Button button)
	{
		final String origin = button.getText().toString();

		try
		{
			button.setText("...");

			String url = UrlFactory.tagUnFollow(m_tagInfo.getId());

			Requests.authRequestByDelete(url, null, Object.class, new Response.Listener<Object>()
			{
				@Override
				public void onResponse(Object response)
				{
					if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true) return;

					m_tagInfo.setFollow(false);
					button.setText(R.string.userinfo_follow);
					button.setSelected(true);
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true) return;

					button.setText(origin);
					ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
				}
			});
		}
		catch (Exception e)
		{
			button.setText(origin);
			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
		}
	}

	protected void onClickAdFree()
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

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void loadData()
	{
		if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true)
			return;

		if (m_isLoading == true)
			return;

		try
		{
			m_isLoading = true;
			String url = UrlFactory.getTagInfo(m_tag.getTag());
			if (UserManager.getInstance().isGuest() == true)
				Requests.requestByGet(url, TagInfoData.class, m_response, m_errorResponse);
			else
				Requests.authRequestByGet(url, TagInfoData.class, m_response, m_errorResponse);
		}
		catch (Exception ignored)
		{
		}
	}

	private void initView()
	{
		constructHeader();
		constructCreators();
		constructContents();
		initFabButton();
	}

	private void constructHeader()
	{
		String prettyCount = null;
		boolean isFollow = m_tagInfo.isFollow();

		if (m_tagInfo.getCount() > 0)
		{
			prettyCount = new StringBuffer()
					.append(m_tagInfo.gePrettytCount())
					.append(" ")
					.append(getString(R.string.userinfo_tabs_posts))
					.toString();
		}

		m_headerTitle.setText(m_tag.getPrettyTag());
		m_headerCount.setText(prettyCount);
		m_headerfollow.setSelected(!isFollow);
		m_headerfollow.setText(isFollow ? R.string.userinfo_following : R.string.userinfo_follow);
		m_headerfollow.setVisibility(View.VISIBLE);

		GlideApp.with(getContext())
				.load(getBackgroundUrl())
				.centerCrop()
				.diskCacheStrategy(DiskCacheStrategy.DATA)
				.transition(GenericTransitionOptions.with(R.anim.short_fade_in))
				.into(m_headerBackground);
	}

	private void constructCreators()
	{
		final LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		if (hasCreator() == false)
		{
			List<SimpleCreator> creators = new ArrayList<>();
			m_tagInfo.setCreators(creators);
		}

		if (hasUploadCard() == false)
		{
			SimpleCreator upload = new SimpleCreator();
			upload.setUsername(CreatorAdapter.KEY_UPLOAD);
			m_tagInfo.getCreators().add(upload);
		}

		m_creatorSamples.setLayoutManager(layout);
		m_creatorSamples.setAdapter(m_creatorAdapter);
		m_creatorProgress.setVisibility(View.GONE);
	}

	private void constructContents()
	{
		m_layout = new GridLayoutManagerEx(getActivity(), getContentsSpanCount());

		m_contentsSample.setLayoutManager(m_layout);
		m_contentsSample.setAdapter(m_contentsAdapter);
		m_contentsPrgress.setVisibility(View.GONE);
	}

	private void constructNativeAd()
	{
		try
		{
			m_nativeAdMainContent.setRatio(2d);

			TextView adfree = m_nativeAdContainer.findViewById(R.id.adfree);
			TextView body = m_nativeAdContainer.findViewById(R.id.native_ad_body);
			TextView title = m_nativeAdContainer.findViewById(R.id.native_ad_title);
			TextView button = m_nativeAdContainer.findViewById(R.id.native_ad_call_to_action);
			MediaView media = m_nativeAdContainer.findViewById(R.id.native_ad_media);
			AdIconView icon = m_nativeAdContainer.findViewById(R.id.native_ad_icon);
			FrameLayout adChoice = m_nativeAdContainer.findViewById(R.id.native_ad_choice);

			if (m_nativeAds != null && m_nativeAds.size() != 0)
			{
				int rand = new Random().nextInt(m_nativeAds.size());

				IntegrateNativeAd nativeAd = m_nativeAds.get(rand);

				body.setText(nativeAd.getAdBody());
				title.setText(nativeAd.getAdTitle());
				button.setText(nativeAd.getAdCallToAction());

				final String adBodyText = nativeAd.getAdBody();

				if (TextUtils.isEmpty(adBodyText != null ? adBodyText.trim() : ""))
					body.setVisibility(View.GONE);

				if (adChoice.getChildCount() > 0)
					adChoice.removeAllViews();

				AdChoicesView adChoicesView = new AdChoicesView(getContext(), nativeAd.getNativeAd(), true);
				adChoice.addView(adChoicesView);

				List<View> clickableViews = new ArrayList<>();
				clickableViews.add(icon);
				clickableViews.add(title);
				clickableViews.add(body);
				clickableViews.add(media);
				clickableViews.add(button);
				nativeAd.registerViewForInteraction(m_nativeAdContainer, media, icon, clickableViews);

				String content = new StringBuffer()
						.append(getString(R.string.adfree_title)).append(" ")
						.append(getString(R.string.pieinfo_tabs_charge)).toString();
				String word = getString(R.string.pieinfo_tabs_charge);

				adfree.setText(content);
				adfree.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						onClickAdFree();
					}
				});

				int start = content.lastIndexOf(word);
				int end = start + word.length();

				try
				{
					Spannable span = (Spannable) adfree.getText();
					span.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					span.setSpan(new ForegroundColorSpan(0xFF9B9B9B), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				}
				catch (Exception e)
				{
					e.toString();
				}

				m_nativeAdContainer.setVisibility(View.VISIBLE);
			}
		}
		catch (Exception e)
		{
			if (m_nativeAdMainContent != null)
				m_nativeAdContainer.setVisibility(View.GONE);
		}
	}

	private void initFabButton()
	{
		try
		{
			m_fabButton.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "Upload2_TagComm");
					uploadContents();
				}
			});
		}
		catch (Exception ignored)
		{
		}
	}

	private void uploadContents()
	{
		if (UserManager.getInstance().isGuest() == true)
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_NONE));
		else
		{
			Fragment fragment = UploadContentsFragment.newInstance();
			AbsMainActivity.getTabStackHelper(TagInfoFragment.this).showFragment(fragment);
		}
	}

	private boolean hasUploadCard()
	{
		if (hasCreator() == false)
			return false;

		for (SimpleCreator creator : m_tagInfo.getCreators())
		{
			if (creator.getUsername().equals(CreatorAdapter.KEY_UPLOAD))
				return true;
		}

		return false;
	}

	private boolean hasCreator()
	{
		if (m_tagInfo == null || m_tagInfo.getCreators() == null || m_tagInfo.getCreators().isEmpty())
			return false;

		return true;
	}

	private String getBackgroundUrl()
	{
		try
		{
			if (m_tagInfo != null || m_tagInfo.getContents() != null || m_tagInfo.getContents().isEmpty() == false)
			{
				int size = m_tagInfo.getContents().size();
				int rand = new Random().nextInt(size);

				return m_tagInfo.getContents().get(rand).getPreview().getUrl();
			}

			if (m_tagInfo != null || m_tagInfo.getCreators() != null || m_tagInfo.getCreators().isEmpty() == false)
			{
				for (SimpleCreator creator : m_tagInfo.getCreators())
				{
					if (creator.getThumbnail().isEmpty() == false)
						return creator.getThumbnail();
				}
			}

		}
		catch (Exception e)
		{
			return null;
		}

		return null;
	}

	private void initToolbar()
	{
		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

		if (toolbar == null)
			return;

		toolbar.setTitle("");
		toolbar.setTranslationY(0);
		toolbar.setBackgroundResource(R.color.transparent);
		toolbar.setNavigationIcon(R.drawable.ic_back_white_shadow);

		if (toolbar.getOverflowIcon() != null)
		{
			int color = getResources().getColor(R.color.white);
			toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_more);
			toolbar.setOverflowIcon(icon);
		}
	}

	private int getContentsSpanCount()
	{
		final Context context = getActivity();

		return DeviceUtils.isTablet(context) == true ? 4 : 2;
	}

	//=========================================================================
	// GalleryAdapter
	//=========================================================================

	private CreatorAdapter m_creatorAdapter = new CreatorAdapter()
	{

		@Override
		public int getItemCount()
		{
			return hasCreator() == false ? 0 : m_tagInfo.getCreators().size();
		}

		@Override
		protected SimpleCreator getItem(int position)
		{
			try
			{
				return m_tagInfo.getCreators().get(position);
			}
			catch (Exception e)
			{
				return null;
			}
		}

		@Override
		protected void onClickCreator(View view, SimpleCreator SimpleCreator)
		{
			TagInfoFragment.this.onClickCreator(SimpleCreator);
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private BackgroundsAdapter m_contentsAdapter = new BackgroundsAdapter()
	{
		@Override
		public int getItemViewType(int position)
		{
			return R.layout.item_background;
		}

		@Override
		protected void onClickAdFree()
		{
		}

		@Override
		public int getItemCount()
		{
			if (m_tagInfo == null || m_tagInfo.getContents() == null || m_tagInfo.getContents().isEmpty())
				return 0;

			return m_tagInfo.getContents().size();
		}

		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(@NotNull final ViewHolder holder, final int position)
		{
			onBindViewHolder(getActivity(), getBackground(position), holder, position);
		}

		@Override
		protected void onClickBackground(View view, Background background)
		{
			TagInfoFragment.this.onClickBackground(background);
		}

		@Override
		protected ArrayList<IntegrateNativeAd> getBackgroundNativeAdsList()
		{
			return null;
		}

		private Background getBackground(int position)
		{
			try
			{
				return m_tagInfo.getContents().get(position);
			}
			catch (Exception e)
			{
				return null;
			}
		}
	};

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<TagInfoData> m_response = new Response.Listener<TagInfoData>()
	{
		@Override
		public void onResponse(TagInfoData tagInfos)
		{
			if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true) return;

			m_isLoading = false;

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
				m_rootContainer.scrollTo(0, 0);
			}

			m_tagInfo = tagInfos.getTagInfo();
			initView();
		}
	};

	protected Response.ErrorListener m_errorResponse = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError volleyError)
		{
			if (FragmentUtils.isDestroyed(TagInfoFragment.this) == true) return;

			m_isLoading = false;

			if (m_swipeRefreshLayout.isRefreshing() == true)
				m_swipeRefreshLayout.setRefreshing(false);

			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_TAG = "KEY_TAG";
	private static final String KEY_TAG_INFO = "KEY_TAG_INFO";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.scroll) ViewGroup m_rootContainer;
	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	@BindView(R.id.header_title) TextView m_headerTitle;
	@BindView(R.id.header_count) TextView m_headerCount;
	@BindView(R.id.header_follow) Button m_headerfollow;
	@BindView(R.id.header_background) ImageView m_headerBackground;

	@BindView(R.id.creator_container) ConstraintLayout m_creatorContainer;
	@BindView(R.id.creator_samples) RecyclerView m_creatorSamples;
	@BindView(R.id.creator_loading_progress) View m_creatorProgress;

	@BindView(R.id.contents_container) ConstraintLayout m_contentsContainer;
	@BindView(R.id.contents_samples) RecyclerView m_contentsSample;
	@BindView(R.id.contents_loading_progress) View m_contentsPrgress;

	@BindView(R.id.native_ad_container) ViewGroup m_nativeAdContainer;
	@BindView(R.id.native_ad_main_content) MeasuredFrameLayout m_nativeAdMainContent;

	@BindView(R.id.fab) FloatingActionButton m_fabButton;

	private boolean m_isLoading;

	private Tag m_tag;
	private TagInfo m_tagInfo;

	private GridLayoutManager m_layout;
	private ArrayList<IntegrateNativeAd> m_nativeAds;

	private Unbinder m_unbinder;
}