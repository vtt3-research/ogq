package com.ogqcorp.bgh.fragment.explore;

import rx.Subscription;
import rx.functions.Action1;

import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthLoginMainActivity;
import com.ogqcorp.bgh.chat.BusChatEvent;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.fragment.base.BaseFragment;
import com.ogqcorp.bgh.gcm.BusGcm;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.system.FragmentFactory;
import com.ogqcorp.bgh.system.IntentLauncher;
import com.ogqcorp.bgh.system.RxBus;
import com.ogqcorp.bgh.view.ScrollableViewPager;

public final class MainFragment extends BaseActionBarFragment
{
	//=========================================================================
	// Constructors
	//=========================================================================

	@Deprecated
	public MainFragment()
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
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	@Override
	public void onViewCreated(View view, final Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (savedInstanceState != null)
		{
			m_fragmentInfos = savedInstanceState.getSparseParcelableArray(KEY_FRAGMENT_INFOS);
		}

		if (UserManager.getInstance().isGuest() == false)
		{
			registerListeners();
		}

		m_tabsAdapter = new TabsAdapter(getChildFragmentManager());
		m_viewPager.setCanScroll(false);
		m_viewPager.setAdapter(m_tabsAdapter);
		m_viewPager.setCurrentItem(TAB_EXPLORE, false);

		m_navigationView.setSelectedItemId(R.id.action_explore);
		m_navigationView.setOnNavigationItemSelectedListener(m_navigationItemSelectedListener);
		m_navigationView.setOnNavigationItemReselectedListener(m_onNavigationItemReselectedListener);

		int viewCount = PreferencesManager.getInstance().getBackgroundPageViewCount(getContext());
		boolean isShow = PreferencesManager.getInstance().isShowVideoUploadGuide(getContext());

		if (isShow == true && viewCount >= 3)
		{
			final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.move_up_down);

			m_tooltip.setVisibility(View.VISIBLE);
			m_tooltip.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					if (m_tooltip != null)
					{
						m_tooltip.startAnimation(anim);
					}
				}
			}, 2000);
			m_tooltip.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					if (m_tooltip != null)
					{
						m_tooltip.setVisibility(View.GONE);
						m_tooltip.clearAnimation();
						PreferencesManager.getInstance().setShowVideoUploadGuide(getContext(), false);
						if (getActivity() instanceof AbsMainActivity)
						{
							((AbsMainActivity) getActivity()).onClickUpload();
						}
					}
				}
			});
		}

		initBadgeView();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();

		if (UserManager.getInstance().isGuest() == false)
		{
			unregisterListeners();
		}

		if (m_tooltip != null)
		{
			m_tooltip.clearAnimation();
		}

		m_userInfoListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putSparseParcelableArray(KEY_FRAGMENT_INFOS, m_fragmentInfos);
		outState.putString(KEY_NOTI_COUNT_ACTIVITIES, getBadgeCount(TAB_ACTIVITIES));
		outState.putString(KEY_NOTI_COUNT_MY_PROFILE, getBadgeCount(TAB_MY_PROFILE));
	}

	@Override
	public void onResume()
	{
		super.onResume();

		if (m_viewPager.getCurrentItem() == TAB_MY_PROFILE)
		{
			//Used to update notification count!
			UserManager.getInstance().updateUserSelfInfo();
		}
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

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance()
	{
		return new MainFragment();
	}

	public void setCurrentItem(int position)
	{
		switch (position)
		{
			case TAB_UPLOAD:
				m_navigationView.setSelectedItemId(R.id.action_upload);
				break;
			case TAB_ACTIVITIES:
				m_navigationView.setSelectedItemId(R.id.action_notification);
				break;
			case TAB_MY_PROFILE:
				m_navigationView.setSelectedItemId(R.id.action_profile);
				break;
		}
	}

	public int getCurrentItem()
	{
		switch (m_navigationView.getSelectedItemId())
		{
			case R.id.action_feed:
				return TAB_USER_FEEDS;
			case R.id.action_explore:
				return TAB_EXPLORE;
			case R.id.action_upload:
				return TAB_UPLOAD;
			case R.id.action_notification:
				return TAB_ACTIVITIES;
			case R.id.action_profile:
				return TAB_MY_PROFILE;
			default:
				return -1;
		}
	}

	public boolean moveLandingScreen()
	{
		boolean isMoved = false;

		if (getCurrentItem() != TAB_EXPLORE)
		{
			isMoved = true;
			m_navigationView.setSelectedItemId(R.id.action_explore);
		}

		final Fragment fragment = m_tabsAdapter.getRegisteredFragment(TAB_EXPLORE);
		if (fragment != null && fragment instanceof ExploreFragment)
		{
			boolean result = ((ExploreFragment) fragment).moveLandingScreen();
			return isMoved ? true : result;
		}

		return isMoved;
	}

	public void initBadgeView()
	{
		BottomNavigationMenuView menuView = (BottomNavigationMenuView) m_navigationView.getChildAt(0);
		BottomNavigationItemView actView = (BottomNavigationItemView) menuView.getChildAt(3);
		BottomNavigationItemView msgView = (BottomNavigationItemView) menuView.getChildAt(4);

		m_actNotiView = LayoutInflater.from(getContext()).inflate(R.layout.item_noti_badge, menuView, false);
		actView.addView(m_actNotiView);

		m_msgNotiView = LayoutInflater.from(getContext()).inflate(R.layout.item_noti_badge, menuView, false);
		msgView.addView(m_msgNotiView);
	}

	public String getBadgeCount(int position)
	{
		try
		{
			TextView badge = null;
			switch (position)
			{
				case TAB_ACTIVITIES:
					badge = m_actNotiView.findViewById(R.id.badge);
					break;
				case TAB_MY_PROFILE:
					badge = m_msgNotiView.findViewById(R.id.badge);
					break;
			}

			return badge != null ? badge.getText().toString() : "0";
		}
		catch (Exception e)
		{
			return "0";
		}
	}

	public void setBadgeCount(int count, int position)
	{
		setBadgeCount(count, position, false);
	}

	public void setBadgeCount(final int count, int position, boolean animation)
	{
		TextView view = null;
		switch (position)
		{
			case TAB_ACTIVITIES:
				view = m_actNotiView.findViewById(R.id.badge);
				break;
			case TAB_MY_PROFILE:
				view = m_msgNotiView.findViewById(R.id.badge);
				break;
			default:
				return;
		}

		if (view == null) throw new AssertionError();

		User user = UserManager.getInstance().getUser();
		final TextView badge = view;
		final String prettyCount = user.getPrettyUnreadCount(count);

		if (count > 0)
		{
			final Runnable startAction = new Runnable()
			{
				@Override
				public void run()
				{
					badge.setText(prettyCount);
					badge.setVisibility(View.VISIBLE);
					badge.setScaleX(0);
					badge.setScaleY(0);
				}
			};

			if (animation == true)
			{
				badge.animate()
						.scaleX(1)
						.scaleY(1)
						.setDuration(ANIMATION_DURATION)
						.setInterpolator(OVERSHOOT_INTERPOLATOR)
						.withStartAction(startAction)
						.start();
			} else
			{
				startAction.run();
			}
		} else
		{
			final Runnable endAction = new Runnable()
			{
				@Override
				public void run()
				{
					badge.setText("0");
					badge.setVisibility(View.GONE);
				}
			};

			if (animation == true)
			{
				badge.animate()
						.scaleX(0)
						.scaleY(0)
						.setDuration(ANIMATION_DURATION)
						.setInterpolator(ANTICIPATE_INTERPOLATOR)
						.withEndAction(endAction)
						.start();
			} else
			{
				endAction.run();
			}
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

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

	private void showUnreadCount()
	{
		User user = UserManager.getInstance().getUser();
		int messageCount = user.getUnreadMessageCount();
		int activityCount = user.getUnreadActivityCount();

		setBadgeCount(messageCount, TAB_MY_PROFILE, true);
		setBadgeCount(activityCount, TAB_ACTIVITIES, true);
	}

	private void registerListeners()
	{
		if (UserManager.getInstance().isGuest() == false)
		{
			UserManager.getInstance().registerUpdateUserInfoListener(m_userInfoListener);

			m_subscription = RxBus.getInstance().registerListenerMainThread(BusGcm.class, new Action1<BusGcm>()
			{
				@Override
				public void call(BusGcm busGcm)
				{
					showUnreadCount();
				}
			});

			m_subscriptionChatEvent = RxBus.getInstance().registerListenerMainThread(BusChatEvent.class, new Action1<BusChatEvent>()
			{
				@Override
				public void call(BusChatEvent busChat)
				{
					switch (busChat.getEventType())
					{
						case BusChatEvent.EVENT_TYPE_COMPLETE_ALERT_CHECK:
							setBadgeCount(0, TAB_MY_PROFILE, true);
							break;
						default:
							break;
					}
				}
			});
		}
	}

	private void unregisterListeners()
	{
		UserManager.getInstance().unregisterUpdateUserInfoListener(m_userInfoListener);

		if (m_subscription != null)
		{
			m_subscription.unsubscribe();
		}

		if (m_subscriptionChatEvent != null)
		{
			m_subscriptionChatEvent.unsubscribe();
		}
	}

	//=========================================================================
	// TabsAdapter
	//=========================================================================
	private class TabsAdapter extends FragmentStatePagerAdapter
	{
		SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

		TabsAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public int getCount()
		{
			return TAB_TOTAL_COUNT;
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
						case TAB_USER_FEEDS:
							return FragmentFactory.createHomeFragment();
						case TAB_EXPLORE:
							return FragmentFactory.createExploreFragment();
						case TAB_UPLOAD:
							return FragmentFactory.createUploadContentsFragment();
						case TAB_ACTIVITIES:
							return FragmentFactory.createActivitiesFragment(UserManager.getInstance().getUser());
						case TAB_MY_PROFILE:
							return FragmentFactory.createMyInfoFragment();
					}
					return null;
				}
			});
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragments.put(position, fragment);
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object)
		{
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		public Fragment getRegisteredFragment(int position)
		{
			return registeredFragments.get(position);
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

		public static final Creator<FragmentInfo> CREATOR = new Creator<MainFragment.FragmentInfo>()
		{
			public MainFragment.FragmentInfo createFromParcel(Parcel in)
			{
				return new MainFragment.FragmentInfo(in);
			}

			public MainFragment.FragmentInfo[] newArray(int size)
			{
				return new MainFragment.FragmentInfo[size];
			}
		};

		private String clazz;
		private Bundle argument;
	}

	//========================================================================
	// Listeners
	//========================================================================

	BottomNavigationView.OnNavigationItemSelectedListener m_navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
	{
		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item)
		{
			boolean isGuest = UserManager.getInstance().isGuest();

			switch (item.getItemId())
			{
				case R.id.action_feed:
				{
					if (isGuest == true)
					{
						IntentLauncher.startAuthLogInMainActivity(getActivity(), AuthLoginMainActivity.TAB_INDEX_FEED);
						return false;
					} else
					{
						setActionBarAlpha(255);
						m_viewPager.setCurrentItem(TAB_USER_FEEDS, false);
						return true;
					}
				}
				case R.id.action_explore:
				{
					setActionBarAlpha(255);
					m_viewPager.setCurrentItem(TAB_EXPLORE, false);
					return true;
				}
				case R.id.action_upload:
				{
					if (isGuest == true)
					{
						IntentLauncher.startAuthLogInMainActivity(getActivity(), AuthLoginMainActivity.TAB_INDEX_UPLOAD);
						return false;
					} else
					{
						setActionBarAlpha(255);
						m_viewPager.setCurrentItem(TAB_UPLOAD, false);
						return true;
					}
				}
				case R.id.action_notification:
				{
					if (isGuest == true)
					{
						IntentLauncher.startAuthLogInMainActivity(getActivity(), AuthLoginMainActivity.TAB_INDEX_ACTIVITY);
						return false;
					} else
					{
						setActionBarAlpha(255);
						m_viewPager.setCurrentItem(TAB_ACTIVITIES, false);
						setBadgeCount(0, TAB_ACTIVITIES);
						UserManager.getInstance().getUser().setUnreadActivityCount(0);
						return true;
					}
				}
				case R.id.action_profile:
				{
					if (isGuest == true)
					{
						IntentLauncher.startAuthLogInMainActivity(getActivity(), AuthLoginMainActivity.TAB_INDEX_PROFILE);
						return false;
					} else
					{
						m_viewPager.setCurrentItem(TAB_MY_PROFILE, false);
						return true;
					}
				}
			}
			return false;
		}
	};

	BottomNavigationView.OnNavigationItemReselectedListener m_onNavigationItemReselectedListener = new BottomNavigationView.OnNavigationItemReselectedListener()
	{
		@Override
		public void onNavigationItemReselected(@NonNull MenuItem item)
		{
			BaseFragment fragment = (BaseFragment) m_viewPager.getAdapter().instantiateItem(m_viewPager, m_viewPager.getCurrentItem());
			fragment.onScrollTop();
		}
	};

	private UserManager.UpdateUserInfoListener m_userInfoListener = new UserManager.UpdateUserInfoListener()
	{
		@Override
		public void onSuccess(User user)
		{
			showUnreadCount();
		}

		@Override
		public void onFail(Exception e)
		{
			// Nothing
		}
	};

	//=========================================================================
	// Constants
	//=========================================================================
	private static final String KEY_FRAGMENT_INFOS = "KEY_FRAGMENT_INFOS";
	private static final String KEY_NOTI_COUNT_ACTIVITIES = "KEY_NOTI_COUNT_ACTIVITIES";
	private static final String KEY_NOTI_COUNT_MY_PROFILE = "KEY_NOTI_COUNT_MY_PROFILE";

	public static final int TAB_USER_FEEDS = 0;
	public static final int TAB_EXPLORE = 1;
	public static final int TAB_UPLOAD = 2;
	public static final int TAB_ACTIVITIES = 3;
	public static final int TAB_MY_PROFILE = 4;

	private static final int TAB_TOTAL_COUNT = 5;

	private static int ANIMATION_DURATION = 300;
	private static Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
	private static Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.view_pager) ScrollableViewPager m_viewPager;
	@BindView(R.id.tooltip) TextView m_tooltip;
	@BindView(R.id.bottom_navigation) BottomNavigationView m_navigationView;

	private View m_actNotiView;
	private View m_msgNotiView;

	private Subscription m_subscription;
	private Subscription m_subscriptionChatEvent;

	private Unbinder m_unbinder;

	private TabsAdapter m_tabsAdapter;
	private SparseArray<FragmentInfo> m_fragmentInfos = new SparseArray<FragmentInfo>();

}