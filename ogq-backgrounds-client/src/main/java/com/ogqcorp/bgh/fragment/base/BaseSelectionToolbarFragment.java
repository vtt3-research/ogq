package com.ogqcorp.bgh.fragment.base;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.data.Background;

public abstract class BaseSelectionToolbarFragment extends BaseRecyclerFragmentEx
{
	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		if (savedInstanceState != null)
		{
			m_isSelectionMode = savedInstanceState.getBoolean(KEY_SELECTION_MODE);
			m_selectedBackgrounds = savedInstanceState.getParcelableArrayList(KEY_SELECTED_BACKGROUNDS);

			if (m_isSelectionMode == true)
			{
				setEnableSelectionToolbar(true, false);
				notifySelectionDataChanged();
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putBoolean(KEY_SELECTION_MODE, m_isSelectionMode);
		outState.putParcelableArrayList(KEY_SELECTED_BACKGROUNDS, m_selectedBackgrounds);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		removeSelectionToolbar();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
		{
			if (m_isSelectionMode == true)
			{
				setEnableSelectionToolbar(false, true);
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	//========================================================================
	// Protected Methodds
	//========================================================================

	protected void onRemoveSelections()
	{
		getBackgroundsList().removeAll(m_selectedBackgrounds);
		m_selectedBackgrounds.clear();
		notifySelectionDataChanged();

		if (m_selectionToolbar != null) m_selectionToolbar.setTitle(String.valueOf(m_selectedBackgrounds.size()));
	}

	protected void onSelectBackground(View view, Background background)
	{
		if (isSelected(background) == true)
		{
			m_selectedBackgrounds.remove(background);
		}
		else
		{
			m_selectedBackgrounds.add(background);
		}

		notifySelectionDataChanged();

		m_selectionToolbar.setTitle(String.valueOf(m_selectedBackgrounds.size()));
	}

	protected boolean isSelected(Background background)
	{
		return m_selectedBackgrounds.contains(background);
	}

	protected void setEnableSelectionToolbar(boolean enable, boolean clearSelections)
	{
		if (enable == true)
		{
			m_isSelectionMode = true;

			if (clearSelections == true) m_selectedBackgrounds.clear();

			addSelectionToolbar();
		}
		else
		{
			m_isSelectionMode = false;

			m_selectedBackgrounds.clear();
			notifySelectionDataChanged();
			removeSelectionToolbar();
		}
	}

	protected List<Background> getSelectedBackgrounds()
	{
		return m_selectedBackgrounds;
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void addSelectionToolbar()
	{
		if (getActivity().findViewById(R.id.selection_toolbar) != null) return;

		ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.content);
		m_selectionToolbar = (Toolbar) getLayoutInflater().inflate(R.layout.selection_toolbar, container, false);
		m_selectionToolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				setEnableSelectionToolbar(false, true);
			}
		});
		m_selectionToolbar.setNavigationIcon(R.drawable.ic_back);
		m_selectionToolbar.inflateMenu(R.menu.selection_toolbar);
		m_selectionToolbar.setTitle(String.valueOf(m_selectedBackgrounds.size()));
		m_selectionToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId())
				{
					case R.id.action_check_none:
						m_selectedBackgrounds.clear();
						notifySelectionDataChanged();
						m_selectionToolbar.setTitle(String.valueOf(m_selectedBackgrounds.size()));
						return true;

					case R.id.action_check_all:
						m_selectedBackgrounds = new ArrayList<>(getBackgroundsList());
						notifySelectionDataChanged();
						m_selectionToolbar.setTitle(String.valueOf(m_selectedBackgrounds.size()));
						return true;

					case R.id.action_remove_all:
						onActionRemoveSelections(m_selectedBackgrounds);
						return true;
				}

				return false;
			}
		});
		container.addView(m_selectionToolbar);
	}

	private void removeSelectionToolbar()
	{
		ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.content);
		container.removeView(m_selectionToolbar);
		m_selectionToolbar = null;
	}

	//========================================================================
	// Abstract Methods
	//========================================================================

	protected abstract void notifySelectionDataChanged();

	protected abstract List<Background> getBackgroundsList();

	protected abstract void onActionRemoveSelections(List<Background> selections);

	//========================================================================
	// Constants
	//========================================================================

	private static final String KEY_SELECTION_MODE = "KEY_SELECTION_MODE";
	private static final String KEY_SELECTED_BACKGROUNDS = "KEY_SELECTED_BACKGROUNDS";

	//========================================================================
	// Variables
	//========================================================================

	private Toolbar m_selectionToolbar;
	private ArrayList<Background> m_selectedBackgrounds = new ArrayList<>();

	protected boolean m_isSelectionMode = false;
}
