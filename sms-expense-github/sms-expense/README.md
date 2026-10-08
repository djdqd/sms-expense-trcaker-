# SMS Expense Tracker

A simple Android expense tracker that reads payment SMS locally, detects likely debit/credit transactions, and lists them in the app.

## Privacy
- SMS processing is local to the phone in v1.
- No backend or cloud service is required by the app.
- The app requests `READ_SMS` and `RECEIVE_SMS` because Android requires explicit permissions for SMS access.
- This project is intended for personal testing/sideloading. Review Google Play SMS permission rules before publishing.

## One-click APK build with GitHub Actions

1. Create a new GitHub repository.
2. Upload the contents of this folder to the repository root (not the parent folder).
3. Push to `main` or `master`, or open **Actions → Build Android APK → Run workflow**.
4. Wait for the workflow to finish.
5. Open the completed workflow run.
6. Under **Artifacts**, download `sms-expense-debug-apk`.
7. Extract the downloaded artifact and install `app-debug.apk` on your Android phone.

No Android Studio is required for the GitHub Actions build.

## Local build

If Android Studio/Gradle is installed, run:

```bash
gradle assembleDebug
```

The APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`
