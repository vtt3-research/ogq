package com.ogqcorp.bgh.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ogqcorp.bgh.R;
import com.ogqcorp.commons.WebDialogFragment;
import com.ogqcorp.commons.utils.FragmentUtils;
import com.ogqcorp.commons.utils.ToastUtils;

public class WebDialogFragmentEx extends WebDialogFragment
{
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);

		getToolbar().inflateMenu(R.menu.fragment_webdialog_ex);
	}

	@Override
	public boolean onMenuItemClick(MenuItem menuItem)
	{
		if (menuItem.getItemId() == R.id.action_open_width)
		{
			Bundle headers = getArguments().getBundle("KEY_EXTRA_HEADERS");

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(getWebView().getUrl()));
			intent.putExtra(Browser.EXTRA_HEADERS, headers);
			startActivity(intent);
			return true;
		}

		return super.onMenuItemClick(menuItem);
	}

	@Override
	protected void onInitWebView(WebView webView)
	{
		super.onInitWebView(webView);

		getWebView().setWebViewClient(new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				if (FragmentUtils.isDestroyed(WebDialogFragmentEx.this) == true)
				{
					return true;
				}

				try
				{
					final Uri uri = Uri.parse(url);
					final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					startActivity(intent);
					ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, getString(R.string.move_to_sponsor_page)).show();
				}
				catch (Exception e)
				{
					ToastUtils.makeInfoToast(getContext(), Toast.LENGTH_SHORT, getString(R.string.move_to_sponsor_page_fail)).show();
				}

				WebDialogFragmentEx.this.dismiss();
				//getActivity().onBackPressed();
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url)
			{
				if (FragmentUtils.isDestroyed(WebDialogFragmentEx.this) == true) return;

				if (getToolbar() != null)
				{
					getToolbar().setTitle(getTitle());
				}

				view.setVisibility(View.VISIBLE);

				@SuppressWarnings("ConstantConditions")
				final View progressView = getView().findViewById(com.ogqcorp.commons.R.id.progress);
				if (progressView != null)
				{
					progressView.setVisibility(View.GONE);
				}
			}
		});
	}

	public static class Builder extends WebDialogFragment.Builder
	{
		public Builder(String url)
		{
			super(url);
		}

		@Override
		public WebDialogFragmentEx start(FragmentManager fragmentManager)
		{
			try
			{
				final WebDialogFragmentEx fragment = new WebDialogFragmentEx();
				final Bundle args = buildArguments();
				fragment.setArguments(args);
				//fragment.show(fragmentManager, WebDialogFragmentEx.class.getSimpleName());

				FragmentTransaction ft = fragmentManager.beginTransaction();
				ft.add(fragment, TAG_FRAGMENT);
				ft.commitAllowingStateLoss();

				return fragment;
			}
			catch (Exception e)
			{
				return null;
			}
		}
	}

	private static final String TAG_FRAGMENT = WebDialogFragmentEx.class.getSimpleName();
}
