package com.ogqcorp.bgh.upload;

import java.util.LinkedHashMap;

public final class UploadPrepareResult
{
	//=========================================================================
	// Constructors
	//=========================================================================

	public UploadPrepareResult()
	{
		// Nothing
	}

	public AfterUpload after_upload;
	public String uuid;
	public Upload upload;

	public static class AfterUpload
	{
		public String url;
		public String method;
	}

	public static class Upload
	{
		public String url;
		public LinkedHashMap<String, Object> params;
		public String method;
	}
}
