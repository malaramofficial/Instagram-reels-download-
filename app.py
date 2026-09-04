import logging
from urllib.parse import urlparse

from flask import Flask, jsonify, request
import yt_dlp

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
ALLOWED_HOSTS = {"instagram.com", "www.instagram.com", "m.instagram.com"}

@app.get("/")
def root():
    return jsonify({"service": "public-instagram-media-resolver", "ok": True})

@app.get("/health")
def health():
    return jsonify({"ok": True, "engine": "yt-dlp", "cookiesConfigured": False})

@app.post("/resolve")
def resolve():
    body = request.get_json(silent=True) or {}
    shared_url = body.get("url", "")
    if not isinstance(shared_url, str) or not is_allowed_instagram_url(shared_url):
        return jsonify({"authorized": False, "error": "Please provide a valid public Instagram URL"}), 400
    try:
        info = extract_public_media(shared_url)
        media_url = pick_media_url(info)
        if not media_url:
            return jsonify({"authorized": False, "error": "No directly accessible media stream was returned for this public link"}), 404
        return jsonify({"authorized": True, "downloadUrl": media_url, "title": info.get("title"), "id": info.get("id"), "webpageUrl": info.get("webpage_url") or shared_url})
    except yt_dlp.utils.DownloadError as exc:
        app.logger.info("yt-dlp could not resolve URL: %s", str(exc))
        return jsonify({"authorized": False, "error": "This link is unavailable publicly or currently cannot be resolved"}), 422
    except Exception:
        app.logger.exception("Resolver failure")
        return jsonify({"authorized": False, "error": "Resolver failed"}), 500

def is_allowed_instagram_url(value):
    try:
        parsed = urlparse(value.strip())
        return parsed.scheme in {"http", "https"} and parsed.hostname in ALLOWED_HOSTS
    except Exception:
        return False

def extract_public_media(url):
    options = {"quiet": True, "no_warnings": True, "skip_download": True, "noplaylist": True, "format": "best[ext=mp4]/best", "socket_timeout": 20}
    with yt_dlp.YoutubeDL(options) as ydl:
        return ydl.extract_info(url, download=False)

def pick_media_url(info):
    direct = info.get("url")
    if isinstance(direct, str) and direct.startswith(("http://", "https://")):
        return direct
    formats = info.get("formats") or []
    candidates = [item for item in formats if item.get("url") and item.get("vcodec") != "none"]
    if not candidates:
        candidates = [item for item in formats if item.get("url")]
    if not candidates:
        return None
    candidates.sort(key=lambda item: (item.get("height") or 0, item.get("tbr") or 0), reverse=True)
    return candidates[0].get("url")

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=10000)
