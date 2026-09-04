package com.malaram.reelsdownloader;

/**
 * Backend resolver URL. No Instagram credentials are stored in the app.
 */
public final class ApiConfig {
    private ApiConfig() {}

    public static final String RESOLVER_URL =
            "https://instagram-reels-python-resolver.onrender.com/resolve";
}
