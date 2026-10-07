package nz.eloque.foss_wallet.ui

import androidx.test.core.app.ApplicationProvider
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.api.ImportOutcome
import nz.eloque.foss_wallet.api.ImportResult
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ImportOutcomeMessageTest {
    private val resources = ApplicationProvider.getApplicationContext<android.content.Context>().resources

    @Test
    fun `single imports report how the pass was added`() {
        assertMessage(R.string.pass_imported, ImportOutcome.Single("id", ImportResult.New))
        assertMessage(R.string.pass_already_imported, ImportOutcome.Single("id", ImportResult.Replaced))
        assertMessage(R.string.pass_imported_into_the_archive, ImportOutcome.Single("id", ImportResult.AutoArchived))
    }

    @Test
    fun `bundles report the number of imported passes`() {
        assertEquals(resources.getString(R.string.n_passes_imported, 3), ImportOutcome.Multiple(3).message(resources))
    }

    @Test
    fun `failed imports report why`() {
        assertMessage(R.string.invalid_pass_toast, ImportOutcome.Invalid)
        assertMessage(R.string.no_passes_found_in_file, ImportOutcome.Empty)
    }

    private fun assertMessage(
        expected: Int,
        outcome: ImportOutcome,
    ) {
        assertEquals(resources.getString(expected), outcome.message(resources))
    }
}
