package com.ogqcorp.bgh.fragment;

import java.util.List;
import java.util.Random;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.gallery.GalleryFragment;
import com.ogqcorp.bgh.spirit.data.Splash;
import com.ogqcorp.bgh.spirit.data.Splashs;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.system.SplashManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.PreventDoubleTap;
import com.ogqcorp.commons.TabStackHelper;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public class SplashFragment extends Fragment
{

	public SplashFragment()
	{
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_splash, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		m_unbinder = ButterKnife.bind(this, view);

		m_splashs = SplashManager.getInstance().getData();

		// Splash 이미지가 없는 경우
		if (hasNoSplash() == true)
		{
			finish();
		}
		else
		{
			initView();
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((AppCompatActivity) getActivity()).getSupportActionBar().hide();
	}

	@Override
	public void onStop()
	{
		super.onStop();
		((AppCompatActivity) getActivity()).getSupportActionBar().show();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	@OnClick({ R.id.title, R.id.name, R.id.next })
	public void onClick(View view)
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == false)
			return;

		if (m_splash == null)
			return;

		if (m_splash.isGalleryType())
		{
			showGallery();
		}
		else if (m_splash.isImageType())
		{
			showBackground();
		}
	}

	//========================================================================
	// Public Method
	//========================================================================

	public static Fragment newInstance()
	{
		Fragment fragment = new SplashFragment();
		return fragment;
	}

	//========================================================================
	// Private Method
	//========================================================================

	private void showGallery()
	{
		Fragment fragment = GalleryFragment.newInstance(m_splash.getId());
		TabStackHelper tabStackHelper = AbsMainActivity.getTabStackHelper(this);
		tabStackHelper.onBackPressed();
		tabStackHelper.showFragment(fragment);
	}

	private void showBackground()
	{
		try
		{
			String id = m_splash.getId();
			String server = PreferencesManager.getInstance().getCurrentServerUrl(getContext());
			String dataUrl = server + "/v4/backgrounds/" + id;

			final Fragment fragment = BackgroundPageFragment.newInstance(dataUrl);
			new Handler().post(new Runnable()
			{
				@Override
				public void run()
				{
					TabStackHelper tabStackHelper = AbsMainActivity.getTabStackHelper(SplashFragment.this);
					tabStackHelper.onBackPressed();
					tabStackHelper.showFragment(fragment);
				}
			});
		}
		catch (Exception e)
		{
			ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, R.string.error_has_occurred).show();
		}
	}

	private void initView()
	{
		try
		{
			List<Splash> list = m_splashs.getSplashList();

			int size = list.size();
			int rand = new Random().nextInt(size);
			m_splash = list.get(rand);

			if (m_splash == null || m_splash.isGalleryType() == false)
				m_splash = m_splashs.getDefault();

			String url = m_splash.getCoverUrl();
			if (m_splash.isGalleryType() == true)
			{
				url += "?type=h1280";
			}

			GlideApp.with(this)
					.asBitmap()
					.load(url)
					.transition(GenericTransitionOptions.with(R.anim.abc_fade_in))
					.format(DecodeFormat.PREFER_ARGB_8888)
					.thumbnail(0.3f)
					.diskCacheStrategy(DiskCacheStrategy.DATA)
					.listener(new RequestListener<Bitmap>()
					{
						@Override
						public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
						{
							if (FragmentUtils.isDestroyed(SplashFragment.this) == false)
							{
								finish();
							}

							return false;
						}

						@Override
						public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
						{
							if (FragmentUtils.isDestroyed(SplashFragment.this) == false)
							{
								m_dim.setVisibility(View.VISIBLE);
							}

							return false;
						}
					})
					.into(m_image);

			m_name.setText(m_splash.getNmae());
			startTimer();
		}
		catch (Exception e)
		{
			finish();
		}
	}

	private void startTimer()
	{
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (FragmentUtils.isDestroyed(SplashFragment.this) == true)
					return;

				finish();
			}
		}, 1500);
	}

	private boolean hasNoSplash()
	{
		try
		{
			if (m_splashs.getDefault() == null && m_splashs.getSplashList().isEmpty() == true)
				return true;

			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}

	private void finish()
	{
		if (FragmentUtils.isDestroyed(this) == true)
			return;

		if (m_isFinishing == false)
		{
			m_isFinishing = true;
			getActivity().onBackPressed();
		}
	}

	//=========================================================================
	// Variables
	//=========================================================================

	private Unbinder m_unbinder;

	private Splash m_splash;
	private Splashs m_splashs;

	private boolean m_isFinishing;

	@BindView(R.id.image) ImageView m_image;
	@BindView(R.id.name) TextView m_name;
	@BindView(R.id.dim_view) View m_dim;
}
