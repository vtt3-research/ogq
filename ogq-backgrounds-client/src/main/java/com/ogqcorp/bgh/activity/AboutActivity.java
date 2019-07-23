package com.ogqcorp.bgh.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.commons.TextDialogFragment;

public final class AboutActivity extends AppCompatActivity
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		initToolbar(toolbar);

		ButterKnife.bind(this);
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

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context)
	{
		return new Intent(context.getApplicationContext(), AboutActivity.class);
	}

	@OnClick(R.id.thanks)
	public void onThanks()
	{
		try
		{
			new TextDialogFragment.Builder()
					.setTheme(R.style.BG_Theme_Activity_Settings)
					.setLayout(R.layout.fragment_text_dialog)
					.setToolbarNavigationIcon(R.drawable.ic_back)
					.setTitle(this, R.string.p_about_ogq)
					.setText(getAssets().open("text/thanks.txt"))
					.start(getSupportFragmentManager());
		}
		catch (Exception e)
		{
			// Nothing

		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initToolbar(Toolbar toolbar)
	{
		toolbar.setTitle(R.string.p_about_ogq);

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
}
