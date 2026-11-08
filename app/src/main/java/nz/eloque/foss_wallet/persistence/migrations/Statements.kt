package nz.eloque.foss_wallet.persistence.migrations

import androidx.sqlite.SQLiteStatement

internal fun SQLiteStatement.getTextOrNull(index: Int): String? = if (isNull(index)) null else getText(index)

internal fun SQLiteStatement.bindTextOrNull(
    index: Int,
    value: String?,
) = if (value == null) bindNull(index) else bindText(index, value)
