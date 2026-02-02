# NightSound Android App

An Android app that records audio overnight, detects the 10 loudest 10-second snippets, and uploads them to S3.

## Architecture

- **MVVM + Clean Architecture** with Kotlin
- **Jetpack Compose** for UI
- **Foreground Service** for overnight recording
- **Room Database** for local metadata
- **WorkManager** for S3 uploads
- **Hilt** for dependency injection

## Features

- Records audio continuously in the background using a foreground service
- Calculates RMS (Root Mean Square) loudness for each 10-second audio chunk
- Maintains top 10 loudest snippets using a priority queue (min heap)
- Automatically deletes non-top-10 files to save storage
- Uploads recordings to AWS S3 after recording completes
- Real-time volume visualizer
- Playback interface for saved recordings
- Configurable settings (night time, S3 credentials)

## Project Structure

```
app/src/main/java/com/nightsound/
├── data/
│   ├── local/
│   │   ├── dao/              # Room DAOs
│   │   ├── database/         # Room Database
│   │   └── entities/         # Room Entities
│   ├── remote/               # (Future: API services)
│   ├── repository/           # Data repositories
│   └── workers/              # WorkManager workers
├── di/                       # Hilt dependency injection
├── service/                  # Foreground service
│   └── audio/                # Audio processing components
├── ui/                       # Jetpack Compose UI
│   ├── navigation/           # Navigation graph
│   ├── screens/              # Screen composables
│   │   ├── main/            # Main recording screen
│   │   ├── playback/        # Playback screen
│   │   └── settings/        # Settings screen
│   └── theme/               # App theme
└── util/                     # Utility classes
```

## Setup Instructions

### Prerequisites

- Android Studio (Iguana or later)
- Android SDK 26+ (minimum), SDK 34 (target)
- JDK 17
- AWS S3 account (for uploads)

### Build Steps

1. **Clone or copy the project**
   ```bash
   cd _nightsound
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - File → Open → Select the `_nightsound` directory
   - Wait for Gradle sync to complete

3. **Generate launcher icons** (optional but recommended)
   - Right-click on `app` → New → Image Asset
   - Create launcher icons for the app
   - The placeholder PNG files are already created in mipmap folders

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on device or emulator**
   - Connect an Android device or start an emulator
   - Click Run (green play button) in Android Studio
   - Or use: `./gradlew installDebug`

## Configuration

### S3 Setup

1. **Create an S3 bucket** in AWS Console
2. **Configure IAM permissions** - Create an IAM user with S3 upload permissions:
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Effect": "Allow",
         "Action": [
           "s3:PutObject",
           "s3:GetObject"
         ],
         "Resource": "arn:aws:s3:::your-bucket-name/*"
       }
     ]
   }
   ```
3. **Get credentials** - Note the Access Key ID and Secret Access Key

### App Configuration

1. Open the app
2. Grant microphone and notification permissions
3. Navigate to Settings
4. Configure:
   - S3 Bucket Name (e.g., `my-nightsound-recordings`)
   - AWS Region (e.g., `us-east-1`)
   - AWS Access Key
   - AWS Secret Key
5. Save settings

**Security Note**: For production use, implement AWS Cognito or STS for temporary credentials instead of storing long-term credentials in the app.

## Usage

### Recording Audio

1. **Start Recording**
   - Open the app
   - Tap "Start Recording" button
   - A foreground notification appears
   - The app records audio in 10-second chunks

2. **Monitor Progress**
   - Real-time volume visualizer shows current audio level
   - Snippet counter shows how many of the top 10 slots are filled

3. **Stop Recording**
   - Tap "Stop Recording" button
   - The service finalizes the top 10 snippets
   - Non-top-10 files are automatically deleted
   - S3 upload workers are enqueued

### Playback

1. Navigate to Recordings screen (list icon)
2. View all saved snippets sorted by loudness
3. Tap play button to listen to a snippet
4. View upload status for each snippet
5. Delete unwanted recordings

### Settings

- Configure night start/end times (display only in current implementation)
- Set S3 bucket and region
- Enter AWS credentials

## Technical Details

### Audio Recording

- **Sample Rate**: 16kHz (optimized for voice/snoring)
- **Format**: PCM 16-bit mono
- **Chunk Duration**: 10 seconds
- **File Format**: WAV with 44-byte header
- **Storage**: ~320 KB per 10-second chunk

### Loudness Detection

Uses RMS (Root Mean Square) calculation:
```
RMS = sqrt(sum(sample²) / n)
```

### Top 10 Snippets Algorithm

- **Data Structure**: Min heap (PriorityQueue)
- **Size**: 10 snippets maximum
- **Ordering**: By RMS value (ascending)
- **Efficiency**: O(log n) insert, O(1) peek minimum
- **Storage Optimization**: Immediately deletes files not in top 10

### Battery Optimization

- Partial wake lock only (no screen wake)
- 16kHz sample rate (lower CPU usage than 44.1kHz)
- Efficient buffer processing
- Foreground service for reliable operation

### S3 Upload

- WorkManager ensures reliable upload even after app closes
- Automatic retry on failure
- Uploads in background after recording completes
- Deletes local files after successful upload

## Testing

### Unit Tests

```bash
./gradlew test
```

Key test areas:
- `LoudnessAnalyzer`: RMS calculation accuracy
- `TopSnippetsManager`: Priority queue logic
- `AudioFileWriter`: WAV header generation

### Integration Tests

```bash
./gradlew connectedAndroidTest
```

### Manual Testing

1. **Grant permissions**: Verify app requests and handles permissions correctly
2. **Short recording**: Record for 2-3 minutes with varying volumes
3. **Overnight test**: Let it run for 8+ hours
4. **Playback**: Verify all saved snippets play correctly
5. **S3 upload**: Check S3 bucket for uploaded files
6. **Battery usage**: Monitor Settings → Battery to verify reasonable usage

## Known Limitations

1. **Time pickers**: Settings screen shows times but doesn't have interactive pickers yet
2. **Launcher icons**: Placeholder icons need to be replaced with proper assets
3. **AWS credentials**: Stored in DataStore (use Cognito in production)
4. **No scheduled recording**: Manual start/stop required
5. **No audio visualization**: Volume visualizer is basic circle animation

## Future Enhancements

- Scheduled recording based on night start/end times
- Advanced audio visualization (waveform, spectrogram)
- Export recordings to local storage
- Share recordings via email/messaging apps
- Audio event detection (snoring, coughing, etc.)
- Cloud sync for settings across devices
- Wear OS companion app for remote control

## Troubleshooting

### Recording doesn't start
- Check microphone permission is granted
- Check notification permission is granted
- Verify device has sufficient storage

### No audio in playback
- Check file exists in app cache
- Verify AudioRecord permissions
- Check device audio output settings

### S3 upload fails
- Verify S3 bucket name is correct
- Check AWS credentials are valid
- Ensure IAM user has PutObject permission
- Check internet connectivity

### Service stops overnight
- Disable battery optimization for the app
- Check manufacturer-specific battery settings
- Verify wake lock is being held

## Dependencies

- Jetpack Compose BOM 2023.10.01
- Room 2.6.1
- WorkManager 2.9.0
- Hilt 2.48
- AWS SDK for Kotlin 1.0.30
- Kotlin Coroutines 1.7.3
- DataStore 1.0.0

## License

This project is provided as-is for educational and development purposes.

## Support

For issues or questions, check the implementation plan document or review the inline code comments.
