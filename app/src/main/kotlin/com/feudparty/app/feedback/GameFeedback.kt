package com.feudparty.app.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.feudparty.app.R

/**
 * نوع التنبيه — كل واحد له صوت ونمط اهتزاز.
 * الستريكات تلاتة، وكل وحدة إلها صوتها زي البرنامج.
 */
enum class Cue {
    REVEAL, STRIKE_1, STRIKE_2, STRIKE_3, WRONG, WIN, BUZZ, CLOCK,
    // أصوات المشاهد — المقدمة، افتتاحية الجولة، «استعدوا»، والنتائج.
    INTRO, ROUND_START, VERSUS, COUNT, CROWN, FANFARE, FIREWORK, JOIN, TAP
}

/**
 * الصوت والاهتزاز مع بعض. لعبة بتنلعب بغرفة فيها ناس، فالتنبيه لازم
 * يوصل بالأذن وبالإيد مش بس بالعين.
 */
class GameFeedback(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds: Map<Cue, Int> = mapOf(
        Cue.REVEAL to soundPool.load(appContext, R.raw.sfx_reveal, 1),
        Cue.STRIKE_1 to soundPool.load(appContext, R.raw.sfx_strike1, 1),
        Cue.STRIKE_2 to soundPool.load(appContext, R.raw.sfx_strike2, 1),
        Cue.STRIKE_3 to soundPool.load(appContext, R.raw.sfx_strike3, 1),
        Cue.WRONG to soundPool.load(appContext, R.raw.sfx_wrong, 1),
        Cue.WIN to soundPool.load(appContext, R.raw.sfx_win, 1),
        Cue.BUZZ to soundPool.load(appContext, R.raw.sfx_press, 1),
        Cue.CLOCK to soundPool.load(appContext, R.raw.sfx_clock, 1),
        Cue.INTRO to soundPool.load(appContext, R.raw.sfx_intro, 1),
        Cue.ROUND_START to soundPool.load(appContext, R.raw.sfx_round_start, 1),
        Cue.VERSUS to soundPool.load(appContext, R.raw.sfx_versus, 1),
        Cue.COUNT to soundPool.load(appContext, R.raw.sfx_count, 1),
        Cue.CROWN to soundPool.load(appContext, R.raw.sfx_crown, 1),
        Cue.FANFARE to soundPool.load(appContext, R.raw.sfx_fanfare, 1),
        Cue.FIREWORK to soundPool.load(appContext, R.raw.sfx_firework, 1),
        Cue.JOIN to soundPool.load(appContext, R.raw.sfx_join, 1),
        Cue.TAP to soundPool.load(appContext, R.raw.sfx_tap, 1)
    )

    /** صوت الخطأ حسب رقمه: الأول، التاني، التالت. */
    fun playStrike(number: Int) = play(
        when (number) {
            1 -> Cue.STRIKE_1
            2 -> Cue.STRIKE_2
            else -> Cue.STRIKE_3
        }
    )

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = appContext.getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun play(cue: Cue) {
        sounds[cue]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
        vibrate(cue)
    }

    /**
     * دقّات آخر خمس ثواني. بترجّع رقم المجرى حتى نقدر نسكّتها إذا اللاعب
     * جاوب قبل ما يخلص الوقت.
     */
    fun startClock(): Int? = sounds[Cue.CLOCK]?.let { soundPool.play(it, 1f, 1f, 2, 0, 1f) }

    fun stopStream(streamId: Int) {
        soundPool.stop(streamId)
    }

    private fun vibrate(cue: Cue) {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        // نمط مميّز لكل حدث — الغلط ضربتين، الفوز ثلاث نبضات. و`null`
        // يعني هاد المشهد بدون اهتزاز.
        val timings = vibrationPattern(cue) ?: return
        val amplitudes = timings.mapIndexed { index, _ ->
            if (index % 2 == 0) 0 else VibrationEffect.DEFAULT_AMPLITUDE
        }.toIntArray()

        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    fun release() {
        soundPool.release()
    }
}

val LocalGameFeedback = compositionLocalOf<GameFeedback?> { null }

/** بيوفّر نسخة وحدة للتطبيق كله وبيسكّرها لما تنتهي الشاشة. */
@Composable
fun ProvideGameFeedback(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val feedback = remember(context) { GameFeedback(context) }
    DisposableEffect(feedback) { onDispose { feedback.release() } }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalGameFeedback provides feedback,
        content = content
    )
}
