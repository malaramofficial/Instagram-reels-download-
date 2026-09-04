package com.malaram.reelsdownloader;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public final class Ui {
    private Ui() {}
    public static int dp(Activity a,int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
    public static GradientDrawable bg(Activity a,int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(a,r));return g;}
    public static TextView text(Activity a,String s,float z,int c){TextView v=new TextView(a);v.setText(s);v.setTextSize(z);v.setTextColor(c);return v;}
    public static Button button(Activity a,String s,int bg,int fg){Button b=new Button(a);b.setText(s);b.setTextColor(fg);b.setTextSize(13);b.setAllCaps(false);b.setBackground(bg(a,bg,16));return b;}
    public static LinearLayout card(Activity a,int color){LinearLayout l=new LinearLayout(a);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(a,18),dp(a,18),dp(a,18),dp(a,18));l.setBackground(bg(a,color,22));return l;}
    public static void space(LinearLayout l,Activity a,int h){Space s=new Space(a);l.addView(s,new LinearLayout.LayoutParams(1,dp(a,h)));}
    public static final int NAVY=Color.rgb(15,23,42), CARD=Color.rgb(30,41,59), PURPLE=Color.rgb(124,58,237), MUTED=Color.rgb(148,163,184);
}