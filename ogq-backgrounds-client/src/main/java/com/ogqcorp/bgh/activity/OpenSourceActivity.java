package com.ogqcorp.bgh.activity;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;

public final class OpenSourceActivity extends AppCompatActivity
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opensource);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		initToolbar(toolbar);

		ButterKnife.bind(this);

		try
		{
			String content = IOUtils.toString(getAssets().open("text/open_source.txt"));
			m_content.setText(content);

		}
		catch (Exception e)
		{

		}
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
		return new Intent(context.getApplicationContext(), OpenSourceActivity.class);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initToolbar(Toolbar toolbar)
	{
		toolbar.setTitle(R.string.p_opensource_title);

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

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.content) TextView m_content;
}
