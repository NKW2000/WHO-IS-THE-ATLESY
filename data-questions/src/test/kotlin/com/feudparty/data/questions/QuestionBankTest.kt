package com.feudparty.data.questions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class QuestionBankTest {
    @Test
    fun `load parses starter questions with answers sorted by points descending`() {
        val questions = QuestionBank.load()
        assertTrue(questions.isNotEmpty())
        val first = questions.first()
        assertEquals("q001", first.id)
        val points = first.answers.map { it.points }
        assertEquals(points.sortedDescending(), points)
    }

    @Test
    fun `every question is well formed`() {
        val questions = QuestionBank.load()
        assertTrue("لازم يكون في ٥٠ سؤال عالأقل", questions.size >= 50)
        assertEquals("في معرّفات مكررة", questions.size, questions.map { it.id }.toSet().size)
        questions.forEach { question ->
            assertTrue("سؤال بدون نص: ${question.id}", question.text.isNotBlank())
            assertTrue("سؤال بدون تصنيف: ${question.id}", question.category.isNotBlank())
            assertTrue("سؤال بأقل من ٣ أجوبة: ${question.id}", question.answers.size >= 3)
            assertTrue(
                "أجوبة بدون نص أو بنقاط غير موجبة: ${question.id}",
                question.answers.all { it.text.isNotBlank() && it.points > 0 }
            )
            assertTrue(
                "ما في جواب مكشوف مسبقاً: ${question.id}",
                question.answers.none { it.revealed }
            )
        }
    }

    @Test
    fun `randomRound returns the requested number of distinct questions`() {
        val round = QuestionBank.randomRound(count = 8, random = Random(42))
        assertEquals(8, round.size)
        assertEquals(8, round.map { it.id }.toSet().size)
    }

    @Test
    fun `randomRound is deterministic for a given seed`() {
        assertEquals(
            QuestionBank.randomRound(count = 5, random = Random(7)).map { it.id },
            QuestionBank.randomRound(count = 5, random = Random(7)).map { it.id }
        )
    }

    @Test
    fun `randomRound never asks for more questions than the bank holds`() {
        val all = QuestionBank.load()
        assertEquals(all.size, QuestionBank.randomRound(count = all.size + 100).size)
    }

    @Test
    fun `randomGame splits rounds and fast money without repeating a question`() {
        val game = QuestionBank.randomGame(rounds = 6, fastMoneyCount = 5, random = Random(11))
        assertEquals(6, game.rounds.size)
        assertEquals(5, game.fastMoney.size)
        val ids = (game.rounds + game.fastMoney).map { it.id }
        assertEquals(11, ids.toSet().size)
    }

    @Test
    fun `randomGame is deterministic for a given seed`() {
        val first = QuestionBank.randomGame(rounds = 4, random = Random(3))
        val second = QuestionBank.randomGame(rounds = 4, random = Random(3))
        assertEquals(first.rounds.map { it.id }, second.rounds.map { it.id })
        assertEquals(first.fastMoney.map { it.id }, second.fastMoney.map { it.id })
    }
}
