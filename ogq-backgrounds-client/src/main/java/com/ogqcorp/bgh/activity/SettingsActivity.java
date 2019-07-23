package com.ogqcorp.bgh.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.coverslider.system.CoverGetDialog;
import com.ogqcorp.bgh.fragment.SettingsFragment;
import com.ogqcorp.bgh.fragment.setting.OGQCoverFragment;
import com.ogqcorp.commons.preference.PreferenceFragment;
import com.ogqcorp.commons.utils.ToastUtils;

public final class SettingsActivity extends AppCompatActivity implements SettingsFragment.Callback
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		initToolbar(toolbar);

		m_fragmentManager = getSupportFragmentManager();

		if (savedInstanceState == null)
			processIntent(getIntent());
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		CoverGetDialog.getInstance().dismissAllDialog();
	}

	@Override
	public void showPreferenceFragment(PreferenceFragment fragment)
	{
		m_fragmentManager.beginTransaction()
				.addToBackStack(null)
				.setCustomAnimations(
						R.anim.short_fade_in, R.anim.short_fade_out,
						R.anim.short_fade_in, R.anim.short_fade_out
				)
				.add(R.id.content, fragment)
				.commit();
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context)
	{
		return new Intent(context.getApplicationContext(), SettingsActivity.class);
	}

	public void setTitle(int res)
	{
		final Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(res);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initToolbar(final Toolbar toolbar)
	{
		toolbar.setTitle(R.string.p_settings);
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});
	}

	private void processIntent(Intent intent)
	{
		if (intent == null)
			return;

		if (intent.getAction() == null || intent.getAction().equals(Intent.ACTION_VIEW) == false)
		{
			onSchemeSetting();
			return;
		}

		final Uri uri = intent.getData();

		if (uri.getScheme().equals(SCHEME_BGH) == true)
		{
			if (uri.getHost().equals(HOST_COVER) == true)
				onSchemeCover();
			else
				onSchemeSetting();
		}
	}

	protected void onSchemeSetting()
	{
		try
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					m_fragmentManager.beginTransaction()
							.replace(R.id.content, SettingsFragment.newInstance())
							.commitAllowingStateLoss();
				}
			});
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		}
	}

	protected void onSchemeCover()
	{
		try
		{
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					m_fragmentManager.beginTransaction()
							.replace(R.id.content, OGQCoverFragment.newInstance())
							.commitAllowingStateLoss();
				}
			});
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(this, Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		}
	}

	//========================================================================
	// Constants
	//========================================================================

	private static final String SCHEME_BGH = "bghset";

	private static final String HOST_MAIN = "main";
	private static final String HOST_COVER = "cover";

	//=========================================================================
	// Variables
	//=========================================================================

	private FragmentManager m_fragmentManager;
}