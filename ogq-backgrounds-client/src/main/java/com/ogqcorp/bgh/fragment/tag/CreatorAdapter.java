package com.ogqcorp.bgh.fragment.tag;

import org.jetbrains.annotations.Nullable;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.data.SimpleCreator;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.utils.ListenerUtils;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class CreatorAdapter extends RecyclerView.Adapter<CreatorAdapter.ViewHolder>
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public int getItemViewType(int position)
	{
		SimpleCreator creator = getItem(position);

		if (creator.getUsername().equals(KEY_UPLOAD))
		{
			return R.layout.item_tag_upload;
		}
		else
		{
			return R.layout.item_creator;
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false), this);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		bindViewHolder(holder.itemView.getContext(), getItem(position), holder);
	}

	//=========================================================================
	// Public Methods
	//=========================================================================

	@CalledByReflection
	public void onClickCreator(View view)
	{
		SimpleCreator SimpleCreator = (SimpleCreator) view.getTag();
		onClickCreator(view, SimpleCreator);
	}

	//=========================================================================
	// Protected Methods
	//=========================================================================

	protected void bindViewHolder(Context context, SimpleCreator creator, ViewHolder viewHolder)
	{
		try
		{
			viewHolder.m_card.setTag(creator);

			if (creator.getUsername().equals(KEY_UPLOAD))
			{
				viewHolder.m_upload.setTag(creator);
			}
			else
			{
				//viewHolder.m_job.setText("PHOTOGRAPHER");
				viewHolder.m_name.setText(creator.getName());

				if (TextUtils.isEmpty(creator.getAvataurl()) == false)
				{
					GlideApp.with(context)
							.asBitmap()
							.load(creator.getAvataurl())
							.into(viewHolder.m_profile);
				}

				if (TextUtils.isEmpty(creator.getThumbnail()) == false)
				{
					GlideApp.with(context)
							.asBitmap()
							.load(creator.getThumbnail())
							.into(viewHolder.m_image);
				}
			}
		}
		catch (Exception e)
		{
			// Nothing
		}
	}

	//=========================================================================
	// Protected Abstract Methods
	//=========================================================================

	protected abstract SimpleCreator getItem(int position);

	protected abstract void onClickCreator(View view, SimpleCreator SimpleCreator);

	//=========================================================================
	// Constants
	//=========================================================================

	public static final String KEY_UPLOAD = "KEY_UPLOAD";

	//=========================================================================
	// ViewHolder
	//=========================================================================

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		public ViewHolder(View view, Object target)
		{
			super(view);
			ButterKnife.bind(this, view);

			ListenerUtils.setOnClickListener(view, R.id.card, target, "onClickCreator");
			ListenerUtils.setOnClickListener(view, R.id.upload, target, "onClickCreator");
		}

		@Nullable @BindView(R.id.card) View m_card;
		//@Nullable @BindView(R.id.job) TextView m_job;
		@Nullable @BindView(R.id.name) TextView m_name;
		@Nullable @BindView(R.id.image) ImageView m_image;
		@Nullable @BindView(R.id.profile_image) ImageView m_profile;
		@Nullable @BindView(R.id.upload) Button m_upload;

	}

	//========================================================================
	// Variables
	//========================================================================

}