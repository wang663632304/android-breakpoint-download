package com.example.mutildownloadtest;

import java.util.List;

import com.tubb.mutildownload.MutilDownloadClient;
import com.tubb.mutildownload.MutilDownloadHandler;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.content.Context;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
	private Button bt_start;
	
	Context mContext;
	
	MutilDownloadClient downloadClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		mContext = this;

		downloadClient = new MutilDownloadClient(mContext);
		
		bt_start = (Button) findViewById(R.id.bt_start);

		bt_start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String urlPath = "http://gdown.baidu.com/data/wisegame/83a1deee8c943eb2/SubwaySurf.apk";
				
				downloadClient.start(urlPath, "SubwaySurf.apk", 26252477, "/sdcard/Download", 3, new MutilDownloadHandler(){
					@Override
					public void onFailure(List<Throwable> e,
							String responseFailureMsg) {
						Log.i(TAG, responseFailureMsg);
						for (Throwable throwable : e) {
							Log.i("INFO", throwable.getMessage());
						}
					}
					
					@Override
					public void onSuccess(String responseMsg) {
						Log.i("INFO", responseMsg);
					}
					
					@Override
					public void onFinish() {
						Log.i("INFO", "下载完成");
					}
				});
				
			}
		});
		
	}

}
