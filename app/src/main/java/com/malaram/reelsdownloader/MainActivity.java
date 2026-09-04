package com.malaram.reelsdownloader;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {
    int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density); }

    GradientDrawable bg(int color, float radius){
        GradientDrawable g=new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp((int)radius));
        return g;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20),dp(40),dp(20),dp(20));
        root.setBackgroundColor(Color.rgb(250,250,252));

        TextView logo=new TextView(this);
        logo.setText("REELS DOWNLOADER");
        logo.setTextSize(13); logo.setTextColor(Color.rgb(120,120,130));
        root.addView(logo);

        TextView title=new TextView(this);
        title.setText("Download Reels\nSimply & Fast");
        title.setTextSize(30); title.setTextColor(Color.rgb(30,30,38));
        title.setPadding(0,dp(10),0,dp(8));
        root.addView(title);

        TextView sub=new TextView(this);
        sub.setText("Paste the link to a video you own or are authorized to save.");
        sub.setTextSize(16); sub.setTextColor(Color.DKGRAY);
        sub.setPadding(0,0,0,dp(24));
        root.addView(sub);

        EditText link=new EditText(this);
        link.setHint("Paste reel link here...");
        link.setSingleLine(true);
        link.setTextSize(16);
        link.setPadding(dp(16),0,dp(16),0);
        link.setBackground(bg(Color.WHITE,16));
        root.addView(link,new LinearLayout.LayoutParams(-1,dp(58)));

        Button paste=new Button(this);
        paste.setText("PASTE LINK");
        LinearLayout.LayoutParams p1=new LinearLayout.LayoutParams(-1,dp(54));
        p1.topMargin=dp(14);
        root.addView(paste,p1);
        paste.setOnClickListener(v -> {
            android.content.ClipboardManager cm=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            if(cm.hasPrimaryClip() && cm.getPrimaryClip()!=null)
                link.setText(cm.getPrimaryClip().getItemAt(0).getText());
        });

        Button action=new Button(this);
        action.setText("CHECK LINK");
        LinearLayout.LayoutParams p2=new LinearLayout.LayoutParams(-1,dp(56));
        p2.topMargin=dp(10);
        root.addView(action,p2);

        TextView status=new TextView(this);
        status.setText("Ready");
        status.setGravity(Gravity.CENTER);
        status.setTextSize(15);
        status.setTextColor(Color.rgb(90,90,100));
        status.setPadding(0,dp(22),0,0);
        root.addView(status);

        action.setOnClickListener(v -> {
            String s=link.getText().toString().trim();
            if(s.isEmpty()) status.setText("Please paste a valid link first.");
            else status.setText("Link received. Connect an authorized download service in the next build.");
        });

        Space space=new Space(this);
        root.addView(space,new LinearLayout.LayoutParams(1,0,1));

        TextView footer=new TextView(this);
        footer.setText("Fast • Simple • Private");
        footer.setGravity(Gravity.CENTER);
        footer.setTextColor(Color.GRAY);
        root.addView(footer);

        setContentView(root);
    }
}