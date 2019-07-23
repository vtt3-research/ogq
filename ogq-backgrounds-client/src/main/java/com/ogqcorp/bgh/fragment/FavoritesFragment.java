package com.ogqcorp.bgh.fragment;

import java.util.ArrayList;
import java.util.List;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.adapter.BackgroundsAdapter;
import com.ogqcorp.bgh.ads.IntegrateNativeAd;
import com.ogqcorp.bgh.coverslider.system.CoverFLManagerCompatUtils;
import com.ogqcorp.bgh.fragment.base.BaseSelectionToolbarFragment;
import com.ogqcorp.bgh.model.BackgroundsModel;
import com.ogqcorp.bgh.model.BackgroundsModelData;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.manager.FavoritesManager;
import com.ogqcorp.bgh.system.GridLayoutManagerEx;
import com.ogqcorp.bgh.system.ViewTransitionHelper;
import com.ogqcorp.commons.Log;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.utils.DeviceUtils;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public final class FavoritesFragment extends BaseSelectionToolbarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (FavoritesManager.getInstance().isDirty() == true)
		{
			FavoritesManager.getInstance().setDirty(false);

			BackgroundsModel.getInstance().release(this);
		}

		m_data = BackgroundsModel.getInstance().get(this, new BaseModel.DataCreator<BackgroundsModelData>()
		{
			@Override
			public BackgroundsModelData newInstance()
			{
				final List<Background> backgroundsList = FavoritesManager.getInstance().getBackgroundsList();

				return new BackgroundsModelData(backgroundsList);
			}
		});

		BackgroundsModel.getInstance().update(m_data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_favorites, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		m_swipeRefreshLayout.setEnabled(false);

		m_layout = new GridLayoutManagerEx(getActivity(), getSpanCount()/**/);
		m_layout.setSpanSizeLookup(m_spanSizeLookup);

		m_listView.setLayoutManager(m_layout);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);

		m_listView.setAdapter(m_mergeAdapter);

		showProgress(false);

		keepContext();
		updateEmptyList();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_favorites, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int menuId = item.getItemId();

		switch (menuId)
		{
			case R.id.action_remove_all:
				onActionRemoveAll();
				return true;
		}

		return false;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
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
	public void onRelease()
	{
		BackgroundsModel.getInstance().release(this);
	}

	@Override
	protected void onInitActionBar()
	{
		super.onInitActionBar();

		getToolbar().setTitle(R.string.top_favorites);
	}

	@Override
	protected boolean isOverlayActionBar()
	{
		return false;
	}

	@Override
	protected int getFirstVisiblePosition()
	{
		return m_layout.findFirstVisibleItemPosition();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		/** COVER */
		if (CoverFLManagerCompatUtils.getInstance().isCoverLikeUpdated())
		{
			if (m_data.isLoaded() == false || m_data == null || (m_data != null && m_data.getBackgroundsList().size() < 1))
			{
				return;
			}

			CoverFLManagerCompatUtils.getInstance().coverSyncLike(m_data.getBackgroundsList());
			if (FavoritesManager.getInstance().isDirty() == true)
			{
				FavoritesManager.getInstance().setDirty(false);

				BackgroundsModel.getInstance().release(this);
			}
			m_data = BackgroundsModel.getInstance().get(this, new BaseModel.DataCreator<BackgroundsModelData>()
			{
				@Override
				public BackgroundsModelData newInstance()
				{
					final List<Background> backgroundsList = FavoritesManager.getInstance().getBackgroundsList();

					return new BackgroundsModelData(backgroundsList);
				}
			});

			BackgroundsModel.getInstance().update(m_data);
			getActivity().runOnUiThread(new Runnable() {
				public void run()
				{
					m_mergeAdapter = new MergeRecyclerAdapter();
					m_mergeAdapter.add(m_adapter);
					m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);

					m_listView.setAdapter(m_mergeAdapter);

					showProgress(false);

					keepContext();
					updateEmptyList();
				}
			});
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		final Fragment fragment = new FavoritesFragment();
		return BaseModel.wrap(fragment);
	}

	public void onActionRemoveAll()
	{
		final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback()
		{
			@Override
			public void onPositive(MaterialDialog dialog)
			{
				FavoritesManager.getInstance().clear();

				m_data.getBackgroundsList().clear();
				m_adapter.notifyDataSetChanged();

				updateEmptyList();
			}
		};

		new MaterialDialog.Builder(getActivity())
				.title(R.string.favorites_remove_title)
				.content(R.string.favorites_remove_content_all)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.callback(callback)
				.show();
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void onClickBackground(View view, Background background)
	{
		final int position = m_listView.getChildPosition(view);
		m_data.setIndex(position);

		final View heroView = view.findViewById(R.id.image);
		ViewTransitionHelper.getInstance().setHeroView(heroView);

		onOpenBackground(this);
	}

	@Override
	protected void notifySelectionDataChanged()
	{
		if (m_mergeAdapter != null)
		{
			m_mergeAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected List<Background> getBackgroundsList()
	{
		return m_data.getBackgroundsList();
	}

	@Override
	protected void onActionRemoveSelections(final List<Background> selections)
	{
		final MaterialDialog.ButtonCallback callback = new MaterialDialog.ButtonCallback()
		{
			@Override
			public void onPositive(MaterialDialog dialog)
			{
				FavoritesManager.getInstance().getBackgroundsList().removeAll(selections);
				onRemoveSelections();
				setEnableSelectionToolbar(false, true);
			}
		};

		new MaterialDialog.Builder(getActivity())
				.title(R.string.favorites_remove_title)
				.content(R.string.favorites_remove_content_selected)
				.positiveText(android.R.string.ok)
				.negativeText(android.R.string.cancel)
				.callback(callback)
				.show();
	}

	@Override
	protected void onRemoveSelections()
	{
		super.onRemoveSelections();
		updateEmptyList();
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private int getSpanCount()
	{
		final Context context = getActivity();

		//return DeviceUtils.isLandscape(context) == false ? 2 : 4;
		return DeviceUtils.isTablet(context) == true ? 4 : 2;
	}

	private void showProgress(boolean isShow)
	{
		try
		{
			final View view = m_mergeAdapter.findViewById(R.id.progress);

			if (isShow == true)
			{
				view.setVisibility(View.VISIBLE);
			}
			else
			{
				view.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			Log.e(e);
		}
	}

	private void keepContext()
	{
		final int index = m_data.getIndex();

		if (index != BackgroundsModelData.INVALID_INDEX)
		{
			m_listView.post(new Runnable()
			{
				@Override
				public void run()
				{
					if (m_listView != null)
					{
						m_listView.scrollToPosition(index);
					}
				}
			});

			m_data.setIndex(BackgroundsModelData.INVALID_INDEX);
		}
	}

	private void updateEmptyList()
	{
		final View emptyList = getView().findViewById(R.id.empty_list);

		if (m_adapter == null || m_adapter.getItemCount() == 0)
		{
			emptyList.setVisibility(View.VISIBLE);
			setMenuVisibility(false);
		}
		else
		{
			emptyList.setVisibility(View.GONE);
			setMenuVisibility(true);
		}
	}

	//=========================================================================
	// Span Size Lookup
	//=========================================================================

	private GridLayoutManager.SpanSizeLookup m_spanSizeLookup = new GridLayoutManager.SpanSizeLookup()
	{
		@Override
		public int getSpanSize(int position)
		{
			final int viewType = m_mergeAdapter.getItemViewType(position);

			if (StaticViewAdapter.isStaticView(viewType) == true) return m_layout.getSpanCount();

			return 1;
		}
	};

	//=========================================================================
	// Adapter
	//=========================================================================

	private BackgroundsAdapter m_adapter = new BackgroundsAdapter()
	{
		@Override
		public int getItemViewType(int position)
		{
			return R.layout.item_background;
		}

		@Override
		public int getItemCount()
		{
			if (m_data.isLoaded() == false) return 0;
			return m_data.getBackgroundsList().size();
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			return onCreateViewHolder(getLayoutInflater(), parent, viewType);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, final int position)
		{
			onBindViewHolder(getActivity(), getBackground(position), holder, position);

			holder.itemView.findViewById(R.id.check).setVisibility(isSelected(getBackground(position))
					? View.VISIBLE
					: View.GONE);

			holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					onLongClickBackground(v, getBackground(position));
					return true;
				}
			});
		}

		@Override
		protected void onClickBackground(View view, Background background)
		{
			if (m_isSelectionMode == false)
			{
				FavoritesFragment.this.onClickBackground(view, background);
			}
			else
			{
				FavoritesFragment.this.onSelectBackground(view, background);
			}
		}

		@CalledByReflection
		public void onLongClickBackground(View view, Background background)
		{
			if (m_isSelectionMode == false)
			{
				setEnableSelectionToolbar(true, true);
				onSelectBackground(view, background);
			}
		}

		@Override
		protected void onClickAdFree()
		{
			//TODO:
		}

		@Override
		protected ArrayList<IntegrateNativeAd> getBackgroundNativeAdsList()
		{
			return null;
		}

		private Background getBackground(int position)
		{
			if (m_data.isLoaded() == false) return null;
			return m_data.getBackgroundsList().get(position);
		}
	};

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout m_swipeRefreshLayout;

	private Unbinder m_unbinder;
	private GridLayoutManager m_layout;
	private MergeRecyclerAdapter m_mergeAdapter;
	private BackgroundsModelData m_data;
}
