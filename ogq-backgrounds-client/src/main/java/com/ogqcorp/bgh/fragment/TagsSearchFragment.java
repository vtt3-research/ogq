package com.ogqcorp.bgh.fragment;

import static com.ogqcorp.bgh.adapter.TagAdapter.UUID_SPLIT_TITLE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.adapter.TagAdapter;
import com.ogqcorp.bgh.fragment.tag.TagInfoFragment;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Tag;
import com.ogqcorp.bgh.spirit.data.Tags;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.commons.Log;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.JsonUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public class TagsSearchFragment extends Fragment implements SearchDialogFragment.OnQueryCallback
{
	//========================================================================
	// Public Methods
	//========================================================================

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_search_tag_dialog, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		m_historyOriginal = loadHistoryFromFile();

		m_filteredHistory = new ArrayList<>(m_historyOriginal);
		m_filteredResults = new ArrayList<>();

		m_layout = new GridLayoutManager(getActivity(), 1, LinearLayoutManager.VERTICAL, false);

		DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider);
		horizontalDecoration.setDrawable(horizontalDivider);

		m_mergedAdapter = new MergeRecyclerAdapter();
		m_mergedAdapter.add(getLayoutInflater(), R.layout.item_search_progress);

		m_resultList.setAdapter(m_mergedAdapter);
		m_resultList.setLayoutManager(m_layout);

		updateAdapter();
		loadRecommendTags();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			/*String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);*/
		}
		catch (Exception e)
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
	public void onHistorySearch(String query)
	{
		m_query = query;

		if (query.length() > 0)
		{
			m_filteredResults.clear();
			m_filteredResults.addAll(filterItemListByTag(m_historyOriginal, query));
			m_mergedAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNetworkSearch(String query)
	{
		m_query = query;

		if (checkQueryLength(query) == true)
		{
			showSearchProgress();
			requestSearchQuery(query);
		}
	}

	@Override
	public void onClearQuery()
	{
		m_query = "";

		m_filteredResults.clear();
		m_filteredHistory.clear();
		m_filteredHistory = new ArrayList<>(m_historyOriginal);
		m_mergedAdapter.notifyDataSetChanged();

		updateAdapter();

		if (m_request != null)
		{
			m_request.cancel();
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private synchronized List<Tag> loadHistoryFromFile()
	{
		final File dataFile = new File(getActivity().getFilesDir(), "/" + SEARCH_NAME);

		List<Tag> items;

		/** @formatter:x **/
		try
		{
			items = JsonUtils.OBJECT_MAPPER.readValue(dataFile, JsonUtils.OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Tag.class));
		}
		catch (Exception e)
		{
			items = new ArrayList<>();
		}
		/** @formatter:o **/

		return items;
	}

	private synchronized void saveHistoryToFile()
	{
		try
		{
			final File dataFile = new File(getActivity().getFilesDir(), "/" + SEARCH_NAME);
			JsonUtils.OBJECT_MAPPER.writeValue(dataFile, m_historyOriginal);
		}
		catch (Exception e)
		{
			Log.e(e);
		}
	}

	private void requestSearchQuery(final String query)
	{
		String url = UrlFactory.searchTags();
		m_request = Requests.requestByPost(url, ParamFactory.search(query), Tags.class, listener, errorListener);
	}

	private void loadRecommendTags()
	{
		if (hasRecommendTags() == true)
			return;

		try
		{
			String url = UrlFactory.featuredTags();

			Requests.requestByGet(url, Tags.class, new Response.Listener<Tags>()
			{
				@Override
				public void onResponse(Tags response)
				{
					try
					{
						if (response == null)
							return;

						m_recommendList = response.getTagsList();
						updateAdapter();
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

	private boolean hasRecommendTags()
	{
		if (m_recommendList == null || m_recommendList.isEmpty() == true)
			return false;

		return true;
	}

	private boolean hasTags()
	{
		if (m_tagList == null || m_tagList.isEmpty() == true)
			return false;

		return true;
	}

	private void updateAdapter()
	{
		try
		{
			if (m_tagList != null)
			{
				m_tagList.clear();
			}
			m_mergedAdapter.clear();

			if (TextUtils.isEmpty(m_query) == false)
			{
				if (hasResultTags() == false)
				{
					m_mergedAdapter.add(getLayoutInflater(), R.layout.item_search_user_empty);
					addRecommendTag();
				}
				else
				{
					addResultTag();
				}
			}else
			{
				addHistoryTag();
				addRecommendTag();
			}
			m_mergedAdapter.add(m_tagAdapter);
		}catch (Exception e)
		{

		}
	}

	private void addHistoryTag()
	{
		if (hasHistoryTags() == false)
			return;

		Tag split = new Tag();
		split.setTagId(UUID_SPLIT_TITLE);
		split.setTag(getString(R.string.search_history_title));

		if (hasTags() == false)
		{
			m_tagList = new ArrayList<>();
		}

		m_tagList.add(split);
		m_tagList.addAll(m_filteredHistory);
	}

	private void addRecommendTag()
	{
		if (hasRecommendTags() == false)
			return;

		Tag split = new Tag();
		split.setTagId(UUID_SPLIT_TITLE);
		split.setTag(getString(R.string.popular_tag_title));

		if (hasTags() == false)
		{
			m_tagList = new ArrayList<>();
		}

		m_tagList.add(split);
		m_tagList.addAll(m_recommendList);
	}

	private void addResultTag()
	{
		if (hasResultTags() == false)
			return;

		Tag split = new Tag();
		split.setTagId(UUID_SPLIT_TITLE);
		split.setTag(getString(R.string.search_results));

		if (hasTags() == false)
		{
			m_tagList = new ArrayList<>();
		}

		m_tagList.add(split);
		m_tagList.addAll(m_filteredResults);
	}

	private void removeHistoryItem(Tag item)
	{
		m_historyOriginal.remove(item);
		saveHistoryToFile();

		m_filteredHistory.remove(item);
		updateAdapter();
	}

	private boolean showHistoryRemoveDialog(final Tag item)
	{
		if (m_filteredHistory.contains(item) == false)
			return true;

		new MaterialDialog.Builder(getActivity())
				.title(R.string.search_delete_query_title)
				.content(R.string.search_delete_query_content)
				.positiveText(R.string.delete)
				.negativeText(R.string.cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						removeHistoryItem(item);
					}
				})
				.show();

		return true;
	}

	private boolean checkQueryLength(String query)
	{
		int length = query.length();

		if (isHangul(query) == true && length >= MIN_HANGUL_QUERY_LENGTH)
		{
			return true;
		}

		if (length >= MIN_QUERY_LENGTH)
		{
			return true;
		}

		return false;
	}

	private boolean isHangul(String text)
	{
		return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
	}

	private List<Tag> filterItemListByTag(List<Tag> itemList, String filterTag)
	{
		List<Tag> tempTags = new ArrayList<>();

		for (Tag tag : itemList)
		{
			String tagString = tag.getTag();
			if (tagString != null)
			{
				if (tagString.toLowerCase(Locale.US).contains(filterTag) == true)
				{
					tempTags.add(tag);
				}
			}
		}
		return tempTags;
	}

	private List<Tag> filterTagListByCount(List<Tag> tagList, int count)
	{
		List<Tag> tempTags = new ArrayList<>();

		for (Tag tag : tagList)
		{
			if (tag.getBackgroundsCount() > count)
			{
				tempTags.add(tag);
			}
		}
		return tempTags;
	}

	private void orderByBackgroundsCount(List<Tag> tagList)
	{
		Collections.sort(tagList, new Comparator<Tag>()
		{
			@Override
			public int compare(Tag lhs, Tag rhs)
			{
				return rhs.getBackgroundsCount() - lhs.getBackgroundsCount();
			}
		});
	}

	private void orderByExactQueryToFirst(List<Tag> tagList, String query)
	{
		for (Tag tag : tagList)
		{
			if (query.equals(tag.getTag()) == true)
			{
				tagList.remove(tag);
				tagList.add(0, tag);
				break;
			}
		}
	}

	private void colorizeTextView(TextView textView, String fullText, String queryText, int color)
	{
		int sIdx = fullText.toLowerCase().indexOf(queryText);
		int eIdx = sIdx + queryText.length();

		if (sIdx == -1 || TextUtils.isEmpty(queryText))
		{
			textView.setText(fullText);
		}
		else
		{
			final SpannableStringBuilder sp = new SpannableStringBuilder(fullText);
			sp.setSpan(new ForegroundColorSpan(color), sIdx, eIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			textView.setText(sp);
		}
	}

	//========================================================================
	// Listener
	//========================================================================

	private Response.Listener<Tags> listener = new Response.Listener<Tags>()
	{
		@Override
		public void onResponse(Tags data)
		{
			if (FragmentUtils.isDestroyed(TagsSearchFragment.this) == true) return;

			if (data == null) return;

			List<Tag> itemList = data.getTagsList();
			//itemList.removeAll(m_filteredHistory);
			itemList = filterTagListByCount(itemList, 0);

			reorderNetworkResponseData(itemList, m_query);

			m_filteredResults.clear();
			m_filteredResults.addAll(itemList);

			updateAdapter();
		}
	};

	private Response.ErrorListener errorListener = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError error)
		{
			if (FragmentUtils.isDestroyed(TagsSearchFragment.this) == true) return;

			updateAdapter();
			ToastUtils.makeErrorToast(getActivity(), Toast.LENGTH_SHORT, error.toString()).show();
		}
	};

	//========================================================================
	// Protected Methods
	//========================================================================

	protected void showSearchProgress()
	{
		m_mergedAdapter.clear();
		m_mergedAdapter.add(getLayoutInflater(), R.layout.item_search_progress);
		m_mergedAdapter.notifyDataSetChanged();
	}

	protected void reorderNetworkResponseData(List<Tag> tags, String query)
	{
		orderByBackgroundsCount(tags);
		orderByExactQueryToFirst(tags, query);
	}

	protected void onClickTag(Tag tag)
	{
		m_historyOriginal.remove(tag);
		m_historyOriginal.add(0, tag);

		if (m_historyOriginal.size() > MAX_RECENT_TAGS_SIZE)
		{
			m_historyOriginal.remove(m_historyOriginal.size() - 1);
		}

		saveHistoryToFile();

		AnalyticsManager.getInstance().eventSearchTag(getContext(), tag.getTag());
		AnalyticsManager.getInstance().searchEvent(getContext(), "Search2", "RecomTag_Search2");

		Fragment fragment = TagInfoFragment.newInstance(tag);
		AbsMainActivity.getTabStackHelper(TagsSearchFragment.this).showFragment(fragment);
	}

	private boolean hasHistoryTags()
	{
		if (m_filteredHistory == null || m_filteredHistory.isEmpty() == true)
			return false;

		return true;
	}

	private boolean hasResultTags()
	{
		if (m_filteredResults == null || m_filteredResults.isEmpty() == true)
			return false;

		return true;
	}

	//========================================================================
	// Adapter
	//========================================================================

	private TagAdapter m_tagAdapter = new TagAdapter()
	{
		@Override
		protected Tag getItem(int position)
		{
			return m_tagList.get(position);
		}

		@Override
		protected void onClickTag(View view, Tag tag)
		{
			TagsSearchFragment.this.onClickTag(tag);
		}

		@Override
		protected boolean onLongClickTag(View view, Tag tag)
		{
			return showHistoryRemoveDialog(tag);
		}

		@Override
		protected void onClickFollow(View view, Tag tag)
		{
		}

		@Override
		public int getItemCount()
		{
			if (hasTags() == false)
				return 0;

			return m_tagList.size();
		}

		@Override
		protected boolean hideFollowButton()
		{
			return true;
		}

		@Override
		protected void modifyView(TagAdapter.ViewHolder viewHolder, Tag tag)
		{
			final TextView name = viewHolder.getView().findViewById(R.id.name);

			colorizeTextView(name, tag.getTag(), m_query, Color.RED);
		}
	};

	//========================================================================
	// Constructor
	//========================================================================

	public static Fragment newInstance()
	{
		return new TagsSearchFragment();
	}

	//========================================================================
	// Constants
	//========================================================================

	private static final String SEARCH_NAME = "tags";

	private static final int MAX_RECENT_TAGS_SIZE = 5;
	private static final int MIN_QUERY_LENGTH = 3;
	private static final int MIN_HANGUL_QUERY_LENGTH = 1;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(android.R.id.list) RecyclerView m_resultList;

	protected Request m_request;

	private String m_query = "";
	private List<Tag> m_historyOriginal;
	private List<Tag> m_filteredHistory;
	private List<Tag> m_filteredResults;
	private List<Tag> m_recommendList;
	private List<Tag> m_tagList;

	protected MergeRecyclerAdapter m_mergedAdapter;

	private GridLayoutManager m_layout;

	private Unbinder m_unbinder;
}
