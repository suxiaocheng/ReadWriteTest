package com.example.hp.readwritetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @InjectView(R.id.spinnerDiskLabel)
    Spinner spinnerDiskLabel;

    @InjectView(R.id.btCancel)
    Button btCancel;

    @InjectView(R.id.btStart)
    Button btStart;

    @InjectView(R.id.tvOutput)
    TextView tvOutput;

    @InjectView(R.id.tvCurrentStatus)
    TextView tvCurrentStatus;

    @OnClick(R.id.btStart)
    void startTest() {
        Intent startIntent = new Intent(this, ReadWriteService.class);
        startService(startIntent);
    }

    private List<String> listDiskLabel;
    private ArrayAdapter<String> arrayAdapterDiskLabel;
    private BroadcastReceiver mExternalStorageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //using butter knife
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startWatchingExternalStorage();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopWatchingExternalStorage();
    }

    void refreashDiskLabel() {

        tvOutput.append("Refresh disk list\n");

        listDiskLabel = new ArrayList<String>();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdCardDir = Environment.getExternalStorageDirectory();//获取SDCard目录
            listDiskLabel.add(sdCardDir.getPath());

            tvOutput.append("SD: " + getSDAvailableSize() + "/" + getSDTotalSize() + "\n");
        }
        listDiskLabel.add(getFilesDir().toString());

        tvOutput.append("Internal: " + getRomAvailableSize() + "/" + getRomTotalSize() + "\n");

        //适配器
        arrayAdapterDiskLabel = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listDiskLabel);
        //设置样式
        arrayAdapterDiskLabel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinnerDiskLabel.setAdapter(arrayAdapterDiskLabel);
    }

    void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("test", "Storage: " + intent.getData());
                refreashDiskLabel();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mExternalStorageReceiver, filter);
        refreashDiskLabel();
    }

    void stopWatchingExternalStorage() {
        unregisterReceiver(mExternalStorageReceiver);
    }

    /**
     * 获得SD卡总大小
     *
     * @return
     */
    private String getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    private String getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(MainActivity.this, blockSize * availableBlocks);
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    private String getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(MainActivity.this, blockSize * totalBlocks);
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    private String getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(MainActivity.this, blockSize * availableBlocks);
    }
}

