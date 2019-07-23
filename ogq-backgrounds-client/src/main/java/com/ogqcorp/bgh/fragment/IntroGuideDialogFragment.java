package com.ogqcorp.bgh.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.commons.WebDialogFragment;

public class IntroGuideDialogFragment extends DialogFragment
{
	//========================================================================
	// Constructure
	//========================================================================

	public IntroGuideDialogFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	public static DialogFragment start(FragmentManager fragmentManager, int resId, DialogCallback callback)
	{
		DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
		if (fragment == null)
		{
			fragment = new IntroGuideDialogFragment();

			Bundle args = new Bundle();
			args.putInt(KEY_RES_ID, resId);
			args.putParcelable(KEY_CALLBACK, callback);

			fragment.setArguments(args);
			fragment.show(fragmentManager, TAG_FRAGMENT);

			if (callback != null)
			{
				callback.onShow(fragment);
			}
		}

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments().isEmpty())
		{
			dismiss();
		}

		m_resId = getArguments().getInt(KEY_RES_ID);
		m_callback = getArguments().getParcelable(KEY_CALLBACK);
	}

	//=========================================================================
	// Constants
	//=========================================================================

	@Override
	public int getTheme()
	{
		//return R.style.BG_Theme_Dialog_Guide;
		return R.style.BG_Theme_Dialog_Search;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(m_resId, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			getDialog().getWindow().setStatusBarColor(0x66000000);
		}

		TextView textView = view.findViewById(R.id.description);

		if (textView != null)
		{
			final String content = textView.getText().toString();
			final String word = textView.getTag().toString();

			int start = content.lastIndexOf(word);
			int end = start + word.length();

			ClickableSpan clickableSpan = new ClickableSpan()
			{
				@Override
				public void onClick(View view)
				{
					switch (m_resId)
					{
						case R.layout.fragment_intro_guide_terms:
							showWebDialog(UrlFactory.TERMS_URL, word);
							break;
						case R.layout.fragment_intro_guide_privacy:
							showWebDialog(UrlFactory.PRIVACY_URL, word);
							break;
						case R.layout.fragment_intro_guide_management:
							showWebDialog(UrlFactory.MANAGEMENT_URL, word);
							break;
					}
				}
			};

			try
			{
				Spannable span = (Spannable) textView.getText();
				span.setSpan(clickableSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
				span.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				span.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
				span.setSpan(new ForegroundColorSpan(0xFF0A0A0A), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

				textView.setMovementMethod(LinkMovementMethod.getInstance());
			}
			catch (Exception e)
			{
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new Dialog(getActivity(), getTheme()){
			@Override
			public void onBackPressed() {
				IntroGuideDialogFragment.this.dismiss();
				IntroGuideDialogFragment.this.getActivity().finish();
			}
		};
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		try {
			FragmentTransaction ft = manager.beginTransaction();
			ft.add(this, tag);
			ft.commitAllowingStateLoss();
		} catch (IllegalStateException e) {
			//Nothing
		}
	}


	@Override
	public void onDismiss(DialogInterface dialog)
	{
		super.onDismiss(dialog);
		m_callback.onDismiss(this);
	}

	@OnClick(R.id.close)
	public void onClose()
	{
		m_callback.onClose(this);
		dismiss();
	}

	@OnClick(R.id.agree)
	public void onPositive()
	{
		m_callback.onPositive(this);
		dismiss();
	}

	private void showWebDialog(String url, String title)
	{
		new WebDialogFragment.Builder(url)
				.setTheme(R.style.BG_Theme_Activity)
				.setLayout(R.layout.fragment_web_dialog)
				.setToolbarNavigationIcon(R.drawable.ic_back)
				.setTitle(title)
				.start(getActivity().getSupportFragmentManager());
	}

	//=========================================================================
	// Callback
	//=========================================================================
	public static class DialogCallback implements Parcelable
	{
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

		public DialogCallback()
		{
			// Nothing
		}

		public DialogCallback(Parcel in)
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

		public void onClose(Fragment fragment)
		{
			// Nothing
		}

		public void onPositive(Fragment fragment)
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

	}

	//=========================================================================
	// Constants
	//=========================================================================

	private static final String KEY_RES_ID = "KEY_RES_ID";
	private static final String KEY_CALLBACK = "KEY_CALLBACK";

	private static final String TAG_FRAGMENT = "IntroGuideDialogFragment";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.agree) Button m_agreeBtn;
	@BindView(R.id.close) ImageButton m_closeBtn;

	private int m_resId;
	private Unbinder m_unbinder;
	private DialogCallback m_callback;
}