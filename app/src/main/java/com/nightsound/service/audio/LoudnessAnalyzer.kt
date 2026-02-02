package com.nightsound.service.audio

import kotlin.math.sqrt

/**
 * Analyzes audio data to calculate loudness using RMS (Root Mean Square).
 */
class LoudnessAnalyzer {

    /**
     * Calculate RMS (Root Mean Square) value for an audio buffer.
     * RMS = sqrt(sum(sample²) / n)
     *
     * @param audioData PCM 16-bit audio samples
     * @return RMS value representing loudness
     */
    fun calculateRMS(audioData: ShortArray): Double {
        if (audioData.isEmpty()) return 0.0

        var sum = 0.0
        for (sample in audioData) {
            val normalized = sample.toDouble() / Short.MAX_VALUE
            sum += normalized * normalized
        }

        return sqrt(sum / audioData.size)
    }

    /**
     * Calculate RMS from byte array (16-bit PCM little-endian)
     */
    fun calculateRMSFromBytes(audioBytes: ByteArray): Double {
        if (audioBytes.size < 2) return 0.0

        val shortArray = ShortArray(audioBytes.size / 2)
        for (i in shortArray.indices) {
            val index = i * 2
            // Convert little-endian bytes to short
            shortArray[i] = ((audioBytes[index + 1].toInt() shl 8) or
                            (audioBytes[index].toInt() and 0xFF)).toShort()
        }

        return calculateRMS(shortArray)
    }

    /**
     * Calculate decibel level from RMS
     * dB = 20 * log10(RMS)
     */
    fun rmsToDecibels(rms: Double): Double {
        if (rms <= 0.0) return Double.NEGATIVE_INFINITY
        return 20 * kotlin.math.log10(rms)
    }
}
