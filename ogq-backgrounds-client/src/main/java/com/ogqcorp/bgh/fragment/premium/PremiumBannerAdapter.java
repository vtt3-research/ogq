package com.ogqcorp.bgh.fragment.premium;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import java.util.List;

import com.bumptech.glide.Glide;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.data.TopBanner;
import com.ogqcorp.commons.utils.ToastUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.viewpager.widget.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class PremiumBannerAdapter extends PagerAdapter
{
	public PremiumBannerAdapter(Context context, List<TopBanner> data) {
		m_context = context;
		m_data = data;
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public Object instantiateItem(ViewGroup container, final int position)
	{
		LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.item_premium_banner, null);

		ImageView imageView = view.findViewById(R.id.image);
		imageView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				try
				{
					TopBanner banner = m_data.get(position);
					Intent i = new Intent(Intent.ACTION_VIEW);
					Uri u = Uri.parse(banner.getLink());
					i.setData(u);
					m_context.startActivity(i);

					if (banner.getLinkType().equals(TopBanner.TYPE_IFRAME))
					{
						ToastUtils.makeInfoToast(m_context, Toast.LENGTH_SHORT, R.string.banner_click_message).show();
					}
				}
				catch (Exception e)
				{
					ToastUtils.makeInfoToast(m_context, Toast.LENGTH_SHORT, R.string.error_code_unknown).show();
				}
			}
		});

		Glide.with(m_context).load(m_data.get(position).getImage().getUrl()).into(imageView);
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View) object);
	}

	@Override
	public int getCount()
	{
		return m_data.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == object;
	}

	//=========================================================================
	// Variables
	//=========================================================================

	Context m_context;
	List<TopBanner> m_data;

}