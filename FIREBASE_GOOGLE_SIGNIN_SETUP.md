# 🔐 Firebase Google Sign-In Setup Guide

## Prerequisites
- Firebase project already created
- Google Services JSON file (`google-services.json`) already added to `app/` directory

## Step 1: Enable Google Sign-In in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your TaskMaster project
3. Navigate to **Authentication** → **Sign-in method**
4. Click on **Google** provider
5. Toggle **Enable** to ON
6. Add your project's **Support email**
7. Click **Save**

## Step 2: Get Web Client ID

1. In Firebase Console, go to **Project Settings** (gear icon)
2. Scroll down to **Your apps** section
3. Find your **Web app** (if you don't have one, create it)
4. Copy the **Web client ID** from the config object
5. It looks like: `123456789-abcdefghijklmnop.apps.googleusercontent.com`

## Step 3: Update Android App Configuration

1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_WEB_CLIENT_ID_HERE` with your actual Web client ID:
   ```xml
   <string name="default_web_client_id">123456789-abcdefghijklmnop.apps.googleusercontent.com</string>
   ```

## Step 4: Add SHA-1 Fingerprint (for Release)

1. Get your SHA-1 fingerprint:
   ```bash
   # For debug keystore
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # For release keystore (if you have one)
   keytool -list -v -keystore your-release-key.keystore -alias your-key-alias
   ```

2. In Firebase Console, go to **Project Settings** → **Your apps**
3. Select your Android app
4. Click **Add fingerprint**
5. Paste your SHA-1 fingerprint
6. Click **Save**

## Step 5: Test the Integration

1. Build and run your app
2. Try signing in with Google on both Login and Register screens
3. Check Firebase Console → Authentication → Users to see if the user was created

## Troubleshooting

### Common Issues:

1. **"Google sign in failed" error**
   - Check if Web client ID is correct
   - Verify SHA-1 fingerprint is added
   - Ensure Google Sign-In is enabled in Firebase

2. **"Authentication failed" error**
   - Check Firebase project configuration
   - Verify `google-services.json` is up to date
   - Check internet connection

3. **App crashes on Google Sign-In**
   - Check if all dependencies are properly added
   - Verify the button IDs exist in layouts
   - Check Logcat for detailed error messages

## Security Notes

- Never commit your actual Web client ID to public repositories
- Use different Web client IDs for debug and release builds
- Regularly rotate your SHA-1 fingerprints for security

## Next Steps

After successful setup:
1. Test Google Sign-In on both Login and Register screens
2. Verify user data is stored in Firebase Authentication
3. Test logout functionality
4. Consider adding Facebook Sign-In as additional SSO option

---

**Note**: This setup is required for the Google Sign-In feature to work properly. Without proper configuration, the app will show authentication errors.
