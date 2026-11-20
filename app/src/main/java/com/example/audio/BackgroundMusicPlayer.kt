package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

/**
 * Generates and loops a gentle, cozy lo-fi kalimba & warm electric-piano
 * background track ("musik santai") at soft volume underneath cat SFX.
 */
class BackgroundMusicPlayer {
    private val bgmDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + bgmDispatcher)

    private val sampleRate = 22050
    private var loopJob: Job? = null
    private var audioTrack: AudioTrack? = null

    @Volatile
    private var isSoundEnabled = true

    @Volatile
    private var isAppForeground = true

    private val loopPcm: ShortArray by lazy { synthesizeCozyLoop() }

    fun updateState(soundEnabled: Boolean, appForeground: Boolean) {
        isSoundEnabled = soundEnabled
        isAppForeground = appForeground
        if (soundEnabled && appForeground) {
            startLoopIfNeeded()
        } else {
            stopLoop()
        }
    }

    private fun startLoopIfNeeded() {
        if (loopJob?.isActive == true) return
        loopJob = scope.launch {
            runCatching {
                val pcm = loopPcm
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.setVolume(0.42f)
                track.play()

                val chunkSize = 2048
                var cursor = 0
                while (isActive && isSoundEnabled && isAppForeground) {
                    val remaining = pcm.size - cursor
                    val toWrite = minOf(chunkSize, remaining)
                    val written = track.write(pcm, cursor, toWrite)
                    if (written <= 0) break
                    cursor += written
                    if (cursor >= pcm.size) {
                        cursor = 0
                    }
                }

                runCatching {
                    track.pause()
                    track.flush()
                    track.release()
                }
                if (audioTrack === track) {
                    audioTrack = null
                }
            }
        }
    }

    private fun stopLoop() {
        loopJob?.cancel()
        loopJob = null
        audioTrack?.let { track ->
            runCatching {
                track.pause()
                track.flush()
                track.release()
            }
        }
        audioTrack = null
    }

    fun release() {
        isAppForeground = false
        stopLoop()
        bgmDispatcher.close()
    }

    private fun synthesizeCozyLoop(): ShortArray {
        // 8 bars at 75 BPM -> 1 beat = 0.8s, 1 bar (4 beats) = 3.2s, 4 bars = 12.8s
        val beatSec = 0.80
        val totalBeats = 16
        val totalSec = totalBeats * beatSec
        val totalSamples = (sampleRate * totalSec).toInt()
        val mix = DoubleArray(totalSamples)

        // Cozy cat-cafe chord progression (Cmaj7 -> Am7 -> Fmaj7 -> G6)
        val chords = listOf(
            listOf(130.81, 261.63, 329.63, 392.00, 493.88), // Cmaj7
            listOf(110.00, 220.00, 261.63, 329.63, 392.00), // Am7
            listOf(174.61, 261.63, 349.23, 440.00, 523.25), // Fmaj7
            listOf(196.00, 246.94, 293.66, 392.00, 440.00)  // G6
        )

        // Add warm sustained felt-piano pad chords (4 beats per chord)
        chords.forEachIndexed { chordIdx, freqs ->
            val startSample = (chordIdx * 4.0 * beatSec * sampleRate).toInt()
            val durSamples = (4.0 * beatSec * sampleRate).toInt()
            for (n in 0 until durSamples) {
                val idx = (startSample + n) % totalSamples
                val t = n.toDouble() / sampleRate
                val u = n.toDouble() / durSamples
                val env = (n / (sampleRate * 0.14)).coerceAtMost(1.0) *
                    ((durSamples - n) / (sampleRate * 0.22)).coerceAtMost(1.0)
                var chordSum = 0.0
                freqs.forEachIndexed { fIdx, f ->
                    val weight = if (fIdx == 0) 0.40 else 0.22
                    chordSum += weight * (sin(2.0 * PI * f * t) + 0.15 * sin(4.0 * PI * f * t))
                }
                mix[idx] += chordSum * env * 0.34
            }
        }

        // Delicate kalimba / music-box melody notes (in beats)
        val melody = listOf(
            // Bar 1 (Cmaj7)
            0.0 to 659.25, 1.0 to 587.33, 1.5 to 523.25, 2.5 to 659.25, 3.5 to 783.99,
            // Bar 2 (Am7)
            4.0 to 659.25, 5.0 to 523.25, 5.5 to 440.00, 6.5 to 523.25, 7.5 to 587.33,
            // Bar 3 (Fmaj7)
            8.0 to 523.25, 9.0 to 440.00, 9.5 to 392.00, 10.5 to 440.00, 11.5 to 523.25,
            // Bar 4 (G6)
            12.0 to 587.33, 13.0 to 493.88, 13.5 to 440.00, 14.5 to 392.00, 15.25 to 493.88
        )

        val noteSamples = (1.15 * sampleRate).toInt()
        for ((beatPos, freq) in melody) {
            val startSample = (beatPos * beatSec * sampleRate).toInt()
            for (n in 0 until noteSamples) {
                val idx = (startSample + n) % totalSamples
                val t = n.toDouble() / sampleRate
                val attack = (n / (sampleRate * 0.012)).coerceAtMost(1.0)
                val decay = exp(-t * 3.4)
                val tone = sin(2.0 * PI * freq * t) + 0.22 * sin(4.0 * PI * freq * t) * exp(-t * 6.0)
                mix[idx] += tone * attack * decay * 0.32
            }
        }

        // Normalize cleanly for warm, soothing level
        var maxAbs = 0.0
        for (v in mix) {
            val a = abs(v)
            if (a > maxAbs) maxAbs = a
        }
        val scale = if (maxAbs > 1e-6) (0.72 / maxAbs) else 1.0
        val out = ShortArray(totalSamples)
        for (i in mix.indices) {
            out[i] = (mix[i] * scale * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }
}
