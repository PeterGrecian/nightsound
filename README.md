# NightSound Android App

An Android app that records audio overnight, detects the loudest audio snippets, and saves them for review. Supports configurable snippet count and duration, periodic saving, scheduled auto-stop, and delayed start.

## Architecture

- **MVVM + Clean Architecture** with Kotlin
- **Jetpack Compose** for UI
- **Foreground Service** for overnight recording
- **Room Database** for local metadata
- **WorkManager** for S3 uploads
- **Hilt** for dependency injection

## Features

- Records audio continuously in the background using a foreground service
- Calculates RMS (Root Mean Square) loudness for each audio chunk
- Maintains top N loudest snippets using a priority queue (min heap)
- Automatically deletes non-top-N files to save storage
- Real-time volume visualizer with bar chart
- Playback interface for saved recordings
- **Configurable snippet count** (1-20) and **chunk duration** (5-60s)
- **Periodic save** — automatically save the N loudest snippets every X minutes, clearing the list so fresh candidates can accumulate
- **Auto-stop** — stop recording at a set time of day (e.g. 6:00 AM)
- **Delayed start** — wait N minutes after pressing Start before recording begins, with a live countdown on screen

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
   - If **Delayed Start** is enabled, a countdown appears on screen — tap "Cancel" to abort
   - Once recording begins, a foreground notification appears
   - Audio is recorded in chunks of the configured duration

2. **Monitor Progress**
   - Real-time volume bar chart shows current audio level
   - Snippet counter shows how many of the top N slots are filled
   - Live list of current top snippets with timestamps and loudness

3. **Stop Recording**
   - Tap "Stop Recording" button, or let **Auto-Stop** end it at the configured time
   - The service finalises the remaining top snippets and saves them to the database
   - Non-top-N files are automatically deleted

### Playback

1. Navigate to Recordings screen (list icon)
2. View all saved snippets sorted by loudness
3. Tap play button to listen to a snippet
4. Delete unwanted recordings

### Settings

Navigate to Settings (gear icon) to configure:

**Recording**
- **Top Snippets** (1-20, default 3) — how many loudest chunks to keep in memory
- **Snippet Length** (5-60s, default 10s) — duration of each audio chunk

**Periodic Save** (toggle, off by default)
- **Save count** (1-10, default 2) — how many of the loudest snippets to save each interval
- **Interval** (15-180 min, default 60) — how often to flush the loudest snippets to the database and clear the in-memory list so recording continues accumulating new candidates

**Auto-Stop Recording** (toggle, off by default)
- **Stop time** (hour + minute, default 6:00 AM) — recording stops automatically at this time of day. If the time has already passed today, it schedules for the next day.

**Delayed Start** (toggle, off by default)
- **Delay** (5-120 min, default 30) — after pressing Start, a countdown runs on the main screen before recording actually begins. Useful for placing the phone down and falling asleep first.

## Technical Details

### Audio Recording

- **Sample Rate**: 16kHz (optimized for voice/snoring)
- **Format**: PCM 16-bit mono
- **Chunk Duration**: Configurable 5-60 seconds (default 10)
- **File Format**: WAV with 44-byte header
- **Storage**: ~320 KB per 10-second chunk

### Loudness Detection

Uses RMS (Root Mean Square) calculation:
```
RMS = sqrt(sum(sample²) / n)
```

### Top N Snippets Algorithm

- **Data Structure**: Min heap (PriorityQueue)
- **Size**: Configurable 1-20 snippets (default 3)
- **Ordering**: By RMS value (ascending)
- **Efficiency**: O(log n) insert, O(1) peek minimum
- **Storage Optimization**: Immediately deletes files not in top N
- **Periodic Save**: Optionally extracts and persists the loudest snippets at a regular interval, then clears them from the heap so new candidates can compete

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

1. **Launcher icons**: Placeholder icons need to be replaced with proper assets
2. **Auto-stop precision**: Uses coroutine delay, so the stop time may drift by a few seconds
3. **Delayed start keeps screen on**: The countdown runs in the ViewModel; the phone can be locked but the countdown continues in memory

## Future Enhancements

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
