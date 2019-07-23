package com.ogqcorp.bgh.fragment;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.adapter.BackgroundsAdapter;
import com.ogqcorp.bgh.adapter.FeaturedTagAdapter;
import com.ogqcorp.bgh.fragment.tag.TagInfoFragment;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Tag;
import com.ogqcorp.bgh.spirit.data.Tags;
import com.ogqcorp.bgh.spirit.data.TopBanner;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.TopBannerManager;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public final class RecentFragment extends BackgroundsFragment
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public RecentFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_recent, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState != null)
		{
			m_tagIndex = savedInstanceState.getInt(KEY_TAG_INDEX, 0);
			m_tags = savedInstanceState.getParcelable(KEY_TAGS);
		}

		if (isEmptyFeaturedTags() == true)
		{
			loadFeaturedTags();
		}
		else
		{
			constructFeaturedTags();
		}
	}

	@Override
	protected void onInitActionBar()
	{
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected String getDataUrl()
	{
		return UrlFactory.recent();
	}

	@Override
	protected boolean isNew(Background background)
	{
		long gap = (System.currentTimeMillis() - background.getPubDate()) / (3600000);
		return (gap < BackgroundsAdapter.IS_NEW_BACKGROUND_HOUR) ? true : false;
	}

	@Override
	public int getHeaderCount()
	{
		int headerCount = HEADER_COUNT;
		if (TopBannerManager.getInstance().isOpenBanner() == true)
		{
			headerCount += 1;
		}

		if (hasHeaderTags() == true)
		{
			headerCount += 1;
		}

		return headerCount;
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

	@Override
	public void onStop()
	{
		super.onStop();

		if (m_isTopBannerClose == true)
		{
			m_isTopBannerClose = false;
			updateList();
		}
	}

	@Override
	public void onRefresh()
	{
		super.onRefresh();

		loadFeaturedTags();
	}

	@Override
	public boolean isTopBanner()
	{
		return (TopBannerManager.getInstance().getTopBanner() != null) ? true : false;
	}

	@Override
	public TopBanner getTopBanner()
	{
		return TopBannerManager.getInstance().getTopBanner();
	}

	@Override
	protected boolean isTopBannerOpen()
	{
		return TopBannerManager.getInstance().isOpenBanner();
	}

	@Override
	protected void onClickTopBanner(final View view, final BackgroundsAdapter adapter)
	{
		if (TopBannerManager.getInstance().isOpenBanner() == true)
		{
			TopBannerManager.getInstance().clickBanner(RecentFragment.this.getContext());

			if (TopBannerManager.getInstance().isBannerCloseAction())
				m_isTopBannerClose = true;

			try
			{
				TopBanner banner = TopBannerManager.getInstance().getTopBanner();
				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = Uri.parse(banner.getLink());
				i.setData(u);
				startActivity(i);

				if (banner.getLinkType().equals(TopBanner.TYPE_IFRAME))
				{
					ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.banner_click_message).show();
				}
			}
			catch (Exception e)
			{
				//ActivityNotFoundException occurred in certain devices
				ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.error_code_unknown).show();
			}
		}
	}

	@Override
	protected void onClickTopBannerClose(final View view, final BackgroundsAdapter adapter)
	{
		if (TopBannerManager.getInstance().isOpenBanner() == true)
		{
			TopBannerManager.getInstance().closeBanner(RecentFragment.this.getContext());
			closeBanner(view, adapter);
		}
	}

	private void closeBanner(final View view, final BackgroundsAdapter adapter)
	{
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (FragmentUtils.isDestroyed(RecentFragment.this) == true)
				{
					return;
				}
				RecentFragment.this.m_listView.scrollToPosition(2);
			}
		}, 150);

		view.setScaleX(1.0f);
		view.setScaleY(1.0f);

		view.animate().setInterpolator(new OvershootInterpolator())
				.scaleX(0.0f)
				.scaleY(0.0f)
				.setDuration(300)
				.setListener(new Animator.AnimatorListener()
				{
					@Override
					public void onAnimationStart(Animator animation)
					{

					}

					@Override
					public void onAnimationEnd(Animator animation)
					{
						if (FragmentUtils.isDestroyed(RecentFragment.this) == true)
						{
							return;
						}

						adapter.notifyDataSetChanged();
						view.setScaleX(1.0f);
						view.setScaleY(1.0f);
						view.setVisibility(View.GONE);
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

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		int index = 0;
		if (m_layoutManager != null)
		{
			index = m_layoutManager.findFirstVisibleItemPosition();
		}

		outState.putInt(KEY_TAG_INDEX, index);
		outState.putParcelable(KEY_TAGS, m_tags);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		final Fragment fragment = new RecentFragment();
		return BaseModel.wrap(fragment);
	}

	boolean m_isTopBannerClose = false;

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void loadFeaturedTags()
	{
		try
		{
			m_tagIndex = 0;
			String url = UrlFactory.featuredTags();

			Requests.requestByGet(url, Tags.class, new Response.Listener<Tags>()
			{
				@Override
				public void onResponse(Tags response)
				{
					try
					{
						if (isEmptyFeaturedTags() == false)
						{
							m_tags.getTagsList().clear();
						}

						m_tags = response;
						if (isEmptyFeaturedTags() == false)
						{
							Tag tag = new Tag();
							tag.setTagId(MORE_TAG_UUID);
							tag.setTag(getString(R.string.more));
							m_tags.getTagsList().add(tag);
						}
						constructFeaturedTags();
					}
					catch (Exception e)
					{
					}
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{

				}
			});
		}
		catch (Exception e)
		{
		}
	}

	private void constructFeaturedTags()
	{
		final ViewGroup viewGroup = (ViewGroup) m_mergeAdapter.findViewById(R.id.featured_tags);
		final RecyclerView recyclerView = viewGroup.findViewById(R.id.featured_tags_list);

		if (isEmptyFeaturedTags() == true)
		{
			viewGroup.setVisibility(View.GONE);
			return;
		}

		m_layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

		recyclerView.setLayoutManager(m_layoutManager);
		recyclerView.setAdapter(m_tagAdapter);
		recyclerView.scrollToPosition(m_tagIndex);
		recyclerView.setVisibility(View.VISIBLE);

		viewGroup.setVisibility(View.VISIBLE);
		m_mergeAdapter.notifyDataSetChanged();
	}

	private boolean isEmptyFeaturedTags()
	{
		if (m_tags == null || m_tags.getTagsList() == null || m_tags.getTagsList().isEmpty())
			return true;

		return false;
	}

	@Override
	protected boolean hasHeaderTags()
	{
		return true;
	}

	//=========================================================================
	// HeaderTagsAdapter
	//=========================================================================

	private FeaturedTagAdapter m_tagAdapter = new FeaturedTagAdapter()
	{
		@Override
		public int getItemCount()
		{
			if (isEmptyFeaturedTags() == true)
				return 0;

			return m_tags.getTagsList().size();
		}

		@Override
		protected Tag getItem(int position)
		{
			if (isEmptyFeaturedTags() == true)
				return null;

			return m_tags.getTagsList().get(position);
		}

		@Override
		protected void onClickTag(View view, Tag tag)
		{
			try
			{
				if (TextUtils.isEmpty(tag.getTagId()) == false &&
						tag.getTagId().equals(MORE_TAG_UUID) == true)
				{
					AnalyticsManager.getInstance().RecentEvent(getContext(), "Search_Recent", null);

					final FragmentManager fragmentManager = getChildFragmentManager();
					SearchDialogFragment.start(fragmentManager);
				}
				else
				{
					AnalyticsManager.getInstance().RecentEvent(getContext(), "Tag_Recent", null);

					Fragment fragment = TagInfoFragment.newInstance(tag);
					AbsMainActivity.getTabStackHelper(RecentFragment.this).showFragment(fragment);
				}
			}
			catch (Exception ignored)
			{
			}
		}

		@Override
		protected boolean isSearchTag(int position)
		{
			try
			{
				Tag tag = m_tags.getTagsList().get(position);

				if (tag.getTagId().equals(MORE_TAG_UUID))
				{
					return true;
				}
			}
			catch (Exception ignored)
			{
			}
			return false;
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_TAGS = "KEY_TAGS";
	private static final String KEY_TAG_INDEX = "KEY_TAG_INDEX";

	public static final String MORE_TAG_UUID = "MORE_TAG_UUID";

	//=========================================================================
	// Variables
	//=========================================================================

	private Tags m_tags;
	private int m_tagIndex;

	private LinearLayoutManager m_layoutManager;
}
