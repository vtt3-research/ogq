package com.ogqcorp.bgh.upload;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.boye.httpclientandroidlib.HttpEntity;
import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntityBuilder;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyLog;
import com.ogqcorp.commons.request.volley.HttpHeaderParserPreventNoCache;
import com.ogqcorp.commons.request.volley.ParseErrorEx;

public class UploadToS3Request extends Request<String>
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public UploadToS3Request(int method, String url, HashMap<String, Object> paramsMap, Listener<String> listener, ErrorListener errorListener)
	{
		this(method, url, paramsMap, false, false, listener, errorListener, 0);
	}

	public UploadToS3Request(int method, String url, HashMap<String, Object> paramsMap, boolean isMultipart, Listener<String> listener, ErrorListener errorListener)
	{
		this(method, url, paramsMap, isMultipart, false, listener, errorListener, 0);
	}

	public UploadToS3Request(int method, String url, HashMap<String, Object> paramsMap, boolean isMultipart, boolean isJsonRequest, Listener<String> listener, ErrorListener errorListener)
	{
		this(method, url, paramsMap, isMultipart, isJsonRequest, listener, errorListener, 0);
	}

	public UploadToS3Request(int method, String url, HashMap<String, Object> paramsMap, boolean isMultipart, boolean isJsonRequest, Listener<String> listener, ErrorListener errorListener, int conditionalMaxAge)
	{
		super(method, url, errorListener);

		if (method == Method.POST || method == Method.PUT)
		{
			buildParameters(paramsMap, isMultipart, isJsonRequest);
		}

		m_listener = listener;

		m_conditionalMaxAge = conditionalMaxAge;
	}

	//=========================================================================
	// Override Methods
	//=========================================================================

	@Override
	protected Map<String, String> getParams() throws AuthFailureError
	{
		if (m_paramsMap != null) return m_paramsMap;

		return null;
	}

	@Override
	public String getBodyContentType()
	{
		if (m_multipartEntity != null)
		{
			return m_multipartEntity.getContentType().getValue();
		}

		return super.getBodyContentType();
	}

	@Override
	public byte[] getBody() throws AuthFailureError
	{
		if (m_multipartEntity != null)
		{
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();

			try
			{
				m_multipartEntity.writeTo(bos);
			}
			catch (IOException e)
			{
				VolleyLog.e("IOException writing to ByteArrayOutputStream");
			}

			return bos.toByteArray();
		}

		return super.getBody();
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response)
	{
		try
		{
			String resultData = null;

			if (response.data != null && response.data.length != 0)
			{
				resultData = new String(response.data);
			}

			final Cache.Entry entry = HttpHeaderParserPreventNoCache.parseCacheHeaders(response, m_conditionalMaxAge);
			return Response.success(resultData, entry);
		}
		catch (Exception e)
		{
			return Response.error(new ParseErrorEx(e, response));
		}
	}

	@Override
	protected void deliverResponse(String response)
	{
		if (m_listener != null)
		{
			m_listener.onResponse(response);
		}
	}

	//=========================================================================
	// Private Methods
	//=========================================================================

	private void buildParameters(HashMap<String, Object> paramsMap, boolean isMultipart, boolean isJsonRequest)
	{
		if (paramsMap != null)
		{
			if (isMultipart == true)
			{
				final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

				multipartEntityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

				for (Map.Entry<String, Object> entry : paramsMap.entrySet())
				{
					final Object value = entry.getValue();

					if (value != null)
					{
						addBody(multipartEntityBuilder, entry.getKey(), value);
					}
				}

				m_multipartEntity = multipartEntityBuilder.build();
			}
			else
			{
				m_paramsMap = new HashMap<>();

				for (Map.Entry<String, Object> entry : paramsMap.entrySet())
				{
					final Object value = entry.getValue();

					if (value != null)
					{
						m_paramsMap.put(entry.getKey(), value.toString());
					}
				}
			}
		}
	}

	private void addBody(MultipartEntityBuilder multipartEntityBuilder, String key, Object value)
	{
		if (value instanceof File)
		{
			multipartEntityBuilder.addBinaryBody(key, (File) value);
		}
		else
		{
			multipartEntityBuilder.addTextBody(key, value.toString());
		}
	}

	//=========================================================================
	// Variables
	//=========================================================================

	private final Listener<String> m_listener;
	private final int m_conditionalMaxAge;
	private HashMap<String, String> m_paramsMap;
	private HttpEntity m_multipartEntity;
}