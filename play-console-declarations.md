# Play Console Permission Declarations

## 1. FOREGROUND_SERVICE_DATA_SYNC Permission

### Task Type
Select: **Network processing** (under Data sync)

### Description (copy-paste)
```
Our app is an IPTV streaming platform that allows users to download video content for offline viewing. When a user initiates a video download, the app uses a foreground service with FOREGROUND_SERVICE_DATA_SYNC to download the video file from our servers to the user's device.

The download task must start immediately because the user has explicitly requested to download content for offline access. The foreground service ensures the download continues reliably even if the user navigates away from the download screen within the app, showing a persistent notification with download progress.

The task cannot be paused or delayed because:
1. The user expects the download to begin immediately upon tapping the download button
2. Video files are large and delaying the download would result in a poor user experience
3. The foreground notification keeps the user informed of download progress and allows them to manage active downloads

The foreground service is only active while a download is in progress and is stopped once the download completes or is cancelled by the user.
```

### Video Demonstration
Record a video showing:
1. Open the app and browse to a video
2. Tap the download button on a video
3. Show the foreground notification appearing with download progress
4. Show the download completing and the video being available offline

Upload to YouTube as unlisted and paste the link.

---

## 2. REQUEST_INSTALL_PACKAGES Permission

### Core Purpose
Select: **None of these**

> Note: This permission is used for in-app app updates (checking and installing newer APK versions). Since "app updates" is not one of the listed core purposes, select "None of these."

### Usage
Select: **App functionality**

### Description (copy-paste)
```
Our app uses the REQUEST_INSTALL_PACKAGES permission to provide in-app update functionality. The app checks our server for newer versions of the APK and, when an update is available, allows the user to download and install the updated APK directly from within the app.

This is necessary because:
1. The app is distributed both through Google Play and directly to users in regions where Play Store may not be available
2. Users who installed the app outside of Play Store need a way to receive updates
3. The in-app update feature prompts the user with a dialog showing the new version details and asks for explicit consent before downloading and installing the update

The permission is only triggered when the user explicitly taps "Update" after being informed about the new version. The user is always in control and can decline the update.
```

### Video Demonstration
Record a video showing:
1. Open the app
2. Show the app detecting a new version is available (update dialog/notification)
3. Show the user tapping "Update"
4. Show the APK being downloaded and the install prompt appearing
5. Show the user confirming the installation

Upload to YouTube as unlisted and paste the link.

---

## 3. Alternative: Remove Unused Permissions

If your app does NOT actually use these features (no in-app APK update, no foreground download service), you should **remove the permissions** instead of declaring them:

### To remove FOREGROUND_SERVICE_DATA_SYNC:
- Remove from `android/app/src/main/AndroidManifest.xml` line 6:
  `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC"/>`

### To remove REQUEST_INSTALL_PACKAGES:
- This permission is likely injected by a plugin. To find which plugin adds it:
  - Check `android/app/build/intermediates/merged_manifests/` for the permission
  - Or search plugin AndroidManifest.xml files in `~/.pub-cache/hosted/pub.dev/`
- If the app doesn't need in-app APK updates, remove the plugin or add `tools:node="remove"` to your manifest:
  `<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" tools:node="remove"/>`

---

## Summary of Actions Needed

| Permission | Action | Priority |
|---|---|---|
| FOREGROUND_SERVICE_DATA_SYNC | Declare with video OR remove if unused | High |
| REQUEST_INSTALL_PACKAGES | Declare with video OR remove if unused | High |
| Target API 36 | Already configured (compileSdk 36, targetSdk 36) | Done |
| 16 KB page size | Already configured (NDK 28, AGP 8.11.1) | Done |
