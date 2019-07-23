package com.ogqcorp.bgh.fragment;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.TermsPoliciesAction;
import com.ogqcorp.bgh.activity.UploadActivity;
import com.ogqcorp.bgh.adapter.UploadTagAdapter;
import com.ogqcorp.bgh.gifwallpaper.GifLiveWallpaperFileUtils;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Tag;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.AppLogger;
import com.ogqcorp.bgh.system.ItemTouchHelperCallbackEx;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.bgh.upload.LoadingProgressBarDiag;
import com.ogqcorp.bgh.upload.UploadData;
import com.ogqcorp.bgh.upload.UploadService;
import com.ogqcorp.bgh.view.FlowLayoutManager.FlowLayoutManager;
import com.ogqcorp.commons.GlideApp;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.StaticViewAdapter;
import com.ogqcorp.commons.WebDialogFragment;
import com.ogqcorp.commons.annotation.CalledByReflection;
import com.ogqcorp.commons.collection.ArrayListSet;
import com.ogqcorp.commons.utils.ActivityUtils;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.KeyboardUtils;
import com.ogqcorp.commons.utils.ListenerUtils;
import com.ogqcorp.commons.utils.PathUtils;
import com.ogqcorp.commons.utils.ToastUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

public class SimpleUploadWriteFragment extends Fragment implements UploadActivity.OnKeyDownListener, RequestListener<Bitmap>
{
	//========================================================================
	// Public Methods
	//========================================================================

	public static Fragment newInstance(Uri uri, Background background, boolean iscrop)
	{
		SimpleUploadWriteFragment f = new SimpleUploadWriteFragment();
		Bundle b = new Bundle();
		b.putParcelable(KEY_URI, uri);
		b.putParcelable(KEY_BACKGROUND, background);
		b.putBoolean(KEY_CROP, iscrop);
		f.setArguments(b);
		return BaseModel.wrap(f);
	}

	private boolean isWepickMode()
	{
		return ((UploadActivity) getActivity()).getUploadMode() == UploadActivity.MODE_WEPICK;
	}

	private String getWepickId()
	{
		return ((UploadActivity) getActivity()).getWepickId();
	}

