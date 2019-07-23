package com.ogqcorp.bgh.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.DownloadAction;
import com.ogqcorp.bgh.action.Mp4PreviewAction;
import com.ogqcorp.bgh.activity.LicenseGuideActivity;
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
import com.ogqcorp.bgh.upload.UploadVideoData;
import com.ogqcorp.bgh.upload.UploadVideoService;
import com.ogqcorp.bgh.video.VideoPreviewActivity;
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

public class SimpleUploadVideoWriteFragment extends Fragment implements UploadActivity.OnKeyDownListener, RequestListener<Bitmap>
{
	//========================================================================
	// Public Methods
	//========================================================================

	public static Fragment newInstance(Uri uri, Background background, int license)
	{
		SimpleUploadVideoWriteFragment f = new SimpleUploadVideoWriteFragment();
		Bundle b = new Bundle();
		b.putParcelable(KEY_URI, uri);
		b.putParcelable(KEY_BACKGROUND, background);
		b.putInt(KEY_LICENSE, license);
		f.setArguments(b);
		return BaseModel.wrap(f);
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
				m_uploadTagsList = new ArrayListSet<>();
				m_uploadTagsList.addAll(Arrays.asList(tagsList != null ? tagsList : new String[] {}));
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onCreate Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onCreate Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_simple_upload_video_write, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onViewCreated");

			m_unbinder = ButterKnife.bind(this, view);

			m_toolbar = view.findViewById(R.id.toolbar);
			initToolbar(m_toolbar);

			m_uri = getArguments().getParcelable(KEY_URI);
			m_license = getArguments().getInt(KEY_LICENSE);
			m_background = getArguments().getParcelable(KEY_BACKGROUND);

			// 내 게시물 수정 모드
			if (isEditMode() == true)
			{
				setContent(Uri.parse(m_background.getImage().getUrl()));
				return;
			}

			// 동영상 업로드 모드
			if (isShareMode() == true)
			{
				setContent(m_uri);
				return;
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onViewCreated Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onViewCreated Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		try
		{
			if (m_uploadTagsList != null && m_uploadTagsList.size() > 0)
			{
				outState.putStringArray(KEY_TAG_LIST, m_uploadTagsList.toArray(new String[m_uploadTagsList.size()]));
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onSaveInstanceState Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onSaveInstanceState Exception");
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
				} else
				{
					getActivity().finish();
				}
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onKeyEvent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onKeyEvent Exception");
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onClickAddTag Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onClickAddTag Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();

		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onDestroyView");

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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onDestroyView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onDestroyView Exception");
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
			} else
			{
				m_titleLayout.setError(null);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onTitleTextChanged Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onTitleTextChanged Exception");
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
			} else
			{
				if (str.length() > m_tagLayout.getCounterMaxLength())
				{
					m_tagLayout.setError(getString(R.string.upload_content_tag_too_long));
				} else
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onTagTextChanged Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onTagTextChanged Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

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

			if (m_uploadTagsList.contains(tag) == true)
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
			m_uploadTagsList.add(tag);
			m_uploadTagAdapter.notifyItemInserted(m_uploadTagsList.size() - 1);
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
				if (FragmentUtils.isDestroyed(SimpleUploadVideoWriteFragment.this) == true)
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
			//((UploadActivity) getActivity()).showPreviewStepUploadVideo(m_uri);

			playPreview();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onClickPreview Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onClickPreview Exception");
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
		if (m_progressImage != null)
			m_progressImage.setVisibility(View.GONE);

		m_play.setVisibility(View.VISIBLE);

		return false;
	}

	public void onClickDone()
	{
		try
		{
			if (checkInputViewsValidation() == false)
			{
				return;
			}

			// Function : 업로드 시, 동일 사진 비교 기능 추가
			//requestSameUploadCheck();

			// Test Code
			if (m_loadingProgressDialog != null)
				return;

			m_loadingProgressDialog = new LoadingProgressBarDiag(getActivity());
			m_loadingProgressDialog.setMessage(getResources().getString(R.string.upload_same_compare_progress));
			m_loadingProgressDialog.show();

			final String _title = m_titleView.getText().toString();
			final String _description = m_contentView.getText().toString();
			final UploadVideoData uploadVideoData = new UploadVideoData(_title, _description, m_uploadTagsList, m_license, m_uri);

			requestUploadStart(uploadVideoData);

			//----
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onClickDone Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onClickDone Exception");
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onClickUpdate Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onClickUpdate Exception");
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
				} else if (m_uri != null && m_uri.toString() != null && m_uri.toString().startsWith("content://") == true)
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment isGifMode Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite isGifMode Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return false;
	}

	private void insertValueToViews()
	{
		try
		{
			m_toolbar.setTitle(R.string.action_edit_post);

			m_titleView.setText(m_background.getTitle());
			m_contentView.setText(m_background.getDescription());

			m_titleView.setSelection(m_titleView.getText().toString().length());

			m_uploadTagsList.clear();
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
							m_uploadTagsList.add(s);
						}
					});
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment insertValueToViews Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite insertValueToViews Exception");
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
			if (m_uploadTagsList == null)
			{
				m_uploadTagsList = new ArrayListSet<>();
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment setContent Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite setContent Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void setGlideView(Uri uri)
	{
		try
		{
			if (m_progressImage != null)
				m_progressImage.setVisibility(View.VISIBLE);

			if (isEditMode() == true)
			{
				GlideApp.with(SimpleUploadVideoWriteFragment.this)
						.asBitmap()
						.load(uri)
						.diskCacheStrategy(DiskCacheStrategy.DATA)
						.listener(SimpleUploadVideoWriteFragment.this)
						.into(m_preview);
			} else
			{
				Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
				m_preview.setImageBitmap(bitmap);

				if (m_progressImage != null)
					m_progressImage.setVisibility(View.GONE);

				m_play.setVisibility(View.VISIBLE);
			}
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment setGlideView Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite setGlideView Exception");
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment getTitleHint Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite getTitleHint Exception");
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment getDescriptionHint Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite getDescriptionHint Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}

		return "";
	}

	private void initToolbar(Toolbar toolbar)
	{
		try
		{
			int color = getResources().getColor(R.color.black);
			toolbar.setTitle(getString(R.string.upload_video_toolbar_title));
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
							} else
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment initToolbar Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite initToolbar Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	protected void playPreview()
	{
		if (isEditMode() == true)
		{
			DownloadAction action = new Mp4PreviewAction();
			action.setDownloadMode(1);
			action.run(SimpleUploadVideoWriteFragment.this, m_background);
		} else
		{
			VideoPreviewActivity.createVideoPreviewActivity(this, m_background, m_uri.toString());
		}
	}

	private void showUploadInfo()
	{
		try
		{
			final Intent intent = new Intent(getContext(), LicenseGuideActivity.class);
			intent.putExtra(LicenseGuideActivity.LAYOUT_RES_ID, R.layout.activity_video_upload_license_guide);
			getContext().startActivity(intent);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment showUploadInfo Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite showUploadInfo Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	private void requestUpdatePost()
	{
		try
		{
			showProgressDialog();

			String _title = m_titleView.getText().toString();
			String _description = m_contentView.getText().toString();
			String _tags = TextUtils.join(" ", m_uploadTagsList);

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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment requestUpdatePost Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite requestUpdatePost Exception");
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
					if (FragmentUtils.isDestroyed(SimpleUploadVideoWriteFragment.this) == true)
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment requestDeletePost Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite requestDeletePost Exception");
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
			} else if (title.length() > m_titleLayout.getCounterMaxLength())
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

			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment checkInputViewsValidation Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite checkInputViewsValidation Exception");
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

	private void requestUploadStart(UploadVideoData data)
	{
		try
		{
			AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoWrite requestUploadStart");

			if (m_loadingProgressDialog != null)
			{
				m_loadingProgressDialog.dismiss();
				m_loadingProgressDialog = null;
			}

			if (ActivityUtils.isDestroyed(getActivity()) == true)
				return;

			UploadVideoService.upload(getContext(), data);

			((UploadActivity) getActivity()).showComplete(UploadCompleteFragment.MODE_UPLOAD_VIDEO);
		}
		catch (Exception e)
		{
			if (ActivityUtils.isDestroyed(getActivity()) == false)
			{
				ToastUtils.makeErrorToast(getActivity(), 0, R.string.error_has_occurred).show();
			}

			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment requestUploadStart Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite requestUploadStart Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);
		}
	}

	// Function : 업로드 시, 동일 사진 비교 기능 추가
	private void requestSameImageUploadStart(final UploadVideoData uploaddata)
	{
		try
		{

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

							if (dialog != null)
								dialog.dismiss();
						}
					})
					.onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							AnalyticsManager.getInstance().eventStatsSameImage(getContext(), "UPLOAD");

							if (dialog != null)
								dialog.dismiss();

							requestUploadStart(uploaddata);
						}
					})
					.onNeutral(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
						{
							AnalyticsManager.getInstance().eventStatsSameImage(getContext(), "LICENSE");
							showWebDialog();
						}
					})
					.show();
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment requestSameImageUploadStart Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite requestSameImageUploadStart Exception");
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
			return m_uploadTagsList.get(position);
		}

		@Override
		public int getItemCount()
		{
			try
			{
				if (m_uploadTagsList == null) return 0;
				return m_uploadTagsList.size();
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadVideoWriteFragment getItemCount Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite getItemCount Exception");
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

				m_uploadTagsList.remove(position);
				m_uploadTagAdapter.notifyItemRemoved(position);
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onRemove Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onRemove Exception");
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment getMIMETypeFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite getMIMETypeFromUri Exception");
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
			FirebaseCrashLog.log("SimpleUploadVideoWriteFragment getFilePathFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite getFilePathFromUri Exception");
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
				if (m_uploadTagsList.size() <= toPosition) return;

				if (fromPosition < toPosition)
				{
					for (int i = fromPosition; i < toPosition; i++)
					{
						Collections.swap(m_uploadTagsList, i, i + 1);
					}
				} else
				{
					for (int i = fromPosition; i > toPosition; i--)
					{
						Collections.swap(m_uploadTagsList, i, i - 1);
					}
				}

				m_uploadTagAdapter.notifyItemMoved(fromPosition, toPosition);
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onMove Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onMove Exception");
				AppLogger.getInstance().e(AppLogger.TAG.UI, e);
			}
		}

		@Override
		protected boolean onRemove(int position)
		{
			try
			{
				m_uploadTagsList.remove(position);
				m_uploadTagAdapter.notifyItemRemoved(position);
			}
			catch (Exception e)
			{
				FirebaseCrashLog.log("SimpleUploadVideoWriteFragment onRemove Exception");
				FirebaseCrashLog.logException(e);

				AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoWrite onRemove Exception");
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
	private static final String KEY_LICENSE = "KEY_LICENSE";
	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";
	private static final String KEY_TAG_LIST = "KEY_TAG_LIST";

	public static final int RESULT_EDIT_OK = 6000;
	public static final int RESULT_DELETE_OK = 7000;

	public static final String BAN = "[\\{\\}\\[\\][?].,;:[|]\\)[*]~`!\\^-[+]<>@#[$]%&\\\\=()'\"[/]]";

	//========================================================================
	// Variables
	//========================================================================

	@BindView(R.id.btn_play) ImageView m_play;
	@BindView(R.id.image_preview) ImageView m_preview;
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

	private int m_license;

	private Uri m_uri;
	private ArrayListSet<String> m_uploadTagsList;
	private Background m_background;

	private MaterialDialog m_progressDialog;
	private LoadingProgressBarDiag m_loadingProgressDialog;

	private Unbinder m_unbinder;
}
