package test.utils

import kotlin.test.assertContains
import kotlin.test.assertEquals

fun assertErrors(expected: List<String>, message: String) {
    // asserts the number of "Expected" found in the string.
    assertEquals(
        expected.size + 1,
        message.split("Expected").size,
        message
    )
    expected.forEach { assertContains(message, it) }
}
