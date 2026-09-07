package com.feudparty.app.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.feudparty.core.game.GameState
import com.feudparty.core.game.PlayerMark

/**
 * بيراقب حالة اللعبة وبيشغّل الصوت/الاهتزاز عند كل حدث: كشف جواب، خطأ،
 * ونهاية جولة. منشغّلها بشاشة المضيف وبشاشات اللاعبين حتى يسمع الكل.
 */
@Composable
fun GameStateCues(state: GameState?) {
    val feedback = LocalGameFeedback.current ?: return
    var lastRevealed by remember { mutableIntStateOf(state.revealedCount()) }
    var lastStrikes by remember { mutableIntStateOf(state?.strikes ?: 0) }
    var lastAward by remember { mutableStateOf(state?.lastAward) }

    LaunchedEffect(state?.revealedCount(), state?.strikes, state?.lastAward) {
        if (state == null) return@LaunchedEffect

        val revealed = state.revealedCount()
        val award = state.lastAward

        when {
            // نهاية الجولة بتكشف اللوح كله، فبنعلن الفوز مش كل خانة.
            award != null && award != lastAward -> feedback.play(Cue.WIN)
            state.strikes > lastStrikes -> feedback.playStrike(state.strikes)
            revealed > lastRevealed -> feedback.play(Cue.REVEAL)
        }

        lastRevealed = revealed
        lastStrikes = state.strikes
        lastAward = award
    }
}

/**
 * صوت البزر — بس بالمواجهة، لأنه هناك السرعة هي اللعبة. بمرحلة اللعب
 * اللمسة مجرد إشارة للمضيف إنك عم تجاوب، فما بدها صوت. الصح والغلط
 * بيجوا من [GameStateCues] مرة وحدة، حتى ما ينعاد الصوت مرتين.
 */
@Composable
fun PlayerMarkCues(mark: PlayerMark, faceOff: Boolean) {
    val feedback = LocalGameFeedback.current ?: return
    var last by remember { mutableStateOf(mark) }

    LaunchedEffect(mark, faceOff) {
        if (mark != last) {
            if (mark == PlayerMark.BUZZED && faceOff) feedback.play(Cue.BUZZ)
            last = mark
        }
    }
}

private fun GameState?.revealedCount(): Int =
    this?.currentQuestion?.answers?.count { it.revealed } ?: 0
