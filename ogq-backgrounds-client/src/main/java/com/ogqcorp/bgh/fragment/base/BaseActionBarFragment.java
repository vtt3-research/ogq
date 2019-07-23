package com.ogqcorp.bgh.fragment.base;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.system.MainActionBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

public abstract class BaseActionBarFragment extends BaseFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);
		initActionBarHeight(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		toolbar = getActivity().findViewById(R.id.toolbar);
		onInitActionBar();
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void onInitActionBar()
	{
		setActionBarAlpha(isOverlayActionBar() ? 0 : 255);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		{
			final int elevation = getResources().getDimensionPixelSize(R.dimen.appbar_elevation);
			toolbar.setElevation(elevation);
		}
	}

	protected boolean isOverlayActionBar()
	{
		return false;
	}

	protected Toolbar getToolbar()
	{
		return toolbar;
	}

	protected int getActionBarHeight()
	{
		return m_actionBarHeight;
	}

	protected int calculateAlpha(int scrollPosition, int headerHeight, boolean isOver)
	{
		if (isOver == true)
		{
			return 255;
		}
		if (headerHeight == 0)
		{
			return 0;
		}
		else if (headerHeight <= m_actionBarHeight)
		{
			return 255;
		}

		headerHeight = headerHeight - m_actionBarHeight;

		return (int) Math.min(Math.max(scrollPosition, 0) / (float) headerHeight * 255, 255);
	}

	protected void setActionBarAlpha(int alpha)
	{
		MainActionBar.setAlpha(toolbar, alpha);
	}

	protected void setActionBarAlphaWithoutTitleAlpha(int alpha)
	{
		MainActionBar.setAlphaWithoutTitleAlpha(toolbar, alpha);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initActionBarHeight(Context context)
	{
		final TypedArray typedArray = context.obtainStyledAttributes(new int[] { R.attr.actionBarSize });
		m_actionBarHeight = typedArray.getDimensionPixelSize(0, 0);
		typedArray.recycle();
	}

	//=========================================================================
	// Variables
	//=========================================================================

	private int m_actionBarHeight;
	private Toolbar toolbar;
}