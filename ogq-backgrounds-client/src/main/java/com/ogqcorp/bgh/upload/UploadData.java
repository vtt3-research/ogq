package com.ogqcorp.bgh.upload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.ogqcorp.bgh.filter.FilterManager;
import com.ogqcorp.bgh.gifwallpaper.GifLiveWallpaperFileUtils;
import com.ogqcorp.bgh.spirit.analytics.AnalyticsManager;
import com.ogqcorp.bgh.spirit.data.AnnotationLabels;
import com.ogqcorp.bgh.spirit.firebasecrash.FirebaseCrashLog;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.ParamFactory;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.bgh.system.AppLogger;
import com.ogqcorp.commons.request.volley.RequestManager;
import com.ogqcorp.commons.support.Base64;
import com.ogqcorp.commons.utils.BitmapUtils;
import com.ogqcorp.commons.utils.PathUtils;

public class UploadData implements Parcelable
{

	public UploadData(String title, String description, List<String> tags, int license, Uri uri, int filterIndex, boolean isCrop, String wepickId)
	{
		m_title = title;
		m_description = description;
		m_userTags = new ArrayList<>();
		if (tags != null && tags.size() > 0)
		{
			m_userTags.addAll(tags);
		}
		m_originTags = new ArrayList<>();
		m_license = license;
		m_uri = uri.toString();
		m_wepickId = wepickId;
		m_filterIndex = filterIndex;
		m_isCrop = isCrop;
	}

	//========================================================================
	// Parcelable Methods
	//========================================================================

