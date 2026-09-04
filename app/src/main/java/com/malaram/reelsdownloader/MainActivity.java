package com.malaram.reelsdownloader;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private static final int NAVY=Color.rgb(10,18,35), CARD=Color.rgb(27,38,60), MUTED=Color.rgb(166,178,199), WHITE=Color.rgb(242,244,248), PURPLE=Color.rgb(139,92,246);
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private LinearLayout content, nav; private TextView title;
    private EditText linkInput; private TextView status; private Button continueBtn;

    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(NAVY);showHome();}

    private void shell(String page){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(NAVY);
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(20),dp(18),dp(20),dp(14));
        if(page.equals("Home")){
            TextView mark=text("▶",22,WHITE);mark.setGravity(Gravity.CENTER);mark.setTypeface(Typeface.DEFAULT_BOLD);mark.setBackground(gradient(20));
            top.addView(mark,new LinearLayout.LayoutParams(dp(48),dp(48)));
            TextView brand=text("ReelMate",27,WHITE);brand.setTypeface(Typeface.DEFAULT_BOLD);brand.setPadding(dp(14),0,0,0);top.addView(brand,new LinearLayout.LayoutParams(0,dp(48),1));
            Button set=button("⚙",CARD,WHITE);set.setTextSize(21);set.setOnClickListener(v->showProfile());top.addView(set,new LinearLayout.LayoutParams(dp(58),dp(48)));
        }else{ title=text(page,26,WHITE);title.setTypeface(Typeface.DEFAULT_BOLD);top.addView(title,new LinearLayout.LayoutParams(0,dp(52),1)); if(page.equals("Downloads")){Button plus=button("+",PURPLE,WHITE);plus.setTextSize(28);plus.setOnClickListener(v->showDownloadTool());top.addView(plus,new LinearLayout.LayoutParams(dp(58),dp(52)));}}
        root.addView(top);
        View line=new View(this);line.setBackgroundColor(Color.rgb(43,53,76));root.addView(line,new LinearLayout.LayoutParams(-1,1));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(20),dp(22),dp(20),dp(110));
        scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        nav=row();nav.setGravity(Gravity.CENTER);nav.setPadding(dp(10),dp(10),dp(10),dp(14));nav.setBackgroundColor(Color.rgb(12,21,39));root.addView(nav,new LinearLayout.LayoutParams(-1,dp(82)));
        setContentView(root);
    }

    private void showHome(){shell("Home");
        content.addView(text("WELCOME BACK",14,MUTED));
        TextView h=text("What are we saving today?",30,WHITE);h.setTypeface(Typeface.DEFAULT_BOLD);h.setPadding(0,dp(12),0,dp(28));content.addView(h);
        LinearLayout action=card(22);action.setBackground(gradient(26)); action.setOnClickListener(v->showDownloadTool());
        TextView icon=text("⎘",28,WHITE);icon.setGravity(Gravity.CENTER);icon.setBackground(round(Color.argb(40,255,255,255),22));
        LinearLayout ar=row();ar.setGravity(Gravity.CENTER_VERTICAL);ar.addView(icon,new LinearLayout.LayoutParams(dp(64),dp(64)));
        LinearLayout words=new LinearLayout(this);words.setOrientation(LinearLayout.VERTICAL);words.setPadding(dp(18),0,0,0);
        TextView a=text("Paste a public link",22,WHITE);a.setTypeface(Typeface.DEFAULT_BOLD);TextView b=text("Instagram · Facebook · X · public pages",15,Color.rgb(229,220,255));words.addView(a);words.addView(b);ar.addView(words,new LinearLayout.LayoutParams(0,-2,1));TextView arrow=text("›",42,WHITE);ar.addView(arrow);action.addView(ar);content.addView(action);
        LinearLayout stats=row();stats.setPadding(0,dp(22),0,0);stats.addView(stat("✓","3","Completed"));stats.addView(stat("◷","0","In queue"));stats.addView(stat("▱","68 MB","Used"));content.addView(stats);
        content.addView(space(24));LinearLayout recent=card(22);TextView rh=text("Recent activity",21,WHITE);rh.setTypeface(Typeface.DEFAULT_BOLD);recent.addView(rh);
        recent.addView(activity("city_night_timelapse.mp4","Instagram · 24.8 MB · Today, 14:20"));recent.addView(activity("morning_desk_setup.mp4","Facebook · 12.1 MB · Yesterday, 09:05"));recent.addView(activity("dusk_mountains_reel.mp4","Instagram · 31.6 MB · 2 Sep, 18:44"));content.addView(recent);
        buildNav(0);
    }

    private LinearLayout stat(String icon,String value,String label){LinearLayout c=card(18);c.setPadding(dp(16),dp(14),dp(10),dp(14));TextView i=text(icon,21,Color.rgb(166,110,255));TextView v=text(value,24,WHITE);v.setTypeface(Typeface.DEFAULT_BOLD);TextView l=text(label,14,MUTED);c.addView(i);c.addView(v);c.addView(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(120),1);p.setMargins(dp(3),0,dp(3),0);c.setLayoutParams(p);return c;}

    private View activity(String name,String sub){LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);r.setPadding(0,dp(18),0,0);TextView dot=text("▶",18,Color.rgb(168,104,255));dot.setGravity(Gravity.CENTER);dot.setBackground(round(Color.rgb(18,25,44),18));r.addView(dot,new LinearLayout.LayoutParams(dp(54),dp(54)));LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(14),0,0,0);TextView n=text(name,17,WHITE);n.setTypeface(Typeface.DEFAULT_BOLD);TextView s=text(sub,13,MUTED);x.addView(n);x.addView(s);r.addView(x,new LinearLayout.LayoutParams(0,-2,1));TextView ok=text("✓",24,Color.rgb(52,211,153));r.addView(ok);return r;}

    private void showDownloads(){shell("Downloads");
        EditText search=new EditText(this);search.setHint("⌕  Search your files");search.setHintTextColor(MUTED);search.setTextColor(WHITE);search.setTextSize(18);search.setSingleLine(true);search.setPadding(dp(20),0,dp(20),0);search.setBackground(round(Color.rgb(35,47,71),24));content.addView(search,new LinearLayout.LayoutParams(-1,dp(66)));
        TextView count=text("3 FILES · 68.5 MB",14,MUTED);count.setTypeface(Typeface.DEFAULT_BOLD);count.setPadding(0,dp(26),0,dp(12));content.addView(count);
        content.addView(fileCard("city_night_timelapse.mp4","Instagram · 1080p · 24.8 MB","0:38"));content.addView(space(14));content.addView(fileCard("morning_desk_setup.mp4","Facebook · 720p · 12.1 MB","0:21"));content.addView(space(14));content.addView(fileCard("dusk_mountains_reel.mp4","Instagram · 1080p · 31.6 MB","0:52"));buildNav(1);
    }

    private LinearLayout fileCard(String n,String s,String dur){LinearLayout c=card(24);LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);TextView thumb=text("▶",26,WHITE);thumb.setGravity(Gravity.CENTER);thumb.setBackground(round(Color.rgb(15,18,31),20));r.addView(thumb,new LinearLayout.LayoutParams(dp(104),dp(104)));LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(18),0,0,0);TextView a=text(n,18,WHITE);a.setTypeface(Typeface.DEFAULT_BOLD);TextView b=text(s,14,MUTED);TextView d=text(dur,13,Color.rgb(200,190,230));x.addView(a);x.addView(b);x.addView(d);r.addView(x,new LinearLayout.LayoutParams(0,-2,1));c.addView(r);LinearLayout buttons=row();buttons.setPadding(dp(122),dp(10),0,0);Button sh=button("↗  Share",Color.rgb(35,47,71),WHITE);Button del=button("⌫  Delete",Color.rgb(43,37,52),Color.rgb(248,113,113));buttons.addView(sh,new LinearLayout.LayoutParams(0,dp(48),1));LinearLayout.LayoutParams q=new LinearLayout.LayoutParams(0,dp(48),1);q.setMargins(dp(10),0,0,0);buttons.addView(del,q);c.addView(buttons);return c;}

    private void showHelp(){shell("Help & guide");LinearLayout how=card(24);TextView t=text("How it works",23,WHITE);t.setTypeface(Typeface.DEFAULT_BOLD);how.addView(t);how.addView(step("⧉","1. Copy a public link","Open the post, tap share and choose Copy link."));how.addView(step("⇲","2. Paste it in ReelMate","Tap Paste a public link on the home screen."));how.addView(step("⇩","3. Pick quality and save","Choose available media, then save to your library."));content.addView(how);content.addView(space(20));LinearLayout note=card(22);TextView nt=text("♢   ReelMate only works with content that is already public. Please respect creators' rights and local copyright rules when saving media.",17,MUTED);nt.setLineSpacing(dp(6),1f);note.addView(nt);content.addView(note);content.addView(space(20));content.addView(faq("Which links are supported?"));content.addView(space(10));content.addView(faq("Where are my files saved?"));buildNav(2);}

    private View step(String i,String a,String b){LinearLayout r=row();r.setPadding(0,dp(22),0,0);TextView icon=text(i,24,Color.rgb(166,110,255));icon.setGravity(Gravity.CENTER);icon.setBackground(round(Color.rgb(31,43,67),18));r.addView(icon,new LinearLayout.LayoutParams(dp(54),dp(54)));LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(16),0,0,0);TextView aa=text(a,18,WHITE);aa.setTypeface(Typeface.DEFAULT_BOLD);TextView bb=text(b,15,MUTED);bb.setPadding(0,dp(4),0,0);x.addView(aa);x.addView(bb);r.addView(x,new LinearLayout.LayoutParams(0,-2,1));return r;}
    private LinearLayout faq(String s){LinearLayout f=card(20);LinearLayout r=row();TextView a=text(s,18,WHITE);a.setTypeface(Typeface.DEFAULT_BOLD);r.addView(a,new LinearLayout.LayoutParams(0,-2,1));TextView v=text("⌄",24,MUTED);r.addView(v);f.addView(r);f.setOnClickListener(vv->Toast.makeText(this,"ReelMate supports permitted public links and saves files to Downloads/ReelMate.",Toast.LENGTH_LONG).show());return f;}

    private void showProfile(){shell("Profile");LinearLayout profile=card(26);profile.setGravity(Gravity.CENTER_HORIZONTAL);TextView avatar=text("MR",27,WHITE);avatar.setTypeface(Typeface.DEFAULT_BOLD);avatar.setGravity(Gravity.CENTER);avatar.setBackground(gradient(42));profile.addView(avatar,new LinearLayout.LayoutParams(dp(108),dp(108)));TextView name=text("Malaram",27,WHITE);name.setTypeface(Typeface.DEFAULT_BOLD);name.setPadding(0,dp(22),0,0);profile.addView(name);TextView role=text("Creator & designer of ReelMate",16,MUTED);role.setPadding(0,dp(6),0,dp(16));profile.addView(role);TextView chip=text("Indie developer · India",15,WHITE);chip.setGravity(Gravity.CENTER);chip.setBackground(round(Color.rgb(51,42,74),20));profile.addView(chip,new LinearLayout.LayoutParams(dp(230),dp(48)));content.addView(profile);TextView gt=text("GET IN TOUCH",14,MUTED);gt.setTypeface(Typeface.DEFAULT_BOLD);gt.setPadding(dp(8),dp(32),0,dp(12));content.addView(gt);content.addView(contact("◎","Instagram","@malaramofficial",v->openUrl("https://www.instagram.com/malaramofficial/")));content.addView(space(12));content.addView(contact("✉","Email","malaramofficial@gmail.com",v->openUrl("mailto:malaramofficial@gmail.com")));content.addView(space(12));content.addView(contact("◯","WhatsApp","+91 8302776659",v->openUrl("https://wa.me/918302776659")));buildNav(3);}

    private LinearLayout contact(String i,String a,String b,View.OnClickListener l){LinearLayout c=card(22);LinearLayout r=row();r.setGravity(Gravity.CENTER_VERTICAL);TextView ic=text(i,27,Color.rgb(166,110,255));ic.setGravity(Gravity.CENTER);ic.setBackground(round(Color.rgb(29,39,59),20));r.addView(ic,new LinearLayout.LayoutParams(dp(70),dp(70)));LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(18),0,0,0);x.addView(text(a,15,MUTED));TextView bb=text(b,20,WHITE);bb.setTypeface(Typeface.DEFAULT_BOLD);x.addView(bb);r.addView(x,new LinearLayout.LayoutParams(0,-2,1));r.addView(text("›",32,MUTED));c.addView(r);c.setOnClickListener(l);return c;}

    private void showDownloadTool(){shell("Download");TextView h=text("Paste a public link",28,WHITE);h.setTypeface(Typeface.DEFAULT_BOLD);content.addView(h);TextView sub=text("Preview available media and save it in a few simple steps.",17,MUTED);sub.setPadding(0,dp(8),0,dp(24));content.addView(sub);
        linkInput=new EditText(this);linkInput.setHint("https://...");linkInput.setHintTextColor(MUTED);linkInput.setTextColor(WHITE);linkInput.setTextSize(17);linkInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);linkInput.setSingleLine(false);linkInput.setMinLines(3);linkInput.setPadding(dp(18),dp(14),dp(18),dp(14));linkInput.setBackground(round(CARD,22));content.addView(linkInput,new LinearLayout.LayoutParams(-1,-2));
        Button paste=button("Paste from clipboard",Color.rgb(35,47,71),WHITE);paste.setOnClickListener(v->paste());LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(54));pp.setMargins(0,dp(14),0,0);content.addView(paste,pp);
        continueBtn=button("Continue",PURPLE,WHITE);continueBtn.setTypeface(Typeface.DEFAULT_BOLD);continueBtn.setOnClickListener(v->startProcessing());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(58));cp.setMargins(0,dp(12),0,0);content.addView(continueBtn,cp);
        status=text("Only save public media you have permission to download.",14,MUTED);status.setGravity(Gravity.CENTER);status.setPadding(0,dp(18),0,0);content.addView(status);buildNav(-1);
    }

    private void buildNav(int active){String[] icons={"⌂","⇩","?","♙"};String[] labels={"Home","Downloads","Help","Profile"};for(int i=0;i<4;i++){final int n=i;LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setGravity(Gravity.CENTER);TextView ic=text(icons[i],25,i==active?WHITE:MUTED);ic.setGravity(Gravity.CENTER);if(i==active)ic.setBackground(gradient(20));item.addView(ic,new LinearLayout.LayoutParams(dp(58),dp(42)));TextView lb=text(labels[i],13,i==active?WHITE:MUTED);lb.setGravity(Gravity.CENTER);lb.setTypeface(i==active?Typeface.DEFAULT_BOLD:Typeface.DEFAULT);item.addView(lb);item.setOnClickListener(v->{if(n==0)showHome();else if(n==1)showDownloads();else if(n==2)showHelp();else showProfile();});nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));}}

    private void paste(){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null&&c.hasPrimaryClip()){CharSequence s=c.getPrimaryClip().getItemAt(0).coerceToText(this);if(s!=null){linkInput.setText(s.toString().trim());status.setText("Link pasted. Tap Continue.");return;}}status.setText("Clipboard does not contain a text link.");}
    private void startProcessing(){String link=linkInput.getText().toString().trim();if(link.isEmpty()){linkInput.setError("Paste a public link first");return;}setLoading(true,"Processing public link...");executor.execute(()->{String direct=DownloadResolver.resolveAuthorizedMedia(link);runOnUiThread(()->{if(direct==null)setLoading(false,"This link could not be resolved right now.");else enqueueDownload(direct);});});}
    private void setLoading(boolean b,String m){status.setText(m);continueBtn.setEnabled(!b);continueBtn.setText(b?"Processing...":"Continue");}
    private void enqueueDownload(String u){try{String fn="ReelMate_"+System.currentTimeMillis()+".mp4";DownloadManager.Request r=new DownloadManager.Request(Uri.parse(u));r.setTitle("ReelMate media");r.setDescription("Downloading");r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"ReelMate/"+fn);((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r);setLoading(false,"Download started. Check Downloads/ReelMate.");}catch(Exception e){setLoading(false,"Download could not be started.");}}
    private void openUrl(String u){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){Toast.makeText(this,"No compatible app found",Toast.LENGTH_SHORT).show();}}
    private LinearLayout row(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.HORIZONTAL);return x;}
    private LinearLayout card(int r){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setPadding(dp(18),dp(18),dp(18),dp(18));x.setBackground(round(CARD,r));return x;}
    private Button button(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setTextColor(fg);b.setTextSize(16);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT_BOLD);b.setPadding(dp(8),0,dp(8),0);b.setBackground(round(bg,18));return b;}
    private TextView text(String s,float z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setFontFeatureSettings("kern");return v;}
    private View space(int h){Space s=new Space(this);s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(h)));return s;}
    private GradientDrawable round(int c,int r){GradientDrawable g=new GradientDrawable();g.setColor(c);g.setCornerRadius(dp(r));return g;}
    private GradientDrawable gradient(int r){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(116,69,230),Color.rgb(196,111,226)});g.setCornerRadius(dp(r));return g;}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override protected void onDestroy(){super.onDestroy();executor.shutdownNow();}
}