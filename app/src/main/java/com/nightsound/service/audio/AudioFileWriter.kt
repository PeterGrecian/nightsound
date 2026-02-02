package com.nightsound.service.audio

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Writes PCM audio data to WAV files with proper headers.
 */
class AudioFileWriter(
    private val sampleRate: Int = 16000,
    private val channels: Int = 1,
    private val bitsPerSample: Int = 16
) {
    private val TAG = "AudioFileWriter"

    /**
     * Write audio data to a WAV file.
     *
     * @param file Output file
     * @param audioData PCM audio samples (16-bit)
     * @throws IOException if writing fails
     */
    fun writeWavFile(file: File, audioData: ShortArray) {
        try {
            FileOutputStream(file).use { fos ->
                val byteData = shortArrayToByteArray(audioData)
                val wavData = addWavHeader(byteData)
                fos.write(wavData)
            }
            Log.d(TAG, "Wrote WAV file: ${file.name}, size: ${file.length()} bytes")
        } catch (e: IOException) {
            Log.e(TAG, "Error writing WAV file: ${file.name}", e)
            throw e
        }
    }

    /**
     * Write raw byte array to WAV file.
     */
    fun writeWavFile(file: File, audioBytes: ByteArray) {
        try {
            FileOutputStream(file).use { fos ->
                val wavData = addWavHeader(audioBytes)
                fos.write(wavData)
            }
            Log.d(TAG, "Wrote WAV file: ${file.name}, size: ${file.length()} bytes")
        } catch (e: IOException) {
            Log.e(TAG, "Error writing WAV file: ${file.name}", e)
            throw e
        }
    }

    /**
     * Convert short array to byte array (little-endian).
     */
    private fun shortArrayToByteArray(shortArray: ShortArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(shortArray.size * 2)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        for (value in shortArray) {
            byteBuffer.putShort(value)
        }
        return byteBuffer.array()
    }

    /**
     * Add WAV header to PCM data.
     * WAV format: 44-byte header + PCM data
     */
    private fun addWavHeader(pcmData: ByteArray): ByteArray {
        val headerSize = 44
        val totalSize = headerSize + pcmData.size
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteBuffer.allocate(headerSize)
        header.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        header.put("RIFF".toByteArray())
        header.putInt(totalSize - 8) // File size - 8
        header.put("WAVE".toByteArray())

        // fmt chunk
        header.put("fmt ".toByteArray())
        header.putInt(16) // fmt chunk size
        header.putShort(1) // Audio format (1 = PCM)
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort(bitsPerSample.toShort())

        // data chunk
        header.put("data".toByteArray())
        header.putInt(pcmData.size)

        // Combine header and PCM data
        val wavFile = ByteArray(totalSize)
        System.arraycopy(header.array(), 0, wavFile, 0, headerSize)
        System.arraycopy(pcmData, 0, wavFile, headerSize, pcmData.size)

        return wavFile
    }
}
