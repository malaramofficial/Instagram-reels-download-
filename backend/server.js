/**
 * Official Meta API integration for media owned by the authorized account.
 * Secrets remain only in the deployment environment.
 */
import express from "express";

const app = express();
app.use(express.json({ limit: "8kb" }));

const GRAPH_VERSION = process.env.META_GRAPH_VERSION || "v23.0";
const TOKEN = process.env.INSTAGRAM_ACCESS_TOKEN;
const ACCOUNT_ID = process.env.ALLOWED_INSTAGRAM_ACCOUNT_ID;

app.get("/", (_req, res) => res.json({ service: "authorized-instagram-media-resolver", ok: true }));
app.get("/health", (_req, res) => res.json({
  ok: true,
  configured: Boolean(TOKEN && ACCOUNT_ID)
}));

app.post("/resolve", async (req, res) => {
  const sharedUrl = typeof req.body?.url === "string" ? normalize(req.body.url) : "";
  if (!isInstagramUrl(sharedUrl)) {
    return res.status(400).json({ authorized: false, error: "Invalid Instagram URL" });
  }
  if (!TOKEN || !ACCOUNT_ID) {
    return res.status(503).json({ authorized: false, error: "Server API credentials are not configured" });
  }

  try {
    // Official API workflow: enumerate media accessible to the authorized account,
    // then match the canonical permalink supplied by the app.
    const endpoint = new URL(`https://graph.facebook.com/${GRAPH_VERSION}/${encodeURIComponent(ACCOUNT_ID)}/media`);
    endpoint.searchParams.set("fields", "id,permalink,media_type,media_url,thumbnail_url,timestamp");
    endpoint.searchParams.set("limit", "100");
    endpoint.searchParams.set("access_token", TOKEN);

    const response = await fetch(endpoint);
    const payload = await response.json();
    if (!response.ok) {
      return res.status(502).json({ authorized: false, error: "Meta API request failed" });
    }

    const item = Array.isArray(payload.data)
      ? payload.data.find(media => normalize(media.permalink || "") === sharedUrl)
      : null;

    if (!item) {
      return res.status(404).json({
        authorized: false,
        error: "This URL is not media accessible to the authorized Instagram account"
      });
    }

    if (!item.media_url) {
      return res.status(404).json({ authorized: false, error: "No downloadable media URL was returned by the API" });
    }

    return res.json({
      authorized: true,
      mediaId: item.id,
      mediaType: item.media_type,
      downloadUrl: item.media_url,
      thumbnailUrl: item.thumbnail_url || null
    });
  } catch (error) {
    console.error("Resolve failed:", error?.message || error);
    return res.status(500).json({ authorized: false, error: "Resolver failed" });
  }
});

function normalize(value) {
  try {
    const u = new URL(value);
    u.hash = "";
    u.search = "";
    let result = u.toString();
    return result.endsWith("/") ? result : result + "/";
  } catch {
    return String(value).trim();
  }
}

function isInstagramUrl(value) {
  try {
    const u = new URL(value);
    return (u.protocol === "https:" || u.protocol === "http:") &&
      /(^|\.)instagram\.com$/i.test(u.hostname);
  } catch {
    return false;
  }
}

app.listen(process.env.PORT || 3000, () => {
  console.log("Authorized media resolver started");
});