	public UploadData(Parcel in)
	{
		m_title = in.readString();
		m_description = in.readString();
		m_userTags = (List<String>) in.readValue(String.class.getClassLoader());
		m_originTags = (List<String>) in.readValue(String.class.getClassLoader());
		m_license = in.readInt();
		m_uri = in.readString();
		m_wepickId = in.readString();
		m_filterIndex = in.readInt();
		m_upFile = (File) in.readValue(File.class.getClassLoader());
		m_isCrop = (in.readInt() == 1) ? true : false;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(m_title);
		dest.writeString(m_description);
		dest.writeValue(m_userTags);
		dest.writeValue(m_originTags);
		dest.writeInt(m_license);
		dest.writeString(m_uri.toString());
		dest.writeString(m_wepickId);
		dest.writeInt(m_filterIndex);
		dest.writeValue(m_upFile);
		dest.writeInt((m_isCrop == true) ? 1 : 0);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public static final Creator<UploadData> CREATOR = new Creator<UploadData>()
	{
		public UploadData createFromParcel(Parcel in)
		{
			return new UploadData(in);
		}

		public UploadData[] newArray(int size)
		{
			return new UploadData[size];
		}
	};

	//========================================================================
	// Public Methods
	//========================================================================

	public void setTitle(String title)
	{
		m_title = title;
	}

	public void setDescription(String description)
	{
		m_description = description;
	}

	public void setUserTags(List<String> tags)
	{
		if (m_userTags != null)
		{
			m_userTags.clear();
		}
		else
		{
			m_userTags = new ArrayList<>();
		}

		m_userTags.addAll(tags);
	}

	public long uploadRequestAnnotationLabels(final Context context, final ProgressListener listener)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				requestAnnotationLabels(context, listener);
			}
		});

		m_uid = thread.getId();
		thread.start();

		return m_uid;
	}

	public long upload(final Context context, final ProgressListener listener)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				//requestAnnotationLabels(context, listener);
				uploadImage(context, listener);
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
		if (m_originTags != null)
			m_originTags.clear();

		if (m_userTags != null)
			m_userTags.clear();

		listener.onCompleted(m_uid, isSuccessed, errorMsg);
	}

	private void onCompletedSameImage(final ProgressListener listener, final boolean isSuccessed, final boolean isExists, final List<String> tags, final String errorMsg)
	{
		listener.onCompletedSameImage(m_uid, isSuccessed, isExists, tags, errorMsg);
	}

	private void requestAnnotationLabels(final Context context, final ProgressListener listener)
	{
		AsyncTask<Object, Object, Object> asyncTagTask = new AsyncTask<Object, Object, Object>()
		{
			@Override
			protected Object doInBackground(Object... params)
			{
				try
				{
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData requestAnnotationLabels 0");

					//onProgress(listener, 0);
					onProgress(listener, 10);

					Uri uri = Uri.parse(m_uri);

					boolean isGif = isGifMode(context);
					File o_file = null;
					InputStream inputStream = null;

					// GIF
					if (isGif == true)
					{
						if (uri.toString().contains(context.getPackageName()) == true &&
								uri.toString().contains("data") == true &&
								uri.toString().startsWith("file://") == false)
						{
							uri = uri.parse("file://" + uri.toString());
						}

						inputStream = context.getContentResolver().openInputStream(uri);
						o_file = PathUtils.createTempFile(context, "upload", ".jpg"); // original copy
						FileUtils.copyInputStreamToFile(inputStream, o_file);
					}
					else
					{
						if (uri.toString().contains(context.getPackageName()) == true &&
								uri.toString().contains("data") == true &&
								uri.toString().startsWith("file://") == false)
						{
							o_file = new File(uri.toString());
						}
						else
						{
							inputStream = context.getContentResolver().openInputStream(uri);
							o_file = PathUtils.createTempFile(context, "upload", ".jpg"); // original copy
							FileUtils.copyInputStreamToFile(inputStream, o_file);
						}
					}

					m_upFile = PathUtils.createTempFile(context, "upload", ".jpg");    // original copy
					File l_file = PathUtils.createTempFile(context, "upload", ".jpg"); // low quality
					onProgress(listener, 30);

					Bitmap bitmap = BitmapUtils.createBitmapFromFile(o_file, Bitmap.Config.ARGB_8888, -1);
					if (m_filterIndex > 0)
					{
						// 필터 적용
						bitmap = FilterManager.getInstance().getFilteredBitmap(context, bitmap, m_filterIndex);
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
						byte[] bitmapdata = bos.toByteArray();
						ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
						FileUtils.copyInputStreamToFile(bs, o_file);
						bs.close();
					}

					if (inputStream != null)
						inputStream.close();

					onProgress(listener, 50);
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData requestAnnotationLabels 1");

					if (bitmap.getHeight() > IMAGE_HEIGHT)
					{
						createResizedImageFileBaseOnHeight(o_file, m_upFile, IMAGE_HEIGHT, UPLOAD_QUALITY);
					}
					else
					{
						m_upFile = o_file;
					}

					if (bitmap.getHeight() > ANNOTATION_HEIGHT)
					{
						createResizedImageFileBaseOnHeight(m_upFile, l_file, ANNOTATION_HEIGHT, UPLOAD_QUALITY);
					}
					else
					{
						l_file = m_upFile;
					}

					bitmap.recycle();
					//onProgress(listener, 10);
					onProgress(listener, 70);
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData requestAnnotationLabels 2");

					final byte[] readFile = FileUtils.readFileToByteArray(l_file);
					final String encodedFile = Base64.encodeToString(readFile, Base64.NO_WRAP);
					final HashMap<String, Object> parameters = ParamFactory.attachImage(encodedFile);

					//onProgress(listener, 20);

					//Requests.authRequestByPost(UrlFactory.annotationLabel(), parameters, AnnotationLabels.class, new Response.Listener<AnnotationLabels>()
					Requests.authRequestByPost(UrlFactory.annotationLabelByVtt(), parameters, AnnotationLabels.class, new Response.Listener<AnnotationLabels>()
					{
						@Override
						public void onResponse(AnnotationLabels response)
						{
							if (response.getLabels() != null && response.getLabels().size() > 0)
							{
								m_originTags.addAll(response.getLabels());
							}

							onProgress(listener, 100);

							AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData requestAnnotationLabels 3");
							//uploadImage(context, listener);
							onCompletedSameImage(listener, true, response.isExistsSameImage(), m_originTags, null);
						}
					}, new Response.ErrorListener()
					{
						@Override
						public void onErrorResponse(VolleyError error)
						{
							//firebase_crash
							FirebaseCrashLog.log("requestAnnotationLabels Section onErrorResponse");
							FirebaseCrashLog.exceptionVolleyErrorLog(error);
							FirebaseCrashLog.logException(error);

							AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData requestAnnotationLabels 4");
							//onCompleted(listener, false, error.getLocalizedMessage());
							onCompletedSameImage(listener, false, false, null, error.getLocalizedMessage());
						}
					});
				}
				catch (Exception e)
				{
					FirebaseCrashLog.log("requestAnnotationLabels Section Exception");
					FirebaseCrashLog.logException(context, e); // firebase_crash

					AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadData requestAnnotationLabels Exception");
					AppLogger.getInstance().e(AppLogger.TAG.UI, e);
					return e;
				}
				return null;
			}

			protected void onPostExecute(Object o)
			{
				super.onPostExecute(o);

				if (o instanceof Exception)
				{
					String errorMsg = ((Exception) o).getLocalizedMessage();
					//onCompleted(listener, false, errorMsg);
					onCompletedSameImage(listener, false, false, null, errorMsg);
					return;
				}
			}
		};

		asyncTagTask.execute();
	}

	private void uploadImage(final Context context, final ProgressListener listener)
	{
		AsyncTask<Object, Object, Object> asyncUploadTask = new AsyncTask<Object, Object, Object>()
		{
			@Override
			protected Object doInBackground(Object... params)
			{
				try
				{
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 0");
					//onProgress(listener, 55);
					onProgress(listener, 0);

					Point size = null;
					String contentHash = "";

					boolean isGif = isGifMode(context);

					// GIF
					if (isGif == true)
					{
						Uri uri = null;
						if (m_uri.contains(context.getPackageName()) == true &&
								m_uri.contains("data") == true &&
								m_uri.startsWith("file://") == false)
						{
							uri = Uri.parse("file://" + m_uri);
						}
						else
						{
							uri = Uri.parse(m_uri);
						}

						InputStream inputStream = context.getContentResolver().openInputStream(uri);
						File upGifFile = PathUtils.createTempFile(context, "upload", GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION);
						FileUtils.copyInputStreamToFile(inputStream, upGifFile);
						size = BitmapUtils.getBitmapSize(upGifFile);
						contentHash = getContentHash(upGifFile);
						inputStream.close();
					}
					else
					{
						// "4" : BitmapFactory.Options.inSampleSize
						size = BitmapUtils.getBitmapSize(m_upFile);
						size.x *= 4;
						size.y *= 4;
						contentHash = getContentHash(m_upFile);
					}

					//onProgress(listener, 60);
					onProgress(listener, 10);
					AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 1");

					String userTags = "";
					if (m_userTags != null && m_userTags.size() > 0)
					{
						// GIF
						if (isGif == true)
						{
							if (m_userTags.contains("gif") == false)
								m_userTags.add("gif");
						}

						userTags = TextUtils.join(" ", m_userTags);
					}

					String originTags = "";
					if (m_originTags != null && m_originTags.size() > 0)
					{
						// GIF
						if (isGif == true)
						{
							if (m_originTags.contains("gif") == false)
								m_originTags.add("gif");
						}

						originTags = TextUtils.join(" ", m_originTags);
					}

					String url = UrlFactory.upload();

					HashMap<String, Object> param = null;

					// GIF
					if (isGif == true)
					{
						param = ParamFactory.uploadGif(m_title, m_description, m_license, originTags, size.x, size.y, contentHash, GifLiveWallpaperFileUtils.GIF_CONTENT_TYPE);
					}
					else
					{
						if (TextUtils.isEmpty(m_wepickId) == true)
						{
							param = ParamFactory.upload(m_title, m_description, m_license, originTags, userTags, size.x, size.y, contentHash);
						}
						else
						{
							param = ParamFactory.uploadWepick(m_title, m_description, m_license, originTags, userTags, size.x, size.y, contentHash, m_wepickId);
						}
					}

					final RequestFuture<UploadPrepareResult> requestFuture = RequestFuture.newFuture();

					UploadPrepareResult upr = null;

					try
					{
						Requests.authRequestByPost(
								url,
								param,
								UploadPrepareResult.class,
								requestFuture,
								requestFuture);

						//onProgress(listener, 70);
						onProgress(listener, 40);
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 2");

						upr = requestFuture.get();
					}
					catch (Exception e)
					{
						FirebaseCrashLog.log("UploadImage UploadPrepareResult Requests Section Exception");
						FirebaseCrashLog.logException(context, e);

						AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 0 Exception");
						AppLogger.getInstance().e(AppLogger.TAG.UI, e);

						return e;
					}

					if (upr.upload.method.equals("POST"))
					{
						upr.upload.params.put("file", m_upFile);

						final RequestFuture<String> requestFuture2 = RequestFuture.newFuture();

						//NetSpeedChecker.getInstance().start(context, NetSpeedChecker.TYPE_UPLOAD);

						try
						{
							final UploadToS3Request request = new UploadToS3Request(
									Request.Method.POST,
									upr.upload.url,
									upr.upload.params,
									true,
									requestFuture2,
									requestFuture2);

							request.setShouldCache(false);
							request.setRetryPolicy(RETRY_POLICY);
							RequestManager.getInstance().addToRequestQueue(request);

							requestFuture2.get();
						}
						catch (Exception e)
						{
							FirebaseCrashLog.log("UploadImage UploadToS3Request Requests Section Exception");
							FirebaseCrashLog.logException(context, e);

							AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 1 Exception");
							AppLogger.getInstance().e(AppLogger.TAG.UI, e);

							return e;
						}

						//onProgress(listener, 90);
						onProgress(listener, 70);
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 3");
						//NetSpeedChecker.getInstance().end(context, NetSpeedChecker.TYPE_UPLOAD, m_VideoFile.length());

						try
						{
							// GIF
							if (isGif == true)
							{
								if (m_isCrop)
									AnalyticsManager.getInstance().eventStatsUploadImageType(context, "GIF_CROP");
								else
									AnalyticsManager.getInstance().eventStatsUploadImageType(context, "GIF");

							}
							else
							{
								if (m_isCrop)
									AnalyticsManager.getInstance().eventStatsUploadImageType(context, "IMAGE_CROP");
								else
									AnalyticsManager.getInstance().eventStatsUploadImageType(context, "IMAGE");
							}
						}
						catch (Exception e)
						{
						}

						try
						{
							final RequestFuture<AsyncStats.Empty> requestFuture3 = RequestFuture.newFuture();

							Requests.authRequestByPost(
									upr.after_upload.url,
									null,
									AsyncStats.Empty.class,
									requestFuture3,
									requestFuture3);

							requestFuture3.get();
						}
						catch (Exception e)
						{
							FirebaseCrashLog.log("UploadImage after_upload Requests Section Exception");
							FirebaseCrashLog.logException(context, e);

							AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 2 Exception");
							AppLogger.getInstance().e(AppLogger.TAG.UI, e);

							return e;
						}

						onProgress(listener, 100);
						AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 4");
					}
				}
				catch (Exception e)
				{
					FirebaseCrashLog.log("UploadImage Section Exception");
					FirebaseCrashLog.logException(context, e); // firebase_crash

					AppLogger.getInstance().e(AppLogger.TAG.UI, "### Upload - UploadData uploadImage 3 Exception");
					AppLogger.getInstance().e(AppLogger.TAG.UI, e);
					return e;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Object o)
			{
				super.onPostExecute(o);

				if (o instanceof Exception)
				{
					String errorMsg = ((Exception) o).getLocalizedMessage();
					onCompleted(listener, false, errorMsg);
					return;
				}

				AppLogger.getInstance().d(AppLogger.TAG.UI, "### Upload - UploadData uploadImage Completed!!!");
				onCompleted(listener, true, null);
			}
		};

		asyncUploadTask.execute();
	}

	private void createResizedImageFileBaseOnHeight(File src, File dst, int height, int quality) throws IOException
	{
		Bitmap bitmap = BitmapUtils.createBitmapFromFile(src, Bitmap.Config.ARGB_8888, -1, height);
		BitmapUtils.saveBitmap(bitmap, dst, Bitmap.CompressFormat.JPEG, quality);
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

	private boolean isGifMode(Context context)
	{
		boolean isGif = false;

		if (UrlFactory.isStagingServer(context) == true)
		{
			if (m_uri != null && m_uri.contains(GifLiveWallpaperFileUtils.GIF_FILE_EXTENDYION))
			{
				isGif = true;
			}
			else if (m_uri != null && m_uri.startsWith("content://") == true)
			{
				Uri uri = Uri.parse(m_uri);
				String MIMEType = getMIMETypeFromUri(context, uri);

				if (MIMEType != null && MIMEType.contains("gif"))
					isGif = true;
			}
		}

		return isGif;
	}

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
			FirebaseCrashLog.log("UploadData getMIMETypeFromUri Exception");
			FirebaseCrashLog.logException(e);
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
			FirebaseCrashLog.log("UploadData getFilePathFromUri Exception");
			FirebaseCrashLog.logException(e);
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

		void onCompletedSameImage(long uid, boolean isSuccess, boolean isExists, List<String> tags, String errorMessage);
	}

	//========================================================================
	// Constants
	//========================================================================

	//private static DefaultRetryPolicy RETRY_POLICY = new DefaultRetryPolicy(5000, 3, 1.5f);
	private static DefaultRetryPolicy RETRY_POLICY = new DefaultRetryPolicy(10000, 3, 1.5f);
	private static int IMAGE_HEIGHT = 2560;
	private static int UPLOAD_QUALITY = 90;

	private static int ANNOTATION_HEIGHT = 240;

	//========================================================================
	// Variables
	//========================================================================

	String m_title;
	String m_description;
	List<String> m_userTags;
	List<String> m_originTags;
	int m_license;
	String m_uri;
	String m_wepickId;
	int m_filterIndex;

	long m_uid;
	File m_upFile;

	private boolean m_isCrop = false;
}