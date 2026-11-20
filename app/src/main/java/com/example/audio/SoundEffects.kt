package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

class SoundEffects(context: Context) {
    private val appContext = context.applicationContext
    private val audioDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + audioDispatcher)
    private var activeTrack: AudioTrack? = null
    private val mediaLock = Any()
    private var activeMediaPlayer: MediaPlayer? = null

    private val sampleRate = 44100

    private val hungryMeowPcm: ShortArray by lazy {
        synthesizeCuteKittenMeow(
            durationMs = 460,
            f0Start = 660.0,
            f0Peak = 1220.0,
            f0End = 860.0,
            questionLift = true
        )
    }

    private val sadMeowPcm: ShortArray by lazy {
        synthesizeCuteKittenMeow(
            durationMs = 640,
            f0Start = 880.0,
            f0Peak = 920.0,
            f0End = 450.0,
            questionLift = false
        )
    }

    fun playHungryMeow(enabled: Boolean) {
        if (!enabled) return
        playBuffer(hungryMeowPcm)
    }

    fun playHappyChime(enabled: Boolean) {
        if (!enabled) return
        playRawMp3(R.raw.kimochi)
    }

    fun playAngryBuzz(enabled: Boolean) {
        if (!enabled) return
        playRawMp3(R.raw.what_the_hell)
    }

    fun playGameOver(enabled: Boolean) {
        if (!enabled) return
        playBuffer(sadMeowPcm)
    }

    private fun playRawMp3(resId: Int) {
        scope.launch {
            stopActiveAudio()
            var player: MediaPlayer? = null
            runCatching {
                val mp = MediaPlayer()
                player = mp
                mp.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                appContext.resources.openRawResourceFd(resId).use { afd ->
                    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                }
                mp.setVolume(1.0f, 1.0f)
                mp.setOnCompletionListener { completed ->
                    runCatching { completed.release() }
                    synchronized(mediaLock) {
                        if (activeMediaPlayer === completed) {
                            activeMediaPlayer = null
                        }
                    }
                }
                mp.setOnErrorListener { failed, _, _ ->
                    runCatching { failed.release() }
                    synchronized(mediaLock) {
                        if (activeMediaPlayer === failed) {
                            activeMediaPlayer = null
                        }
                    }
                    true
                }
                mp.prepare()
                synchronized(mediaLock) {
                    activeMediaPlayer = mp
                }
                mp.start()
            }.onFailure {
                player?.let { failed ->
                    runCatching { failed.release() }
                    synchronized(mediaLock) {
                        if (activeMediaPlayer === failed) {
                            activeMediaPlayer = null
                        }
                    }
                }
            }
        }
    }

    fun release() {
        stopActiveAudio()
        runCatching { audioDispatcher.close() }
    }

    private fun stopActiveAudio() {
        activeTrack?.let { prev ->
            runCatching {
                prev.pause()
                prev.flush()
                prev.release()
            }
        }
        activeTrack = null

        val prevPlayer = synchronized(mediaLock) {
            val p = activeMediaPlayer
            activeMediaPlayer = null
            p
        }
        prevPlayer?.let { mp ->
            runCatching {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
        }
    }

    private fun playBuffer(pcm: ShortArray) {
        scope.launch {
            runCatching {
                stopActiveAudio()

                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(4096)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
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

                activeTrack = track
                track.setVolume(AudioTrack.getMaxVolume())
                track.play()
                track.write(pcm, 0, pcm.size)
                val tailSilence = ShortArray(sampleRate / 15)
                track.write(tailSilence, 0, tailSilence.size)
                track.stop()
                track.release()
                if (activeTrack === track) {
                    activeTrack = null
                }
            }
        }
    }

    /**
     * Synthesizes a clear, sweet kitten "Mee-yaa-ow" using a soft glottal source
     * shaped by two sweeping vocal-tract formant resonators (F1 & F2).
     */
    private fun synthesizeCuteKittenMeow(
        durationMs: Int,
        f0Start: Double,
        f0Peak: Double,
        f0End: Double,
        questionLift: Boolean
    ): ShortArray {
        val count = (sampleRate * durationMs) / 1000
        val raw = DoubleArray(count)
        var phase = 0.0

        // Biquad states for two vocal-tract formants
        var f1Y1 = 0.0
        var f1Y2 = 0.0
        var f2Y1 = 0.0
        var f2Y2 = 0.0

        for (i in 0 until count) {
            val u = i.toDouble() / count
            val t = i.toDouble() / sampleRate

            // Expressive pitch contour: sweet rise then gentle slide (plus optional cute question lift)
            val baseF0 = when {
                u < 0.30 -> {
                    val k = u / 0.30
                    f0Start + (f0Peak - f0Start) * sin(k * PI * 0.5)
                }
                questionLift && u > 0.78 -> {
                    val k = (u - 0.78) / 0.22
                    f0End + 160.0 * sin(k * PI * 0.5)
                }
                else -> {
                    val k = (u - 0.30) / 0.70
                    f0Peak + (f0End - f0Peak) * (k * k)
                }
            }

            // Gentle kitten vibrato
            val f0 = baseF0 * (1.0 + 0.015 * sin(2.0 * PI * 7.5 * t))
            phase = (phase + f0 / sampleRate) % 1.0

            // Soft glottal pulse (rounded sawtooth + fundamental warmth + subtle breathiness)
            val glottal = sin(2.0 * PI * phase) +
                0.55 * sin(4.0 * PI * phase) +
                0.30 * sin(6.0 * PI * phase) +
                0.15 * sin(8.0 * PI * phase)
            val breath = Random.nextDouble(-0.04, 0.04) * (1.0 - u * 0.4)
            val source = glottal * 0.55 + breath

            // Formant 1 & 2 sweep: "m" (closed) -> "ee" -> "aa" -> "ow"
            val f1Freq = when {
                u < 0.16 -> 450.0 + (u / 0.16) * 350.0
                u < 0.50 -> 800.0 + ((u - 0.16) / 0.34) * 520.0
                else -> 1320.0 - ((u - 0.50) / 0.50) * 520.0
            }
            val f2Freq = when {
                u < 0.25 -> 2550.0
                else -> 2550.0 - ((u - 0.25) / 0.75) * 1050.0
            }

            val y1 = applyResonator(source, f1Freq, bandwidthHz = 140.0, y1 = f1Y1, y2 = f1Y2)
            f1Y2 = f1Y1
            f1Y1 = y1

            val y2 = applyResonator(source, f2Freq, bandwidthHz = 220.0, y1 = f2Y1, y2 = f2Y2)
            f2Y2 = f2Y1
            f2Y1 = y2

            // Nasal "m" onset mixes fundamental softly before mouth opens into bright "yaa"
            val mouthOpen = (u / 0.18).coerceIn(0.0, 1.0)
            val attack = (i / (sampleRate * 0.022)).coerceAtMost(1.0)
            val release = ((count - i) / (sampleRate * 0.075)).coerceAtMost(1.0)
            val env = attack * release

            val vocal = (sin(2.0 * PI * phase) * 0.45 * (1.0 - 0.4 * mouthOpen)) +
                (y1 * 0.52 * (0.35 + 0.65 * mouthOpen)) +
                (y2 * 0.30 * mouthOpen)

            raw[i] = vocal * env
        }

        return normalizeToPcm(raw, targetPeak = 0.88)
    }

    private fun synthesizeHappyMeow(): ShortArray {
        // Iconic "Kucing Sigma" sound: deep, smooth, confident baritone "Meee-ooow~"
        // with a punchy sigma bass drop underneath.
        val durationMs = 560
        val count = (sampleRate * durationMs) / 1000
        val raw = DoubleArray(count)
        var phase = 0.0
        var subPhase = 0.0

        var f1Y1 = 0.0
        var f1Y2 = 0.0
        var f2Y1 = 0.0
        var f2Y2 = 0.0

        for (i in 0 until count) {
            val u = i.toDouble() / count
            val t = i.toDouble() / sampleRate

            // Confident deep baritone "Meee-ooow" pitch contour
            val f0 = when {
                u < 0.28 -> {
                    val k = u / 0.28
                    175.0 + (290.0 - 175.0) * sin(k * PI * 0.5)
                }
                else -> {
                    val k = (u - 0.28) / 0.72
                    290.0 - (290.0 - 128.0) * (k * k)
                }
            }
            phase = (phase + f0 / sampleRate) % 1.0

            // Rich baritone glottal source
            val glottal = sin(2.0 * PI * phase) +
                0.65 * sin(4.0 * PI * phase) +
                0.42 * sin(6.0 * PI * phase) +
                0.24 * sin(8.0 * PI * phase) +
                0.12 * sin(10.0 * PI * phase)

            // Deeper male/sigma vocal tract formants ("m" -> "ee" -> "aa" -> "ow")
            val f1Freq = when {
                u < 0.18 -> 320.0 + (u / 0.18) * 260.0
                u < 0.52 -> 580.0 + ((u - 0.18) / 0.34) * 220.0
                else -> 800.0 - ((u - 0.52) / 0.48) * 340.0
            }
            val f2Freq = when {
                u < 0.25 -> 1650.0
                else -> 1650.0 - ((u - 0.25) / 0.75) * 700.0
            }

            val y1 = applyResonator(glottal * 0.48, f1Freq, bandwidthHz = 110.0, y1 = f1Y1, y2 = f1Y2)
            f1Y2 = f1Y1
            f1Y1 = y1

            val y2 = applyResonator(glottal * 0.48, f2Freq, bandwidthHz = 170.0, y1 = f2Y1, y2 = f2Y2)
            f2Y2 = f2Y1
            f2Y1 = y2

            val mouthOpen = (u / 0.16).coerceIn(0.0, 1.0)
            val attack = (i / (sampleRate * 0.025)).coerceAtMost(1.0)
            val release = ((count - i) / (sampleRate * 0.095)).coerceAtMost(1.0)
            val vocalEnv = attack * release

            val sigmaVoice = (sin(2.0 * PI * phase) * 0.52 * (1.0 - 0.25 * mouthOpen)) +
                (y1 * 0.62 * (0.35 + 0.65 * mouthOpen)) +
                (y2 * 0.26 * mouthOpen)

            // Punchy sigma phonk sub-bass thump at start & mid-drop
            val subFreq = 62.0 - 24.0 * u
            subPhase = (subPhase + subFreq / sampleRate) % 1.0
            val subEnv = exp(-t * 6.5) * 0.45
            val subHit = sin(2.0 * PI * subPhase) * subEnv

            raw[i] = sigmaVoice * vocalEnv + subHit
        }

        return normalizeToPcm(raw, targetPeak = 0.92)
    }

    private fun synthesizePureCatHiss(): ShortArray {
        // Pure realistic feline hiss ("KHHHHSSSSSHH!") with explosive onset & dual air resonances
        val hissMs = 480
        val count = (sampleRate * hissMs) / 1000
        val hissRaw = DoubleArray(count)
        var bp1Y1 = 0.0
        var bp1Y2 = 0.0
        var bp2Y1 = 0.0
        var bp2Y2 = 0.0
        var prevNoise = 0.0

        for (i in 0 until count) {
            val u = i.toDouble() / count
            val t = i.toDouble() / sampleRate

            val white = Random.nextDouble(-1.0, 1.0)
            // High-pass emphasis for sharp air friction
            val crispAir = white - 0.68 * prevNoise
            prevNoise = white

            // Lower throat-air resonance (~2350 Hz) + bright dental hiss (~5100 Hz)
            val fMid = 2500.0 - 450.0 * u
            val midBand = applyResonator(crispAir, freqHz = fMid, bandwidthHz = 850.0, y1 = bp1Y1, y2 = bp1Y2)
            bp1Y2 = bp1Y1
            bp1Y1 = midBand

            val fHigh = 5300.0 - 800.0 * u
            val highBand = applyResonator(crispAir, freqHz = fHigh, bandwidthHz = 1400.0, y1 = bp2Y1, y2 = bp2Y2)
            bp2Y2 = bp2Y1
            bp2Y1 = highBand

            // Sharp plosive "K" burst at start followed by intense rushing air
            val burst = if (u < 0.08) 1.35 else 1.0
            val throatFlutter = 0.88 + 0.12 * sin(2.0 * PI * 36.0 * t)
            val attack = (i / (sampleRate * 0.012)).coerceAtMost(1.0)
            val sustainFade = when {
                u < 0.42 -> 1.0
                else -> exp(-(u - 0.42) * 4.2)
            }

            hissRaw[i] = (midBand * 0.55 + highBand * 0.85) * burst * throatFlutter * attack * sustainFade
        }

        return normalizeToPcm(hissRaw, targetPeak = 0.90)
    }

    private fun applyResonator(
        input: Double,
        freqHz: Double,
        bandwidthHz: Double,
        y1: Double,
        y2: Double
    ): Double {
        val r = exp(-PI * bandwidthHz / sampleRate)
        val cosTheta = cos(2.0 * PI * freqHz / sampleRate)
        val a1 = -2.0 * r * cosTheta
        val a2 = r * r
        val gain = 1.0 - r
        return gain * input - a1 * y1 - a2 * y2
    }

    private fun normalizeToPcm(samples: DoubleArray, targetPeak: Double): ShortArray {
        var maxAbs = 0.0
        for (s in samples) {
            val a = abs(s)
            if (a > maxAbs) maxAbs = a
        }
        val scale = if (maxAbs > 1e-6) (targetPeak / maxAbs) else 1.0
        val out = ShortArray(samples.size)
        for (i in samples.indices) {
            val v = (samples[i] * scale * Short.MAX_VALUE).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out[i] = v.toShort()
        }
        return out
    }
}
