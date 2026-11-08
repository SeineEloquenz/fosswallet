package nz.eloque.foss_wallet.persistence.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.async.executeSQL

val M_23_24 =
    object : Migration(23, 24) {
        override suspend fun migrate(connection: SQLiteConnection) {
            // ---------------------------------------------------------
            // Create PassMetadata table
            // ---------------------------------------------------------
            connection.executeSQL(
                """
                CREATE TABLE IF NOT EXISTS PassMetadata (
                    passId TEXT NOT NULL PRIMARY KEY,
                    groupId INTEGER,
                    archived INTEGER NOT NULL,
                    autoArchive INTEGER NOT NULL,
                    renderLegacy INTEGER NOT NULL,
                    FOREIGN KEY(passId) REFERENCES Pass(id) ON DELETE CASCADE,
                    FOREIGN KEY(groupId) REFERENCES PassGroup(id) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Backfill metadata from old Pass table
            // ---------------------------------------------------------
            connection.executeSQL(
                """
                INSERT INTO PassMetadata (passId, groupId, archived, autoArchive, renderLegacy)
                SELECT id, groupId, archived, autoArchive, renderLegacy
                FROM Pass
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Rebuild Pass table (remove moved columns)
            // ---------------------------------------------------------
            // Renaming Pass itself would make SQLite repoint PassMetadata's foreign key at the old table
            connection.executeSQL(
                """
                CREATE TABLE Pass_new (
                    id TEXT NOT NULL PRIMARY KEY,
                    description TEXT NOT NULL,
                    formatVersion INTEGER NOT NULL,
                    organization TEXT NOT NULL,
                    serialNumber TEXT NOT NULL,
                    type TEXT NOT NULL,
                    barCodes TEXT NOT NULL,
                    addedAt INTEGER NOT NULL,
                    hasLogo INTEGER NOT NULL,
                    hasStrip INTEGER NOT NULL,
                    hasThumbnail INTEGER NOT NULL,
                    hasFooter INTEGER NOT NULL,
                    deviceId TEXT NOT NULL,
                    colors TEXT,
                    relevantDates TEXT NOT NULL,
                    expirationDate TEXT,
                    logoText TEXT,
                    authToken TEXT,
                    webServiceUrl TEXT,
                    passTypeIdentifier TEXT,
                    locations TEXT NOT NULL,
                    headerFields TEXT NOT NULL,
                    primaryFields TEXT NOT NULL,
                    secondaryFields TEXT NOT NULL,
                    auxiliaryFields TEXT NOT NULL,
                    backFields TEXT NOT NULL
                )
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Recreate index (IMPORTANT: DROP first for schema match)
            // ---------------------------------------------------------
            connection.executeSQL(
                """
                DROP INDEX IF EXISTS index_PassMetadata_groupId
                """.trimIndent(),
            )

            connection.executeSQL(
                """
                CREATE INDEX index_PassMetadata_groupId ON PassMetadata(groupId)
                """.trimIndent(),
            )

            connection.executeSQL(
                """
                CREATE INDEX index_PassMetadata_passId ON PassMetadata(passId)
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Copy data into the new Pass table
            // ---------------------------------------------------------
            connection.executeSQL(
                """
                INSERT INTO Pass_new (
                    id,
                    description,
                    formatVersion,
                    organization,
                    serialNumber,
                    type,
                    barCodes,
                    addedAt,
                    hasLogo,
                    hasStrip,
                    hasThumbnail,
                    hasFooter,
                    deviceId,
                    colors,
                    relevantDates,
                    expirationDate,
                    logoText,
                    authToken,
                    webServiceUrl,
                    passTypeIdentifier,
                    locations,
                    headerFields,
                    primaryFields,
                    secondaryFields,
                    auxiliaryFields,
                    backFields
                )
                SELECT
                    id,
                    description,
                    formatVersion,
                    organization,
                    serialNumber,
                    type,
                    barCodes,
                    addedAt,
                    hasLogo,
                    hasStrip,
                    hasThumbnail,
                    hasFooter,
                    deviceId,
                    colors,
                    relevantDates,
                    expirationDate,
                    logoText,
                    authToken,
                    webServiceUrl,
                    passTypeIdentifier,
                    locations,
                    headerFields,
                    primaryFields,
                    secondaryFields,
                    auxiliaryFields,
                    backFields
                FROM Pass
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Replace the old table
            // ---------------------------------------------------------
            connection.executeSQL("DROP TABLE Pass")
            connection.executeSQL("ALTER TABLE Pass_new RENAME TO Pass")
        }
    }
