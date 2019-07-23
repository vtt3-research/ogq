package com.ogqcorp.bgh.fragment;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ogqcorp.bgh.R;
import com.ogqcorp.commons.GlideApp;

public class PopupDialogFragment extends DialogFragment
{
	//========================================================================
	// Constructure
	//========================================================================

	public PopupDialogFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	public static DialogFragment start(FragmentManager fragmentManager, String imageUrl, DialogCallback callback)
	{
		DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
		if (fragment == null)
		{
			fragment = new PopupDialogFragment();

			Bundle args = new Bundle();
			args.putString(KEY_IMAGE_URL, imageUrl);
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

		m_imageUrl = getArguments().getString(KEY_IMAGE_URL);
		m_callback = getArguments().getParcelable(KEY_CALLBACK);
	}

	@Override
	public int getTheme()
	{
		return R.style.BG_Theme_Dialog_Search;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_popup_dialog, container, false);
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

		GlideApp.with(this)
				.load(m_imageUrl)
				.transition(withCrossFade(300))
				.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
				.thumbnail(0.2f)
				.fitCenter()
				.into(m_imageView);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		return new Dialog(getActivity(), getTheme())
		{
			@Override
			public void onBackPressed()
			{
				PopupDialogFragment.this.dismiss();
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
	public void show(FragmentManager manager, String tag)
	{
		try
		{
			FragmentTransaction ft = manager.beginTransaction();
			ft.add(this, tag);
			ft.commitAllowingStateLoss();
		}
		catch (IllegalStateException e)
		{
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

	@OnClick(R.id.dont_show)
	public void onClickDontShow()
	{
		m_callback.onClickDontShow(this);
		dismiss();
	}

	@OnClick(R.id.image)
	public void onClickLink()
	{
		m_callback.onClickLink(this);
		dismiss();
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

		public void onClickDontShow(Fragment fragment)
		{
			// Nothing
		}

		public void onClickLink(Fragment fragment)
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

	private static final String KEY_IMAGE_URL = "KEY_IMAGE_URL";
	private static final String KEY_CALLBACK = "KEY_CALLBACK";

	private static final String TAG_FRAGMENT = "PopupDialogFragment";

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(R.id.image) ImageView m_imageView;
	@BindView(R.id.close) TextView m_closeBtn;
	@BindView(R.id.dont_show) TextView m_dontShowBtn;

	private Unbinder m_unbinder;
	private String m_imageUrl;
	private DialogCallback m_callback;
}