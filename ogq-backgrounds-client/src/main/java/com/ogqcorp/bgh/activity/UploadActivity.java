package com.ogqcorp.bgh.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.fragment.SimpleUploadFilterFragment;
import com.ogqcorp.bgh.fragment.SimpleUploadPreviewFragment;
import com.ogqcorp.bgh.fragment.SimpleUploadVideoPreviewFragment;
import com.ogqcorp.bgh.fragment.SimpleUploadVideoWriteFragment;
import com.ogqcorp.bgh.fragment.SimpleUploadWriteFragment;
import com.ogqcorp.bgh.fragment.UploadCompleteFragment;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.system.AppLogger;
import com.ogqcorp.commons.utils.ToastUtils;

public final class UploadActivity extends AppCompatActivity
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload);

		if (savedInstanceState != null)
		{
			return;
		}

		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - Start ###");

		Intent intent = getIntent();

		try
		{
			if (Intent.ACTION_SEND.equals(intent.getAction()) == true && intent.getExtras() != null)
			{
				m_uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
				m_mode = intent.getIntExtra(UPLOAD_MODE, MODE_IMAGE);
				m_wepickId = intent.getStringExtra(WEPICK_ID);

				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - Info URI : " + m_uri.toString());
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - Info MODE : " + m_mode);
				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - Info WEPICK : " + m_wepickId);

				if (UserManager.getInstance().isGuest() == true)
				{
					startActivity(AuthActivity.createIntent(this, m_uri, AuthActivity.SIGN_ACTION_UPLOAD));
					finish();
					return;
				}
			}

			if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL_STORAGE) == false)
			{
				attachFragment();
			}
		}
		catch (Exception e)
		{
			ToastUtils.makeWarningToast(this, 0, R.string.error_code_unknown).show();
			finish();
			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - onCreate Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE)
		{
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				attachFragment();
			}
			else
			{
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0]) == false && m_isPremissionGuide == false)
				{
					final MaterialDialog.SingleButtonCallback onNegative = new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							finish();

							final Uri uri = Uri.fromParts("package", getPackageName(), null);
							final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
							startActivity(intent);
						}
					};

					final MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							finish();
						}
					};

					new MaterialDialog.Builder(this)
							.customView(R.layout.fragment_permission_storage_retry, true)
							.negativeText(R.string.str_setting)
							.onNegative(onNegative)
							.positiveText(R.string.ok)
							.onPositive(onPositive)
							.canceledOnTouchOutside(true)
							.dismissListener(new DialogInterface.OnDismissListener()
							{
								@Override
								public void onDismiss(DialogInterface dialog)
								{
									finish();
								}
							})
							.show();
				}
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == UploadCompleteFragment.RESULT_UPLOAD_REOPEN)
		{
			m_uri = null;
			m_background = null;
			attachFragment();
		}
	}

	@Override
	public void onBackPressed()
	{
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content);

		if (f instanceof OnKeyDownListener)
		{
			if (((OnKeyDownListener) f).onKeyEvent(keyCode, event))
			{
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy()
	{
		AnalyticsManager.getInstance().eventUserActionUpload(this, String.valueOf(m_stage));

		super.onDestroy();

		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - End ###");
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context)
	{
		return new Intent(context.getApplicationContext(), UploadActivity.class);
	}

	public static Intent createIntent(Context context, Background background)
	{
		Intent intent = new Intent(context.getApplicationContext(), UploadActivity.class);
		intent.putExtra(KEY_BACKGROUND, background);
		return intent;
	}

	public void showComplete(int mode)
	{
		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - showComplete");

		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, 0, 0)
				.replace(R.id.content, UploadCompleteFragment.newInstance(mode))
				.commitAllowingStateLoss();

		if (m_background == null) m_stage = 2;
	}

	public void showFilterStepUpload()
	{
		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment = SimpleUploadFilterFragment.newInstance(m_uri, m_background);

		fragmentManager.beginTransaction()
				.replace(R.id.content, fragment)
				.commitAllowingStateLoss();
	}

	public void showWriteStepUpload(Uri uri, boolean iscrop)
	{
		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - showWriteStep");

		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment = SimpleUploadWriteFragment.newInstance(uri, m_background, iscrop);

		fragmentManager.beginTransaction()
				.add(R.id.content, fragment)
				.addToBackStack(null)
				//.replace(R.id.content, fragment)
				.commitAllowingStateLoss();
	}

	public void showPreviewStepUpload(Uri uri)
	{
		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - showPreviewStep");

		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment = SimpleUploadPreviewFragment.newInstance(uri, m_background);

		fragmentManager.beginTransaction()
				.add(R.id.content, fragment)
				.addToBackStack(null)
				//.replace(R.id.content, fragment)
				.commitAllowingStateLoss();
	}

	public void showPreviewStepUploadVideo(Uri uri)
	{
		FragmentManager fragmentManager = getSupportFragmentManager();

		Fragment fragment = SimpleUploadVideoPreviewFragment.newInstance(uri, m_background);

		fragmentManager.beginTransaction()
				.add(R.id.content, fragment)
				.addToBackStack(null)
				//.replace(R.id.content, fragment)
				.commitAllowingStateLoss();
	}

	public int getUploadMode()
	{
		return m_mode;
	}

	public String getWepickId()
	{
		return m_wepickId;
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void attachFragment()
	{
		FragmentManager fragmentManager = getSupportFragmentManager();

		m_background = getIntent().getParcelableExtra(KEY_BACKGROUND);

		Fragment fragment = null;

		if (m_mode == MODE_VIDEO)
		{
			int license = getIntent().getIntExtra(UPLOAD_LICENSE, LICENSE_GENERAL);
			fragment = SimpleUploadVideoWriteFragment.newInstance(m_uri, m_background, license);
		}
		else
		{
			// Edit
			if (m_uri == null && m_background != null)
			{
				fragment = SimpleUploadWriteFragment.newInstance(m_uri, m_background, false);
			}
			// Create
			else
			{
				fragment = SimpleUploadFilterFragment.newInstance(m_uri, m_background);
			}
		}

		fragmentManager.beginTransaction()
				.replace(R.id.content, fragment)
				.commitAllowingStateLoss();

		if (m_background == null) m_stage = 0;
	}

	private boolean requestPermission(final String permission, final int requestCode)
	{

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
		{
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission) == true)
			{
				m_isPremissionGuide = true;
				final MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						dialog.dismiss();
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
						{
							requestPermissions(new String[] { permission }, requestCode);
						}
					}
				};

				new MaterialDialog.Builder(this)
						.customView(R.layout.fragment_permission_storage, true)
						.positiveText(R.string.ok)
						.onPositive(onPositive)
						.show();
			}
			else
			{
				m_isPremissionGuide = false;
				requestPermissions(new String[] { permission }, requestCode);
			}

			return true;
		}

		return false;
	}

	//========================================================================
	// KeyEvent Listener
	//========================================================================

	public interface OnKeyDownListener
	{
		boolean onKeyEvent(int keyCode, KeyEvent event);
	}

	//========================================================================
	// Constants
	//========================================================================

	public static final String WEPICK_ID = "WEPICK_ID";
	public static final String UPLOAD_MODE = "UPLOAD_MODE";
	public static final String UPLOAD_LICENSE = "UPLOAD_LICENSE";

	public static final int MODE_IMAGE = 0;
	public static final int MODE_VIDEO = 1;
	public static final int MODE_WEPICK = 2;

	public static final int LICENSE_PUBLIC = 8;
	public static final int LICENSE_GENERAL = 11;

	private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 405;

	//========================================================================
	// Variables\
	//========================================================================

	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";

	private Background m_background;
	private int m_stage = -1;
	private int m_mode;
	private Uri m_uri;
	private String m_wepickId;
	private boolean m_isPremissionGuide = false;
}
