package com.tubb.mutildownload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.http.HttpStatus;

public class MutilDownloadThread implements Runnable {

	private File saveFile;
	private String urlPath;
	private long block;
	private int threadId = -1;
	private long downLength;
	private MutilDownloadService downloader;

	protected MutilDownloadThread(MutilDownloadService downloader, String urlPath,
			File saveFile, long block, long downLength, int threadId) {
		this.urlPath = urlPath;
		this.saveFile = saveFile;
		this.block = block;
		this.downloader = downloader;
		this.threadId = threadId;
		this.downLength = downLength;
	}

	@Override
	public void run() {
		if (downLength < block) {
			download();
		}
	}

	private void download() {

		RandomAccessFile raf = null;
		BufferedInputStream bis = null;
		HttpURLConnection conn = null;
		FileChannel fc = null;

		try {
			URL url = new URL(urlPath);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", url.toString());
			conn.setRequestProperty("Charset", "UTF-8");

			long startPos = block * (threadId - 1) + downLength;// 开始位置
			long endPos = block * threadId - 1;// 结束位置
			conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// 设置获取实体数据的范围
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");

			int respCode = conn.getResponseCode();

			if (respCode == HttpStatus.SC_PARTIAL_CONTENT) {
				bis = new BufferedInputStream(conn.getInputStream());
				byte[] buffer = new byte[1024 * 4];
				int offset = 0;
				raf = new RandomAccessFile(this.saveFile, "rw");
				fc = raf.getChannel();
				fc.position(startPos);
				while (((offset = bis.read(buffer)) != -1)) {
					fc.write(ByteBuffer.wrap(buffer, 0, offset));
					downLength += offset;
				}
				downloader.update(urlPath, threadId, downLength);
				downloader.sendDownloadSuccessMsg(threadId);
			} else {
				downloader.sendDownloadFailureMsg(threadId, new Exception(
						String.format("error code : %d", respCode)));
			}

		} catch (UnknownHostException e1) {
			update(e1);
		}catch (SocketException e2) {
			update(e2);
		}catch (SocketTimeoutException e3) {
			update(e3);
		}catch (IOException e4) { // 线程下载过程中被中断
			update(e4);
		}finally {
			close(fc,raf, bis, conn);
		}
	}
	
	private void update(IOException e4) {
		downloader.update(urlPath, threadId, downLength);
		downloader.sendDownloadFailureMsg(threadId, e4);
	}

	private void close(FileChannel fc, RandomAccessFile raf, InputStream inStream,
			HttpURLConnection conn) {
		if(fc != null){
			try {
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (conn != null) {
			conn.disconnect();
		}
	}
//	private void download2() {
//
//		RandomAccessFile raf = null;
//		BufferedInputStream bis = null;
//		HttpURLConnection conn = null;
//
//		try {
//			URL url = new URL(urlPath);
//			conn = (HttpURLConnection) url.openConnection();
//			conn.setConnectTimeout(10 * 1000);
//			conn.setRequestMethod("GET");
//			conn.setRequestProperty(
//					"Accept",
//					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
//			conn.setRequestProperty("Accept-Language", "zh-CN");
//			conn.setRequestProperty("Referer", url.toString());
//			conn.setRequestProperty("Charset", "UTF-8");
//
//			long startPos = block * (threadId - 1) + downLength;// 开始位置
//			long endPos = block * threadId - 1;// 结束位置
//			conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// 设置获取实体数据的范围
//			conn.setRequestProperty(
//					"User-Agent",
//					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
//			conn.setRequestProperty("Connection", "Keep-Alive");
//
//			int respCode = conn.getResponseCode();
//
//			if (respCode == HttpStatus.SC_PARTIAL_CONTENT) {
//				bis = new BufferedInputStream(conn.getInputStream());
//				byte[] buffer = new byte[1024 * 4];
//				int offset = 0;
//				raf = new RandomAccessFile(this.saveFile, "rwd");
//				raf.seek(startPos);
//				while (((offset = bis.read(buffer)) != -1)) {
//					raf.write(buffer, 0, offset);
//					downLength += offset;
//				}
//				downloader.update(urlPath, threadId, downLength);
//				downloader.sendDownloadSuccessMsg(threadId);
//			} else {
//				downloader.sendDownloadFailureMsg(threadId, new Exception(
//						String.format("error code : %d", respCode)));
//			}
//
//		} catch (UnknownHostException e1) {
//			update(e1);
//		}catch (SocketException e2) {
//			update(e2);
//		}catch (SocketTimeoutException e3) {
//			update(e3);
//		}catch (IOException e4) { // 线程下载过程中被中断
//			update(e4);
//		}finally {
//			close(raf, bis, conn);
//		}
//	}
}
