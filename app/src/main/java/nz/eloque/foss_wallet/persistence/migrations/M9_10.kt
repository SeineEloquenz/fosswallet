package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val M_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `Pass_new` (
              `id` TEXT NOT NULL,
              `description` TEXT NOT NULL,
              `formatVersion` INTEGER NOT NULL,
              `organization` TEXT NOT NULL,
              `serialNumber` TEXT NOT NULL,
              `type` TEXT NOT NULL,
              `barCodes` TEXT NOT NULL,
              `addedAt` INTEGER NOT NULL DEFAULT 0,
              `hasLogo` INTEGER NOT NULL,
              `hasStrip` INTEGER NOT NULL,
              `hasThumbnail` INTEGER NOT NULL,
              `hasFooter` INTEGER NOT NULL,
              `deviceId` TEXT NOT NULL DEFAULT '2b767e5b-75fd-4bec-89d7-188e832b2dc3',
              `colors` TEXT,
              `relevantDate` INTEGER NOT NULL,
              `expirationDate` INTEGER NOT NULL,
              `logoText` TEXT,
              `authToken` TEXT,
              `webServiceUrl` TEXT,
              `passTypeIdentifier` TEXT,
              `locations` TEXT NOT NULL,
              `headerFields` TEXT NOT NULL,
              `primaryFields` TEXT NOT NULL,
              `secondaryFields` TEXT NOT NULL,
              `auxiliaryFields` TEXT NOT NULL,
              `backFields` TEXT NOT NULL,
              PRIMARY KEY(`id`)
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO Pass_new (
                id, description, formatVersion, organization, serialNumber,
                type, barCodes, addedAt, hasLogo, hasStrip, hasThumbnail, hasFooter,
                deviceId, colors, relevantDate, expirationDate, logoText, authToken,
                webServiceUrl, passTypeIdentifier, locations, headerFields, primaryFields,
                 secondaryFields, auxiliaryFields, backFields
            )
            SELECT 
                CAST(id AS TEXT), description, formatVersion, organization, serialNumber,
                type, barCodes, addedAt, hasLogo, hasStrip, hasThumbnail, hasFooter,
                deviceId, colors, relevantDate, expirationDate, logoText, authToken,
                webServiceUrl, passTypeIdentifier, locations, headerFields, primaryFields,
                 secondaryFields, auxiliaryFields, backFields
            FROM Pass
        """.trimIndent()
        )

        db.execSQL("DROP TABLE Pass")

        db.execSQL("ALTER TABLE Pass_new RENAME TO Pass")



        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `localization_new` (
              `passId` TEXT NOT NULL,
              `lang` TEXT NOT NULL,
              `label` TEXT NOT NULL,
              `text` TEXT NOT NULL,
              PRIMARY KEY(`passId`, `lang`, `label`),
              FOREIGN KEY(`passId`) REFERENCES `Pass`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL(
            """
            INSERT INTO localization_new (
                passId, lang, label, text
            )
            SELECT 
                CAST(passId AS TEXT), lang, label, text
            FROM localization
        """.trimIndent()
        )

        db.execSQL("DROP TABLE localization")

        db.execSQL("ALTER TABLE localization_new RENAME TO localization")
    }
}