package com.ogqcorp.bgh.upload;

import java.util.LinkedHashMap;

import com.google.gson.annotations.SerializedName;

public final class UploadVideoPrepareResult
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public UploadVideoPrepareResult()
	{
		// Nothing
	}

	@SerializedName("after_upload")
	public AfterUpload m_afterUpload;

	@SerializedName("abort_upload")
	public AbortUpload m_abortUpload;

	@SerializedName("video_upload")
	public Upload m_videoUpload;

	@SerializedName("thumbnail_upload")
	public Upload m_thumbnailUpload;

	public static class AfterUpload
	{
		public String url;
		public String method;
	}

	public static class AbortUpload
	{
		public String url;
		public String method;
	}

	public static class Upload
	{
		public String url;
		public String method;
		public LinkedHashMap<String, Object> params;
	}
}
