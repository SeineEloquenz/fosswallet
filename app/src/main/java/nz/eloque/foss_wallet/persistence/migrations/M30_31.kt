package nz.eloque.foss_wallet.persistence.migrations

import androidx.room3.DeleteColumn
import androidx.room3.migration.AutoMigrationSpec

@Suppress("ClassName")
@DeleteColumn(
    tableName = "PassMetadata",
    columnName = "renderLegacy",
)
class M30_31 : AutoMigrationSpec
