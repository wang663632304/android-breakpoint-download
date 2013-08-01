package com.tubb.mutildownload;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;

public class MutilDownloadService {

	private MutilDownloadHandler handler;
	
	private MutilDownloadDBService progressService;
	private String urlPath;
	
	private ExecutorService exec;
	private ArrayList<Future<Object>> futures = new ArrayList<Future<Object>>(); 
	private HashMap<String, ThreadControler> execResultsMap = new HashMap<String, ThreadControler>();
	
	protected MutilDownloadService(Context context) {
		progressService = new MutilDownloadDBService(context);
		exec = Executors.newCachedThreadPool();
	}
	
	protected void start(String urlPath, String fileSaveName, long fileSize, String fileSaveDir, int mutilThreadNum, MutilDownloadHandler handler){
		
		init(handler, urlPath);
		
		File dirFile = makeSaveDirs(fileSaveDir);
		
		File filePath = new File(dirFile.getAbsolutePath()+File.separator+fileSaveName);

		if(mutilThreadNum > MutilDownloadClient.DEFAULT_MUTILTHREADNUM){
			mutilDownload(urlPath, fileSize, mutilThreadNum, filePath);
		}else{
			singleDownload(urlPath, fileSize, filePath);
		}
	}

	private void singleDownload(String urlPath, long fileSize, File filePath) {
		if(!filePath.exists()){
			try {
				filePath.createNewFile();
			} catch (IOException e) {
				// TODO
			}
		}
		
		long downloadedLength = filePath.length();
		if(downloadedLength < fileSize){
			execResultsMap.put(String.valueOf(MutilDownloadClient.DEFAULT_MUTILTHREADNUM), new ThreadControler(false, null));
			SingleDownloadThread sdt = new SingleDownloadThread(this, urlPath, filePath, MutilDownloadClient.DEFAULT_MUTILTHREADNUM);
			futures.add(exec.submit(Executors.callable(sdt)));
			exec.shutdown();
		}
	}

	private void mutilDownload(String urlPath, long fileSize,
			int mutilThreadNum, File filePath) {
		MutilDownloadThread[] downloadThreads = new MutilDownloadThread[mutilThreadNum];
		
		long preThreadBlock = (fileSize % mutilThreadNum) == 0 ? fileSize / mutilThreadNum : fileSize / mutilThreadNum + 1;
		
		// long time TODO
		Map<Integer, Long> threadsProgress = progressService.getMutilThreadProgress(urlPath);
		
		int threadsProgressSize = threadsProgress.size();
		
		if(threadsProgressSize != 0 && threadsProgressSize != mutilThreadNum){
			// long time TODO
			progressService.delete(urlPath);
		}else{
			for (int i = 0; i < mutilThreadNum; i++) {
				threadsProgress.put(i+1, 0L);//初始化每条线程已经下载的数据长度为0
			}
			progressService.save(urlPath, threadsProgress);
		}
		
		if(!filePath.exists()){
			RandomAccessFile raf = null;
			try {
				filePath.createNewFile();
				raf = new RandomAccessFile(filePath, "rw");
				raf.setLength(fileSize);
			} catch (IOException e) {
				// TODO
				//throw e;
			}finally{
				if(raf != null){
					try {
						raf.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		for (int i = 0; i < downloadThreads.length; i++) {//开启线程进行下载
			int threadId = i+1;
			long downloadedLength = threadsProgress.get(threadId);
			execResultsMap.put(String.valueOf(threadId), new ThreadControler(false, null));
			if(downloadedLength < preThreadBlock){//判断线程是否已经完成下载,否则继续下载	
				downloadThreads[i] = new MutilDownloadThread(this, urlPath, filePath, preThreadBlock, downloadedLength, threadId);
				futures.add(exec.submit(Executors.callable(downloadThreads[i])));
			}
		}
		exec.shutdown();
	}

	private void init(MutilDownloadHandler handler, String urlPath) {
		this.handler = handler;
		this.urlPath = urlPath; 
		exec = Executors.newCachedThreadPool();
		futures.clear();
		execResultsMap.clear();
	}

	private File makeSaveDirs(String fileSaveDir) {
		File dirFile = new File(fileSaveDir);
		if(!dirFile.exists()){
			dirFile.mkdirs();
		}
		return dirFile;
	}

	protected synchronized void update(String urlPath, int threadId, long pos) {
		progressService.updatePreThreadProgress(urlPath, threadId, pos);
	}
	
	protected synchronized void sendDownloadSuccessMsg(int threadId){
		execResultsMap.get(String.valueOf(threadId)).setDownloadSucc(true);
		sendMsg(threadId);
	}
	
	protected synchronized void sendDownloadFailureMsg(int threadId, Throwable e){
		ThreadControler threadControler = execResultsMap.get(String.valueOf(threadId));
		threadControler.setDownloadSucc(false);
		threadControler.setE(e);
		sendMsg(threadId);
	}
	
	private void sendMsg(int threadId) {
		boolean isAllThreadComplete = isAllThradComplete(threadId);
		if(isAllThreadComplete){
			
			handler.sendFinishMessage();
			
			boolean isAllThradSucc = true;
			Collection<ThreadControler> values = execResultsMap.values();
			for (ThreadControler value : values) {
				if(!value.isDownloadSucc()){
					isAllThradSucc = false;
				}
			}
			
			if(isAllThradSucc){
				progressService.delete(urlPath);
				handler.sendSuccessMessage("成功完成下载");
			}else{
				ArrayList<Throwable> throwables = new ArrayList<Throwable>();
				for (ThreadControler controler : execResultsMap.values()) {
					throwables.add(controler.getE());
				}
				handler.sendFailureMessage(throwables, "下载失败");
			}
			
		}
	}
	
	private boolean isAllThradComplete(int threadId) {
		boolean allThreadsComplete = true;
		
		for (int i = 0; i < futures.size(); i++) {
			Future<Object> future = futures.get(i);
			if(i != (threadId - 1) && !future.isDone()){
				allThreadsComplete = false;
				break;
			}
		}
		
		return allThreadsComplete;
	}
	

	private static class ThreadControler{
		private boolean isDownloadSucc;
		private Throwable e;
		public ThreadControler(boolean isDownloadSucc, Throwable e) {
			this.isDownloadSucc = isDownloadSucc;
			this.e = e;
		}
		public boolean isDownloadSucc() {
			return isDownloadSucc;
		}
		public void setDownloadSucc(boolean isDownloadSucc) {
			this.isDownloadSucc = isDownloadSucc;
		}
		public Throwable getE() {
			return e;
		}
		public void setE(Throwable e) {
			this.e = e;
		}
		
	}
	
}
