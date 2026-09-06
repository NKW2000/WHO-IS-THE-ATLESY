package com.feudparty.data.questions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** استيراد بنك أسئلة من ملف المضيف. */
class BankImportTest {

    private fun failureOf(json: String): String {
        val result = QuestionBank.parse(json)
        assertTrue("توقعنا رفض الملف", result is BankResult.Failure)
        return (result as BankResult.Failure).message
    }

    @Test
    fun `a valid bank is parsed and its answers sorted by points`() {
        val result = QuestionBank.parse(
            """
            [
              {
                "text": "سؤال",
                "category": "أكل",
                "answers": [
                  {"text": "ب", "points": 20},
                  {"text": "أ", "points": 55},
                  {"text": "ج", "points": 10}
                ]
              }
            ]
            """.trimIndent()
        )

        assertTrue(result is BankResult.Success)
        val question = (result as BankResult.Success).questions.single()
        assertEquals("أكل", question.category)
        assertEquals(listOf("أ", "ب", "ج"), question.answers.map { it.text })
        assertEquals(listOf(55, 20, 10), question.answers.map { it.points })
    }

    @Test
    fun `id and category are optional`() {
        val result = QuestionBank.parse(
            """[{"text": "سؤال", "answers": [{"text": "أ", "points": 10}, {"text": "ب", "points": 5}]}]"""
        )

        val question = (result as BankResult.Success).questions.single()
        assertEquals("q1", question.id)
        assertEquals("عام", question.category)
    }

    @Test
    fun `broken json is refused with a readable reason`() {
        assertTrue(failureOf("{ليس JSON").contains("JSON"))
    }

    @Test
    fun `an empty bank is refused`() {
        assertTrue(failureOf("[]").contains("فاضي"))
    }

    @Test
    fun `a question with a single answer is refused`() {
        val message = failureOf("""[{"text": "سؤال", "answers": [{"text": "أ", "points": 10}]}]""")
        assertTrue(message.contains("١") || message.contains("1"))
    }

    @Test
    fun `a question without text is refused`() {
        val message = failureOf(
            """[{"text": "  ", "answers": [{"text": "أ", "points": 10}, {"text": "ب", "points": 5}]}]"""
        )
        assertTrue(message.contains("بدون نص"))
    }

    @Test
    fun `an answer worth nothing is refused`() {
        val message = failureOf(
            """[{"text": "سؤال", "answers": [{"text": "أ", "points": 10}, {"text": "ب", "points": 0}]}]"""
        )
        assertTrue(message.contains("صفر"))
    }

    @Test
    fun `too many answers are refused`() {
        val answers = (1..QuestionBank.MAX_ANSWERS + 1).joinToString(",") {
            """{"text": "جواب $it", "points": $it}"""
        }
        val message = failureOf("""[{"text": "سؤال", "answers": [$answers]}]""")
        assertTrue(message.contains("أكتر"))
    }

    @Test
    fun `a game draws its rounds from the imported bank without repeats`() {
        val questions = (1..12).map { index ->
            (QuestionBank.parse(
                """[{"text": "سؤال $index", "answers": [{"text": "أ", "points": 10}, {"text": "ب", "points": 5}]}]"""
            ) as BankResult.Success).questions.single().copy(id = "q$index")
        }

        val rounds = QuestionBank.randomGame(rounds = 4, source = questions)

        assertEquals(4, rounds.size)
        val ids = rounds.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
