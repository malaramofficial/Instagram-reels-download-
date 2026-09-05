package com.malaram.reelsdownloader;
import android.app.*;import android.content.*;import android.graphics.*;import android.graphics.drawable.GradientDrawable;import android.os.*;import android.view.*;import android.widget.*;
public class SplashActivity extends Activity{
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(7,12,25));getWindow().setNavigationBarColor(Color.rgb(7,12,25));
  LinearLayout r=new LinearLayout(this);r.setOrientation(LinearLayout.VERTICAL);r.setGravity(Gravity.CENTER);r.setPadding(Ui.dp(this,32),0,Ui.dp(this,32),0);r.setBackgroundColor(Color.rgb(7,12,25));
  TextView logo=Ui.text(this,"▷",38,Color.WHITE);logo.setGravity(Gravity.CENTER);logo.setTypeface(Typeface.DEFAULT,Typeface.BOLD);GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(145,78,255),Color.rgb(201,91,225)});g.setCornerRadius(Ui.dp(this,32));logo.setBackground(g);r.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,112),Ui.dp(this,112)));
  Ui.space(r,this,24);TextView name=Ui.text(this,"ReelMate",32,Color.WHITE);name.setTypeface(Typeface.create("sans",Typeface.BOLD));name.setGravity(Gravity.CENTER);r.addView(name);
  TextView tag=Ui.text(this,"Your media downloader companion",15,Color.rgb(158,164,185));tag.setGravity(Gravity.CENTER);tag.setPadding(0,Ui.dp(this,8),0,0);r.addView(tag);
  TextView load=Ui.text(this,"━━━━━━",15,Color.rgb(178,91,239));load.setGravity(Gravity.CENTER);load.setPadding(0,Ui.dp(this,45),0,0);r.addView(load);
  TextView skip=Ui.text(this,"SKIP",14,Color.rgb(158,164,185));skip.setGravity(Gravity.CENTER);skip.setTypeface(Typeface.create("sans",Typeface.BOLD));r.addView(skip);
  new Handler(Looper.getMainLooper()).postDelayed(()->{startActivity(new Intent(this,MainActivity.class));finish();},1500);setContentView(r);
 }}