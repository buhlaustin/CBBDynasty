package dev.novux.dynasty.cbb.core.save

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DynastyLoadFailureMapperTest {
    @Test
    fun fromReadException_tagsSqliteFailures() {
        val tagged = DynastyLoadFailure.fromReadException(
            SQLiteDatabaseCorruptException("database disk image is malformed"),
        )
        assertTrue(tagged.detail.startsWith("sqlite:"))
        assertTrue(tagged.detail.contains("malformed"))
    }

    @Test
    fun fromReadException_genericReadPrefix() {
        val failure = DynastyLoadFailure.fromReadException(IllegalStateException("boom"))
        assertEquals("read: boom", failure.detail)
    }

    private class SQLiteDatabaseCorruptException(message: String) : RuntimeException(message)
}
