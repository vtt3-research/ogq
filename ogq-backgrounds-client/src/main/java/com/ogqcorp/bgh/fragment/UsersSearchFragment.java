package com.ogqcorp.bgh.fragment;

import static com.ogqcorp.bgh.adapter.FollowAdapter.UUID_SPLIT_TITLE;

import java.io.File;
import java.util.ArrayList;
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
import android.widget.Button;
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
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.adapter.FollowAdapter;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Follows;
import com.ogqcorp.bgh.spirit.data.SimpleUser;
import com.ogqcorp.bgh.spirit.manager.FollowManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.commons.Log;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.JsonUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public class UsersSearchFragment extends Fragment implements SearchDialogFragment.OnQueryCallback, FollowManager.FollowListListener
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
		loadRecommendUsers();

		if (UserManager.getInstance().isGuest() == false)
		{
			FollowManager.getInstance().registerFollowListener(this);
		}
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

		if (UserManager.getInstance().isGuest() == false)
		{
			FollowManager.getInstance().unregisterFollowListener(this);
		}
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

	private synchronized List<SimpleUser> loadHistoryFromFile()
	{
		final File dataFile = new File(getActivity().getFilesDir(), "/" + SEARCH_NAME);

		List<SimpleUser> items;

		/** @formatter:x **/
		try
		{
			items = JsonUtils.OBJECT_MAPPER.readValue(dataFile, JsonUtils.OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, SimpleUser.class));
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
		String url = UrlFactory.searchUsers();
		m_request = Requests.requestByPost(url, ParamFactory.search(query), Follows.class, listener, errorListener);
	}

	private void loadRecommendUsers()
	{
		if (hasRecommendUsers() == true)
			return;

		try
		{
			String url = UrlFactory.featuredCreator();

			Requests.requestByGet(url, Follows.class, new Response.Listener<Follows>()
			{
				@Override
				public void onResponse(Follows response)
				{
					try
					{
						if (response == null)
							return;

						m_recommendList = response.getFollowsList();
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

	private boolean hasRecommendUsers()
	{
		if (m_recommendList == null || m_recommendList.isEmpty() == true)
			return false;

		return true;
	}

	private boolean hasTags()
	{
		if (m_userList == null || m_userList.isEmpty() == true)
			return false;

		return true;
	}

	private void updateAdapter()
	{
		try
		{
			if (m_userList != null)
			{
				m_userList.clear();
			}
			m_mergedAdapter.clear();

			if (TextUtils.isEmpty(m_query) == false)
			{
				if (hasResultUsers() == false)
				{
					m_mergedAdapter.add(getLayoutInflater(), R.layout.item_search_user_empty);
					addRecommendUser();
				}
				else
				{
					addResultUser();
				}
			}
			else
			{
				addHistoryUser();
				addRecommendUser();
			}
			m_mergedAdapter.add(m_userAdapter);
		}
		catch (Exception e)
		{

		}
	}

	private void addHistoryUser()
	{
		if (hasHistoryUsers() == false)
			return;

		SimpleUser split = new SimpleUser();
		split.setUsername(UUID_SPLIT_TITLE);
		split.setName(getString(R.string.search_history_title));

		if (hasTags() == false)
		{
			m_userList = new ArrayList<>();
		}

		m_userList.add(split);
		m_userList.addAll(m_filteredHistory);
	}

	private void addRecommendUser()
	{
		if (hasRecommendUsers() == false)
			return;

		SimpleUser split = new SimpleUser();
		split.setUsername(UUID_SPLIT_TITLE);
		split.setName(getString(R.string.popular_creator_title));

		if (hasTags() == false)
		{
			m_userList = new ArrayList<>();
		}

		m_userList.add(split);
		m_userList.addAll(m_recommendList);
	}

	private void addResultUser()
	{
		if (hasResultUsers() == false)
			return;

		SimpleUser split = new SimpleUser();
		split.setUsername(UUID_SPLIT_TITLE);
		split.setName(getString(R.string.search_results));

		if (hasTags() == false)
		{
			m_userList = new ArrayList<>();
		}

		m_userList.add(split);
		m_userList.addAll(m_filteredResults);
	}

	private void removeHistoryItem(SimpleUser item)
	{
		m_historyOriginal.remove(item);
		saveHistoryToFile();

		m_filteredHistory.remove(item);
		updateAdapter();
	}

	private boolean showHistoryRemoveDialog(final SimpleUser item)
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

	public List<SimpleUser> filterItemListByTag(List<SimpleUser> itemList, String filterTag)
	{
		List<SimpleUser> tempTags = new ArrayList<>();

		for (SimpleUser tag : itemList)
		{
			String tagString = tag.getName();
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

	private void orderByExactQueryToFirst(List<SimpleUser> tagList, String query)
	{
		for (SimpleUser user : tagList)
		{
			if (query.equals(user.getUsername()) == true)
			{
				tagList.remove(user);
				tagList.add(0, user);
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

	private Response.Listener<Follows> listener = new Response.Listener<Follows>()
	{
		@Override
		public void onResponse(Follows data)
		{
			if (FragmentUtils.isDestroyed(UsersSearchFragment.this) == true) return;

			if (data == null) return;

			List<SimpleUser> itemList = data.getFollowsList();
			//itemList.removeAll(m_filteredHistory);

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
			if (FragmentUtils.isDestroyed(UsersSearchFragment.this) == true) return;

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

	protected void reorderNetworkResponseData(List<SimpleUser> users, String query)
	{
		orderByExactQueryToFirst(users, query);
	}

	protected void onClickUser(SimpleUser user)
	{
		m_historyOriginal.remove(user);
		m_historyOriginal.add(0, user);

		if (m_historyOriginal.size() > MAX_RECENT_TAGS_SIZE)
		{
			m_historyOriginal.remove(m_historyOriginal.size() - 1);
		}

		saveHistoryToFile();

		String username = user.getUsername();
		AnalyticsManager.getInstance().eventSearchUser(getContext(), user.getUsername());
		AnalyticsManager.getInstance().searchEvent(getContext(), "Search2", "RecomCreator_Search2");

		AnalyticsManager.getInstance().eventUserActionProfile(getContext(), "SEARCH");
		AnalyticsManager.getInstance().eventUserTrackingProfile(getContext(), username);

		if (UserManager.getInstance().isMe(username) == false)
		{
			AnalyticsManager.getInstance().eventUserActionOtherProfile(getContext(), "SEARCH");
		}

		String userInfoUrl = UrlFactory.usersInfo(username);
		Fragment fragment = UserInfoFragment.newInstance(userInfoUrl);
		AbsMainActivity.getTabStackHelper(getActivity()).showFragment(fragment);
	}

	@CalledByReflection
	private void onClickFollow(final View view, SimpleUser user)
	{
		final Button button = (Button) view;
		button.setText("...");

		if (button.isSelected() == false)
		{
			AnalyticsManager.getInstance().searchEvent(getContext(), "Search2", "FollowCreator_Search2");
		}

		final FollowManager.OnFollowCallback callback = new FollowManager.OnFollowCallback()
		{
			@Override
			public void onIsFollowing(SimpleUser user, boolean isFollower)
			{
				if (FragmentUtils.isDestroyed(UsersSearchFragment.this) == true) return;

				button.setSelected(isFollower);
				button.setText(isFollower ? R.string.userinfo_following : R.string.userinfo_follow);

				if (UserManager.getInstance().isGuest() == true)
				{
					AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "SEARCH_USER_FALLOW");
					getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_FOLLOW));
				}
				else
				{
					if (isFollower == true)
					{
						AnalyticsManager.getInstance().eventUserActionFollow(getContext(), "SEARCH_USER");
					}
				}
			}
		};

		FollowManager.getInstance().toggleFollow(user, callback);
	}

	private boolean hasHistoryUsers()
	{
		if (m_filteredHistory == null || m_filteredHistory.isEmpty() == true)
			return false;

		return true;
	}

	private boolean hasResultUsers()
	{
		if (m_filteredResults == null || m_filteredResults.isEmpty() == true)
			return false;

		return true;
	}

	//========================================================================
	// Adapter
	//========================================================================

	private FollowAdapter m_userAdapter = new FollowAdapter()
	{
		@Override
		protected SimpleUser getItem(int position)
		{
			return m_userList.get(position);
		}

		@Override
		protected void onClickUser(View view, SimpleUser user)
		{
			UsersSearchFragment.this.onClickUser(user);
		}

		@Override
		protected boolean onLongClickUser(View view, SimpleUser user)
		{
			return showHistoryRemoveDialog(user);
		}

		@Override
		protected void onClickFollow(View view, SimpleUser user)
		{
			UsersSearchFragment.this.onClickFollow(view, user);
		}

		@Override
		public int getItemCount()
		{
			if (hasTags() == false)
				return 0;

			return m_userList.size();
		}

		@Override
		protected void modifyView(final ViewHolder viewHolder, final SimpleUser user)
		{
			final TextView name = viewHolder.getView().findViewById(R.id.name);
			final TextView username = viewHolder.getView().findViewById(R.id.username);

			colorizeTextView(name, user.getName(), m_query, Color.RED);
			colorizeTextView(username, "@" + user.getUsername(), m_query, Color.RED);
		}
	};

	//========================================================================
	// FollowListListener
	//========================================================================

	@Override
	public void onSuccess()
	{
		if (FragmentUtils.isDestroyed(UsersSearchFragment.this) == true) return;

		m_mergedAdapter.notifyDataSetChanged();
	}

	@Override
	public void onFail(Exception e)
	{
		if (FragmentUtils.isDestroyed(UsersSearchFragment.this) == true) return;

		ToastUtils.makeErrorToast(getActivity(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
	}

	//========================================================================
	// Constructor
	//========================================================================

	public static Fragment newInstance()
	{
		return new UsersSearchFragment();
	}

	//========================================================================
	// Constants
	//========================================================================

	private static final String SEARCH_NAME = "users";

	private static final int MAX_RECENT_TAGS_SIZE = 5;
	private static final int MIN_QUERY_LENGTH = 3;
	private static final int MIN_HANGUL_QUERY_LENGTH = 1;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(android.R.id.list) RecyclerView m_resultList;

	protected Request m_request;

	private String m_query = "";
	private List<SimpleUser> m_historyOriginal;
	private List<SimpleUser> m_filteredHistory;
	private List<SimpleUser> m_filteredResults;
	private List<SimpleUser> m_recommendList;
	private List<SimpleUser> m_userList;

	protected MergeRecyclerAdapter m_mergedAdapter;

	private GridLayoutManager m_layout;

	private Unbinder m_unbinder;
}
