package com.ogqcorp.bgh.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.user.BlockedUserFragment;

public final class BlockedUserActivity extends AppCompatActivity
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blocked_users);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		initToolbar(toolbar);

		m_fragmentManager = getSupportFragmentManager();

		if (savedInstanceState == null)
		{
			m_fragmentManager.beginTransaction()
					.replace(R.id.content, BlockedUserFragment.newInstance())
					.commit();
		}
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context)
	{
		return new Intent(context.getApplicationContext(), BlockedUserActivity.class);
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
		toolbar.setTitle(R.string.p_privacy_block_user);
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

	private FragmentManager m_fragmentManager;
}
