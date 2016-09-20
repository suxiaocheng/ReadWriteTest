package com.example.hp.readwritetest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ReadWriteService extends Service {
    public static final String TAG = "ReadWriteService";
    private Toast toastInfo;

    private MyBinder mBinder = new MyBinder();
    private ReadWriteHandler mReadWriteHandler;

    public ReadWriteService() {
    }

    // Handler that receives messages from the thread
    private final class ReadWriteHandler extends Handler {
        public ReadWriteHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg2);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() executed");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        if(toastInfo != null){
            toastInfo.cancel();
            toastInfo = null;
        }
        toastInfo = Toast.makeText(getApplicationContext(), "rw service start", Toast.LENGTH_SHORT);
        toastInfo.show();

        Message msg = mReadWriteHandler.obtainMessage();
        msg.arg1 = 0x0;
        msg.arg2 = startId;
        msg.obj = null;

        mReadWriteHandler.sendMessage(msg);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(toastInfo != null){
            toastInfo.cancel();
            toastInfo = null;
        }
        Log.d(TAG, "onDestroy() executed");
    }

    class MyBinder extends Binder {
    }
}

