# Storage Problem Solutions for WasteSmart App

## 🔧 **Steps to Fix Storage Issues:**

### 1. **Firebase Storage Configuration**

#### A. Update Storage Rules in Firebase Console:
1. Go to Firebase Console → Your Project → Storage → Rules
2. Replace the rules with the content from `FIREBASE_STORAGE_RULES.txt`
3. Click "Publish"

#### B. Check Firebase Configuration:
1. Verify `google-services.json` is in the correct location (`app/` directory)
2. Ensure Firebase Storage is enabled in your project
3. Check if billing is enabled (required for uploads)

### 2. **Network & Connectivity Issues**

#### Check Internet Connection:
- App now validates network connectivity before upload
- Error message: "No internet connection. Please check your network and try again."

#### Solutions:
- Ensure device has stable internet connection
- Try switching between WiFi and mobile data
- Check firewall settings if on corporate network

### 3. **File Size & Format Issues**

#### Image Size Limits:
- **Maximum file size:** 5MB per image
- **Automatic compression:** Images are compressed to max 1024x1024 pixels
- **Supported formats:** JPEG, PNG

#### Error Messages:
- "Photo file is too large. Please use a smaller image (max 5MB)."
- "Selected image is too large. Please choose a smaller image (max 5MB)."

### 4. **Permission Issues**

#### Required Permissions:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

#### Runtime Permission Checks:
- Camera permission for photo capture
- Gallery permission for image selection (Android 13+)
- All permissions are requested automatically

### 5. **Firebase Authentication Issues**

#### User Authentication:
- User must be logged in to upload images
- Error message: "Please log in to submit a report"

#### Solutions:
- Ensure user is properly authenticated
- Check Firebase Auth configuration
- Verify user session hasn't expired

### 6. **Storage Path Issues**

#### Correct Storage Structure:
```
/waste_reports/
  /{userId}/
    /{timestamp}.jpg
```

#### File Naming Convention:
- Format: `yyyyMMdd_HHmmss.jpg`
- Example: `20250624_143022.jpg`

### 7. **Common Error Messages & Solutions**

| Error Message | Cause | Solution |
|---------------|-------|----------|
| "Object not exists" | File path issue or Firebase rules | Check Firebase Storage rules |
| "Photo file not found" | Camera capture failed | Retake photo, check camera permissions |
| "Error uploading image" | Network or Firebase issue | Check internet connection, Firebase config |
| "Firebase initialization error" | Config problem | Verify google-services.json file |
| "Cannot access selected image" | Gallery permission | Grant gallery permission |

### 8. **Debug Steps**

#### Enable Detailed Logging:
1. Check logcat with filter: `ReportWasteActivity`
2. Look for detailed error messages and file paths
3. Verify Firebase Storage upload progress

#### Test Network Connectivity:
```bash
# Test Firebase Storage access
curl -I https://firebasestorage.googleapis.com
```

#### Check File Permissions:
1. Verify app has storage permissions
2. Check if external storage is available
3. Ensure app-specific directory is writable

### 9. **Production Checklist**

#### Before Deployment:
- [ ] Firebase Storage rules configured
- [ ] Billing enabled for Firebase project
- [ ] Image compression working
- [ ] File size validation active
- [ ] Network connectivity checks in place
- [ ] Error handling for all upload scenarios
- [ ] User authentication working
- [ ] FileProvider properly configured

#### Performance Optimization:
- [ ] Image compression reduces file sizes
- [ ] Upload progress indicators working
- [ ] Retry mechanisms for failed uploads
- [ ] Offline storage for later upload (future enhancement)

### 10. **Testing Storage Functionality**

#### Test Cases:
1. **Camera Photo Upload:** Take photo → Upload → Verify in Firebase Storage
2. **Gallery Photo Upload:** Select image → Upload → Verify in Firebase Storage
3. **Large File Handling:** Try uploading >5MB image → Should show error
4. **Network Issues:** Disable internet → Try upload → Should show error
5. **Authentication:** Logout → Try upload → Should prompt login

#### Firebase Console Verification:
1. Go to Firebase Console → Storage
2. Navigate to `waste_reports/{userId}/`
3. Verify uploaded images appear with correct names
4. Check file sizes are reasonable (compressed)

## 🚀 **Expected Results After Fixes:**

✅ Images upload successfully to Firebase Storage  
✅ File size validation prevents oversized uploads  
✅ Image compression reduces storage costs  
✅ Network validation prevents wasted upload attempts  
✅ Clear error messages guide users to solutions  
✅ Proper authentication ensures security  
✅ Detailed logging helps with debugging  

## 📞 **If Issues Persist:**

1. Check Firebase Console for detailed error logs
2. Verify Firebase project configuration
3. Test with different devices/network conditions
4. Review logcat output for specific error details
5. Ensure all dependencies are up to date