	public static class Empty
	{
		// Nothing
	}

	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		try
		{
			if (savedInstanceState != null)
			{
				final String[] tagsList = savedInstanceState.getStringArray(KEY_TAG_LIST);
				m_userTags = new ArrayListSet<>();
				m_userTags.addAll(Arrays.asList(tagsList != null ? tagsList : new String[] {}));
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onCreate Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onCreate Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_simple_upload_write, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite onViewCreated");

			m_unbinder = ButterKnife.bind(this, view);

			m_toolbar = view.findViewById(R.id.toolbar);
			initToolbar(m_toolbar);

			m_uri = getArguments().getParcelable(KEY_URI);
			m_background = getArguments().getParcelable(KEY_BACKGROUND);
			m_isCrop = getArguments().getBoolean(KEY_CROP, false);

			// 예전 시나리오 (사진 갤러리 -> 사진업로드 / 현재 사용 안함)
			if (isPickerMode() == true)
			{
				view.setAlpha(0);

				if (savedInstanceState != null)
				{
					return;
				}
				startPhotoPicker();
				return;
			}

			// 내 게시물 수정 모드
			if (isEditMode() == true)
			{
				setContent(Uri.parse(m_background.getImage().getUrl()));
				return;
			}

			// 사진 업로드 모드
			if (isShareMode() == true)
			{
				setContent(m_uri);
				requestSameUploadCheck();
				return;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onViewCreated Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onViewCreated Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		try
		{
			if (m_userTags != null && m_userTags.size() > 0)
			{
				outState.putStringArray(KEY_TAG_LIST, m_userTags.toArray(new String[m_userTags.size()]));
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onSaveInstanceState Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onSaveInstanceState Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onKeyEvent(int keyCode, KeyEvent event)
	{
		try
		{
			if (keyCode == KeyEvent.KEYCODE_BACK)
			{
				PathUtils.clearTempDirectory(getActivity(), "upload");

				if (isEditMode() == true)
				{
					safetyFinish();
				}
				else
				{
					if (getFragmentManager().getBackStackEntryCount() > 0)
					{

						FragmentManager fm = getFragmentManager();
						fm.popBackStack();

						return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onKeyEvent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onKeyEvent Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return false;
	}

	@CalledByReflection
	public void
	onClickAddTag(View v)
	{
		try
		{
			String newTag = StringUtils.deleteWhitespace(m_tagView.getText().toString());
			String[] tags = { newTag };
			addTag(tags);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onClickAddTag Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onClickAddTag Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite onDestroyView");

			m_unbinder.unbind();

			KeyboardUtils.hideSoftKeyboard(getActivity());

			if (m_loadingProgressDialog != null)
			{
				m_loadingProgressDialog.dismiss();
				m_loadingProgressDialog = null;
			}

			if (m_preview != null)
			{
				Drawable drawable = m_preview.getDrawable();
				if (drawable instanceof Animatable)
				{
					Animatable gif = (Animatable) drawable;
					if (gif != null && gif.isRunning())
						gif.stop();
				}

				m_preview.setImageResource(0);
				m_preview.destroyDrawingCache();
				m_preview.setImageBitmap(null);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onDestroyView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onDestroyView Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			String screenName = getClass().getSimpleName();
			AnalyticsManager.getInstance().screen(getContext(), screenName);
		}
		catch (Exception e)
		{
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		try
		{
			switch (requestCode)
			{
				case SELECT_PHOTO:
					if (resultCode == Activity.RESULT_OK)
					{
						getView().setAlpha(1);
						try
						{
							if (isEditMode() == false)
							{
								m_selectedFilterIndex = 0;
							}
							m_uri = data.getData();
							setContent(m_uri);

							getArguments().putParcelable(KEY_URI, m_uri);
						}
						catch (Exception e)
						{
							ToastUtils.makeErrorToast(getContext(), 0, "ERROR : " + e.toString()).show();
							getActivity().finish();
						}
					}
					else if (m_uri == null)
					{
						getActivity().finish();
					}
					break;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onActivityResult Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onActivityResult Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@OnTextChanged(R.id.title)
	protected void onTitleTextChanged(CharSequence s, int start, int before, int count)
	{
		try
		{
			String title = s.toString();

			if (TextUtils.isEmpty(title) == true)
			{
				return;
			}

			if (title.length() > m_titleLayout.getCounterMaxLength())
			{
				m_titleLayout.setError(getString(R.string.upload_content_title_long));
			}
			else
			{
				m_titleLayout.setError(null);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onTitleTextChanged Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onTitleTextChanged Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@OnTextChanged(R.id.tag)
	protected void onTagTextChanged(CharSequence s, int start, int before, int count)
	{
		try
		{
			final String str = s.toString().replaceAll(BAN, "");
			final boolean isFiltered = s.length() > str.length() ? true : false;

			if (TextUtils.isEmpty(str) == true || str.trim().length() < 1)
			{
				if (count > 0)
				{
					initTagView();
				}
				return;
			}

			String[] tags = TextUtils.split(str, "\\s+");

			if (tags.length > 1)
			{
				addTag(tags);
			}
			else
			{
				if (str.length() > m_tagLayout.getCounterMaxLength())
				{
					m_tagLayout.setError(getString(R.string.upload_content_tag_too_long));
				}
				else
				{
					if (isFiltered == true)
					{
						new Handler().postDelayed(new Runnable()
						{
							@Override
							public void run()
							{
								m_tagView.setText(str);
							}
						}, 100);
					}
					m_tagLayout.setError(null);
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onTagTextChanged Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onTagTextChanged Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	/*@OnTextChanged(value = R.id.tag, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
	protected void onAfterTagTextChanged(Editable editable)
	{
	}*/

	public void addTag(String[] tags)
	{
		boolean isAdded = false;
		boolean isDuplicate = false;
		boolean isOverLength = false;

		for (String tag : tags)
		{
			if (TextUtils.isEmpty(tag) == true)
			{
				continue;
			}

			if (m_userTags.contains(tag) == true)
			{
				isDuplicate = true;
				continue;
			}

			if (tag.length() > m_tagLayout.getCounterMaxLength())
			{
				isOverLength = true;
				continue;
			}

			isAdded = true;
			m_userTags.add(tag);
			m_uploadTagAdapter.notifyItemInserted(m_userTags.size() - 1);
		}

		initTagView();

		if (isDuplicate == true)
		{
			ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_tags_input_exist).show();
			return;
		}

		if (isOverLength)
		{
			ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_tag_too_long).show();
			return;
		}
	}

	public void initTagView()
	{
		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				if (FragmentUtils.isDestroyed(SimpleUploadWriteFragment.this) == true)
				{
					return;
				}
				m_tagView.setText(null);
				m_tagLayout.setError(null);
			}
		}, 100);
	}

	@OnClick(R.id.delete)
	public void onClickDelete(View view)
	{
		AnalyticsManager.getInstance().eventUserActionDeletePost(getContext(), "SIMPLE_UPLOAD_WRITE_PAGE");
		new MaterialDialog.Builder(getActivity())
				.content(R.string.upload_content_delete_confirm)
				.canceledOnTouchOutside(true)
				.autoDismiss(true)
				.positiveText(R.string.ok)
				.negativeText(R.string.cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						requestDeletePost();
					}
				})
				.show();
	}

	@OnClick(R.id.image_preview)
	public void onClickPreview(View view)
	{
		try
		{
			KeyboardUtils.hideSoftKeyboard(getActivity());

			((UploadActivity) getActivity()).showPreviewStepUpload(m_uri);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onClickPreview Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onClickPreview Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource)
	{
		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		return true;
	}

	@Override
	public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource)
	{
		setTextResolution();
		return false;
	}

	public void onClickDone()
	{
		try
		{
			if (TextUtils.isEmpty(checkImageValidation(m_uri)) == false || checkInputViewsValidation() == false)
				return;

			requestUpload();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onClickDone Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onClickDone Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	public void onClickUpdate()
	{
		try
		{
			if (checkInputViewsValidation() == false)
			{
				return;
			}

			requestUpdatePost();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment onClickUpdate Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onClickUpdate Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private boolean isEditMode()
	{
		return m_uri == null && m_background != null;
	}

	private boolean isPickerMode()
	{
		return m_uri == null && m_background == null;
	}

	private boolean isShareMode()
	{
		return m_uri != null && m_background == null;
	}

	private boolean isGifMode()
	{
		boolean isGif = false;

		try
		{
			if (UrlFactory.isStagingServer(getContext()) == true)
			{
				// GIF
				if (m_uri != null && m_uri.toString() != null && m_uri.toString().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION))
				{
					isGif = true;
				}
				else if (m_uri != null && m_uri.toString() != null && m_uri.toString().startsWith("content://") == true)
				{
					String MIMEType = getMIMETypeFromUri(getContext(), m_uri);

					if (MIMEType != null && MIMEType.contains("gif"))
						isGif = true;
				}

				if (isGif)
					return true;
				else
				{
					if (m_background != null && m_background.getImage() != null &&
							m_background.getImage().getUrl() != null && m_background.getImage().getUrl().toString() != null &&
							m_background.getImage().getUrl().toString().isEmpty() == false)
					{
						if (m_background.getImage().getUrl().toString().contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION))
							return true;
					}
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment isGifMode Exception");
			FirebaseCrashLog.logException(e);


			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite isGifMode Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return false;
	}

	private void startPhotoPicker()
	{
		try
		{
			final Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment startPhotoPicker Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite startPhotoPicker Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void insertValueToViews()
	{
		try
		{
			m_toolbar.setTitle(R.string.action_edit_post);

			m_titleView.setText(m_background.getTitle());
			m_contentView.setText(m_background.getDescription());

			m_titleView.setSelection(m_titleView.getText().toString().length());

			m_userTags.clear();
			Observable.from(m_background.getTagsList())
					.map(new Func1<Tag, String>()
					{
						@Override
						public String call(Tag tag)
						{
							return tag.getTag();
						}
					})
					.toBlocking()
					.subscribe(new Action1<String>()
					{
						@Override
						public void call(String s)
						{
							m_userTags.add(s);
						}
					});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment insertValueToViews Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite insertValueToViews Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void safetyFinish()
	{
		new MaterialDialog.Builder(getActivity())
				.content(R.string.upload_content_exit_confirm_update)
				.autoDismiss(true)
				.canceledOnTouchOutside(true)
				.positiveText(R.string.ok)
				.negativeText(R.string.cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						getActivity().finish();
					}
				})
				.show();
	}

	private void setContent(Uri uri)
	{
		try
		{
			if (m_userTags == null)
			{
				m_userTags = new ArrayListSet<>();
			}

			final FlowLayoutManager layoutManager = new FlowLayoutManager();
			layoutManager.setAutoMeasureEnabled(true);

			m_tagsView.setLayoutManager(layoutManager);

			final MergeRecyclerAdapter mergeAdapter = new MergeRecyclerAdapter();
			mergeAdapter.add(m_uploadTagAdapter);
			m_tagsView.setAdapter(mergeAdapter);

			final ItemTouchHelper touchHelper = new ItemTouchHelper(m_colorTouchHelperCallback);
			touchHelper.attachToRecyclerView(m_tagsView);

			m_deleteView.setVisibility(isEditMode() ? View.VISIBLE : View.GONE);

			if (isEditMode() == true)
			{
				insertValueToViews();
				m_toolbar.setNavigationOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						safetyFinish();
					}
				});

				uri = Uri.parse(m_background.getImage().getUrl());
			}

			setGlideView(uri);

			int index = new Random().nextInt(3);
			m_titleView.setHint(getTitleHint(index));
			m_contentView.setHint(getDescriptionHint(index));

			ListenerUtils.setOnClickListener(m_addTag, this, "onClickAddTag");
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment setContent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite setContent Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void setGlideView(Uri uri)
	{
		try
		{
			if (m_progressImage != null)
				m_progressImage.setVisibility(View.VISIBLE);

			if (isGifMode() == true)
			{
				RequestListener<GifDrawable> listenerGif = new RequestListener<GifDrawable>()
				{
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource)
					{
						if (m_progressImage != null)
							m_progressImage.setVisibility(View.GONE);
						return true;
					}

					@Override
					public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource)
					{
						setTextResolution();
						return false;
					}
				};

				if (uri.toString().contains(getContext().getPackageName()) == true &&
						uri.toString().contains("data") == true &&
						uri.toString().startsWith("file://") == false)
				{
					uri = Uri.parse("file://" + uri.toString());
				}

				GlideApp.with(SimpleUploadWriteFragment.this)
						.asGif()
						.load(uri)
						//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.listener(listenerGif)
						.into(m_preview);
			}
			else
			{
				if (uri.toString().contains(getContext().getPackageName()) == true &&
						uri.toString().contains("data") == true &&
						uri.toString().startsWith("file://") == false)
				{
					uri = Uri.parse("file://" + uri.toString());
				}

				GlideApp.with(SimpleUploadWriteFragment.this)
						.asBitmap()
						.load(uri)
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.listener(SimpleUploadWriteFragment.this)
						.into(m_preview);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment setGlideView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite setGlideView Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private String getTitleHint(int index)
	{
		try
		{
			switch (index)
			{
				case 0:
					return getString(R.string.tell_us_your_story);
				case 1:
					return getString(R.string.every_masterpiece_deserves_a_title);
				case 2:
				default:
					return getString(R.string.how_does_this_image_make_you_feel);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment getTitleHint Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite getTitleHint Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return "";
	}

	private String getDescriptionHint(int index)
	{
		try
		{
			switch (index)
			{
				case 0:
					return getString(R.string.what_does_this_piece_mean_to_you);
				case 1:
					return getString(R.string.every_masterpiece_deserves_a_description);
				case 2:
				default:
					return getString(R.string.how_would_you_describe_your_image);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment getDescriptionHint Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite getDescriptionHint Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return "";
	}

	private void initToolbar(Toolbar toolbar)
	{
		try
		{
			int color = getResources().getColor(R.color.black);
			toolbar.setTitle(getString(isWepickMode() ? R.string.wepick_uploading_title : R.string.upload_content_toolbar_title));
			toolbar.setTitleTextColor(ColorUtils.setAlphaComponent(color, 255));

			toolbar.setNavigationIcon(R.drawable.ic_back);
			toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			toolbar.setNavigationOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					onKeyEvent(KeyEvent.KEYCODE_BACK, null);
				}
			});

			toolbar.inflateMenu(R.menu.fragment_simple_upload_write);
			toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
			{
				@Override
				public boolean onMenuItemClick(MenuItem item)
				{
					switch (item.getItemId())
					{
						case R.id.menu_upload:
							if (isEditMode() == true)
							{
								onClickUpdate();
							}
							else
							{
								onClickDone();
							}
							break;
					}
					return true;
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment initToolbar Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite initToolbar Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void showUploadInfo()
	{
		try
		{
			View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_upload_prepare_info, null);
			TextView terms = view.findViewById(R.id.terms);
			terms.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					TermsPoliciesAction action = new TermsPoliciesAction((AppCompatActivity) getActivity());
					action.run(null, null);
				}
			});

			new MaterialDialog.Builder(getActivity())
					.customView(view, true)
					.canceledOnTouchOutside(true)
					.autoDismiss(true)
					.positiveText(R.string.ok)
					.onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							dialog.dismiss();
						}
					})
					.show();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment showUploadInfo Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite showUploadInfo Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	// 이미지 사이즈 체크 (사이즈가 MIN_SIZE_WIDTH / MIN_SIZE_HEIGHT 값보다 작은 경우 예외처리)
	private String checkImageValidation(Uri uri)
	{
		try
		{
			if (uri.toString().startsWith("http"))
			{
				return null;
			}

			if (uri.toString().contains(getContext().getPackageName()) == true &&
					uri.toString().contains("data") == true &&
					uri.toString().startsWith("file://") == false)
			{
				uri = Uri.parse("file://" + uri.toString());
			}

			final Uri tr_uri = uri;
			InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
			PathUtils.clearTempDirectory(getActivity(), "check");
			//File o_file = PathUtils.createTempFile(getActivity(), "check", ".jpg"); // original copy
			//FileUtils.copyInputStreamToFile(inputStream, o_file);
			Point size = new Point();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			options.inSampleSize = 4;
			//BitmapFactory.decodeFile(o_file.getAbsolutePath(), options);
			BitmapFactory.decodeResourceStream(getResources(), new TypedValue(), inputStream, new Rect(), options);

			size.x = options.outWidth * 4;
			size.y = options.outHeight * 4;

			if (inputStream != null)
				inputStream.close();
			inputStream = null;

			if (size.x < MIN_SIZE_WIDTH) return getString(R.string.upload_prepare_choose_fail_min_width, String.valueOf(size.x));
			if (size.y < MIN_SIZE_HEIGHT) return getString(R.string.upload_prepare_choose_fail_min_height, String.valueOf(size.y));
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment checkImageValidation Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite checkImageValidation Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);

			return e.toString();
		}
		return null;
	}

	//최종 이미지 업로드
	private void requestUpdatePost()
	{
		try
		{
			showProgressDialog();

			String _title = m_titleView.getText().toString();
			String _description = m_contentView.getText().toString();

			String _tags = TextUtils.join(" ", m_userTags);

			HashMap<String, Object> params = ParamFactory.update(_title, _description, _tags);

			Requests.authRequestByPut(UrlFactory.update(m_background.getUuid()), params, Background.class, new Response.Listener<Background>()
			{
				@Override
				public void onResponse(Background response)
				{
					m_progressDialog.dismiss();

					getActivity().setResult(RESULT_EDIT_OK);
					getActivity().finish();
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					m_progressDialog.dismiss();

					VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
					volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
					volleyErrorHandler.handleError(error);
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment requestUpdatePost Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestUpdatePost Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void requestDeletePost()
	{
		try
		{
			showProgressDialog();

			Requests.authRequestByDelete(UrlFactory.delete(m_background.getUuid()), null, Empty.class, new Response.Listener<Empty>()
			{
				@Override
				public void onResponse(Empty response)
				{
					if (FragmentUtils.isDestroyed(SimpleUploadWriteFragment.this) == true)
					{
						return;
					}

					m_progressDialog.dismiss();

					ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.detele_post_success).show();
					getActivity().setResult(RESULT_DELETE_OK);
					getActivity().finish();
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					m_progressDialog.dismiss();

					VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
					volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
					volleyErrorHandler.handleError(error);
				}
			});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment requestDeletePost Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestDeletePost Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private boolean checkInputViewsValidation()
	{
		try
		{
			String title = m_titleView.getText().toString();

			if (title.trim().length() == 0)
			{
				m_titleLayout.setError(getString(R.string.upload_content_title_empty));
				m_titleView.setText("");
				m_titleView.requestFocus();
				ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_title_empty).show();
				return false;
			}
			else if (title.length() > m_titleLayout.getCounterMaxLength())
			{
				m_titleLayout.setError(getString(R.string.upload_content_title_long));
				m_titleView.requestFocus();
				ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_title_long).show();
				return false;
			}

			String content = m_contentView.getText().toString();

			if (content.length() > m_contentLayout.getCounterMaxLength())
			{
				m_contentLayout.setError(getString(R.string.upload_content_content_long));
				m_contentView.requestFocus();
				ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_content_long).show();
				return false;
			}

			return true;
		}
		catch (Exception e)
		{
			ToastUtils.makeInfoToast(getContext(), 0, R.string.upload_content_notification_complete_failed_text2).show();

			FirebaseCrashLog.log("SimpleUploadWriteFragment checkInputViewsValidation Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite checkInputViewsValidation Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			return false;
		}
	}

	private void showProgressDialog()
	{
		m_progressDialog = new MaterialDialog.Builder(getActivity())
				.content(R.string.processing)
				.progress(true, 0)
				.cancelable(false)
				.show();
	}

	private void requestUpload()
	{
		final String title = m_titleView.getText().toString();
		final String description = m_contentView.getText().toString();

		m_uploadData.setTitle(title);
		m_uploadData.setDescription(description);
		m_uploadData.setUserTags(m_userTags);

		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite requestUpload title : " + title);
		AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite requestUpload description : " + description);

		requestUploadStart(m_uploadData);
	}

	// Function : 유사사진 비교, Google Vision or VTT TAG API로 태그 정보 가져옴
	private void requestSameUploadCheck()
	{
		try
		{
			if (m_loadingProgressDialog != null)
				return;

			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite requestSameUploadCheck");

			String title = getResources().getString(R.string.upload_content_upload_title);
			m_loadingProgressDialog = new LoadingProgressBarDiag(getActivity(), title);
			m_loadingProgressDialog.setMessage(getResources().getString(R.string.upload_same_compare_progress_description));
			m_loadingProgressDialog.show();

			final String _title = m_titleView.getText().toString();
			final String _description = m_contentView.getText().toString();

			m_uploadData = new UploadData(_title, _description, m_userTags, 11, m_uri, m_selectedFilterIndex, m_isCrop, isWepickMode() ? getWepickId() : null);
			m_uploadData.uploadRequestAnnotationLabels(getContext(), new UploadData.ProgressListener()
			{
				@Override
				public synchronized void onProgress(long uid, int progress)
				{
					try
					{
						if (FragmentUtils.isDestroyed(SimpleUploadWriteFragment.this) == true)
							return;

						if (m_loadingProgressDialog != null)
							m_loadingProgressDialog.setNewProgress(progress);
					}
					catch (Exception e)
					{
						FirebaseCrashLog.log("SimpleUploadWriteFragment requestSameUploadCheck onProgress Exception");
						FirebaseCrashLog.logException(e);

						AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestSameUploadCheck onProgress Exception");
						AppLogger.getInstance().e(AppLogger.TAG.UI, e);
					}
				}

				@Override
				public void onCompleted(long uid, boolean isSuccess, String errorMessage)
				{
				}

				@Override
				public void onCompletedSameImage(long uid, boolean isSuccess, final boolean isExists, final List<String> tags, String errorMessage)
				{
					try
					{
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite onCompletedSameImage / Success : " + isSuccess + " / Exists : " + isExists);

						if (ActivityUtils.isDestroyed(getActivity()) == true || FragmentUtils.isDestroyed(SimpleUploadWriteFragment.this) == true)
							return;

						if (m_loadingProgressDialog != null)
						{
							m_loadingProgressDialog.dismiss();
							m_loadingProgressDialog = null;
						}

						if (tags != null && tags.isEmpty() == false)
						{
							m_originTags = tags;

							String[] arg = m_originTags.toArray(new String[m_originTags.size()]);
							addTag(arg);
						}

						if (isSuccess == false)
						{
							ToastUtils.makeErrorToast(getActivity(), 0, R.string.error_has_occurred).show();
						}

						if (isExists == true)
						{
							showSameImageConfirmDialog();
						}
					}
					catch (Exception e)
					{
						FirebaseCrashLog.log("SimpleUploadWriteFragment requestSameUploadCheck onCompletedSameImage Exception");
						FirebaseCrashLog.logException(e);

						AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestSameUploadCheck onCompletedSameImage Exception");
						AppLogger.getInstance().e(AppLogger.TAG.UI, e);
					}
				}
			});
		}
		catch (Exception e)
		{
			if (ActivityUtils.isDestroyed(getActivity()) == false)
			{
				ToastUtils.makeErrorToast(getActivity(), 0, R.string.error_has_occurred).show();
			}

			FirebaseCrashLog.log("SimpleUploadWriteFragment requestSameUploadCheck Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestSameUploadCheck Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	// Function : 업로드 시, 동일 사진 비교 기능 추가
	private void requestUploadStart(UploadData uploaddata)
	{
		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite requestUploadStart");

			if (m_loadingProgressDialog != null)
			{
				m_loadingProgressDialog.dismiss();
				m_loadingProgressDialog = null;
			}

			if (ActivityUtils.isDestroyed(getActivity()) == true)
			{
				return;
			}

			UploadService.upload(getContext(), uploaddata);

			AnalyticsManager.getInstance().eventUserActionUploadFilter(getContext(), m_selectedFilterIndex);

			((UploadActivity) getActivity()).showComplete(UploadCompleteFragment.MODE_UPLOAD_IMAGE);
		}
		catch (Exception e)
		{
			if (ActivityUtils.isDestroyed(getActivity()) == false)
			{
				ToastUtils.makeErrorToast(getActivity(), 0, R.string.error_has_occurred).show();
			}

			FirebaseCrashLog.log("SimpleUploadWriteFragment requestUploadStart Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestUploadStart Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void showSameImageConfirmDialog()
	{
		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite showSameImageConfirmDialog");

			AnalyticsManager.getInstance().eventStatsSameImage(getContext(), "SAME");

			if (m_loadingProgressDialog != null)
			{
				m_loadingProgressDialog.dismiss();
				m_loadingProgressDialog = null;
			}

			new MaterialDialog.Builder(getActivity())
					.content(R.string.upload_same_compare_dialog)
					.autoDismiss(true)
					.canceledOnTouchOutside(true)
					.neutralText(R.string.upload_same_compare_license_button)
					.positiveText(R.string.ok)
					.negativeText(R.string.cancel)
					.autoDismiss(false)
					.onNegative(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							AnalyticsManager.getInstance().eventStatsSameImage(getContext(), "CANCEL");

							AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite showSameImageConfirmDialog CANCEL");

							if (dialog != null)
								dialog.dismiss();

							getActivity().finish();
							//onKeyEvent(KeyEvent.KEYCODE_BACK, null);
						}
					})
					.onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							AnalyticsManager.getInstance().eventStatsSameImage(getContext(), "UPLOAD");

							AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite showSameImageConfirmDialog UPLOAD");

							if (dialog != null)
								dialog.dismiss();

							/*if (m_originTags != null && m_originTags.isEmpty() == false)
							{
								String[] tags = m_originTags.toArray(new String[m_originTags.size()]);
								addTag(tags);
							}*/
						}
					})
					.onNeutral(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							AnalyticsManager.getInstance().eventStatsSameImage(getContext(), "LICENSE");

							AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadWrite showSameImageConfirmDialog LICENSE");
							showWebDialog();
						}
					})
					.show();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment requestSameImageUploadStart Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite requestSameImageUploadStart Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void showWebDialog()
	{
		new WebDialogFragment.Builder("http://bgh.ogqcorp.com/api/v4/documents/management")
				.setTheme(R.style.BG_Theme_Activity)
				.setLayout(R.layout.fragment_web_dialog)
				.setToolbarNavigationIcon(R.drawable.ic_back)
				.setTitle(getResources().getString(R.string.p_terms_policy_management))
				.start(getFragmentManager());
	}
	//========================================================================
	// UploadTagAdapter class
	//========================================================================

	private UploadTagAdapter m_uploadTagAdapter = new UploadTagAdapter()
	{
		@Override
		protected String getItem(int position)
		{
			return m_userTags.get(position);
		}

		@Override
		public int getItemCount()
		{
			try
			{
				if (m_userTags == null) return 0;
				return m_userTags.size();
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadWriteFragment getItemCount Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite getItemCount Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			}

			return 0;
		}

		@Override
		protected void onClickTag(View view, final String tag)
		{
			try
			{
				final int position = ((RecyclerView.LayoutParams) ((View) view.getParent()).getLayoutParams()).getViewAdapterPosition();
				if (position < 0)
				{
					m_tagsView.removeView(view);
				}

				m_userTags.remove(position);
				m_uploadTagAdapter.notifyItemRemoved(position);
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadWriteFragment onRemove Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onRemove Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			}
		}
	};

	private String getMIMETypeFromUri(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Images.Media.MIME_TYPE };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment getMIMETypeFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite getMIMETypeFromUri Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}

		return null;
	}

	private String getFilePathFromUri(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment getFilePathFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite getFilePathFromUri Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
		finally
		{
			if (cursor != null)
			{
				cursor.close();
			}
		}

		return null;
	}

	private void setTextResolution()
	{

		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		String quality = "";
		int width = 0;
		int height = 0;

		try
		{
			if (m_background != null)
			{
				width = m_background.getImage().getWidth();
				height = m_background.getImage().getHeight();
			}
			else
			{
				if (isGifMode())
				{
					Uri uri = null;
					if (m_uri.toString().contains(getContext().getPackageName()) == true &&
							m_uri.toString().contains("data") == true &&
							m_uri.toString().startsWith("file://") == false)
					{
						uri = Uri.parse("file://" + m_uri.toString());
					}
					else
						uri = m_uri;

					pl.droidsonroids.gif.GifDrawable drawable = new pl.droidsonroids.gif.GifDrawable(getContext().getContentResolver(), uri);
					width = drawable.getIntrinsicWidth();
					height = drawable.getIntrinsicHeight();
				}
				else
				{
					/*
					String filepath = m_uri.toString();

					if(m_uri.toString().startsWith("file://") == true)
					{
						if(	m_uri.toString().contains(getContext().getPackageName()) == true &&
							m_uri.toString().contains("data") == true)
						{
						}
						else
						{
							filepath = m_uri.toString().replaceFirst("file://", "");
						}
					}
					else
					{
						if(m_uri.toString().startsWith("content://") == true)
						{
							filepath = getFilePathFromUri(getContext(), m_uri);
						}
					}

					File o_file = new File(filepath);
					*/
					Uri uri = null;
					if (m_uri.toString().contains(getContext().getPackageName()) == true &&
							m_uri.toString().contains("data") == true &&
							m_uri.toString().startsWith("file://") == false)
					{
						uri = Uri.parse("file://" + m_uri);
					}
					else
					{
						uri = Uri.parse(m_uri.toString());
					}

					InputStream inputStream = getContext().getContentResolver().openInputStream(uri);

					final BitmapFactory.Options options = new BitmapFactory.Options();
					options.inJustDecodeBounds = true;
					options.inSampleSize = 4;
					//BitmapFactory.decodeFile(o_file.getAbsolutePath(), options);
					BitmapFactory.decodeResourceStream(getResources(), new TypedValue(), inputStream, new Rect(), options);
					width = options.outWidth * 4;
					height = options.outHeight * 4;

					if (inputStream != null)
						inputStream.close();
					inputStream = null;
				}
			}

			if (width == 0 && height == 0)
			{
				m_textResolution.setVisibility(View.GONE);
			}
			else
			{
				quality += width + " X " + height;
				m_textResolution.setText(quality);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadWriteFragment setTextResolution Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite setTextResolution Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	//========================================================================
	// ItemTouchHelperCallbackEx instance
	//========================================================================

	private ItemTouchHelperCallbackEx m_colorTouchHelperCallback = new ItemTouchHelperCallbackEx()
	{
		@Override
		public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
		{
			if (viewHolder instanceof StaticViewAdapter.ViewHolder)
			{
				return 0;
			}

			return super.getMovementFlags(recyclerView, viewHolder);
		}

		@Override
		protected void onMove(int fromPosition, int toPosition)
		{
			try
			{
				if (m_userTags.size() <= toPosition) return;

				if (fromPosition < toPosition)
				{
					for (int i = fromPosition; i < toPosition; i++)
					{
						Collections.swap(m_userTags, i, i + 1);
					}
				}
				else
				{
					for (int i = fromPosition; i > toPosition; i--)
					{
						Collections.swap(m_userTags, i, i - 1);
					}
				}

				m_uploadTagAdapter.notifyItemMoved(fromPosition, toPosition);
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadWriteFragment onMove Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onMove Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			}
		}

		@Override
		protected boolean onRemove(int position)
		{
			try
			{
				m_userTags.remove(position);
				m_uploadTagAdapter.notifyItemRemoved(position);
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadWriteFragment onRemove Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadWrite onRemove Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);

			}

			return true;
		}

		@Override
		protected View onGetHitTrashView()
		{
			return m_removeTagView;
		}
	};

	//========================================================================
	// Constants
	//========================================================================

	private static final String KEY_URI = "KEY_URI";
	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";
	private static final String KEY_TAG_LIST = "KEY_TAG_LIST";
	private static final String KEY_CROP = "KEY_CROP";

	public static final int RESULT_EDIT_OK = 6000;
	public static final int RESULT_DELETE_OK = 7000;

	private static final int MIN_SIZE_WIDTH = 48;
	private static final int MIN_SIZE_HEIGHT = 48;

	public static final int SELECT_PHOTO = 100;

	public static final String BAN = "[\\{\\}\\[\\][?].,;:[|]\\)[*]~`!\\^-[+]<>@#[$]%&\\\\=()'\"[/]]";

	//========================================================================
	// Variables
	//========================================================================

	@BindView(R.id.image_preview) ImageView m_preview;
	@BindView(R.id.image_preview_text) TextView m_textResolution;
	@BindView(R.id.progress_image) ProgressWheel m_progressImage;
	@BindView(R.id.title_layout) TextInputLayout m_titleLayout;
	@BindView(R.id.content_layout) TextInputLayout m_contentLayout;
	@BindView(R.id.tag_layout) TextInputLayout m_tagLayout;
	@BindView(R.id.add_tag) ImageView m_addTag;
	@BindView(R.id.scroll) NestedScrollView m_scrollView;
	@BindView(R.id.tags) RecyclerView m_tagsView;
	@BindView(R.id.remove_tag) View m_removeTagView;
	@BindView(R.id.toolbar) Toolbar m_toolbar;
	@BindView(R.id.delete) Button m_deleteView;
	@BindView(R.id.tag) TextView m_tagView;
	@BindView(R.id.content) TextView m_contentView;
	@BindView(R.id.title) TextInputEditText m_titleView;

	private Uri m_uri;
	private List<String> m_originTags;
	private ArrayListSet<String> m_userTags;
	private Background m_background;

	private MaterialDialog m_progressDialog;
	private LoadingProgressBarDiag m_loadingProgressDialog;

	private int m_selectedFilterIndex = -1;
	private boolean m_isCrop = false;

	private UploadData m_uploadData;

	private Unbinder m_unbinder;
}
