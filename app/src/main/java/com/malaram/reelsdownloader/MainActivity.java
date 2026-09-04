package com.malaram.reelsdownloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int PAD = 18;
    private EditText linkInput;
    private TextView status;
    private Button downloadButton;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(15, 23, 42));
        getWindow().setNavigationBarColor(Color.rgb(15, 23, 42));
        buildUi();
        handleIntent(getIntent());
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(15, 23, 42));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(PAD), dp(18), dp(PAD), dp(32));
        scroll.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Top bar
        LinearLayout top = row();
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView logo = text("RM", 16, Color.WHITE);
        logo.setGravity(Gravity.CENTER);
        logo.setTypeface(null, 1);
        logo.setBackground(round(Color.rgb(236, 72, 153), 28));
        top.addView(logo, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(dp(14), 0, dp(8), 0);
        TextView title = text("ReelMate", 24, Color.WHITE);
        title.setTypeface(null, 1);
        TextView subtitle = text("Fast • Simple • Professional", 12, Color.rgb(148, 163, 184));
        titleBox.addView(title);
        titleBox.addView(subtitle);
        top.addView(titleBox, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button menu = button("⋮", Color.TRANSPARENT, Color.WHITE);
        menu.setTextSize(28);
        menu.setOnClickListener(v -> showMenu(menu));
        top.addView(menu, new LinearLayout.LayoutParams(dp(48), dp(52)));
        root.addView(top);

        root.addView(space(18));

        // Hero card
        LinearLayout hero = card(Color.rgb(30, 41, 59), 24);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        TextView heroIcon = text("↓", 46, Color.WHITE);
        heroIcon.setGravity(Gravity.CENTER);
        heroIcon.setBackground(round(Color.rgb(124, 58, 237), 42));
        hero.addView(heroIcon, new LinearLayout.LayoutParams(dp(84), dp(84)));

        TextView heroTitle = text("Instagram Video Downloader", 22, Color.WHITE);
        heroTitle.setGravity(Gravity.CENTER);
        heroTitle.setTypeface(null, 1);
        heroTitle.setPadding(0, dp(16), 0, dp(6));
        hero.addView(heroTitle, matchWrap());

        TextView heroText = text("Paste a public Instagram Reel or video link and let ReelMate process it.", 14,
                Color.rgb(203, 213, 225));
        heroText.setGravity(Gravity.CENTER);
        hero.addView(heroText, matchWrap());
        root.addView(hero);

        root.addView(space(18));

        // Download panel
        LinearLayout panel = card(Color.WHITE, 22);
        TextView panelTitle = text("Download a video", 19, Color.rgb(15, 23, 42));
        panelTitle.setTypeface(null, 1);
        panel.addView(panelTitle, matchWrap());

        TextView hint = text("Paste the Instagram link below", 13, Color.rgb(100, 116, 139));
        hint.setPadding(0, dp(4), 0, dp(10));
        panel.addView(hint, matchWrap());

        linkInput = new EditText(this);
        linkInput.setHint("https://www.instagram.com/reel/...");
        linkInput.setTextSize(14);
        linkInput.setSingleLine(false);
        linkInput.setMinLines(2);
        linkInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        linkInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        linkInput.setBackground(round(Color.rgb(241, 245, 249), 16));
        panel.addView(linkInput, matchWrap());

        LinearLayout actions = row();
        actions.setPadding(0, dp(12), 0, 0);
        Button paste = button("Paste", Color.rgb(241, 245, 249), Color.rgb(30, 41, 59));
        paste.setOnClickListener(v -> pasteFromClipboard());
        actions.addView(paste, new LinearLayout.LayoutParams(0, dp(52), 0.34f));

        downloadButton = button("Download Video", Color.rgb(124, 58, 237), Color.WHITE);
        downloadButton.setTypeface(null, 1);
        downloadButton.setOnClickListener(v -> startProcessing());
        LinearLayout.LayoutParams dl = new LinearLayout.LayoutParams(0, dp(52), 0.66f);
        dl.setMargins(dp(10), 0, 0, 0);
        actions.addView(downloadButton, dl);
        panel.addView(actions, matchWrap());

        status = text("Ready. You can also share a link directly to ReelMate.", 13,
                Color.rgb(71, 85, 105));
        status.setPadding(0, dp(12), 0, 0);
        panel.addView(status, matchWrap());
        root.addView(panel);

        root.addView(space(18));

        // Quick guide
        LinearLayout guide = card(Color.rgb(20, 31, 50), 20);
        TextView guideTitle = text("How it works", 18, Color.WHITE);
        guideTitle.setTypeface(null, 1);
        guide.addView(guideTitle);
        guide.addView(guideRow("1", "Copy", "Copy a public Instagram Reel or video link."));
        guide.addView(guideRow("2", "Paste", "Paste the link here or share it directly to the app."));
        guide.addView(guideRow("3", "Download", "Process the link and save available media."));
        root.addView(guide);

        root.addView(space(18));

        // Creator card
        LinearLayout creator = card(Color.rgb(30, 41, 59), 22);
        LinearLayout creatorTop = row();
        TextView avatar = text("M", 26, Color.WHITE);
        avatar.setGravity(Gravity.CENTER);
        avatar.setTypeface(null, 1);
        avatar.setBackground(round(Color.rgb(14, 165, 233), 34));
        creatorTop.addView(avatar, new LinearLayout.LayoutParams(dp(68), dp(68)));

        LinearLayout creatorText = new LinearLayout(this);
        creatorText.setOrientation(LinearLayout.VERTICAL);
        creatorText.setPadding(dp(14), 0, 0, 0);
        TextView madeBy = text("Made by Mala Ram", 18, Color.WHITE);
        madeBy.setTypeface(null, 1);
        TextView role = text("Creator & Developer", 13, Color.rgb(148, 163, 184));
        creatorText.addView(madeBy);
        creatorText.addView(role);
        creatorTop.addView(creatorText, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        creator.addView(creatorTop);

        TextView creatorNote = text("Connect with me for updates, ideas and support.", 13,
                Color.rgb(203, 213, 225));
        creatorNote.setPadding(0, dp(14), 0, dp(10));
        creator.addView(creatorNote);

        LinearLayout social = row();
        Button insta = button("Instagram", Color.rgb(225, 48, 108), Color.WHITE);
        insta.setOnClickListener(v -> openUrl("https://www.instagram.com/malaramofficial/"));
        social.addView(insta, new LinearLayout.LayoutParams(0, dp(48), 1f));

        Button mail = button("Email", Color.rgb(51, 65, 85), Color.WHITE);
        mail.setOnClickListener(v -> openUrl("mailto:malaramofficial@gmail.com"));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        mp.setMargins(dp(8), 0, 0, 0);
        social.addView(mail, mp);

        Button whatsapp = button("WhatsApp", Color.rgb(22, 163, 74), Color.WHITE);
        whatsapp.setOnClickListener(v -> openUrl("https://wa.me/918302776659"));
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, dp(48), 1f);
        wp.setMargins(dp(8), 0, 0, 0);
        social.addView(whatsapp, wp);
        creator.addView(social);
        root.addView(creator);

        root.addView(space(18));
        TextView footer = text("ReelMate • Version 1.0 • Use only with content you are permitted to save",
                11, Color.rgb(100, 116, 139));
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, matchWrap());

        setContentView(scroll);
    }

    private LinearLayout guideRow(String number, String title, String description) {
        LinearLayout row = row();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, 0);
        TextView badge = text(number, 13, Color.WHITE);
        badge.setGravity(Gravity.CENTER);
        badge.setTypeface(null, 1);
        badge.setBackground(round(Color.rgb(124, 58, 237), 18));
        row.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        TextView t = text(title, 14, Color.WHITE);
        t.setTypeface(null, 1);
        TextView d = text(description, 12, Color.rgb(148, 163, 184));
        copy.addView(t);
        copy.addView(d);
        row.addView(copy, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private void pasteFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence value = clip.getItemAt(0).coerceToText(this);
                if (value != null) {
                    linkInput.setText(value.toString().trim());
                    linkInput.setSelection(linkInput.length());
                    status.setText("Link pasted. Tap Download Video.");
                    return;
                }
            }
        }
        status.setText("Clipboard does not contain a text link.");
    }

    private void startProcessing() {
        String link = linkInput.getText().toString().trim();
        if (link.isEmpty()) {
            linkInput.setError("Paste an Instagram link first");
            linkInput.requestFocus();
            return;
        }
        autoProcess(link);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null
                && intent.getType().startsWith("text/")) {
            String link = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (link != null && !link.trim().isEmpty()) {
                linkInput.setText(link.trim());
                status.setText("Shared link received. Processing...");
                autoProcess(link.trim());
            }
        }
    }

    private void autoProcess(String link) {
        setLoading(true, "Processing link securely...");
        executor.execute(() -> {
            String directUrl = DownloadResolver.resolveAuthorizedMedia(link);
            runOnUiThread(() -> {
                if (directUrl == null) {
                    setLoading(false, "This public link could not be resolved right now. Please try again later.");
                } else {
                    enqueueDownload(directUrl);
                }
            });
        });
    }

    private void setLoading(boolean loading, String message) {
        status.setText(message);
        downloadButton.setEnabled(!loading);
        downloadButton.setAlpha(loading ? 0.65f : 1f);
        downloadButton.setText(loading ? "Processing..." : "Download Video");
    }

    private void enqueueDownload(String url) {
        try {
            String fileName = "ReelMate_" + System.currentTimeMillis() + ".mp4";
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("ReelMate video");
            request.setDescription("Downloading video");
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, "ReelMate/" + fileName);
            ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
            setLoading(false, "Download started. Check your Downloads/ReelMate folder.");
        } catch (Exception e) {
            setLoading(false, "Download could not be started. Please try again.");
        }
    }

    private void showMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add("Settings");
        popup.getMenu().add("About ReelMate");
        popup.getMenu().add("Contact Creator");
        popup.setOnMenuItemClickListener(item -> {
            String value = item.getTitle().toString();
            if ("Settings".equals(value)) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName())));
            } else if ("About ReelMate".equals(value)) {
                new AlertDialog.Builder(this)
                        .setTitle("About ReelMate")
                        .setMessage("A simple Instagram media companion created by Mala Ram.\n\nVersion 1.0")
                        .setPositiveButton("Close", null)
                        .show();
            } else {
                openUrl("https://www.instagram.com/malaramofficial/");
            }
            return true;
        });
        popup.show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No compatible app found", Toast.LENGTH_SHORT).show();
        }
    }

    private LinearLayout row() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private LinearLayout card(int color, int radius) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(18), dp(18), dp(18), dp(18));
        layout.setBackground(round(color, radius));
        return layout;
    }

    private Button button(String value, int bg, int fg) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextColor(fg);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(round(bg, 16));
        return button;
    }

    private TextView text(String value, float size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private View space(int height) {
        Space space = new Space(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        return space;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }
}
