package com.ogqcorp.bgh.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ogqcorp.bgh.R;

public class NoticeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notice);

		final Toolbar toolbar = findViewById(R.id.toolbar);
		initToolbar(toolbar);

		list = findViewById(R.id.content);
		list.setDividerHeight(0);
		adapter = new HelpAdapter(this);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);

	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	public static Intent createIntent(Context context)
	{
		return new Intent(context.getApplicationContext(), NoticeActivity.class);
	}

	public void setTitle(int res)
	{
		final Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(res);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void initToolbar(final Toolbar toolbar)
	{
		toolbar.setTitle(R.string.p_settings);
		toolbar.setNavigationIcon(R.drawable.ic_back);
		toolbar.setNavigationOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onBackPressed();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		if (selected == position)
			selected = -1;
		else
			selected = position;
		((BaseAdapter) list.getAdapter()).notifyDataSetChanged();
	}

	public class HelpAdapter extends BaseAdapter
	{
		HelpAdapter(@NonNull Context context)
		{
			super();
		}

		@Override
		public int getCount()
		{
			return 5;
		}

		@Override
		public Object getItem(int position)
		{
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
				convertView = LayoutInflater.from(NoticeActivity.this).inflate(R.layout.item_notice, null);

			TextView answer = convertView.findViewById(R.id.ans);
			TextView question = convertView.findViewById(R.id.question);
			ImageView arrow = convertView.findViewById(R.id.arrow);

			//  question.setText();
			//	answer.setText(item.getAnswer());

			if (selected == position)
			{
				answer.findViewById(R.id.ans).setVisibility(View.VISIBLE);
				arrow.setImageResource(R.drawable.ic_arrow_close);
			} else
			{
				answer.findViewById(R.id.ans).setVisibility(View.GONE);
				arrow.setImageResource(R.drawable.ic_arrow_open);
			}

			return convertView;
		}
	}

	//========================================================================
	// Constants
	//========================================================================

	//=========================================================================
	// Variables
	//=========================================================================
	private ListView list;
	private HelpAdapter adapter;
	private List<String> strList;
	private int selected = -1;
}
