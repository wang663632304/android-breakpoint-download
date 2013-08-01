package com.tubb.mutildownload;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class MutilDownloadClient {
	
	private Context mContext;
	protected static final int DEFAULT_MUTILTHREADNUM = 1;
	private static final String DEFAULT_FILESAVEDIR = Environment.getExternalStorageDirectory().getPath()+File.separator+"Download";

	public MutilDownloadClient(Context context){
		this.mContext = context;
	}
	
	public void start(String urlPath, String fileSaveName, long fileSize, MutilDownloadHandler handler){
		start(urlPath, fileSaveName, fileSize, DEFAULT_FILESAVEDIR, DEFAULT_MUTILTHREADNUM, handler);
	}
	
	public void start(String urlPath, String fileSaveName, long fileSize, String fileSaveDir, int mutilThreadNum, MutilDownloadHandler handler){
		MutilDownloadService mds = new MutilDownloadService(mContext);
		mds.start(urlPath, fileSaveName, fileSize, fileSaveDir, mutilThreadNum, handler);
	}

}
