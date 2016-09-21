package com.example.hp.readwritetest;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class ReadWriteService extends Service {
    public static final String TAG = "ReadWriteService";
    public static final String TEST_DIRECTORY = "Test directory";
    public static final String TEST_FILE_NAME = "test_file.bin";
    private Toast toastInfo;

    private MyBinder mBinder = new MyBinder();
    private ReadWriteHandler mReadWriteHandler;
    private Looper mServiceLooper;

    public ReadWriteService() {
    }

    // Handler that receives messages from the thread
    private final class ReadWriteHandler extends Handler {
        public ReadWriteHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String strDirectory = (String) msg.obj;
            File fDirectory = new File(strDirectory);
            byte[] w_buffer = new byte[65536];
            byte[] r_buffer = new byte[65536];
            Double r_speed, w_speed;
            Boolean bFileOpError = false;

            Random random = new Random();

            if (fDirectory.exists()) {
                if (fDirectory.canWrite()) {
                    while(bFileOpError == false) {
                        // 1. Create the file.
                        String strTestFile = strDirectory + File.separator + TEST_FILE_NAME;
                        File fTestFile = new File(strTestFile);
                        int seed = random.nextInt();
                        long lWriteLength, lReadLength;
                        Boolean bNeedQuit = false;
                        Boolean bDataCompareFail = false;

                        try {
                            // 2. Delete the test file.
                            if (fTestFile.exists()) {
                                fTestFile.delete();
                                Log.d(TAG, "Delete test file: " + strTestFile);
                            }

                            // 3. Write the file to the max size of disk
                            FileOutputStream fos = new FileOutputStream(fTestFile);
                            lWriteLength = 0x0;
                            bNeedQuit = false;
                            while (bNeedQuit == false) {
                                for (int i = 0; i < w_buffer.length / 512; i++) {
                                    for (int j = 0; j < 512; j++) {
                                        w_buffer[i * 512 + j] = (byte) (i + lWriteLength);
                                    }
                                }
                                try {
                                    fos.write(w_buffer);
                                } catch (IOException e) {
                                    //e.printStackTrace();
                                    bNeedQuit = true;
                                    Log.d(TAG, "Write the file end or error");
                                } finally {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        bFileOpError = true;
                                        Log.e(TAG, "Close the write file error");
                                    }
                                }
                                lWriteLength += w_buffer.length;
                            }
                            if(bFileOpError){
                                break;
                            }
                            // 4. Verify the file in the disk
                            FileInputStream fis = new FileInputStream(fTestFile);
                            lReadLength = 0x0;
                            bNeedQuit = false;
                            bDataCompareFail = false;
                            while ((bNeedQuit == false) && (bDataCompareFail == false)) {
                                for (int i = 0; i < w_buffer.length / 512; i++) {
                                    for (int j = 0; j < 512; j++) {
                                        w_buffer[i * 512 + j] = (byte) (i + lReadLength);
                                    }
                                }
                                try {
                                    fis.read(r_buffer);
                                    for (int i = 0; i < w_buffer.length; i++) {
                                        if (w_buffer[i] != r_buffer[i]) {
                                            Log.e(TAG, "Data compare fail at address: " + (lReadLength + i));
                                            bDataCompareFail = true;
                                            break;
                                        }
                                    }
                                } catch (IOException e) {
                                    //e.printStackTrace();
                                    bNeedQuit = true;
                                    Log.d(TAG, "Read the file end or error");
                                } finally {
                                    try {
                                        fis.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        bFileOpError = true;
                                        Log.e(TAG, "Close the read file error");
                                    }
                                }
                                lReadLength += r_buffer.length;
                            }
                            if(bFileOpError){
                                break;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            bFileOpError = true;
                            Log.e(TAG, "File operation error");
                        }
                    }
                }
            }

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

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mReadWriteHandler = new ReadWriteHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() executed");
        if (toastInfo != null) {
            toastInfo.cancel();
            toastInfo = null;
        }
        toastInfo = Toast.makeText(getApplicationContext(), "rw service start", Toast.LENGTH_SHORT);
        toastInfo.show();

        Message msg = mReadWriteHandler.obtainMessage();
        msg.arg1 = 0x0;
        msg.arg2 = startId;
        msg.obj = intent.getStringExtra(TEST_DIRECTORY);

        mReadWriteHandler.sendMessage(msg);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (toastInfo != null) {
            toastInfo.cancel();
            toastInfo = null;
        }
        Log.d(TAG, "onDestroy() executed");
    }

    class MyBinder extends Binder {
    }
}

