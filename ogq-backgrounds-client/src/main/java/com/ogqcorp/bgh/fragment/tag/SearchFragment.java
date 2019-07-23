package com.ogqcorp.bgh.fragment.tag;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.adapter.FeaturedTagAdapter;
import com.ogqcorp.bgh.adapter.TagGridAdapter;
import com.ogqcorp.bgh.fragment.PurchaseAdFreeDialogFragment;
import com.ogqcorp.bgh.fragment.SearchDialogFragment;
import com.ogqcorp.bgh.fragment.UploadContentsFragment;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.SearchInfo;
import com.ogqcorp.bgh.spirit.data.SearchInfoData;
import com.ogqcorp.bgh.spirit.data.SimpleCreator;
import com.ogqcorp.bgh.spirit.data.Tag;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.commons.utils.DeviceUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public final class SearchFragment extends BaseActionBarFragment implements SwipeRefreshLayout.OnRefreshListener
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public SearchFragment()
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_search, container, false);
	}

	@Override
	public void onViewCreated(View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
		{
			m_searchInfo = savedInstanceState.getParcelable(KEY_SEARCH_INFO);
		}

		m_swipeRefreshLayout.setPadding(0, -getActionBarHeight(), 0, 0);
		m_swipeRefreshLayout.setColorSchemeResources(R.color.light_blue_900);
		m_swipeRefreshLayout.setOnRefreshListener(this);

		initToolbar();

		if (m_searchInfo == null)
		{
			loadData();
		}
		else
		{
			initView();
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
	public void onResume()
	{
		super.onResume();

		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
		toolbar.setVisibility(View.GONE);

	}

	@Override
	public void onPause()
	{
		super.onPause();

		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
		toolbar.setVisibility(View.VISIBLE);
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

		outState.putParcelable(KEY_SEARCH_INFO, m_searchInfo);
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
		catch (Exception e)
		{
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		Fragment fragment = new SearchFragment();
		return fragment;
	}

	public void onClickCreator(SimpleCreator creator)
	{
		if (creator.getUsername().equals(CreatorAdapter.KEY_UPLOAD))
		{
			AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "Upload1_TagComm");

			uploadContents();
		}
		else
		{
			AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "BestCreator_TagComm");
			AnalyticsManager.getInstance().searchEvent(getContext(), "Search1", "BestCreator_Search1");

			String userInfoUrl = UrlFactory.usersInfo(creator.getUsername());

			Fragment fragment = UserInfoFragment.newInstance(userInfoUrl);
			AbsMainActivity.getTabStackHelper(SearchFragment.this).showFragment(fragment);
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

	@OnClick(R.id.query)
	public void onClickQuery()
	{
		final FragmentManager fragmentManager = getChildFragmentManager();
		SearchDialogFragment.start(fragmentManager);

		AnalyticsManager.getInstance().eventExploreSearch(getActivity());
		AnalyticsManager.getInstance().searchEvent(getContext(), "Search1", "SearchBar_Search1");
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void loadData()
	{
		if (FragmentUtils.isDestroyed(SearchFragment.this) == true)
			return;

		if (m_isLoading == true)
			return;

		try
		{
			m_isLoading = true;
			String url = UrlFactory.searchRecommended();
			Requests.requestByGet(url, SearchInfoData.class, m_response, m_errorResponse);
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private boolean isEmptyFollowingTags()
	{
		if (m_searchInfo == null || m_searchInfo.getFollowingTags() == null || m_searchInfo.getFollowingTags().isEmpty())
			return true;

		return false;
	}

	private boolean isEmptyFeaturedTags()
	{
		if (m_searchInfo == null || m_searchInfo.getFeaturedTags() == null || m_searchInfo.getFeaturedTags().isEmpty())
			return true;

		return false;
	}

	private void initView()
	{
		constructCreators();
		constructFollowingTags();
		constructFeaturedTags();
	}

	private void initToolbar()
	{
		m_toolbar.setNavigationIcon(R.drawable.ic_back);
		m_toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getActivity().onBackPressed();
			}
		});
	}

	private void constructFollowingTags()
	{
		if (isEmptyFollowingTags() == true)
		{
			m_followingTagsView.setVisibility(View.GONE);
			m_followingTagsEmptyView.setVisibility(View.VISIBLE);
			return;
		}

		m_ftLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		m_followingTagsView.setLayoutManager(m_ftLayout);
		m_followingTagsView.setAdapter(m_followingAdapter);
		m_followingTagsView.setVisibility(View.VISIBLE);
		m_followingTagsEmptyView.setVisibility(View.GONE);
		m_followingAdapter.notifyDataSetChanged();
	}

	private void constructCreators()
	{
		final LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		if (hasCreator() == false)
		{
			List<SimpleCreator> creators = new ArrayList<>();
			m_searchInfo.setCreators(creators);
		}

		if (hasUploadCard() == false)
		{
			SimpleCreator upload = new SimpleCreator();
			upload.setUsername(CreatorAdapter.KEY_UPLOAD);
			m_searchInfo.getCreators().add(upload);
		}

		m_creatorSamples.setLayoutManager(layout);
		m_creatorSamples.setAdapter(m_creatorAdapter);
		m_creatorProgress.setVisibility(View.GONE);
	}

	private void constructFeaturedTags()
	{
		m_layout = new GridLayoutManagerEx(getActivity(), getContentsSpanCount());

		m_contentsSample.setLayoutManager(m_layout);
		m_contentsSample.setAdapter(m_featureAdapter);
		m_contentsPrgress.setVisibility(View.GONE);
	}

	private void uploadContents()
	{
		if (UserManager.getInstance().isGuest() == true)
		{
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_NONE));
		}
		else
		{
			Fragment fragment = UploadContentsFragment.newInstance();
			AbsMainActivity.getTabStackHelper(SearchFragment.this).showFragment(fragment);
		}
	}

	private boolean hasUploadCard()
	{
		if (hasCreator() == false)
			return false;

		for (SimpleCreator creator : m_searchInfo.getCreators())
		{
			if (creator.getUsername().equals(CreatorAdapter.KEY_UPLOAD))
				return true;
		}

		return false;
	}

	private boolean hasCreator()
	{
		if (m_searchInfo == null || m_searchInfo.getCreators() == null || m_searchInfo.getCreators().isEmpty())
		{
			return false;
		}

		return true;
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
			return hasCreator() == false ? 0 : m_searchInfo.getCreators().size();
		}

		@Override
		protected SimpleCreator getItem(int position)
		{
			try
			{
				return m_searchInfo.getCreators().get(position);
			}
			catch (Exception e)
			{
				return null;
			}
		}

		@Override
		protected void onClickCreator(View view, SimpleCreator SimpleCreator)
		{
			SearchFragment.this.onClickCreator(SimpleCreator);
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private TagGridAdapter m_featureAdapter = new TagGridAdapter()
	{
		@Override
		public int getItemCount()
		{
			if (isEmptyFeaturedTags() == true)
				return 0;

			return m_searchInfo.getFeaturedTags().size();
		}

		@Override
		protected Tag getItem(int position)
		{
			if (isEmptyFeaturedTags() == true)
				return null;

			return m_searchInfo.getFeaturedTags().get(position);
		}

		@Override
		protected void onClickTag(View view, Tag tag)
		{
			try
			{
				AnalyticsManager.getInstance().searchEvent(getContext(), "Search1", "BestTag_Search1");
				Fragment fragment = TagInfoFragment.newInstance(tag);
				AbsMainActivity.getTabStackHelper(SearchFragment.this).showFragment(fragment);
			}
			catch (Exception e)
			{

			}
		}
	};

	//=========================================================================
	// Following TagsAdapter
	//=========================================================================

	private FeaturedTagAdapter m_followingAdapter = new FeaturedTagAdapter()
	{
		@Override
		public int getItemCount()
		{
			if (isEmptyFollowingTags() == true)
				return 0;

			return m_searchInfo.getFollowingTags().size();
		}

		@Override
		protected Tag getItem(int position)
		{
			if (isEmptyFollowingTags() == true)
				return null;

			return m_searchInfo.getFollowingTags().get(position);
		}

		@Override
		protected void onClickTag(View view, Tag tag)
		{
			try
			{
				if (TextUtils.isEmpty(tag.getTagId()) == false &&
						tag.getTagId().equals(MORE_TAG_UUID) == true)
				{
					AnalyticsManager.getInstance().searchEvent(getContext(), "Search1", "FollowTagMore_Search1");

					final FragmentManager fragmentManager = getChildFragmentManager();
					SearchDialogFragment.start(fragmentManager);
				}
				else
				{
					AnalyticsManager.getInstance().searchEvent(getContext(), "Search1", "FollowTag_Search1");

					Fragment fragment = TagInfoFragment.newInstance(tag);
					AbsMainActivity.getTabStackHelper(SearchFragment.this).showFragment(fragment);
				}
			}
			catch (Exception e)
			{

			}
		}

		@Override
		protected boolean isSearchTag(int position)
		{
			try
			{
				Tag tag = m_searchInfo.getFollowingTags().get(position);

				if (tag.getTagId().equals(MORE_TAG_UUID))
				{
					return true;
				}
			}
			catch (Exception e)
			{
			}
			return false;
		}
	};

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<SearchInfoData> m_response = new Response.Listener<SearchInfoData>()
	{
		@Override
		public void onResponse(SearchInfoData info)
		{
			if (FragmentUtils.isDestroyed(SearchFragment.this) == true) return;

			m_isLoading = false;

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
				m_rootContainer.scrollTo(0, 0);
			}

			if (info != null)
			{
				m_searchInfo = info.getSearchInfo();

				if (isEmptyFollowingTags() == false)
				{
					Tag tag = new Tag();
					tag.setTagId(MORE_TAG_UUID);
					tag.setTag(getString(R.string.action_search));
					m_searchInfo.getFollowingTags().add(tag);
				}
				initView();
			}
		}
	};

	protected Response.ErrorListener m_errorResponse = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError volleyError)
		{
			if (FragmentUtils.isDestroyed(SearchFragment.this) == true) return;

			m_isLoading = false;

			if (m_swipeRefreshLayout.isRefreshing() == true)
			{
				m_swipeRefreshLayout.setRefreshing(false);
			}

			ToastUtils.makeWarningToast(getContext(), Toast.LENGTH_SHORT, R.string.error_code_xxx).show();
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_SEARCH_INFO = "KEY_SEARCH_INFO";

	public static final String MORE_TAG_UUID = "MORE_TAG_UUID";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.toolbar2) Toolbar m_toolbar;

	@BindView(R.id.scroll) ViewGroup m_rootContainer;
	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	@BindView(R.id.empty_view) View m_followingTagsEmptyView;
	@BindView(R.id.following_tags) RecyclerView m_followingTagsView;

	@BindView(R.id.creator_container) ConstraintLayout m_creatorContainer;
	@BindView(R.id.creator_samples) RecyclerView m_creatorSamples;
	@BindView(R.id.creator_loading_progress) View m_creatorProgress;

	@BindView(R.id.contents_container) ConstraintLayout m_contentsContainer;
	@BindView(R.id.contents_samples) RecyclerView m_contentsSample;
	@BindView(R.id.contents_loading_progress) View m_contentsPrgress;

	private boolean m_isLoading;

	private SearchInfo m_searchInfo;

	private GridLayoutManager m_layout;
	private LinearLayoutManager m_ftLayout;

	private Unbinder m_unbinder;
}