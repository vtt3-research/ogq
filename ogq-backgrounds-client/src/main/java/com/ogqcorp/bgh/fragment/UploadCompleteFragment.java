package com.ogqcorp.bgh.fragment;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.system.ShareManager;

public class UploadCompleteFragment extends Fragment
{
	//========================================================================
	// override Methods
	//========================================================================

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_upload_complete, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		try
		{
			int mode = getArguments().getInt(KEY_MODE);
			switch (mode)
			{
				case MODE_UPLOAD_IMAGE:
					AnalyticsManager.getInstance().shareEvent(getContext(), "Share_ImageUpload");
					break;
				case MODE_UPLOAD_VIDEO:
					AnalyticsManager.getInstance().shareEvent(getContext(), "Share_VideoUpload");
					break;
			}
		}
		catch (Exception e)
		{

		}

		if (isWepickMode() == true)
		{
			StringBuffer buffer = new StringBuffer()
					.append(getString(R.string.wepick_upload_complete_desc_1))
					.append("\n\n")
					.append(getString(R.string.wepick_upload_complete_desc_2));

			m_titleView.setText(R.string.wepick_upload_complete_title);
			m_descView.setText(buffer.toString());
			m_moreView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@OnClick({ R.id.close, R.id.done })
	protected void onClickClose(View view)
	{
		getActivity().setResult(RESULT_UPLOAD_CLOSE);
		getActivity().finish();
	}

	@OnClick(R.id.upload_more)
	protected void onClickUploadMore(View view)
	{
		getActivity().setResult(RESULT_UPLOAD_REOPEN);
		getActivity().finish();
	}

	@OnClick(R.id.share)
	protected void onClickShare(View view)
	{
		try
		{
			AnalyticsManager.getInstance().shareEvent(getContext(), "Share_ImageUpload");
		}
		catch (Exception e)
		{
		}

		String username = UserManager.getInstance().getUser().getUsername();
		ShareManager.getInstance().share(this, ShareManager.TYPE_PROFILE, "", username);
	}

	//========================================================================
	// public Methods
	//========================================================================

	public static UploadCompleteFragment newInstance(int mode)
	{
		UploadCompleteFragment fragment = new UploadCompleteFragment();
		Bundle args = new Bundle();
		args.putInt(KEY_MODE, mode);
		fragment.setArguments(args);

		return fragment;
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private boolean isWepickMode()
	{
		return ((UploadActivity) getActivity()).getUploadMode() == UploadActivity.MODE_WEPICK;
	}

	//========================================================================
	// Constants
	//========================================================================

	public static final int RESULT_UPLOAD_CLOSE = 5000;
	public static final int RESULT_UPLOAD_REOPEN = 4000;

	public static final int MODE_UPLOAD_IMAGE = 0;
	public static final int MODE_UPLOAD_VIDEO = 1;

	public static final String KEY_MODE = "KEY_MODE";

	//========================================================================
	// Variables
	//========================================================================

	private Unbinder m_unbinder;

	@BindView(R.id.title) TextView m_titleView;
	@BindView(R.id.description) TextView m_descView;
	@BindView(R.id.upload_more) TextView m_moreView;
}
