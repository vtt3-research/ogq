package com.ogqcorp.bgh.upload;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.google.android.gms.common.images.Size;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ogqcorp.bgh.spirit.data.Gallery;
import com.ogqcorp.bgh.spirit.data.GalleryArtwork;
import com.ogqcorp.bgh.spirit.request.Requests;
import com.ogqcorp.bgh.spirit.request.factory.UrlFactory;
import com.ogqcorp.bgh.spirit.system.AsyncStats;
import com.ogqcorp.commons.Log;
import com.ogqcorp.commons.request.volley.RequestManager;

public class UploadGalleryData implements Parcelable
{

	public UploadGalleryData(Gallery gallery)
	{
		m_gallery = gallery;
	}

	//========================================================================
	// Parcelable Methods
	//========================================================================

	public UploadGalleryData(Parcel in)
	{
		m_gallery = in.readParcelable(Gallery.class.getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeParcelable(m_gallery, flags);
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	public static final Creator<UploadGalleryData> CREATOR = new Creator<UploadGalleryData>()
	{
		public UploadGalleryData createFromParcel(Parcel in)
		{
			return new UploadGalleryData(in);
		}

		public UploadGalleryData[] newArray(int size)
		{
			return new UploadGalleryData[size];
		}
	};

	//========================================================================
	// Public Methods
	//========================================================================

	public long uploadRequesMetaInfo(final Context context, final ProgressListener listener)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				uploadMetaInfo(context, listener);
			}
		});

		m_uid = thread.getId();
		thread.start();

		return m_uid;
	}

	public long editRequestMetaInfo(final Context context, final ProgressListener listener)
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				editMetaInfo(context, listener);
			}
		});

		m_uid = thread.getId();
		thread.start();

		return m_uid;
	}

	//========================================================================
	// Private Methods
	//========================================================================

	public void createContentHashNSize()
	{
		Size size = null;
		String url = null;

		//Cover Image
		url = m_gallery.getCoverUrl();
		size = getSize(url);
		m_gallery.setWidth(size.getWidth());
		m_gallery.setHeight(size.getHeight());
		m_gallery.setCoverHash(getContentHash(url));

		//Artworks Image
		int index = 1;
		for (GalleryArtwork artwork : m_gallery.getArtworkList())
		{
			url = artwork.getImageUrl();
			size = getSize(url);
			artwork.setWidth(size.getWidth());
			artwork.setHeight(size.getHeight());
			artwork.setArrangement(String.valueOf(index++));
			artwork.setContentHash(getContentHash(url));
		}
	}

	private String getContentHash(String path)
	{
		try
		{
			Uri uri = Uri.parse(path);
			File file = new File(uri.getPath());

			byte[] b = FileUtils.readFileToByteArray(file);
			return bin2hex(getHash(b));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	private Size getSize(String path)
	{
		BitmapFactory.Options bitMapOption = new BitmapFactory.Options();
		bitMapOption.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bitMapOption);
		int width = bitMapOption.outWidth;
		int height = bitMapOption.outHeight;

		return new Size(width, height);
	}

	private void onProgress(final ProgressListener listener, final int progress)
	{
		listener.onProgress(m_uid, progress);
	}

	private void onCompleted(final ProgressListener listener, final boolean isSuccessed, final String galleryId, final String errorMsg)
	{
		listener.onCompleted(m_uid, isSuccessed, galleryId, errorMsg);
	}

	private void uploadMetaInfo(final Context context, final ProgressListener listener)
	{
		AsyncTask<Object, Object, Object> asyncUploadTask = new AsyncTask<Object, Object, Object>()
		{
			@Override
			protected Object doInBackground(Object... params)
			{
				int progress = 0;
				Object response;
				String galleryId = null;
				String afterUrl = null;
				String abortUrl = null;
				boolean uploadSuccess = false;

				try
				{
					onProgress(listener, progress);
					createContentHashNSize();

					onProgress(listener, progress += 10);
					String url = UrlFactory.galleryUpload();
					HashMap<String, Object> param = m_gallery.toMap();

					final RequestFuture<Object> requestFuture = RequestFuture.newFuture();

					try
					{
						Log.d("UPLOAD GALLERY : upload gallery meta info");
						Requests.authRequestByPost(
								url,
								param,
								Object.class,
								requestFuture,
								requestFuture);

						onProgress(listener, progress += 20);

						response = requestFuture.get();
					}
					catch (Exception e)
					{
						Log.d("UPLOAD GALLERY : upload gallery meta info : Exception!!");
						onCompleted(listener, false, null, e.getLocalizedMessage());
						return e;
					}

					// Upload Image File to S3
					if (response != null)
					{
						Gson gson = new GsonBuilder().create();
						JSONObject object = new JSONObject((HashMap<String, Object>) response);
						UploadGalleryPrepareResult ugpr = gson.fromJson(object.getJSONObject("data").toString(), UploadGalleryPrepareResult.class);

						galleryId = ugpr.m_galleryId;
						afterUrl = ugpr.m_afterUpload.url;
						abortUrl = ugpr.m_abortUpload.url;

						RequestFuture<String> requestFuture2;

						int i = 0;
						int length = ugpr.m_uploads.size();
						int progressUnit = 70 / length;

						for (UploadGalleryPrepareResult.Upload upload : ugpr.m_uploads)
						{
							if (upload.method.equals("POST"))
							{
								if (upload.arrangement.equals("0"))
								{
									upload.params.put("file", new File(m_gallery.getCoverUrl()));
								}
								else
								{
									upload.params.put("file", new File(m_gallery.getArtworkList().get(i++).getImageUrl()));
								}

								requestFuture2 = RequestFuture.newFuture();
								try
								{
									Log.d("UPLOAD GALLERY : upload gallery image file");

									final UploadToS3Request request = new UploadToS3Request(
											Request.Method.POST,
											upload.url,
											upload.params,
											true,
											requestFuture2,
											requestFuture2);

									request.setShouldCache(false);
									//request.setRetryPolicy(RETRY_POLICY);
									RequestManager.getInstance().addToRequestQueue(request);
									response = requestFuture2.get(180, TimeUnit.SECONDS);
								}
								catch (Exception e)
								{
									Log.d("UPLOAD GALLERY : upload gallery image file : Exception!!");
									onCompleted(listener, false, null, e.getLocalizedMessage());
									return e;
								}
							}
							onProgress(listener, progress += progressUnit);
						}

						onProgress(listener, 90);

						try
						{
							final RequestFuture<AsyncStats.Empty> requestFuture3 = RequestFuture.newFuture();

							Log.d("UPLOAD GALLERY : request afterUrl");

							Requests.authRequestByPost(
									afterUrl,
									null,
									AsyncStats.Empty.class,
									requestFuture3,
									requestFuture3);

							response = requestFuture3.get();
						}
						catch (Exception e)
						{
							Log.d("UPLOAD GALLERY : request afterUrl : Exception!!");
							onCompleted(listener, false, null, e.getLocalizedMessage());
							return e;
						}

						uploadSuccess = true;
						onProgress(listener, 100);
					}
				}
				catch (Exception e)
				{
					return e;
				}
				finally
				{
					Log.d("UPLOAD GALLERY : finally");
					if (uploadSuccess == false)
					{
						//실패처리
						try
						{
							final RequestFuture<AsyncStats.Empty> requestFuture4 = RequestFuture.newFuture();

							Log.d("UPLOAD GALLERY : request abortUrl");
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
							Log.d("UPLOAD GALLERY : request abortUrl : Exception!!");
							onCompleted(listener, false, null, e.getLocalizedMessage());
							return e;
						}
					}
					onCompleted(listener, true, galleryId, null);
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
					onCompleted(listener, false, null, errorMsg);
					return;
				}
			}
		};

		asyncUploadTask.execute();
	}

	private void editMetaInfo(final Context context, final ProgressListener listener)
	{
		AsyncTask<Object, Object, Object> asyncUploadTask = new AsyncTask<Object, Object, Object>()
		{
			@Override
			protected Object doInBackground(Object... params)
			{
				Object response;
				String galleryId = null;

				try
				{
					onProgress(listener, 10);

					String url = UrlFactory.galleryEdit(m_gallery.getId());
					HashMap<String, Object> param = m_gallery.toMap();

					final RequestFuture<Object> requestFuture = RequestFuture.newFuture();
					try
					{
						Log.d("EDIT GALLERY : upload gallery meta info");
						Requests.authRequestByPut(
								url,
								param,
								Object.class,
								requestFuture,
								requestFuture);

						onProgress(listener, 30);

						response = requestFuture.get();
					}
					catch (Exception e)
					{
						Log.d("EDIT GALLERY : upload gallery meta info : Exception!!");
						onCompleted(listener, false, null, e.getLocalizedMessage());
						return e;
					}

					onProgress(listener, 100);
					onCompleted(listener, true, m_gallery.getId(), null);
				}
				catch (Exception e)
				{
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
					onCompleted(listener, false, null, errorMsg);
					return;
				}
			}
		};

		asyncUploadTask.execute();
	}

	public byte[] getHash(byte[] b) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(b);
		return digest.digest();
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

		void onCompleted(long uid, boolean isSuccess, String galleryId, String errorMessage);
	}

	//========================================================================
	// Constants
	//========================================================================

	//private static DefaultRetryPolicy RETRY_POLICY = new DefaultRetryPolicy(120000, 3, 1.5f);

	//========================================================================
	// Variables
	//========================================================================

	long m_uid;
	Gallery m_gallery;
}