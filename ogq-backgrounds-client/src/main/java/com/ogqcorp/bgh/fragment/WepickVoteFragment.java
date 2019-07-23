package com.ogqcorp.bgh.fragment;

import org.jetbrains.annotations.NotNull;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.adapter.WepickVoteAdapter;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.shine.ShineButton;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.data.Wepick;
import com.ogqcorp.bgh.spirit.data.Wepicks;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.FLManagerCompatUtils;
import com.ogqcorp.bgh.system.FragmentFactory;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.bgh.view.vote.Koloda;
import com.ogqcorp.bgh.view.vote.KolodaListener;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

/**
 * Created by ogq on 2018. 6. 27..
 */

public class WepickVoteFragment extends BaseActionBarFragment
{

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		m_sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

		m_GgyroSensor = m_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		m_GyroListener = new GyroscopeListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_wepick_vote, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);
		m_isIntroShown = PreferencesManager.getInstance().getWepickIntroShown(getContext());

		if (getArguments() != null)
		{
			m_wepicks = getArguments().getParcelable(KEY_WEPICKS);
		}
		else if (savedInstanceState != null)
		{
			m_wepicks = savedInstanceState.getParcelable(KEY_WEPICKS);
		}

		m_adapter = new WepickVoteAdapter(getContext())
		{
			@Override
			protected void onClickClose()
			{
				getActivity().onBackPressed();
			}

			@Override
			protected void onClickProfile(User user)
			{
				Fragment fragment = UserInfoFragment.newInstance(UrlFactory.usersInfo(user.getUsername()));
				AbsMainActivity.getTabStackHelper(WepickVoteFragment.this).showFragment(fragment);
			}
		};

		m_actionVote.init(getActivity());
		m_actionVote.setClickable(false);
		m_actionVote.setChecked(true, false);

		initializeDeck();
		fillData();
	}

	@Override
	public void onStart()
	{
		super.onStart();

		final ViewTreeObserver viewTreeObserver = m_root.getViewTreeObserver();
		viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
		{
			@Override
			public void onGlobalLayout()
			{
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
				{
					//noinspection deprecation
					m_root.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
				else
				{
					m_root.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			}
		});
		onStartHelper();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);
		}
		catch (Exception ignored)
		{
		}
	}

	private void onStartHelper()
	{
		if (getUserVisibleHint() == true && getView() != null)
		{
			if (isEmpty() == true)
			{
				loadData();
			}
			else
			{
				if (m_isIntroShown == false && m_isIntroShowning == false)
				{
					new Handler().postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							if (FragmentUtils.isDestroyed(WepickVoteFragment.this)) return;
							showIntroStep1();
						}
					}, 300);
				}
			}
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((AppCompatActivity) getActivity()).getSupportActionBar().hide();
		m_sensorManager.registerListener(m_GyroListener, m_GgyroSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		((AppCompatActivity) getActivity()).getSupportActionBar().show();
		m_sensorManager.unregisterListener(m_GyroListener);
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
		outState.putParcelable(KEY_WEPICKS, m_wepicks);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Fragment newInstance(Wepicks wepicks)
	{
		final Fragment fragment = new WepickVoteFragment();

		if (wepicks != null)
		{
			Bundle bundle = new Bundle();
			bundle.putParcelable(KEY_WEPICKS, wepicks);
			fragment.setArguments(bundle);
		}

		return fragment;
	}

	private boolean isEmpty()
	{
		return m_wepicks == null || m_wepicks.getWepickList().isEmpty() == true;

	}

	private void loadData()
	{
		if (FragmentUtils.isDestroyed(WepickVoteFragment.this) == true)
			return;

		if (m_isLoading == true)
			return;

		try
		{
			m_isLoading = true;
			String dataUrl = UrlFactory.wepicksCurrent();
			if (UserManager.getInstance().isGuest() == true)
			{
				Requests.requestByGet(dataUrl, Wepicks.class, m_response, m_errorResponse);
			}
			else
			{
				Requests.authRequestByGet(dataUrl, Wepicks.class, m_response, m_errorResponse);
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	private void initializeDeck()
	{
		m_kolodaListener = new KolodaListener()
		{
			@Override
			public void onNewTopCard(int position)
			{
				updateCount(position + 1);
				//Toast.makeText(getContext(), "On new top card", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCardDrag(int position, @NotNull View cardView, float progress)
			{
				if (m_actionVote != null)
				{
					m_actionVote.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onCardReset(int position)
			{
				if (m_actionVote != null)
				{
					m_actionVote.setVisibility(View.GONE);
				}
			}

			@Override
			public void onCardSwipedLeft(int position)
			{
				//Toast.makeText(getContext(), "On card swiped left", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCardSwipedRight(int position)
			{
				//Toast.makeText(getContext(), "On card swiped right", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCardSwipedTop(int position)
			{
				if (FragmentUtils.isDestroyed(WepickVoteFragment.this) == true) return;

				vote(position, true);
			}

			@Override
			public void onCardSwipedBottom(int position)
			{
				if (FragmentUtils.isDestroyed(WepickVoteFragment.this) == true) return;

				vote(position, false);
			}

			@Override
			public void onClickRight(int position)
			{
			}

			@Override
			public void onClickLeft(int position)
			{
			}

			@Override
			public void onCardSingleTap(int position)
			{
			}

			@Override
			public void onCardDoubleTap(int position)
			{
			}

			@Override
			public void onCardLongPress(int position)
			{
			}

			@Override
			public void onEmptyDeck()
			{
				m_isVoteEnd = true;
			}
		};
		m_voteView.setKolodaListener(m_kolodaListener);
	}

	public void fillData()
	{
		if (isEmpty() == false)
		{
			m_adapter.setData(m_wepicks.getWepickList());
			m_progresload.setVisibility(View.GONE);
		}
		m_voteView.setAdapter(m_adapter);
		m_voteView.setNeedCircleLoading(false);
	}

	@OnClick(R.id.guide)
	public void onClickGuide(View view)
	{
		switch (view.getId())
		{
			case R.id.guide:
				showIntroNext();
				break;
			default:
				break;
		}
	}

	public void showVoteAnimation()
	{
		m_actionVote.setVisibility(View.VISIBLE);
		m_actionVote.setChecked(true, true);
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (FragmentUtils.isDestroyed(WepickVoteFragment.this))
				{
					return;
				}
				m_actionVote.setVisibility(View.GONE);
				m_actionVote.setChecked(false);
				onPostVote();
			}
		}, 800);
	}

	public void onPostVote()
	{
		if (m_isVoteEnd == true)
		{
			String id = null;
			String title = null;
			try
			{
				title = m_wepicks.getTitle();
				id = m_wepicks.getWepickList().get(0).getId();
			}
			catch (Exception ignored)
			{
			}
			finally
			{
				AbsMainActivity.getTabStackHelper(WepickVoteFragment.this).onBackPressed();
			}

			if (TextUtils.isEmpty(id) == false)
			{
				//Fragment fragment = FragmentFactory.createWepicksFragment(id, title);
				Fragment fragment = FragmentFactory.createWepicksFragment(id);
				AbsMainActivity.getTabStackHelper(WepickVoteFragment.this).showFragment(fragment);
			}
		}
	}

	public void updateCount(int count)
	{
		m_count.setVisibility(View.VISIBLE);
		m_total.setVisibility(View.VISIBLE);

		m_count.setText(String.valueOf(count));
		m_total.setText("/" + String.valueOf(m_adapter.getCount()));
	}

	public void vote(int position, final boolean skip)
	{
		updateBackgroundContents();
		final Wepick wepick = m_wepicks.getWepickList().get(position);
		if (wepick == null)
			return;

		if (skip == true)
		{
			//Must be called in order, if changed, an error occurs!
			m_actionVote.setVisibility(View.GONE);
			onPostVote();
		}
		else
		{
			showVoteAnimation();
		}

		Requests.authRequestByPut(
				UrlFactory.wepickVote(),
				ParamFactory.wepickVote(wepick.getItemId(), skip ? "SKIP" : "UP"),
				Object.class,
				new Response.Listener<Object>()
				{
					@Override
					public void onResponse(Object response)
					{
						if (skip == false)
						{
							FLManagerCompatUtils.insert(wepick.getBackground());
						}
					}
				},
				new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						//Nothing
					}
				}
		);
	}

	//=========================================================================
	// Listeners
	//=========================================================================

	protected Response.Listener<Wepicks> m_response = new Response.Listener<Wepicks>()
	{
		@Override
		public void onResponse(Wepicks wepicks)
		{
			if (FragmentUtils.isDestroyed(WepickVoteFragment.this) == true) return;

			if (wepicks != null)
			{
				if (m_wepicks == null)
				{
					m_wepicks = wepicks;
				}
				else
				{
					m_wepicks.setWepickList(wepicks.getWepickList());
				}

				if (m_isIntroShown == false && m_isIntroShowning == false)
				{
					showIntroStep1();
				}
				m_adapter.setData(wepicks.getWepickList());
				m_adapter.notifyDataSetChanged();
			}
			m_isLoading = false;
			m_progresload.setVisibility(View.GONE);
		}

	};

	protected Response.ErrorListener m_errorResponse = new Response.ErrorListener()
	{
		@Override
		public void onErrorResponse(VolleyError volleyError)
		{
			if (FragmentUtils.isDestroyed(WepickVoteFragment.this) == true) return;

			try
			{
				m_isLoading = false;
				m_progresload.setVisibility(View.GONE);
				VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
				volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				volleyErrorHandler.handleError(volleyError);
			}
			catch (Exception e)
			{
				// Nothing
			}
		}
	};

	private void showIntroStep1()
	{
		m_isIntroShowning = true;
		m_curGuideStep = GUIDE_STEP1;
		m_guide.setVisibility(View.VISIBLE);
		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_out);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				m_guideText1.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				m_guideText1.setVisibility(View.GONE);
				showIntroStep2();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});
		m_guideText1.startAnimation(anim);
	}

	private void showIntroStep2()
	{
		m_guideText1.clearAnimation();
		m_curGuideStep = GUIDE_STEP2;
		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in_out);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				m_guideText2.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				m_guideText2.setVisibility(View.GONE);
				showIntroStep3();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});
		m_guideText2.startAnimation(anim);
	}

	private void showIntroStep3()
	{
		m_guideText2.clearAnimation();
		m_curGuideStep = GUIDE_STEP3;
		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.short_fade_in);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				m_guideText3.setVisibility(View.VISIBLE);
				m_guideBoxIcon.setVisibility(View.VISIBLE);
				m_guideHandIcon.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				showIntroStep4();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});
		m_guideText3.startAnimation(anim);
	}

	private void showIntroStep4()
	{
		m_guideText3.clearAnimation();
		m_curGuideStep = GUIDE_STEP4;
		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.move_down);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				m_guideHandIcon.setVisibility(View.INVISIBLE);
				showIntroStep5();
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});

		m_guideHandIcon.startAnimation(anim);
	}

	private void showIntroStep5()
	{
		m_guideHandIcon.clearAnimation();
		m_curGuideStep = GUIDE_STEP5;
		PreferencesManager.getInstance().setWepickIntroShown(getContext(), true);

		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.short_fade_in);
		anim.setAnimationListener(new Animation.AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
				m_guideText3.setText(R.string.wepick_vote_guide4);
				m_guideBoxIcon.setImageResource(R.drawable.ic_wepick_vote);
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						if (FragmentUtils.isDestroyed(WepickVoteFragment.this)) return;
						m_guideText3.setVisibility(View.GONE);
						m_guideBoxIcon.setVisibility(View.GONE);
						m_guide.setVisibility(View.GONE);
						m_isIntroShown = true;
						m_isIntroShowning = false;
					}
				}, 800);
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
		});

		m_guideText3.startAnimation(anim);
		m_guideBoxIcon.startAnimation(anim);
	}

	private void showIntroNext()
	{
		switch (m_curGuideStep)
		{
			case GUIDE_STEP1:
				showIntroStep2();
				break;
			case GUIDE_STEP2:
				showIntroStep3();
				break;
			case GUIDE_STEP3:
				showIntroStep4();
				break;
			case GUIDE_STEP4:
				showIntroStep5();
				break;
			case GUIDE_STEP5:
				m_guide.setVisibility(View.GONE);
				break;
		}
	}

	private void updateBackgroundContents()
	{
		m_defaultBgView.setVisibility(View.GONE);
		m_finishBgView.setVisibility(View.VISIBLE);
	}

	//=========================================================================
	// GyroscopeListener
	//=========================================================================

	private class GyroscopeListener implements SensorEventListener
	{
		@Override
		public void onSensorChanged(SensorEvent event)
		{
			try
			{
				//double gyroX = event.values[0];
				double gyroY = event.values[1];

				/* 각속도를 적분하여 회전각을 추출하기 위해 적분 간격(m_dt)을 구한다.
				 * m_dt : 센서가 현재 상태를 감지하는 시간 간격 */
				m_dt = (event.timestamp - m_timestamp) * NS2S;
				m_timestamp = event.timestamp;

				/* 센서 인식을 활성화 하여 처음 timestamp가 0일때는 dt값이 올바르지 않으므로 넘어간다. */
				if (m_dt - m_timestamp * NS2S != 0)
				{
					/* 각속도 성분을 적분 -> 회전각(m_pitch, m_roll)으로 변환.
					 * 여기까지의 m_pitch, roll의 단위는 '라디안'이다.*/
					m_pitch = gyroY * m_dt;

					m_voteView.onDrag(m_pitch);
				}
			}catch (Exception e)
			{
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy)
		{
		}
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final int GUIDE_STEP1 = 0;
	private static final int GUIDE_STEP2 = 1;
	private static final int GUIDE_STEP3 = 2;
	private static final int GUIDE_STEP4 = 3;
	private static final int GUIDE_STEP5 = 4;

	private static final String KEY_WEPICKS = "KEY_WEPICKS";

	//NS2S : nano second -> second
	private static final float NS2S = 1.0f / 1000000000.0f;

	//=========================================================================
	// Variables
	//=========================================================================

	private Unbinder m_unbinder;
	private KolodaListener m_kolodaListener;

	private Wepicks m_wepicks;
	private WepickVoteAdapter m_adapter;

	private int m_curGuideStep;

	private boolean m_isVoteEnd;
	private boolean m_isLoading;
	private boolean m_isIntroShown;
	private boolean m_isIntroShowning;

	private SensorManager m_sensorManager;

	private Sensor m_GgyroSensor;
	private SensorEventListener m_GyroListener;

	private double m_dt;
	private double m_timestamp;

	private double m_pitch;

	@BindView(R.id.root) View m_root;
	@BindView(R.id.guide) View m_guide;

	@BindView(R.id.bg_view_intro) View m_defaultBgView;
	@BindView(R.id.bg_view_finish) View m_finishBgView;

	@BindView(R.id.content1) TextView m_guideText1;
	@BindView(R.id.content2) TextView m_guideText2;
	@BindView(R.id.content3) TextView m_guideText3;

	@BindView(R.id.photo) ImageView m_bgImage;
	@BindView(R.id.title) TextView m_bgTitle;
	@BindView(R.id.sub_title) TextView m_bgSubtitle;

	@BindView(R.id.box) ImageView m_guideBoxIcon;
	@BindView(R.id.hand) ImageView m_guideHandIcon;

	@BindView(R.id.count) TextView m_count;
	@BindView(R.id.total) TextView m_total;

	@BindView(R.id.action_vote) ShineButton m_actionVote;

	@BindView(R.id.vote) Koloda m_voteView;
	@BindView(R.id.progress_loading) ProgressWheel m_progresload;
}
