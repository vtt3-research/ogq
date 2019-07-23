package com.ogqcorp.bgh.fragment.tag;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ogqcorp.bgh.Application;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.BackgroundsFragment;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;

public final class TagContentsFragment extends BackgroundsFragment
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public TagContentsFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onInitActionBar()
	{
		super.onInitActionBar();

		initToolbar();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		if (getArguments().containsKey(KEY_TAG))
		{
			m_tag = getArguments().getString(KEY_TAG);
		}

		if (TextUtils.isEmpty(m_tag))
		{
			throw new AssertionError("Tag does not exist.");
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		setHasOptionsMenu(true);
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
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected String getDataUrl()
	{
		String order = m_mode == MODE_POPULAR ? ORDER_BY_POPULAR : ORDER_BY_RECENT;

		return UrlFactory.getTagContents(m_tag, order);
	}

	protected void updateHeaderViews()
	{
		final ViewGroup headersView = (ViewGroup) m_mergeAdapter.findViewById(R.id.headers);
		if (headersView.getChildCount() > 0)
			return;

		final View headerView = getLayoutInflater().inflate(R.layout.item_header_tag_content, headersView, false);
		final TextView recentView = headerView.findViewById(R.id.recent);
		final TextView popularView = headerView.findViewById(R.id.popular);

		recentView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (m_mode == MODE_RECENT)
					return;

				m_mode = MODE_RECENT;
				recentView.setTextColor(v.getResources().getColor(R.color.default_main_text_color));
				popularView.setTextColor(v.getResources().getColor(R.color.default_sub_text_color));

				m_swipeRefreshLayout.setRefreshing(true);
				onRefresh();
			}
		});

		popularView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (m_mode == MODE_POPULAR)
					return;

				AnalyticsManager.getInstance().tagCommunityEvent(getContext(), "Best_More_TagComm");

				m_mode = MODE_POPULAR;
				recentView.setTextColor(v.getResources().getColor(R.color.default_sub_text_color));
				popularView.setTextColor(v.getResources().getColor(R.color.default_main_text_color));

				m_swipeRefreshLayout.setRefreshing(true);
				onRefresh();
			}
		});

		headersView.addView(headerView);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Fragment newInstance(String tag)
	{
		final Bundle args = new Bundle();
		args.putString(KEY_TAG, tag);

		final Fragment fragment = new TagContentsFragment();
		fragment.setArguments(args);

		return BaseModel.wrap(fragment);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initToolbar()
	{
		int color = getResources().getColor(R.color.black);
		Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

		toolbar.setTranslationY(0);
		toolbar.setBackgroundResource(R.drawable.actionbar_bg);
		if (toolbar.getOverflowIcon() != null)
		{
			toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		}

		toolbar.setTitle(R.string.contents_title);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			toolbar.setElevation(0);
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================

	public static final int MODE_RECENT = 0;
	public static final int MODE_POPULAR = 1;

	public static final String KEY_TAG = "KEY_TAG";

	public static final String ORDER_BY_RECENT = "recent";
	public static final String ORDER_BY_POPULAR = "popular";

	//=========================================================================
	// Variables
	//=========================================================================

	private int m_mode = MODE_POPULAR;
	private String m_tag;

}
