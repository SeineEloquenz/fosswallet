package nz.eloque.foss_wallet.persistence.pass

import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassMetadata
import java.time.Instant

internal object AutoArchiver {
    fun shouldBeAutoArchived(
        pass: Pass,
        metadata: PassMetadata,
        previous: Pass? = null,
        now: Instant = Instant.now(),
    ): Boolean {
        if (metadata.archived) return true
        if (!metadata.autoArchive) return false
        if (pass.voided && previous?.voided == false) return true
        return endedWhileHeld(pass, now)
    }

    fun endedWhileHeld(
        pass: Pass,
        now: Instant = Instant.now(),
    ): Boolean {
        val end = pass.endsAt() ?: return false
        return !end.isAfter(now) && pass.addedAt.isBefore(end)
    }
}
