package com.feudparty.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class RevealMotionTest {

    @Test
    fun `single new reveal gets no delay`() {
        val delays = revealDelays(previous = setOf(0), current = setOf(0, 3))
        assertEquals(mapOf(3 to 0f), delays)
    }

    @Test
    fun `several reveals in one update are staggered in board order`() {
        val delays = revealDelays(previous = setOf(1), current = setOf(1, 5, 2, 7), step = 0.12f)
        assertEquals(0f, delays.getValue(2), 1e-6f)
        assertEquals(0.12f, delays.getValue(5), 1e-6f)
        assertEquals(0.24f, delays.getValue(7), 1e-6f)
        assertEquals(setOf(2, 5, 7), delays.keys)
    }

    @Test
    fun `slots that were already revealed keep no delay`() {
        val delays = revealDelays(previous = setOf(0, 1), current = setOf(0, 1))
        assertEquals(emptyMap<Int, Float>(), delays)
    }

    @Test
    fun `a fresh board with reveals already in it is not staggered`() {
        // لاعب انضم بنص الجولة: كل شي مكشوف بيبين فوراً بدون طابور.
        val delays = revealDelays(previous = null, current = setOf(0, 2))
        assertEquals(emptyMap<Int, Float>(), delays)
    }
}
