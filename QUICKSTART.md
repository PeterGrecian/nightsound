# NightSound - Quick Start Guide

## 1. Open in Android Studio

```bash
# Navigate to the project directory
cd _nightsound

# Open Android Studio and select "Open an existing project"
# Select the _nightsound directory
```

## 2. First Time Setup

### Generate Gradle Wrapper (if needed)
```bash
# If gradlew doesn't work, generate the wrapper:
gradle wrapper --gradle-version 8.2
```

### Sync Gradle
- Android Studio will automatically prompt to sync
- Click "Sync Now" when prompted
- Wait for all dependencies to download

### Generate Launcher Icons
1. Right-click `app` in project view
2. New → Image Asset
3. Configure Icon Type: Launcher Icons (Adaptive and Legacy)
4. Choose your icon image
5. Click Next → Finish

## 3. Configure AWS S3

### Create S3 Bucket
```bash
# Using AWS CLI
aws s3 mb s3://my-nightsound-recordings --region us-east-1
```

### Create IAM User
1. Go to AWS Console → IAM → Users
2. Create new user: `nightsound-app`
3. Attach policy with S3 permissions (see README.md)
4. Save Access Key ID and Secret Access Key

## 4. Build and Run

### On Emulator
```bash
# Start emulator first, then:
./gradlew installDebug

# Or click the green "Run" button in Android Studio
```

### On Physical Device
1. Enable Developer Options on your phone
2. Enable USB Debugging
3. Connect via USB
4. Click "Run" in Android Studio

## 5. App Configuration

### Grant Permissions
When the app launches:
1. Grant "Record Audio" permission
2. Grant "Post Notifications" permission

### Configure Settings
1. Tap Settings icon (top right)
2. Enter S3 Bucket Name: `my-nightsound-recordings`
3. Enter AWS Region: `us-east-1`
4. Enter AWS Access Key
5. Enter AWS Secret Key
6. Tap "Save S3 Settings"

## 6. Test Recording

### Quick Test (2-3 minutes)
1. Return to main screen
2. Tap "Start Recording"
3. Make various sounds at different volumes:
   - Talk loudly
   - Whisper
   - Clap
   - Stay silent
4. Watch the volume visualizer respond
5. After 2-3 minutes, tap "Stop Recording"

### View Results
1. Tap the List icon (top right) to view recordings
2. You should see up to 10 snippets
3. Tap play button to listen
4. Verify upload status shows "Uploaded"

### Check S3
```bash
# List files in your bucket
aws s3 ls s3://my-nightsound-recordings/recordings/

# Download a file to verify
aws s3 cp s3://my-nightsound-recordings/recordings/1/audio_1_0_1234567890.wav ./test.wav
```

## 7. Overnight Test

1. Plug in your device charger
2. Disable battery optimization for NightSound:
   - Settings → Apps → NightSound → Battery → Unrestricted
3. Start recording before bed
4. Check in the morning:
   - View recordings
   - Check S3 bucket
   - Monitor battery usage

## Common Issues

### Build Fails
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

### Permissions Error
- Go to Settings → Apps → NightSound → Permissions
- Manually enable Microphone and Notifications

### Service Stops Recording
- Disable battery optimization
- Check manufacturer-specific power saving settings
- Ensure wake lock is being held (check logcat)

### S3 Upload Fails
```bash
# Test credentials manually
aws s3 ls s3://my-nightsound-recordings --region us-east-1
```

### No Audio in Playback
- Check logcat for file path errors
- Verify files exist: `adb shell ls /data/data/com.nightsound/cache/audio_recordings/`
- Try different audio output (speaker vs headphones)

## Debug with Logcat

```bash
# View all app logs
adb logcat -s AudioRecordingService:D LoudnessAnalyzer:D TopSnippetsManager:D S3UploadWorker:D

# Clear logcat
adb logcat -c

# Save logs to file
adb logcat > nightsound_logs.txt
```

## Useful ADB Commands

```bash
# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Clear app data
adb shell pm clear com.nightsound

# Check app storage usage
adb shell du -sh /data/data/com.nightsound

# View database
adb shell
su
sqlite3 /data/data/com.nightsound/databases/nightsound_database
.tables
SELECT * FROM audio_snippets;
.exit
```

## Performance Monitoring

```bash
# Monitor CPU usage
adb shell top -n 1 | grep nightsound

# Check battery stats
adb shell dumpsys batterystats | grep nightsound

# Monitor wake locks
adb shell dumpsys power | grep nightsound
```

## Next Steps

- Review README.md for detailed documentation
- Check IMPLEMENTATION_SUMMARY.md for technical details
- Implement unit tests (see app/src/test/)
- Add integration tests (see app/src/androidTest/)
- Customize UI theme colors
- Add more audio processing features

## Getting Help

1. Check logcat for error messages
2. Review code comments in source files
3. Consult Android documentation:
   - https://developer.android.com/guide/topics/media/mediarecorder
   - https://developer.android.com/guide/components/services
   - https://developer.android.com/topic/libraries/architecture/workmanager

Happy coding!
