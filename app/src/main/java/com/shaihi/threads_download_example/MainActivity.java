package com.shaihi.threads_download_example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private String downloadUrl = "https://testing.taxi/wp-content/uploads/2023/06/compressed-pdf-2G.zip";
    private String filePath;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        filePath = getExternalFilesDir(null) + "/downloadedfile.zip";
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        downloadFile();
    }

    private void downloadFile() {
        new Thread(() -> {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int totalSize = connection.getContentLength();
                int downloadedSize = 0;

                byte[] buffer = new byte[1024];
                int bufferLength;

                InputStream inputStream = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    int finalDownloadedSize = downloadedSize;
                    handler.post(() -> progressBar.setProgress((int) ((finalDownloadedSize * 100L) / totalSize)));
                    Thread.sleep(30);
                }
                fileOutputStream.close();
                inputStream.close();
                handler.post(() -> Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_SHORT).show());
                File file = new File(filePath);
                if (file.exists()) {
                    int file_size = Integer.parseInt(String.valueOf(file.length()/(1024*1024)));
                    Log.i("ProgressBar App", "Download Complete. File size is "+String.valueOf(file_size) + "MB");
                } else {
                    Log.i("ProgressBar App", "Download Complete. but no such file ");
                }
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("ProgressBar App", "Error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
            Log.i("ProgressBar App", "File deleted");
        }
    }
}