package com.ogqcorp.bgh.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.ogqcorp.commons.TabStackHelper;

public abstract class TabStackActivity extends AppCompatActivity implements TabStackHelper.TabStackAdapter
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		m_tabStackHelper.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart()
	{
		m_tabStackHelper.onStart();
		super.onStart();
	}

	@Override
	protected void onDestroy()
	{
		m_tabStackHelper.onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		m_tabStackHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onBackPressed()
	{
		if (m_tabStackHelper.onBackPressed() == true)
		{
			super.onBackPressed();
		}
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	public TabStackHelper getTabStackHelper()
	{
		return m_tabStackHelper;
	}

	//=========================================================================
	// Variables
	//=========================================================================

	private TabStackHelper m_tabStackHelper = new TabStackHelper(this);
}
