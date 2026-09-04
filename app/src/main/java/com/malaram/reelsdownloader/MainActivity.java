package com.malaram.reelsdownloader;

import android.app.*;
import android.os.*;
import android.content.*;
import android.net.Uri;
import android.view.Gravity;
import android.widget.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private TextView status;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        status = new TextView(this);
        status.setGravity(Gravity.CENTER);
        status.setTextSize(18);
        status.setPadding(32,64,32,32);
        status.setText("Reels Downloader\nReady to receive a shared link");
        setContentView(status);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType()!=null
                && intent.getType().startsWith("text/")) {
            String link = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (link != null && !link.trim().isEmpty()) autoProcess(link.trim());
        }
    }

    private void autoProcess(String link) {
        status.setText("Link received\nProcessing...");
        executor.execute(() -> {
            // Architecture hook: send link only to your authorized resolver service.
            // Resolver must return a direct, permitted media URL.
            String directUrl = DownloadResolver.resolveAuthorizedMedia(link);
            runOnUiThread(() -> {
                if (directUrl == null) {
                    status.setText("Unable to resolve this link with the configured authorized service.");
                } else {
                    enqueueDownload(directUrl);
                }
            });
        });
    }

    private void enqueueDownload(String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("Video download");
            request.setDescription("Downloading authorized media");
            request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                android.os.Environment.DIRECTORY_DOWNLOADS, "Reels/Video.mp4");
            ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
            status.setText("Download started automatically");
        } catch (Exception e) {
            status.setText("Download could not be started.");
        }
    }

    @Override public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }
}