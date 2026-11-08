package nz.eloque.foss_wallet.model

import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class AuxiliaryRowsTest {
    private fun field(
        key: String,
        row: Int?,
    ) = PassField(key, null, PassContent.Plain(key), row = row)

    private fun pass(vararg fields: PassField) =
        Pass("id", "desc", 1, "org", "serial", PassType.Event, setOf(), addedAt = Instant.EPOCH, auxiliaryFields = fields.toList())

    private fun Pass.rowKeys() = auxiliaryRows().map { row -> row.map { it.key } }

    @Test
    fun `splits fields by row keeping their order`() {
        val pass = pass(field("a", 1), field("b", null), field("c", 0), field("d", 1))

        assertEquals(listOf(listOf("b", "c"), listOf("a", "d")), pass.rowKeys())
    }

    @Test
    fun `keeps a single row when no field sets one`() {
        assertEquals(listOf(listOf("a", "b")), pass(field("a", null), field("b", null)).rowKeys())
    }

    @Test
    fun `omits an empty first row`() {
        assertEquals(listOf(listOf("a")), pass(field("a", 1)).rowKeys())
    }

    @Test
    fun `has no rows without auxiliary fields`() {
        assertEquals(emptyList<List<String>>(), pass().rowKeys())
    }
}
