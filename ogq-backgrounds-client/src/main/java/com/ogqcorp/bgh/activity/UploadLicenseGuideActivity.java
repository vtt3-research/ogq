package com.ogqcorp.bgh.activity;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.commons.WebDialogFragment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class UploadLicenseGuideActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_license_guide);

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

	@OnClick(R.id.done)
	public void onClickClose()
	{
		onBackPressed();
	}

	@OnClick(R.id.terms)
	public void onClickTerms(View v)
	{
		showWebDialog(UrlFactory.TERMS_URL, ((TextView)v).getText().toString());
	}

	@OnClick(R.id.privacy)
	public void onClickPrivacy(View v)
	{
		showWebDialog(UrlFactory.PRIVACY_URL, ((TextView)v).getText().toString());
	}

	@OnClick(R.id.management)
	public void onClickManagement(View v)
	{
		showWebDialog(UrlFactory.MANAGEMENT_URL, ((TextView)v).getText().toString());
	}

	//========================================================================
	// Private Method
	//========================================================================

	private void showWebDialog(String url, String title)
	{
		new WebDialogFragment.Builder(url)
				.setTheme(R.style.BG_Theme_Activity)
				.setLayout(R.layout.fragment_web_dialog)
				.setToolbarNavigationIcon(R.drawable.ic_back)
				.setTitle(title)
				.start(getSupportFragmentManager());
	}

	//========================================================================
	// Constants
	//========================================================================


	// ========================================================================
	// Variables
	//========================================================================

	private Unbinder m_unbinder;
}
