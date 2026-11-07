package nz.eloque.foss_wallet.model

import java.util.UUID

/** The PassKit web service of an updatable pass. */
data class PassWebService(
    val url: String,
    val authToken: String,
    val passTypeIdentifier: String,
    val serialNumber: String,
    val deviceId: UUID,
)
