package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val M_23_24 =
    object : Migration(23, 24) {
        override fun migrate(connection: SQLiteConnection) {
            // ---------------------------------------------------------
            // Create PassMetadata table
            // ---------------------------------------------------------
            connection.execSQL(
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
            connection.execSQL(
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
            connection.execSQL(
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
            connection.execSQL(
                """
                DROP INDEX IF EXISTS index_PassMetadata_groupId
                """.trimIndent(),
            )

            connection.execSQL(
                """
                CREATE INDEX index_PassMetadata_groupId ON PassMetadata(groupId)
                """.trimIndent(),
            )

            connection.execSQL(
                """
                CREATE INDEX index_PassMetadata_passId ON PassMetadata(passId)
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Copy data into the new Pass table
            // ---------------------------------------------------------
            connection.execSQL(
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
            connection.execSQL("DROP TABLE Pass")
            connection.execSQL("ALTER TABLE Pass_new RENAME TO Pass")
        }
    }
