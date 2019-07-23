package com.ogqcorp.bgh.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.ogqcorp.bgh.R;

public class LicenseGuideActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		int resId = -1;
		if (getIntent() != null && getIntent().getExtras() != null)
		{
			resId = getIntent().getIntExtra(LAYOUT_RES_ID, -1);
			if (resId < 0)
			{
				finish();
				return;
			}
		}

		setContentView(resId);
		m_unbinder = ButterKnife.bind(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if (m_unbinder != null)
		{
			m_unbinder.unbind();
		}
	}

	//========================================================================
	// Public Method
	//========================================================================

	@OnClick(R.id.close)
	public void onClickClose()
	{
		onBackPressed();
	}

	//========================================================================
	// Constants
	//========================================================================

	public static final String LAYOUT_RES_ID = "LAYOUT_RES_ID";

	// ========================================================================
	// Variables
	//========================================================================

	private Unbinder m_unbinder;
}
