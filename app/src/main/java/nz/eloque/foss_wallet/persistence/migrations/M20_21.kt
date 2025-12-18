package nz.eloque.foss_wallet.persistence.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@Suppress("ClassName")
@DeleteColumn(
    tableName = "Pass",
    columnName = "relevantDate"
)
class M20_21 : AutoMigrationSpec