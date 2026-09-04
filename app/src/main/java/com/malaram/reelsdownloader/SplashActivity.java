package com.malaram.reelsdownloader;
import android.app.*;import android.content.*;import android.graphics.Color;import android.os.*;import android.view.*;import android.widget.*;
public class SplashActivity extends Activity{
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Ui.NAVY);LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setGravity(Gravity.CENTER);r.setPadding(Ui.dp(this,32),0,Ui.dp(this,32),0);r.setBackgroundColor(Ui.NAVY);
 TextView logo=Ui.text(this,"RM",32,Color.WHITE);logo.setGravity(Gravity.CENTER);logo.setTypeface(null,1);logo.setBackground(Ui.bg(this,Ui.PURPLE,42));r.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,96),Ui.dp(this,96)));
 Ui.space(r,this,22);TextView name=Ui.text(this,"ReelMate",30,Color.WHITE);name.setTypeface(null,1);name.setGravity(Gravity.CENTER);r.addView(name);
 TextView tag=Ui.text(this,"Fast • Simple • Professional",14,Ui.MUTED);tag.setGravity(Gravity.CENTER);tag.setPadding(0,Ui.dp(this,8),0,0);r.addView(tag);setContentView(r);
 new Handler(Looper.getMainLooper()).postDelayed(()->{startActivity(new Intent(this,HomeActivity.class));finish();},1600);
 }} 