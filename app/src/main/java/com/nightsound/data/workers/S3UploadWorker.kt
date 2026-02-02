package com.nightsound.data.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nightsound.data.local.database.NightSoundDatabase
import com.nightsound.data.repository.S3Repository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class S3UploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val s3Repository: S3Repository,
    private val database: NightSoundDatabase
) : CoroutineWorker(context, params) {

    private val TAG = "S3UploadWorker"

    override suspend fun doWork(): Result {
        val snippetId = inputData.getLong(KEY_SNIPPET_ID, -1)
        val bucketName = inputData.getString(KEY_BUCKET_NAME)
        val region = inputData.getString(KEY_REGION) ?: "us-east-1"

        if (snippetId == -1L || bucketName.isNullOrEmpty()) {
            Log.e(TAG, "Invalid input data")
            return Result.failure()
        }

        try {
            // Get snippet from database
            val snippet = database.audioSnippetDao().getSnippetById(snippetId)
            if (snippet == null) {
                Log.e(TAG, "Snippet not found: $snippetId")
                return Result.failure()
            }

            // Check if already uploaded
            if (snippet.uploadedToS3) {
                Log.d(TAG, "Snippet already uploaded: ${snippet.fileName}")
                return Result.success()
            }

            // Get the file
            val cacheDir = File(applicationContext.cacheDir, "audio_recordings")
            val file = File(cacheDir, snippet.fileName)

            if (!file.exists()) {
                Log.e(TAG, "File not found: ${file.path}")
                return Result.failure()
            }

            // Generate S3 key
            val s3Key = "recordings/${snippet.sessionId}/${snippet.fileName}"

            // Upload to S3
            val success = s3Repository.uploadFile(
                file = file,
                bucketName = bucketName,
                key = s3Key,
                region = region
            )

            if (success) {
                // Mark as uploaded in database
                database.audioSnippetDao().markAsUploaded(snippetId, s3Key)

                // Delete local file to save space
                if (file.delete()) {
                    Log.d(TAG, "Deleted local file: ${file.name}")
                }

                Log.d(TAG, "Successfully uploaded snippet: ${snippet.fileName}")
                return Result.success()
            } else {
                Log.e(TAG, "Failed to upload snippet: ${snippet.fileName}")
                return Result.retry()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error in S3UploadWorker", e)
            return Result.retry()
        }
    }

    companion object {
        const val KEY_SNIPPET_ID = "snippet_id"
        const val KEY_BUCKET_NAME = "bucket_name"
        const val KEY_REGION = "region"
        const val WORK_NAME_PREFIX = "s3_upload_"
    }
}
