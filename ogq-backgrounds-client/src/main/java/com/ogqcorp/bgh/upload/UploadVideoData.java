package com.ogqcorp.bgh.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.bgh.system.AppLogger;
import com.ogqcorp.commons.request.volley.RequestManager;
import com.ogqcorp.commons.utils.PathUtils;

public class UploadVideoData implements Parcelable
{

	public UploadVideoData(String title, String description, List<String> tags, int license, Uri uri)
	{
		m_title = title;
		m_description = description;
		m_tags = new ArrayList<>();
		if (tags != null && tags.size() > 0)
		{
			m_tags.addAll(tags);
		}
		m_license = license;
		m_uri = uri.toString();
	}

	//========================================================================
	// Parcelable Methods
	//========================================================================

	public UploadVideoData(Parcel in)
	{
		m_title = in.readString();
		m_description = in.readString();
		m_tags = (List<String>) in.readValue(String.class.getClassLoader());
		m_license = in.readInt();
		m_uri = in.readString();
		m_VideoFile = (File) in.readValue(File.class.getClassLoader());
		m_thumbFile = (File) in.readValue(File.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(m_title);
		dest.writeString(m_description);
		dest.writeValue(m_tags);
		dest.writeInt(m_license);
		dest.writeString(m_uri.toString());
		dest.writeValue(m_VideoFile);
		dest.writeValue(m_thumbFile);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public static final Creator<UploadVideoData> CREATOR = new Creator<UploadVideoData>()
	{
		public UploadVideoData createFromParcel(Parcel in)
		{
			return new UploadVideoData(in);
		}

		public UploadVideoData[] newArray(int size)
		{
			return new UploadVideoData[size];
		}
	};

	//========================================================================
	// Public Methods
	//========================================================================

	public long upload(final Context context, final ProgressListener listener)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				UploadVideo(context, listener);
			}
		});

		m_uid = thread.getId();
		thread.start();

		return m_uid;
	}

	//========================================================================
	// Private Methods
	//========================================================================

	private void onProgress(final ProgressListener listener, final int progress)
	{
		listener.onProgress(m_uid, progress);
	}

	private void onCompleted(final ProgressListener listener, final boolean isSuccessed, final String errorMsg)
	{
		if (m_tags != null)
		{
			m_tags.clear();
		}
		listener.onCompleted(m_uid, isSuccessed, errorMsg);
	}

	private void UploadVideo(final Context context, final ProgressListener listener)
	{
		AsyncTask<Object, Object, Object> asyncUploadTask = new AsyncTask<Object, Object, Object>()
		{
			@Override
			protected Object doInBackground(Object... params)
			{
				boolean uploadSuccess = false;

				String afterUrl = null;
				String abortUrl = null;
				Object response;

				try
				{
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 0");

					onProgress(listener, 0);

					Point size = new Point();
					String videoHash = "";
					String contentHash = "";

					if (m_VideoFile == null)
					{
						Uri uri = Uri.parse(m_uri);
						m_VideoFile = new File(uri.getPath());
					}

					onProgress(listener, 10);

					if (m_thumbFile == null)
					{
						Uri uri = Uri.parse(m_uri);
						Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
						m_thumbFile = PathUtils.createTempFile(context, "upload", ".jpg");    // original copy
						saveBitmapToTempFile(bitmap, m_thumbFile.getPath());

						size.x = bitmap.getWidth();
						size.y = bitmap.getHeight();
					}

					onProgress(listener, 20);
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 1");

					videoHash = getContentHash(m_VideoFile);
					contentHash = getContentHash(m_thumbFile);

					onProgress(listener, 25);

					final String tags = TextUtils.join(" ", m_tags);

					String url = UrlFactory.uploadVideo();
					HashMap<String, Object> param = null;
					param = ParamFactory.uploadVideo(m_title, m_description, m_license, tags, size.x, size.y, contentHash, videoHash);

					final RequestFuture<Object> requestFuture = RequestFuture.newFuture();
					try
					{
						// Upload Video meta info
						Requests.authRequestByPost(
								url,
								param,
								Object.class,
								requestFuture,
								requestFuture);

						response = requestFuture.get();
					}
					catch (Exception e)
					{
						FirebaseCrashLog.log("UploadVideo UploadPrepareResult Requests Section Exception");
						FirebaseCrashLog.logException(context, e);

						AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 0 Exception");
						AppLogger.getInstance().e(AppLogger.TAG.UI, e);
						return e;
					}

					onProgress(listener, 40);
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 2");

					// Upload Data File to S3
					if (response != null)
					{
						Gson gson = new GsonBuilder().create();
						JSONObject object = new JSONObject((HashMap<String, Object>) response);
						UploadVideoPrepareResult uvpr = gson.fromJson(object.getJSONObject("data").toString(), UploadVideoPrepareResult.class);

						afterUrl = uvpr.m_afterUpload.url;
						abortUrl = uvpr.m_abortUpload.url;

						//Upload Thumbnail File
						RequestFuture<String> requestFuture2 = RequestFuture.newFuture();
						try
						{
							uvpr.m_thumbnailUpload.params.put("file", m_thumbFile);
							final UploadToS3Request request = new UploadToS3Request(
									Request.Method.POST,
									uvpr.m_thumbnailUpload.url,
									uvpr.m_thumbnailUpload.params,
									true,
									requestFuture2,
									requestFuture2);

							request.setShouldCache(false);
							RequestManager.getInstance().addToRequestQueue(request);
							response = requestFuture2.get(180, TimeUnit.SECONDS);
						}
						catch (Exception e)
						{
							onCompleted(listener, false, e.getLocalizedMessage());
							return e;
						}

						onProgress(listener, 60);
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 3");

						//Upload Video File
						requestFuture2 = RequestFuture.newFuture();
						try
						{
							uvpr.m_videoUpload.params.put("file", m_VideoFile);
							final UploadToS3Request request = new UploadToS3Request(
									Request.Method.POST,
									uvpr.m_videoUpload.url,
									uvpr.m_videoUpload.params,
									true,
									requestFuture2,
									requestFuture2);

							request.setShouldCache(false);
							RequestManager.getInstance().addToRequestQueue(request);
							response = requestFuture2.get(180, TimeUnit.SECONDS);
						}
						catch (Exception e)
						{
							onCompleted(listener, false, e.getLocalizedMessage());

							AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 1 Exception");
							AppLogger.getInstance().e(AppLogger.TAG.UI, e);

							return e;
						}

						onProgress(listener, 90);
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 4");

						try
						{
							//AnalyticsManager.getInstance().eventStatsUploadVideoType(context, "VIDEO");
						}
						catch (Exception e)
						{
						}

						try
						{
							final RequestFuture<AsyncStats.Empty> requestFuture3 = RequestFuture.newFuture();

							Requests.authRequestByPost(
									afterUrl,
									null,
									AsyncStats.Empty.class,
									requestFuture3,
									requestFuture3);

							requestFuture3.get();
						}
						catch (Exception e)
						{
							FirebaseCrashLog.log("UploadVideo after_upload Requests Section Exception");
							FirebaseCrashLog.logException(context, e);

							AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 2 Exception");
							AppLogger.getInstance().e(AppLogger.TAG.UI, e);
							return e;
						}

						uploadSuccess = true;
						onProgress(listener, 100);
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 5");
					}
				}
				catch (Exception e)
				{
					FirebaseCrashLog.log("UploadVideo Section Exception");
					FirebaseCrashLog.logException(context, e); // firebase_crash

					AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 3 Exception");
					AppLogger.getInstance().e(AppLogger.TAG.UI, e);
					return e;
				}
				finally
				{
					if (uploadSuccess == false)
					{
						//실패처리
						try
						{
							final RequestFuture<AsyncStats.Empty> requestFuture4 = RequestFuture.newFuture();

							Requests.authRequestByDelete(
									abortUrl,
									null,
									AsyncStats.Empty.class,
									requestFuture4,
									requestFuture4);

							response = requestFuture4.get();
							onProgress(listener, 100);
						}
						catch (Exception e)
						{
							onCompleted(listener, false, e.getLocalizedMessage());
							AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo 4 Exception");
							AppLogger.getInstance().e(AppLogger.TAG.UI, e);

							return e;
						}
					}
					onCompleted(listener, true, null);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Object o)
			{
				super.onPostExecute(o);

				try
				{
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadVideoData UploadVideo Completed!!!");
				}
				catch (Exception e)
				{
				}

				if (o instanceof Exception)
				{
					String errorMsg = ((Exception) o).getLocalizedMessage();
					onCompleted(listener, false, errorMsg);
					return;
				}

				onCompleted(listener, true, null);
			}
		};

		asyncUploadTask.execute();
	}

	private String getContentHash(File file) throws IOException, NoSuchAlgorithmException
	{
		byte[] b = FileUtils.readFileToByteArray(file);
		return bin2hex(getHash(b));
	}

	public byte[] getHash(byte[] b) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(b);
		return digest.digest();
	}

	private String getMIMETypeFromUri(Context context, Uri contentUri)
	{
		Cursor cursor = null;
		try
		{
			String[] proj = { MediaStore.Video.Media.MIME_TYPE };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("UploadVideo getMIMETypeFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData getMIMETypeFromUri Exception");
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
			String[] proj = { MediaStore.Video.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("UploadVideo getFilePathFromUri Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData getFilePathFromUri Exception");
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

	private void saveBitmapToTempFile(Bitmap bitmap, String strFilePath)
	{
		//File fileCacheItem = new File(strFilePath);
		m_thumbFile = new File(strFilePath);
		OutputStream out = null;

		try
		{
			m_thumbFile.createNewFile();
			out = new FileOutputStream(m_thumbFile);

			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		}
		catch (Exception e)
		{
			FirebaseCrashLog.log("UploadVideo saveBitmapToTempFile Exception");
			FirebaseCrashLog.logException(e);

			AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadVideoData saveBitmapToTempFile Exception");
			AppLogger.getInstance().e(AppLogger.TAG.UI, e);

			e.printStackTrace();
		}
		finally
		{
			try
			{
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	static String bin2hex(byte[] data)
	{
		return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
	}

	//========================================================================
	// Listener Interface
	//========================================================================

	public interface ProgressListener
	{
		void onProgress(long uid, int progress);

		void onCompleted(long uid, boolean isSuccess, String errorMessage);

		void onCompletedSameImage(long uid, boolean isSuccess, boolean isExists, String errorMessage);
	}

	//========================================================================
	// Constants
	//========================================================================

	private static DefaultRetryPolicy RETRY_POLICY = new DefaultRetryPolicy(10000, 3, 1.5f);

	//========================================================================
	// Variables
	//========================================================================

	String m_title;
	String m_description;
	List<String> m_tags;
	int m_license;
	String m_uri;

	long m_uid;
	File m_VideoFile;
	File m_thumbFile;
}