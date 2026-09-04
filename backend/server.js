/**
 * Official Meta API integration for media accessible to the authorized account.
 * Secrets stay in the deployment environment.
 */
import express from "express";

const app = express();
app.use(express.json({ limit: "8kb" }));

const GRAPH_VERSION = process.env.META_GRAPH_VERSION || "v23.0";
const TOKEN = process.env.INSTAGRAM_ACCESS_TOKEN;
const ACCOUNT_ID = process.env.ALLOWED_INSTAGRAM_ACCOUNT_ID;
const PRIMARY_HOST = (process.env.META_API_HOST || "https://graph.instagram.com").replace(/\/$/, "");

app.get("/", (_req, res) => {
  res.json({ service: "authorized-instagram-media-resolver", ok: true });
});

app.get("/health", (_req, res) => {
  res.json({
    ok: true,
    configured: Boolean(TOKEN && ACCOUNT_ID),
    apiHost: PRIMARY_HOST,
    graphVersion: GRAPH_VERSION
  });
});

app.get("/account", async (_req, res) => {
  if (!TOKEN) {
    return res.status(503).json({ ok: false, configured: false, error: "Server API credentials are not configured" });
  }

  try {
    const endpoint = new URL(`${PRIMARY_HOST}/${GRAPH_VERSION}/me`);
    endpoint.searchParams.set("fields", "id,username");
    endpoint.searchParams.set("access_token", TOKEN);

    const response = await fetch(endpoint);
    const payload = await safeJson(response);

    if (!response.ok) {
      return res.status(502).json({
        ok: false,
        error: payload?.error?.message || "Unable to read authorized account"
      });
    }

    return res.json({
      ok: true,
      id: payload?.id || null,
      username: payload?.username || null
    });
  } catch (error) {
    return res.status(502).json({ ok: false, error: error?.message || "Request failed" });
  }
});

app.get("/diagnostics", async (_req, res) => {
  if (!TOKEN || !ACCOUNT_ID) {
    return res.status(503).json({ ok: false, configured: false, error: "Server API credentials are not configured" });
  }

  try {
    const response = await fetch(mediaListUrl());
    const payload = await safeJson(response);
    return res.status(response.ok ? 200 : 502).json({
      ok: response.ok,
      configured: true,
      graphVersion: GRAPH_VERSION,
      accountIdConfigured: true,
      status: response.status,
      mediaCountOnFirstPage: Array.isArray(payload?.data) ? payload.data.length : 0,
      error: response.ok ? null : (payload?.error?.message || "Meta API request failed")
    });
  } catch (error) {
    return res.status(502).json({ ok: false, configured: true, error: error?.message || "Request failed" });
  }
});

app.post("/resolve", async (req, res) => {
  const originalUrl = typeof req.body?.url === "string" ? req.body.url.trim() : "";

  if (!isInstagramUrl(originalUrl)) {
    return res.status(400).json({ authorized: false, error: "Invalid Instagram URL" });
  }
  if (!TOKEN || !ACCOUNT_ID) {
    return res.status(503).json({ authorized: false, error: "Server API credentials are not configured" });
  }

  try {
    const sharedUrl = normalizeInstagramUrl(await canonicalizeInstagramUrl(originalUrl));
    const shortcode = instagramShortcode(sharedUrl);
    const result = await findAuthorizedMedia(sharedUrl, shortcode);

    if (!result.item) {
      return res.status(404).json({
        authorized: false,
        error: "This media was not found in the authorized account's accessible media list",
        checkedUrl: sharedUrl,
        shortcode
      });
    }

    const fresh = await getMediaById(result.item.id);
    const item = fresh || result.item;

    if (!item.media_url) {
      return res.status(404).json({
        authorized: false,
        error: "No media URL was returned by the official API for this item"
      });
    }

    return res.json({
      authorized: true,
      mediaId: item.id,
      mediaType: item.media_type,
      downloadUrl: item.media_url,
      thumbnailUrl: item.thumbnail_url || null,
      permalink: item.permalink || sharedUrl
    });
  } catch (error) {
    console.error("Resolve failed:", error?.message || error);
    return res.status(500).json({ authorized: false, error: "Resolver failed" });
  }
});

async function findAuthorizedMedia(sharedUrl, shortcode) {
  let endpoint = mediaListUrl();
  let pages = 0;
  let lastError = null;

  while (endpoint && pages < 50) {
    pages += 1;
    try {
      const response = await fetch(endpoint);
      const payload = await safeJson(response);

      if (!response.ok) {
        lastError = new Error(payload?.error?.message || "Meta API request failed");
        break;
      }

      const item = Array.isArray(payload?.data)
        ? payload.data.find((media) => sameInstagramMedia(media.permalink, sharedUrl, shortcode))
        : null;

      if (item) return { item };
      endpoint = payload?.paging?.next || null;
    } catch (error) {
      lastError = error;
      break;
    }
  }

  if (lastError) console.warn("Media lookup failed:", lastError.message || lastError);
  return { item: null };
}

function mediaListUrl() {
  const endpoint = new URL(`${PRIMARY_HOST}/${GRAPH_VERSION}/${encodeURIComponent(ACCOUNT_ID)}/media`);
  endpoint.searchParams.set("fields", "id,permalink,media_type,media_url,thumbnail_url,timestamp");
  endpoint.searchParams.set("limit", "100");
  endpoint.searchParams.set("access_token", TOKEN);
  return endpoint.toString();
}

async function getMediaById(mediaId) {
  const endpoint = new URL(`${PRIMARY_HOST}/${GRAPH_VERSION}/${encodeURIComponent(mediaId)}`);
  endpoint.searchParams.set("fields", "id,permalink,media_type,media_url,thumbnail_url,timestamp");
  endpoint.searchParams.set("access_token", TOKEN);

  const response = await fetch(endpoint);
  if (!response.ok) return null;
  return safeJson(response);
}

async function canonicalizeInstagramUrl(value) {
  try {
    const response = await fetch(value, {
      method: "GET",
      redirect: "follow",
      headers: { "User-Agent": "Mozilla/5.0" }
    });
    return response.url || value;
  } catch {
    return value;
  }
}

async function safeJson(response) {
  try { return await response.json(); } catch { return null; }
}

function isInstagramUrl(value) {
  try {
    const u = new URL(value);
    const host = u.hostname.toLowerCase();
    return (u.protocol === "https:" || u.protocol === "http:") &&
      (host === "instagram.com" || host.endsWith(".instagram.com"));
  } catch {
    return false;
  }
}

function normalizeInstagramUrl(value) {
  try {
    const u = new URL(value);
    const host = u.hostname.toLowerCase();
    if (!(host === "instagram.com" || host.endsWith(".instagram.com"))) return "";
    const path = u.pathname.replace(/\/+$/, "") || "/";
    return `https://www.instagram.com${path === "/" ? "/" : path + "/"}`;
  } catch {
    return "";
  }
}

function instagramShortcode(value) {
  try {
    const parts = new URL(value).pathname.split("/").filter(Boolean);
    const marker = parts.findIndex((part) => ["reel", "p", "tv"].includes(part.toLowerCase()));
    return marker >= 0 ? parts[marker + 1] || null : null;
  } catch {
    return null;
  }
}

function sameInstagramMedia(a, b, shortcode) {
  const left = normalizeInstagramUrl(a || "");
  const right = normalizeInstagramUrl(b || "");
  if (left && right && left === right) return true;
  const leftCode = instagramShortcode(left);
  return Boolean(shortcode && leftCode && shortcode === leftCode);
}

app.listen(process.env.PORT || 3000, () => {
  console.log("Authorized media resolver started");
});
