package com.malaram.reelsdownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class LauncherActivity extends Activity {
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(15,23,42));
        getWindow().setNavigationBarColor(Color.rgb(15,23,42));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(15,23,42));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(30));
        scroll.addView(root);

        LinearLayout top = row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark = label("RM", 16, Color.WHITE);
        mark.setGravity(Gravity.CENTER);
        mark.setTypeface(null, 1);
        mark.setBackground(bg(Color.rgb(236,72,153), 28));
        top.addView(mark, new LinearLayout.LayoutParams(dp(54), dp(54)));

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        names.setPadding(dp(14),0,0,0);
        TextView app = label("ReelMate", 25, Color.WHITE);
        app.setTypeface(null, 1);
        TextView sub = label("Your media companion", 12, Color.rgb(148,163,184));
        names.addView(app); names.addView(sub);
        top.addView(names, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button menu = button("⋮", Color.TRANSPARENT, Color.WHITE);
        menu.setTextSize(28);
        menu.setOnClickListener(v -> menu(v));
        top.addView(menu, new LinearLayout.LayoutParams(dp(48), dp(54)));
        root.addView(top);

        root.addView(space(24));

        LinearLayout hero = card(Color.rgb(30,41,59), 24);
        TextView big = label("Welcome to ReelMate", 25, Color.WHITE);
        big.setTypeface(null, 1);
        hero.addView(big);
        TextView intro = label("A clean and simple space for your video workflow.", 14, Color.rgb(203,213,225));
        intro.setPadding(0,dp(8),0,dp(18));
        hero.addView(intro);
        Button open = button("Open Reel Tool", Color.rgb(124,58,237), Color.WHITE);
        open.setTypeface(null, 1);
        open.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        hero.addView(open, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        root.addView(hero);

        root.addView(space(18));

        LinearLayout creator = card(Color.WHITE, 22);
        TextView section = label("About the Creator", 18, Color.rgb(15,23,42));
        section.setTypeface(null, 1);
        creator.addView(section);

        LinearLayout profile = row();
        profile.setGravity(Gravity.CENTER_VERTICAL);
        profile.setPadding(0,dp(16),0,dp(10));
        TextView avatar = label("M", 27, Color.WHITE);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTypeface(null, 1);
        avatar.setBackground(bg(Color.rgb(14,165,233), 36));
        profile.addView(avatar, new LinearLayout.LayoutParams(dp(72), dp(72)));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(14),0,0,0);
        TextView name = label("Mala Ram", 20, Color.rgb(15,23,42));
        name.setTypeface(null, 1);
        TextView role = label("Creator • Music • Technology", 13, Color.rgb(100,116,139));
        info.addView(name); info.addView(role);
        profile.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        creator.addView(profile);

        TextView note = label("Tap any link below to connect directly.", 13, Color.rgb(71,85,105));
        creator.addView(note);

        creator.addView(social("Instagram  @malaramofficial", Color.rgb(225,48,108),
                "https://www.instagram.com/malaramofficial/"));
        creator.addView(social("Email  malaramofficial@gmail.com", Color.rgb(51,65,85),
                "mailto:malaramofficial@gmail.com"));
        creator.addView(social("WhatsApp  +91 8302776659", Color.rgb(22,163,74),
                "https://wa.me/918302776659"));
        root.addView(creator);

        root.addView(space(18));

        LinearLayout features = card(Color.rgb(20,31,50), 20);
        TextView ft = label("Inside the app", 18, Color.WHITE);
        ft.setTypeface(null, 1);
        features.addView(ft);
        features.addView(feature("↗", "Share Support", "Send a supported link directly to the app."));
        features.addView(feature("⚙", "Settings", "Manage app settings from one place."));
        features.addView(feature("●", "Creator Profile", "Quick access to official contact links."));
        root.addView(features);

        root.addView(space(22));
        TextView footer = label("ReelMate • Version 1.0", 11, Color.rgb(100,116,139));
        footer.setGravity(Gravity.CENTER);
        root.addView(footer);

        setContentView(scroll);
    }

    private View social(String title, int color, String url) {
        Button b = button(title, color, Color.WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        p.topMargin = dp(8);
        b.setLayoutParams(p);
        b.setOnClickListener(v -> open(url));
        return b;
    }

    private View feature(String icon, String title, String desc) {
        LinearLayout r = row();
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(0,dp(14),0,0);
        TextView i = label(icon, 18, Color.WHITE);
        i.setGravity(Gravity.CENTER);
        i.setBackground(bg(Color.rgb(71,85,105), 18));
        r.addView(i, new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12),0,0,0);
        TextView t = label(title,14,Color.WHITE); t.setTypeface(null,1);
        TextView d = label(desc,12,Color.rgb(148,163,184));
        c.addView(t); c.addView(d);
        r.addView(c, new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        return r;
    }

    private void menu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Settings");
        popup.getMenu().add("About");
        popup.setOnMenuItemClickListener(item -> {
            if ("Settings".contentEquals(item.getTitle())) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName())));
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("ReelMate")
                        .setMessage("Designed for a simple, professional experience.\n\nCreated by Mala Ram.")
                        .setPositiveButton("Close", null).show();
            }
            return true;
        });
        popup.show();
    }

    private void open(String url) {
        try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
        catch (Exception e) { Toast.makeText(this, "No compatible app found", Toast.LENGTH_SHORT).show(); }
    }

    private LinearLayout row() { LinearLayout x = new LinearLayout(this); x.setOrientation(LinearLayout.HORIZONTAL); return x; }

    private LinearLayout card(int color, int radius) {
        LinearLayout x = new LinearLayout(this);
        x.setOrientation(LinearLayout.VERTICAL);
        x.setPadding(dp(18),dp(18),dp(18),dp(18));
        x.setBackground(bg(color,radius));
        return x;
    }

    private TextView label(String text, float size, int color) {
        TextView x = new TextView(this); x.setText(text); x.setTextSize(size); x.setTextColor(color); return x;
    }

    private Button button(String text, int color, int textColor) {
        Button b = new Button(this);
        b.setText(text); b.setTextColor(textColor); b.setTextSize(13); b.setAllCaps(false);
        b.setBackground(bg(color,16));
        return b;
    }

    private View space(int height) {
        Space s = new Space(this); s.setLayoutParams(new LinearLayout.LayoutParams(1,dp(height))); return s;
    }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g;
    }
}
