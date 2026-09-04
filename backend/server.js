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
const FALLBACK_HOST = PRIMARY_HOST.includes("graph.instagram.com")
  ? "https://graph.facebook.com"
  : "https://graph.instagram.com";

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

app.get("/diagnostics", async (_req, res) => {
  if (!TOKEN || !ACCOUNT_ID) {
    return res.status(503).json({
      ok: false,
      configured: false,
      error: "Server API credentials are not configured"
    });
  }

  const attempts = [];
  for (const host of uniqueHosts()) {
    try {
      const response = await fetch(mediaListUrl(host));
      const payload = await safeJson(response);
      attempts.push({
        host,
        ok: response.ok,
        status: response.status,
        mediaCountOnFirstPage: Array.isArray(payload?.data) ? payload.data.length : 0,
        error: response.ok ? null : (payload?.error?.message || "Meta API request failed")
      });
      if (response.ok) {
        return res.json({ ok: true, configured: true, graphVersion: GRAPH_VERSION, attempts });
      }
    } catch (error) {
      attempts.push({ host, ok: false, error: error?.message || "Request failed" });
    }
  }

  return res.status(502).json({
    ok: false,
    configured: true,
    graphVersion: GRAPH_VERSION,
    attempts
  });
});

app.post("/resolve", async (req, res) => {
  const originalUrl = typeof req.body?.url === "string" ? req.body.url.trim() : "";

  if (!isInstagramUrl(originalUrl)) {
    return res.status(400).json({ authorized: false, error: "Invalid Instagram URL" });
  }

  if (!TOKEN || !ACCOUNT_ID) {
    return res.status(503).json({
      authorized: false,
      error: "Server API credentials are not configured"
    });
  }

  try {
    const sharedUrl = await canonicalizeInstagramUrl(originalUrl);
    const result = await findAuthorizedMedia(sharedUrl);

    if (!result.item) {
      return res.status(404).json({
        authorized: false,
        error: "This URL is not media accessible to the authorized Instagram account",
        checkedUrl: sharedUrl
      });
    }

    const fresh = await getMediaById(result.host, result.item.id);
    const item = fresh || result.item;

    if (!item.media_url) {
      return res.status(404).json({
        authorized: false,
        error: "No downloadable media URL was returned by the official API"
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

function uniqueHosts() {
  return [...new Set([PRIMARY_HOST, FALLBACK_HOST])];
}

async function findAuthorizedMedia(sharedUrl) {
  let lastError = null;

  for (const host of uniqueHosts()) {
    try {
      let endpoint = mediaListUrl(host);
      let pages = 0;

      while (endpoint && pages < 50) {
        pages += 1;
        const response = await fetch(endpoint);
        const payload = await safeJson(response);

        if (!response.ok) {
          lastError = new Error(payload?.error?.message || "Meta API request failed");
          break;
        }

        const item = Array.isArray(payload?.data)
          ? payload.data.find((media) => sameInstagramUrl(media.permalink, sharedUrl))
          : null;

        if (item) return { item, host };
        endpoint = payload?.paging?.next || null;
      }
    } catch (error) {
      lastError = error;
    }
  }

  if (lastError) console.warn("Media lookup failed:", lastError.message || lastError);
  return { item: null, host: PRIMARY_HOST };
}

function mediaListUrl(host) {
  const endpoint = new URL(`${host}/${GRAPH_VERSION}/${encodeURIComponent(ACCOUNT_ID)}/media`);
  endpoint.searchParams.set(
    "fields",
    "id,permalink,media_type,media_url,thumbnail_url,timestamp"
  );
  endpoint.searchParams.set("limit", "100");
  endpoint.searchParams.set("access_token", TOKEN);
  return endpoint.toString();
}

async function getMediaById(host, mediaId) {
  const endpoint = new URL(`${host}/${GRAPH_VERSION}/${encodeURIComponent(mediaId)}`);
  endpoint.searchParams.set(
    "fields",
    "id,permalink,media_type,media_url,thumbnail_url,timestamp"
  );
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
    const finalUrl = response.url || value;
    return isInstagramUrl(finalUrl) ? normalizeInstagramUrl(finalUrl) : normalizeInstagramUrl(value);
  } catch {
    return normalizeInstagramUrl(value);
  }
}

async function safeJson(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
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
    return `https://www.instagram.com${path.endsWith("/") ? path : path + "/"}`;
  } catch {
    return "";
  }
}

function sameInstagramUrl(a, b) {
  return normalizeInstagramUrl(a || "") === normalizeInstagramUrl(b || "");
}

app.listen(process.env.PORT || 3000, () => {
  console.log("Authorized media resolver started");
});
