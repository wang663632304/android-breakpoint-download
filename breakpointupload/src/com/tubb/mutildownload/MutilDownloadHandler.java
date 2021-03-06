package com.tubb.mutildownload;

import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MutilDownloadHandler {

    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;
    protected static final int START_MESSAGE = 2;
    protected static final int FINISH_MESSAGE = 3;

    private Handler handler;
    
    protected MutilDownloadHandler(){
        if(Looper.myLooper() != null) {
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                	MutilDownloadHandler.this.handleMessage(msg);
                }
            };
        }
    }
    
    private void handleMessage(Message msg){
    	
    	int what = msg.what;
    	
    	switch (what) {
		case SUCCESS_MESSAGE:
			Object[] responseSuccessMsg = (Object[]) msg.obj;
			handleSuccessMessage(responseSuccessMsg[1].toString());
			break;
		case FAILURE_MESSAGE:
			Object[] responseFailureMsg = (Object[]) msg.obj;
			handleFailureMessage((List<Throwable>)responseFailureMsg[0], responseFailureMsg[1].toString());
			break;
		case START_MESSAGE:
			onStart();
			break;
		case FINISH_MESSAGE:
			onFinish();
			break;
		default:
			break;
		}
    }
    
    protected void onFinish() {}

    protected void onStart() {}

	private void handleFailureMessage(List<Throwable> e, String responseFailureMsg) {
    	onFailure(e, responseFailureMsg);
	}

	protected void onFailure(List<Throwable> e, String responseFailureMsg) {
		onFailure();
	}

	protected void onFailure() {}

	private void handleSuccessMessage(String responseMsg) {
    	onSuccess(responseMsg);
	}

	protected void onSuccess(String responseMsg) {
		onSuccess();
	}

	protected void onSuccess() {}
	
	protected void sendSuccessMessage(String msg) {
		sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{null, msg}));
	}

    protected void sendFailureMessage(List<Throwable> e, String msg) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{e, msg}));
    }

    protected void sendMessage(Message msg) {
        if(handler != null){
            handler.sendMessage(msg);
        } else {
            handleMessage(msg);
        }
    }

    protected Message obtainMessage(int what, Object[] response) {
        Message msg = null;
        if(handler != null){
            msg = this.handler.obtainMessage(what, response);
        }else{
            msg = Message.obtain();
            msg.what = what;
            msg.obj = response;
        }
        return msg;
    }
	
    protected void sendStartMessage() {
        sendMessage(obtainMessage(START_MESSAGE, null));
    }

    protected void sendFinishMessage() {
        sendMessage(obtainMessage(FINISH_MESSAGE, null));
    }
}
