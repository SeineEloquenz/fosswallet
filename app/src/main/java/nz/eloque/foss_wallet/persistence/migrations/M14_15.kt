package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
class M14_15 : AutoMigrationSpec {
    override fun onPostMigrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            UPDATE pass
            SET relevantDate = NULL
            WHERE relevantDate = 0
        """.trimIndent())
        db.execSQL("""
            UPDATE pass
            SET expirationDate = NULL
            WHERE expirationDate = 0
        """.trimIndent())
    }
}
