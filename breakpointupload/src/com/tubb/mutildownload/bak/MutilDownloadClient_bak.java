package com.tubb.mutildownload.bak;
//package com.justsy.eleschoolbag.mutildownload;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.util.HashMap;
//import java.util.Map;
//
//import android.content.Context;
//import android.os.Environment;
//
//public class MutilDownloadClient_bak {
//
//	private static final int DEFAULT_MUTILTHREADNUM = 1;
//	private static final String DEFAULT_FILESAVEDIR = Environment.getExternalStorageDirectory().getPath()+File.separator+"Download";
//	
//	private boolean isFinish = false;
//	private boolean isStop = false;
//	
//	private Context context;
//	private String urlPath;
//	private MutilDownloadHandler mutilDownloadHandler;
//	private int mutilThreadNum = DEFAULT_MUTILTHREADNUM;
//	private String fileSaveDir = DEFAULT_FILESAVEDIR;
//	private String fileSaveName;
//	private long fileSize = 0L;
//	
//	private MutilDownloadDBService progressService;
//	
//	private Map<Integer, Long> threadsProgress = new HashMap<Integer, Long>();
//	
//	private long preThreadBlock;
//	
//	private MutilDownloadThread[] downloadThreads;
//	
//	public MutilDownloadClient_bak(Context context, String urlPath,
//			MutilDownloadHandler mutilDownloadHandler, long fileSize) {
//
//		this.context = context;
//		this.urlPath = urlPath;
//		this.mutilDownloadHandler = mutilDownloadHandler;
//		this.fileSize = fileSize;
//		
//	}
//
//	public MutilDownloadClient_bak(Context context, String urlPath,
//			MutilDownloadHandler mutilDownloadHandler, int mutilThreadNum,
//			String fileSaveDir, String fileSaveName, long fileSize) {
//		
//		this.context = context;
//		this.urlPath = urlPath;
//		this.mutilDownloadHandler = mutilDownloadHandler;
//		this.mutilThreadNum = mutilThreadNum;
//		this.fileSaveDir = fileSaveDir;
//		this.fileSaveName = fileSaveName;
//		this.fileSize = fileSize;
//		
//	}
//
//	public void start() throws IOException{
//		
//		if(fileSize <= 0L){
//			throw new IllegalArgumentException("the illegal fileSize exception");
//		}
//		
//		if(fileSaveName == null){
//			
//			/** init fileSaveName */
//			fileSaveName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
//			if(fileSaveName == null || "".equals(fileSaveName)){
//				throw new IllegalArgumentException("can't obtain fileSaveName exception");
//			}
//			
//		}
//		
//		progressService = new MutilDownloadDBService(context);
//		
//		// long time TODO
//		threadsProgress = progressService.getMutilThreadProgress(urlPath);
//		
//		preThreadBlock = (fileSize % mutilThreadNum) == 0 ? fileSize / mutilThreadNum : fileSize / mutilThreadNum + 1;
//		
//		int threadsProgressSize = threadsProgress.size();
//		
//		if(threadsProgressSize != 0 && threadsProgressSize != mutilThreadNum){
//			// long time TODO
//			progressService.delete(urlPath);
//		}else{
//			for (int i = 0; i < mutilThreadNum; i++) {
//				threadsProgress.put(i+1, 0L);//初始化每条线程已经下载的数据长度为0
//			}
//		}
//		
//		File dirFile = new File(fileSaveDir);
//		if(!dirFile.exists()){
//			dirFile.mkdirs();
//		}
//		
//		File filePath = new File(dirFile.getAbsolutePath()+File.separator+fileSaveName);
//		
//		/** mutil thread */
//		if(mutilThreadNum > DEFAULT_MUTILTHREADNUM){
//			
//			if(!filePath.exists()){
//				RandomAccessFile raf = null;
//				try {
//					filePath.createNewFile();
//					raf = new RandomAccessFile(filePath, "rw");
//					raf.setLength(fileSize);
//				} catch (IOException e) {
//					throw e;
//				}finally{
//					if(raf != null){
//						try {
//							raf.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//			
//			downloadThreads = new MutilDownloadThread[mutilThreadNum];
//			
//			for (int i = 0; i < downloadThreads.length; i++) {//开启线程进行下载
//				long downloadedLength = threadsProgress.get(i+1);
//				if(downloadedLength < preThreadBlock){//判断线程是否已经完成下载,否则继续下载	
//					downloadThreads[i] = new MutilDownloadThread(this, urlPath, filePath, preThreadBlock, downloadedLength, i+1);
//					downloadThreads[i].start();
//				}else{
//					downloadThreads[i] = null;
//				}
//			}
//			
//		}else{ // one thread
//			// TODO
//			if(!filePath.exists()){
//				try {
//					filePath.createNewFile();
//				} catch (IOException e) {
//					throw e;
//				}
//			}
//			
//			long downloadedLength = filePath.length();
//			if(downloadedLength < preThreadBlock){
//				downloadThreads[0] = new MutilDownloadThread(this, urlPath, filePath, preThreadBlock, downloadedLength, 1);
//				downloadThreads[0].start();
//			}
//			
//		}
//		
//	}
//
//	public void stop() {
//	}
//
//	public boolean isFinish() {
//		return isFinish;
//	}
//
//	public void setFinish(boolean isFinish) {
//		this.isFinish = isFinish;
//	}
//
//	public boolean isStop() {
//		return isStop;
//	}
//
//	public void setStop(boolean isStop) {
//		this.isStop = isStop;
//	}
//
//	public synchronized void update(int threadId, long pos) {
//		threadsProgress.put(threadId, pos);
//		progressService.update(urlPath, threadsProgress);
//	}
//}
