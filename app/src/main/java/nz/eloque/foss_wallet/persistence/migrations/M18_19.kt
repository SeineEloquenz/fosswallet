package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val M_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE Pass
            ADD COLUMN relevantDateTimePoints TEXT NOT NULL DEFAULT '[]'
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE Pass
            ADD COLUMN relevantDateTimeRanges TEXT NOT NULL DEFAULT '[]'
        """.trimIndent())
    }
}
