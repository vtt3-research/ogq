package com.ogqcorp.bgh.fragment.explore;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.fragment.WepickVoteFragment;
import com.ogqcorp.bgh.fragment.base.BaseFragment;
import com.ogqcorp.bgh.fragment.tag.SearchFragment;
import com.ogqcorp.bgh.gallery.GalleryCardsFragment;
import com.ogqcorp.bgh.gallery.GalleryEditCoverFragment;
import com.ogqcorp.bgh.gallery.GallerySaveUtil;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Gallery;
import com.ogqcorp.bgh.spirit.data.Wepicks;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.FragmentFactory;
import com.ogqcorp.bgh.system.IntentLauncher;
import com.ogqcorp.bgh.system.PopupManager;
import com.ogqcorp.bgh.system.RxBus;
import com.ogqcorp.bgh.system.TopBannerManager;
import com.ogqcorp.bgh.view.SpinnerEx;
import com.ogqcorp.commons.DisplayManager;
import com.ogqcorp.commons.PreventDoubleTap;
import com.ogqcorp.commons.SizeDeterminer;
import com.ogqcorp.commons.SizeReadyCallback;
import com.ogqcorp.commons.utils.FragmentUtils;

public final class ExploreFragment extends BaseRecyclerFragmentEx2
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public ExploreFragment()
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
		return inflater.inflate(R.layout.fragment_explore, container, false);
	}

	@Override
	public void onViewCreated(View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
		{
			m_fragmentInfos = savedInstanceState.getSparseParcelableArray(KEY_FRAGMENT_INFOS);
		}

		TabsAdapter tabsAdapter = new TabsAdapter(getChildFragmentManager());
		m_viewPager.setAdapter(tabsAdapter);
		m_viewPager.setCurrentItem(TAB_INDEX_RECENTS);
		m_viewPager.setOffscreenPageLimit(1);

		m_tabs.setupWithViewPager(m_viewPager);
		m_tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
		{
			@Override
			public void onTabSelected(TabLayout.Tab tab)
			{
				try
				{
					if (tab.getPosition() == TAB_INDEX_RECENTS)
					{
						if (TopBannerManager.getInstance().isOpenBanner() == true)
							TopBannerManager.getInstance().stats("SEEN");
					}
				}
				catch (Exception ignored)
				{
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab)
			{
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab)
			{
				if (tab.getPosition() != TAB_INDEX_POPULAR)
				{
					onScrollTop();
				}
			}
		});

		if (TopBannerManager.getInstance().isOpenBanner() == true)
			TopBannerManager.getInstance().stats("SEEN");

		m_viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
		{
			@Override
			public void onPageSelected(int position)
			{
				switch (position)
				{
					case TAB_INDEX_RECENTS:
						//case TAB_INDEX_GALLERY:
					case TAB_INDEX_WEPICK:
						showFabButton();
						break;
					default:
						hideFabButton();
						break;
				}
			}

			@Override
			public void onPageScrollStateChanged(int state)
			{
				float y = getToolbar().getTranslationY();

				if (y != 0)
				{
					showActionBarSlide(true, true);
				}
			}
		});

		checkNewWepick();

		TabLayout.Tab tab = m_tabs.getTabAt(TAB_INDEX_POPULAR);
		SpinnerEx spinnerEx = (SpinnerEx) LayoutInflater.from(getContext()).inflate(R.layout.item_tab_spinner, null);

		tab.setCustomView(null);
		tab.setCustomView(spinnerEx);
		initTabSpinner(spinnerEx);
		initFabButton();

		new SizeDeterminer(m_tabs).getSize(new SizeReadyCallback()
		{
			@Override
			public void onSizeReady(View view, int width, int height)
			{
				if (savedInstanceState == null)
				{
					m_tabs.setScrollPosition(TAB_INDEX_RECENTS, 0, true);
				}
			}
		}, true);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				PopupManager.getInstance().showPopup((AppCompatActivity) getActivity());
			}
		}, 700);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.fragment_explore, menu);
		menu.findItem(R.id.action_sign_in).setVisible(UserManager.getInstance().isGuest());
		getToolbar().setTitle(R.string.top_explore);
		onInitActionBar();

		showActionBarSlide(true, true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		final int menuId = item.getItemId();

		switch (menuId)
		{
			case R.id.action_sign_in:
				onActionSignIn();
				return true;
			case R.id.action_search:
				onActionSearch();
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
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putSparseParcelableArray(KEY_FRAGMENT_INFOS, m_fragmentInfos);
	}

	@Override
	public void onRelease()
	{
		try
		{
			for (int index = 0; index < m_fragmentInfos.size(); index++)
			{
				final FragmentInfo fragmentInfo = m_fragmentInfos.valueAt(index);

				final BaseFragment fragment = (BaseFragment) getFragment(fragmentInfo);
				fragment.onRelease();
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();

		/*
		 * 화면 이동 후 툴바가 깜박이던 현상 수정을 위해 넣은 코드
		 * 화면 이동 후 다시 돌아왔을때 onInitActionBar()가 8번을 타는 이슈 발견
		 * 따라서 elevation을 0으로 설정하는 코드를 onResume에서 미리 호출하여
		 * 깜박이던 현상 수정 완료 (해당 코드는 차후 개선된 방향 혹은 방법이 있을 경우 수정될 예정)
		 */
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			getToolbar().setElevation(0);
	}

	@Override
	protected void onInitActionBar()
	{
		setActionBarAlpha(isOverlayActionBar() ? 0 : 255);

		Toolbar toolbar = getToolbar();

		if (toolbar != null && isAdded() == true && getUserVisibleHint() == true)
		{
			int color = getResources().getColor(R.color.black);

			toolbar.setBackgroundResource(R.drawable.actionbar_bg);
			if (toolbar.getOverflowIcon() != null)
			{
				toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			toolbar.setElevation(0);
		}
	}

	@Override
	public void onScrollTop()
	{
		BaseFragment fragment = (BaseFragment) m_viewPager.getAdapter().instantiateItem(m_viewPager, m_viewPager.getCurrentItem());
		fragment.onScrollTop();
		showActionBarSlide(true, false);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);

		if (isVisibleToUser == true)
		{
			m_stopTabScroll = false;
			showActionBarSlide(true, false);
		} else
		{
			m_stopTabScroll = true;
		}
	}

	public boolean moveLandingScreen()
	{
		if (m_viewPager.getCurrentItem() == TAB_INDEX_RECENTS)
		{
			return false;
		} else
		{
			m_viewPager.setCurrentItem(TAB_INDEX_RECENTS);
			return true;
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public void setCurrentItem(int num, boolean smoothScroll)
	{
		m_viewPager.setCurrentItem(num, smoothScroll);
	}

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		return new ExploreFragment();
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	public void onContinuousWriteDialog()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_C) == false)
			return;

		final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
				.customView(R.layout.dialog_gallery_continuous_write, false)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.show();

		final ViewGroup customView = (ViewGroup) dialog.getCustomView();

		final Button btnNew = customView.findViewById(R.id.write_new);
		final Button btnContinue = customView.findViewById(R.id.write_continue);

		btnContinue.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Gallery gallery = GallerySaveUtil.readFromFile(getContext());
				Fragment fragment = GalleryCardsFragment.newInstance(gallery);
				AbsMainActivity.getTabStackHelper(ExploreFragment.this).showFragment(fragment);
				//GallerySaveUtil.remove(getContext());

				dialog.dismiss();
			}
		});

		btnNew.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Fragment fragment = GalleryEditCoverFragment.newInstance();
				AbsMainActivity.getTabStackHelper(ExploreFragment.this).showFragment(fragment);
				GallerySaveUtil.remove(getContext());

				dialog.dismiss();
			}
		});
	}

	private void onActionSignIn()
	{
		AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "TOOLBAR");
		IntentLauncher.startAuthSignInActivity(getActivity(), AuthActivity.SIGN_ACTION_EXPLOREPAGE);
	}

	private void onActionSearch()
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == true)
		{
			Fragment fragment = SearchFragment.newInstance();
			AbsMainActivity.getTabStackHelper(ExploreFragment.this).showFragment(fragment);
		}
	}

	private Fragment getFragment(int position, FragmentCallback callback)
	{
		final FragmentInfo fragmentInfo = m_fragmentInfos.get(position);

		if (fragmentInfo == null)
		{
			final Fragment fragment = callback.createFragment(position);

			m_fragmentInfos.put(position, new FragmentInfo(fragment));

			return fragment;
		}

		return getFragment(fragmentInfo);
	}

	private Fragment getFragment(FragmentInfo fragmentInfo)
	{
		return Fragment.instantiate(getActivity(), fragmentInfo.clazz, fragmentInfo.argument);
	}

	private void initTabSpinner(final SpinnerEx spinner)
	{
		final int eightDp = DisplayManager.getInstance().getPixelFromDp(getContext(), 8);
		final int sixTeenDp = DisplayManager.getInstance().getPixelFromDp(getContext(), 16);

		String dropDownItems[] = new String[]
				{
						getString(R.string.explore_popular_daily),
						getString(R.string.explore_popular_weekly),
						getString(R.string.explore_popular_monthly),
						getString(R.string.explore_popular_all),
				};

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner_dropdown, dropDownItems)
		{
			@NonNull
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				View spinnerText = super.getView(position, convertView, parent);
				spinnerText.setPadding(sixTeenDp, 0, eightDp, 0);
				return spinnerText;
			}
		};

		spinner.setAdapter(adapter);
		spinner.setSelection(PreferencesManager.getInstance().getPopularMode(getContext()), false);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				PreferencesManager.getInstance().setPopularMode(getContext(), position);
				RxBus.getInstance().onEvent(new BusPopularMode(position));
				spinner.requestFocus();

				showActionBarSlide(true, true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{
				spinner.requestFocus();
			}
		});

		View parent = (View) spinner.getParent();

		if (parent != null)
		{
			parent.setPadding(0, 0, eightDp, 0);
		}
	}

	private String getTabTitle(int index)
	{
		switch (index)
		{
			case 0:
				return getString(R.string.explore_recent);
			case 1:
				return getString(R.string.explore_premium);
			case 2:
				return getString(R.string.explore_wepick);
			case 3:
				return getString(R.string.explore_popular_daily);
			case 4:
				return getString(R.string.action_shuffle);
		}

		return null;
	}

	private void checkNewWepick()
	{
		if (UserManager.getInstance().isGuest() == true)
			return;

		try
		{
			Requests.authRequestByGet(UrlFactory.wepicksCurrent(), Wepicks.class, new Response.Listener<Wepicks>()
			{
				@Override
				public void onResponse(final Wepicks wepicks)
				{
					if (FragmentUtils.isDestroyed(ExploreFragment.this) == true) return;

					if (wepicks == null | wepicks.getWepickList() == null || wepicks.getWepickList().isEmpty() == true)
					{
						m_wepicks = null;
						m_hasNewWepick = false;
						return;
					} else
					{
						m_wepicks = wepicks;
						m_hasNewWepick = true;
					}

					int curTab = m_tabs.getSelectedTabPosition();
					if (curTab == TAB_INDEX_RECENTS || curTab == TAB_INDEX_WEPICK)
					{
						boolean isShowVUGuide = PreferencesManager.getInstance().isShowVideoUploadGuide(getContext());

						//	m_fabButton.setVisibility(View.VISIBLE);
						m_fabButton.show();

						if (isShowVUGuide == false)
						{
							final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_scale);
							m_fabButton.startAnimation(anim);
						}
					}
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					//Nothing
				}
			});
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void initFabButton()
	{
		if (UserManager.getInstance().isGuest() == true)
		{
			if (m_fabButton != null)
			{
				m_hasNewWepick = true;
				m_fabButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "WEPICK_FAB");
						getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_WEPICK));

						try
						{
							int curTab = m_tabs.getSelectedTabPosition();

							if (curTab == TAB_INDEX_RECENTS)
								AnalyticsManager.getInstance().RecentEvent(getContext(), "WePickFAB_Recent", "Guest");
							else if (curTab == TAB_INDEX_WEPICK)
								AnalyticsManager.getInstance().WepickEvent(getContext(), "WePickFAB_WePick", "Guest");
						}
						catch (Exception ignored)
						{
						}
					}
				});
				int curTab = m_tabs.getSelectedTabPosition();
				if (curTab == TAB_INDEX_RECENTS || curTab == TAB_INDEX_WEPICK)
				{
					final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_scale);
					m_fabButton.startAnimation(anim);
					m_fabButton.show();
				}
			}
		} else
		{
			if (m_fabButton != null)
			{
				//m_hasNewWepick = true;
				m_fabButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						int curTab = m_tabs.getSelectedTabPosition();

						switch (curTab)
						{
							case TAB_INDEX_RECENTS:
							case TAB_INDEX_WEPICK:
							{
								if (m_hasNewWepick == false)
									return;

								Fragment fragment = WepickVoteFragment.newInstance(m_wepicks);
								AbsMainActivity.getTabStackHelper(ExploreFragment.this).showFragment(fragment);

								try
								{
									if (curTab == TAB_INDEX_RECENTS)
										AnalyticsManager.getInstance().RecentEvent(getContext(), "WePickFAB_Recent", "LoginUser");
									else if (curTab == TAB_INDEX_WEPICK)
										AnalyticsManager.getInstance().WepickEvent(getContext(), "WePickFAB_WePick", "LoginUser");
								}
								catch (Exception ignored)
								{
								}
							}
							break;
							/*case TAB_INDEX_GALLERY:
							{
								if (GallerySaveUtil.isExist(getContext()) == true)
								{
									onContinuousWriteDialog();
									return;
								}

								Fragment fragment = GalleryEditCoverFragment.newInstance();
								AbsMainActivity.getTabStackHelper(ExploreFragment.this).showFragment(fragment);
							}
							break;*/
						}
					}
				});
			}
		}
	}

	private void showFabButton()
	{
		switch (m_tabs.getSelectedTabPosition())
		{
			/*case TAB_INDEX_GALLERY:
			{
				//m_fabButton.setImageResource(R.drawable.ic_file_upload);
				m_fabButton.setImageResource(R.drawable.ic_write);
				m_fabButton.setVisibility(View.VISIBLE);
			}
			break;
			*/
			case TAB_INDEX_RECENTS:
			case TAB_INDEX_WEPICK:
			{
				m_fabButton.setImageResource(R.drawable.ic_fab_wepick_vote);
				if (m_hasNewWepick == true)
					m_fabButton.show();
				else
					m_fabButton.hide();
			}
			break;
			default:
				m_fabButton.hide();
		}
	}

	private void hideFabButton()
	{
		if (m_fabButton != null)
		{
			m_fabButton.hide();
		}
	}

	//=========================================================================
	// TabsAdapter
	//=========================================================================

	private class TabsAdapter extends FragmentStatePagerAdapter
	{
		public TabsAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public int getCount()
		{
			return 5;
		}

		@Override
		public Fragment getItem(int position)
		{
			return getFragment(position, new FragmentCallback()
			{
				@Override
				public Fragment createFragment(int position)
				{
					switch (position)
					{
						case 0:
							return FragmentFactory.createRecentFragment();
						case 1:
							return FragmentFactory.createPremiumFragment();
						case 2:
							return FragmentFactory.createWepickThemesFragment();
						case 3:
							return FragmentFactory.createPopularsFragment(PreferencesManager.getInstance().getPopularMode(getContext()));
						case 4:
							return FragmentFactory.createShuffleFragment();
					}
					return null;
				}
			});
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return getTabTitle(position);
		}
	}

	//=========================================================================
	// FragmentCallback
	//=========================================================================

	interface FragmentCallback
	{
		Fragment createFragment(int position);
	}

	//=========================================================================
	// FragmentInfo
	//=========================================================================

	private static class FragmentInfo implements Parcelable
	{
		public FragmentInfo(Fragment fragment)
		{
			clazz = fragment.getClass().getName();
			argument = fragment.getArguments();
		}

		@SuppressWarnings("unchecked")
		private FragmentInfo(Parcel in)
		{
			clazz = in.readString();
			argument = in.readBundle();
		}

		@Override
		public int describeContents()
		{
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			dest.writeString(clazz);
			dest.writeBundle(argument);
		}

		public static final Creator<FragmentInfo> CREATOR = new Creator<FragmentInfo>()
		{
			public FragmentInfo createFromParcel(Parcel in)
			{
				return new FragmentInfo(in);
			}

			public FragmentInfo[] newArray(int size)
			{
				return new FragmentInfo[size];
			}
		};

		private String clazz;
		private Bundle argument;
	}

	//=========================================================================
	// BusPopularMode
	//=========================================================================

	public class BusPopularMode
	{
		private int m_popularMode;

		public BusPopularMode(int popularMode)
		{
			m_popularMode = popularMode;
		}

		public int getPopularMode()
		{
			return m_popularMode;
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================
	private static final String KEY_FRAGMENT_INFOS = "KEY_FRAGMENT_INFOS";
	private static final int TAB_INDEX_RECENTS = 0;
	private static final int TAB_INDEX_GALLERY = 1;
	private static final int TAB_INDEX_WEPICK = 2;
	private static final int TAB_INDEX_POPULAR = 3;

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.view_pager) ViewPager m_viewPager;
	@BindView(R.id.tabs) TabLayout m_tabs;
	@BindView(R.id.fab) FloatingActionButton m_fabButton;

	private boolean m_hasNewWepick;
	private Wepicks m_wepicks;

	private Unbinder m_unbinder;
	private SparseArray<FragmentInfo> m_fragmentInfos = new SparseArray<FragmentInfo>();
}