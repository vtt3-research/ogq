package com.ogqcorp.bgh.fragment.tag;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.collection.CollectionCreateDialogFragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class TagFollowGuideDialogFragment extends DialogFragment
{
	//========================================================================
	// Constructure
	//========================================================================

	public TagFollowGuideDialogFragment()
	{
		// Nothing
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	public static DialogFragment start(FragmentManager fragmentManager, DialogCallback callback)
	{
		DialogFragment fragment = (DialogFragment) fragmentManager.findFragmentByTag(TAG_FRAGMENT);
		if (fragment == null)
		{
			Bundle args = new Bundle();
			args.putParcelable(KEY_CALLBACK, callback);

			fragment = new TagFollowGuideDialogFragment();
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

		m_callback = getArguments().getParcelable(KEY_CALLBACK);
	}

	@Override
	public int getTheme()
	{
		return R.style.BG_Theme_Dialog_Transparent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_tag_follow_guide, container, false);
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

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				dismiss();
			}
		}, 1500);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		return new Dialog(getActivity(), getTheme())
		{
			@Override
			public void onBackPressed()
			{
				TagFollowGuideDialogFragment.this.dismiss();
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

		if (m_callback != null)
		{
			m_callback.onDismiss(this);
		}
	}

	@OnClick(R.id.container)
	public void onClickOutSide()
	{
		dismiss();
	}

	//=========================================================================
	// Callback
	//=========================================================================
	public static class DialogCallback implements Parcelable
	{
		public static final Creator<CollectionCreateDialogFragment.DialogCallback> CREATOR = new Creator<CollectionCreateDialogFragment.DialogCallback>()
		{
			@Override
			public CollectionCreateDialogFragment.DialogCallback createFromParcel(Parcel in)
			{
				return new CollectionCreateDialogFragment.DialogCallback(in);
			}

			@Override
			public CollectionCreateDialogFragment.DialogCallback[] newArray(int size)
			{
				return new CollectionCreateDialogFragment.DialogCallback[size];
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

	public static final String KEY_CALLBACK = "KEY_CALLBACK";

	private static final String TAG_FRAGMENT = "TagFollowGuideDialogFragment";

	//=========================================================================
	// Variables
	//=========================================================================

	private Unbinder m_unbinder;

	private DialogCallback m_callback;

	@BindView(R.id.content) TextView m_contentView;
	@BindView(R.id.link_container) View m_containerView;
}