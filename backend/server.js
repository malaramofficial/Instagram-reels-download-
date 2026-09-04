/**
 * Secure resolver service skeleton.
 *
 * Secrets stay in environment variables:
 *   INSTAGRAM_ACCESS_TOKEN
 *   ALLOWED_INSTAGRAM_ACCOUNT_ID
 *
 * Do not commit real tokens to GitHub.
 */
import express from "express";

const app = express();
app.use(express.json({ limit: "8kb" }));

app.get("/health", (_req, res) => res.json({ ok: true }));

app.post("/resolve", async (req, res) => {
  const sharedUrl = typeof req.body?.url === "string" ? req.body.url.trim() : "";
  if (!isInstagramUrl(sharedUrl)) {
    return res.status(400).json({ authorized: false, error: "Invalid Instagram URL" });
  }

  // TODO: Require your own authenticated user/session here before resolving.
  // TODO: Verify the requested media belongs to the Instagram account(s)
  //       authorized for this app. Never act as a public downloader.
  //
  // When implementing the official Meta API call, read the access token only
  // from process.env.INSTAGRAM_ACCESS_TOKEN and keep it server-side.
  return res.status(501).json({
    authorized: false,
    error: "Official resolver not configured"
  });
});

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
  console.log("Resolver service started");
});