package com.ogqcorp.bgh.activity;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PermissionGuideActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_permission_guide);

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

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		PreferencesManager.getInstance().setPermissionGuideShown(this,true);
	}

	//========================================================================
	// Public Method
	//========================================================================

	@OnClick({R.id.close, R.id.ok})
	public void onClickClose()
	{
		onBackPressed();
	}

	// ========================================================================
	// Variables
	//========================================================================

	private Unbinder m_unbinder;
}
