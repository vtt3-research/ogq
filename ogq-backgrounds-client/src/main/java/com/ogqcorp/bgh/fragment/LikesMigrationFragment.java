package com.ogqcorp.bgh.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.manager.FavoritesManager;
import com.ogqcorp.bgh.spirit.manager.LikesManager;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.GlideUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public class LikesMigrationFragment extends Fragment
{
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_likes_migration, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		if (UserManager.getInstance().getUser().getAvatar() != null)
		{
			GlideUtils.setImage(this, UserManager.getInstance().getUser().getAvatar())
					.into(m_profilePhotoView);
		}
		m_nickNameView.setText(UserManager.getInstance().getUser().getName());

		migrateFavoritesToLikes();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();
	}

	private List<String> getIdListFromBackgrounds(List<Background> backgroundList)
	{
		final List<String> idList = new ArrayList<>();
		for (Background background : backgroundList)
		{
			idList.add(background.getUuid());
		}
		return idList;
	}

	private void migrateFavoritesToLikes()
	{
		final List<Background> favorites = FavoritesManager.getInstance().getBackgroundsList();

		LikesManager.getInstance().init(getContext());

		final HashMap<String, Object> params = ParamFactory.likedObjectIdList(getIdListFromBackgrounds(favorites));

		Requests.authRequestByPost(UrlFactory.liked(), params, JSONObject.class, new Response.Listener<JSONObject>()
		{
			@Override
			public void onResponse(JSONObject response)
			{
				if (FragmentUtils.isDestroyed(LikesMigrationFragment.this) == true)
					return;

				ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, getString(R.string.auth_login_success)).show();
				((AuthActivity) getActivity()).complete();
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(LikesMigrationFragment.this) == true)
					return;

				ToastUtils.makeErrorToast(getContext(), Toast.LENGTH_SHORT, getString(R.string.auth_login_fail)).show();
				((AuthActivity) getActivity()).error();
			}
		});
	}

	public static LikesMigrationFragment newInstance()
	{
		return new LikesMigrationFragment();
	}

	@BindView(R.id.profile_photo) ImageView m_profilePhotoView;
	@BindView(R.id.nickname) TextView m_nickNameView;

	private Unbinder m_unbinder;
}
