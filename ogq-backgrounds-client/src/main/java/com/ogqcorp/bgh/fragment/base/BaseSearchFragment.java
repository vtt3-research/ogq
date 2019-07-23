package com.ogqcorp.bgh.fragment.base;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.ogqcorp.bgh.adapter.SearchAdapter;
import com.ogqcorp.bgh.fragment.SearchDialogFragment;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.commons.Log;
import com.ogqcorp.commons.MergeAdapter;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.JsonUtils;
import com.ogqcorp.commons.utils.ListenerUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public abstract class BaseSearchFragment<ITEM extends Parcelable, DATA> extends Fragment implements SearchDialogFragment.OnQueryCallback
{
	//========================================================================
	// Public Methods
	//========================================================================

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_search_dialog, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		m_historyOriginal = loadHistoryFromFile();

		m_filteredHistory = new ArrayList<>(m_historyOriginal);
		m_filteredResults = new ArrayList<>();

		m_mergedAdapter = new MergeAdapter();
		m_mergedAdapter.addView(getActivity().getLayoutInflater(), R.layout.item_search_progress);
		m_mergedAdapter.addView(getActivity().getLayoutInflater(), R.layout.item_search_user_empty);
		m_mergedAdapter.addAdapter(m_historyAdapter);
		m_mergedAdapter.addAdapter(m_resultsAdapter);
		//m_mergedAdapter.addView(getActivity().getLayoutInflater(), R.layout.empty_search_layout);
		m_resultList.setAdapter(m_mergedAdapter);

		showFragmentEmpty();
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
		//if (getUserVisibleHint() == false) return;

		m_query = query;

		if (query.length() > 0)
		{
			m_filteredHistory.clear();
			m_filteredHistory.addAll(filterItemListByTag(m_historyOriginal, query));
			m_mergedAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNetworkSearch(String query)
	{
		//if (getUserVisibleHint() == false) return;

		m_query = query;

		if (checkQueryLength(query) == true)
		{
			requestSearchQuery(query);
			showProgress(true);
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

		showEmpty(false);
		showProgress(false);
		showFragmentEmpty();

		if (m_request != null)
		{
			m_request.cancel();
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private synchronized List<ITEM> loadHistoryFromFile()
	{
		final File dataFile = new File(getActivity().getFilesDir(), "/" + getName());

		List<ITEM> items;

		/** @formatter:x **/
		try
		{
			items = JsonUtils.OBJECT_MAPPER.readValue(dataFile, JsonUtils.OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, getItemType()));
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
			final File dataFile = new File(getActivity().getFilesDir(), "/" + getName());
			JsonUtils.OBJECT_MAPPER.writeValue(dataFile, m_historyOriginal);
		}
		catch (Exception e)
		{
			Log.e(e);
		}
	}

	private void requestSearchQuery(final String query)
	{
		m_request = Requests.requestByPost(getSearchUrl(), ParamFactory.search(query), getDataType(), listener, errorListener);
	}

	private void removeHistoryItem(ITEM item)
	{
		m_historyOriginal.remove(item);
		saveHistoryToFile();

		m_filteredHistory.remove(item);

		showFragmentEmpty();
		m_mergedAdapter.notifyDataSetChanged();
	}

	private boolean showHistoryRemoveDialog(final ITEM item)
	{
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

		if (isHangul(query) == true && length >= getMinimumHangulQueryLength())
		{
			return true;
		}

		if (length >= getMinimumQueryLength())
		{
			return true;
		}

		return false;
	}

	private boolean isHangul(String text)
	{
		return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
	}

	//========================================================================
	// Public Methods
	//========================================================================

	public List<ITEM> filterItemListByTag(List<ITEM> itemList, String filterTag)
	{
		return itemList;
	}

	public List<ITEM> filterTagListByCount(List<ITEM> itemList, int i)
	{
		return itemList;
	}

	@SuppressWarnings("unchecked")
	@CalledByReflection
	public void onClickItem(View view)
	{
		final ITEM tag = (ITEM) view.getTag(getItemResId());

		m_historyOriginal.remove(tag);
		m_historyOriginal.add(0, tag);

		if (m_historyOriginal.size() > MAX_RECENT_TAGS_SIZE)
		{
			m_historyOriginal.remove(m_historyOriginal.size() - 1);
		}

		saveHistoryToFile();

		onClickItem(tag);
	}

	@SuppressWarnings("unchecked")
	@CalledByReflection
	public boolean onLongClickItem(View view)
	{
		final ITEM tag = (ITEM) view.getTag(getItemResId());
		showHistoryRemoveDialog(tag);
		return true;
	}

	//========================================================================
	// Protected Methods
	//========================================================================

	protected void onClickItem(ITEM item)
	{
		/*if (getParentFragment() instanceof DialogFragment)
		{
			DialogFragment dialogFragment = (DialogFragment) getParentFragment();
			dialogFragment.dismiss();
		}*/
	}

	//========================================================================
	// Listener
	//========================================================================

	private Response.Listener<DATA> listener = new Response.Listener<DATA>()
	{
		@Override
		public void onResponse(DATA data)
		{
			if (FragmentUtils.isDestroyed(BaseSearchFragment.this) == true) return;

			showProgress(false);

			if (data == null) return;

			List<ITEM> itemList = getItemList(data);
			itemList.removeAll(m_filteredHistory);
			itemList = filterTagListByCount(itemList, 0);

			reorderNetworkResponseData(itemList, m_query);

			m_filteredResults.clear();
			m_filteredResults.addAll(itemList);

			showEmpty(itemList.size() == 0);
			showFragmentEmpty();

			m_mergedAdapter.notifyDataSetChanged();
		}
	};

	private Response.ErrorListener errorListener = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError error)
		{
			if (FragmentUtils.isDestroyed(BaseSearchFragment.this) == true) return;

			showProgress(false);
			ToastUtils.makeErrorToast(getActivity(), Toast.LENGTH_SHORT, error.toString()).show();
		}
	};

	//========================================================================
	// Adapter
	//========================================================================

	private SearchAdapter m_historyAdapter = new SearchAdapter()
	{
		@Override
		public int getCount()
		{
			return m_filteredHistory.size();
		}

		@Override
		public Object getItem(int position)
		{
			return m_filteredHistory.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return getView(getActivity(), getActivity().getLayoutInflater(), position, convertView, parent);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void initAdapterView(Context context, ViewHolder viewHolder, Object item)
		{
			BaseSearchFragment.this.initAdapterView(context, viewHolder, (ITEM) item, m_query);

			ListenerUtils.setOnClickListener(viewHolder.getView(), BaseSearchFragment.this, "onClickItem");
			ListenerUtils.setOnLongClickListener(viewHolder.getView(), BaseSearchFragment.this, "onLongClickItem");
		}

		@Override
		protected int getItemResId()
		{
			return BaseSearchFragment.this.getItemResId();
		}
	};

	private SearchAdapter m_resultsAdapter = new SearchAdapter()
	{
		@Override
		public int getCount()
		{
			return m_filteredResults.size();
		}

		@Override
		public Object getItem(int position)
		{
			return m_filteredResults.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			return getView(getActivity(), getActivity().getLayoutInflater(), position, convertView, parent);
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void initAdapterView(Context context, ViewHolder viewHolder, Object item)
		{
			BaseSearchFragment.this.initAdapterView(context, viewHolder, (ITEM) item, m_query);

			ListenerUtils.setOnClickListener(viewHolder.getView(), BaseSearchFragment.this, "onClickItem");
		}

		@Override
		protected int getItemResId()
		{
			return BaseSearchFragment.this.getItemResId();
		}
	};

	//========================================================================
	// Protected Methods
	//========================================================================

	protected void showProgress(final boolean show)
	{
		m_mergedAdapter.findViewTraversal(R.id.progress).setVisibility(show ? View.VISIBLE : View.GONE);
		m_mergedAdapter.notifyDataSetChanged();

		showEmpty(false);
		showFragmentEmpty();
	}

	protected void showEmpty(final boolean show)
	{
		m_mergedAdapter.findViewTraversal(R.id.empty).setVisibility(show ? View.VISIBLE : View.GONE);
		m_mergedAdapter.notifyDataSetChanged();
	}

	protected void showFragmentEmpty()
	{
		final String emptyString = getActivity().getString(R.string.empty_search, getTitleString());
		m_emptySearchTextview.setText(emptyString);

		boolean show = false;

		if ((m_historyOriginal != null && m_historyOriginal.size() <= 0) &&
				(m_filteredHistory != null && m_filteredHistory.size() <= 0) &&
				(m_filteredResults != null && m_filteredResults.size() <= 0) &&
				(m_mergedAdapter.findViewTraversal(R.id.progress).getVisibility() == View.GONE) &&
				(m_mergedAdapter.findViewTraversal(R.id.empty).getVisibility() == View.GONE))
		{
			show = true;
		}
		else
		{
			show = false;
		}

		m_emptySearchLayout.setVisibility(show ? View.VISIBLE : View.GONE);
		m_mergedAdapter.notifyDataSetChanged();
	}
	//========================================================================
	// Protected Abstract Methods
	//========================================================================

	protected abstract Class<ITEM> getItemType();

	protected abstract Class<DATA> getDataType();

	protected abstract String getSearchUrl();

	protected abstract String getName();

	protected abstract String getTitleString();

	protected abstract String getItemDataUrl(ITEM item);

	protected abstract List<ITEM> getItemList(DATA data);

	protected abstract int getItemResId();

	protected abstract long getMinimumHangulQueryLength();

	protected abstract long getMinimumQueryLength();

	protected abstract void initAdapterView(Context context, SearchAdapter.ViewHolder viewHolder, ITEM item, String query);

	protected abstract void reorderNetworkResponseData(List<ITEM> items, String query);

	//=========================================================================
	// Constants
	//=========================================================================

	private static final int MAX_RECENT_TAGS_SIZE = 20;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(android.R.id.list) ListView m_resultList;
	@BindView(R.id.empty_search) LinearLayout m_emptySearchLayout;
	@BindView(R.id.empty_text) TextView m_emptySearchTextview;

	protected Request m_request;

	private String m_query = "";
	private List<ITEM> m_historyOriginal;
	private List<ITEM> m_filteredHistory;
	private List<ITEM> m_filteredResults;
	protected MergeAdapter m_mergedAdapter;

	private Unbinder m_unbinder;
}
