package com.ogqcorp.bgh.fragment;

import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.Report2Action;

public class ErrorDialogFragment extends DialogFragment
{
	//========================================================================
	// Constructure
	//========================================================================

	public ErrorDialogFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		m_exceptionMessage = getArguments().getString(KEY_EXCEPTION_MESSAGE);
		m_exceptionCode = getArguments().getInt(KEY_EXCEPTION_CODE);
		m_exceptionMessage = getArguments().getString(KEY_EXCEPTION_MESSAGE);
		m_buttonCallback = getArguments().getParcelable(KEY_BUTTON_CALLBACK);
	}

	@Override
	public int getTheme()
	{
		return R.style.BG_Theme_Dialog_Search;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_error, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		m_reportText.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);

		if(m_buttonCallback == null)
		{
			m_retryButton.setVisibility(View.GONE);
		}

		procExceptionMessage();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getDialog().getWindow().setStatusBarColor(0x66000000);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);
		if(m_buttonCallback != null)
		{
			m_buttonCallback.onDismiss(this);
		}
	}

	@OnClick(R.id.retry_button)
	public void onClickRetry()
	{
		if(m_buttonCallback != null)
		{
			m_buttonCallback.onRetry(this);
		}

		try
		{
			dismissAllowingStateLoss();
		}
		catch (Exception e)
		{

		}
	}

	@OnClick(R.id.report_text)
	public void onClickReport()
	{
		new Report2Action().run(getActivity(), getFragmentManager(), getView(), m_errorCode);
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void procExceptionMessage()
	{
		if (m_exceptionMessage.contains("hostname") == true)
		{
			m_errorCode = "ERR_HOSTNAME";
			setErrorMessage(R.string.error_connection, "");
		}
		else if (m_exceptionMessage.contains("ParseError") == true)
		{
			m_errorCode = "ERR_PARSE";
			setErrorMessage(R.string.error_connection, "");
		}
		else if (m_exceptionMessage.contains("ServerError") == true)
		{
			m_errorCode = "ERR_SERVER";
			if(m_exceptionCode == 401)
			{
				setErrorMessage(R.string.error_code_401, "");
			}
			else if(m_exceptionCode == 404)
			{
				setErrorMessage(R.string.error_code_404, "");
			}
			else if (m_exceptionCode >= 500)
			{
				setErrorMessage(R.string.error_code_5xx, "");
			}
			else if(m_exceptionCode >= 400)
			{
				setErrorMessage(R.string.error_code_4xx, "");
			}
			else
			{
				setErrorMessage(R.string.error_server, "");
			}
		}
		else
		{
			m_errorCode = "ERR_NETWORK";
			setErrorMessage(R.string.error_network, m_exceptionMessage);
		}

		m_errorCode += " - ERROR CODE: " + m_exceptionCode;
	}

	private void setErrorMessage(int titleRes, String message)
	{
		m_errorTitle.setText(titleRes);
		m_errorText.setText(message);
	}

	//=========================================================================
	// Static Methods
	//=========================================================================

	public static DialogFragment start(FragmentManager fragmentManager, Exception e, DialogCallback callback)
	{
		int errorCode = 0;
		if(e instanceof VolleyError)
		{
			if((((VolleyError) e).networkResponse) != null)
			{
				NetworkResponse response = ((VolleyError) e).networkResponse;
				errorCode = response.statusCode;
			}
		}
		return start(fragmentManager, e.toString(), errorCode, callback);
	}

	public static DialogFragment start(FragmentManager fragmentManager, String message, int errorCode, DialogCallback callback)
	{
		DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
		if (fragment == null)
		{
			fragment = new ErrorDialogFragment();

			Bundle args = new Bundle();
			args.putString(KEY_EXCEPTION_MESSAGE, message);
			args.putInt(KEY_EXCEPTION_CODE, errorCode);
			args.putParcelable(KEY_BUTTON_CALLBACK, callback);
			fragment.setArguments(args);

			fragment.show(fragmentManager, TAG_FRAGMENT);
			if(callback != null)
			{
				callback.onShow(fragment);
			}
		}

		return fragment;
	}

	//=========================================================================
	// Callback
	//=========================================================================

	public static class DialogCallback implements Parcelable
	{
		public DialogCallback()
		{
			// Nothing
		}

		public void onRetry(Fragment fragment)
		{
			// Nothing
		}

		public void onDismiss(Fragment fragment)
		{
			// Nothing
		}

		public void onShow(Fragment fragment)
		{
			// Nothing
		}

		@Override
		public int describeContents()
		{
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags)
		{
			// Nothing
		}

		public DialogCallback(Parcel in)
		{
			// Nothing
		}

		public static final Creator<DialogCallback> CREATOR = new Creator<DialogCallback>()
		{
			@Override
			public DialogCallback createFromParcel(Parcel in)
			{
				return new DialogCallback(in);
			}

			@Override
			public DialogCallback[] newArray(int size)
			{
				return new DialogCallback[size];
			}
		};

	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String TAG_FRAGMENT = "ERROR_DIALOG_FRAGMENT";
	private static final String KEY_BUTTON_CALLBACK = "KEY_BUTTON_CALLBACK";
	private static final String KEY_EXCEPTION_MESSAGE = "KEY_EXCEPTION_MESSAGE";
	private static final String KEY_EXCEPTION_CODE = "KEY_EXCEPTION_CODE";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.retry_button) Button m_retryButton;
	@BindView(R.id.report_text) TextView m_reportText;
	@BindView(R.id.error_icon) ImageView m_errorIcon;
	@BindView(R.id.error_title) TextView m_errorTitle;
	@BindView(R.id.error_text) TextView m_errorText;

	private DialogCallback m_buttonCallback;
	private String m_exceptionMessage;
	private int m_exceptionCode;
	private String m_errorCode;
	
	private Unbinder m_unbinder;
}
