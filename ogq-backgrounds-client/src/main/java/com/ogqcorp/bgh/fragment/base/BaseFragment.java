package com.ogqcorp.bgh.fragment.base;

import androidx.fragment.app.Fragment;
import android.view.KeyEvent;

import com.ogqcorp.commons.TabStackHelper.TabFragmentListener;
import com.ogqcorp.commons.utils.CallbackUtils;

public abstract class BaseFragment extends Fragment implements TabFragmentListener
{
	//=========================================================================
	// Protected Methods
	//=========================================================================

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onStart()
	{
		super.onStart();
	}

	@Override
	public void onScrollTop()
	{
		// Nothing
	}

	@Override
	public void onRelease()
	{
		// Nothing
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	public boolean dispatchKeyEvent(KeyEvent event)
	{
		return false;
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void onOpenBackground(Fragment sourceFragment)
	{
		final Callback callback = CallbackUtils.getCallback(this, Callback.class);
		if (callback != null) callback.onOpenBackground(sourceFragment);
	}

	protected void onOpenBackgrounds(String dataUrl)
	{
		final Callback callback = CallbackUtils.getCallback(this, Callback.class);
		if (callback != null) callback.onOpenBackgrounds(dataUrl);
	}

	//=========================================================================
	// Callback
	//=========================================================================

	public interface Callback
	{
		void onOpenBackground(Fragment sourceFragment);

		void onOpenBackgrounds(String dataUrl);
	}
}