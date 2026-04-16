package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val M_23_24 =
    object : Migration(23, 24) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // ---------------------------------------------------------
            // Create PassMetadata table
            // ---------------------------------------------------------
            db.execSQL(
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
            db.execSQL(
                """
                INSERT INTO PassMetadata (passId, groupId, archived, autoArchive, renderLegacy)
                SELECT id, groupId, archived, autoArchive, renderLegacy
                FROM Pass
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Rebuild Pass table (remove moved columns)
            // ---------------------------------------------------------

            db.execSQL("ALTER TABLE Pass RENAME TO Pass_old")

            db.execSQL(
                """
                CREATE TABLE Pass (
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
            db.execSQL(
                """
                DROP INDEX IF EXISTS index_PassMetadata_groupId
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE INDEX index_PassMetadata_groupId ON PassMetadata(groupId)
                """.trimIndent(),
            )

            db.execSQL(
                """
                CREATE INDEX index_PassMetadata_passId ON PassMetadata(passId)
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Copy data back into new Pass table
            // ---------------------------------------------------------
            db.execSQL(
                """
                INSERT INTO Pass (
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
                FROM Pass_old
                """.trimIndent(),
            )

            // ---------------------------------------------------------
            // Drop old table
            // ---------------------------------------------------------
            db.execSQL("DROP TABLE Pass_old")
        }
    }
