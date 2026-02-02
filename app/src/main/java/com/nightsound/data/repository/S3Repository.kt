package com.nightsound.data.repository

import android.content.Context
import android.util.Log
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class S3Repository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "S3Repository"

    /**
     * Upload a file to S3.
     *
     * @param file The file to upload
     * @param bucketName S3 bucket name
     * @param key S3 object key (path in bucket)
     * @return true if upload successful, false otherwise
     */
    suspend fun uploadFile(
        file: File,
        bucketName: String,
        key: String,
        region: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: ${file.path}")
                return@withContext false
            }

            Log.d(TAG, "Uploading file: ${file.name} to s3://$bucketName/$key")

            // Note: In production, use AWS Cognito or STS for temporary credentials
            // For now, this assumes credentials are configured via environment or AWS config

            S3Client {
                region = region
            }.use { s3 ->
                val request = PutObjectRequest {
                    bucket = bucketName
                    this.key = key
                    body = ByteStream.fromFile(file)
                }

                s3.putObject(request)
                Log.d(TAG, "Successfully uploaded: ${file.name}")
                return@withContext true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading file: ${file.name}", e)
            return@withContext false
        }
    }

    /**
     * Delete a file from S3.
     */
    suspend fun deleteFile(
        bucketName: String,
        key: String,
        region: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            S3Client {
                this.region = region
            }.use { s3 ->
                s3.deleteObject {
                    bucket = bucketName
                    this.key = key
                }
                Log.d(TAG, "Successfully deleted: s3://$bucketName/$key")
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file from S3: $key", e)
            return@withContext false
        }
    }
}
