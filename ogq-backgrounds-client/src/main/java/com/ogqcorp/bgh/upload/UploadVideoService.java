package com.ogqcorp.bgh.upload;

import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.ogqcorp.bgh.R;
import com.ogqcorp.bgh.spirit.auth.UserManager;
import com.ogqcorp.bgh.spirit.data.User;
import com.ogqcorp.bgh.spirit.preference.PreferencesManager;
import com.ogqcorp.bgh.system.NotiChannelManager;
import com.ogqcorp.commons.utils.PathUtils;

public class UploadVideoService extends Service
{

	//========================================================================
	// Static Methods
	//========================================================================

	public static void upload(Context context, UploadVideoData uploadData)
	{
		Intent intent = new Intent(context, UploadVideoService.class);
		intent.putExtra(UPLOAD_DATA, uploadData);
		context.startService(intent);
	}

	//========================================================================
	// Override Methods
	//========================================================================

	@Override
	public void onCreate()
	{
		super.onCreate();

		init();

		if (PreferencesManager.getInstance().isUploading(this) == true)
		{
			PreferencesManager.getInstance().setIsUploading(this, false);
			uploadCompleted(getString(R.string.upload_video_notification_complete_failed_title), getString(R.string.upload_content_notification_complete_failed_text2));
			UploadVideoService.this.stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (intent != null)
		{
			PreferencesManager.getInstance().setIsUploading(this, true);
			UploadVideoData data = intent.getParcelableExtra(UPLOAD_DATA);
			m_progressMap.put(data.upload(this, m_listener), 0);
		}
		return Service.START_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		clear();
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void init()
	{
		if (m_notificationManager != null)
		{
			return;
		}

		m_notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			m_builder = new NotificationCompat.Builder(this, NotiChannelManager.CHANNEL_ID_DEFAULT);
		}
		else
		{
			m_builder = new NotificationCompat.Builder(this);
		}

		m_builder.setColor(ContextCompat.getColor(this, R.color.color_primary))
				.setSmallIcon(R.drawable.ic_upload_grey)
				.setTicker(getString(R.string.upload_video_notification_ticker))
				.setOngoing(true)
				.setAutoCancel(true);
	}

	private void clear()
	{
		PreferencesManager.getInstance().setIsUploading(this, false);
		m_progressMap.clear();
		m_notificationManager = null;
		m_builder = null;
	}

	private void uploadCompleted(final String title, final String text)
	{
		PreferencesManager.getInstance().setIsUploading(UploadVideoService.this, false);
		PathUtils.clearTempDirectory(UploadVideoService.this, "upload");
		m_notificationManager.cancel(UPLOAD_NOTI_ID);

		User user = UserManager.getInstance().getUser();
		PendingIntent notifyPendingIntent = null;
		if (user != null)
		{
			Uri uri = Uri.parse("bgh://userinfo?username=" + user.getUsername() + "&screen=post");
			Intent notifyIntent = new Intent(Intent.ACTION_VIEW, uri);
			notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			notifyPendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		NotificationCompat.Builder builder = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			builder = new NotificationCompat.Builder(this, NotiChannelManager.CHANNEL_ID_DEFAULT);
		}
		else
		{
			builder = new NotificationCompat.Builder(this);
		}

		Notification notification = builder.setColor(ContextCompat.getColor(this, R.color.color_primary))
				.setContentTitle(title)
				.setSmallIcon(R.drawable.ic_upload_done)
				.setContentIntent(notifyPendingIntent)
				.setVibrate(new long[] { 300, 300 })
				.setAutoCancel(true)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(text))
				.build();

		m_notificationManager.notify(UPLOAD_NOTI_ID, notification);
	}

	//========================================================================
	// ProgressListener
	//========================================================================

	private UploadVideoData.ProgressListener m_listener = new UploadVideoData.ProgressListener()
	{
		@Override
		public synchronized void onProgress(long uid, int progress)
		{
			if (m_notificationManager == null)
			{
				return;
			}

			m_progressMap.put(uid, progress);
			int totalProgress = 0;
			String contentTitle;
			if (m_progressMap.size() <= 1)
			{
				totalProgress = progress;
				contentTitle = getString(R.string.upload_video_notification_ticker) + " (" + progress + "%)";
			}
			else
			{
				for (Long key : m_progressMap.keySet())
				{
					totalProgress += m_progressMap.get(key);
				}
				totalProgress /= m_progressMap.size();
				contentTitle = String.format(getString(R.string.upload_video_notification), m_progressMap.size()) + " (" + totalProgress + "%)";
			}

			m_builder.setContentTitle(contentTitle);
			m_builder.setProgress(100, totalProgress, false);
			m_notificationManager.notify(UPLOAD_NOTI_ID, m_builder.build());

		}

		@Override
		public void onCompleted(long uid, boolean isSuccess, String errorMessage)
		{
			if (m_notificationManager == null)
			{
				return;
			}

			m_progressMap.remove(uid);
			if (m_progressMap.size() == 0)
			{
				if (isSuccess == true)
				{
					uploadCompleted(getString(R.string.upload_content_notification_complete_successed), getString(R.string.upload_video_notification_complete_successed_text));
				}
				else
				{
					uploadCompleted(getString(R.string.upload_video_notification_complete_failed_title), getString(R.string.upload_content_notification_complete_failed_text1));
				}

				UploadVideoService.this.stopSelf();
			}
		}

		@Override
		public void onCompletedSameImage(long uid, boolean isSuccess, boolean isExists, String errorMessage)
		{

		}
	};

	//========================================================================
	// Constants
	//========================================================================

	private static final String UPLOAD_DATA = "UPLOAD_DATA";
	private static final int UPLOAD_NOTI_ID = 2028;

	//========================================================================
	// Variables
	//========================================================================

	private NotificationManager m_notificationManager = null;
	private NotificationCompat.Builder m_builder = null;

	private Map<Long, Integer> m_progressMap = new HashMap<>();

}
