package com.ogqcorp.bgh.fragment;

import static com.ogqcorp.bgh.adapter.CommentsAdapter.COMMENT_EMPHASIZE_LIMIT;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.action.UserReportAction;
import com.ogqcorp.bgh.activity.AbsMainActivity;
import com.ogqcorp.bgh.activity.AuthActivity;
import com.ogqcorp.bgh.adapter.CommentsAdapter;
import com.ogqcorp.bgh.fragment.base.BaseActionBarFragment;
import com.ogqcorp.bgh.model.BaseModel;
import com.ogqcorp.bgh.model.CommentsModel;
import com.ogqcorp.bgh.model.CommentsModelData;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.Background;
import com.ogqcorp.bgh.spirit.data.Comment;
import com.ogqcorp.bgh.spirit.data.CommentExtData;
import com.ogqcorp.bgh.spirit.data.Comments;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.system.PageScrollAdapter;
import com.ogqcorp.bgh.system.SpannableUtils;
import com.ogqcorp.bgh.system.VolleyErrorHandler;
import com.ogqcorp.bgh.user.UserInfoFragment;
import com.ogqcorp.commons.MergeRecyclerAdapter;
import com.ogqcorp.commons.PreventDoubleTap;
import com.ogqcorp.commons.SimpleTextWatcher;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.KeyboardUtils;
import com.ogqcorp.commons.utils.ToastUtils;
import com.pnikosis.materialishprogress.ProgressWheel;

