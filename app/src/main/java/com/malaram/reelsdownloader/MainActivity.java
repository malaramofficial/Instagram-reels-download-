package com.malaram.reelsdownloader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
 @Override public void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  LinearLayout layout=new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(48,80,48,48);
  TextView title=new TextView(this); title.setTextSize(24); title.setText("Reels Downloader");
  TextView info=new TextView(this); info.setTextSize(16); info.setText("\nDownload and manage videos you own or are authorized to save.\n\nApp foundation is ready.");
  layout.addView(title); layout.addView(info); setContentView(layout);
 }
}