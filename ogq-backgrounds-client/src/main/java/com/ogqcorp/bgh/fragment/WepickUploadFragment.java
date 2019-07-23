package com.ogqcorp.bgh.fragment;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.core.graphics.ColorUtils;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.bumptech.glide.Glide;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.WepickTheme;
import com.ogqcorp.bgh.system.FragmentFactory;

/**
 * Created by ogq on 2018. 6. 27..
 */

public class WepickUploadFragment extends BaseActionBarFragment
{

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
		return inflater.inflate(R.layout.fragment_wepick_upload, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		Bundle bundle = getArguments();
		if (bundle != null)
		{
			m_wepickTheme = bundle.getParcelable(KEY_WEPICK_THEME);
		}

		constructView();
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

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(WepickTheme theme)
	{
		final Fragment fragment = new WepickUploadFragment();

		if (theme != null)
		{
			Bundle bundle = new Bundle();
			bundle.putParcelable(KEY_WEPICK_THEME, theme);
			fragment.setArguments(bundle);
		}

		return fragment;
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void constructView()
	{
		getToolbar().setTitle(R.string.wepick_uploading_title);

		String days = getString(R.string.wepick_uploading_end_dayleft, m_wepickTheme.getRemainDay(m_wepickTheme.getUploadEndDate()));

		m_titleView.setText(m_wepickTheme.getTitle());
		m_periodView.setText(days);
		Glide.with(this).load(m_wepickTheme.getPreviewUrl()).thumbnail(0.2f).into(m_imageView);

		m_fabButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (UserManager.getInstance().isGuest() == true)
				{
					AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "WEPICK_UPLOAD_FAB");
					getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_WEPICK));
					return;
				}
				else
				{
					Fragment fragment = FragmentFactory.createUploadImageBoxesFragment(UploadActivity.MODE_WEPICK, m_wepickTheme.getId());
					AbsMainActivity.getTabStackHelper(WepickUploadFragment.this).showFragment(fragment);
					return;
				}
			}
		});

		final Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce_scale);
		m_fabButton.startAnimation(anim);
		m_fabButton.setVisibility(View.VISIBLE);
	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_WEPICK_THEME = "KEY_WEPICK_THEME";

	//=========================================================================
	// Variables
	//=========================================================================

	private Unbinder m_unbinder;
	private WepickTheme m_wepickTheme;

	@BindView(R.id.image) ImageView m_imageView;
	@BindView(R.id.title) TextView m_titleView;
	@BindView(R.id.period) TextView m_periodView;
	@BindView(R.id.wepick) FloatingActionButton m_fabButton;
}