public final class CommentsFragment extends BaseActionBarFragment
{
	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getArguments() != null)
		{
			m_background = getArguments().getParcelable(KEY_BACKGROUND);
		}

		m_data = CommentsModel.getInstance().get(this, new BaseModel.DataCreator<CommentsModelData>()
		{
			@Override
			public CommentsModelData newInstance()
			{
				return new CommentsModelData();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_comments, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		m_unbinder = ButterKnife.bind(this, view);

		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		initToolbar(view);

		m_layoutManager = new GridLayoutManager(getActivity(), 1, LinearLayoutManager.VERTICAL, true);

		m_mergeAdapter = new MergeRecyclerAdapter();
		m_mergeAdapter.add(m_adapter);
		m_mergeAdapter.add(getLayoutInflater(), R.layout.item_progress);

		DividerItemDecoration horizontalDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
		Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(), R.drawable.horizontal_divider);
		horizontalDecoration.setDrawable(horizontalDivider);

		m_listView.setAdapter(m_mergeAdapter);
		m_listView.setLayoutManager(m_layoutManager);
		m_listView.addOnScrollListener(m_pageScrollAdapter);
		m_listView.addItemDecoration(horizontalDecoration);

		if (UserManager.getInstance().isGuest() == false)
		{
			makeCommentsInputLayout();
		}
		else
		{
			makeCommentsInputLayoutForGuest();
		}

		if (m_data.isLoaded() == false)
		{
			loadComments();
		}
		else
		{
			showProgress(false);
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if(m_data != null)
			m_data.cancelRequests();
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		m_unbinder.unbind();

		KeyboardUtils.hideSoftKeyboard(getActivity());
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
	public void onRelease()
	{
		CommentsModel.getInstance().release(this);
	}

	private void initToolbar(View view)
	{
		view.setPadding(0, getActionBarHeight(), 0, 0);

		Toolbar toolbar = getToolbar();
		toolbar.setTitle(getString(R.string.comments_title));
		toolbar.setTranslationY(0);
		if (toolbar != null && isAdded() == true && getUserVisibleHint() == true)
		{
			int color = getResources().getColor(R.color.black);
			toolbar.setTitleTextColor(ColorUtils.setAlphaComponent(color, 255));
			toolbar.getNavigationIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			if (toolbar.getOverflowIcon() != null)
			{
				toolbar.getOverflowIcon().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}
		}
	}

	private void postComment()
	{
		final TextView send = m_commentInputView.findViewById(R.id.send);
		final EditText inputComment = m_commentInputView.findViewById(R.id.input_comment);
		final View inputProgress = m_commentInputView.findViewById(R.id.comment_enter_progress);

		String comment = inputComment.getText().toString().trim();
		String commentPostUrl = UrlFactory.commentPost();

		HashMap params = ParamFactory.commentPost(m_background.getUuid(), comment);

		Requests.authRequestByPost(commentPostUrl, params, Object.class, new Response.Listener<Object>()
		{
			@Override
			public void onResponse(Object response)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				m_isCommentPosting = false;

				inputComment.setEnabled(true);
				inputComment.setText("");

				send.setVisibility(View.VISIBLE);
				send.setTextColor(getResources().getColor(R.color.date_text_color));

				inputProgress.setVisibility(View.GONE);

				loadComments();
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				m_isCommentPosting = false;

				inputComment.setEnabled(true);

				send.setVisibility(View.VISIBLE);
				send.setTextColor(getResources().getColor(R.color.color_accent));

				inputProgress.setVisibility(View.GONE);

				VolleyErrorHandler errorHandler = new VolleyErrorHandler(getActivity());
				errorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				errorHandler.handleError(error);
			}
		});

		AnalyticsManager.getInstance().eventUserActionComment(getActivity(), "ALL_COMMENTS");
	}

	private void loadNext()
	{
		showProgress(true);

		m_data.requestNext(new Response.Listener<Comments>()
		{
			@Override
			public void onResponse(Comments response)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				showProgress(false);
				m_mergeAdapter.notifyDataSetChanged();
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
				volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				volleyErrorHandler.handleError(error);
			}
		});
	}

	private void loadComments()
	{
		showProgress(true);

		m_data.request(m_background.getCommentsUrl(), new Response.Listener<Comments>()
		{
			@Override
			public void onResponse(Comments response)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				showProgress(false);
				m_mergeAdapter.notifyDataSetChanged();
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				showProgress(false);

				VolleyErrorHandler volleyErrorHandler = new VolleyErrorHandler(getActivity());
				volleyErrorHandler.registerErrorListener(new VolleyErrorHandler.ToastErrorListener(getActivity()));
				volleyErrorHandler.handleError(error);
			}
		});
	}

	/*private void addLatestComments(final Comments newComments, final Comment currentLatest)
	{
		Observable.from(newComments.getCommentsList())
				.filter(new Func1<Comment, Boolean>()
				{
					@Override
					public Boolean call(Comment comment)
					{
						return comment.getRegDate() > currentLatest.getRegDate();
					}
				})
				.toBlocking()
				.subscribe(new Action1<Comment>()
				{
					@Override
					public void call(Comment comment)
					{
						m_comments.getCommentsList().add(0, comment);
					}
				});

		Collections.sort(m_comments.getCommentsList(), new Comparator<Comment>()
		{
			@Override
			public int compare(Comment lhs, Comment rhs)
			{
				return ((lhs.getRegDate() > rhs.getRegDate()) ? -1 : (lhs.getRegDate() < rhs.getRegDate()) ? +1 : 0);
			}
		});
	}*/

	//=========================================================================
	// Public Methods
	//=========================================================================

	@SuppressWarnings("deprecation")
	public static Fragment newInstance(Background background)
	{
		final Fragment fragment = new CommentsFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_BACKGROUND, background);
		fragment.setArguments(bundle);

		return BaseModel.wrap(fragment);
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void makeCommentsInputLayoutForGuest()
	{
		final View guestClickable = m_commentInputView.findViewById(R.id.guest_clickable);
		guestClickable.setVisibility(View.VISIBLE);

		guestClickable.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "COMMENT");
				getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_COMMENT));
			}
		});
	}

	private void makeCommentsInputLayout()
	{
		String avatarUrl = UserManager.getInstance().getUser().getAvatar().getUrl();

		final TextView send = m_commentInputView.findViewById(R.id.send);
		final EditText inputComment = m_commentInputView.findViewById(R.id.input_comment);
		final View inputProgress = m_commentInputView.findViewById(R.id.comment_enter_progress);

		if (TextUtils.isEmpty(inputComment.getText()) == true)
		{
			send.setTextColor(getResources().getColor(R.color.date_text_color));
		}

		inputComment.addTextChangedListener(new SimpleTextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				final String str = s.toString().trim();

				if (before == 0 && TextUtils.isEmpty(str) == false)
				{
					send.setTextColor(getResources().getColor(R.color.color_accent));
				}

				if (TextUtils.isEmpty(str) == true)
				{
					send.setTextColor(getResources().getColor(R.color.date_text_color));
				}
			}
		});

		send.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(final View v)
			{
				String comment = inputComment.getText().toString().trim();

				if (m_isCommentPosting == true)
				{
					ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.processing).show();
					return;
				}
				if (TextUtils.isEmpty(comment) == true)
				{
					return;
				}

				if (comment.length() > 500)
				{
					ToastUtils.makeInfoToast(getActivity(), Toast.LENGTH_SHORT, R.string.comment_too_long).show();
					return;
				}

				m_isCommentPosting = true;

				inputComment.setEnabled(false);
				inputProgress.setVisibility(View.VISIBLE);
				send.setVisibility(View.INVISIBLE);

				postComment();
			}
		});
	}

	private void showCommentMenu(View view, final Comment comment)
	{
		final PopupMenu popup = new PopupMenu(getContext(), view);
		popup.getMenuInflater().inflate(R.menu.comment, popup.getMenu());

		if (UserManager.getInstance().isMe(m_background.getUser()) == false &&
				UserManager.getInstance().isMe(comment.getUser()) == false)
		{
			popup.getMenu().findItem(R.id.comment_delete).setVisible(false);
		}

		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				final int menuId = item.getItemId();

				switch (menuId)
				{
					case R.id.comment_report:
						onClickCommentReport(comment);
						return true;

					case R.id.comment_delete:
						onClickCommentDelete(comment);
						return true;
				}
				return false;
			}
		});

		popup.show();
	}

	private void onClickCommentReply(Comment comment)
	{
		String text = "@" + comment.getUser().getUsername() + " ";

		final EditText inputComment = m_commentInputView.findViewById(R.id.input_comment);
		inputComment.append(text);
		inputComment.requestFocus();
	}

	private synchronized void onClickCommentTranslate(final View view, final Comment comment)
	{
		if (PreventDoubleTap.isContinue(PreventDoubleTap.SITUATION_A) == false)
			return;

		final String origin = comment.getContent();
		final String language = Locale.getDefault().getLanguage();

		final TextView translate = view.findViewById(R.id.translate);
		final TextView textView = view.findViewById(R.id.description);

		if (UserManager.getInstance().isGuest())
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "COMMENT_TRANSLATE");
			getActivity().startActivity(AuthActivity.createIntent(getActivity(), AuthActivity.SIGN_ACTION_TRANSLATION));
			return;
		}

		if (comment.isTranslated() == true)
		{
			String content = comment.getContent();
			translate.setText(R.string.comment_translate);
			comment.setTranslated(false);

			if (content.contains("@") == false)
			{
				textView.setText(content);
			}
			else
			{
				SpannableUtils.setUsernameClickable(textView,
						content,
						"@",
						ContextCompat.getColor(getContext(), R.color.user_id_color),
						COMMENT_EMPHASIZE_LIMIT,
						new SpannableUtils.Clickable()
						{
							@Override
							public void onClick(String username)
							{
								onClickUsername(username.substring(1));
							}
						});
			}
		}
		else
		{
			AnalyticsManager.getInstance().eventUserActionCommentTranslate(getContext(), "ALL_COMMENTS");
			AnalyticsManager.getInstance().eventUserTrackingTranslate(getContext(), language);

			if (TextUtils.isEmpty(comment.getTranslateContent()) == false)
			{
				translate.setText(R.string.comment_original);
				textView.setText(comment.getTranslateContent());
				comment.setTranslated(true);
				return;
			}

			translate.setText(R.string.comment_translation);
			Requests.requestByPut(
					UrlFactory.commentTranslate(),
					ParamFactory.commentTranslate(language, origin, comment.getUuid()),
					CommentExtData.class,
					new Response.Listener<CommentExtData>()
					{
						@Override
						public void onResponse(CommentExtData commentExtData)
						{
							if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

							try
							{
								String content = commentExtData.getData().getComment();
								textView.setText(content);
								comment.setTranslated(true);
								comment.setTranslateContent(content);
								translate.setText(R.string.comment_original);
							}
							catch (Exception e)
							{
								translate.setText(R.string.comment_translate);
								ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, R.string.comment_translate_fail).show();
							}
						}
					}, new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

							translate.setText(R.string.comment_translate);
							ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, R.string.comment_translate_fail).show();
						}
					});
		}
	}

	private void onClickCommentReport(final Comment comment)
	{
		if (UserManager.getInstance().isGuest() == true)
		{
			AnalyticsManager.getInstance().eventStatsSignIn(getContext(), "REPORT");
			startActivity(AuthActivity.createIntent(getContext(), AuthActivity.SIGN_ACTION_REPORT_COMMENT));
			return;
		}

		HashMap<String, String> itemsMap = new LinkedHashMap<>();
		itemsMap.put(getString(R.string.report_type_adult), "A");
		itemsMap.put(getString(R.string.report_type_spam), "S");
		itemsMap.put(getString(R.string.report_type_censure), "C");
		itemsMap.put(getString(R.string.report_type_etc), "E");

		final UserReportAction.OnResultListener listener = new UserReportAction.OnResultListener()
		{
			@Override
			public void onResult(Boolean isSuccessed)
			{
				if (FragmentUtils.isDestroyed(CommentsFragment.this) == true) return;

				String message = isSuccessed == true
						? getString(R.string.report_comment_success)
						: getString(R.string.report_comment_fail);

				Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
			}
		};

		new UserReportAction.Builder(getContext())
				.setUrl(UrlFactory.reportComment())
				.setUuid(comment.getUuid())
				.setButtonTextMap(itemsMap)
				.setDefaultItem("C")
				.setListener(listener)
				.run();
	}

	private void onClickCommentDelete(final Comment comment)
	{
		MaterialDialog.SingleButtonCallback onPositive = new MaterialDialog.SingleButtonCallback()
		{
			@Override
			public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
			{
				onCommentDelete(comment);
			}
		};

		new MaterialDialog.Builder(getContext())
				.title(R.string.comment_menu_delete_title)
				.content(R.string.comment_menu_delete_content)
				.autoDismiss(true)
				.negativeText(R.string.cancel)
				.positiveText(R.string.comment_menu_delete_button)
				.onPositive(onPositive)
				.show();
	}

	public void onClickUsername(String username)
	{
		AnalyticsManager.getInstance().eventUserActionProfile(getContext(), "ALL_COMMENTS");
		AnalyticsManager.getInstance().eventUserTrackingProfile(getContext(), username);

		if (UserManager.getInstance().isMe(username) == false)
		{
			AnalyticsManager.getInstance().eventUserActionOtherProfile(getContext(), "ALL_COMMENTS");
		}

		String userInfoUrl = UrlFactory.usersInfo(username);

		Fragment fragment = UserInfoFragment.newInstance(userInfoUrl);
		AbsMainActivity.getTabStackHelper(CommentsFragment.this).showFragment(fragment);
	}

	private void onCommentDelete(Comment comment)
	{
		onCommentDeleteHelper(comment, m_data.getCommentList(), m_adapter);

		if (isEmpty() == true)
		{
			showProgress(false);
		}
	}

	public void onCommentDeleteHelper(final Comment comment, final List<Comment> commentsList, final RecyclerView.Adapter adapter)
	{
		final int index = commentsList.indexOf(comment);

		if (index != -1)
		{
			commentsList.remove(index);
			adapter.notifyItemRemoved(index);
		}

		Requests.authRequestByDelete(
				UrlFactory.commentDelete(comment.getUuid()),
				null,
				Empty.class,
				null,
				null);
	}

	private boolean isEmpty()
	{
		if (m_data == null || m_data.getCommentList() == null || m_data.getCommentList().isEmpty())
			return true;

		return false;
	}

	private void showProgress(boolean isShowing)
	{
		if (isShowing == true)
		{
			m_empty.setVisibility(View.GONE);
			m_progress.setVisibility(isEmpty() ? View.VISIBLE : View.GONE);
		}
		else
		{
			m_progress.setVisibility(View.GONE);
			m_empty.setVisibility(isEmpty() ? View.VISIBLE : View.GONE);

			if (m_commentInputView != null && isEmpty())
			{
				final EditText inputComment = m_commentInputView.findViewById(R.id.input_comment);
				inputComment.setEnabled(true);
				inputComment.requestFocus();
				InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(inputComment, InputMethodManager.SHOW_FORCED);
			}
		}

		m_listView.setVisibility(isEmpty() ? View.GONE : View.VISIBLE);
		m_commentInfo.setVisibility(isEmpty() ? View.VISIBLE : View.GONE);
		m_mergeAdapter.findViewById(R.id.progress).setVisibility(m_data.hasNext() ? View.VISIBLE : View.GONE);
	}

	//=========================================================================
	// Adapter
	//=========================================================================

	private CommentsAdapter m_adapter = new CommentsAdapter()
	{
		@Override
		protected Comment getItem(int position)
		{
			return m_data.getCommentList().get(position);
		}

		@Override
		protected void onClickUser(View view, User user)
		{
			onClickUsername(user.getUsername());
		}

		@Override
		protected void onClickTranslate(View view, Comment comment)
		{
			onClickCommentTranslate(view, comment);
		}

		@Override
		protected void onClickReply(View view, Comment comment)
		{
			onClickCommentReply(comment);
		}

		@Override
		protected void onClickMenu(View view, Comment comment)
		{
			showCommentMenu(view, comment);
		}

		@Override
		public int getItemCount()
		{
			return isEmpty() ? 0 : m_data.getCommentList().size();
		}

		@Override
		protected void onClickUsername(String username)
		{
			CommentsFragment.this.onClickUsername(username);
		}
	};

	//=========================================================================
	// Page Scroll Adapter
	//=========================================================================

	final PageScrollAdapter m_pageScrollAdapter = new PageScrollAdapter()
	{
		@Override
		protected void onLoadNext()
		{
			loadNext();
		}

		@Override
		protected boolean hasNext()
		{
			return m_data.hasNext();
		}

		@Override
		protected boolean isLoading()
		{
			return m_data.isLoading();
		}

		@Override
		protected int findLastVisibleItemPosition()
		{
			return m_layoutManager.findLastVisibleItemPosition();
		}
	};

	//=========================================================================
	// Empty Class
	//=========================================================================

	public static class Empty
	{
		// Nothing
	}

	//========================================================================
	// Constants
	//========================================================================

	private static final String KEY_BACKGROUND = "KEY_BACKGROUND";

	private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
	private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();

	//=========================================================================
	// Variables
	//=========================================================================

	@BindView(android.R.id.list) RecyclerView m_listView;
	@BindView(R.id.comment_input) ViewGroup m_commentInputView;
	@BindView(R.id.layout_comments_info) FrameLayout m_commentInfo;
	@BindView(R.id.progress_loading) ProgressWheel m_progress;
	@BindView(R.id.empty_comments) LinearLayout m_empty;

	private GridLayoutManager m_layoutManager;
	private MergeRecyclerAdapter m_mergeAdapter;
	private Background m_background;

	private boolean m_isCommentPosting;
	private CommentsModelData m_data;

	private Unbinder m_unbinder;
}
