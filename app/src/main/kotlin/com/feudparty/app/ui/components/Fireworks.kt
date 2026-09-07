package com.feudparty.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.feudparty.app.ui.theme.FeudColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * ألعاب نارية بنفس مفردات التصميم: قصاصات مربّعة ودائرية بألوان اللعبة،
 * كل وحدة بحدّ حبر سميك — مش نقط ملوّنة ناعمة.
 *
 * كل انفجار بيطلع من نقطة، بيتمدّد، وبيهبط شوي بالجاذبية قبل ما يختفي،
 * وبعدين بيعيد من جديد بتوقيت مختلف حتى ما يصيروا كلهم مع بعض.
 */
private data class Burst(
    val x: Float,
    val y: Float,
    val delay: Float,
    val radius: Float,
    val seed: Int
)

private val PALETTE = listOf(
    FeudColors.gold,
    FeudColors.pink,
    FeudColors.teal,
    FeudColors.lime,
    FeudColors.team1,
    FeudColors.team2
)

@Composable
fun Fireworks(
    modifier: Modifier = Modifier,
    bursts: Int = 5,
    sparksPerBurst: Int = 22,
    cycleMillis: Int = 2600
) {
    val random = remember { Random(7) }
    val shots = remember {
        List(bursts) { index ->
            Burst(
                x = 0.14f + random.nextFloat() * 0.72f,
                y = 0.16f + random.nextFloat() * 0.5f,
                delay = index / bursts.toFloat(),
                radius = 0.16f + random.nextFloat() * 0.16f,
                seed = index * 31 + 7
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "fireworks")
    val clock by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(cycleMillis, easing = LinearEasing)),
        label = "fireworksClock"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        shots.forEach { burst ->
            // كل انفجار عنده وقته الخاص ضمن الدورة.
            val life = ((clock + burst.delay) % 1f)
            if (life > 0.75f) return@forEach
            drawBurst(burst, life / 0.75f, sparksPerBurst)
        }
    }
}

private fun DrawScope.drawBurst(burst: Burst, life: Float, sparks: Int) {
    val origin = Offset(size.width * burst.x, size.height * burst.y)
    val spread = burst.radius * size.minDimension * ease(life)
    val fade = (1f - life * life).coerceIn(0f, 1f)
    val gravity = size.height * 0.16f * life * life

    repeat(sparks) { index ->
        val angle = (index / sparks.toFloat()) * Math.PI.toFloat() * 2f +
            burst.seed * 0.37f
        val distance = spread * (0.62f + fraction(burst.seed + index) * 0.38f)
        val point = Offset(
            origin.x + cos(angle) * distance,
            origin.y + sin(angle) * distance + gravity
        )
        val side = size.minDimension * (0.012f + fraction(burst.seed * 3 + index) * 0.012f)
        val color = PALETTE[(burst.seed + index) % PALETTE.size].copy(alpha = fade)
        val ink = FeudColors.ink.copy(alpha = fade)
        val square = (burst.seed + index) % 3 != 0

        if (square) {
            rotate(degrees = life * 260f + index * 12f, pivot = point) {
                drawRect(
                    color = ink,
                    topLeft = Offset(point.x - side / 2f - 2f, point.y - side / 2f - 2f),
                    size = androidx.compose.ui.geometry.Size(side + 4f, side + 4f)
                )
                drawRect(
                    color = color,
                    topLeft = Offset(point.x - side / 2f, point.y - side / 2f),
                    size = androidx.compose.ui.geometry.Size(side, side)
                )
            }
        } else {
            drawCircle(color = ink, radius = side / 2f + 2f, center = point)
            drawCircle(color = color, radius = side / 2f, center = point)
        }
    }

    // ومضة صغيرة بمركز الانفجار أول ما يفرقع.
    val flash = (1f - life * 4f).coerceAtLeast(0f)
    if (flash > 0f) {
        drawCircle(
            color = Color.White.copy(alpha = flash * 0.5f),
            radius = size.minDimension * 0.05f * flash,
            center = origin
        )
    }
}

private fun ease(t: Float): Float = 1f - (1f - t) * (1f - t) * (1f - t)

/** رقم ثابت بين ٠ و١ لكل بذرة — بدل عشوائي جديد كل إطار. */
private fun fraction(seed: Int): Float {
    val value = sin(seed * 12.9898f) * 43758.547f
    return value - kotlin.math.floor(value)
}
