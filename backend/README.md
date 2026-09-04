# Authorized Instagram Resolver

This folder is a backend skeleton for the Android app.

## Security rules
- Never put an Instagram/Meta access token in the Android APK.
- Never commit `.env` or real secrets to GitHub.
- Keep `INSTAGRAM_ACCESS_TOKEN` only on the server.
- Require authentication and verify ownership/authorization before returning media.
- Use HTTPS in production.

## Deploy
1. Copy `.env.example` to your deployment platform's secret/environment settings.
2. Set the real server-side token there.
3. Implement the official Meta API lookup in `server.js` only for media your app is authorized to access.
4. Deploy over HTTPS.
5. Put the final `https://...` endpoint in Android `ApiConfig.RESOLVER_URL`.

The current endpoint deliberately returns HTTP 501 until authorization and official API lookup are implemented.
