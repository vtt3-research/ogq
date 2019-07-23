package com.ogqcorp.bgh.activity;

import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.IntroGuideDialogFragment;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.system.IntentLauncher;
import com.ogqcorp.commons.utils.ActivityUtils;

public class IntroGuideActivity extends AppCompatActivity
{
	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_auth);
		m_unbinder = ButterKnife.bind(this);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				showTermsDialog();
			}
		}, 150);
		//showTermsDialog();
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		m_unbinder.unbind();
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(this, screenName);
		}
		catch (Exception e)
		{
		}
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void showTermsDialog()
	{
		IntroGuideDialogFragment.DialogCallback callback = new IntroGuideDialogFragment.DialogCallback()
		{
			@Override
			public void onDismiss(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
			}

			@Override
			public void onPositive(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							AnalyticsManager.getInstance().FirstSignInGuide(getApplicationContext(), "Agree_Terms");
						}
						catch (Exception e)
						{
						}

						showPrivacyDialog();
					}
				}, INTERVAL_UNIT);
			}

			public void onBackPressed()
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				finish();
			}

			@Override
			public void onClose(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				finish();
			}
		};

		IntroGuideDialogFragment.start(getSupportFragmentManager(), R.layout.fragment_intro_guide_terms, callback);
	}

	private void showPrivacyDialog()
	{
		IntroGuideDialogFragment.DialogCallback callback = new IntroGuideDialogFragment.DialogCallback()
		{
			@Override
			public void onDismiss(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
			}

			@Override
			public void onPositive(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							AnalyticsManager.getInstance().FirstSignInGuide(getApplicationContext(), "Agree_Privacy");
						}
						catch (Exception e)
						{
						}

						showManagementDialog();
					}
				}, INTERVAL_UNIT);
			}

			public void onBackPressed()
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				finish();
			}

			@Override
			public void onClose(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				finish();
			}
		};

		IntroGuideDialogFragment.start(getSupportFragmentManager(), R.layout.fragment_intro_guide_privacy, callback);
	}

	private void showManagementDialog()
	{
		IntroGuideDialogFragment.DialogCallback callback = new IntroGuideDialogFragment.DialogCallback()
		{
			@Override
			public void onDismiss(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
			}

			@Override
			public void onPositive(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							AnalyticsManager.getInstance().FirstSignInGuide(getApplicationContext(), "Agree_Login");
						}
						catch (Exception e)
						{
						}

						finish();
						IntentLauncher.startAuthIntroActivity(IntroGuideActivity.this);
					}
				}, INTERVAL_UNIT);
			}

			public void onBackPressed()
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				finish();
			}

			@Override
			public void onClose(Fragment fragment)
			{
				if (ActivityUtils.isDestroyed(IntroGuideActivity.this) == true) return;
				finish();
			}
		};
		IntroGuideDialogFragment.start(getSupportFragmentManager(), R.layout.fragment_intro_guide_management, callback);
	}

	//========================================================================
	// Constants
	//========================================================================
	private final int INTERVAL_UNIT = 100;

	//========================================================================
	// Variables
	//========================================================================

	private Unbinder m_unbinder;

}