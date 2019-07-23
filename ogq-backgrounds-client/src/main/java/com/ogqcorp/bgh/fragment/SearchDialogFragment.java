package com.ogqcorp.bgh.fragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;

public class SearchDialogFragment extends DialogFragment implements Toolbar.OnMenuItemClickListener
{
	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public int getTheme()
	{
		return R.style.BG_Theme_Dialog_Search;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		final int inputMode1 = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
		final int inputMode2 = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;

		getActivity().getWindow().setSoftInputMode(inputMode1 | inputMode2);

		return inflater.inflate(R.layout.fragment_search_dialog_base, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		m_toolbar.inflateMenu(R.menu.fragment_search);
		m_toolbar.setOnMenuItemClickListener(this);
		m_toolbar.setNavigationIcon(R.drawable.ic_back);
		m_toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dismiss();
			}
		});

		m_viewPager.setAdapter(new SearchFragmentAdapter(getChildFragmentManager()));
		m_viewPager.setCurrentItem(TAB_SEARCH_TAG);

		m_queryText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_SEARCH)
				{
					final String query = m_queryText.getText().toString();
					m_queryText.setText(query);
					m_queryText.setSelection(query.length());
					return true;
				}
				return false;
			}
		});

		m_tabLayout.setupWithViewPager(m_viewPager);
		m_tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
		{
			@Override
			public void onTabSelected(TabLayout.Tab tab)
			{
				if (tab.getPosition() == TAB_SEARCH_USER)
				{
					AnalyticsManager.getInstance().searchEvent(getContext(), "Search2", "Creator_Search2");
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab)
			{
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab)
			{
			}
		});

		registerQueryTextChanges();
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem)
	{
		if (menuItem.getItemId() == R.id.action_clear_query)
		{
			m_queryText.setText("");
		}

		onClearQuery();

		return true;
	}

	@Override
	public void onDestroyView()
	{
		if (m_querySubscription != null)
		{
			m_querySubscription.unsubscribe();
		}

		m_unbinder.unbind();

		super.onDestroyView();
	}

	//========================================================================
	// Public Methods
	//========================================================================

	@OnTextChanged(R.id.query)
	public void onQueryTextChanged(CharSequence query)
	{
		try
		{
			String q = query.toString().toLowerCase();

			if (q.length() > 0)
			{
				m_toolbar.getMenu().findItem(R.id.action_clear_query).setVisible(true);
				onHistorySearch(q);
			}
			else
			{
				m_toolbar.getMenu().findItem(R.id.action_clear_query).setVisible(false);
				onClearQuery();
			}
		} catch (Exception e)
		{

		}
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void registerQueryTextChanges()
	{
		final Action1<CharSequence> searchKeyword = new Action1<CharSequence>()
		{
			@Override
			public void call(final CharSequence query)
			{
				onRequestSearch(query.toString().toLowerCase());
			}
		};

		m_querySubscription = RxTextView.textChanges(m_queryText)
				.debounce(INPUT_DEBOUNCE_TIME, TimeUnit.MILLISECONDS, Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(searchKeyword);
	}

	private void onHistorySearch(String query)
	{
		final List<Fragment> fragmentList = getChildFragmentManager().getFragments();

		if (fragmentList != null)
		{
			for (Fragment fragment : fragmentList)
			{
				if (fragment instanceof OnQueryCallback)
				{
					OnQueryCallback onQueryCallback = (OnQueryCallback) fragment;
					onQueryCallback.onHistorySearch(query);
				}
			}
		}
	}

	private void onRequestSearch(String query)
	{
		final List<Fragment> fragmentList = getChildFragmentManager().getFragments();

		if (fragmentList != null)
		{
			for (Fragment fragment : getChildFragmentManager().getFragments())
			{
				if (fragment instanceof OnQueryCallback)
				{
					OnQueryCallback onQueryCallback = (OnQueryCallback) fragment;
					onQueryCallback.onNetworkSearch(query);
				}
			}
		}
	}

	private void onClearQuery()
	{
		final List<Fragment> fragmentList = getChildFragmentManager().getFragments();

		if (fragmentList != null)
		{
			for (Fragment fragment : getChildFragmentManager().getFragments())
			{
				if (fragment instanceof OnQueryCallback)
				{
					OnQueryCallback onQueryCallback = (OnQueryCallback) fragment;
					onQueryCallback.onClearQuery();
				}
			}
		}
	}

	//========================================================================
	// PagerAdapter
	//========================================================================

	class SearchFragmentAdapter extends FragmentStatePagerAdapter
	{
		public SearchFragmentAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int position)
		{

			switch (position)
			{
				case TAB_SEARCH_TAG:
					return TagsSearchFragment.newInstance();

				case TAB_SEARCH_USER:
					return UsersSearchFragment.newInstance();
			}
			return null;
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return getResources().getString(position == 0 ? R.string.search_tab_tags : R.string.search_tab_ceator);
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static SearchDialogFragment start(FragmentManager fragmentManager)
	{
		try
		{
			final SearchDialogFragment fragment = new SearchDialogFragment();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			ft.add(fragment, TAG_FRAGMENT);
			ft.commitAllowingStateLoss();
			return fragment;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	//========================================================================
	// Query Callback
	//========================================================================

	public interface OnQueryCallback
	{
		void onHistorySearch(String query);

		void onNetworkSearch(String query);

		void onClearQuery();
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String TAG_FRAGMENT = SearchDialogFragment.class.getSimpleName();
	private static final long INPUT_DEBOUNCE_TIME = 700;

	public static final int TAB_SEARCH_TAG = 0;
	public static final int TAB_SEARCH_USER = 1;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.query) EditText m_queryText;
	@BindView(R.id.tabs) TabLayout m_tabLayout;
	@BindView(R.id.toolbar) Toolbar m_toolbar;
	@BindView(R.id.view_pager) ViewPager m_viewPager;

	private Unbinder m_unbinder;
	private Subscription m_querySubscription;
}
