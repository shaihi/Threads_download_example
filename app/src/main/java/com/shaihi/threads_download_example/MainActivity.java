package com.shaihi.threads_download_example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.Manifest;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private final String smallFilePath = "https://ash-speed.hetzner.com/100MB.bin";
    private final String bigFilePath = "https://ash-speed.hetzner.com/1GB.bin";

    private final String hugeFilePath = "https://ash-speed.hetzner.com/10GB.bin";


    private String downloadUrl;
    private String filePath;
    //Here we are connecting the Handler with the Looper of the Main Thread
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        filePath = getExternalFilesDir(null) + "/downloadedfile.zip";

        Button smallerBtn = findViewById(R.id.oneG);
        Button biggerBtn = findViewById(R.id.fiftyG);

        smallerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadUrl = smallFilePath;

                smallerBtn.setEnabled(false);
                biggerBtn.setEnabled(false);
                //We do not need to ask for permission in API 30 and above because we are writing to our App's Storage
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                downloadFile();
            }
        });
        biggerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadUrl = bigFilePath;

                smallerBtn.setEnabled(false);
                biggerBtn.setEnabled(false);
                //We do not need to ask for permission in API 30 and above because we are writing to our App's Storage
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                downloadFile();
            }
        });
    }

    private void downloadFile() {
        // A new thread is created to handle the heavy and potentially long task of downloading a file
        new Thread(() -> {
            try {
                //**************************************************
                // This block of code handles downloading from a URL
                //**************************************************
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int totalSize = connection.getContentLength();
                int downloadedSize = 0;

                byte[] buffer = new byte[1024];
                int bufferLength;

                InputStream inputStream = connection.getInputStream();
                FileOutputStream fileOutputStream = new FileOutputStream(filePath);

                Log.i("ProgressBar App", "starting file download of " + downloadUrl);
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    int finalDownloadedSize = downloadedSize;
                    //**************************************************
                    // Until the line above it is code for handling url download
                    //**************************************************

                    //The below is inserting a message (Runnable) to the main thread queue to update the progress bar
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress((int) ((finalDownloadedSize * 100L) / totalSize));
                        }
                    });

                    //I placed this sleep in case the download is too quick to see the progress
                    //Thread.sleep(30);
                }
                fileOutputStream.close();
                inputStream.close();
                //Each activity related to UI such as Toast needs to be done on the main thread
                //and so we use the handler to send the message to the main Thread
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Download complete", Toast.LENGTH_SHORT).show();
                    }
                });
                File file = new File(filePath);
                if (file.exists()) {
                    int file_size = Integer.parseInt(String.valueOf(file.length()/(1024*1024)));
                    Log.i("ProgressBar App", "Download Complete. File size is "+String.valueOf(file_size) +
                            "MB/ Buffer Length " + String.valueOf(downloadedSize/(1024*1024)) + "MB");
                } else {
                    Log.i("ProgressBar App", "Download Complete. but no such file ");
                }
            } catch (Exception e) {
                //Each activity related to UI such as Toast needs to be done on the main thread
                //and so we use the handler to send the message to the main Thread
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("ProgressBar App", "Error: " + e.getMessage());
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File file = new File(filePath);
        if (file.exists()) {
            //This is not a good method to clean the file since onDestroy is not always called...
            file.delete();
            Log.i("ProgressBar App", "File deleted");
        }
    }
}