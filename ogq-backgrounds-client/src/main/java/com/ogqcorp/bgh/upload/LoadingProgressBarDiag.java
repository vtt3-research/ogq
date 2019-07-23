package com.ogqcorp.bgh.upload;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.Unbinder;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;
import com.ogqcorp.bgh.R;

/**
 * Created by bongkyucha on 2017. 7. 3..
 */

public class LoadingProgressBarDiag extends MaterialDialog implements OnProgressBarListener
{
	public LoadingProgressBarDiag(Context context) {
		super(new Builder(context)
					.customView(R.layout.loadingnumberprogessbar_dialog, false)
					.title(context.getResources().getString(R.string.upload_content_upload_info)));

		this.setCancelable(false);
		m_unbinder = ButterKnife.bind(this, view);
		m_handler = new Handler();
		m_progress.setOnProgressBarListener(this);
	}

	public LoadingProgressBarDiag(Context context, String title) {
		super(new Builder(context)
				.customView(R.layout.loadingnumberprogessbar_dialog, false)
				.title(title));

		this.setCancelable(false);
		m_unbinder = ButterKnife.bind(this, view);
		m_handler = new Handler();
		m_progress.setOnProgressBarListener(this);
	}

	protected LoadingProgressBarDiag(Builder builder)
	{
		super(builder);
		LayoutInflater layoutInflater = getLayoutInflater();
		final View view = layoutInflater.inflate(R.layout.loadingnumberprogessbar_dialog, null);
		this.addContentView(view, null);

//		setContentView(R.layout.loadingprogessbar_dialog);
		this.setTitle(getContext().getResources().getString(R.string.upload_content_upload_info));
		this.setCancelable(false);

		m_unbinder.unbind();

		m_progress.setOnProgressBarListener(this);
	}

	@Override
	public void dismiss()
	{
		super.dismiss();
		m_unbinder.unbind();
		m_handler = null;
	}

	@Override
	public void onProgressChange(int current, int max) {
	}

	public void setMessage(String msg) {
		m_message.setText(msg);
	}

	public void setNewProgress(final int current) {
		new Thread(new Runnable() {
			public void run() {
				if(m_handler != null)
				{
					m_handler.post(new Runnable() {
						public void run() {
							if(m_progress != null)
								m_progress.setProgress(current);
						}
					});
				}
			}
		}).start();

	}

	Handler m_handler = null;

	private Unbinder m_unbinder;
	
	@BindView(R.id.dialog_message) TextView m_message;
	@BindView(R.id.number_progress_bar) NumberProgressBar m_progress;
}
