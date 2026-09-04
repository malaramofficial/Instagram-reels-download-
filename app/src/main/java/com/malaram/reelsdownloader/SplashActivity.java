package com.malaram.reelsdownloader;
import android.app.*;import android.content.*;import android.graphics.*;import android.os.*;import android.view.*;import android.widget.*;
public class SplashActivity extends Activity{
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Ui.NAVY);getWindow().setNavigationBarColor(Ui.NAVY);
 LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setGravity(Gravity.CENTER);r.setPadding(Ui.dp(this,32),0,Ui.dp(this,32),0);r.setBackgroundColor(Ui.NAVY);
 TextView logo=Ui.text(this,"RM",34,Color.WHITE);logo.setGravity(Gravity.CENTER);logo.setTypeface(null,1);logo.setBackground(Ui.bg(this,Ui.PURPLE,30));r.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,104),Ui.dp(this,104)));
 Ui.space(r,this,24);TextView name=Ui.text(this,"ReelMate",32,Color.WHITE);name.setTypeface(null,1);name.setGravity(Gravity.CENTER);r.addView(name);
 TextView tag=Ui.text(this,"Your media companion",14,Ui.MUTED);tag.setGravity(Gravity.CENTER);tag.setPadding(0,Ui.dp(this,8),0,0);r.addView(tag);
 new Handler(Looper.getMainLooper()).postDelayed(()->{startActivity(new Intent(this,HomeActivity.class));finish();},1400);setContentView(r);
 }} 