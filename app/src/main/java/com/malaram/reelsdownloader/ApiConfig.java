package com.malaram.reelsdownloader;

/**
 * No Instagram token belongs in the Android app.
 * The resolver URL points to the deployed HTTPS backend.
 */
public final class ApiConfig {
    private ApiConfig() {}

    public static final String RESOLVER_URL =
            "https://instagram-reels-download.onrender.com/resolve";
}
