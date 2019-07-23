package com.ogqcorp.bgh.upload;

import java.util.LinkedHashMap;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public final class UploadGalleryPrepareResult
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public UploadGalleryPrepareResult()
	{
		// Nothing
	}

	@SerializedName("gallery_id")
	public String m_galleryId;

	@SerializedName("after_upload")
	public AfterUpload m_afterUpload;

	@SerializedName("abort_upload")
	public AbortUpload m_abortUpload;

	@SerializedName("file_upload")
	public List<Upload> m_uploads;

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
		public String arrangement;
		public LinkedHashMap<String, Object> params;
	}
}
