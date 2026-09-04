# Reels Downloader

## Auto Share → Auto Process → Auto Download

Flow:

1. User shares a permitted media link to the app.
2. Android delivers the link through ACTION_SEND.
3. MainActivity starts processing automatically.
4. DownloadResolver is the backend integration point.
5. The resolver must return a direct HTTPS URL only for media the user is authorized to save.
6. Android DownloadManager starts the download automatically and shows system progress/completion notifications.

### Backend contract

POST `/resolve`

Request:
`{"url":"shared media URL"}`

Response:
`{"authorized":true,"downloadUrl":"https://example.com/file.mp4"}`

The current resolver is intentionally a placeholder until an authorized backend endpoint is configured.
